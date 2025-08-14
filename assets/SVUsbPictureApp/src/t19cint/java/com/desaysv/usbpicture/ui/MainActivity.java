package com.desaysv.usbpicture.ui;


import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.svlibfileoperation.FileOperationManager;
import com.desaysv.svlibfileoperation.iinterface.IFileOperationListener;
import com.desaysv.svlibusbdialog.dialog.SourceDialogUtil;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.adapter.MainFragmentAdapter;
import com.desaysv.usbpicture.constant.Point;
import com.desaysv.usbpicture.fragment.AlbumFragment;
import com.desaysv.usbpicture.fragment.BaseUSBPictureListFragment;
import com.desaysv.usbpicture.fragment.USB1PictureListFragment;
import com.desaysv.usbpicture.trigger.PointTrigger;
import com.desaysv.usbpicture.view.CanScrollViewPager;
import com.desaysv.usbpicture.view.ConfirmDialog;
import com.google.android.material.tabs.TabLayout;
import com.desaysv.usbpicture.utils.ProductUtils;
import java.lang.ref.WeakReference;
import java.util.List;
import androidx.fragment.app.Fragment;

public class MainActivity extends FragmentActivity implements View.OnClickListener, BaseUSBPictureListFragment.IStatueChange, AlbumFragment.ISelectChange, IFileOperationListener {
    private static final String TAG = "MainActivity";

    private CanScrollViewPager vp_root_pager;
    private RelativeLayout rl_tab;
    private RelativeLayout rl_tab0;
    private RelativeLayout rl_tab1;
    private MainFragmentAdapter fragmentAdapter;
    private RelativeLayout rl_root_option;//右侧选项的总布局
    private LinearLayout ll_root_option_type;//右侧类型选项的总布局
    private ImageView iv_root_option_type_all;//图库"全部图片"类型选项
    private ImageView iv_root_option_type_folder;//图库"文件夹"类型选项
    private ImageView iv_root_option_edit;//相册"编辑"选项

    private AlbumFragment albumFragment;
    private USB1PictureListFragment usbFragment;
    private int autoTab = 1;//自动跳转到USB图片
    private int currentTab = 1;//当前页
    private boolean forceUpdate = false;//导出相册图片后，切到USB图片时需要强制更新
    private int contentState = 0;

    private RelativeLayout rl_pictureList_root_control;
    private TextView tv_current_dir;

    private RelativeLayout rl_toast;
    private TextView tv_toast;

    private RelativeLayout rl_album_edit;//编辑模式的控制
    private ImageView iv_edit_close;
    private TextView tv_select_count;
    private ImageView iv_edit_delete;
    private ImageView iv_edit_export;
    private ImageView iv_edit_selectAll;

    private TextView tv_main_title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        boolean isRightRudder = ProductUtils.isRightRudder();
        setContentView(isRightRudder ? R.layout.layout_mainactivity_right : R.layout.layout_mainactivity);
        initView();
        initViewListener();
        FileOperationManager.getInstance().init(this);
        FileOperationManager.getInstance().registerOperationListener(this,this.getClass().getSimpleName());//允许后台操作
        Log.d(TAG,"onCreate");
        if (savedInstanceState != null){
            iv_root_option_type_all.setSelected(savedInstanceState.getBoolean("iv_root_option_type_all"));
            iv_root_option_type_folder.setSelected(savedInstanceState.getBoolean("iv_root_option_type_folder"));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("iv_root_option_type_all",iv_root_option_type_all.isSelected());
        outState.putBoolean("iv_root_option_type_folder",iv_root_option_type_folder.isSelected());
        super.onSaveInstanceState(outState);
    }

    private void initView(){
        myHandler = new MyHandler(this);
        vp_root_pager = findViewById(R.id.vp_root_pager);
        rl_tab = findViewById(R.id.rl_tab);
        tv_main_title = findViewById(R.id.tv_main_title);
        rl_tab0 = findViewById(R.id.rl_tab0);
        rl_tab1 = findViewById(R.id.rl_tab1);
        rl_root_option = findViewById(R.id.rl_root_option);
        ll_root_option_type = findViewById(R.id.ll_root_option_type);
        iv_root_option_type_all = findViewById(R.id.iv_root_option_type_all);
        iv_root_option_type_folder = findViewById(R.id.iv_root_option_type_folder);
        iv_root_option_edit = findViewById(R.id.iv_root_option_edit);
        //这一部分很重要，用于重载之后，从系统保存的对象取出 fragment(例如白天黑夜切换)
        //否则因为系统默认恢复的是保存的fragment，走的是恢复对象的生命周期，
        //而这里又new了新的fragment对象，就会导致new的对象里面的参数实际并没有初始化，因为new这个fragment不会被add进去，不会执行相应的生命周期
        //就会导致各种空指针异常
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null || fragments.size() == 0){
            albumFragment = new AlbumFragment();
            usbFragment = new USB1PictureListFragment();
        }else {
            if (ProductUtils.hasCVBox()) {
                if (fragments.get(0) instanceof AlbumFragment){
                    albumFragment = (AlbumFragment) fragments.get(0);
                    usbFragment = (USB1PictureListFragment) fragments.get(1);
                }else {
                    albumFragment = (AlbumFragment) fragments.get(1);
                    usbFragment = (USB1PictureListFragment) fragments.get(0);
                }
            }else {
                usbFragment = (USB1PictureListFragment) fragments.get(0);
            }
        }
        fragmentAdapter = new MainFragmentAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragmentAdapter.setFragment(albumFragment,usbFragment);
        vp_root_pager.setAdapter(fragmentAdapter);
        vp_root_pager.setCurrentItem(1);
        if (ProductUtils.hasCVBox()) {
            rl_tab1.setSelected(true);
            tv_main_title.setVisibility(View.GONE);
        }else {
            rl_tab.setVisibility(View.GONE);
            tv_main_title.setVisibility(View.VISIBLE);
        }
        iv_root_option_type_all.setSelected(true);
        iv_root_option_type_folder.setSelected(false);

        rl_pictureList_root_control = findViewById(R.id.rl_pictureList_root_control);
        tv_current_dir = findViewById(R.id.tv_current_dir);

        rl_album_edit = findViewById(R.id.rl_album_edit);
        iv_edit_close = findViewById(R.id.iv_edit_close);
        tv_select_count = findViewById(R.id.tv_select_count);
        iv_edit_delete = findViewById(R.id.iv_edit_delete);
        iv_edit_export = findViewById(R.id.iv_edit_export);
        iv_edit_selectAll = findViewById(R.id.iv_edit_selectAll);
        rl_toast = findViewById(R.id.rl_toast);
        tv_toast = findViewById(R.id.tv_toast);
    }

    private void initViewListener(){
        iv_root_option_type_all.setOnClickListener(this);
        iv_root_option_type_folder.setOnClickListener(this);
        iv_root_option_edit.setOnClickListener(this);
        rl_tab0.setOnClickListener(this);
        rl_tab1.setOnClickListener(this);
        rl_pictureList_root_control.setOnClickListener(this);
        iv_edit_close.setOnClickListener(this);
        iv_edit_delete.setOnClickListener(this);
        iv_edit_export.setOnClickListener(this);
        iv_edit_selectAll.setOnClickListener(this);
        vp_root_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG,"onPageSelected:"+position);
                updateTitle(position,false);
                currentTab = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 根据当前选中位置更新顶部界面
     * @param position
     */
    private void updateTitle(int position, boolean scroll){
        Log.d(TAG,"updateTitle,position:"+position+",scroll:"+scroll);
        if (position == 0){
            rl_tab0.setSelected(true);
            rl_tab1.setSelected(false);
            iv_root_option_edit.setVisibility(View.VISIBLE);
            if ( DeviceStatusBean.getInstance().isUSB1Connect()) {
                ll_root_option_type.setVisibility(View.GONE);
            }
            if (scroll) {
                vp_root_pager.setCurrentItem(0, false);
            }
            albumFragment.initData();
        }else if (position == 1){
            rl_tab1.setSelected(true);
            rl_tab0.setSelected(false);
            iv_root_option_edit.setVisibility(View.GONE);
            if (DeviceStatusBean.getInstance().isUSB1Connect() && contentState != BaseUSBPictureListFragment.CONTENT_STATE_EMPTY) {
                ll_root_option_type.setVisibility(View.VISIBLE);
                iv_root_option_type_all.setEnabled(true);
                iv_root_option_type_folder.setEnabled(true);
            }
            if (scroll) {
                vp_root_pager.setCurrentItem(1, false);
            }
            if(forceUpdate) {
                forceUpdate = false;
                usbFragment.forceUpdateData();//导出成功后，更新USB图片的数据
            }
        }
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG,"onNewIntent");
        if (intent != null){
            autoTab = intent.getIntExtra(SourceDialogUtil.AUTO_TAB,currentTab);
            vp_root_pager.setCurrentItem(autoTab);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        FileOperationManager.getInstance().unregisterOperationListener(this.getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        PointTrigger.getInstance().trackEvent(Point.KeyName.OpenPictureClick,Point.Field.OpenType,Point.FieldValue.CLICKACTION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        updateViewWithUSBStatus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        PointTrigger.getInstance().trackEvent(Point.KeyName.ClosePictureClick,Point.Field.CloseType,Point.FieldValue.CLICKACTION);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_root_option_type_all){
            //show all picture
            if ((!iv_root_option_type_all.isSelected()) && usbFragment.handleShowAllPictures()) {
                iv_root_option_type_all.setSelected(true);
                iv_root_option_type_folder.setSelected(false);
            }
        }else if (id == R.id.iv_root_option_type_folder){
            //show folder
            if (usbFragment.handleShowFolderPictures()) {
                iv_root_option_type_all.setSelected(false);
                iv_root_option_type_folder.setSelected(true);
            }
        }else if (id == R.id.iv_root_option_edit){
            updateWithEdit(true,true);
        }else if (id == R.id.rl_tab0){
            updateTitle(0,true);
        }else if (id == R.id.rl_tab1){
            updateTitle(1,true);
        }else if (id == R.id.rl_pictureList_root_control){
            usbFragment.handleClick();
        }else if (id == R.id.iv_edit_close){
            updateWithEdit(false,true);
        }else if (id == R.id.iv_edit_delete){
            //handle delete
            //FileOperationManager.getInstance().startDeleteFiles(this,albumFragment.getSelectList());
            new ConfirmDialog(this,R.style.dialogstyle,albumFragment.getSelectList()).show();
        }else if (id == R.id.iv_edit_export){
            //handle export
            FileOperationManager.getInstance().startExportFiles(this,albumFragment.getSelectList());
        }else if (id == R.id.iv_edit_selectAll){
            //handle selectAll
            v.setSelected(!v.isSelected());
            updateWithSelectAll(v.isSelected(), true);
        }
    }

    @Override
    public void changed(boolean isRoot, String showName) {
        Log.d(TAG,"changed, isRoot:"+isRoot);
        if (isRoot){
            vp_root_pager.setScrollable(false);
            if (ProductUtils.hasCVBox()) {
                rl_tab.setVisibility(View.VISIBLE);
                tv_main_title.setVisibility(View.GONE);
            }else {
                rl_tab.setVisibility(View.GONE);
                tv_main_title.setVisibility(View.VISIBLE);
            }
            rl_root_option.setVisibility(View.VISIBLE);
            rl_pictureList_root_control.setVisibility(View.GONE);
        }else {
            vp_root_pager.setScrollable(true);
            rl_tab.setVisibility(View.GONE);
            tv_main_title.setVisibility(View.GONE);
            rl_root_option.setVisibility(View.GONE);
            rl_pictureList_root_control.setVisibility(View.VISIBLE);
            tv_current_dir.setText(showName);
        }
    }

    @Override
    public void onFolderClick(String folder) {
        Log.d(TAG,"onFolderClick,folder:"+folder);
        ValueAnimator animatorClickFolder = ValueAnimator.ofFloat(1f,0f,1f);
        animatorClickFolder.setDuration(BaseUSBPictureListFragment.ANIM_TIME_CLICK_FOLDER);
        animatorClickFolder.start();
        animatorClickFolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rl_tab.setAlpha((Float) animation.getAnimatedValue());
                tv_main_title.setAlpha((Float) animation.getAnimatedValue());
                rl_root_option.setAlpha((Float) animation.getAnimatedValue());

            }
        });
    }

    @Override
    public void onUSBStatueChange(String path, boolean status) {
        rl_pictureList_root_control.setVisibility(View.GONE);
        forceUpdate = false;//插拔USB的时候需要重置
        if (status){
            if (ProductUtils.hasCVBox()){
                if(currentTab == 1) {
                    rl_root_option.setVisibility(View.VISIBLE);
                    ll_root_option_type.setVisibility(View.VISIBLE);
                }
            }else {
                rl_root_option.setVisibility(View.VISIBLE);
                ll_root_option_type.setVisibility(View.VISIBLE);
            }
            iv_root_option_type_all.setEnabled(true);
            iv_root_option_type_folder.setEnabled(true);
            //重置为All
            iv_root_option_type_all.setSelected(true);
            iv_root_option_type_folder.setSelected(false);
        }else {
//            ll_root_option_type.setVisibility(View.GONE);
//            //重置为All
//            iv_root_option_type_all.setSelected(true);
//            iv_root_option_type_folder.setSelected(false);
            iv_root_option_type_all.setEnabled(false);
            iv_root_option_type_folder.setEnabled(false);
        }
    }

    @Override
    public void onContentChange(int state) {
        Log.d(TAG,"onContentChange:"+state);
        contentState = state;
        if (state == BaseUSBPictureListFragment.CONTENT_STATE_EMPTY && !DeviceStatusBean.getInstance().isUSB1Connect()){//扫描到内容为空
//            ll_root_option_type.setVisibility(View.GONE);
            iv_root_option_type_all.setEnabled(false);
            iv_root_option_type_folder.setEnabled(false);
        }else if (state == BaseUSBPictureListFragment.CONTENT_STATE_NO_EMPTY){//内容不为空
            if (ProductUtils.hasCVBox()){
                if(currentTab == 1) {
                    ll_root_option_type.setVisibility(View.VISIBLE);
                }
            }else {
                ll_root_option_type.setVisibility(View.VISIBLE);
            }
            rl_tab.setAlpha(1);
            tv_main_title.setAlpha(1);
            rl_root_option.setAlpha(1);
            iv_root_option_type_all.setEnabled(true);
            iv_root_option_type_folder.setEnabled(true);
        }
    }

    private void updateViewWithUSBStatus(){
        if ( DeviceStatusBean.getInstance().isUSB1Connect()){
            if (ProductUtils.hasCVBox()){
                if(currentTab == 1) {
                    ll_root_option_type.setVisibility(View.VISIBLE);
                }
            }else {
                ll_root_option_type.setVisibility(View.VISIBLE);
            }
            iv_root_option_type_all.setEnabled(true);
            iv_root_option_type_folder.setEnabled(true);
        }else {
//            ll_root_option_type.setVisibility(View.GONE);
            iv_root_option_type_all.setEnabled(false);
            iv_root_option_type_folder.setEnabled(false);
        }
    }

    /**
     * 更新编辑模式的UI
     * @param edit
     */
    public void updateWithEdit(boolean edit, boolean updateAdapter){
        Log.d(TAG,"updateWithEdit,edit:"+edit+",updateAdapter:"+updateAdapter);
        if (updateAdapter) {//删除后不需要更新，因为删除后需要更新列表
            albumFragment.handleSelectMode(edit);
        }
        tv_select_count.setText(getResources().getString(R.string.select_none));//重置选中数量
        iv_edit_selectAll.setSelected(false);//重置选中状态
        if (edit){
            rl_album_edit.setVisibility(View.VISIBLE);
            rl_tab.setVisibility(View.GONE);
            iv_root_option_edit.setVisibility(View.GONE);
            iv_edit_export.setEnabled(false);
            iv_edit_delete.setEnabled(false);
        }else {
            rl_album_edit.setVisibility(View.GONE);
            rl_tab.setVisibility(View.VISIBLE);
            iv_root_option_edit.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新全选的UI
     * @param selectAll 是否全选
     * @param needUpdate 是否需要更新列表
     */
    public void updateWithSelectAll(boolean selectAll, boolean needUpdate){
        Log.d(TAG,"updateWithSelectAll,selectAll:"+selectAll+",needUpdate:"+needUpdate);
        if (needUpdate) {
            albumFragment.handleSelectAll(selectAll);
            if (selectAll) {
                tv_select_count.setText(String.format(getResources().getString(R.string.select_count), albumFragment.getSelectList().size()));
            } else {
                tv_select_count.setText(getResources().getString(R.string.select_none));
            }
        }
        iv_edit_selectAll.setSelected(selectAll);
    }

    @Override
    public void onSelectChange() {
        if (albumFragment.getSelectList().size() > 0){
            iv_edit_export.setEnabled(true);
            iv_edit_delete.setEnabled(true);
        }else {
            iv_edit_export.setEnabled(false);
            iv_edit_delete.setEnabled(false);
        }
        tv_select_count.setText(String.format(getResources().getString(R.string.select_count),albumFragment.getSelectList().size()));
        if (albumFragment.getSelectList().size() == albumFragment.getAllList().size()){//全选
            updateWithSelectAll(true,false);
        }else {
            updateWithSelectAll(false,false);
        }
    }

    @Override
    public void onExportState(int state, int current, int total) {
        Log.d(TAG,"onExportState,state:"+state+",current:"+current);
        if (state == FileOperationManager.OPERATION_STATE_SUCCESS){
            rl_toast.setVisibility(View.VISIBLE);
            tv_toast.setText(getResources().getString(R.string.export_ok));
            updateWithEdit(false,true);
            forceUpdate = true;
        }else if (state == FileOperationManager.OPERATION_STATE_ERROR_SPACE){//空间不足
            rl_toast.setVisibility(View.VISIBLE);
            tv_toast.setText(getResources().getString(R.string.export_space));
            updateWithEdit(false,true);
        }else if (state == FileOperationManager.OPERATION_STATE_ERROR_IO){//U盘中断
            rl_toast.setVisibility(View.VISIBLE);
            tv_toast.setText(getResources().getString(R.string.export_io));
            updateWithEdit(false,true);
        }
        myHandler.sendEmptyMessageDelayed(MSG_UPDATE_TOAST,1500);
    }

    @Override
    public void onDeleteState(int state, int current, int total) {
        Log.d(TAG,"onDeleteState,state:"+state+",current:"+current);
            if (state == FileOperationManager.OPERATION_STATE_SUCCESS || state == FileOperationManager.OPERATION_STATE_CANCEL){
                updateWithEdit(false,false);
                albumFragment.updateData();
            }
    }

    public void updateToast(boolean visible){
        rl_toast.setVisibility(visible ? View.VISIBLE: View.GONE);
    }
    protected static final int MSG_UPDATE_TOAST = 0;
    protected MainActivity.MyHandler myHandler;
    protected static class MyHandler extends Handler {
        private WeakReference<MainActivity> weakReference;
        public MyHandler(MainActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_TOAST:
                    weakReference.get().updateToast(false);
                    break;
            }
        }
    };
}