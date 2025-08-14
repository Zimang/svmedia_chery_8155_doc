/*
 Copyright 2011, 2012 Chris Banes.
 <p>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p>
 http://www.apache.org/licenses/LICENSE-2.0
 <p>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.desaysv.usbpicture.photo.photoview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

import com.desaysv.usbpicture.bean.MessageBean;

//import org.greenrobot.eventbus.EventBus;


/**
 * A zoomable ImageView. See {@link PhotoViewAttacher} for most of the details on how the zooming
 * is accomplished
 */
@SuppressWarnings("unused")
public class PhotoView extends AppCompatImageView {

    /**
     * 图片 ，忽略计算 精度 范围
     */
    public static final float MAXIMUM_SCALE_PRECISION = 0.05f;

    private PhotoViewAttacher attacher;
    private ScaleType pendingScaleType;

    private IPhotoViewActionListener mActionListener;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    private void init() {
        attacher = new PhotoViewAttacher(this);
        //We always pose as a Matrix scale type, though we can change to another scale type
        //via the attacher
        super.setScaleType(ScaleType.MATRIX);
        //apply the previously applied scale type
        if (pendingScaleType != null) {
            setScaleType(pendingScaleType);
            pendingScaleType = null;
        }
    }

    /**
     * Get the current {@link PhotoViewAttacher} for this view. Be wary of holding on to references
     * to this attacher, as it has a reference to this view, which, if a reference is held in the
     * wrong place, can cause memory leaks.
     *
     * @return the attacher.
     */
    public PhotoViewAttacher getAttacher() {
        return attacher;
    }

    @Override
    public ScaleType getScaleType() {
        return attacher.getScaleType();
    }

    @Override
    public Matrix getImageMatrix() {
        return attacher.getImageMatrix();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        attacher.setOnLongClickListener(l);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        attacher.setOnClickListener(l);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (attacher == null) {
            pendingScaleType = scaleType;
        } else {
            attacher.setScaleType(scaleType);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        // setImageBitmap calls through to this method
        if (attacher != null) {
            attacher.forceUpdate();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (attacher != null) {
            attacher.forceUpdate();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (attacher != null) {
            attacher.forceUpdate();
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            attacher.update();
        }
        return changed;
    }

    public void setRotationTo(float rotationDegree) {
        attacher.setRotationTo(rotationDegree);
    }

    public void setRotationBy(float rotationDegree) {
        attacher.setRotationBy(rotationDegree);
    }

    public boolean isZoomable() {
        return attacher.isZoomable();
    }

    public void setZoomable(boolean zoomable) {
        attacher.setZoomable(zoomable);
    }

    public RectF getDisplayRect() {
        return attacher.getDisplayRect();
    }

    public void getDisplayMatrix(Matrix matrix) {
        attacher.getDisplayMatrix(matrix);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return attacher.setDisplayMatrix(finalRectangle);
    }

    public void getSuppMatrix(Matrix matrix) {
        attacher.getSuppMatrix(matrix);
    }

    public boolean setSuppMatrix(Matrix matrix) {
        return attacher.setDisplayMatrix(matrix);
    }

    public float getMinimumScale() {
        return attacher.getMinimumScale();
    }

    public float getMediumScale() {
        return attacher.getMediumScale();
    }

    public float getMaximumScale() {
        return attacher.getMaximumScale();
    }

    public float getScale() {
        return attacher.getScale();
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        attacher.setAllowParentInterceptOnEdge(allow);
    }

    public void setMinimumScale(float minimumScale) {
        attacher.setMinimumScale(minimumScale);
    }

    public void setMediumScale(float mediumScale) {
        attacher.setMediumScale(mediumScale);
    }

    public void setMaximumScale(float maximumScale) {
        attacher.setMaximumScale(maximumScale);
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        attacher.setScaleLevels(minimumScale, mediumScale, maximumScale);
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        attacher.setOnMatrixChangeListener(listener);
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        attacher.setOnPhotoTapListener(listener);
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener listener) {
        attacher.setOnOutsidePhotoTapListener(listener);
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        attacher.setOnViewTapListener(listener);
    }

    public void setOnViewDragListener(OnViewDragListener listener) {
        attacher.setOnViewDragListener(listener);
    }

    public void setScale(float scale) {
        attacher.setScale(scale);
    }

    public void setScale(float scale, boolean animate) {
        attacher.setScale(scale, animate);
    }

    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        attacher.setScale(scale, focalX, focalY, animate);
    }

    public void setZoomTransitionDuration(int milliseconds) {
        attacher.setZoomTransitionDuration(milliseconds);
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener onDoubleTapListener) {
        attacher.setOnDoubleTapListener(onDoubleTapListener);
    }

    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangedListener) {
        attacher.setOnScaleChangeListener(onScaleChangedListener);
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        attacher.setOnSingleFlingListener(onSingleFlingListener);
    }


    public void setOnActionListener(IPhotoViewActionListener listener) {
        mActionListener = listener;
    }


    public void resetMatrix(){
        attacher.resetMatrix();
//        EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_ENLARGE_PHOTO,false));
//        EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_NARROW_PHOTO,true));
        if (mActionListener !=null) {
            mActionListener.onScaleState(MessageBean.CAN_NOT_ENLARGE_PHOTO, false);
            mActionListener.onScaleState(MessageBean.CAN_NOT_NARROW_PHOTO, true);
        }
    }
    private float defauleScale=0.2f;
    public void enlargePhoto(){
        if (null != attacher) {
//            EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_NARROW_PHOTO,false));
            if (mActionListener != null) {
                mActionListener.onScaleState(MessageBean.CAN_NOT_NARROW_PHOTO, false);
            }
            float scale=getScale();
            scale+=defauleScale;
            if (scale>=getMaximumScale()){
                scale=getMaximumScale();
//                EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_ENLARGE_PHOTO,true));
                if (mActionListener != null) {
                    mActionListener.onScaleState(MessageBean.CAN_NOT_ENLARGE_PHOTO, true);
                }
            }
            setScale(scale,true);
        }
    }
    public void narrowPhoto(){
        if (null != attacher) {
//            EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_ENLARGE_PHOTO,false));
            if (mActionListener != null){
                mActionListener.onScaleState(MessageBean.CAN_NOT_ENLARGE_PHOTO,false);
            }
            float scale=getScale();
            scale -= defauleScale;
            Log.d("PhotoView","narrowPhoto origin = "+scale);
            // 图片可缩放的范围最小值不一定是1，以实际值为准，不然会出现缩放倍数越界的情况导致奔溃
            if (scale<=getMinimumScale()){
                scale=getMinimumScale();
            }
            Log.d("PhotoView","narrowPhoto final = "+scale);
            setScale(scale,true);
        }
    }
    public void doubleTapNarrowPhoto(){
        if (null != attacher) {
            float scale;
            scale=getMinimumScale();
            setScale(scale,true);
//            EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_NARROW_PHOTO,true));
//            EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_ENLARGE_PHOTO,false));
            if (mActionListener != null){
                mActionListener.onScaleState(MessageBean.CAN_NOT_NARROW_PHOTO,true);
                mActionListener.onScaleState(MessageBean.CAN_NOT_ENLARGE_PHOTO,false);
            }
        }
    }
    public void doubleTapEnlargePhoto(){
        if (null != attacher) {

            float scale;
            scale=getMaximumScale();
            setScale(scale,true);
//            EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_NARROW_PHOTO,false));
//            EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_ENLARGE_PHOTO,true));
            if (mActionListener != null){
                mActionListener.onScaleState(MessageBean.CAN_NOT_NARROW_PHOTO,false);
                mActionListener.onScaleState(MessageBean.CAN_NOT_ENLARGE_PHOTO,true);
            }
        }
    }
    public void doubleTapPhoto(){
        if (getScale() <= 1/*getMaximumScale() - PhotoView.MAXIMUM_SCALE_PRECISION > getScale()*/){//Bugclose2352
            doubleTapEnlargePhoto();
        }else {
            doubleTapNarrowPhoto();
        }
    }
    public void flipPhoto(){
        setRotationBy(90);
//        EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_ENLARGE_PHOTO,false));
//        EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_NARROW_PHOTO,true));
        if (mActionListener != null){
            mActionListener.onScaleState(MessageBean.CAN_NOT_ENLARGE_PHOTO,false);
            mActionListener.onScaleState(MessageBean.CAN_NOT_NARROW_PHOTO,true);
        }
    }

    /**
     * 根据屏幕的实际大小，进行缩放处理
     * 否则全屏时无法做到全屏播放
     * @param width，全屏宽度
     * @param height，全屏高度
     */
    public void updateScreenParams(int width, int height){
        attacher.updateScreenParams(width,height);
    }
}
