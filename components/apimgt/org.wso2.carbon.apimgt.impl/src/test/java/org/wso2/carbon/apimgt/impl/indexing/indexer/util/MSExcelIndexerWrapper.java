package org.wso2.carbon.apimgt.impl.indexing.indexer.util;

import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.wso2.carbon.apimgt.impl.indexing.indexer.MSExcelIndexer;
import org.wso2.carbon.registry.indexing.AsyncIndexer;

import java.io.IOException;

public class MSExcelIndexerWrapper extends MSExcelIndexer {
    private XSSFExcelExtractor xssfExcelExtractor;
    private ExcelExtractor excelExtractor;

    public MSExcelIndexerWrapper(XSSFExcelExtractor xssfExtractor, ExcelExtractor excelExtractor) {
        this.xssfExcelExtractor = xssfExtractor;
        this.excelExtractor = excelExtractor;
    }

    @Override
    protected XSSFExcelExtractor getXssfExcelExtractor(AsyncIndexer.File2Index fileData) throws IOException {
        return this.xssfExcelExtractor;
    }

    @Override
    protected ExcelExtractor getExcelExtractor(AsyncIndexer.File2Index fileData) throws IOException {
        return this.excelExtractor;
    }
}
