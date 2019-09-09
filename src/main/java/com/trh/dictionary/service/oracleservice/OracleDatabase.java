package com.trh.dictionary.service.oracleservice;

import com.trh.dictionary.bean.ColumnInfo;
import com.trh.dictionary.bean.IndexInfo;
import com.trh.dictionary.bean.TableInfo;
import com.trh.dictionary.dao.oracleservice.OracleJdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Chen.Chun
 * @program: cdtrh_group-database-dictionary-master
 * @description: 创建oracle链接
 * @create: 2019-08-30 11:00
 **/
public class OracleDatabase {

    //驱动
    private static String driver = "oracle.jdbc.driver.OracleDriver";

    //得到表信息
    public static List<TableInfo> getTableInfo(String url, String username, String password) throws Exception {
        List<TableInfo> list = new ArrayList<TableInfo>();
        PreparedStatement pstmt;
        Connection conn = null;
        try {
            //1.注册驱动
            Class.forName(driver);
            //2.建立连接
            conn = DriverManager.getConnection(url, username, password);
            //查询数据库下所有的表名
            pstmt = conn.prepareStatement("select t.table_name,u.COMMENTS from user_tables t LEFT JOIN user_tab_comments u ON t.Table_Name=u.Table_Name");
            //建立一个结果集，用来保存查询出来的结果
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TableInfo tableInfo = new TableInfo();
                //得到表名
                String tableName = rs.getString("TABLE_NAME");
                //得到表的注释
                String COMMENTS = rs.getString("COMMENTS");
                //通过表名查询主键索引名和列名
                PreparedStatement constraintNamePstmt = conn.prepareStatement("select cu.constraint_name,cu.column_name from user_cons_columns cu LEFT JOIN user_constraints au ON cu.constraint_name = au.constraint_name WHERE au.constraint_type = 'P' and au.table_name = '" + tableName + "'");
                ResultSet index_name = constraintNamePstmt.executeQuery();
                String stringName = "";
                String column_name = "";
                while (index_name.next()) {
                    stringName = index_name.getString(1);
                    column_name = index_name.getString(2);
                }
                constraintNamePstmt.close();
                //通过表名查询所有的列数据
                PreparedStatement columnIdPstmt = conn.prepareStatement("SELECT T.column_id,T.column_name,T.data_type,T.data_default,T.nullable,b.comments\n" +
                        "FROM USER_TAB_COLUMNS T LEFT JOIN user_col_comments b ON T.TABLE_NAME =b.table_name AND T.COLUMN_NAME =b.column_name  WHERE T.TABLE_NAME ='" + tableName + "'");
                ResultSet columnRs = columnIdPstmt.executeQuery();
                List<ColumnInfo> listColumn = new ArrayList<ColumnInfo>();
                while (columnRs.next()) {
                    ColumnInfo columnInfo = new ColumnInfo();
                    //得到列名
                    String columnName = columnRs.getString(2);
                    columnInfo.setName(columnName);
                    columnInfo.setIsIndex(0);
                    //得到主键的列名
                    if (column_name.equals(columnName)) {
                        columnInfo.setIsIndex(1);
                    }
                    //得到序号
                    int order = columnRs.getInt(1);
                    columnInfo.setOrder(order);
                    //得到类型
                    String type = columnRs.getString(3);
                    columnInfo.setType(type);
                    //得到默认值
                    String defaultValue = columnRs.getString(4);
                    columnInfo.setDefaultValue(defaultValue);
                    //是否为空
                    String isNull = columnRs.getString(5);
                    if (isNull.equalsIgnoreCase("N")) {
                        isNull = "NO";
                    } else {
                        isNull = "YES";
                    }
                    columnInfo.setIsNull(isNull);
                    //得到列的描述
                    String description = columnRs.getString(6);
                    columnInfo.setDescription(description);
                    listColumn.add(columnInfo);
                }
                //查询表的索引说明
                PreparedStatement rownumPstmt = conn.prepareStatement("SELECT rownum,rs.* FROM ( select t.index_name, i.index_type,listagg(t.column_name,',') within group (order by t.index_name) col_name from user_ind_columns t,user_indexes i where t.index_name = i.index_name " +
                        "and t.table_name = i.table_name and t.table_name = '" + tableName + "' GROUP BY t.index_name, i.index_type) rs ");
                ResultSet IndexRs = rownumPstmt.executeQuery();
                List<IndexInfo> listIndex = new ArrayList<IndexInfo>();
                while (IndexRs.next()) {
                    IndexInfo indexInfo = new IndexInfo();
                    String indexRsString = IndexRs.getString(2);
                    if (stringName.equalsIgnoreCase(indexRsString)) {
                        indexInfo.setIsIndex(1);
                    }
                    indexInfo.setOrder(IndexRs.getInt(1));
                    indexInfo.setName(indexRsString);
                    indexInfo.setType(IndexRs.getString(3));
                    indexInfo.setContainKey(IndexRs.getString(4));
                    listIndex.add(indexInfo);
                }
                IndexRs.close();
                columnIdPstmt.close();
                columnRs.close();
                rownumPstmt.close();
                index_name.close();
                tableInfo.setTableName(tableName);
                tableInfo.setDescription(COMMENTS);
                tableInfo.setColumnList(listColumn);
                tableInfo.setIndexInfoList(listIndex);
                list.add(tableInfo);
            }
            rs.close();
            pstmt.close();
        } finally {
            if (conn != null) {
                try {
                    //关闭链接
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }
}