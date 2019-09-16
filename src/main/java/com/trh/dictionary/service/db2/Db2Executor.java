package com.trh.dictionary.service.db2;

import com.mysql.jdbc.StringUtils;
import com.trh.dictionary.bean.ColumnInfo;
import com.trh.dictionary.bean.IndexInfo;
import com.trh.dictionary.bean.TableInfo;
import com.trh.dictionary.util.SqlExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TangXu
 * @create 2019-09-02 17:32
 * @description:
 */
public class Db2Executor {

    private static Logger logger = LoggerFactory.getLogger(Db2Executor.class);

//    public static void main(String[] args) throws SQLException, ClassNotFoundException {
//        Connection connection = SqlExecutor.newDB2Connection("192.168.171.230", "TEST", "db2", "system");
//        List<TableInfo> db2Tables = getDB2Tables(connection, "TEST");
//        for (TableInfo table : db2Tables) {
//            getDb2Columns(connection, table.getTableName(), "TEST");
//            getTableIndexMap(connection, table.getTableName());
//        }
//    }

    /**
     * 查询表结构
     *
     * @param connection
     * @param table
     * @param schema
     * @throws SQLException
     */
    public static List<ColumnInfo> getDb2Columns(Connection connection, String table, String schema) throws SQLException {
        String sql = "SELECT " +
                " T.COLNO, " +
                " T.COLNAME, " +
                " T.REMARKS, " +
                " T.TYPENAME, " +
                " T.LENGTH, " +
                " T.SCALE, " +
                " T.DEFAULT, " +
                " T.NULLS " +
                "FROM " +
                " SYSCAT.COLUMNS T " +
                "WHERE " +
                " T.TABSCHEMA =  '" + schema + "'" +
                " AND T.TABNAME =  '" + table + "'" +
                " ORDER BY T.COLNO";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
        while (rs.next()) {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setOrder(rs.getInt("COLNO") + 1);
            columnInfo.setName(rs.getString("COLNAME").trim());
            columnInfo.setDescription(rs.getString("REMARKS").trim());
            columnInfo.setIsNull(rs.getString("NULLS").trim());
            columnInfo.setDefaultValue(StringUtils.isNullOrEmpty(rs.getString("DEFAULT")) ? "" : rs.getString("DEFAULT").trim());
            columnInfo.setType(rs.getString("TYPENAME").trim() + "(" + rs.getString("LENGTH").trim() + ")");
            columnInfo.setIsIndex(getTablePrimaryKey(connection, table, rs.getString("COLNAME").trim()));
            columns.add(columnInfo);
        }
        SqlExecutor.releaseResource(null, null, rs, statement);
        return columns;
    }

    /**
     * 判断是否是索引
     *
     * @param connection
     * @param table
     * @param columnName
     * @return
     * @throws SQLException
     */
    public static int isIndex(Connection connection, String table, String columnName) throws SQLException {
        List<Map<String, Object>> tableIndexes = getTableIndexMap(connection, table);
        for (Map map : tableIndexes) {
            if (map.containsKey(columnName)) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 获取表信息
     *
     * @param connection
     * @param table
     * @return
     * @throws SQLException
     */
    public static List<Map<String, Object>> getTableIndexMap(Connection connection, String table) throws SQLException {
        List<Map<String, Object>> indexList = new ArrayList<Map<String, Object>>();
        String sql = "SELECT INDNAME, COLNAMES, UNIQUERULE FROM SYSCAT.INDEXES WHERE TABNAME = '" + table + "'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Map<String, Object> indexMap = new HashMap<String, Object>();
            String indexName = rs.getString("INDNAME");
            String columnName = rs.getString("COLNAMES");
            columnName = columnName.substring(1);
//             String uniqueRule = rs.getString("UNIQUERULE");
            indexMap.put(columnName, indexName);
            indexList.add(indexMap);
        }
        SqlExecutor.releaseResource(null, null, rs, statement);
        return indexList;
    }

    /**
     * 获取表信息
     *
     * @param connection
     * @param table
     * @return
     * @throws SQLException
     */
    public static List<IndexInfo> getTableIndexList(Connection connection, String table) throws SQLException {
        List<IndexInfo> indexList = new ArrayList<IndexInfo>();
        String sql = "SELECT IID, INDNAME, COLNAMES, UNIQUERULE FROM SYSCAT.INDEXES WHERE TABNAME = '" + table + "'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            IndexInfo indexInfo = new IndexInfo();
            String indexName = rs.getString("INDNAME");
            String columnName = rs.getString("COLNAMES");
            columnName = columnName.substring(1);
            String uniqueRule = rs.getString("UNIQUERULE");
            indexInfo.setType(uniqueRule);
            indexInfo.setName(indexName);
            indexInfo.setContainKey(columnName);
            int tablePrimaryKey = getTablePrimaryKey(connection, table, columnName);
            indexInfo.setIsIndex(tablePrimaryKey);
            indexInfo.setOrder(rs.getInt("IID"));
            indexList.add(indexInfo);
        }
        SqlExecutor.releaseResource(null, null, rs, statement);
        return indexList;
    }

    /**
     * 判断是否是主鍵
     *
     * @param connection
     * @param table
     * @param columnName
     * @return
     * @throws SQLException
     */
    public static int getTablePrimaryKey(Connection connection, String table, String columnName) throws SQLException {
        String sql = "SELECT CONSTNAME, COLNAME FROM SYSCAT.KEYCOLUSE WHERE TABNAME = '" + table + "'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            String primaryKey = rs.getString("CONSTNAME");
            String column_name = rs.getString("COLNAME");
            if (column_name.equalsIgnoreCase(columnName)) {
                return 1;
            }
        }
        SqlExecutor.releaseResource(null, null, rs, statement);
        return 0;
    }

    public static List<TableInfo> getDB2Tables(String host, int port, String schema, String user, String password) throws SQLException, ClassNotFoundException {
        Connection connection = SqlExecutor.newDB2Connection(host, port, schema, user, password);
        List<TableInfo> db2Tables = getDB2Tables(connection, schema);
        return db2Tables;
    }

    /**
     * 获取表
     *
     * @param connection
     * @param schema
     * @return
     * @throws SQLException
     */
    public static List<TableInfo> getDB2Tables(Connection connection, String schema) throws SQLException {
        List<TableInfo> tables = new ArrayList<TableInfo>();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables(null, schema, "%", new String[]{"TABLE"});
        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            String remarks = rs.getString("REMARKS");
            if (StringUtils.isNullOrEmpty(tableName)) {
                continue;
            }
            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableName(tableName);
            tableInfo.setDescription(remarks);
            tableInfo.setColumnList(getDb2Columns(connection, tableName, schema));
            tableInfo.setIndexInfoList(getTableIndexList(connection, tableName));
            tables.add(tableInfo);
        }
        SqlExecutor.releaseResource(null, null, rs, null);
        return tables;
    }

    /**
     * 获取数据库实例集合
     *
     * @param conn
     * @return
     */
    public static List<String> databases(Connection conn) throws SQLException {
        if (null == conn) {
            logger.info("connection can not be null");
            throw new SQLException("connection can not be null");
        }
        String sql = " select SCHEMANAME, OWNER, CREATE_TIME from syscat.schemata WHERE OWNERTYPE = 'U' ";
        List<String> schemas = new ArrayList<>();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            String schema = rs.getString("SCHEMANAME");
            if (!StringUtils.isNullOrEmpty(schema)) {
                schemas.add(schema);
            }
        }
        SqlExecutor.releaseResource(null, null, rs, statement);
        return schemas;
    }
}