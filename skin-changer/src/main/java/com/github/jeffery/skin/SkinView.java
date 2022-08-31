package com.github.jeffery.skin;

import android.view.View;

import java.util.List;

/**
 * @author mxlei
 * @date 2022/8/28
 */
public class SkinView {
    private final View view;
    private final List<SkinAttr> attrs;

    public SkinView(View view, List<SkinAttr> attrs) {
        this.view = view;
        this.attrs = attrs;
    }

    public View getView() {
        return view;
    }

    public List<SkinAttr> getAttrs() {
        return attrs;
    }
}
