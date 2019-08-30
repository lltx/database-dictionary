package com.trh.dictionary.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 内容
 *
 * @author
 * @create 2019-08-21 11:09
 */
public class ContentEvent extends PdfPageEventHelper {
    private int page;

    public Map<String, Integer> index = new LinkedHashMap<String, Integer>();
    @Override
    public void onStartPage (PdfWriter writer, Document document) {
        page++;
    }

    @Override
    public void onChapter (PdfWriter writer, Document document, float paragraphPosition, Paragraph title) {
        index.put(title.getContent(), page);
    }

    @Override
    public void onSection (PdfWriter writer, Document document, float paragraphPosition, int depth, Paragraph title) {
        onChapter(writer, document, paragraphPosition, title);
    }
}
