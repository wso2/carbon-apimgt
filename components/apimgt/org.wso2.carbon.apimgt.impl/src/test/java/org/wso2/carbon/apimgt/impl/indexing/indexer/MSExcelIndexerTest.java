package org.wso2.carbon.apimgt.impl.indexing.indexer;

import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.solr.common.SolrException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.indexing.indexer.util.MSExcelIndexerWrapper;
import org.wso2.carbon.registry.indexing.AsyncIndexer;

import java.io.IOException;

public class MSExcelIndexerTest {
    private ExcelExtractor excelExtractor;
    private XSSFExcelExtractor xssfExtractor;
    private MSExcelIndexer msExcelIndexer;
    private AsyncIndexer.File2Index file2Index;

    @Before
    public void setup() {
        excelExtractor = Mockito.mock(ExcelExtractor.class);
        xssfExtractor = Mockito.mock(XSSFExcelExtractor.class);
        msExcelIndexer = new MSExcelIndexerWrapper(xssfExtractor, excelExtractor);
        file2Index = new AsyncIndexer.File2Index("".getBytes(),
                "", "", -1234, "");
    }

    @Test
    public void testShouldReturnIndexedDocmentWhenParameterCorrect() {
        String excelText = "excel";
        Mockito.when(excelExtractor.getText())
                .thenReturn(excelText)
                .thenThrow(OfficeXmlFileException.class)
                .thenThrow(Exception.class);
        Mockito.when(xssfExtractor.getText()).thenReturn(excelText);

        try {
            // retrieving indexed document with ExcelExtractor
            msExcelIndexer.getIndexedDocument(file2Index);

            // switching the mediaType null check
            file2Index = new AsyncIndexer.File2Index("".getBytes(),
                    null, "", -1234, "");

            // retrieving indexed document with MSExcelIndexer
            // Note: .thenReturn(excelText).thenThrow(OfficeXmlFileException.class) this switches the indexer
            msExcelIndexer.getIndexedDocument(file2Index);

            // switching to silent Exception catch block
            msExcelIndexer.getIndexedDocument(file2Index);
        } catch (Exception e) {
            Assert.fail("Should not throw any exceptions");
        }
    }

    @Test(expected = SolrException.class)
    public void testShouldThrowExceptionWhenErrorOccurs() {
        Mockito.when(excelExtractor.getText()).thenThrow(OfficeXmlFileException.class);
        Mockito.when(xssfExtractor.getText()).thenThrow(IOException.class);

        // SolrException is expected
        msExcelIndexer.getIndexedDocument(file2Index);
    }

}
