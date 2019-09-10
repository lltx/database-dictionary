package com.trh.dictionary.service.mysql;

import com.trh.dictionary.bean.ColumnInfo;
import com.trh.dictionary.bean.IndexInfo;
import com.trh.dictionary.bean.TableInfo;
import com.trh.dictionary.dao.ConnectionFactory;
import com.trh.dictionary.service.BuildPDF;
import com.trh.dictionary.util.ColumnBasicEnum;
import com.trh.dictionary.util.SignEnum;
import com.trh.dictionary.util.TableBasicEnum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mysqlpdf生成
 *
 * @author
 * @create 2019-09-09 11:10
 */
public class BuildMysqlPdf {
    static Logger logger = LoggerFactory.getLogger(BuildMysqlPdf.class);

    /**
     * 下载pdf
     * @param ip
     * @param dbName
     * @param port
     * @param userName
     * @param passWord
     */
    public static void MakeMysqlPdf(String ip, String dbName, String port, String userName, String passWord, HttpServletResponse response) {
        try {
            //得到生成数据
            String url = "jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC";
            Connection connection = ConnectionFactory.getConnection(url, userName, passWord, "mySql");
            List<TableInfo> list = getBuildPdfTableData(getTables(connection, dbName));
            if (list.size() == 0) {
                return;
            }
            BuildPDF.getDocumentBuild(list, response);
        } catch (Exception e) {
            logger.error("生成MysqlPDF失败.......", e);
        }
    }
    /**
     * 生成PDF
     *
     * @param ip       ：数据库连接的IP  例如：127.0.0.1 或者 localhost
     * @param dbName   例如: test
     * @param port     例如: 3306
     * @param userName 例如: root
     * @param passWord 例如: root
     * @param filePath 例如:  D:\ideaspace\export_dbInfo\src\main\resources\
     * @param pdfName  例如:  testPDF
     */
    public static void MakePdf(String ip, String dbName, String port, String userName, String passWord, String filePath, String pdfName) {
        try {
            //得到生成数据
            String url = "jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC";
            Connection connection = ConnectionFactory.getConnection(url, userName, passWord, "mySql");
            List<TableInfo> list = BuildMysqlPdf.getBuildPdfTableData(BuildMysqlPdf.getTables(connection, dbName));
            if (list.size() == 0) {
                return;
            }
            FileUtils.forceMkdir(new File(filePath));
            //带目录
            BuildPDF.build(filePath, list, pdfName);
        } catch (Exception e) {
            logger.error("生成PDF失败.......", e);
        }
    }
    /**
     * pg得到数据库所有库名
     *
     * @param ip
     * @param dbName
     * @param port
     * @param userName
     * @param passWord
     * @return
     */
    public static List<String> getDataBaseName(String ip, String dbName, String port, String userName, String passWord) {
        //得到生成数据
        String url = "jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC";
        try {
            Connection connection = ConnectionFactory.getConnection(url, userName, passWord, "mySql");
            Statement statement = connection.createStatement();
            List<String> dbList = new ArrayList<>(8);
            ResultSet resultSet = null;
            String sql = " show databases ";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String dbNames = resultSet.getString(1);
                dbList.add(dbNames);
            }
            return dbList;
        } catch (Exception e) {
            logger.error("查询数据库名字集合异常", e);
            return new ArrayList<>(1);
        }
    }

    /**
     * 得到构建pdf文件的数据
     *
     * @param tables 获取构建pdf需要的表信息的集合
     * @return List
     */
    public static List<TableInfo> getBuildPdfTableData(List<Map<String, Object>> tables) {
        //循环处理表
        List<TableInfo> resultList = new ArrayList<TableInfo>();
        for (Map<String, Object> table : tables) {
            TableInfo tableInfo = (TableInfo) table.get("tableInfo");
            String tableName = tableInfo.getTableName();
            String createTable = (String) table.get("createTable");
            tableInfo.setTableName(tableName);
            //处理表信息字符
            tableInfo = takeTableInfo(tableInfo, createTable);
            resultList.add(tableInfo);
        }
        return resultList;
    }


    /**
     * 得到表的基本数据
     *
     * @param tableInfo  设置单张表构建pdf的数据
     * @param tableInfos 表信息字符串
     * @return TableInfo 表信息对象
     */
    public static TableInfo takeTableInfo(TableInfo tableInfo, String tableInfos) {
        //去掉回车
        tableInfos = BuildPDF.dest(tableInfos, SignEnum.back_quote.getDesc());
        tableInfos = BuildPDF.dest(tableInfos, SignEnum.single_quotation_marks.getDesc());
        String[] test = tableInfos.split("\n");
        //处理字符串
        String str = test[test.length - 1];
        str = BuildPDF.dest(str, SignEnum.right_brackets.getDesc()).trim();
        String[] table = str.split(" ");
        List<IndexInfo> indexInfoList = new ArrayList<IndexInfo>();
        int indexInfoSize = test.length;
        for (int i = 0; i < indexInfoSize - 1; i++) {
            String temp = test[i];
            //主键索引
            if (temp.contains(TableBasicEnum.PRIMARY_KEY.getDesc())) {
                temp = BuildPDF.dest(temp, SignEnum.left_brackets.getDesc());
                temp = BuildPDF.dest(temp, SignEnum.right_brackets.getDesc());
                String[] tempForIndex = temp.trim().split(" ");
                String containKey = tempForIndex[tempForIndex.length - 1];
                IndexInfo indexInfo1 = new IndexInfo(TableBasicEnum.WORD_PRIMARY.getDesc(), TableBasicEnum.WORD_PRIMARY.getDesc(), BuildPDF.drop(containKey));
                indexInfo1.setIsIndex(1);
                indexInfoList.add(indexInfo1);
            }
            //唯一索引
            if (temp.contains(TableBasicEnum.UNIQUE_KEY.getDesc())) {
                String[] tempForIndex = temp.trim().split(" ");
                String containKey = tempForIndex[tempForIndex.length - 1];
                String type = tempForIndex[0] + tempForIndex[1];
                String name = tempForIndex[2];
                containKey = BuildPDF.dest(containKey, SignEnum.left_brackets.getDesc());
                containKey = BuildPDF.dest(containKey, SignEnum.right_brackets.getDesc());
                containKey = BuildPDF.dest(containKey, SignEnum.single_quotation_marks.getDesc());
                IndexInfo indexInfo1 = new IndexInfo(name, type, BuildPDF.drop(containKey));
                indexInfoList.add(indexInfo1);
            }
            //普通索引
            if (temp.contains(TableBasicEnum.KEY.getDesc())) {
                String[] tempForIndex = temp.trim().split(" ");
                if (!tempForIndex[0].equalsIgnoreCase(TableBasicEnum.WORD_key.getDesc())) {
                    continue;
                }
                String containKey = tempForIndex[tempForIndex.length - 1];
                String type = tempForIndex[0];
                String name = tempForIndex[1];
                containKey = BuildPDF.dest(containKey, SignEnum.left_brackets.getDesc());
                containKey = BuildPDF.dest(containKey, SignEnum.right_brackets.getDesc());
                containKey = BuildPDF.dest(containKey, SignEnum.single_quotation_marks.getDesc());
                IndexInfo indexInfo1 = new IndexInfo(name, type, BuildPDF.drop(containKey));
                indexInfoList.add(indexInfo1);
            }
            //全文索引
            if (temp.contains("FULLTEXT KEY")) {
                String[] tempForIndex = temp.trim().split(" ");
                String containKey = tempForIndex[tempForIndex.length - 1];
                String type = tempForIndex[0];
                String name = tempForIndex[2];
                containKey = BuildPDF.dest(containKey, SignEnum.left_brackets.getDesc());
                containKey = BuildPDF.dest(containKey, SignEnum.right_brackets.getDesc());
                containKey = BuildPDF.dest(containKey, SignEnum.single_quotation_marks.getDesc());
                IndexInfo indexInfo1 = new IndexInfo(name, type, BuildPDF.drop(containKey));
                indexInfoList.add(indexInfo1);
            }
        }
        tableInfo.setIndexInfoList(indexInfoList);
        //得到表字符集和ENGINE、表注释
        for (int i = 0; i < table.length; i++) {
            String oneTemp = table[i];
            //引擎
            if (oneTemp.contains(TableBasicEnum.ENGINE.getDesc())) {
                tableInfo.setStorageEngine(BuildPDF.dropSign(table[i]));
                continue;
            } else {
                if (tableInfo.getStorageEngine() == null) {
                    tableInfo.setStorageEngine("");
                }
            }
            //字符集
            if (oneTemp.contains(TableBasicEnum.CHARSET.getDesc())) {
                tableInfo.setOrderType(BuildPDF.dropSign(table[i]));
                continue;
            } else {
                if (tableInfo.getOrderType() == null) {
                    tableInfo.setOrderType("");
                }
            }

            //描述
            if (oneTemp.contains(TableBasicEnum.COMMENT.getDesc())) {
                tableInfo.setDescription(BuildPDF.dropSign(table[i]));
                continue;
            } else {
                if (tableInfo.getDescription() == null) {
                    tableInfo.setDescription("");
                }
            }
        }

        return tableInfo;
    }


    /**
     * 获取数据库所有表信息
     *
     * @param connection
     * @param dbName
     * @return
     */
    public static List<Map<String, Object>> getTables(Connection connection, String dbName) {
        Statement statement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> tables = new ArrayList<Map<String, Object>>();
        try {
            //获取表名
            statement = connection.createStatement();
            String sql = "show full tables FROM `" + dbName + "`"+"where Table_type = 'BASE TABLE'";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Map<String, Object> resMap = new HashMap<String, Object>(2);
                TableInfo tableInfo = new TableInfo();
                //表名
                String tableName = resultSet.getString(1);
                //获取表信息
                String sqlTableInfo = "SHOW CREATE TABLE `" + tableName + "`";
                String createTable = getTableInfo(connection, sqlTableInfo);
                tableInfo.setTableName(tableName);
                resMap.put("createTable", createTable);
                String sql1 = "show full columns from `" + tableName + "`";
                List<ColumnInfo> columnInfos = getTableBaseInfo(connection, sql1);
                tableInfo.setColumnList(columnInfos);
                resMap.put("tableInfo", tableInfo);
                tables.add(resMap);
            }
            return tables;
        } catch (Exception e) {
            e.printStackTrace();
            return tables;
        } finally {
            try {
                ConnectionFactory.releaseResource(connection, null
                        , resultSet, statement);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取单个表全部信息
     *
     * @param connection   数据库连接
     * @param sqlTableInfo 表信息sql
     * @return
     * @throws SQLException
     */
    public static String getTableInfo(Connection connection, String sqlTableInfo) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlTableInfo);
        String table = "";
        while (resultSet.next()) {
            table = resultSet.getString("Create Table");
        }
        return table;
    }

    /**
     * 设置表的基本信息
     *
     * @param connection 数据库连接
     * @param sql
     * @return
     * @throws Exception
     */
    public static List<ColumnInfo> getTableBaseInfo(Connection connection, String sql) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>();
        int order = 1;
        while (resultSet.next()) {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setName(resultSet.getString(ColumnBasicEnum.Field.getDesc()) + " " + resultSet.getString(ColumnBasicEnum.Extra.getDesc()));
            columnInfo.setType(resultSet.getString(ColumnBasicEnum.Type.getDesc()));
            columnInfo.setDescription(resultSet.getString(ColumnBasicEnum.Comment.getDesc()));
            columnInfo.setIsNull(resultSet.getString(ColumnBasicEnum.Null.getDesc()));
            columnInfo.setOrder(order++);
            columnInfo.setDefaultValue(resultSet.getString(ColumnBasicEnum.Default.getDesc()));
            columnInfos.add(columnInfo);
            if (null == columnInfo.getDefaultValue()) {
                columnInfo.setDescription("");
            }
        }
        statement.close();
        resultSet.close();
        return columnInfos;
    }
}
