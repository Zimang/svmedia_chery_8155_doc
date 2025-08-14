package com.desaysv.usbpicture.fragment;

/**
 * 按照分工，“相册”由雄狮实现，这个Fragment也由雄狮提供，因此这部分不再维护
 * 顶部操作栏仍旧放在MainActivity，通过“相册”提供的接口进行对应操作
 */

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.querypicture.QueryManager;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.manager.AlbumListManager;
import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.adapter.AlbumAdapter;
import com.desaysv.usbpicture.ui.AlbumActivity;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;

public class AlbumFragment extends Fragment implements AlbumAdapter.IClickListener{
    private static final String TAG = "AlbumFragment";

    private RecyclerView rv_album_list;
    private RelativeLayout rl_no_album;
    private AlbumAdapter albumAdapter;
    private ISelectChange listener;

    private RelativeLayout rl_album_loading;
    private ImageView iv_album_loading;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ISelectChange){
            listener = (ISelectChange) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResource(),null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
        initViewListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart()");
        AlbumListManager.getInstance().attachObserver(albumObserver);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop()");
        AlbumListManager.getInstance().detachObserver(albumObserver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG,"onDestroyView()");
        if (loadingAnimator != null){
            loadingAnimator.cancel();
            loadingAnimator = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private int getLayoutResource(){
        return R.layout.layout_album_fragment;
    }

    private void initView(View view){
        rv_album_list = view.findViewById(R.id.rv_album_list);
        rl_no_album = view.findViewById(R.id.rl_no_album);
//        rv_album_list.addItemDecoration(new SpaceItemDecoration(0, 0));
        rv_album_list.setLayoutManager( new GridLayoutManager(getContext(), 5));
        albumAdapter = new AlbumAdapter(getContext(),this);
        rv_album_list.setAdapter(albumAdapter);
        myHandler = new MyHandler(this);

        rl_album_loading = view.findViewById(R.id.rl_album_loading);
        iv_album_loading = view.findViewById(R.id.iv_album_loading);
    }

    private boolean isSlideUp = false;//是否向上滑动
    private void initViewListener(){
        rv_album_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastItem = ((GridLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                if (isSlideUp){
                    if (lastItem == albumAdapter.getItemCount() - 1){
                        if (AlbumListManager.getInstance().getAlbumList().size() > albumAdapter.getItemCount() - 1) {
                            albumAdapter.setLoadState(AlbumAdapter.STATE_LOADING);
                        }else {
                            albumAdapter.setLoadState(AlbumAdapter.STATE_LOADED);
                        }
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isSlideUp = (dy > 0);
            }
        });
    }


    /**
     * 启动数据查询
     */
    public void initData(){
        QueryManager.getInstance().startQueryAlbum();
        startLoadingAnimation(true);
    }


    /**
     * 更新数据
     */
    public void updateData(){
        albumAdapter.resetSelectMode();
        QueryManager.getInstance().startQueryAlbum();
    }

    /**
     * 更新数据
     */
    public void updateList(){
        albumAdapter.updateList(AlbumListManager.getInstance().getAlbumList());
        startLoadingAnimation(false);
        if (AlbumListManager.getInstance().getAlbumList().size() < 1){
            rl_no_album.setVisibility(View.VISIBLE);
            rv_album_list.setVisibility(View.GONE);
        }else {
            rl_no_album.setVisibility(View.GONE);
            rv_album_list.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 开始/结束 Loading动画
     * @param animate
     */
    private ObjectAnimator loadingAnimator;
    private void startLoadingAnimation(boolean animate){
        Log.d(TAG,"startLoadingAnimation: "+animate);
        if (loadingAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingAnimator = ObjectAnimator.ofFloat(iv_album_loading, "rotation", 0f, 360f);
            loadingAnimator.setDuration(1000);
            loadingAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        if (animate) {
            rl_album_loading.setVisibility(View.VISIBLE);
            if (!loadingAnimator.isStarted()) {
                loadingAnimator.start();
            }
        }else {
            rl_album_loading.setVisibility(View.GONE);
            if (loadingAnimator.isStarted()) {
                loadingAnimator.end();
            }
        }
    }


    private Observer albumObserver = new Observer() {
        @Override
        public void onUpdate() {
            myHandler.sendEmptyMessage(MSG_UPDATE_LIST);
        }
    };

    @Override
    public void onClick(int position) {
        //进入预览模式
        Intent intent = new Intent(getContext(), AlbumActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlbumActivity.INTENT_POSITION, position);
        getContext().startActivity(intent);
    }

    @Override
    public void onUpdateSelect() {
        //更新选中数据
        if (listener != null){
            listener.onSelectChange();
        }
    }


    /**
     * 获取所有列表
     * @return
     */
    public CopyOnWriteArrayList<FileMessage> getAllList(){
        return AlbumListManager.getInstance().getAlbumList();
    }


    /**
     * 获取选中状态的列表
     * @return
     */
    public CopyOnWriteArrayList<FileMessage> getSelectList(){
        return albumAdapter.getSelectList();
    }


    /**
     * 点击进入编辑模式
     * @param selectMode
     */
    public void handleSelectMode(boolean selectMode){
        Log.d(TAG,"handleSelectMode:"+selectMode);
        albumAdapter.setSelectMode(selectMode);
    }

    /**
     * 点击全选按钮
     * @param selectAll
     */
    public void handleSelectAll(boolean selectAll){
        albumAdapter.updateAllSelect(selectAll);
        onUpdateSelect();
    }

    /**
     * 删除完成后更新UI
     */
    public void handleDelete(){
        albumAdapter.notifyDataSetChanged();
    }

    public interface ISelectChange {
        /**
         * 选中内容变化
         */
        void onSelectChange();
    }


    protected static final int MSG_UPDATE_LIST = 0;
    protected MyHandler myHandler;
    protected static class MyHandler extends Handler {
        private WeakReference<AlbumFragment> weakReference;
        public MyHandler(AlbumFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_LIST:
                    weakReference.get().updateList();
                    break;
            }
        }
    };
}
