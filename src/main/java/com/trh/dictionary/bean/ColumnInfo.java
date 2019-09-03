package com.trh.dictionary.bean;

/**
 * 列信息
 *
 * @author
 * @create 2019-07-25 14:23
 */
public class ColumnInfo {
    /**
     * 是否为主键
     */
    private int isIndex;
    /**
     * 序号
     */
    private int order;
    /**
     * 类型
     */
    private String name= "";
    /**
     * 类型
     */
    private String type= "";
    /**
     * 是否允许为空
     */
    private String isNull= "";
    /**
     * 默认值
     */
    private String defaultValue= "";
    /**
     * 描述
     */
    private String description= "";

    public void setIsIndex(int isIndex) {
        this.isIndex = isIndex;
    }

    public int getIsIndex() {
        return isIndex;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name==null){
            name = "";
        }
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type==null){
            type = "";
        }
        this.type = type;
    }

    public String getIsNull() {
        return isNull;
    }

    public void setIsNull(String isNull) {
        if (isNull==null){
            isNull = "";
        }
        this.isNull = isNull;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        if (defaultValue==null){
            defaultValue = "";
        }
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description==null){
            description = "";
        }
        this.description = description;
    }
}
