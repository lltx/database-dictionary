package com.trh.dictionary.service.postgreSQL;

import com.trh.dictionary.bean.ColumnInfo;
import com.trh.dictionary.bean.IndexInfo;
import com.trh.dictionary.bean.TableInfo;
import com.trh.dictionary.dao.ConnectionFactory;
import com.trh.dictionary.service.BuildPDF;
import com.trh.dictionary.util.TableBasicEnum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 构建pg数据库pdf
 *
 * @author wangyu
 * @create 2019-09-04 10:29
 */
public class BuildPgSqlPdf {
    static Logger logger = LoggerFactory.getLogger(BuildPgSqlPdf.class);

    public static void buildPdf(String ip, String dbName, String port, String userName, String passWord, HttpServletResponse response) throws Exception {
        //得到生成数据
        String url = "jdbc:postgresql://" + ip + ":" + port + "/" + dbName;
        Connection connection = ConnectionFactory.getConnection(url, userName, passWord, "pgSql");
        List<TableInfo> list = getBuildPdfTableData(connection);
        if (list.size() == 0) {
            return;
        }

        try {
            BuildPDF.getDocumentBuild( list, response);
        } catch (Exception e) {
            logger.error("生成PG数据库pdf异常", e);
        }
    }

    public static List<TableInfo> getBuildPdfTableData(Connection connection) {
        String sql = "SELECT   pg_tables.schemaname,pg_tables.tablename,cast(obj_description(relfilenode,'pg_class') as varchar) as comment   FROM   pg_tables LEFT JOIN pg_class on relname=tablename WHERE   tablename   NOT   LIKE   'pg%' AND tablename NOT LIKE 'sql_%'";

        ResultSet resultSet;
        List<TableInfo> tableInfos = new ArrayList<TableInfo>(64);
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                TableInfo tableInfo = new TableInfo();
                String schemaName = resultSet.getString(1);
                String tableName = resultSet.getString(2);
                String desc = resultSet.getString(3);
                tableInfo.setDescription(desc);
                tableInfo.setStorageEngine(schemaName);
                tableInfo.setTableName(tableName);
                String sqlColumn = "SELECT A.attname AS 列名,concat_ws('',t.typname,SUBSTRING(format_type(A.atttypid,A.atttypmod) from '\\(.*\\)')),(CASE WHEN A.attlen> 0 THEN A.attlen ELSE A.atttypmod-4 END) AS 长度,A.attnotnull AS 是否可为空,d.adsrc AS 默认值,col_description (A.attrelid,A.attnum) AS 备注 FROM pg_class C,pg_attribute A LEFT JOIN (SELECT A.attname,ad.adsrc FROM pg_class C,pg_attribute A,pg_attrdef ad WHERE relname='" + tableName + "' AND ad.adrelid=C.oid AND adnum=A.attnum AND attrelid=C.oid) AS d ON A.attname=d.attname  LEFT JOIN pg_type t on A.atttypid = t.oid WHERE C.relname='" + tableName + "' AND A.attrelid=C.oid AND A.attnum> 0;";
                String sqlIndex = "SELECT A.SCHEMANAME,A.TABLENAME,A.INDEXNAME,A.INDEXDEF,B.AMNAME,C.INDISUNIQUE,C.INDISPRIMARY,C.INDISCLUSTERED,D.DESCRIPTION FROM PG_AM B LEFT JOIN PG_CLASS F ON B.OID=F.RELAM LEFT JOIN PG_STAT_ALL_INDEXES E ON F.OID=E.INDEXRELID LEFT JOIN PG_INDEX C ON E.INDEXRELID=C.INDEXRELID LEFT OUTER JOIN PG_DESCRIPTION D ON C.INDEXRELID=D.OBJOID,PG_INDEXES A WHERE A.SCHEMANAME=E.SCHEMANAME AND A.TABLENAME=E.RELNAME AND A.INDEXNAME=E.INDEXRELNAME AND E.RELNAME='" + tableName + "'";
                //设置列信息
                tableInfo = getTableBaseInfo(connection, tableInfo, sqlColumn, sqlIndex);
                tableInfos.add(tableInfo);
            }
        } catch (Exception e) {
            logger.error("执行sql异常", e);
        }
        return tableInfos;
    }

    /**
     * 得到列名和索引
     *
     * @param connection
     * @param sqlColumn
     * @param sqlIndex
     * @return
     */
    public static TableInfo getTableBaseInfo(Connection connection, TableInfo tableInfo, String sqlColumn, String sqlIndex) {
        List<IndexInfo> indexInfos = new ArrayList<>(8);
        List<ColumnInfo> columnInfos = new ArrayList<>(16);
        ResultSet resultSet;
        ResultSet resultSet1;
        //列
        try {
            Statement statement = connection.createStatement();
            Statement statement1 = connection.createStatement();
            resultSet = statement.executeQuery(sqlIndex);
            while (resultSet.next()) {
                IndexInfo indexInfo = new IndexInfo();
                //索引名
                indexInfo.setName(resultSet.getString(3));
                //索引包含字段
                indexInfo.setContainKey(getIndexKey(resultSet.getString(4)));
                //索引类型
                boolean isPk = resultSet.getString(7).trim().equals("t");
                if (isPk) {
                    indexInfo.setType(TableBasicEnum.WORD_PRIMARY.getDesc());
                    indexInfo.setIsIndex(1);
                }
                if (resultSet.getString(6).equals("t") && !isPk) {
                    indexInfo.setType("UNIQUE");
                    indexInfo.setIsIndex(0);
                } else if (resultSet.getString(6).equals("f") && !isPk) {
                    indexInfo.setType(" ");
                    indexInfo.setIsIndex(0);
                }
                indexInfos.add(indexInfo);
            }
            resultSet1 = statement1.executeQuery(sqlColumn);
            int order = 1;
            while (resultSet1.next()) {
                ColumnInfo columnInfo = new ColumnInfo();
                String name = resultSet1.getString(1);
                for (IndexInfo info : indexInfos) {
                    if (info.getContainKey().equals(name)) {
                        columnInfo.setIsIndex(1);
                    }
                }
                columnInfo.setOrder(order);
                columnInfo.setName(name);
                columnInfo.setType(resultSet1.getString(2));
                boolean isNull = resultSet1.getString(4).equals("t");
                if (isNull) {
                    columnInfo.setIsNull("NO");
                } else {
                    columnInfo.setIsNull("YES");
                }

                columnInfo.setDefaultValue(resultSet1.getString(5));
                columnInfo.setDescription(resultSet1.getString(6));
                if (columnInfo.getDefaultValue() == null) {
                    columnInfo.setDefaultValue("");
                }
                columnInfos.add(columnInfo);
                order++;
            }
        } catch (Exception e) {
            logger.error("获取列信息和索引信息异常", e);
        }
        tableInfo.setColumnList(columnInfos);
        tableInfo.setIndexInfoList(indexInfos);
        return tableInfo;
    }

    /**
     * 得到括号内的内容
     *
     * @param desc
     * @return
     */
    public static String getIndexKey(String desc) {
        int start = desc.indexOf("(");
        int end = desc.indexOf(")");
        String key = desc.substring(start + 1, end);
        return BuildPDF.dest(key, "\"");
    }

    public static String getPgMarkdown(String ip, String dbName, String port, String userName, String passWord) throws Exception {
        //得到生成数据
        String url = "jdbc:postgresql://" + ip + ":" + port + "/" + dbName;
        Connection connection = ConnectionFactory.getConnection(url, userName, passWord, "pgSql");
        List<TableInfo> list = getBuildPdfTableData(connection);
        if (list.size() == 0) {
            return "## 数据库无数据";
        }
        return BuildPDF.writeMarkdown(list);
    }

    /**
     * mysql得到数据库所有库名
     * @param ip
     * @param dbName
     * @param port
     * @param userName
     * @param passWord
     * @return
     */
    public static List<String> getDataBaseName(String ip, String dbName, String port, String userName, String passWord) {
        //得到生成数据
        String url = "jdbc:postgresql://" + ip + ":" + port + "/" + dbName;
        try {
            Connection connection = ConnectionFactory.getConnection(url, userName, passWord, "pgSql");
            Statement statement = connection.createStatement();
            List<String> dbList = new ArrayList<>(8);
            ResultSet resultSet = null;
            String sql = " SELECT datname FROM pg_database ";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()){
                String dbNames = resultSet.getString(1);
                dbList.add(dbNames);
            }
            return dbList;
        } catch (Exception e) {
            logger.error("查询数据库名字集合异常", e);
            return new ArrayList<>(1);
        }
    }
}
