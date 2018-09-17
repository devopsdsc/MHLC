package red.point.checkpoint.di;

import android.content.Context;

import java.io.File;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class NetworkModule {

    @Provides
    public Cache providesCache(File cacheFile) {
        return new Cache(cacheFile, 10 * 1024 * 1024); // 10MB Cache
    }

    @Provides
    public File providesCacheFile(@ApplicationContextQualifier Context context) {
        File file = context.getCacheDir();

        if (!file.exists()) {
            file.mkdirs();
        }

        return new File(file, "okhttp_cache");
    }

    @Provides
    public OkHttpClient providesOkHttpClient(HttpLoggingInterceptor loggingInterceptor, Cache cache) {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .cache(cache)
                .build();
    }

    @Provides
    public HttpLoggingInterceptor providesLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return interceptor;
    }
}
