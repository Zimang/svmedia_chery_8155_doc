package com.desaysv.videoapp.glidev;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelCache;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.io.InputStream;


/**
 *
 */
public class FileMessageGlideImageLoader extends BaseGlideUrlLoader<FileMessage> {
    private static final String TAG = "FileMessageGlideImageLoader";
    private final ModelCache<FileMessage, GlideUrl> modelCache;

    public FileMessageGlideImageLoader(ModelLoader<GlideUrl, InputStream> concreteLoader, @Nullable ModelCache<FileMessage, GlideUrl> modelCache) {
        super(concreteLoader, modelCache);
        this.modelCache = modelCache;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull FileMessage fileMessage, int width, int height, @NonNull Options options) {
        GlideUrl url;
        if (modelCache != null) {
            url = modelCache.get(fileMessage, 0, 0);
            if (url == null) {
                url = new GlideUrl(fileMessage.getPath());
                modelCache.put(fileMessage, 0, 0, url);
            }
        } else {
            url = new GlideUrl(fileMessage.getPath());
        }
        return new LoadData<>(url, new FileMessageGlideFetcher(fileMessage));
    }

    @Override
    protected String getUrl(FileMessage fileMessage, int width, int height, Options options) {
        return fileMessage.getPath();
    }

    @Override
    public boolean handles(@NonNull FileMessage fileMessage) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<FileMessage, InputStream> {
        private final ModelCache<FileMessage, GlideUrl> modelCache = new ModelCache<FileMessage, GlideUrl>(500);

        @NonNull
        @Override
        public ModelLoader<FileMessage, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            Log.d(TAG, "Factory build: ");
            return new FileMessageGlideImageLoader(multiFactory.build(GlideUrl.class, InputStream.class), modelCache);
        }

        @Override
        public void teardown() {

        }
    }
}
