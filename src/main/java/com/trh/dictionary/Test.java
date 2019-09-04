package com.trh.dictionary;

import com.trh.dictionary.bean.TableInfo;
import com.trh.dictionary.service.BuildPDF;
import com.trh.dictionary.service.db2.Db2Executor;
import com.trh.dictionary.service.oracleservice.OracleDatabase;
import com.trh.dictionary.service.sqlserver.BuildSqlserverPDF;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

/**
 * @author
 * @create 2019-07-25 11:04
 */
public class Test {
    @org.junit.Test
    public  void  testMakeMySqlPdf(){
        String  FILE_DIR = "F:/pdf/";
        BuildPDF.MakePdf("localhost", "cd_core", "3306", "root", "root",FILE_DIR,"cd_core");
    }

    @org.junit.Test
    public  void  testMakeSqlServerPdf(){
        String  FILE_DIR = "F:/pdf/";
        BuildSqlserverPDF.MakePdf("192.168.161.3", "zhou", "1433", "SA", "zhoufan123AAA",FILE_DIR,"zhou");
    }

    @org.junit.Test
    public  void  testMakeOraclePdf(){
        try {
            List<TableInfo> tableInfo = OracleDatabase.getTableInfo("jdbc:oracle:thin:@//127.0.0.1:1521/orcl","root","123456");
            if (tableInfo.size() == 0) {
                return;
            }
            String filePath = "F:/pdf/";
            FileUtils.forceMkdir(new File(filePath));
            //带目录
            BuildPDF.build(filePath, tableInfo, "Oraclecd_core8");
            BuildPDF.writeMarkdown(tableInfo,filePath);
            System.out.println("生成数据字典完毕,一共生成了"+tableInfo.size()+"条数据");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @org.junit.Test
    public void testMakeDb2ServerPdf() throws Exception {
        List<TableInfo> tableInfo = Db2Executor.getDB2Tables("192.168.171.230", "TEST", "db2", "system");
        if (tableInfo.size() == 0) {
            return;
        }
        String filePath = "E:/pdf/";
        FileUtils.forceMkdir(new File(filePath));
        //带目录
        BuildPDF.build(filePath, tableInfo, "Db2");
        BuildPDF.writeMarkdown(tableInfo, "E:/pdf/");
    }

}
