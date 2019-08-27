package com.trh.dictionary.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.trh.dictionary.bean.ColumnInfo;
import com.trh.dictionary.bean.IndexInfo;
import com.trh.dictionary.bean.TableInfo;
import com.trh.dictionary.util.ColumnBasicEnum;
import com.trh.dictionary.util.SignEnum;
import com.trh.dictionary.util.TableBasicEnum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangyu
 * @create 2019-07-25 11:24
 */
public class BuildPDF {
    static Logger logger = LoggerFactory.getLogger(BuildPDF.class);

    /**
     * 生成PDF
     * @param ip ：数据库连接的IP  例如：127.0.0.1 或者 localhost
     * @param dbName 例如: test
     * @param port 例如: 3306
     * @param userName 例如: root
     * @param passWord 例如: root
     * @param filePath 例如:  D:\ideaspace\export_dbInfo\src\main\resources\
     * @param pdfName 例如:  testPDF
     */
    public static  void  MakePdf(String ip,String dbName,String port,String userName,String passWord,String filePath,String pdfName){
        try {
            //得到生成数据
            List<TableInfo> list = getBuildPdfTableData(getTables(ip, dbName, port, userName, passWord));
            if(list.size() == 0){
                return;
            }
            FileUtils.forceMkdir(new File(filePath));
            //带目录
            build(filePath, list, pdfName);
        } catch (Exception e) {
            logger.error("生成PDF失败.......",e);
        }
    }

    public static void main(String[] args) {
        try {
/*            //得到生成数据
//            List<TableInfo> list = getBuildPdfTableData(getTables("localhost", "trh_bill", "3306", "root", "root"));
//            System.out.println("--------" + list.size());
//            String name = "D:\\ideaspace\\export_dbInfo\\src\\main\\resources\\txt\\" + System.currentTimeMillis();
////            String FILE_DIR = "D:\\ideaspace\\database-dictionary\\src\\main\\resources\\pdf\\";
//            String FILE_DIR = BuildPDF.class.getResource("/").getPath().replaceAll("target/classes/", "");
//            System.out.println("FILE_DIR===" + FILE_DIR);
//            FILE_DIR += "src/main/resources/pdf/";
//            //生成markdown语法
////            writeMarkdown(list,name);
//            FileUtils.forceMkdir(new File(FILE_DIR));
//
//            //生成pdf
//            createPdf(FILE_DIR, list, "trh_bill2");
//
//            //带目录
//            build(FILE_DIR, list, "trh_bill3");
////            demo();*/
//            String FILE_DIR = BuildPDF.class.getResource("/").getPath().replaceAll("target/classes/", "");
//            System.out.println("FILE_DIR===" + FILE_DIR);
            String  FILE_DIR = "F:/pdf/";
            MakePdf("localhost", "cd_core", "3306", "root", "root",FILE_DIR,"cd_core");
        } catch (Exception e) {
            e.getStackTrace();

        }
    }


    /**
     * 获取所有表信息
     *
     * @param ip
     * @param dbname
     * @param port
     * @param userName
     * @param passWord
     * @return
     */
    public static List<Map<String, Object>> getTables(String ip, String dbname, String port, String userName, String passWord) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> tables = new ArrayList<Map<String, Object>>();
        String url = "jdbc:mysql://" + ip + ":" + port + "/" + dbname + "?useSSL=false&serverTimezone=UTC";
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            connection = DriverManager.getConnection(url, userName, passWord);
            if (connection.isClosed()) {
                System.out.println("-------------------the connect is closed--------------");
                return null;
            }
            //获取表名
            statement = connection.createStatement();
            String sql = "SHOW  TABLES FROM " + dbname;
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Map<String, Object> resmap = new HashMap<String, Object>(2);
                TableInfo tableInfo = new TableInfo();
                //表名
                String tableName = resultSet.getString(1);
                //获取表信息
                String sqlTableInfo = "SHOW CREATE TABLE " + tableName;
                String createTable = getTableInfo(connection, sqlTableInfo);
                tableInfo.setTableName(tableName);
                resmap.put("createTable", createTable);
                String sql1 = "show full columns from " + tableName;
                List<ColumnInfo> columnInfos = getpub(connection, sql1);
                tableInfo.setColumnList(columnInfos);
                resmap.put("tableInfo", tableInfo);
                tables.add(resmap);
            }
            return tables;
        } catch (Exception e) {
            e.printStackTrace();
            return tables;
        } finally {
            try {
                resultSet.close();
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取单个表全部信息
     *
     * @param connection
     * @param sqlTableInfo
     * @return
     * @throws SQLException
     */
    public static String getTableInfo(Connection connection, String sqlTableInfo) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlTableInfo);
        String table = "";
        while (resultSet.next()) {
            table = resultSet.getString("Create Table");
        }
        return table;
    }

    static public List<ColumnInfo> getpub(Connection connection, String sql1) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql1);
        List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>();
        int order = 1;
        while (resultSet.next()) {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setName(resultSet.getString(ColumnBasicEnum.Field.getDesc()) + " " + resultSet.getString(ColumnBasicEnum.Extra.getDesc()));
            columnInfo.setType(resultSet.getString(ColumnBasicEnum.Type.getDesc()));
            columnInfo.setDescription(resultSet.getString(ColumnBasicEnum.Comment.getDesc()));
            columnInfo.setIsNull(resultSet.getString(ColumnBasicEnum.Null.getDesc()));
            columnInfo.setOrder(order++);
            columnInfo.setDefaultValue(resultSet.getString(ColumnBasicEnum.Default.getDesc()));
            columnInfos.add(columnInfo);
            if (null == columnInfo.getDefaultValue()) {
                columnInfo.setDescription("");
            }
            if (null == columnInfo.getDefaultValue()) {
                columnInfo.setDefaultValue("");
            }
        }
        return columnInfos;
    }

    /**
     * 得到构建pdf文件的数据
     *
     * @param tables
     * @return
     */
    public static List<TableInfo> getBuildPdfTableData(List<Map<String, Object>> tables) {
        //循环处理表
        List<TableInfo> resultList = new ArrayList<TableInfo>();
        for (Map<String, Object> table : tables) {
            TableInfo tableInfo = (TableInfo) table.get("tableInfo");
            String tableName = tableInfo.getTableName();
            String createTable = (String) table.get("createTable");
            tableInfo.setTableName(tableName);
            //处理表信息字符
            tableInfo = takeTableInfo(tableInfo, createTable);
            resultList.add(tableInfo);
        }
        return resultList;
    }

    /**
     * 得到表的基本数据
     *
     * @param tableInfo
     * @param tableInfos
     * @return
     * @throws Exception
     */
    public static TableInfo takeTableInfo(TableInfo tableInfo, String tableInfos) {
        //去掉回车
        tableInfos = dest(tableInfos, SignEnum.back_quote.getDesc());
        tableInfos = dest(tableInfos, SignEnum.single_quotation_marks.getDesc());
        String[] test = tableInfos.split("\n");
        //处理字符串
        String str = test[test.length - 1];
        str = dest(str, SignEnum.right_brackets.getDesc()).trim();
        String[] table = str.split(" ");
        List<IndexInfo> indexInfoList = new ArrayList<IndexInfo>();
        int indexInfoSize = test.length;
        for (int i = 0; i < indexInfoSize - 1; i++) {
            String temp = test[i];
            //主键索引
            if (temp.contains(TableBasicEnum.PRIMARY_KEY.getDesc())) {
                temp = dest(temp, SignEnum.left_brackets.getDesc());
                temp = dest(temp, SignEnum.right_brackets.getDesc());
                String[] tempForIndex = temp.trim().split(" ");
                String containKey = tempForIndex[tempForIndex.length - 1];
                IndexInfo indexInfo1 = new IndexInfo(TableBasicEnum.WORD_PRIMARY.getDesc(), TableBasicEnum.WORD_PRIMARY.getDesc(), drop(containKey));
                indexInfoList.add(indexInfo1);
            }
            //唯一索引
            if (temp.contains(TableBasicEnum.UNIQUE_KEY.getDesc())) {
                String[] tempForIndex = temp.trim().split(" ");
                String containKey = tempForIndex[tempForIndex.length - 1];
                String type = tempForIndex[0] + tempForIndex[1];
                String name = tempForIndex[2];
                containKey = dest(containKey, SignEnum.left_brackets.getDesc());
                containKey = dest(containKey, SignEnum.right_brackets.getDesc());
                containKey = dest(containKey, SignEnum.single_quotation_marks.getDesc());
                IndexInfo indexInfo1 = new IndexInfo(name, type, drop(containKey));
                indexInfoList.add(indexInfo1);
            }
            //普通索引
            if (temp.contains(TableBasicEnum.KEY.getDesc())) {
                String[] tempForIndex = temp.trim().split(" ");
                if (!tempForIndex[0].equalsIgnoreCase(TableBasicEnum.WORD_key.getDesc())) {
                    continue;
                }
                String containKey = tempForIndex[tempForIndex.length - 1];
                String type = tempForIndex[0];
                String name = tempForIndex[1];
                containKey = dest(containKey, SignEnum.left_brackets.getDesc());
                containKey = dest(containKey, SignEnum.right_brackets.getDesc());
                containKey = dest(containKey, SignEnum.single_quotation_marks.getDesc());
                IndexInfo indexInfo1 = new IndexInfo(name, type, drop(containKey));
                indexInfoList.add(indexInfo1);
            }
            //全文索引
            if (temp.contains("FULLTEXT KEY")) {
                String[] tempForIndex = temp.trim().split(" ");
                String containKey = tempForIndex[tempForIndex.length - 1];
                String type = tempForIndex[0];
                String name = tempForIndex[2];
                containKey = dest(containKey, SignEnum.left_brackets.getDesc());
                containKey = dest(containKey, SignEnum.right_brackets.getDesc());
                containKey = dest(containKey, SignEnum.single_quotation_marks.getDesc());
                IndexInfo indexInfo1 = new IndexInfo(name, type, drop(containKey));
                indexInfoList.add(indexInfo1);
            }
        }
        tableInfo.setIndexInfoList(indexInfoList);
        //得到表字符集和ENGINE、表注释
        for (int i = 0; i < table.length; i++) {
            String oneTemp = table[i];
            //引擎
            if (oneTemp.contains(TableBasicEnum.ENGINE.getDesc())) {
                tableInfo.setStorageEngine(dropSign(table[i]));
                continue;
            } else {
                if (tableInfo.getStorageEngine() == null) {
                    tableInfo.setStorageEngine("");
                }
            }
            //字符集
            if (oneTemp.contains(TableBasicEnum.CHARSET.getDesc())) {
                tableInfo.setOrderType(dropSign(table[i]));
                continue;
            } else {
                if (tableInfo.getOrderType() == null) {
                    tableInfo.setOrderType("");
                }
            }

            //描述
            if (oneTemp.contains(TableBasicEnum.COMMENT.getDesc())) {
                tableInfo.setDescription(dropSign(table[i]));
                continue;
            } else {
                if (tableInfo.getDescription() == null) {
                    tableInfo.setDescription("");
                }
            }
        }

        return tableInfo;
    }


    /**
     * 去掉字符串中的符号
     *
     * @param str
     * @return
     */
    public static String dest(String str, String reg) {
        String temp = "";
        if (str != null) {
            Pattern pattern = Pattern.compile(reg);
            Matcher m = pattern.matcher(str);
            temp = m.replaceAll("");
        }
        return temp;
    }

    /**
     * 得到一个包含一个等号的字符串，获取等号后的值
     *
     * @param temp
     * @return
     */
    public static String dropSign(String temp) {
        temp = temp.trim();
        int index = temp.indexOf(SignEnum.equal_sign.getDesc());
        String res = temp.substring(index + 1);
        if (res.contains(SignEnum.single_quotation_marks.getDesc())) {
            try {
                res = dest(res, SignEnum.single_quotation_marks.getDesc());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("获取等号后的值异常");
                return temp;
            }
        }
        return res;
    }

    /**
     * 去掉索引逗号
     *
     * @param temp
     * @return
     * @throws Exception
     */
    public static String drop(String temp) {
        try {
            temp = temp.trim();
            char[] tempArry = temp.toCharArray();
            char res = tempArry[temp.length() - 1];
            if (temp.contains(SignEnum.comma.getDesc()) && res == ',') {
                return temp.substring(0, temp.length() - 1);
            } else {
                return temp;
            }
        } catch (Exception e) {
            e.getStackTrace();
            return temp;
        }
    }


    public static void createPdf(String FILE_DIR, List<TableInfo> tableInfos, String pdfName) {
        try {
            //中文字体
//            BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
//            BaseFont bfChinese = BaseFont.createFont(AsianFontMapper.ChineseTraditionalFont_MHei, AsianFontMapper.ChineseTraditionalEncoding_H, BaseFont.EMBEDDED);
            BaseFont bfChinese = BaseFont.createFont(FILE_DIR.replaceAll("pdf", "font") + "verdana.ttf", BaseFont.MACROMAN, BaseFont.NOT_EMBEDDED);
            Font font = new Font(bfChinese, 12, Font.BOLDITALIC);
            // 设置类型，加粗
            font.setStyle(Font.NORMAL);

            //页面大小
            Rectangle rect = new Rectangle(PageSize.A4).rotate();
            //页面背景色
//            rect.setBackgroundColor(BaseColor.WHITE);
            rect.setBackgroundColor(new BaseColor(0xFF, 0xFF, 0xDE));
            //设置边框颜色
            rect.setBorderColor(new BaseColor(0xFF, 0xFF, 0xDE));

            Document doc = new Document(rect);
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(FILE_DIR + pdfName + ".pdf"));
            //PDF版本(默认1.4)
            writer.setPdfVersion(PdfWriter.PDF_VERSION_1_2);
            //设置行间距
            writer.setInitialLeading(30);

            //页边空白
            doc.setMargins(10, 20, 30, 40);
            //设置页码
            setFooter(writer, bfChinese);
            // 标题
            doc.addTitle("trh-bill");
            doc.open();
            //目录
            doc.newPage();
            Paragraph indexInfo = new Paragraph("目录", font);
            indexInfo.setIndentationLeft(280);
            doc.add(indexInfo);
            int index = 1;
            // 取到锚点
            for (TableInfo tableInfo : tableInfos) {
                Paragraph p1 = new Paragraph();
                p1.setAlignment(Element.ALIGN_JUSTIFIED_ALL);
                String name = tableInfo.getTableName();
                // 生成
                String t = name + getPoint(name, 120) + index;
                System.out.println(t);
                Anchor toUS = new Anchor(t);
                // 取到锚点
                String point = "#" + name;
                toUS.setReference(point);
                p1.add(toUS);
                doc.add(p1);
                index++;
            }

            //居中
            doc.newPage();
            //表格
            //设置表格模板
            String[] tableHeader = {"序列", "列名", "类型", "可空", "默认值", "注释"};
            String[] indexHeader = {"序列", "索引名", "类型", "包含字段"};
            String baseInfo;
            int order = 1;
            for (TableInfo tableInfo : tableInfos) {
                tableInfo = setIsIndex(tableInfo);
                // 生成锚点
                Paragraph paragraph1 = new Paragraph("\n\n");
                Anchor dest = new Anchor(order + "." + tableInfo.getTableName(), font);
                // 设置锚点的名字
                dest.setName(tableInfo.getTableName());
                // 连接
                paragraph1.add(dest);
                doc.add(paragraph1);
                // 生成
                Phrase base = new Phrase("\n基本信息:", getChineseFontAsStyle(BaseColor.BLACK, 16));
                Phrase engine = new Phrase("  " + tableInfo.getStorageEngine(), font);
                Phrase type = new Phrase(" " + tableInfo.getOrderType(), font);
                Phrase description = new Phrase(" " + tableInfo.getDescription() + "\n\n", getChineseFontAsStyle(BaseColor.BLACK, 16));
                Paragraph paragraph = new Paragraph();
                paragraph.add(base);
                paragraph.add(engine);
                paragraph.add(type);
                paragraph.add(description);
                paragraph.setLeading(20f);
                doc.add(paragraph);
                //设置表格
                PdfPTable table = setTableHeader(tableHeader, getChineseFontAsStyle(BaseColor.BLACK, 16));
                //设置列信息
                setTableColumn(table, tableInfo, font);
                doc.add(table);
                //设置索引表
                Paragraph blankTwo = new Paragraph("\n\n");

                doc.add(blankTwo);
                PdfPTable indexTable = setTableHeader(indexHeader, getChineseFontAsStyle(BaseColor.BLACK, 16));
                table.setWidthPercentage(100);
                indexTable = setIndexTableColumn(indexTable, tableInfo.getIndexInfoList(), font);
                doc.add(indexTable);
                //序号
                order++;
            }
            doc.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * 设置表格头部
     *
     * @param header
     * @param font
     * @return
     */
    public static PdfPTable setTableHeader(String[] header, Font font) {
        int columnSize = header.length;
        PdfPTable table = new PdfPTable(columnSize);
        table.setWidthPercentage(100);
        for (int i = 0; i < columnSize; i++) {
            PdfPCell cell1 = new PdfPCell(new Paragraph(header[i], font));
            cell1.setVerticalAlignment(Element.ALIGN_CENTER);
//            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell1);
        }
        return table;
    }

    /**
     * 设置基本表格的列
     *
     * @param table
     * @param tableInfo
     * @param font
     * @return
     */
    public static PdfPTable setTableColumn(PdfPTable table, TableInfo tableInfo, Font font) {
        List<ColumnInfo> Columns = tableInfo.getColumnList();
        for (ColumnInfo column : Columns) {
            table = reflectBuildCell(column, table, font, tableInfo);
        }
        return table;
    }

    /**
     * 设置基本表格的列
     *
     * @param table
     * @param Columns
     * @return
     */
    public static PdfPTable setIndexTableColumn(PdfPTable table, List<IndexInfo> Columns, Font font) {
        int order = 1;
        for (IndexInfo column : Columns) {
            column.setOrder(order);
            table = reflectBuildCell(column, table, font, null);
            order++;
        }
        return table;
    }

    /**
     * 写markdown文件
     */
    private static void writeMarkdown(List<TableInfo> list, String filePath) {
        StringBuffer markdown = new StringBuffer();
        String res1 = "|:------:|:------:|:------:|:------:|:------:|:------:|" + "\n";
        int i = 1;
        for (TableInfo info : list) {
            StringBuffer oneTble = new StringBuffer();
            oneTble.append("##" + i + "." + info.getTableName() + "\n" + "基本信息:" + info.getDescription() + " " + info.getStorageEngine() + " " + info.getOrderType() + "\n\n" + "|序列|列名|类型|可空|默认值|注释|" + "\n");
            oneTble.append(res1);
            List<ColumnInfo> columnInfos = info.getColumnList();
            //拼接列
            for (ColumnInfo Column : columnInfos) {
                oneTble.append("|").append(Column.getOrder()).append("|").
                        append(Column.getName()).append("|").
                        append(Column.getType()).append("|").
                        append(Column.getIsNull()).append("|").
                        append(Column.getDefaultValue()).append("|").
                        append(Column.getDescription()).append("|").
                        append("\n");
            }
            //拼接索引
            oneTble.append("\n");
            oneTble.append("|序列|索引名|类型|包含字段|" + "\n");
            oneTble.append("|:------:|:------:|:------:|:------:|" + "\n");
            List<IndexInfo> indexInfolist = info.getIndexInfoList();
            int j = 1;
            for (IndexInfo indexInfo : indexInfolist) {
                oneTble.append("|").append(j).append("|").
                        append(indexInfo.getName()).append("|").
                        append(indexInfo.getType()).append("|").
                        append(indexInfo.getContainKey()).append("|").
                        append("\n");
                j++;
            }
            i++;
            oneTble.append("\n");
            markdown.append(oneTble);
            createDir(filePath + "\\" + info.getTableName() + ".txt", oneTble.toString());
        }
        //目录
        markdown.insert(0, "[TOC]\n");
        System.out.println("表信息\n" + markdown.toString());
        createDir(filePath + "\\allTable.txt", markdown.toString());
    }

    /**
     * 创建文件夹
     *
     * @param fileName
     * @param content
     */
    public static void createDir(String fileName, String content) {
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
                //写文件
                FileWriter writer = new FileWriter(fileName);
                writer.write(content);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //设置页码
    public static void setFooter(PdfWriter writer, BaseFont bf) {
        pdfPageEvent headerFooter = new pdfPageEvent(bf, 13, PageSize.A0);
        writer.setPageEvent(headerFooter);
    }

    public static String getPoint(String name, int size) {
        int length = name.length();
        int res = size - length;
        System.out.println();
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < res; i++) {
            str.append(SignEnum.point.getDesc());
        }
        return str.toString();
    }

    /**
     * 得到默认字体
     *
     * @param color
     * @param size
     * @return
     */
    public static Font getFontAsStyle(BaseColor color, float size) {
        Font font = new Font();
        font.setColor(color);
        font.setSize(size);
        return font;
    }

    /**
     * 得到汉字字体
     *
     * @param color
     * @param size
     * @return
     */
    public static Font getChineseFontAsStyle(BaseColor color, float size) {
        try {
            //中文字体
            BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font font = new Font(bfChinese, size, Font.NORMAL);
            font.setColor(color);
            return font;
        } catch (Exception e) {
            logger.error("生成中文字体异常", e);
            return new Font();
        }
    }


    /**
     * 通过反射填写表格内容
     *
     * @param obj
     * @author wangyu
     * @date 2019-8-6
     */
    public static PdfPTable reflectBuildCell(Object obj, PdfPTable table, Font font, TableInfo tableInfo) {
        if (obj == null) {
            logger.error("填写表格内容对象为空");
            return table;
        }
        Font cnFont = getChineseFontAsStyle(BaseColor.BLACK, 12);
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int j = 0; j < fields.length; j++) {
            PdfPCell cell = new PdfPCell();
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            Paragraph paragraph = new Paragraph();
            //将设置私有构造器设为可取值
            fields[j].setAccessible(true);
            String name = fields[j].getName();

            if ("isIndex".equals(name)) {
                try {
                    if ((Integer) fields[j].get(obj) == 1) {
                        font = getFontAsStyle(BaseColor.RED, 12);
                        cnFont = getChineseFontAsStyle(BaseColor.RED, 12);
                    } else {
                        font = getFontAsStyle(BaseColor.BLACK, 12);
                        cnFont = getChineseFontAsStyle(BaseColor.BLACK, 12);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                continue;
            }

            if (null != tableInfo) {

            }

            // 得到类型和名字取值
            if (fields[j].getType().getName().equals(java.lang.String.class.getName())) {
                // String type
                try {
                    int k = 1;
                    if ("description".equalsIgnoreCase(name)) {
                        //如果是注释
                        paragraph = new Paragraph(fields[j].get(obj) + "", cnFont);
                    } else {
                        paragraph = new Paragraph(fields[j].get(obj) + "", font);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (fields[j].getType().getName().equals(java.lang.Integer.class.getName())
                    || fields[j].getType().getName().equals("int")) {
                // Integer type
                try {
                    if ("description".equalsIgnoreCase(name)) {
                        //如果是注释
                        paragraph = new Paragraph(fields[j].get(obj) + "", cnFont);
                    } else {
                        paragraph = new Paragraph(fields[j].get(obj) + "", font);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch b lock
                    e.printStackTrace();
                }
            }
            //添加到表格
            cell.addElement(paragraph);
            table.addCell(cell);
        }
        return table;
    }


    public static TableInfo setIsIndex(TableInfo tableInfo) {
        List<ColumnInfo> columnInfos = tableInfo.getColumnList();
        List<IndexInfo> indexInfos = tableInfo.getIndexInfoList();
        for (ColumnInfo columnInfo : columnInfos) {
            for (IndexInfo indexInfo : indexInfos) {
                String temp = indexInfo.getContainKey();
                if (temp.contains(SignEnum.comma.getDesc())) {
                    String[] keys = temp.split(SignEnum.comma.getDesc());
                    for (int i = 0; i < keys.length; i++) {
                        String key = keys[i];
                        if (key.trim().equals(columnInfo.getName().trim())) {
                            columnInfo.setIsIndex(1);
                        }
                        if (key.trim().contains(columnInfo.getName()) && columnInfo.getOrder() == 1) {
                            columnInfo.setIsIndex(1);
                        }
                    }
                } else {
                    if (temp.trim().equals(columnInfo.getName().trim())) {
                        columnInfo.setIsIndex(1);
                    }
                    String[] res = columnInfo.getName().split(" ");
                    if (res[0].equals(temp.trim())) {
                        columnInfo.setIsIndex(1);
                    }
                }
            }
        }
        return tableInfo;
    }


    public static void build(String FILE_DIR, List<TableInfo> tableInfos, String pdfName) throws Exception {
        String  fontDir  = BuildPDF.class.getResource("/").getPath().replaceAll("target/classes/", "");
        fontDir += "src/main/resources/";
        BaseFont bfChinese = BaseFont.createFont(fontDir+"font"+File.separator + "verdana.ttf", BaseFont.MACROMAN, BaseFont.NOT_EMBEDDED);
        Font font = new Font(bfChinese, 12, Font.BOLDITALIC);
        // 设置类型，加粗
        font.setStyle(Font.NORMAL);
        Font cnFont = getChineseFontAsStyle(BaseColor.BLACK, 16);
        //页面大小
        Rectangle rect = new Rectangle(PageSize.A4).rotate();
        //页面背景色
        rect.setBackgroundColor(new BaseColor(0xFF, 0xFF, 0xDE));
        //设置边框颜色
        rect.setBorderColor(new BaseColor(0xFF, 0xFF, 0xDE));
        Document doc = new Document(rect);
        PdfWriter contentWriter = PdfWriter.getInstance(doc, new ByteArrayOutputStream());
        //设置事件
        ContentEvent event = new ContentEvent();
        contentWriter.setPageEvent(event);
        //存目录监听 开始
        doc.open();
        int order=1;
        List<Chapter> chapterList = new ArrayList<Chapter>();
        //根据chapter章节分页
        //表格
        //设置表格模板
        String[] tableHeader = {"序列", "列名", "类型", "可空", "默认值", "注释"};
        String[] indexHeader = {"序列", "索引名", "类型", "包含字段"};
        for (TableInfo tableInfo : tableInfos) {
            Chapter chapter = new Chapter(new Paragraph(tableInfo.getTableName()), order);
            //设置跳转地址
            Phrase point = new Paragraph("基本信息:",cnFont);
            Anchor tome = new Anchor(point);
            tome.setName(tableInfo.getTableName());
            Phrase engine = new Phrase("  " + tableInfo.getStorageEngine(), font);
            Phrase type = new Phrase(" " + tableInfo.getOrderType(), font);
            Phrase description = new Phrase(" " + tableInfo.getDescription() + "\n\n", getChineseFontAsStyle(BaseColor.BLACK, 16));
            //组装基本数据
            Paragraph contentInfo = new Paragraph();
            contentInfo.add(tome);
            contentInfo.add(engine);
            contentInfo.add(type);
            contentInfo.add(description);
            chapter.add(contentInfo);
            chapter.add(new Paragraph(""));
            //组装表格
            Paragraph tableParagraph = new Paragraph();
            //设置表格
            PdfPTable table = setTableHeader(tableHeader, getChineseFontAsStyle(BaseColor.BLACK, 16));
            //设置列信息
            setTableColumn(table, tableInfo, font);
            tableParagraph.add(table);
            chapter.add(tableParagraph);
            //设置索引表
            Paragraph blankTwo = new Paragraph("\n\n");
            chapter.add(blankTwo);
            PdfPTable indexTable = setTableHeader(indexHeader, getChineseFontAsStyle(BaseColor.BLACK, 16));
            table.setWidthPercentage(100);
            indexTable = setIndexTableColumn(indexTable, tableInfo.getIndexInfoList(), font);
            Paragraph indexTableParagraph = new Paragraph();
            indexTableParagraph.add(indexTable);
            chapter.add(indexTableParagraph);

            //加入文档中
            doc.add(chapter);
            //保存章节内容
            chapterList.add(chapter);
            order++;
        }
        doc.close();
        //存目录监听 结束


        Document document = new Document(rect);
        FileOutputStream os = new FileOutputStream(FILE_DIR + pdfName + ".pdf");
        PdfWriter writer = PdfWriter.getInstance(document, os);
        IndexEvent indexEvent = new IndexEvent();
        writer.setPageEvent(indexEvent);
        document.open();
        //添加章节目录
        Chapter indexChapter = new Chapter(new Paragraph("", getFontAsStyle(BaseColor.BLACK, 18)), 0);
        indexChapter.setNumberDepth(-1);
        // 设置数字深度
        int i = 1;
        for (Map.Entry<String, Integer> index : event.index.entrySet()) {
            String key = index.getKey();
            String[] keyValue = key.split(" ");
            //设置跳转显示名称
            int pageNo = index.getValue();
            Chunk pointChunk = new Chunk(new DottedLineSeparator());
            Chunk pageNoChunk = new Chunk(pageNo+"");
            Paragraph jumpParagraph = new Paragraph();
            jumpParagraph.add(key);
            jumpParagraph.add(pointChunk);
            jumpParagraph.add(pageNoChunk);
            Anchor anchor = new Anchor(jumpParagraph);
            String jump = keyValue[keyValue.length-1].trim();
            //设置跳转链接
            anchor.setReference("#" + jump);
            indexChapter.add(anchor);
            indexChapter.add(new Paragraph());
            i++;
        }
        document.add(indexChapter);
        document.newPage();
        //添加内容
        for (Chapter c : chapterList) {
            indexEvent.setBody(true);
            document.add(c);
        }

        document.close();
        os.close();
    }
}
