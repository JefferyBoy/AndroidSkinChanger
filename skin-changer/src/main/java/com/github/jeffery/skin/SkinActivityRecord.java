package com.github.jeffery.skin;

import android.app.Activity;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mxlei
 * @date 2022/8/28
 */
public class SkinActivityRecord {
    /**
     * 当前Activity
     */
    private final Activity activity;
    /**
     * 当前Activity中使用的皮肤
     */
    private String currentSkin;
    /**
     * 当前Activity中支持换肤的View
     */
    private final List<SkinView> skinViewList;
    /**
     * 保存AppcompatDelegate中设置的factory2,用于适配主题变化来创建view
     */
    private final LayoutInflater.Factory2 compatDelegateViewFactory2;

    public SkinActivityRecord(@NonNull Activity activity) {
        this(activity, new ArrayList<SkinView>());
    }

    public SkinActivityRecord(@NonNull Activity activity, @NonNull List<SkinView> skinViewList) {
        this.activity = activity;
        this.skinViewList = skinViewList;
        compatDelegateViewFactory2 = activity.getLayoutInflater().getFactory2();
    }

    @NonNull
    public Activity getActivity() {
        return activity;
    }

    @NonNull
    public List<SkinView> getSkinViewList() {
        return skinViewList;
    }

    public LayoutInflater.Factory2 getCompatDelegateViewFactory2() {
        return compatDelegateViewFactory2;
    }

    public String getCurrentSkin() {
        return currentSkin;
    }

    public void setCurrentSkin(String currentSkin) {
        this.currentSkin = currentSkin;
    }
}
