package com.desaysv.moduleusbvideo.adapter.diffutil;

import androidx.recyclerview.widget.DiffUtil;

import com.desaysv.moduleusbvideo.bean.FolderBean;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;
import java.util.Objects;

/**
 * 用于文件夹列表下adapter数据比较并更新差异
 * Create by extodc87 on 2023-7-14
 * Author: extodc87
 */
public class VideoFolderDiffCallBack extends DiffUtil.Callback {
    private List<FolderBean> oldList, newList;

    public void setData(List<FolderBean> oldList, List<FolderBean> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    /**
     * 判断新旧列表两个位置的项目是否相同
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list
     * @return 如果两个项目表示同一对象，则为 true;如果它们不同，则为 false。
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        FolderBean oldFolder = oldList.get(oldItemPosition);
        FolderBean newFolder = newList.get(newItemPosition);
        return Objects.equals(oldFolder.isFolder(), newFolder.isFolder());
    }

    /**
     * 判断新旧列表两个位置的项目数据是否相同，DiffUtil只会在areItemsTheSame()返回true时才调用此方法。
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list which replaces the
     *                        oldItem
     * @return 如果项目的内容相同，则为 true;如果项目内容不同，则为 false。
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        FolderBean oldFolder = oldList.get(oldItemPosition);
        FolderBean newFolder = newList.get(newItemPosition);
        if (oldFolder.isFolder() != newFolder.isFolder()) {
            return false;
        }
        if (!Objects.equals(oldFolder.getFolderPath(), newFolder.getFolderPath())) {
            return false;
        }
        if (!Objects.equals(oldFolder.getFolderTitle(), newFolder.getFolderTitle())) {
            return false;
        }
        FileMessage oldFileMessage = oldFolder.getVideo();
        FileMessage newFileMessage = newFolder.getVideo();
        if (!Objects.equals(oldFileMessage.getName(), newFileMessage.getName())) {
            return false;
        }
        if (!Objects.equals(oldFileMessage.getFileName(), newFileMessage.getFileName())) {
            return false;
        }
        if (!Objects.equals(oldFileMessage.getPath(), newFileMessage.getPath())) {
            return false;
        }
        return true;
    }
}
