package com.desaysv.moduleusbvideo.base;

import android.util.Log;
import android.util.SparseArray;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.desaysv.moduleusbvideo.base.interfaces.IFragmentManager;
import com.desaysv.moduleusbvideo.util.ListHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * @author xiaohuiy
 * @email xiaohuiy@kotei-info.com
 * @since 2020-9-30
 */
public class FragmentManager implements IFragmentManager<FragmentInfo, androidx.fragment.app.FragmentManager> {
    private static final String TAG = FragmentManager.class.getSimpleName();

    private androidx.fragment.app.FragmentManager mFragmentManager;
    private final List<FragmentInfo> mCurrentFragmentInfo = new ArrayList<>();

    /**
     * 窗口ID栈，用于记录view的显示顺序
     */
    private final Stack<List<Integer>> mViewStacks = new Stack<>();

    /**
     * 所有View信息的存储器
     */
    private final SparseArray<FragmentInfo> mFragments = new SparseArray<>();

    /**
     * 向fragment 集合中添加路径
     *
     * @param views   视图管理容器
     * @param manager 加载视图管理器
     */
    @Override
    public void addViews(SparseArray<FragmentInfo> views, androidx.fragment.app.FragmentManager manager) {
        int size = views.size();
        mFragmentManager = manager;
        FragmentInfo fragmentInfo;
        int id;

        for (int i = 0; i < size; i++) {
            id = views.keyAt(i);
            fragmentInfo = views.get(id);
            Log.d(TAG, "addViews fragmentInfo: " + fragmentInfo.toString());
            mFragments.put(id, fragmentInfo);

        }
    }


    @Override
    public void showView(int id) {
        Log.d(TAG, "showView: show fragmentInfo id: " + id);
        FragmentInfo view = mFragments.get(id);

        //判断当前显示视图是否与要显示的视图相同
        boolean isCurViews = mCurrentFragmentInfo.size() == 1
                && mCurrentFragmentInfo.contains(view);
        if (isCurViews) {
            Log.i(TAG, "showView: dest views is current views!!!");
            return;
        }

        List<Integer> views = new ArrayList<>();
        views.add(id);
        showViews(views);
////        Log.i(TAG, "show fragmentInfo : " + view.toString());
//        FragmentTransaction transaction = mFragmentManager.beginTransaction();
//        Log.i(TAG, "show mCurrentFragmentInfos.isEmpty() : " + mCurrentFragmentInfos.isEmpty());
//        hideCurrentViews(transaction);
//        showView(id, view, transaction);
    }


    private void showView(int id, FragmentInfo view, FragmentTransaction transaction) {
        Log.d(TAG, "show fragmentInfo: " + view.toString());
        Fragment fragment = view.getFragment();
        int contentID = view.getContentID();
        if (!fragment.isAdded()) {
            Log.d(TAG, "add fragment :" + fragment.toString());
            transaction.add(contentID, fragment);
        }

        transaction.show(fragment).commitAllowingStateLoss();
        mCurrentFragmentInfo.add(view);

        List<Integer> list = new ArrayList<>();
        list.add(id);
        stacksAdd(list);
    }


    /**
     * 添加当前数据到栈顶
     * 统一处理链表数据添加，唯一值
     *
     * @param list list
     */
    private void stacksAdd(List<Integer> list) {
        int search = mViewStacks.search(list);
        Log.d(TAG, "------------ start mViewStacks.size() = " + mViewStacks.size());
        if (search != -1) {
            mViewStacks.remove(list);
        }
        mViewStacks.add(list);
        Log.d(TAG, "------------ end mViewStacks.size() = " + mViewStacks.size());
    }

    @Override
    public void showViews(List<Integer> viewIDs) {
        Log.d(TAG, "showViews start!");
        if (viewIDs == null) {
            return;
        }
        List<FragmentInfo> views = getFragmentInfos(viewIDs);
        //判断当前显示视图是否与要显示的视图相同
        boolean isCurViews = ListHelper.compare(views, mCurrentFragmentInfo);
        if (isCurViews) {
            Log.d(TAG, "dest views is current views!!!");
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Log.d(TAG, "showViews, hideCurrentViews! start ");
        List<FragmentInfo> repeatFragmentIds = getRepeatFragmentIds(views);
        //隐藏当前显示画面
        hideCurrentViews(transaction, repeatFragmentIds);
        Log.d(TAG, "showViews, hideCurrentViews! end ");

        Log.d(TAG, "showViews, showViews!");
        showViews(viewIDs, views, transaction, repeatFragmentIds);
        Log.d(TAG, "showViews, end!");
    }

    /**
     * showViewsNow
     * to fix the shake when on the first loading
     *
     * @param viewIDs views
     */
    public void showViewsNow(List<Integer> viewIDs) {
        Log.d(TAG, "showViewsNow start!");
        if (viewIDs == null) {
            return;
        }
        List<FragmentInfo> views = getFragmentInfos(viewIDs);
        //判断当前显示视图是否与要显示的视图相同
        boolean isCurViews = ListHelper.compare(views, mCurrentFragmentInfo);
        if (isCurViews) {
            Log.d(TAG, "dest views is current views!!!");
            return;
        }

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Log.d(TAG, "showViewsNow,  hideCurrentViews!");
        List<FragmentInfo> repeatFragmentIds = getRepeatFragmentIds(views);
        //隐藏当前显示画面
        hideCurrentViews(transaction, repeatFragmentIds);
        transaction.commitNowAllowingStateLoss();
        Log.d(TAG, "showViewsNow, hideCurrentViews!");

        Log.d(TAG, "showViewsNow, showViews!");
        showViewsNow(views, transaction, repeatFragmentIds);
        mCurrentFragmentInfo.addAll(views);
        //将显示视图集合加入到显示堆栈中
        stacksAdd(viewIDs);
        Log.d(TAG, "showViewsNow, end!");
    }


    /**
     * actvity销毁的时候移除之前添加的fragment
     */
    public void removeAllViews(androidx.fragment.app.FragmentManager manager) {
        List<Fragment> fragments = manager.getFragments();
        Log.d(TAG, "removeAllViews size:" + fragments.size() + "," + fragments.toString());
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        for (int i = 0; i < fragments.size(); i++) {
            if (fragments.get(i).isAdded()) {
                Log.d(TAG, "removeAllViews fragment is add");
                fragmentTransaction.remove(fragments.get(i));
            }
        }
        fragmentTransaction.commitAllowingStateLoss();
    }


    private void showViews(List<Integer> viewIDs, List<FragmentInfo> views, FragmentTransaction transaction, List<FragmentInfo> repeatFragments) {
        //显示所有目标视图
        showViews(views, transaction, repeatFragments);
        mCurrentFragmentInfo.addAll(views);
        //将显示视图集合加入到显示堆栈中
        stacksAdd(viewIDs);
    }

    private void showViews(List<FragmentInfo> views, FragmentTransaction transaction, List<FragmentInfo> repeatFragments) {
        handleShow(views, transaction, repeatFragments);
        transaction.commitAllowingStateLoss();
    }

    private void showViewsNow(List<FragmentInfo> views, FragmentTransaction transaction, List<FragmentInfo> repeatFragments) {
        handleShow(views, transaction, repeatFragments);
        transaction.commitNowAllowingStateLoss();
    }

    private void handleShow(List<FragmentInfo> views, FragmentTransaction transaction, List<FragmentInfo> repeatFragments) {
        Log.d(TAG, "handleShow: repeatFragments = " + repeatFragments);
        FragmentInfo fragmentInfo;
        Fragment fragment;
        int contentID;
        int viewSize = views.size();
        for (int i = 0; i < viewSize; i++) {
            fragmentInfo = views.get(i);
            Log.d(TAG, "handleShow: fragmentInfo:" + fragmentInfo + "size:" + views.size() + ",view:" + views.toString());
            fragment = fragmentInfo.getFragment();
            contentID = fragmentInfo.getContentID();
            if (!fragment.isAdded()) {
                Log.d(TAG, "handleShow: add fragment :" + fragmentInfo.toString());
                transaction.add(contentID, fragment);
            }
            if (!repeatFragments.contains(fragmentInfo)) {
                Log.d(TAG, "handleShow: repeatFragments:" + repeatFragments.toString() + ",curFragment:" + fragment);
                transaction.show(fragment);
            }
            Log.d(TAG, "handleShow: fragmentInfo " + i + ": " + fragmentInfo.toString());
        }
    }


    @Override
    public List<FragmentInfo> getCurrentViewInfos() {
        return mCurrentFragmentInfo;
    }

    @Override
    public void initCurrentVideInfos(List<FragmentInfo> currentInfos) {
        mCurrentFragmentInfo.addAll(currentInfos);
    }

    @Override
    public void back() {
        // 判断当前页面是否可以返回上一个页面
        if (!isMoreOneFragmentInStack()) {
            Log.d(TAG, "back: return mViewStacks.size() = " + mViewStacks.size());
            return;
        }
        //当前栈顶窗口退栈
        //获取上一组ViewID，打开上一组View
        List<Integer> lastViewIDs = mViewStacks.pop();
        Log.d(TAG, "back lastViewIDs:" + lastViewIDs.toString());

        //将ID转换为FragmentInfo，并判断是否当前画面已经显示其中的某一个ID视图，
        //如果当前画面已经显示其中某一个ID视图，将不再进行重新显示
        List<FragmentInfo> views = getFragmentInfos(lastViewIDs);
        Log.d(TAG, "back lastViewIDs:" + views.toString());
        // 剔除 当前页面，返回的是否是上一个页面，堆栈没有排除当前页面
        if (views.containsAll(getCurrentViewInfos())) {
            Log.d(TAG, "back: views.containsAll(getCurrentViewInfos()) == true");
            views = getFragmentInfos(mViewStacks.peek());
        }

        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        List<FragmentInfo> repeatFragmentIds = getRepeatFragmentIds(views);

        //隐藏当前显示画面
        hideCurrentViews(transaction, repeatFragmentIds);

        //显示所有目标视图
        showViews(views, transaction, repeatFragmentIds);

        mCurrentFragmentInfo.addAll(views);
    }

    @Override
    public boolean isMoreOneFragmentInStack() {
        // 补充判断- 当前界面显示了两个VIEW 代表在主页面
        if (getCurrentViewInfos().size() > 1) {
            Log.d(TAG, "current views = " + getCurrentViewInfos().size());
            // 当前页面是两个界面 ，为根页面
            return false;
        }
        return mViewStacks.size() > 1;
    }


    /**
     * 获取当前显示画面的fragments和之前显示的fragment重复的部分，
     * 不做重复的show/hide避免生命周期重复不必要的执行
     *
     * @return views
     */
    private List<FragmentInfo> getRepeatFragmentIds(List<FragmentInfo> views) {
        List<FragmentInfo> repeatFragments = new ArrayList<>();
        for (int i = 0; i < views.size(); i++) {
            FragmentInfo curView = views.get(i);
            if (mCurrentFragmentInfo.contains(curView)) {
                repeatFragments.add(curView);
            }
        }
        Log.d(TAG, "getRepeatFragmentIds: " + repeatFragments);
        return repeatFragments;
    }

    private void hideCurrentViews(FragmentTransaction transaction, List<FragmentInfo> repeatFragments) {
        Log.d(TAG, "hideCurrentViews: " + mCurrentFragmentInfo.size());
        if (!mCurrentFragmentInfo.isEmpty()) {
            Iterator<FragmentInfo> it = mCurrentFragmentInfo.iterator();
            FragmentInfo fragmentInfo;
            while (it.hasNext()) {
                fragmentInfo = it.next();
                Fragment curFragment = fragmentInfo.getFragment();
                if (!repeatFragments.contains(fragmentInfo)) {
                    Log.d(TAG, "hideCurrentViews repeatFragments:" + repeatFragments.toString() + ",fragmentInfo:" + fragmentInfo);
                    transaction.hide(curFragment);
                }
                Log.d(TAG, "show hide current fragmentInfo: " + fragmentInfo.toString());
                it.remove();
            }
        }
    }

    private List<FragmentInfo> getFragmentInfos(List<Integer> lastViewIDs) {
        List<FragmentInfo> views = new ArrayList<>();
        int viewIDSize = lastViewIDs.size();
        for (int i = 0; i < viewIDSize; i++) {
            int id = lastViewIDs.get(i);
            FragmentInfo fragmentInfo = mFragments.get(id);
            if (fragmentInfo != null) {
                views.add(fragmentInfo);
            }
        }
        return views;
    }

}
