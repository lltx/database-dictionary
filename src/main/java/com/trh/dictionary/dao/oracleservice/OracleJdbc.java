package com.trh.dictionary.dao.oracleservice;

import java.sql.*;

/**
 * @author: Chen.Chun
 * @program: cdtrh_group-database-dictionary-master
 * @description: oracle链接
 * @create: 2019-08-30 11:05
 **/
public class OracleJdbc {

    //数据库连接对象
    private static Connection conn = null;
    //驱动
    private static String driver = "oracle.jdbc.driver.OracleDriver";
    //连接字符串
    private static String url = "jdbc:oracle:thin:@//127.0.0.1:1521/orcl";
    //用户名
    private static String username = "root";
    //密码
    private static String password = "123456";

    // 获得连接对象
    public static synchronized Connection getConn(){
        if(conn == null){
            try {
                Class.forName(driver);
                conn = DriverManager.getConnection(url, username, password);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }
}