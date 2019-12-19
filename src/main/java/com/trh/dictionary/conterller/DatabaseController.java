package com.trh.dictionary.conterller;

import com.trh.dictionary.bean.TableInfo;
import com.trh.dictionary.dao.ConnectionFactory;
import com.trh.dictionary.service.BuildPDF;
import com.trh.dictionary.service.db2.Db2Executor;
import com.trh.dictionary.service.mysql.BuildMysqlPdf;
import com.trh.dictionary.service.oracleservice.OracleDatabase;
import com.trh.dictionary.service.postgreSQL.BuildPgSqlPdf;
import com.trh.dictionary.service.sqlserver.BuildSqlserverPDF;
import com.trh.dictionary.service.sqlserver.WriteSqlserverMarkDown;
import com.trh.dictionary.util.SqlExecutor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Chen.Chun
 * @program: database-dictionary
 * @description: API
 * @create: 2019-09-05 11:31
 **/
@Controller
public class DatabaseController {
    static Logger logger = LoggerFactory.getLogger(DatabaseController.class);

    @RequestMapping("/login.action")
    public String login(Model model, String selector, String ip, String port, String password, String username, String database) {
        List<TableInfo> tableInfo = null;
        try {
            switch (selector) {
                case "mysql":
                    //得到生成数据
                    String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
                    Connection connection = ConnectionFactory.getConnection(url, username, password, "mySql");
                    tableInfo = BuildMysqlPdf.getBuildPdfTableData(BuildMysqlPdf.getTables(connection, database));
                    break;
                case "oracle":
                    tableInfo = OracleDatabase.getTableInfo("jdbc:oracle:thin:@//" + ip + ":" + port + "/" + database + "", username, password);
                    break;
                case "SQL server":
                    model.addAttribute("markdown", WriteSqlserverMarkDown.MakeMarkdownString(ip, database, port, username, password));

                    return "markdown";

                case "PostgreSQL":
                    model.addAttribute("markdown", BuildPgSqlPdf.getPgMarkdown(ip, database, port, username, password));
                    return "markdown";
                case "DB2":
                    tableInfo = Db2Executor.getDB2Tables(ip, Integer.valueOf(port), database.toUpperCase(), username, password);
                    break;
            }
            if (tableInfo!=null){
                if (tableInfo.size() == 0) {
                    model.addAttribute("markdown", "## 数据库无数据");
                    return "markdown";
                }
            }
            String markdown = BuildPDF.writeMarkdown(tableInfo);
            model.addAttribute("markdown", markdown);
            return "markdown";
        } catch (Exception e) {
            logger.error("error==>"+e);
            model.addAttribute("markdown", "### "+e.getMessage());
            return "markdown";
        }
    }

    @RequestMapping("/getDataBaseNameList")
    @ResponseBody
    public List<String> getDataBaseNmaeList(String selector,String ip, String port, String password, String username, String database){
        List<String> list = new ArrayList<>();
        try {
            switch (selector) {
                case "mysql":
                    return BuildMysqlPdf.getDataBaseName(ip,database,port,username,password);
                case "oracle":
                    list.add(database);
                    return list;
                case "SQL server":

                    return WriteSqlserverMarkDown.getDatabasesList(ip, database, port, username, password);

                case "PostgreSQL":
                    return BuildPgSqlPdf.getDataBaseName(ip,database,port,username,password);
                case "DB2":
                    return Db2Executor.databases(SqlExecutor.newDB2Connection(ip,Integer.valueOf(port),database,username,password));
            }

        } catch (Exception e) {
            logger.error("error==>"+e);
        }
        return list;
    }

    @RequestMapping("/getMarkdownString")
    @ResponseBody
    public String getMarkdownString(Model model, String selector, String ip, String port, String password, String username, String database) {
        List<TableInfo> tableInfo = null;
        try {
            switch (selector) {
                case "mysql":
                    //得到生成数据
                    String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
                    Connection connection = ConnectionFactory.getConnection(url, username, password, "mySql");
                    tableInfo = BuildMysqlPdf.getBuildPdfTableData(BuildMysqlPdf.getTables(connection, database));
                    break;
                case "oracle":
                    tableInfo = OracleDatabase.getTableInfo("jdbc:oracle:thin:@//" + ip + ":" + port + "/" + database + "", username, password);
                    break;
                case "SQL server":
                    return WriteSqlserverMarkDown.MakeMarkdownString(ip, database, port, username, password);
                case "PostgreSQL":
                    return  BuildPgSqlPdf.getPgMarkdown(ip, database, port, username, password);
                case "DB2":
                    tableInfo = Db2Executor.getDB2Tables(ip, Integer.valueOf(port), database.toUpperCase(), username, password);
                    break;
            }
            if (tableInfo!=null){
                if (tableInfo.size() == 0) {
                    return "## 数据库无数据";
                }
            }

            String markdown = BuildPDF.writeMarkdown(tableInfo);

            return markdown;
        } catch (Exception e) {
            logger.error("error==>"+e);
            return "### "+e.getMessage();
        }
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void testDownload(HttpServletResponse res, String selector, String ip, String port, String password, String username, String database) {
        //1.先生成pdf文件
        String filePath = System.getProperty("user.dir");
        try {
            switch (selector) {
                case "mysql":
                    //得到生成数据
                    BuildMysqlPdf.MakeMysqlPdf(ip, database, port, username, password,res);
                    break;
                case "oracle":
                    List<TableInfo> tableInfo = OracleDatabase.getTableInfo("jdbc:oracle:thin:@//" + ip + ":" + port + "/" + database + "", username, password);
                    if (tableInfo.size() == 0) {
                        return;
                    }
                    //带目录
                    BuildPDF.getDocumentBuild(tableInfo,res);
                    break;
                case "SQL server":
                    BuildSqlserverPDF.MakePdf(ip, database, port, username, password,res);
                    break;
                case "PostgreSQL":
                    BuildPgSqlPdf.buildPdf(ip, database, port, username, password,res);
                    break;
                case "DB2":
                    List<TableInfo> Db2tableInfo = Db2Executor.getDB2Tables(ip, Integer.valueOf(port), database, username, password);
                    if (Db2tableInfo.size() == 0) {
                        return;
                    }
                    //带目录
                    BuildPDF.getDocumentBuild( Db2tableInfo,res);
                    break;
            }
        } catch (Exception e) {
            logger.error("error==>" + e);
        }
    }
}