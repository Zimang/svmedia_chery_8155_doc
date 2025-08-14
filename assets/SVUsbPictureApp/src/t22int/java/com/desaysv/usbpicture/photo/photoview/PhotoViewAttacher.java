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
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.OverScroller;

/**
 * The component of {@link PhotoView} which does the work allowing for zooming, scaling, panning, etc.
 * It is made public in case you need to subclass something other than AppCompatImageView and still
 * gain the functionality that {@link PhotoView} offers
 */
public class PhotoViewAttacher implements View.OnTouchListener,
        View.OnLayoutChangeListener {

    private static final String TAG = "PhotoViewAttacher";
    public static float DEFAULT_MAX_SCALE = 4.0f;
    public static float DEFAULT_MID_SCALE = 1.5f;
    public static float DEFAULT_MIN_SCALE = 1.0f;
    private static int DEFAULT_ZOOM_DURATION = 200;

    private static final int HORIZONTAL_EDGE_NONE = -1;
    private static final int HORIZONTAL_EDGE_LEFT = 0;
    private static final int HORIZONTAL_EDGE_RIGHT = 1;
    private static final int HORIZONTAL_EDGE_BOTH = 2;
    private static final int VERTICAL_EDGE_NONE = -1;
    private static final int VERTICAL_EDGE_TOP = 0;
    private static final int VERTICAL_EDGE_BOTTOM = 1;
    private static final int VERTICAL_EDGE_BOTH = 2;
    private static int SINGLE_TOUCH = 1;

    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private int mZoomDuration = DEFAULT_ZOOM_DURATION;
    private float mMinScale = DEFAULT_MIN_SCALE;
    private float mMidScale = DEFAULT_MID_SCALE;
    private float mMaxScale = DEFAULT_MAX_SCALE;

    private boolean mAllowParentInterceptOnEdge = true;
    private boolean mBlockParentIntercept = false;

    private ImageView mImageView;

    // Gesture Detectors
    private GestureDetector mGestureDetector;
    private CustomGestureDetector mScaleDragDetector;

    // These are set so we don't keep allocating them on the heap
    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();
    private final Matrix mSuppMatrix = new Matrix();
    private final RectF mDisplayRect = new RectF();
    private final float[] mMatrixValues = new float[9];

    // Listeners
    private OnMatrixChangedListener mMatrixChangeListener;
    private OnPhotoTapListener mPhotoTapListener;
    private OnOutsidePhotoTapListener mOutsidePhotoTapListener;
    private OnViewTapListener mViewTapListener;
    private View.OnClickListener mOnClickListener;
    private OnLongClickListener mLongClickListener;
    private OnScaleChangedListener mScaleChangeListener;
    private OnSingleFlingListener mSingleFlingListener;
    private OnViewDragListener mOnViewDragListener;

    private FlingRunnable mCurrentFlingRunnable;
    private int mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
    private int mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
    private float mBaseRotation;

    private boolean mZoomEnabled = true;
    private ScaleType mScaleType = ScaleType.FIT_CENTER;

    private boolean isSetScreen = false;

    //是否允许父View拦截触摸事件
    //为了保证双指缩放的功能，需要在双指缩放的情况下，禁止父View拦截触摸事件
    private boolean disallowIntercept = false;

    //上一个两指距离，用于判断两指是否不断变化，是的话属于缩放状态
    private double preDx = -1;
    private double preDy = -1;

    //最小的两指变化的距离，用于区分是正常滑动还是缩放滑动
    private static final int MIN_POINTER_DISTANCE = 5;

    //最小的缩放手指触摸点数(即最少两指触摸)
    private static final int MIN_POINTER_COUNT = 2;


    private OnGestureListener onGestureListener = new OnGestureListener() {
        @Override
        public void onDrag(float dx, float dy) {
            if (mScaleDragDetector.isScaling()) {
                return; // Do not drag if we are already scaling
            }
            if (mOnViewDragListener != null) {
                mOnViewDragListener.onDrag(dx, dy);
            }
            mSuppMatrix.postTranslate(dx, dy);
            checkAndDisplayMatrix();

            /*
             * Here we decide whether to let the ImageView's parent to start taking
             * over the touch event.
             *
             * First we check whether this function is enabled. We never want the
             * parent to take over if we're scaling. We then check the edge we're
             * on, and the direction of the scroll (i.e. if we're pulling against
             * the edge, aka 'overscrolling', let the parent take over).
             */
            ViewParent parent = mImageView.getParent();
            if (mAllowParentInterceptOnEdge && !mScaleDragDetector.isScaling() && !mBlockParentIntercept) {
                if (mHorizontalScrollEdge == HORIZONTAL_EDGE_BOTH
                        || (mHorizontalScrollEdge == HORIZONTAL_EDGE_LEFT && dx >= 1f)
                        || (mHorizontalScrollEdge == HORIZONTAL_EDGE_RIGHT && dx <= -1f)
                        || (mVerticalScrollEdge == VERTICAL_EDGE_TOP && dy >= 1f)
                        || (mVerticalScrollEdge == VERTICAL_EDGE_BOTTOM && dy <= -1f)) {
                    if (parent != null && !disallowIntercept) {
                        parent.requestDisallowInterceptTouchEvent(false);
                    }
                }
            } else {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
        }

        @Override
        public void onFling(float startX, float startY, float velocityX, float velocityY) {
            mCurrentFlingRunnable = new FlingRunnable(mImageView.getContext());
            mCurrentFlingRunnable.fling(getImageViewWidth(mImageView),
                    getImageViewHeight(mImageView), (int) velocityX, (int) velocityY);
            mImageView.post(mCurrentFlingRunnable);
        }

        @Override
        public void onScale(float scaleFactor, float focusX, float focusY) {
            Log.i(TAG, "onGestureListener onScale()");
            if (isZoomable()) {
                if (getScale() < mMaxScale || scaleFactor < 1f) {
                    if (mScaleChangeListener != null) {
                        mScaleChangeListener.onScaleChange(scaleFactor, focusX, focusY);
                    }
                    mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                    checkAndDisplayMatrix();
                }
                if (mScaleChangeListener != null) {
                    mScaleChangeListener.onScaleChange(scaleFactor, focusX, focusY);
                }
            }
        }
    };

    public PhotoViewAttacher(ImageView imageView) {
        mImageView = imageView;
        imageView.setOnTouchListener(this);
        imageView.addOnLayoutChangeListener(this);
        if (imageView.isInEditMode()) {
            return;
        }
        mBaseRotation = 0.0f;
        // Create Gesture Detectors...
        mScaleDragDetector = new CustomGestureDetector(imageView.getContext(), onGestureListener);
        mGestureDetector = new GestureDetector(imageView.getContext(), new GestureDetector.SimpleOnGestureListener() {

            // forward long click listener
            @Override
            public void onLongPress(MotionEvent e) {
                if (mLongClickListener != null) {
                    mLongClickListener.onLongClick(mImageView);
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                if (mSingleFlingListener != null) {
                    if (getScale() > DEFAULT_MIN_SCALE) {
                        return false;
                    }
                    if (e1.getPointerCount() > SINGLE_TOUCH
                            || e2.getPointerCount() > SINGLE_TOUCH) {
                        return false;
                    }
                    return mSingleFlingListener.onFling(e1, e2, velocityX, velocityY);
                }
                return false;
            }
        });
        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(mImageView);
                }
                final RectF displayRect = getDisplayRect();
                final float x = e.getX(), y = e.getY();
                if (mViewTapListener != null) {
                    mViewTapListener.onViewTap(mImageView, x, y);
                }
                if (displayRect != null) {
                    // Check to see if the user tapped on the photo
                    if (displayRect.contains(x, y)) {
                        float xResult = (x - displayRect.left)
                                / displayRect.width();
                        float yResult = (y - displayRect.top)
                                / displayRect.height();
                        if (mPhotoTapListener != null) {
                            mPhotoTapListener.onPhotoTap(mImageView, xResult, yResult);
                        }
                        return true;
                    } else {
                        if (mOutsidePhotoTapListener != null) {
                            mOutsidePhotoTapListener.onOutsidePhotoTap(mImageView);
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent ev) {//这里需要更改逻辑，需求图片可手动放大缩小，可双击放大（150%）和双击缩小（还原），最大可放大500%
                Log.i(TAG, "onDoubleTap()");
                try {
                    float scale = getScale();
                    float x = ev.getX();
                    float y = ev.getY();
                    if (scale < getMediumScale()) {
                        setScale(getMediumScale(), x, y, true);
                    } else if (scale >= getMediumScale() && scale < getMaximumScale()) {
                        setScale(getMaximumScale(), x, y, true);
                    } else {
                        setScale(getMinimumScale(), x, y, true);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Can sometimes happen when getX() and getY() is called
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                // Wait for the confirmed onDoubleTap() instead
                return false;
            }
        });
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener) {
        this.mGestureDetector.setOnDoubleTapListener(newOnDoubleTapListener);
    }

    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangeListener) {
        this.mScaleChangeListener = onScaleChangeListener;
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        this.mSingleFlingListener = onSingleFlingListener;
    }

    @Deprecated
    public boolean isZoomEnabled() {
        return mZoomEnabled;
    }

    public RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    public boolean setDisplayMatrix(Matrix finalMatrix) {
        if (finalMatrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }
        if (mImageView.getDrawable() == null) {
            return false;
        }
        mSuppMatrix.set(finalMatrix);
        checkAndDisplayMatrix();
        return true;
    }

    public void setBaseRotation(final float degrees) {
        mBaseRotation = degrees % 360;
        update();
        setRotationBy(mBaseRotation);
        checkAndDisplayMatrix();
    }

    public void setRotationTo(float degrees) {
        mSuppMatrix.setRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    public void setRotationBy(float degrees) {
        mSuppMatrix.postRotate(degrees % 360);
        checkAndDisplayMatrix();
        if (!isFullScreen) {
            setImageViewSizeScale();
        }
    }

    public float getMinimumScale() {
        return mMinScale;
    }

    public float getMediumScale() {
        return mMidScale;
    }

    public float getMaximumScale() {
        return mMaxScale;
    }

    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow
                (getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int
            oldRight, int oldBottom) {
        // Update our base matrix, as the bounds have changed
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {

            Log.d(TAG,"onLayoutChange,left:"+left + ",top:"+top+",right:"+right+",bottom:"+bottom);
            if (right == fullScreenWidth && bottom == fullScreenHeight){
                isFullScreen = true;
            }else {
                isFullScreen = false;
            }
            updateBaseMatrix(mImageView.getDrawable());
        }
    }

    /**
     * 判断是否处于双指缩放状态y
     * @param dx， 两指 x轴位置的距离
     * @param dy， 两指 y轴位置的距离
     * @return
     */
    private boolean isScaling(float dx, float dy){

        if (preDx != -1 && (Math.abs( dx -preDx) > MIN_POINTER_DISTANCE || Math.abs( dy -preDy) > MIN_POINTER_DISTANCE)){
            return true;
        }

        preDx = dx;
        preDy = dy;

        return false;
    }

    /**
     * 手势抬起时需要重置参数
     */
    private void resetParameters(){
        disallowIntercept = false;
        preDx = -1;
        preDy = -1;
    }


    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        boolean handled = false;
        if (Util.hasDrawable((ImageView) v)) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG,"getPointerCount:"+ev.getPointerCount());
                    if (ev.getPointerCount() >= MIN_POINTER_COUNT){//多点触控
                        //判断双指距离是否在变化
                        float dx = Math.abs(ev.getX(0) - ev.getX(1));
                        float dy = Math.abs(ev.getY(0) - ev.getY(1));
                        if (isScaling(dx,dy)){
                            disallowIntercept = true;
                        }else {
                            disallowIntercept = false;
                        }

                    }else {
                        disallowIntercept = false;
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    ViewParent parent = v.getParent();
                    // First, disable the Parent from intercepting the touch
                    // event
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    // If we're flinging, and the user presses down, cancel
                    // fling
                    cancelFling();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    // If the user has zoomed less than min scale, zoom back
                    // to min scale
                    if (ev.getPointerCount() < MIN_POINTER_COUNT){
                        resetParameters();
                    }
                    Log.i(TAG, "onTouch ACTION_UP() getScale()== " + getScale() + "###mMinScale == " + mMinScale + "###mMaxScale == " + mMaxScale);
                    if (getScale() < mMinScale) {
                        RectF rect = getDisplayRect();
                        if (rect != null) {
                            v.post(new AnimatedZoomRunnable(getScale(), mMinScale,
                                    rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    } else if (getScale() > mMaxScale) {
                        RectF rect = getDisplayRect();
                        if (rect != null) {
                            v.post(new AnimatedZoomRunnable(getScale(), mMaxScale,
                                    rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    }
                    break;
            }
            // Try the Scale/Drag detector
            if (mScaleDragDetector != null) {
                boolean wasScaling = mScaleDragDetector.isScaling();
                boolean wasDragging = mScaleDragDetector.isDragging();
                handled = mScaleDragDetector.onTouchEvent(ev);
                boolean didntScale = !wasScaling && !mScaleDragDetector.isScaling();
                boolean didntDrag = !wasDragging && !mScaleDragDetector.isDragging();
                mBlockParentIntercept = didntScale && didntDrag;
            }
            // Check to see if the user double tapped
            if (mGestureDetector != null && mGestureDetector.onTouchEvent(ev)) {
                handled = true;
            }

        }
        return handled;
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAllowParentInterceptOnEdge = allow;
    }

    public void setMinimumScale(float minimumScale) {
        Util.checkZoomLevels(minimumScale, mMidScale, mMaxScale);
        mMinScale = minimumScale;
    }

    public void setMediumScale(float mediumScale) {
        Util.checkZoomLevels(mMinScale, mediumScale, mMaxScale);
        mMidScale = mediumScale;
    }

    public void setMaximumScale(float maximumScale) {
        Util.checkZoomLevels(mMinScale, mMidScale, maximumScale);
        mMaxScale = maximumScale;
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        Util.checkZoomLevels(minimumScale, mediumScale, maximumScale);
        mMinScale = minimumScale;
        mMidScale = mediumScale;
        mMaxScale = maximumScale;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        mMatrixChangeListener = listener;
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mPhotoTapListener = listener;
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener mOutsidePhotoTapListener) {
        this.mOutsidePhotoTapListener = mOutsidePhotoTapListener;
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        mViewTapListener = listener;
    }

    public void setOnViewDragListener(OnViewDragListener listener) {
        mOnViewDragListener = listener;
    }

    public void setScale(float scale) {
        setScale(scale, false);
    }

    public void setScale(float scale, boolean animate) {
        setScale(scale,
                (mImageView.getRight()) / 2,
                (mImageView.getBottom()) / 2,
                animate);
    }

    public void setScale(float scale, float focalX, float focalY,
                         boolean animate) {
        // Check to see if the scale is within bounds
        if (scale < mMinScale || scale > mMaxScale) {
            return;
        }
        if (animate) {
            mImageView.post(new AnimatedZoomRunnable(getScale(), scale,
                    focalX, focalY));
        } else {
            mSuppMatrix.setScale(scale, scale, focalX, focalY);
            checkAndDisplayMatrix();
        }
    }

    /**
     * Set the zoom interpolator
     *
     * @param interpolator the zoom interpolator
     */
    public void setZoomInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public void setScaleType(ScaleType scaleType) {
        if (Util.isSupportedScaleType(scaleType) && scaleType != mScaleType) {
            mScaleType = scaleType;
            update();
        }
    }

    public boolean isZoomable() {
        return mZoomEnabled;
    }

    public void setZoomable(boolean zoomable) {
        mZoomEnabled = zoomable;
        update();
    }

    public void update() {
        if (mZoomEnabled) {
            // Update the base matrix using the current drawable
            updateBaseMatrix(mImageView.getDrawable());
        } else {
            // Reset the Matrix...
            resetMatrix();
        }
    }



    /**
     * Get the display matrix
     *
     * @param matrix target matrix to copy to
     */
    public void getDisplayMatrix(Matrix matrix) {
        matrix.set(getDrawMatrix());
    }

    /**
     * Get the current support matrix
     */
    public void getSuppMatrix(Matrix matrix) {
        matrix.set(mSuppMatrix);
    }

    private Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    public Matrix getImageMatrix() {
        return mDrawMatrix;
    }

    public void setZoomTransitionDuration(int milliseconds) {
        this.mZoomDuration = milliseconds;
    }

    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     *
     * @param matrix     Matrix to unpack
     * @param whichValue Which value from Matrix.M* to return
     * @return returned value
     */
    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    /**
     * Resets the Matrix back to FIT_CENTER, and then displays its contents
     */
    public void resetMatrix() {
        mSuppMatrix.reset();
        setRotationBy(mBaseRotation);
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();
    }

    private void setImageViewMatrix(Matrix matrix) {
        mImageView.setImageMatrix(matrix);
        // Call MatrixChangedListener if needed
        if (mMatrixChangeListener != null) {
            RectF displayRect = getDisplayRect(matrix);
            if (displayRect != null) {
                mMatrixChangeListener.onMatrixChanged(displayRect);
            }
        }
    }

    /**
     * Helper method that simply checks the Matrix, and then displays the result
     */
    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(getDrawMatrix());
        }
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     *
     * @param matrix - Matrix to map Drawable against
     * @return RectF - Displayed Rectangle
     */
    private RectF getDisplayRect(Matrix matrix) {
        Drawable d = mImageView.getDrawable();
        if (d != null) {
            mDisplayRect.set(0, 0, d.getIntrinsicWidth(),
                    d.getIntrinsicHeight());
            matrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    /**
     * Calculate Matrix for FIT_CENTER
     *
     * @param drawable - Drawable being displayed
     */
    private void updateBaseMatrix(Drawable drawable) {
        Log.i(TAG, "updateBaseMatrix()");
        if (drawable == null) {
            return;
        }
        final float viewWidth = getImageViewWidth(mImageView);
        final float viewHeight = getImageViewHeight(mImageView);
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();
        mBaseMatrix.reset();
        float widthScale = viewWidth / drawableWidth;
        float heightScale = viewHeight / drawableHeight;

        Log.d(TAG,"updateBaseMatrix,widthScale："+widthScale+",heightScale:"+heightScale);

        if (mScaleType == ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F,
                    (viewHeight - drawableHeight) / 2F);

        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);

        } else if (mScaleType == ScaleType.CENTER_INSIDE) {
            if (isFullScreen) {
                float viewWH = viewWidth / viewHeight;// ImageView 的宽高比，全屏时实际上是屏幕的宽高比
                float drawableWH = (float) drawableWidth / (float) drawableHeight;// Drawable 的宽高比，即要显示的图片的宽高比
                Log.d(TAG,"FullScreen viewWH: "+viewWH + ", drawableWH: "+drawableWH);
                float diffWH = Math.abs(viewWH - drawableWH);//ImageView的宽高比 和 显示的图片的宽高比 之间的误差
                Log.d(TAG,"FullScreen diffWH:"+diffWH);
                //最大的放大倍数和按钮的放大保持一致
                heightScale = Math.min(4,heightScale);
                widthScale = Math.min(4,widthScale);
                if (diffWH < 0.5){//小于0.5表示拥有相近的比例，那么直接缩放即可
                    //do nothing
                }else{//宽高比相差较大，优先满足高度铺满，需要等比例计算
                    if (heightScale < widthScale){//高度需要放大的倍数小于宽度需要的倍数
                        widthScale = heightScale;
                    }else {//高度需要放大的倍数大于宽度需要的倍数，则宽度铺满
                        heightScale = widthScale;
                    }
                }
                Log.d(TAG,"FullScreen widthScale: "+widthScale + ", heightScale: "+heightScale);
                mBaseMatrix.postScale(widthScale, heightScale);
                mBaseMatrix.postTranslate((viewWidth - drawableWidth * widthScale) / 2F,
                        (viewHeight - drawableHeight * heightScale) / 2F);
            }else {
                float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
                mBaseMatrix.postScale(scale, scale);
                mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                        (viewHeight - drawableHeight * scale) / 2F);
            }

        } else {
            RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
            RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);
            if ((int) mBaseRotation % 180 != 0) {
                mTempSrc = new RectF(0, 0, drawableHeight, drawableWidth);
            }
            switch (mScaleType) {
                case FIT_CENTER:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);
                    break;
                case FIT_START:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.START);
                    break;
                case FIT_END:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.END);
                    break;
                case FIT_XY:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.FILL);
                    break;
                default:
                    break;
            }
        }
        resetMatrix();
    }

    private boolean checkMatrixBounds() {
        final RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }
        final float height = rect.height(), width = rect.width();
        float deltaX = 0, deltaY = 0;
        final int viewHeight = getImageViewHeight(mImageView);
        if (height <= viewHeight) {
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rect.top;
                    break;
                case FIT_END:
                    deltaY = viewHeight - height - rect.top;
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rect.top;
                    break;
            }
            mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
        } else if (rect.top > 0) {
            mVerticalScrollEdge = VERTICAL_EDGE_TOP;
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            mVerticalScrollEdge = VERTICAL_EDGE_BOTTOM;
            deltaY = viewHeight - rect.bottom;
        } else {
            mVerticalScrollEdge = VERTICAL_EDGE_NONE;
        }
        final int viewWidth = getImageViewWidth(mImageView);
        if (width <= viewWidth) {
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rect.left;
                    break;
                case FIT_END:
                    deltaX = viewWidth - width - rect.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;
                    break;
            }
            mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
        } else if (rect.left > 0) {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_LEFT;
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mHorizontalScrollEdge = HORIZONTAL_EDGE_RIGHT;
        } else {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_NONE;
        }
        // Finally actually translate the matrix
        mSuppMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    private int getImageViewWidth(ImageView imageView) {
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    private int getImageViewHeight(ImageView imageView) {
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    private void cancelFling() {
        if (mCurrentFlingRunnable != null) {
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }

    private class AnimatedZoomRunnable implements Runnable {

        private final float mFocalX, mFocalY;
        private final long mStartTime;
        private final float mZoomStart, mZoomEnd;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom,
                                    final float focalX, final float focalY) {
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {
            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getScale();
            onGestureListener.onScale(deltaScale, mFocalX, mFocalY);
            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                Compat.postOnAnimation(mImageView, this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }
    }

    private class FlingRunnable implements Runnable {

        private final OverScroller mScroller;
        private int mCurrentX, mCurrentY;

        public FlingRunnable(Context context) {
            mScroller = new OverScroller(context);
        }

        public void cancelFling() {
            mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX,
                          int velocityY) {
            final RectF rect = getDisplayRect();
            if (rect == null) {
                return;
            }
            final int startX = Math.round(-rect.left);
            final int minX, maxX, minY, maxY;
            if (viewWidth < rect.width()) {
                minX = 0;
                maxX = Math.round(rect.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }
            final int startY = Math.round(-rect.top);
            if (viewHeight < rect.height()) {
                minY = 0;
                maxY = Math.round(rect.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }
            mCurrentX = startX;
            mCurrentY = startY;
            // If we actually can move, fling the scroller
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX,
                        maxX, minY, maxY, 0, 0);
            }
        }

        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return; // remaining post that should not be handled
            }
            if (mScroller.computeScrollOffset()) {
                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();
                mSuppMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
                checkAndDisplayMatrix();
                mCurrentX = newX;
                mCurrentY = newY;
                // Post On animation
                Compat.postOnAnimation(mImageView, this);
            }
        }
    }

    /**
     * 用于满足旋转之后，图片能够按照比例缩放（新增需求）
     * 其中，通过postScale来设置缩放，参数为缩放的比例
     * 20191120 add by liangzhan
     */
    private void setImageViewSizeScale() {
        if (mImageView != null) {
            Log.i(TAG, "setImageViewSizeScale()");
            //获取图片原来宽高
            Drawable drawable = mImageView.getDrawable();
            if (drawable == null) {
                return;
            }
            float srcHeight = drawable.getBounds().height();
            float srcWidth = drawable.getBounds().width();
            Log.d(TAG, "setImageViewSizeScale() mImageView.getRotationX()==" + mImageView.getRotationX() + "###srcHeight==" + srcHeight + "###srcWidth==" + srcWidth);
            //获取屏幕的宽高，因为mImageView已经设置了父布局的最大值，所以获取这个的宽高就是屏幕的宽高
            float screenHeight = mImageView.getHeight();
            float screenWidth = mImageView.getWidth();
            Log.d(TAG, "setImageViewSizeScale() screenHeight==" + screenHeight + "###screenWidth==" + screenWidth);
            //获取图片拉伸过后的宽高（旋转之前放大或者缩小了）
            RectF displayRect = getDisplayRect();
            float rectHeight = displayRect.height();
            float rectWidth = displayRect.width();
            Log.d(TAG, "setImageViewSizeScale() rectHeight==" + rectHeight + "###rectWidth==" + rectWidth);
            //先看拉伸后的关系
            float scale = getScale();
            Log.d(TAG, "setImageViewSizeScale() scale==" + scale);
            float newScale = DEFAULT_MIN_SCALE;
            if (screenHeight == 0 || rectHeight == 0) {
                return;
            }
            float rectWHscale = rectWidth / rectHeight;
            float screenWHscale = screenWidth / screenHeight;
            float srcWHscale = srcWidth / srcHeight;
            Log.d(TAG, "setImageViewSizeScale() screenWHscale == " + screenWHscale + "###rectWHscale == " + rectWHscale + "###srcWHscale ==" + srcWHscale);
            //每一次旋转之后，根据的都是rect实际缩放的宽高进行缩放
            if (rectWidth > screenWidth & rectHeight > screenHeight) {//拉伸后的宽高都大于屏幕宽高，根据宽高比例来决定转换的比例
                if (rectWHscale >= screenWHscale) {
                    newScale = screenWidth / rectWidth;
                } else {
                    newScale = screenHeight / rectHeight;
                }
            } else if ((rectHeight < screenHeight & rectWidth > screenWidth) || (rectHeight > screenHeight & rectWidth < screenWidth)) {//拉伸过后的图片宽或高大于屏幕宽高时
                if (rectWHscale >= screenWHscale) {
                    newScale = screenWidth / rectWidth;
                } else {
                    newScale = screenHeight / rectHeight;
                }
            } else {//当图片拉伸后的宽高都小于屏幕宽高时，根据原图片宽高和屏幕宽高情况进行处理，保持原来大小(这时由于竖直方向已经缩放，所以，得按照原资源大小进行缩放)
                if (srcHeight < screenHeight && srcWidth < screenWidth) {
                    if (rectWHscale > screenWHscale) {
                        newScale = srcWidth / rectWidth;
                    } else {
                        newScale = srcHeight / rectHeight;
                    }
                } else {
                    if (rectWHscale > screenWHscale) {
                        newScale = screenWidth / rectWidth;
                    } else {
                        newScale = screenHeight / rectHeight;
                    }
                }
            }
            Log.d(TAG, "setImageViewSizeScale() newScale 1==" + newScale);
            mSuppMatrix.postScale(newScale, newScale);//按照新的比例进行缩放
            if (Math.abs(rectWHscale - srcWHscale) <= 0.01) {//如果此时已经回正，则最小为默认
                newScale = DEFAULT_MIN_SCALE;
            } else {
                newScale = getScale();
            }
            Log.d(TAG, "setImageViewSizeScale() newScale 2==" + newScale);
            mMinScale = newScale;
            checkAndDisplayMatrix();
        }
    }

    private boolean isFullScreen = false;
    private int fullScreenWidth;
    private int fullScreenHeight;
    /**
     * 根据屏幕的实际大小，进行缩放处理
     * 否则全屏时无法做到全屏播放
     * @param width，全屏宽度
     * @param height，全屏高度
     */
    public void updateScreenParams(int width, int height){
        Log.d(TAG,"updateScreenParams,width: "+ width + ",height: "+height);
        fullScreenWidth = width;
        fullScreenHeight = height;
    }
}
