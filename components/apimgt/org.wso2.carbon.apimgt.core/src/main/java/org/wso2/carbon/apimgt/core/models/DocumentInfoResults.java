/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns all Document info as a list
 */
public final class DocumentInfoResults {
    private List<DocumentInfo> documentInfoList = new ArrayList<>();
    private boolean isMoreResultsExist;
    private int nextOffset;

    public List<DocumentInfo> getDocumentInfoList() {
        return documentInfoList;
    }

    public void addDocumentInfo(DocumentInfo documentInfo) {
        this.documentInfoList.add(documentInfo);
    }

    public boolean isMoreResultsExist() {
        return isMoreResultsExist;
    }

    public void setMoreResultsExist(boolean moreResultsExist) {
        isMoreResultsExist = moreResultsExist;
    }

    public int getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(int nextOffset) {
        this.nextOffset = nextOffset;
    }
}
