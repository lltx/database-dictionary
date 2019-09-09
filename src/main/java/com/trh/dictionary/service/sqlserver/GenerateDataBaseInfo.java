package com.trh.dictionary.service.sqlserver;

import com.trh.dictionary.bean.ColumnInfo;
import com.trh.dictionary.bean.sqlserver.SqlserverColumnInfo;
import com.trh.dictionary.bean.sqlserver.SqlserverIndexInfo;
import com.trh.dictionary.bean.sqlserver.SqlserverTabelInfo;
import com.trh.dictionary.util.ColumnBasicEnum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhou
 * @create 2019-08-29 16:44
 * @description:
 */
public class GenerateDataBaseInfo {


    /*
     * @Author zhou
     * @Description 所有用户库
     * @Date 10:58 2019/9/9
     * @Param [connection, sqlTableInfo]
     * @return java.util.List<java.lang.String>
     **/
    public static List<String> getDataBaseList(Connection connection, String sqlTableInfo) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlTableInfo);
        List<String> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(resultSet.getString("name"));
        }
        resultSet.close();
        statement.close();
        return list;
    }

    /*
     * @Author zhou
     * @Description 生成数据库表名
     * @Date 16:46 2019/8/29
     * @Param [connection, sqlTableInfo]
     * @return java.util.List<com.trh.dictionary.bean.sqlserver.SqlserverTabelInfo>
     **/
    public static List<SqlserverTabelInfo> getTableInfo(Connection connection, String sqlTableInfo) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlTableInfo);
        List<SqlserverTabelInfo> list = new ArrayList<SqlserverTabelInfo>();
        while (resultSet.next()) {
            SqlserverTabelInfo sqlserverTabelInfo = new SqlserverTabelInfo();
            sqlserverTabelInfo.setTableName(resultSet.getString("name"));
            sqlserverTabelInfo.setValue(resultSet.getString("value"));
            list.add(sqlserverTabelInfo);
        }
        resultSet.close();
        statement.close();
        return list;
    }


    /*
     * @Author zhou
     * @Description 生成数据库表列名
     * @Date 17:16 2019/8/29
     * @Param [connection, sql]
     * @return java.util.List<com.trh.dictionary.bean.sqlserver.SqlserverColumnInfo>
     **/
    public static List<SqlserverColumnInfo> getColumnInfo(Connection connection, String sql) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        List<SqlserverColumnInfo> list = new ArrayList<SqlserverColumnInfo>();
        while (resultSet.next()) {
            SqlserverColumnInfo sqlserverColumnInfo = new SqlserverColumnInfo();
            sqlserverColumnInfo.setTable_name(resultSet.getString("table_name"));
            sqlserverColumnInfo.setColumn_num(resultSet.getString("column_num"));
            sqlserverColumnInfo.setColumn_name(resultSet.getString("column_name"));
            sqlserverColumnInfo.setIs_identity(resultSet.getString("is_identity"));
            sqlserverColumnInfo.setP_k(resultSet.getString("p_k"));
            sqlserverColumnInfo.setType(resultSet.getString("type"));
            sqlserverColumnInfo.setOccupied_num(resultSet.getString("occupied_num"));
            sqlserverColumnInfo.setLength(resultSet.getString("length"));
            sqlserverColumnInfo.setScale(resultSet.getString("scale"));
            sqlserverColumnInfo.setIs_null(resultSet.getString("is_null"));
            sqlserverColumnInfo.setDefault_value(resultSet.getString("default_value"));
            sqlserverColumnInfo.setDecs(resultSet.getString("decs"));
            String class_desc=resultSet.getString("class_desc");
            if("INDEX".equals(class_desc)){
                continue;
            }else{
                sqlserverColumnInfo.setClass_desc(resultSet.getString("class_desc"));
            }

            list.add(sqlserverColumnInfo);
        }
        resultSet.close();
        statement.close();
        return list;
    }

    /*
     * @Author zhou
     * @Description 生成数据库表索引
     * @Date 17:18 2019/8/29
     * @Param [connection, sql]
     * @return java.util.List<com.trh.dictionary.bean.sqlserver.SqlserverIndexInfo>
     **/
    public static List<SqlserverIndexInfo> getIndexInfo(Connection connection, String sql) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        List<SqlserverIndexInfo> list = new ArrayList<SqlserverIndexInfo>();
        while (resultSet.next()) {
            SqlserverIndexInfo sqlserverIndexInfo = new SqlserverIndexInfo();
            sqlserverIndexInfo.setIndex_name(resultSet.getString("index_name"));
            sqlserverIndexInfo.setIndex_desc(resultSet.getString("index_desc"));
            sqlserverIndexInfo.setIndex_keys(resultSet.getString("index_keys"));
            list.add(sqlserverIndexInfo);
        }
        resultSet.close();
        statement.close();
        return list;
    }



}