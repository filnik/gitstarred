package filnik.stargazers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.koushikdutta.ion.Ion;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import filnik.stargazers.model.User;
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
    @BindView(R.id.refresh_layout) RecyclerRefreshLayout refreshLayout;

    private static final String TAG = "stargazers";
    private UsersAdapter usersAdapter;
    Retrofit retrofit;
    private Realm realm;
    private RealmResults<User> postsDownloaded;

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

        refreshLayout.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadData();
            }
        });

        RecyclerView recycleView = (RecyclerView) findViewById(R.id.recycler_view);
        recycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        usersAdapter = new UsersAdapter();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
        if (realm.where(User.class).count() > 0){
            postsDownloaded = realm.where(User.class).findAll();
            Log.d(TAG, "Using cached data...");
        }

        recycleView.setLayoutManager(new LinearLayoutManager(this));
        recycleView.setAdapter(usersAdapter);

        if (postsDownloaded == null) {
            downloadData();
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

    private void downloadData() {
        if (nickname.getText().equals("") || repository.getText().equals("")){
            return;
        }
        DataInterface service = retrofit.create(DataInterface.class);
        Observable<List<User>> postsObservable = service.getStargazers(nickname.getText().toString(), repository.getText().toString());

        Log.d(TAG, "Downloading data...");

        postsObservable.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<List<User>>() {

               @Override
               public void onCompleted() {
                   Toast.makeText(MainActivity.this, R.string.download_done, Toast.LENGTH_SHORT).show();
               }

               @Override
               public void onError(Throwable e) {
                   e.printStackTrace();
                   refreshLayout.setRefreshing(false);
               }

               @Override
               public void onNext(List<User> users) {
                   Log.d(TAG, "Server response:");
                   for (User user : users) {
                       Log.d(TAG, user.getId() + ": " + user.getLogin() + " - " + user.getAvatarUrl());
                   }

                   realm.beginTransaction();
                   if (postsDownloaded == null) {
                       realm.where(User.class).findAll().deleteAllFromRealm();
                   } else {
                       postsDownloaded.deleteAllFromRealm();
                   }
                   realm.copyToRealm(users);
                   realm.commitTransaction();

                   postsDownloaded = realm.where(User.class).findAll();
                   refreshLayout.setRefreshing(false);
                   usersAdapter.notifyDataSetChanged();
               }
           });
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_id) TextView userId;
        @BindView(R.id.user_name) TextView userName;
        @BindView(R.id.user_avatar) ImageView userAvatar;

        public PostViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private class UsersAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_user, parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            PostViewHolder myHolder = (PostViewHolder) holder;
            User user = postsDownloaded.get(position);
            myHolder.userId.setText(user.getId());
            myHolder.userName.setText(user.getLogin());
            Ion.with(getApplicationContext())
                    .load(user.getAvatarUrl())
                    .setTimeout(1000)
                    .intoImageView(myHolder.userAvatar);
        }

        @Override
        public int getItemCount() {
            return postsDownloaded == null ? 0 : postsDownloaded.size();
        }
    }
}
