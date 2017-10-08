package org.wso2.carbon.apimgt.impl.indexing.indexer.util;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.util.PDFTextStripper;
import org.wso2.carbon.apimgt.impl.indexing.indexer.PDFIndexer;
import org.wso2.carbon.registry.indexing.AsyncIndexer;

import java.io.IOException;

public class PDFIndexerWrapper extends PDFIndexer {
    private PDFParser pdfParser;
    private PDFTextStripper pdfTextStripper;

    public PDFIndexerWrapper(PDFParser pdfParser, PDFTextStripper stripper) {
        this.pdfParser = pdfParser;
        this.pdfTextStripper = stripper;
    }

    @Override
    protected PDFParser getPdfParser(AsyncIndexer.File2Index fileData) throws IOException {
        return pdfParser;
    }

    @Override
    protected PDFTextStripper getPdfTextStripper() throws IOException {
        return this.pdfTextStripper;
    }
}
