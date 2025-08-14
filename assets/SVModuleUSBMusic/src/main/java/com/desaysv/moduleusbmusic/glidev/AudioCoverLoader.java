package com.desaysv.moduleusbmusic.glidev;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

/**
 * @author uidq1846
 * @desc AudioCoverLoader
 * @time 2022-11-29 21:19
 */
public class AudioCoverLoader implements ModelLoader<AudioCoverModel, InputStream> {
    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull AudioCoverModel audioCoverModel, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(audioCoverModel), new AudioCoverFetcher(audioCoverModel));
    }

    @Override
    public boolean handles(@NonNull AudioCoverModel audioCoverModel) {
        return true;
    }

    static class Factory implements ModelLoaderFactory<AudioCoverModel, InputStream> {
        @NonNull
        @Override
        public ModelLoader<AudioCoverModel, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new AudioCoverLoader();
        }

        @Override
        public void teardown() {

        }
    }
}
