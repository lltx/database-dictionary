package com.trh.dictionary.bean;

import com.trh.dictionary.service.BuildPDF;
import com.trh.dictionary.service.oracleservice.OracleDatabase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

/**
 * @author: Chen.Chun
 * @program: cdtrh_group-database-dictionary-master
 * @description:
 * @create: 2019-08-30 14:06
 **/
public class OracleTest {

    public static void main(String[] args) {
        try {
            List<TableInfo> tableInfo = OracleDatabase.getTableInfo();
            if (tableInfo.size() == 0) {
                return;
            }
            String filePath = "F:/pdf/";
            FileUtils.forceMkdir(new File(filePath));
            //带目录
            BuildPDF.build(filePath, tableInfo, "Oraclecd_core6");
            System.out.println(tableInfo.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}