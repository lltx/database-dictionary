package com.trh.dictionary.dao.sqlserver;

import com.trh.dictionary.service.BuildPDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * @author zhou
 * @create 2019-08-29 16:31
 * @description:
 */
public class SqlserverConnectionFactory {

    static Logger logger = LoggerFactory.getLogger(SqlserverConnectionFactory.class);

    public static Connection getConnection(String url, String userName, String passWord){
        Connection connection = null;
        //创建驱动
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //创建连接
        try {
            connection = DriverManager.getConnection(url, userName, passWord);
            if (connection.isClosed()) {
                logger.info("-------------------the connect is closed--------------");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    /**
     * 释放资源
     * @param connection 连接
     * @param preparedStatement
     * @param resultSet 结果集
     */
    public static void releaseResource(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet, Statement statement){
        if(null != resultSet){
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (null != statement){
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(null != preparedStatement){
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(null != connection){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}