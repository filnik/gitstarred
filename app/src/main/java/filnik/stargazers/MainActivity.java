package filnik.stargazers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.mobsandgeeks.saripaar.Validator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import filnik.stargazers.model.User;
import filnik.stargazers.model.adapter.UsersAdapter;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import filnik.stargazers.model.DataInterface;
import filnik.stargazers.model.Model;
import filnik.stargazers.view.DividerItemDecoration;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity {
    @BindView(R.id.nickname) TextView nickname;
    @BindView(R.id.repository) TextView repository;
    @BindView(R.id.download_button) Button downloadButton;
    @BindView(R.id.refresh_layout) RecyclerRefreshLayout refreshLayout;
    @BindView(R.id.idLayContent) LinearLayout layContent;
    @BindView(R.id.idBusy) RelativeLayout busyId;

    private static final String TAG = "stargazers";
    private UsersAdapter usersAdapter;
    Retrofit retrofit;
    private Realm realm;
    private RealmResults<User> usersDownloaded;
    private SharedPreferences settings;

    protected Validator validator = new Validator(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stargazers);
        ButterKnife.bind(this);

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(Model.GITHUB_REPO_URL)
                .build();

        setupRealm();
        bindView();

        if (usersDownloaded == null) {
            downloadData();
        }
    }

    private void setupRealm(){
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
        if (realm.where(User.class).count() > 0){
            usersDownloaded = realm.where(User.class).findAll();
            Log.d(TAG, "Using cached data...");
        }
    }

    private void bindView(){
        usersAdapter = new UsersAdapter(getApplicationContext(), usersDownloaded);
        refreshLayout.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadData();
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadData();
            }
        });

        RecyclerView recycleView = (RecyclerView) findViewById(R.id.recycler_view);
        recycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        recycleView.setLayoutManager(new LinearLayoutManager(this));
        recycleView.setAdapter(usersAdapter);

        settings = getSharedPreferences(Model.SHARED_PREF, MODE_PRIVATE);
        String nicknameSavedValue = settings.getString("nickname", "");
        String repositorySavedValue = settings.getString("repository", "");

        nickname.setText(nicknameSavedValue);
        repository.setText(repositorySavedValue);
    }

    public void setBusy(boolean isBusy) {
        if (layContent == null || busyId == null){
            return;
        }
        if (isBusy) {
            layContent.setVisibility(View.GONE);
            busyId.setVisibility(View.VISIBLE);
        } else {
            layContent.setVisibility(View.VISIBLE);
            busyId.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh){
            downloadData();
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePreferences(){
        SharedPreferences.Editor edit = settings.edit();
        edit.putString("nickname", nickname.getText().toString());
        edit.putString("repository", repository.getText().toString());
        edit.apply();
    }

    private void downloadData() {
        if (nickname.getText().equals("") || repository.getText().equals("")){
            return;
        }
        setBusy(true);

        savePreferences();

        DataInterface service = retrofit.create(DataInterface.class);
        Observable<List<User>> postsObservable = service.getStargazers(nickname.getText().toString(), repository.getText().toString());

        Log.d(TAG, "Downloading data for nickname: " + nickname.getText() + ", repository: " + repository.getText() + "...");

        postsObservable.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<List<User>>() {

               @Override
               public void onCompleted() {
                   Log.d(TAG, "Download completed");
                   Toast.makeText(MainActivity.this, R.string.download_done, Toast.LENGTH_SHORT).show();
                   setBusy(false);
               }

               @Override
               public void onError(Throwable e) {
                   Toast.makeText(MainActivity.this, R.string.error_downloading, Toast.LENGTH_SHORT).show();
                   e.printStackTrace();
                   refreshLayout.setRefreshing(false);
                   setBusy(false);
               }

               @Override
               public void onNext(List<User> users) {
                   Log.d(TAG, "Server response:");
                   for (User user : users) {
                       Log.d(TAG, user.getId() + ": " + user.getLogin() + " - " + user.getAvatarUrl());
                   }

                   realm.beginTransaction();
                   if (usersDownloaded == null) {
                       realm.where(User.class).findAll().deleteAllFromRealm();
                   } else {
                       usersDownloaded.deleteAllFromRealm();
                   }
                   realm.copyToRealm(users);
                   realm.commitTransaction();

                   usersDownloaded = realm.where(User.class).findAll();
                   refreshLayout.setRefreshing(false);
                   usersAdapter.setNewUsersDownloaded(usersDownloaded);
                   usersAdapter.notifyDataSetChanged();
               }
           });
    }
}
