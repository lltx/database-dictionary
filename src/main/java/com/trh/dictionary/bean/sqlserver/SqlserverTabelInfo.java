package com.trh.dictionary.bean.sqlserver;

import com.trh.dictionary.bean.ColumnInfo;
import com.trh.dictionary.bean.IndexInfo;

import java.util.List;

/**
 * @author zhou
 * @create 2019-08-29 16:12
 * @description:
 */
public class SqlserverTabelInfo {

    /**
     * 表名
     */
    private String tableName= "";

    private String value= "";

    /**
     * 所有列名
     */
    private List<SqlserverColumnInfo> columnList;

    private List<SqlserverIndexInfo> indexInfoList;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<SqlserverColumnInfo> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<SqlserverColumnInfo> columnList) {
        this.columnList = columnList;
    }

    public List<SqlserverIndexInfo> getIndexInfoList() {
        return indexInfoList;
    }

    public void setIndexInfoList(List<SqlserverIndexInfo> indexInfoList) {
        this.indexInfoList = indexInfoList;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if(null==value){
            value="";
        }
        this.value = value;
    }
}