package com.trh.dictionary.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

/**
 * @author TangXu
 * @create 2019-08-29 17:23
 * @description:
 */
public class SqlExecutor {

    static final Logger logger = LoggerFactory.getLogger(SqlExecutor.class);

    public static Connection newDB2Connection(String db) throws ClassNotFoundException, SQLException {
        String host = "127.0.0.1";
        return newDB2Connection(host, db);
    }

    public static Connection newDB2Connection(String host, String db) throws ClassNotFoundException, SQLException {
        String user = "db2";
        String password = "system";
        return newDB2Connection(host, db, user, password);
    }

    public static Connection newDB2Connection(String host, String db, String user, String password) throws ClassNotFoundException, SQLException {
        int port = 50000;
        return newDB2Connection(host, port, db, user, password);
    }

    public static Connection newDB2Connection(String host, int port, String db, String user, String password) throws ClassNotFoundException, SQLException {
        boolean reconnect = true;
        String encoding = "utf-8";
        return newDB2Connection(host, port, db, reconnect, encoding, user, password);
    }

    public static Connection newDB2Connection(String host, int port, String db, boolean reconnect, String encoding, String username, String password) throws ClassNotFoundException, SQLException, SQLException {
        String driver = "com.ibm.db2.jcc.DB2Driver";
        String format = "jdbc:db2://%s:%d/%s";
        String url = String.format(format, host, port, db);
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    public static Connection getConnections(String driver, String url, String username, String password) throws Exception {
        try {
            Properties props = new Properties();
            props.put("remarksReporting", "true");
            props.put("user", username);
            props.put("password", password);
            Class.forName(driver);
            return DriverManager.getConnection(url, props);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 释放连接
     *
     * @param connection
     * @param preparedStatement
     * @param resultSet
     * @param statement
     */
    public static void releaseResource(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet, Statement statement) {
        try {

            if (null != statement) {
                statement.close();
            }

            if (null != preparedStatement) {
                preparedStatement.close();
            }

            if (null != resultSet) {
                resultSet.close();
            }

            if (null != connection) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
}