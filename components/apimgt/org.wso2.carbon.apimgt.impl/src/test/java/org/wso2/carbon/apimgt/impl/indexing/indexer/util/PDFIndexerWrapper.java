/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
