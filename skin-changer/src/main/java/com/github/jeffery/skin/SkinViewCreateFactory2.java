package com.github.jeffery.skin;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mxlei
 * @date 2022/8/28
 */
public class SkinViewCreateFactory2 implements LayoutInflater.Factory2 {


    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return onCreateView(name, context, attrs);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View view = null;
        if (!SkinManger.getInstance().isCustomSkin()) {
            return null;
        }
        // 创建View, 优先使用系统默认的Factory2工厂来创建（可以适配主题）
        SkinActivityRecord ac = SkinManger.getInstance().getActivityRecord(context);
        LayoutInflater.Factory2 compatFactory2 = ac == null ? null : ac.getCompatDelegateViewFactory2();
        try {
            if (name.indexOf('.') == -1) {
                if (compatFactory2 != null) {
                    view = compatFactory2.onCreateView(name, context, attrs);
                }
                if (view == null) {
                    if (name.equals("View") || name.equals("ViewStub")) {
                        view = LayoutInflater.from(context).createView(name, "android.view.", attrs);
                    } else {
                        view = LayoutInflater.from(context).createView(name, "android.widget.", attrs);
                        if (view == null) {
                            view = LayoutInflater.from(context).createView(name, "android.webkit.", attrs);
                        }
                    }
                }

            } else {
                if (compatFactory2 != null) {
                    view = compatFactory2.onCreateView(name, context, attrs);
                } else {
                    view = LayoutInflater.from(context).createView(name, null, attrs);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (ac != null && view != null) {
            SkinView skinView = wrapSkinView(view, attrs);
            if (skinView != null) {
                ac.getSkinViewList().add(skinView);
            }
        }
        return view;
    }


    private SkinView wrapSkinView(@Nullable View view, @NonNull AttributeSet attrs) {
        int count = attrs.getAttributeCount();
        if (view == null || count == 0) {
            return null;
        }
        Resources appResources = view.getContext().getResources();
        List<SkinAttr> attrList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String attrName = attrs.getAttributeName(i);
            String attrValue = attrs.getAttributeValue(i);
            if (SkinManger.getInstance().isSupportedAttribute(attrName)) {
                if (attrValue.startsWith("@")) {
                    try {
                        int resId = Integer.parseInt(attrValue.substring(1));
                        String resName = appResources.getResourceName(resId);
                        String resType = appResources.getResourceTypeName(resId);
                        String resEntryName = appResources.getResourceEntryName(resId);
                        SkinAttr skAttr = new SkinAttr();
                        skAttr.setAttrName(attrName);
                        skAttr.setSkinResEntryName(resEntryName);
                        skAttr.setSkinResType(resType);
                        skAttr.setOriginResId(resId);
                        attrList.add(skAttr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (attrValue.startsWith("?")) {
                // todo
            }
        }
        return attrList.isEmpty() ? null : new SkinView(view, attrList);
    }
}
