package org.wso2.carbon.apimgt.impl.indexing.indexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class PDFIndexer implements Indexer {

	public static final Log log = LogFactory.getLog(PDFIndexer.class);

	public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException {
		try (PDDocument document = loadPDF(fileData)) {
			PDFTextStripper stripper = getPdfTextStripper();
			String docText = stripper.getText(document);

			IndexDocument indexDoc = new IndexDocument(fileData.path, docText, null);

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

	protected PDFTextStripper getPdfTextStripper() throws IOException {
		return new PDFTextStripper();
	}

	protected PDDocument loadPDF(File2Index fileData) throws IOException {
		return Loader.loadPDF(new RandomAccessReadBuffer(new ByteArrayInputStream(fileData.data)));
	}

}
