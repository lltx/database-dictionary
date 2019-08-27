package com.trh.dictionary;

import com.trh.dictionary.service.BuildPDF;

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


}
