package org.wso2.carbon.apimgt.impl.indexing.indexer;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class PlainTextIndexerTest {

    @Test
    public void testShouldReturnIndexedDocumentWhenParameterCorrect() throws RegistryException {
        String mediaType = "text/txt";
        final String MEDIA_TYPE = "mediaType";
        AsyncIndexer.File2Index file2Index = new AsyncIndexer.File2Index("".getBytes(),
                null, "", -1234, "");
        PlainTextIndexer indexer = new PlainTextIndexer();

        // should return the default media type when media type is not defined in file2Index
        IndexDocument text = indexer.getIndexedDocument(file2Index);
        if (!"text/(.)".equals(text.getFields().get(MEDIA_TYPE).get(0))) {
            Assert.fail();
        }

        // should return the media type we have set in the file2Index
        file2Index.mediaType = mediaType;
        text = indexer.getIndexedDocument(file2Index);
        if (!mediaType.equals(text.getFields().get(MEDIA_TYPE).get(0))) {
            Assert.fail();
        }

    }
}
