package com.github.jeffery.skin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.LayoutInflaterCompat;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

/**
 * @author mxlei
 * @date 2022/8/28
 */
public class SkinManger extends SkinActivityLifecycleCallback {

    private boolean initialized = false;
    private Application app;
    private String currentSkin;
    private Resources skinResources;
    private String skinPackageName;
    private volatile static SkinManger instance = null;
    private final Stack<SkinActivityRecord> activityStack = new Stack<>();
    private static final String TAG = "SkinManager";

    private static final String RES_TYPE_NAME_COLOR = "color";
    private static final String RES_TYPE_NAME_DRAWABLE = "drawable";
    private static final String ATTR_NAME_SRC = "src";
    private static final String ATTR_NAME_TEXT_COLOR = "textColor";
    private static final String ATTR_NAME_BACKGROUND = "background";
    private static final String ATTR_NAME_DRAWABLE_START = "drawableStart";
    private static final String ATTR_NAME_DRAWABLE_LEFT = "drawableLeft";
    private static final String ATTR_NAME_DRAWABLE_TOP = "drawableTop";
    private static final String ATTR_NAME_DRAWABLE_RIGHT = "drawableRight";
    private static final String ATTR_NAME_DRAWABLE_END = "drawableEnd";
    private static final String ATTR_NAME_DRAWABLE_BOTTOM = "drawableBottom";

    private final Set<String> attrSet = new HashSet<>();

    private SkinManger() {
        attrSet.add(ATTR_NAME_BACKGROUND);
        attrSet.add(ATTR_NAME_TEXT_COLOR);
        attrSet.add(ATTR_NAME_SRC);
        attrSet.add(ATTR_NAME_DRAWABLE_BOTTOM);
        attrSet.add(ATTR_NAME_DRAWABLE_TOP);
        attrSet.add(ATTR_NAME_DRAWABLE_LEFT);
        attrSet.add(ATTR_NAME_DRAWABLE_START);
        attrSet.add(ATTR_NAME_DRAWABLE_RIGHT);
        attrSet.add(ATTR_NAME_DRAWABLE_END);
    }

    /**
     * 获取单例
     */
    public static SkinManger getInstance() {
        if (instance == null) {
            synchronized (SkinManger.class) {
                if (instance == null) {
                    instance = new SkinManger();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init(Application app) {
        if (!initialized) {
            initialized = true;
            SkinManger skin = getInstance();
            skin.app = app;
            setAppSkin(getLatestSkin());
            app.registerActivityLifecycleCallbacks(skin);
        }
    }


    /**
     * 获取当前的皮肤
     */
    @Nullable
    public String getCurrentSkin() {
        return currentSkin;
    }

    /**
     * 是否已使用自定义皮肤
     */
    public boolean isCustomSkin() {
        String skin = getCurrentSkin();
        if (skin == null || skin.isEmpty()) {
            return false;
        }
        File file = new File(skin);
        return file.exists() && file.isFile() && file.length() > 0;
    }

    /**
     * 恢复默认皮肤
     */
    private void restoreDefaultSkin() {
        currentSkin = null;
        saveLatestSkin(null);
        skinPackageName = null;
        skinResources = null;
        for (SkinActivityRecord activity : activityStack) {
            reloadSkin(activity);
        }
    }

    /**
     * 设置皮肤
     *
     * @param skin 皮肤包的文件路径
     */
    public boolean setAppSkin(String skin) {
        if (skin == null || skin.isEmpty()) {
            restoreDefaultSkin();
            return false;
        }
        File file = new File(skin);
        if (!file.exists() || file.isDirectory() || file.length() == 0) {
            restoreDefaultSkin();
            return false;
        }
        try {
            PackageManager pm = app.getPackageManager();
            PackageInfo skinPkgInfo = pm.getPackageArchiveInfo(skin, PackageManager.GET_ACTIVITIES);
            skinPackageName = skinPkgInfo.packageName;

            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, skin);
            skinResources = new Resources(assetManager, app.getResources().getDisplayMetrics(), app.getResources().getConfiguration());
            currentSkin = skin;
            saveLatestSkin(skin);
            for (SkinActivityRecord activity : activityStack) {
                reloadSkin(activity);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 是否支持的属性
     */
    public boolean isSupportedAttribute(String attr) {
        if (attr == null || attr.isEmpty()) {
            return false;
        }
        return attrSet.contains(attr);
    }

    /**
     * 重新加载本页面的皮肤
     */
    private void reloadSkin(SkinActivityRecord rc) {
        if (rc != null && !Objects.equals(getCurrentSkin(), rc.getCurrentSkin())) {
            for (SkinView skinView : rc.getSkinViewList()) {
                applySkin(skinView);
            }
            rc.setCurrentSkin(getCurrentSkin());
        }
    }

    /**
     * 应用皮肤到View中
     */
    private void applySkin(SkinView skinView) {
        View view = skinView.getView();
        List<SkinAttr> attrList = skinView.getAttrs();
        if (attrList != null) {
            for (SkinAttr attr : attrList) {
                if (attr.getSkinResId() == 0 && skinResources != null) {
                    attr.setSkinResId(skinResources.getIdentifier(attr.getSkinResEntryName(), attr.getSkinResType(), skinPackageName));
                }
                if (attr.getSkinResId() == 0) {
                    continue;
                }
                switch (attr.getAttrName()) {
                    case ATTR_NAME_SRC:
                        if (view instanceof ImageView
                            && RES_TYPE_NAME_DRAWABLE.equals(attr.getSkinResType())) {
                            ((ImageView) view).setImageDrawable(getSkinDrawable(attr));
                        }
                        break;
                    case ATTR_NAME_BACKGROUND:
                        if (RES_TYPE_NAME_DRAWABLE.equals(attr.getSkinResType())) {
                            view.setBackground(getSkinDrawable(attr));
                        } else if (RES_TYPE_NAME_COLOR.equals(attr.getSkinResType())) {
                            view.setBackgroundColor(getSkinColor(attr));
                        }
                        break;
                    case ATTR_NAME_TEXT_COLOR:
                        if (view instanceof TextView
                            && RES_TYPE_NAME_COLOR.equals(attr.getSkinResType())) {
                            ((TextView) view).setTextColor(getSkinColor(attr));
                        }
                        break;
                    case ATTR_NAME_DRAWABLE_LEFT:
                    case ATTR_NAME_DRAWABLE_START:
                    case ATTR_NAME_DRAWABLE_TOP:
                    case ATTR_NAME_DRAWABLE_BOTTOM:
                    case ATTR_NAME_DRAWABLE_RIGHT:
                    case ATTR_NAME_DRAWABLE_END:
                        if (view instanceof TextView && RES_TYPE_NAME_DRAWABLE.equals(attr.getSkinResType())) {
                            Drawable[] drawables = ((TextView) view).getCompoundDrawables();
                            switch (attr.getAttrName()) {
                                case ATTR_NAME_DRAWABLE_TOP:
                                    drawables[1] = getSkinDrawable(attr);
                                    break;
                                case ATTR_NAME_DRAWABLE_BOTTOM:
                                    drawables[3] = getSkinDrawable(attr);
                                    break;
                                case ATTR_NAME_DRAWABLE_LEFT:
                                case ATTR_NAME_DRAWABLE_START:
                                    drawables[0] = getSkinDrawable(attr);
                                    break;
                                case ATTR_NAME_DRAWABLE_RIGHT:
                                case ATTR_NAME_DRAWABLE_END:
                                    drawables[2] = getSkinDrawable(attr);
                                    break;
                            }
                            ((TextView) view).setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
                        }
                        break;
                    default:
                }
            }
        }
    }

    private Drawable getSkinDrawable(SkinAttr attr) {
        Drawable drawable = null;
        if (skinResources != null) {
            drawable = ResourcesCompat.getDrawable(skinResources, attr.getSkinResId(), null);
        } else {
            drawable = ResourcesCompat.getDrawable(app.getResources(), attr.getOriginResId(), null);
        }
        return drawable;
    }

    private Integer getSkinColor(SkinAttr attr) {
        int color;
        if (skinResources != null) {
            color = ResourcesCompat.getColor(skinResources, attr.getSkinResId(), null);
        } else {
            color = ResourcesCompat.getColor(app.getResources(), attr.getOriginResId(), null);
        }
        return color;
    }

    private String getLatestSkin() {
        SharedPreferences sp = app.getSharedPreferences("skin.db", Context.MODE_PRIVATE);
        return sp.getString("skin", null);
    }

    private void saveLatestSkin(String skin) {
        SharedPreferences sp = app.getSharedPreferences("skin.db", Context.MODE_PRIVATE);
        sp.edit().putString("skin", skin).apply();
    }

    public SkinActivityRecord getActivityRecord(Context context) {
        if (activityStack.isEmpty()) {
            return null;
        }
        if (context instanceof Activity) {
            for (int i = activityStack.size() - 1; i >= 0; i--) {
                SkinActivityRecord r = activityStack.get(i);
                if ((r.getActivity() == context)) {
                    return r;
                }
            }
        } else if (context instanceof ContextWrapper) {
            return getActivityRecord(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        SkinActivityRecord skinActivityRecord = new SkinActivityRecord(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        Class<LayoutInflaterCompat> compatClass = LayoutInflaterCompat.class;
        Class<LayoutInflater> inflaterClass = LayoutInflater.class;
        try {
            Field sCheckedField = compatClass.getDeclaredField("sCheckedField");
            sCheckedField.setAccessible(true);
            sCheckedField.setBoolean(inflater, false);
            Field mFactory = inflaterClass.getDeclaredField("mFactory");
            mFactory.setAccessible(true);
            Field mFactory2 = inflaterClass.getDeclaredField("mFactory2");
            mFactory2.setAccessible(true);
            SkinViewCreateFactory2 factory = new SkinViewCreateFactory2();
            mFactory2.set(inflater, factory);
            mFactory.set(inflater, factory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        activityStack.push(skinActivityRecord);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        super.onActivityResumed(activity);
        // 皮肤包变化后，重新加载本页的皮肤资源
        reloadSkin(getActivityRecord(activity));
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        activityStack.remove(getActivityRecord(activity));
    }
}
