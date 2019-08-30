package com.trh.dictionary;

import com.trh.dictionary.service.BuildPDF;
import com.trh.dictionary.service.sqlserver.BuildSqlserverPDF;

/**
 * @author
 * @create 2019-07-25 11:04
 */
public class Test {
    @org.junit.Test
    public  void  testMakePdf(){
        String  FILE_DIR = "F:/pdf/";
        BuildPDF.MakePdf("localhost", "cd_core", "3306", "root", "root",FILE_DIR,"cd_core");
    }

    @org.junit.Test
    public  void  testMakeSqlServerPdf(){
        String  FILE_DIR = "F:/pdf/";
        BuildSqlserverPDF.MakePdf("192.168.161.3", "zhou", "1433", "SA", "zhoufan123AAA",FILE_DIR,"zhou");
    }


}
