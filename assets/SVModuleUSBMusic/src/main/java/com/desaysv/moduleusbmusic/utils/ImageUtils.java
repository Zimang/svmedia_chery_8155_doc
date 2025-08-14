package com.desaysv.moduleusbmusic.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

/**
 * Created by uidq1846 on 2019-9-4
 * 图片显示工具
 */
public class ImageUtils {
    private static final String TAG = "ImageUtils";
    @SuppressLint("StaticFieldLeak")
    private static ImageUtils mImageUtils;
    private Context mContext;
    private RequestManager requestManager;

    private ImageUtils() {
    }

    public static ImageUtils getInstance() {
        if (mImageUtils == null) {
            synchronized (ImageUtils.class) {
                if (mImageUtils == null) {
                    mImageUtils = new ImageUtils();
                }
            }
        }
        return mImageUtils;
    }

    public void init(Context context) {
        if (mContext == null) {
            mContext = context;
        }
        requestManager = Glide.with(mContext);
    }

    /**
     * 加载图片的工具
     *
     * @param imageView         视图
     * @param path              路径
     * @param defaultResourceId 默认显示图
     */
    @SuppressLint("CheckResult")
    public void showImage(final ImageView imageView, Uri path, @DrawableRes final int defaultResourceId, @DrawableRes final int errorResourceId) {
        //设置占位图
        RequestOptions requestOptions = new RequestOptions();
        //图片加载完成前
        requestOptions.placeholder(defaultResourceId);
        //图片加载失败
        requestOptions.error(errorResourceId);
        requestManager.setDefaultRequestOptions(requestOptions).load(path).transition(DrawableTransitionOptions.withCrossFade()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                Log.d(TAG, "showImage() onLoadFailed!!!");
                //imageView.setImageResource(errorResourceId);
                //requestOptions already set error errorResourceId，do noting here.
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(imageView);
    }

    /**
     * 加载图片的工具
     *
     * @param imageView         视图
     * @param albumPic          路径
     * @param defaultResourceId 默认显示图
     */
    @SuppressLint("CheckResult")
    public void showImage(final ImageView imageView, byte[] albumPic, @DrawableRes final int defaultResourceId, @DrawableRes final int errorResourceId) {
        //设置占位图
        RequestOptions requestOptions = new RequestOptions();
        //图片加载完成前
        requestOptions.placeholder(defaultResourceId);
        //图片加载失败
        requestOptions.error(errorResourceId);
        requestManager.setDefaultRequestOptions(requestOptions).load(albumPic).transition(DrawableTransitionOptions.withCrossFade()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                Log.d(TAG, "showImage() onLoadFailed!!!");
                imageView.setImageResource(errorResourceId);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(imageView);
    }

    /**
     * 展示图片的方式
     *
     * @param imageView imageView
     * @param path      path
     */
    public void showImage(ImageView imageView, String path) {
        requestManager.load(path).into(imageView);
    }
}
