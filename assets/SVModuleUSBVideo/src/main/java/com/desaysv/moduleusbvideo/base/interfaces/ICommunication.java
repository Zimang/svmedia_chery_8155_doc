package com.desaysv.moduleusbvideo.base.interfaces;

import java.util.List;

/**
 * @author xiaohuiy
 * @email xiaohuiy@kotei-info.com
 * @since 2020-9-30
 */
public interface ICommunication {

    /**
     * 显示目标视图
     *
     * @param id 目标视图id
     */
    void showFragment(int id);

    /**
     * 显示目标视图集合
     *
     * @param ids 目标视图组所有id集合
     */
    void showFragments(List<Integer> ids);

    /**
     * 显示目标视图集合
     * 同步
     * @param ids 目标视图组所有id集合
     */
    void showFragmentsNow(List<Integer> ids);

    /**
     * 返回当前显示的所有视图id集合
     *
     * @return 当前显示的所有视图id集合
     */
    List<Integer> getCurrentFragmentIDs();

    /**
     * fragment返回上一组显示画面
     */
    void backPage();
}
