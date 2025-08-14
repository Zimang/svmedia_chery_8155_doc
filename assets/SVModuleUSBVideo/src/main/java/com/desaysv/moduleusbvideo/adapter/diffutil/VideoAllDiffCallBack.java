package com.desaysv.moduleusbvideo.adapter.diffutil;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;
import java.util.Objects;

/**
 * 用于更新视频列表比较刷新adapter
 * Create by extodc87 on 2023-7-14
 * Author: extodc87
 */
public class VideoAllDiffCallBack extends DiffUtil.Callback {
    private List<FileMessage> oldList, newList;

    public void setData(List<FileMessage> oldList, List<FileMessage> newList) {
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
        FileMessage oldFileMessage = oldList.get(oldItemPosition);
        FileMessage newFileMessage = newList.get(newItemPosition);
        return oldFileMessage.getId() == newFileMessage.getId();
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
        FileMessage oldFileMessage = oldList.get(oldItemPosition);
        FileMessage newFileMessage = newList.get(newItemPosition);
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

    /**
     * 如果areItemTheSame()返回true，而areContentsTheSame()返回false，则DiffUtil会调用此方法来获取更改后的负载
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list
     * @return 默认null
     */
    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads)
        //  可以获取这里返回的数据
        // 如果要使用ItemAnimator，可以在实现此方法
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
