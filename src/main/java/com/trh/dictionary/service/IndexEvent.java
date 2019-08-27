package com.trh.dictionary.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/** 页码监听
 * @author wangyu
 * @create 2019-08-21 16:45
 */
public class IndexEvent extends PdfPageEventHelper {
    private int page;
    private boolean body;
    @Override
    public void onEndPage (PdfWriter writer, Document document) {
        if (body) {
            page++;
            //设置页脚页码
            float x = (document.rightMargin() + document.right() + document.leftMargin() - document.left()) / 2.0F + 20F;
            Anchor anchor = new Anchor(""+page);
            anchor.setName("user");
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(anchor),
                    x, document.bottom() - 20, 0);
        }
    }

    public boolean isBody() {
        return body;
    }

    public void setBody(boolean body) {
        this.body = body;
    }
}
