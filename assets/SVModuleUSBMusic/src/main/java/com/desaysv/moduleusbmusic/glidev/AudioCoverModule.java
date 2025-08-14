package com.desaysv.moduleusbmusic.glidev;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

/**
 * @author uidq1846
 * @desc AudioCoverModule
 * @time 2022-11-29 21:18
 */
@GlideModule
public class AudioCoverModule extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
        builder.setIsActiveResourceRetentionAllowed(true);
    }
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.append(AudioCoverModel.class, InputStream.class, new AudioCoverLoader.Factory());
    }
}
