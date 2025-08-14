package com.desaysv.videoapp.glidev;

import androidx.annotation.NonNull;
import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.io.InputStream;

/**
 * Create by extodc87 on 2023-2-3
 * Author: extodc87
 */
@GlideModule
public class VideoGlideModule extends AppGlideModule {

    private static final String TAG = "VideoGlideModule";

    @Override
    public void applyOptions(@NonNull Context context, GlideBuilder builder) {
        Log.d(TAG, "applyOptions: context: " + context + ", builder: " + builder);
//        builder.setIsActiveResourceRetentionAllowed(false);
        // http://events.jianshu.io/p/448c840f583c
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context).build();
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
        int defaultArrayPoolSize = calculator.getArrayPoolSizeInBytes();
        builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565));
        builder.setMemoryCache(new LruResourceCache(defaultMemoryCacheSize / 2));
        builder.setBitmapPool(new LruBitmapPool(defaultBitmapPoolSize / 2));
        builder.setArrayPool(new LruArrayPool(defaultArrayPoolSize / 2));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        Log.d(TAG, "registerComponents: ");
        registry.append(FileMessage.class, InputStream.class, new FileMessageGlideImageLoader.Factory());
//        registry.append(ID3StreamData.class, InputStream.class, new ID3GlideImageLoader.Factory());
//        registry.append(PhoneLinkInfo.class, InputStream.class, new PhoneLinkInfoGlideImageLoader.Factory());
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
