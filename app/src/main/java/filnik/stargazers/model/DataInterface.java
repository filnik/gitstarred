package filnik.stargazers.model;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by fil on 25/09/16.
 */

public interface DataInterface {
    @GET("repos/{user}/{repository}/stargazers")
    Observable<List<User>> getStargazers(@Path("user") String user, @Path("repository") String repository);
}
