package filnik.stargazers.model.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import filnik.stargazers.R;
import filnik.stargazers.model.User;
import io.realm.RealmResults;

/**
 * Created by fil on 05/02/17.
 */

class UserViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.user_id)
    TextView userId;
    @BindView(R.id.user_name) TextView userName;
    @BindView(R.id.user_avatar)
    ImageView userAvatar;

    UserViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }
}

public class UsersAdapter extends RecyclerView.Adapter {
    private final WeakReference<Context> contextRef;
    private RealmResults<User> usersDownloaded;

    public UsersAdapter(Context context, RealmResults<User> usersDownloaded) {
        this.contextRef = new WeakReference<Context>(context);
        this.usersDownloaded = usersDownloaded;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UserViewHolder myHolder = (UserViewHolder) holder;
        User user = usersDownloaded.get(position);
        myHolder.userId.setText(user.getId());
        myHolder.userName.setText(user.getLogin());
        if (contextRef.get() != null) {
            Ion.with(contextRef.get())
                    .load(user.getAvatarUrl())
                    .setTimeout(1000)
                    .intoImageView(myHolder.userAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return usersDownloaded == null ? 0 : usersDownloaded.size();
    }

    public void setNewUsersDownloaded(RealmResults<User> newUsersDownloaded) {
        this.usersDownloaded = newUsersDownloaded;
    }
}