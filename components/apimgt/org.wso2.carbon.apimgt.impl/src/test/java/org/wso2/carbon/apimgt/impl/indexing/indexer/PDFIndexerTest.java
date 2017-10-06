package org.wso2.carbon.apimgt.impl.indexing.indexer;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.solr.common.SolrException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.indexing.indexer.util.PDFIndexerWrapper;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

import java.io.IOException;

public class PDFIndexerTest {
    private AsyncIndexer.File2Index file2Index;

    @Before
    public void setup() {
        file2Index = new AsyncIndexer.File2Index("".getBytes(),
                null, "", -1234, "");
    }

    @Test
    public void testShouldReturnIndexedDocumentWhenParameterCorrect() throws IOException {
        String mediaType = "application/pdf+test";
        final String MEDIA_TYPE = "mediaType";
        PDFParser parser = Mockito.mock(PDFParser.class);
        COSDocument cosDoc = Mockito.mock(COSDocument.class);
        PDFTextStripper pdfTextStripper = Mockito.mock(PDFTextStripper.class);
        Mockito.doThrow(IOException.class).when(cosDoc).close();
        Mockito.when(parser.getDocument()).thenReturn(new COSDocument()).thenReturn(cosDoc);
        Mockito.when(pdfTextStripper.getText(new PDDocument())).thenReturn("");
        PDFIndexer pdfIndexer = new PDFIndexerWrapper(parser, pdfTextStripper);

        // should return the the default media type when media type is not defined in file2Index
        IndexDocument pdf = pdfIndexer.getIndexedDocument(file2Index);
        if (!"application/pdf".equals(pdf.getFields().get(MEDIA_TYPE).get(0))) {
            Assert.fail();
        }

        // should return the media type we have set in the file2Index even if error occurs in finally block
        file2Index.mediaType = mediaType;
        pdf = pdfIndexer.getIndexedDocument(file2Index);
        if (!mediaType.equals(pdf.getFields().get(MEDIA_TYPE).get(0))) {
            Assert.fail();
        }

    }

    @Test(expected = SolrException.class)
    public void testShouldThrowExceptionWhenParameterCorrect() throws IOException {
        PDFIndexer pdfIndexer = new PDFIndexer();

        // SolrException is expected
        pdfIndexer.getIndexedDocument(file2Index);
    }
}
