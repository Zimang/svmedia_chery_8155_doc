package com.desaysv.usbpicture.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.usbpicture.R;

import java.io.File;

/**
 * created by ZNB on 2022-07-22
 * 加载图片的工具类
 */
public class ImageUtils {

    /**
     * 加载图片
     * @param context
     * @param path
     * @param imageView
     */
    public static void LoadThumbnail(Context context, String path, ImageView imageView){
        RequestOptions option = new RequestOptions()
                .transforms(new CenterInside(),new RoundedCorners(getCorner(context)))
                .bitmapTransform(new RoundedCorners(getCorner(context)))
                .error(R.mipmap.icon_default_picture);
        Glide.with(context)
                .load(Uri.fromFile(new File(path)))
                .apply(option)
                .into(imageView);
    }

    /**
     * 获取圆角图片的角度配置
     * @param context
     * @return
     */
    public static int getCorner(Context context){
        return context.getResources().getInteger(R.integer.corner);
    }

}
