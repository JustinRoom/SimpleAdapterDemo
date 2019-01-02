package jsc.exam.com.recycler.retrofit;

import io.reactivex.Observable;
import jsc.exam.com.recycler.BuildConfig;
import retrofit2.http.GET;

public interface ApiService {

    @GET(BuildConfig.VERSION_URL)
    Observable<String> getVersionInfo();

}
