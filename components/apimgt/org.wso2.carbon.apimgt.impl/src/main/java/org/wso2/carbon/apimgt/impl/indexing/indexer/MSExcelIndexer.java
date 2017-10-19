package org.wso2.carbon.apimgt.impl.indexing.indexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class MSExcelIndexer implements Indexer {
	public static final Log log = LogFactory.getLog(MSExcelIndexer.class);
	
	public IndexDocument getIndexedDocument(File2Index fileData)
			throws SolrException {
		try {

            String excelText = null;
            try {

                //Extract Excel 2003 (.xsl) document files
	            ExcelExtractor extractor = getExcelExtractor(fileData);
                excelText = extractor.getText();
            } catch (OfficeXmlFileException e){

                //if 2003 Excel (.xsl) extraction failed, try with Excel 2007 (.xslx) document files extractor
	            XSSFExcelExtractor xssfExcelExtractor = getXssfExcelExtractor(fileData);
                excelText = xssfExcelExtractor.getText();
            } catch (Exception e){
                String msg = "Failed to extract the document";
                log.error(msg, e);
            }

			IndexDocument indexDoc = new IndexDocument(fileData.path, excelText, null);
			Map<String, List<String>> fields = new HashMap<String, List<String>>();
			fields.put("path", Collections.singletonList(fileData.path));

			if (fileData.mediaType != null) {
				fields.put(IndexingConstants.FIELD_MEDIA_TYPE, Collections.singletonList(fileData.mediaType));
			} else {
				fields.put(IndexingConstants.FIELD_MEDIA_TYPE, Collections.singletonList("application/vnd.ms-excel"));
			}
			indexDoc.setFields(fields);
			
			return indexDoc;
		} catch (IOException e) {
			String msg = "Failed to write to the index";
			log.error(msg, e);
			throw new SolrException(ErrorCode.SERVER_ERROR, msg, e);
		}

	}

	protected XSSFExcelExtractor getXssfExcelExtractor(File2Index fileData) throws IOException {
		XSSFWorkbook xssfSheets = new XSSFWorkbook(new ByteArrayInputStream(fileData.data));
		return new XSSFExcelExtractor(xssfSheets);
	}

	protected ExcelExtractor getExcelExtractor(File2Index fileData) throws IOException {
		POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(fileData.data));
		return new ExcelExtractor(fs);
	}

}
