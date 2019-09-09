package com.trh.dictionary.service.mysql;

import com.trh.dictionary.dao.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * mysqlpdf生成
 *
 * @author
 * @create 2019-09-09 11:10
 */
public class BuildMysqlPdf {
    static Logger logger = LoggerFactory.getLogger(BuildMysqlPdf.class);

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
}
