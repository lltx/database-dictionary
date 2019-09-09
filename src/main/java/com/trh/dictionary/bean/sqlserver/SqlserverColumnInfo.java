package com.trh.dictionary.bean.sqlserver;

/**
 * @author zhou
 * @create 2019-08-29 16:13
 * @description:
 */
public class SqlserverColumnInfo {

    private String table_name= "";//表名

    private String column_num= "";//字段序号

    private String column_name= "";//字段名

    private String is_identity= "";//标识

    private String p_k= "";//主键

    private String type= "";//类型

    private String occupied_num= "";//占用字节数

    private String length= "";//长度

    private String scale= "";//小数位数

    private String is_null= "";//允许空

    private String default_value= "";//默认值

    private String decs= " ";//说明

    private String class_desc= " ";

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public String getColumn_num() {
        return column_num;
    }

    public void setColumn_num(String column_num) {
        this.column_num = column_num;
    }

    public String getColumn_name() {
        return column_name;
    }

    public void setColumn_name(String column_name) {
        this.column_name = column_name;
    }

    public String getIs_identity() {
        return is_identity;
    }

    public void setIs_identity(String is_identity) {
        this.is_identity = is_identity;
    }

    public String getP_k() {
        return p_k;
    }

    public void setP_k(String p_k) {
        this.p_k = p_k;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOccupied_num() {
        return occupied_num;
    }

    public void setOccupied_num(String occupied_num) {
        this.occupied_num = occupied_num;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getIs_null() {
        return is_null;
    }

    public void setIs_null(String is_null) {
        this.is_null = is_null;
    }

    public String getDefault_value() {
        return default_value;
    }

    public void setDefault_value(String default_value) {
        this.default_value = default_value;
    }

    public String getDecs() {
        return decs;
    }

    public void setDecs(String decs) {
        this.decs = decs;
    }

    public String getClass_desc() {
        return class_desc;
    }

    public void setClass_desc(String class_desc) {
        this.class_desc = class_desc;
    }

    @Override
    public String toString() {
        return "SqlserverColumnInfo{" +
                "table_name='" + table_name + '\'' +
                ", column_num='" + column_num + '\'' +
                ", column_name='" + column_name + '\'' +
                ", is_identity='" + is_identity + '\'' +
                ", p_k='" + p_k + '\'' +
                ", type='" + type + '\'' +
                ", occupied_num='" + occupied_num + '\'' +
                ", length='" + length + '\'' +
                ", scale='" + scale + '\'' +
                ", is_null='" + is_null + '\'' +
                ", default_value='" + default_value + '\'' +
                ", decs='" + decs + '\'' +
                ", class_desc='" + class_desc + '\'' +
                '}';
    }
}