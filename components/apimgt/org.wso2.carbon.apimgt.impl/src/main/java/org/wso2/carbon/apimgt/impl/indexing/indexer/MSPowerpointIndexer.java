package org.wso2.carbon.apimgt.impl.indexing.indexer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MSPowerpointIndexer implements Indexer {
	public static final Log log = LogFactory.getLog(MSPowerpointIndexer.class);
	
	public IndexDocument getIndexedDocument(File2Index fileData)
			throws SolrException {
		try {
            String ppText = null;
            try {
                //Extract PowerPoint 2003 (.ppt) document files
				HSLFSlideShow slideShow = new HSLFSlideShow(new ByteArrayInputStream(fileData.data));
				SlideShowExtractor extractor = new SlideShowExtractor(slideShow);
                ppText = extractor.getText();
            } catch (OfficeXmlFileException e){
                //if 2003 PowerPoint (.ppt) extraction failed, try with PowerPoint 2007 (.pptx) document file extractor
				XMLSlideShow slideShow = new XMLSlideShow(new ByteArrayInputStream(fileData.data));
				SlideShowExtractor extractor = new SlideShowExtractor(slideShow);
                ppText = extractor.getText();

            } catch (Exception e){
                String msg = "Failed to extract the document";
                log.error(msg, e);
            }

			IndexDocument indexDoc = new IndexDocument(fileData.path, ppText, null);
			
			Map<String, List<String>> fields = new HashMap<String, List<String>>();
			fields.put("path", Collections.singletonList(fileData.path));
			if (fileData.mediaType != null) {
				fields.put(IndexingConstants.FIELD_MEDIA_TYPE, Collections.singletonList(fileData.mediaType));
			} else {
				fields.put(IndexingConstants.FIELD_MEDIA_TYPE, Collections.singletonList("application/vnd" +
				                                                                         ".ms-powerpoint"));
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
