package org.wso2.carbon.apimgt.impl.indexing.indexer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.apache.solr.common.SolrException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class PlainTextIndexer implements Indexer {

	public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException,
            RegistryException {
				
		IndexDocument indexDoc = new IndexDocument(fileData.path, RegistryUtils.decodeBytes(fileData.data), null);
				
		Map<String, List<String>> fields = new HashMap<String, List<String>>();
		fields.put("path", Arrays.asList(fileData.path));
				
		if (fileData.mediaType != null) {
			fields.put(IndexingConstants.FIELD_MEDIA_TYPE, Arrays.asList(fileData.mediaType));
		} else {
			fields.put(IndexingConstants.FIELD_MEDIA_TYPE, Arrays.asList("text/(.)"));
		}
		
		indexDoc.setFields(fields);
		
		return indexDoc;
	}	

}
