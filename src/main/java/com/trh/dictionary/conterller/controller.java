package com.trh.dictionary.conterller;

import com.trh.dictionary.bean.TableInfo;
import com.trh.dictionary.dao.ConnectionFactory;
import com.trh.dictionary.service.BuildPDF;
import com.trh.dictionary.service.db2.Db2Executor;
import com.trh.dictionary.service.oracleservice.OracleDatabase;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author: Chen.Chun
 * @program: database-dictionary
 * @description: API
 * @create: 2019-09-05 11:31
 **/
@Controller
public class controller {

    @RequestMapping("/oracleMarkdown")
    public String oracleMarkdown(Model model){
        List<TableInfo> tableInfo = OracleDatabase.getTableInfo("jdbc:oracle:thin:@//127.0.0.1:1521/orcl","root","123456");
        if (tableInfo.size() == 0) {
            return "/index";
        }
        String markdown = BuildPDF.writeMarkdown(tableInfo);
        model.addAttribute("markdown",markdown);
        return "/index";
    }
    @RequestMapping("/login.action")
    public String login(Model model,String selector,String ip,String port,String password,String username,String database){

/*        	<option value ="1">mysql</option>
	<option value ="2">oracle</option>
	<option value="3">SQL server</option>
	<option value="4">PostgreSQL</option>
	<option value="5">DB2</option>*/
        List<TableInfo> tableInfo = null;

        switch (selector){
            case "1":
                //得到生成数据
                String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
                Connection connection = ConnectionFactory.getConnection(url, username, password, "mySql");
                tableInfo  = BuildPDF.getBuildPdfTableData(BuildPDF.getTables(connection, database));
                break;
            case "2":
                tableInfo = OracleDatabase.getTableInfo("jdbc:oracle:thin:@//"+ip+":"+port+"/"+database+"",username,password);
                break;
            case "3":break;
            case "4":break;
            case "5":
                try {
                    tableInfo = Db2Executor.getDB2Tables(ip, Integer.valueOf(port), database.toUpperCase(), username, password);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }

        if (tableInfo.size() == 0) {
            return "/index";
        }
        String markdown = BuildPDF.writeMarkdown(tableInfo);
        model.addAttribute("markdown",markdown);
        return "/markdown";
    }

}