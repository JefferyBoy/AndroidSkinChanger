package com.github.jeffery.skin;

/**
 * @author mxlei
 * @date 2022/8/28
 */
public class SkinAttr {
    /**
     * 原app中的资源ID
     */
    private int originResId;
    /**
     * 皮肤app中的资源ID
     */
    private int skinResId;
    /**
     * 资源类型
     */
    private String skinResType;
    /**
     * 资源名称
     */
    private String skinResEntryName;
    /**
     * 属性名称
     */
    private String attrName;

    public int getOriginResId() {
        return originResId;
    }

    public void setOriginResId(int originResId) {
        this.originResId = originResId;
    }

    public int getSkinResId() {
        return skinResId;
    }

    public void setSkinResId(int skinResId) {
        this.skinResId = skinResId;
    }

    public String getSkinResType() {
        return skinResType;
    }

    public void setSkinResType(String skinResType) {
        this.skinResType = skinResType;
    }

    public String getSkinResEntryName() {
        return skinResEntryName;
    }

    public void setSkinResEntryName(String skinResEntryName) {
        this.skinResEntryName = skinResEntryName;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }
}
