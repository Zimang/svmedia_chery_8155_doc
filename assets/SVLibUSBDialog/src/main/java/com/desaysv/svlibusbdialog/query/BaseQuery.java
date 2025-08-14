package com.desaysv.svlibusbdialog.query;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.desaysv.svlibusbdialog.constant.Constant;

import java.util.Arrays;

/**
 * 查询数据库的公共类
 */
public abstract class BaseQuery {

    private final String TAG = this.getClass().getSimpleName();

    //是否使用后缀判断不使用mimeType判断文件
    public boolean isDataType = true;

    private Context mContext;

    public BaseQuery(Context mContext) {
        this.mContext = mContext;
    }

    public void doInBackGround() {
        Cursor mCursor = null;
        ContentResolver resolver = mContext.getContentResolver();
        String selection = getQuerySelection();
        String[] args = getArgs();
        try {
            Log.d(TAG, "doInBackGround: selection = " + selection);
            Log.d(TAG, "doInBackGround: args = " + Arrays.toString(args));
            mCursor = resolver.query(getQueryUri(), null, selection, args, null);

            if (mCursor != null) {
                int scanState = getScanState();
                int count = mCursor.getCount();
                Log.d(TAG, "doInBackGround,scanState: " + scanState + ",count: " + count);
                if (Constant.Scanner.STATE_START == scanState) {//如果是扫描未结束的状态
                    if (count > 0) {
                        boolean realHavingData = false;
                        while (mCursor.moveToNext()) {
                            if (filter(mCursor,scanState)) {
                                Log.d(TAG, "filter,data = " + getData(mCursor));
                            } else {
                                realHavingData = true;
                                break;
                            }
                        }
                        if (realHavingData) {
                            notifyQueryState(Constant.Query.STATE_HAVING_DATA);
                        } else {
                            notifyQueryState(Constant.Query.STATE_HAVING_NO_DATA);
                        }
                    } else {
                        notifyQueryState(Constant.Query.STATE_HAVING_NO_DATA);
                    }
                } else if (Constant.Scanner.STATE_FINISH == scanState) {
                    if (count > 0) {
                        boolean realHadData = false;
                        while (mCursor.moveToNext()) {
                            if (filter(mCursor,scanState)) {
                                Log.d(TAG, "filter,data = " + getData(mCursor));
                            } else {
                                realHadData = true;
                                break;
                            }
                        }
                        if (realHadData) {
                            notifyQueryState(Constant.Query.STATE_HAD_DATA);
                        } else {
                            notifyQueryState(Constant.Query.STATE_HAD_NO_DATA);
                        }
                    } else {
                        notifyQueryState(Constant.Query.STATE_HAD_NO_DATA);
                    }
                }

            }
        } catch (IllegalArgumentException illegalArgumentException) {
            return;
        } finally {
            if(mCursor != null){
                mCursor.close();
            }
        }

    }

    protected boolean filter(Cursor cursor, int scanState) {
        return false;
    }

    protected abstract String getData(Cursor cursor);

    protected abstract Uri getQueryUri();

    protected abstract String getQuerySelection();

    protected abstract String[] getArgs();

    protected abstract String getPath();

    protected abstract int getScanState();

    protected abstract void notifyQueryState(int state);
}
