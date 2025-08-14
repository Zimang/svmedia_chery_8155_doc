package com.desaysv.moduleusbmusic.glidev;

import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author uidq1846
 * @desc AudioCoverFetcher
 * @time 2022-11-29 21:21
 */
public class AudioCoverFetcher implements DataFetcher<InputStream> {
    private final AudioCoverModel model;
    private FileInputStream stream;

    AudioCoverFetcher(AudioCoverModel model) {
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(model.mediaPath);
            byte[] picture = retriever.getEmbeddedPicture();
            if (null != picture) {
                callback.onDataReady(new ByteArrayInputStream(picture));
            } else {
                callback.onDataReady(fallback(model.mediaPath));
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
    }

    @Override
    public void cleanup() {
        try {
            if (null != stream) {
                stream.close();
            }
        } catch (IOException ignore) {
        }
    }

    @Override
    public void cancel() {
        // cannot cancel
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

    public static final String[] FALLBACKS = {"cover.jpg", "album.jpg", "folder.jpg"};

    private InputStream fallback(String path) {
        File parent = new File(path).getParentFile();
        for (String fallback : FALLBACKS) {
            // TODO make it smarter by enumerating folder contents and filtering for files
            // example algorithm for that: http://askubuntu.com/questions/123612/how-do-i-set-album-artwork
            File cover = new File(parent, fallback);
            if (cover.exists()) {
                try {
                    return stream = new FileInputStream(cover);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
