package com.desaysv.svlibfileoperation.dialog;

import android.content.Context;

import com.desaysv.svlibfileoperation.FileOperationManager;
import com.desaysv.svlibfileoperation.R;

public class DialogUtils {
    private static DialogUtils mInstance;

    public static DialogUtils getInstance(){
        if (mInstance == null){
            synchronized (DialogUtils.class){
                if (mInstance == null){
                    mInstance = new DialogUtils();
                }
            }
        }
        return mInstance;
    }

    private Context mContext;

    public void init(Context context){
        mContext = context;
    }

    private DeleteDialog deleteDialog;
    private ExportDialog exportDialog;

    /**
     * 更新删除Dialog
     * @param state
     * @param current
     * @param total
     */
    public void updateDeleteDialog(int state, int current, int total){
        if (deleteDialog == null){
            deleteDialog = new DeleteDialog(mContext, R.style.dialogstyle);
        }
        if (!deleteDialog.isShowing()){
            deleteDialog.show();
            deleteDialog.startAnimation();
        }else {
            if (state == FileOperationManager.OPERATION_STATE_CANCEL || state == FileOperationManager.OPERATION_STATE_SUCCESS){
                deleteDialog.dismiss();
                deleteDialog.stopAnimation();
            }
        }
    }

    /**
     * 更新导出Dialog
     * @param state
     * @param current
     * @param total
     */
    public void updateExportDialog(int state, int current, int total){
        if (exportDialog == null){
            exportDialog = new ExportDialog(mContext, R.style.dialogstyle);
        }
        if (state == FileOperationManager.OPERATION_STATE_PROGRESS){
            if (!exportDialog.isShowing()){
                exportDialog.show();
                exportDialog.startAnimation();
            }
            exportDialog.updateExporting(current,total);
        }else{
            exportDialog.dismiss();
            exportDialog.stopAnimation();
        }
    }
}
