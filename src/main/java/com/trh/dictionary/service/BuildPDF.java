package com.trh.dictionary.service;

import com.github.houbb.markdown.toc.util.StringUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.trh.dictionary.bean.ColumnInfo;
import com.trh.dictionary.bean.IndexInfo;
import com.trh.dictionary.bean.TableInfo;
import com.trh.dictionary.dao.ConnectionFactory;
import com.trh.dictionary.service.mysql.BuildMysqlPdf;
import com.trh.dictionary.util.ColumnBasicEnum;
import com.trh.dictionary.util.SignEnum;
import com.trh.dictionary.util.TableBasicEnum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
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
     *
     * @param ip       ：数据库连接的IP  例如：127.0.0.1 或者 localhost
     * @param dbName   例如: test
     * @param port     例如: 3306
     * @param userName 例如: root
     * @param passWord 例如: root
     * @param filePath 例如:  D:\ideaspace\export_dbInfo\src\main\resources\
     */
    public static void MakeMarkDown(String ip, String dbName, String port, String userName, String passWord, String filePath) {
        try {
            //得到生成数据
            String url = "jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC";
            Connection connection = ConnectionFactory.getConnection(url, userName, passWord, "mySql");
            List<TableInfo> list = BuildMysqlPdf.getBuildPdfTableData(BuildMysqlPdf.getTables(connection, dbName));
            if (list.size() == 0) {
                return;
            }
            writeMarkdown(list, filePath);
        } catch (Exception e) {
            logger.error("生成markdown失败.......", e);
        }
    }


    /**
     * 去掉字符串中的符号
     *
     * @param param 需要处理的变量
     * @return String
     */
    public static String dest(String param, String reg) {
        String temp = "";
        if (param != null) {
            Pattern pattern = Pattern.compile(reg);
            Matcher m = pattern.matcher(param);
            temp = m.replaceAll("");
        }
        return temp;
    }

    /**
     * 得到一个包含一个等号的字符串，获取等号后的值
     *
     * @param param 需处理的变量
     * @return String
     */
    public static String dropSign(String param) {
        param = param.trim();
        int index = param.indexOf(SignEnum.equal_sign.getDesc());
        String res = param.substring(index + 1);
        if (res.contains(SignEnum.single_quotation_marks.getDesc())) {
            try {
                res = dest(res, SignEnum.single_quotation_marks.getDesc());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("获取等号后的值异常");
                return param;
            }
        }
        return res;
    }

    /**
     * 去掉索引逗号
     *
     * @param param 需处理的变量
     * @return String
     */
    public static String drop(String param) {
        try {
            param = param.trim();
            char[] tempArry = param.toCharArray();
            char res = tempArry[param.length() - 1];
            if (param.contains(SignEnum.comma.getDesc()) && res == ',') {
                return param.substring(0, param.length() - 1);
            } else {
                return param;
            }
        } catch (Exception e) {
            e.getStackTrace();
            return param;
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
                logger.info(t);
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
     * 转成markdown语法
     */
    public static String writeMarkdown(List<TableInfo> list) {
        StringBuffer markdown = new StringBuffer();
        String res1 = "|:------:|:------:|:------:|:------:|:------:|:------:|" + "\n";
        int i = 1;
        for (TableInfo info : list) {
            StringBuffer oneTble = new StringBuffer();
            oneTble.append("##" + i + "." + info.getTableName()+ " "+info.getDescription() + "\n" + "基本信息:" + info.getDescription() + " " + info.getStorageEngine() + " " + info.getOrderType() + "\n\n" + "|序列|列名|类型|可空|默认值|注释|" + "\n");
            oneTble.append(res1);
            List<ColumnInfo> columnInfos = info.getColumnList();
            //拼接列
            for (int k = 0; k < columnInfos.size(); k++) {
                ColumnInfo Column = columnInfos.get(k);
                oneTble.append("|").append(Column.getOrder()).append("|").
                        append(Column.getName()).append("|").
                        append(Column.getType()).append("|").
                        append(Column.getIsNull()).append("|").
                        append(Column.getDefaultValue()).append("|");
                /*if ((k + 1) == columnInfos.size()) {
                    oneTble.append(Column.getDescription()).append("||").
                            append("\n");
                } else {
                    oneTble.append(Column.getDescription()).append("|").
                            append("\n");

                }*/
                if(null == Column.getDescription()){
                    oneTble.append(" ").append("|").append("\n");
                }else{
                    String str = Column.getDescription();
                    str = str.replaceAll("\n","");
                    if(str.length() == 0){
                        oneTble.append("...").append("|").append("\n");
                    }else{
                        oneTble.append(str).append("|").append("\n");
                    }

                }

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
            //createDir(filePath + "\\" + info.getTableName() + ".txt", oneTble.toString());
        }
        //目录
        markdown.insert(0, "[TOC]\n");
        return markdown.toString();
    }


    /**
     * 写markdown文件
     */
    public static String writeMarkdown(List<TableInfo> list, String filePath) {
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
        return markdown.toString();
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
                    if (isChineseContent(fields[j].get(obj) + "")) {
                        font = cnFont;
                    }
                    paragraph = new Paragraph(fields[j].get(obj) + "", font);

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
                    paragraph = new Paragraph(fields[j].get(obj) + "", font);

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
//                if (temp.contains(SignEnum.comma.getDesc())) {
//                    String[] keys = temp.split(SignEnum.comma.getDesc());
//                    for (int i = 0; i < keys.length; i++) {
//                        String key = keys[i];
//                        if (key.trim().equals(columnInfo.getName().trim())) {
//                            columnInfo.setIsIndex(1);
//                        }
//                        if (key.trim().contains(columnInfo.getName()) && columnInfo.getOrder() == 1) {
//                            columnInfo.setIsIndex(1);
//                        }
//                    }
//                } else {
//                    if (temp.trim().equals(columnInfo.getName().trim())) {
//                        columnInfo.setIsIndex(1);
//                    }
                String[] res = columnInfo.getName().split(" ");
                if (res[0].equals(temp.trim()) && TableBasicEnum.WORD_PRIMARY.getDesc().equals(indexInfo.getType().trim())) {
                    columnInfo.setIsIndex(1);
                }
//                }
            }
        }
        return tableInfo;
    }


    public static void build(String FILE_DIR, List<TableInfo> tableInfos, String pdfName) throws Exception {
        BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
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
        int order = 1;
        List<Chapter> chapterList = new ArrayList<Chapter>();
        //根据chapter章节分页
        //表格
        //设置表格模板
        String[] tableHeader = {"序列", "列名", "类型", "可空", "默认值", "注释"};
        String[] indexHeader = {"序列", "索引名", "类型", "包含字段"};
        for (TableInfo tableInfo : tableInfos) {
            tableInfo = setIsIndex(tableInfo);
            Chapter chapter = new Chapter(new Paragraph(tableInfo.getTableName()), order);
            //设置跳转地址
            Phrase point = new Paragraph("基本信息:", cnFont);
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
            indexTable = setIndexTableColumn(indexTable, tableInfo.getIndexInfoList(), getFontAsStyle(BaseColor.RED, 10));
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
            Chunk pageNoChunk = new Chunk(pageNo + "");
            String tempDescription = key;
            if (!StringUtil.isEmpty(tableInfos.get(i - 1).getDescription())) {
                tempDescription += "(" + tableInfos.get(i - 1).getDescription() + ")";
            }
            Paragraph jumpParagraph = new Paragraph(tempDescription, getChineseFontAsStyle(BaseColor.BLACK, 12));
            jumpParagraph.add(pointChunk);
            jumpParagraph.add(pageNoChunk);
            Anchor anchor = new Anchor(jumpParagraph);
            String jump = keyValue[keyValue.length - 1].trim();
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


    public static boolean isChineseContent(String content) {
        String regex = "[\u4e00-\u9fa5]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return true;
        }
        return false;
    }


    public static void getDocumentBuild( List<TableInfo> tableInfos, HttpServletResponse response) throws Exception {
        BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
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
        int order = 1;
        List<Chapter> chapterList = new ArrayList<Chapter>();
        //根据chapter章节分页
        //表格
        //设置表格模板
        String[] tableHeader = {"序列", "列名", "类型", "可空", "默认值", "注释"};
        String[] indexHeader = {"序列", "索引名", "类型", "包含字段"};
        for (TableInfo tableInfo : tableInfos) {
            tableInfo = setIsIndex(tableInfo);
            Chapter chapter = new Chapter(new Paragraph(tableInfo.getTableName()), order);
            //设置跳转地址
            Phrase point = new Paragraph("基本信息:", cnFont);
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
            indexTable = setIndexTableColumn(indexTable, tableInfo.getIndexInfoList(), getFontAsStyle(BaseColor.RED, 10));
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
//        FileOutputStream os = new FileOutputStream(FILE_DIR + pdfName + ".pdf");
        PdfWriter writer = PdfWriter.getInstance(document, new ByteArrayOutputStream());
        IndexEvent indexEvent = new IndexEvent();
        writer.setPageEvent(indexEvent);
        response.setContentType("application/pdf");
        PdfWriter.getInstance(document, response.getOutputStream());
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
            Chunk pageNoChunk = new Chunk(pageNo + "");
            String tempDescription = key;
            if (!StringUtil.isEmpty(tableInfos.get(i - 1).getDescription())) {
                tempDescription += "(" + tableInfos.get(i - 1).getDescription() + ")";
            }
            Paragraph jumpParagraph = new Paragraph(tempDescription, getChineseFontAsStyle(BaseColor.BLACK, 12));
            jumpParagraph.add(pointChunk);
            jumpParagraph.add(pageNoChunk);
            Anchor anchor = new Anchor(jumpParagraph);
            String jump = keyValue[keyValue.length - 1].trim();
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
    }
}
