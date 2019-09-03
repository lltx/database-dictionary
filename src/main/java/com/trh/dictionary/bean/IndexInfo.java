package com.trh.dictionary.bean;

/**
 * 索引
 *
 * @author
 * @create 2019-07-29 15:03
 */
public class IndexInfo {

    /**
     * 是否为主键
     */
    private int isIndex=0;
    private int order;
    private String name= "";
    private String type= "";
    private String containKey= "";

    public IndexInfo() {
    }

    public IndexInfo(String name, String type, String containKey) {
        this.name = name;
        this.type = type;
        this.containKey = containKey;
    }

    public void setIsIndex(int isIndex) {
        this.isIndex = isIndex;
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
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContainKey() {
        return containKey;
    }

    public void setContainKey(String containKey) {
        this.containKey = containKey;
    }
}
