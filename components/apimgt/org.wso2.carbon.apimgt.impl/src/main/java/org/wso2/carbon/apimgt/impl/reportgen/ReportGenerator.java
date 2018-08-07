/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.reportgen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.wso2.carbon.apimgt.impl.reportgen.model.RowEntry;
import org.wso2.carbon.apimgt.impl.reportgen.model.TableData;

public class ReportGenerator {
    private static float[] columWidth = { 50, 200, 150 };
    private static float rowHeight = 25;
    private static float cellPadding = 10;
    private static float tableMargin = 40; // margin on left side;
    private static float tableWidth = 500;
    private static float tableTopY = 700;

    // Font configuration
    private static final PDFont TEXT_FONT = PDType1Font.HELVETICA;
    private static final float FONT_SIZE = 9;
    
    private static List sampleData() {
        List rows = new ArrayList<RowEntry>();
        RowEntry entry = new RowEntry();
        entry.setEntry("1");
        entry.setEntry("July 2018");
        entry.setEntry("1432");
        rows.add(entry);
        
        entry = new RowEntry();
        entry.setEntry("2");
        entry.setEntry("June 2018");
        entry.setEntry("1442");
        rows.add(entry);
        
        entry = new RowEntry();
        entry.setEntry("3");
        entry.setEntry("May 2018");
        entry.setEntry("1772");
        rows.add(entry);
        return rows;
    }
    
    public static void main(String[] args) {
        ReportGenerator generator = new ReportGenerator();
        TableData table = new TableData();
        String[] columnHeaders = { "", "Date", "Number of requests" };
        table.setColumnHeaders(columnHeaders); 
        table.setRows(sampleData());
        
        InputStream inStream = null;
        
        try {
            generator.generateRequestSummeryReport(table);
        } catch (COSVisitorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static InputStream getSampleReport() {
        ReportGenerator generator = new ReportGenerator();
        TableData table = new TableData();
        String[] columnHeaders = { "", "Date", "Number of requests" };
        table.setColumnHeaders(columnHeaders); 
        table.setRows(sampleData());
        
        InputStream inStream = null;
        
        try {
            inStream = generator.generateRequestSummeryReport(table);
        } catch (COSVisitorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return inStream;
    }
    
    public InputStream generateRequestSummeryReport(TableData table) throws IOException, COSVisitorException {
        System.out.println("X --> " + PDPage.PAGE_SIZE_A4.getWidth() + "  Y --> " + PDPage.PAGE_SIZE_A4.getHeight());
       // String[][] entries = { { "1", "July 2018", "2011" }, { "2", "June 2018", "2211" },
       //         { "3", "May 2018", "4211" } };
        String[] columnHeaders = table.getColumnHeaders();

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        page.setMediaBox(PDPage.PAGE_SIZE_A4);
        page.setRotation(0);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page, false, false);

        // add logo
//        InputStream in = new FileInputStream(
//                new File("/Users/wso2/eclipse-workspace/apimdev/ReportGen/images/wso2-logo-2.jpg"));
//        PDJpeg img = new PDJpeg(document, in);
//        contentStream.drawImage(img, 375, 755);

        // Add topic
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        writeContent(contentStream, tableMargin, 770, "Microgateway request summary");

        // Add generated time
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE);
        writeContent(contentStream, tableMargin, 730, "Report generated on: " + new Date().toString());

        contentStream.setFont(TEXT_FONT, FONT_SIZE);

        // add table with data
        drowTableGrid(contentStream, table.getRows().size());
        writeRowsContent(contentStream, columnHeaders, table.getRows());

        contentStream.close();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();
        
        return new ByteArrayInputStream(out.toByteArray());

    }
    

    public static void drowTableGrid(PDPageContentStream contentStream, int numberOfRows) throws IOException {
        float nextY = tableTopY;
        // draw horizontal lines
        for (int i = 0; i <= numberOfRows + 1; i++) {
            contentStream.drawLine(tableMargin, nextY, tableMargin + tableWidth, nextY);
            nextY -= rowHeight;
        }

        // draw vertical lines
        final float tableYLength = rowHeight + (rowHeight * numberOfRows);////// Check this
        final float tableBottomY = tableTopY - tableYLength;
        float nextX = tableMargin;
        for (int i = 0; i < columWidth.length; i++) {
            contentStream.drawLine(nextX, tableTopY, nextX, tableBottomY);
            nextX += columWidth[i];
        }
        contentStream.drawLine(tableMargin + tableWidth, tableTopY, tableMargin + tableWidth, tableBottomY);
    }

    private void writeContent(PDPageContentStream contentStream, float positionX, float positionY, String text)
            throws IOException {
        contentStream.beginText();
        contentStream.moveTextPositionByAmount(positionX, positionY);
        contentStream.drawString(text != null ? text : "");
        contentStream.endText();
    }

    private void writeColumHeader(PDPageContentStream contentStream, float positionX, float positionY, String[] content)
            throws IOException {

        for (int i = 0; i < columWidth.length; i++) {
            writeContent(contentStream, positionX, positionY, content[i]);
            positionX += columWidth[i];
        }
    }
    private void writeToRow(PDPageContentStream contentStream, float positionX, float positionY, RowEntry entry)
            throws IOException {

        for (int i = 0; i < columWidth.length; i++) {
            writeContent(contentStream, positionX, positionY, (String) entry.getEntries().get(i));
            positionX += columWidth[i];
        }
    }
    private void writeRowsContent(PDPageContentStream contentStream, String[] columnHeaders, List<RowEntry> rowEntries)
            throws IOException {
        float startX = tableMargin + cellPadding; // space between entry and the column line
        float startY = tableTopY - (rowHeight / 2)
                - ((TEXT_FONT.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * FONT_SIZE) / 4);
        // write table colum headers
        writeColumHeader(contentStream, startX, startY, columnHeaders);

        startY -= rowHeight;
        startX = tableMargin + cellPadding;
        // write content
        for (RowEntry entry : rowEntries) {

            writeToRow(contentStream, startX, startY, entry);
            startY -= rowHeight;
            startX = tableMargin + cellPadding;
        }
    }
}
