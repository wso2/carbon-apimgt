package org.wso2.carbon.apimgt.impl.indexing.indexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class MSWordIndexer implements Indexer {
	public static final Log log = LogFactory.getLog(MSWordIndexer.class);

	public IndexDocument getIndexedDocument(File2Index fileData)
			throws SolrException {
		try {
            String wordText = null;
            try {
                //Extract MSWord 2003 document files
                POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(fileData.data));

                WordExtractor msWord2003Extractor = new WordExtractor(fs);
                wordText = msWord2003Extractor.getText();

            }catch (OfficeXmlFileException e){
                //if 2003 extraction failed, try with MSWord 2007 document files extractor
                XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(fileData.data));

                XWPFWordExtractor msWord2007Extractor = new XWPFWordExtractor(doc);
                wordText = msWord2007Extractor.getText();

            }catch (Exception e){
                //The reason for not throwing an exception is that since this is an indexer that runs in the background
                //throwing an exception might lead to adverse behaviors in the client side and might lead to
                //other files not being indexed
                String msg = "Failed to extract the document while indexing";
                log.error(msg, e);
            }
			IndexDocument indexDoc = new IndexDocument(fileData.path, wordText, null);
			
			Map<String, List<String>> fields = new HashMap<String, List<String>>();
			fields.put("path", Collections.singletonList(fileData.path));
			if (fileData.mediaType != null) {
				fields.put(IndexingConstants.FIELD_MEDIA_TYPE, Collections.singletonList(fileData.mediaType));
			} else {
				fields.put(IndexingConstants.FIELD_MEDIA_TYPE, Collections.singletonList("application/pdf"));
			}
			
			indexDoc.setFields(fields);
			
			return indexDoc;
			
		} catch (IOException e) {
			String msg = "Failed to write to the index";
			log.error(msg, e);
			throw new SolrException(ErrorCode.SERVER_ERROR, msg, e);
		}
	}
}
