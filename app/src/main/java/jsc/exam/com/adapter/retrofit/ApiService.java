package jsc.exam.com.adapter.retrofit;

import io.reactivex.Observable;
import jsc.exam.com.adapter.BuildConfig;
import retrofit2.http.GET;

public interface ApiService {

    @GET(BuildConfig.VERSION_URL)
    Observable<String> getVersionInfo();

}
