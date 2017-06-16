package org.montsuqi.monsiaj.util;

import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.io.File;
import java.io.IOException;

public class PDFPaperSize {

    public static MediaSizeName getPDFPaperSize(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb); // Create PDF Print Page

            PDFPage page = pdfFile.getPage(1);
            float width = page.getWidth();
            float height = page.getHeight();

            if (width > height) {
                float swap = width;
                width = height;
                height = swap;
            }
            return MediaSize.findMedia(width / 72, height / 72, Size2DSyntax.INCH);
        } catch (IOException ex) {
            return MediaSizeName.ISO_A4;
        }
    }

    public static MediaSizeName getPDFPaperSize(PDFPage page) {
        MediaSizeName mediaSizeName;
        float width = page.getWidth();
        float height = page.getHeight();

        if (width > height) {
            float swap = width;
            width = height;
            height = swap;
        }
        mediaSizeName = MediaSize.findMedia(width / 72, height / 72, Size2DSyntax.INCH);
        if (mediaSizeName == null) {
            mediaSizeName = MediaSizeName.A;
        }
        return mediaSizeName;
    }

    public static void main(String args[]) throws Exception {
        System.out.println("size:" + getPDFPaperSize(new File(args[0])));
    }
}
