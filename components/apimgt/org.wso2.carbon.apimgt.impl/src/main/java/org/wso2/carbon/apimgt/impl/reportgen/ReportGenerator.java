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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.reportgen.model.RowEntry;
import org.wso2.carbon.apimgt.impl.reportgen.model.TableData;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class ReportGenerator {
    private static final Log log = LogFactory.getLog(ReportGenerator.class);

    private static final String MGW_META = "MGW_CODE";
    private static final String MGW_KEY = "dmMzU0R5MTI4N2ExRlNnMQ==";
    private static final String MGW_ALGO = "AES";

    private static final float[] COLUMN_WIDTH = { 50, 200, 150 };
    private static final float ROW_HEIGHT = 25;
    private static final float CELL_PADDING = 10;
    private static final float CELL_MARGIN = 40; // margin on left side;
    private static final float TABLE_WIDTH = 500;
    private static final float TABLE_TOP_Y = 700;

    // Font configuration
    private static final PDFont TEXT_FONT = PDType1Font.HELVETICA;
    private static final float FONT_SIZE = 9;

    /**
     * Generate PDF file for API microgateway request summary
     *
     * @param table object containing table headers and row data
     * @return InputStream pdf as a stream
     * @throws IOException
     */
    public InputStream generateMGRequestSummeryPDF(TableData table) throws IOException {

        String[] columnHeaders = table.getColumnHeaders();

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        page.setMediaBox(PDRectangle.A4);
        page.setRotation(0);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page, false, false);

        // add logo
        InputStream in = APIManagerComponent.class.getResourceAsStream("/report/wso2-logo.jpg");
        PDImageXObject img = JPEGFactory.createFromStream(document, in);
        contentStream.drawImage(img, 375, 755);

        // Add topic
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        writeContent(contentStream, CELL_MARGIN, 770, "API Microgateway request summary");

        // Add generated time
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE);
        writeContent(contentStream, CELL_MARGIN, 730, "Report generated on: " + new Date().toString());

        contentStream.setFont(TEXT_FONT, FONT_SIZE);

        // add table with data
        drowTableGrid(contentStream, table.getRows().size());
        writeRowsContent(contentStream, columnHeaders, table.getRows());

        // Add meta data
        // Whenever the summary report structure is updated this should be changed
        String requestCount = table.getRows().get(0).getEntries().get(2);
        document.getDocumentInformation().setCustomMetadataValue(MGW_META, getMetaCount(requestCount));

        contentStream.close();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());

    }

    private String getMetaCount(String origCount) {
        String count = origCount;
        Cipher cipher;

        try {
            cipher = Cipher.getInstance(MGW_ALGO);
            SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(MGW_KEY), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytes = cipher.doFinal(origCount.getBytes());
            count = new String(Base64.getEncoder().encode(bytes));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException e) {
            log.info("Couldn't generate the value for Summary report meta field.", e);
        }

        return count;
    }

    private void drowTableGrid(PDPageContentStream contentStream, int numberOfRows) throws IOException {
        float nextY = TABLE_TOP_Y;
        // draw horizontal lines
        for (int i = 0; i <= numberOfRows + 1; i++) {
            contentStream.drawLine(CELL_MARGIN, nextY, CELL_MARGIN + TABLE_WIDTH, nextY);
            nextY -= ROW_HEIGHT;
        }

        // draw vertical lines
        final float tableYLength = ROW_HEIGHT + (ROW_HEIGHT * numberOfRows);
        final float tableBottomY = TABLE_TOP_Y - tableYLength;
        float nextX = CELL_MARGIN;
        for (int i = 0; i < COLUMN_WIDTH.length; i++) {
            contentStream.drawLine(nextX, TABLE_TOP_Y, nextX, tableBottomY);
            nextX += COLUMN_WIDTH[i];
        }
        contentStream.drawLine(CELL_MARGIN + TABLE_WIDTH, TABLE_TOP_Y, CELL_MARGIN + TABLE_WIDTH, tableBottomY);
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

        for (int i = 0; i < COLUMN_WIDTH.length; i++) {
            writeContent(contentStream, positionX, positionY, content[i]);
            positionX += COLUMN_WIDTH[i];
        }
    }

    private void writeToRow(PDPageContentStream contentStream, float positionX, float positionY, RowEntry entry)
            throws IOException {

        for (int i = 0; i < COLUMN_WIDTH.length; i++) {
            writeContent(contentStream, positionX, positionY, (String) entry.getEntries().get(i));
            positionX += COLUMN_WIDTH[i];
        }
    }

    private void writeRowsContent(PDPageContentStream contentStream, String[] columnHeaders, List<RowEntry> rowEntries)
            throws IOException {
        float startX = CELL_MARGIN + CELL_PADDING; // space between entry and the column line
        float startY = TABLE_TOP_Y - (ROW_HEIGHT / 2)
                - ((TEXT_FONT.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * FONT_SIZE) / 4);
        // write table colum headers
        writeColumHeader(contentStream, startX, startY, columnHeaders);

        startY -= ROW_HEIGHT;
        startX = CELL_MARGIN + CELL_PADDING;
        // write content
        for (RowEntry entry : rowEntries) {

            writeToRow(contentStream, startX, startY, entry);
            startY -= ROW_HEIGHT;
            startX = CELL_MARGIN + CELL_PADDING;
        }
    }
}
