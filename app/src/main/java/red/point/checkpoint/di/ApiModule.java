package red.point.checkpoint.di;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.nio.charset.Charset;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import red.point.checkpoint.BuildConfig;
import red.point.checkpoint.MyApplication;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainActivity;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module(includes = {NetworkModule.class, ContextModule.class})
public class ApiModule {

    private static final String TAG = ApiModule.class.getSimpleName();

    private static final String BASE_URL = BuildConfig.API_URL;

    @Inject
    public PrefManager PrefManager;

    @Provides
    public OkHttpClient.Builder providesHttpClient() {
        return new OkHttpClient().newBuilder();
    }

    @Provides
    public Retrofit.Builder providesBuilder() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create());
    }

    @Provides
    public Retrofit retrofit(Retrofit.Builder builder,
                             OkHttpClient.Builder httpClient,
                             @Named("header_interceptor") Interceptor headerInterceptor,
                             @Named("error_handler_interceptor") Interceptor errorHandlerInterceptor) {
        httpClient.addInterceptor(headerInterceptor);
        httpClient.addInterceptor(errorHandlerInterceptor);
        builder.client(httpClient.build());

        return builder.build();
    }

    @Provides
    @Named("header_interceptor")
    public Interceptor providesHeaderInterceptor(@Named("authorized_token") String authorizedToken) {
        return chain -> {
            Request request = chain.request()
                    .newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authorizedToken)
                    .build();

            return chain.proceed(request);
        };
    }

    @Provides
    public Cache providesCache() {
        int cacheSize = 10 * 1024 * 1024;
        File httpCacheDirectory = new File(MyApplication.getCacheDirectory(), "responses");
        return new Cache(httpCacheDirectory, cacheSize);
    }

    @Provides
    @Named("cache_interceptor")
    public Interceptor providesCacheInterceptor() {
        return chain -> {
            Request request = chain.request();
            if (MyApplication.isNetworkConnected()) {
                request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60 * 60 * 24 * 7).build();
            } else {
                request = request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build();
            }

            return chain.proceed(request);
        };
    }

    @Provides
    @Named("error_handler_interceptor")
    public Interceptor providesErrorHandlerInterceptor() {
        return chain -> {
            Request request = chain.request();
            okhttp3.Response response = chain.proceed(request);
            ResponseBody body = response.body();

            String errorMessage = "";
            Integer errorCode = null;
            JsonObject error;

            // Only intercept JSON type responses and ignore the rest.
            if (body != null
                    && body.contentType() != null
                    && body.contentType().subtype() != null
                    && body.contentType().subtype().toLowerCase().equals("json")) {
                errorCode = 200;
                try {
                    BufferedSource source = body.source();
                    source.request(Long.MAX_VALUE); // Buffer the entire body.
                    Buffer buffer   = source.buffer();
                    Charset charset = body.contentType().charset(Charset.forName("UTF-8"));

                    // Clone the existing buffer is they can only read once so we still want to pass the original one to the chain.
                    String json = buffer.clone().readString(charset);
                    JsonElement obj = new JsonParser().parse(json);

                    // Capture error code an message.
                    if (obj instanceof JsonObject && ((JsonObject) obj).has("error")) {
                        error = ((JsonObject) obj).get("error").getAsJsonObject();
                        if (error != null && error.has("code")) {
                            errorCode = error.get("code").getAsInt();
                        }
                        if (error != null && error.has("message")) {
                            errorMessage = error.get("message").getAsString();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error: " + e.getMessage());
                }
            }

            if (errorCode == null) {
                ToastUtil.showConnectionFailure();
            } else {
                // Return user to login activity when user not authenticated
                if (errorCode == 401) {
                    if (MyApplication.getInstance().getActivity() != null && MyApplication.getInstance().getActivity() instanceof MainActivity) {
                        ((MainActivity) MyApplication.getInstance().getActivity()).logout();
                    }
                }

                if (errorMessage != null && !errorMessage.trim().equals("") && errorCode >= 400) {
                    ToastUtil.show(errorMessage);
                }
            }

            return response;
        };
    }

    @Provides
    @Named("authorized_token")
    public String providesAuthorizedToken(PrefManager prefManager) {
        return prefManager.getToken();
    }
}
