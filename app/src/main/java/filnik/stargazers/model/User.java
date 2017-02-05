package filnik.stargazers.model;

/**
 * Created by fil on 26/09/16.
 */

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {
    /*
    [
  {
    "login": "navaneet",
    "id": 2356187,
    "avatar_url": "https://avatars.githubusercontent.com/u/2356187?v=3",
    "gravatar_id": "",
    "url": "https://api.github.com/users/navaneet",
    "html_url": "https://github.com/navaneet",
    "followers_url": "https://api.github.com/users/navaneet/followers",
    "following_url": "https://api.github.com/users/navaneet/following{/other_user}",
    "gists_url": "https://api.github.com/users/navaneet/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/navaneet/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/navaneet/subscriptions",
    "organizations_url": "https://api.github.com/users/navaneet/orgs",
    "repos_url": "https://api.github.com/users/navaneet/repos",
    "events_url": "https://api.github.com/users/navaneet/events{/privacy}",
    "received_events_url": "https://api.github.com/users/navaneet/received_events",
    "type": "User",
    "site_admin": false
  },
     */

    @PrimaryKey
    private String id;
    private String login;
    private String avatar_url;

    public String getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getAvatarUrl() {
        return avatar_url;
    }
}
