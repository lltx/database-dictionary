package com.trh.dictionary.conterller;

import com.trh.dictionary.Test;
import com.trh.dictionary.bean.TableInfo;
import com.trh.dictionary.dao.ConnectionFactory;
import com.trh.dictionary.service.BuildPDF;
import com.trh.dictionary.service.db2.Db2Executor;
import com.trh.dictionary.service.oracleservice.OracleDatabase;
import com.trh.dictionary.service.postgreSQL.BuildPgSqlPdf;
import com.trh.dictionary.service.sqlserver.WriteSqlserverMarkDown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.Connection;
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

    @RequestMapping("/oracleMarkdown")
    public String oracleMarkdown(Model model) {
        List<TableInfo> tableInfo = OracleDatabase.getTableInfo("jdbc:oracle:thin:@//127.0.0.1:1521/orcl", "root", "123456");
        if (tableInfo.size() == 0) {
            return "index";
        }
        String markdown = BuildPDF.writeMarkdown(tableInfo);
        model.addAttribute("markdown", markdown);
        return "index1";
    }

    @RequestMapping("/login.action")
    public String login(Model model, String selector, String ip, String port, String password, String username, String database) {
        List<TableInfo> tableInfo = null;
        try {
            switch (selector) {
                case "mysql":
                    //得到生成数据
                    String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
                    Connection connection = ConnectionFactory.getConnection(url, username, password, "mySql");
                    tableInfo = BuildPDF.getBuildPdfTableData(BuildPDF.getTables(connection, database));
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
}