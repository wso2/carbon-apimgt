package org.wso2.carbon.apimgt.mongodb.persistence.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.pdfbox.cos.COSDocument;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.exceptions.PersistenceException;
import org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MongoDBUtil {
    private static final Log log = LogFactory.getLog(PersistenceUtil.class);

    public static String extractPDFText(InputStream inputStream) throws IOException {
        PDFParser parser = new PDFParser(inputStream);
        parser.parse();
        COSDocument cosDoc = parser.getDocument();
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(new PDDocument(cosDoc));
        cosDoc.close();
        return text;
    }

    public static String extractDocXText(InputStream inputStream) throws IOException {
        XWPFDocument doc = new XWPFDocument(inputStream);
        XWPFWordExtractor msWord2007Extractor = new XWPFWordExtractor(doc);
        return msWord2007Extractor.getText();
    }

    public static String extractDocText(InputStream inputStream) throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(inputStream);
        WordExtractor msWord2003Extractor = new WordExtractor(fs);
        return msWord2003Extractor.getText();
    }

    public static String extractPlainText(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static File writeStream(InputStream uploadedInputStream, String fileName)
            throws PersistenceException {
        String randomFolderName = RandomStringUtils.randomAlphanumeric(10);
        String tmpFolder = System.getProperty(APIConstants.JAVA_IO_TMPDIR) + File.separator
                + APIConstants.DOC_UPLOAD_TMPDIR + File.separator + randomFolderName;
        File docFile = new File(tmpFolder);
        FileOutputStream outFileStream = null;

        boolean folderCreated = docFile.mkdirs();
        if (!folderCreated) {
            throw new PersistenceException("Failed to create temporary folder for document upload ");
        }

        try {
            outFileStream = new FileOutputStream(new File(docFile.getAbsolutePath(), fileName));
            int read;
            byte[] bytes = new byte[1024];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outFileStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            String errorMessage = "Error in transferring files.";
            log.error(errorMessage, e);
            throw new PersistenceException(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(outFileStream);
        }
        return docFile;
    }

    public static InputStream readStream(File docFile, String fileName) throws PersistenceException {
        try {
            InputStream newInputStream = new FileInputStream(docFile.getAbsolutePath() + File.separator + fileName);
            return newInputStream;
        } catch (FileNotFoundException e) {
            throw new PersistenceException("Failed to open file ");
        }
    }

}
