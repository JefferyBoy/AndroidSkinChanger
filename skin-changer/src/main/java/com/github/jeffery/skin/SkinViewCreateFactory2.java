package com.github.jeffery.skin;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mxlei
 * @date 2022/8/28
 */
public class SkinViewCreateFactory2 implements LayoutInflater.Factory2 {

    /**
     * 在theme中定义view默认属性
     * key为view的类型，value为属性集
     */
    private final Map<Class<?>, List<SkinAttr>> skinAttrsOnTheme = new HashMap<>();

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
            SkinView skinView = toSkinView(view, attrs);
            if (skinView != null) {
                ac.getSkinViewList().add(skinView);
            }
        }
        return view;
    }

    @NonNull
    private List<SkinAttr> getDefaultSkinAttrList(View view, Class<?> clazz, @AttrRes int styleableAttrRes) {
        List<SkinAttr> cachedAttrs = skinAttrsOnTheme.get(clazz);
        if (cachedAttrs == null) {
            cachedAttrs = new ArrayList<>();
            TypedArray ta = view.getContext().obtainStyledAttributes(new int[]{styleableAttrRes});
            int resId = ta.getResourceId(0, 0);
            if (resId != 0) {
                for (Pair<String, Integer> pair : SkinManger.ATTR_STYLE_RESOURCES) {
                    SkinAttr skinAttr = toSkinAttr(view.getContext(), pair.first, resId, pair.second);
                    if (skinAttr != null) {
                        cachedAttrs.add(skinAttr);
                    }
                }
            }
            ta.recycle();
            skinAttrsOnTheme.put(clazz, cachedAttrs);
        }
        return cachedAttrs;
    }

    @Nullable
    private List<SkinAttr> getDefaultSkinAttrList(View view) {
        if (view instanceof Button) {
            return getDefaultSkinAttrList(view, Button.class, android.R.attr.buttonStyle);
        }
        if (view instanceof TextView) {
            return getDefaultSkinAttrList(view, TextView.class, android.R.attr.textStyle);
        }
        return null;
    }

    /**
     * 转换view为可换肤的View
     *
     * @return 当view不支持换肤时返回null
     */
    private SkinView toSkinView(@Nullable View view, @NonNull AttributeSet attrs) {
        int count = attrs.getAttributeCount();
        if (view == null || count == 0) {
            return null;
        }
        // View的所有可换肤的属性集合，key为属性名称
        Map<String, SkinAttr> skinAttrMap = new HashMap<>();
        // theme中定义的控件属性
        List<SkinAttr> defaultAttrs = getDefaultSkinAttrList(view);
        if (defaultAttrs != null && !defaultAttrs.isEmpty()) {
            for (SkinAttr skinAttr : defaultAttrs) {
                skinAttrMap.put(skinAttr.getAttrName(), skinAttr);
            }
        }
        for (int i = 0; i < count; i++) {
            String attrName = attrs.getAttributeName(i);
            String attrValue = attrs.getAttributeValue(i);
            if ("style".equals(attrName)) {
                // 当布局中使用style定义控件的属性集时，解析属性集中的每个属性
                int resId = Integer.parseInt(attrValue.substring(1));
                for (Pair<String, Integer> pair : SkinManger.ATTR_STYLE_RESOURCES) {
                    SkinAttr skinAttr = toSkinAttr(view.getContext(), pair.first, resId, pair.second);
                    if (skinAttr != null) {
                        skinAttrMap.put(skinAttr.getAttrName(), skinAttr);
                    }
                }
            } else {
                // 布局中的属性转换为皮肤属性
                SkinAttr skinAttr = toSkinAttr(view.getContext(), attrName, attrValue);
                if (skinAttr != null) {
                    skinAttrMap.put(skinAttr.getAttrName(), skinAttr);
                }
            }
        }
        if (skinAttrMap.isEmpty()) {
            return null;
        } else {
            return new SkinView(view, new ArrayList<>(skinAttrMap.values()));
        }
    }

    /**
     * 转换属性为皮肤属性
     */
    @Nullable
    private SkinAttr toSkinAttr(Context context, String attrName, String attrValue) {
        if (attrValue == null || attrName.length() < 2) {
            return null;
        }
        Resources resources = context.getResources();
        if (SkinManger.getInstance().isSupportedAttribute(attrName)) {
            if (attrValue.startsWith("@")) {
                // @后面是资源ID
                try {
                    int resId = Integer.parseInt(attrValue.substring(1));
                    String resName = resources.getResourceName(resId);
                    String resType = resources.getResourceTypeName(resId);
                    String resEntryName = resources.getResourceEntryName(resId);
                    SkinAttr skAttr = new SkinAttr();
                    skAttr.setAttrName(attrName);
                    skAttr.setSkinResEntryName(resEntryName);
                    skAttr.setSkinResType(resType);
                    skAttr.setOriginResId(resId);
                    return skAttr;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (attrValue.startsWith("?")) {
            // ?后面是另一个属性的ID,通过另一个属性来获取真正的资源ID
            TypedArray ta = context.obtainStyledAttributes(new int[]{Integer.parseInt(attrValue.substring(1))});
            int resId = ta.getResourceId(0, 0);
            ta.recycle();
            if (resId != 0) {
                return toSkinAttr(context, attrName, "@" + resId);
            }
        }
        return null;
    }

    /**
     * 从定义style属性集中获取一个属性，转换为皮肤属性
     */
    @Nullable
    private SkinAttr toSkinAttr(@NonNull Context context,
                                String attrName,
                                @StyleRes int styleRes,
                                @AttrRes int styleableAttr) {
        SkinAttr skinAttr = null;
        TypedValue typedValue = new TypedValue();
        TypedArray ta = context.obtainStyledAttributes(styleRes, new int[]{styleableAttr});
        if (ta.getValue(0, typedValue)) {
            String attrValue = "@" + ta.getResourceId(0, 0);
            skinAttr = toSkinAttr(context, attrName, attrValue);
        }
        ta.recycle();
        return skinAttr;
    }
}
