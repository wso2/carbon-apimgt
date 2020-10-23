package org.wso2.carbon.apimgt.persistence.dto;

import java.util.List;

public class DocumentSearchResult {
    int returnedDocsCount;
    int totalDocsCount;
    int start;
    int offset;
    List<DocumentationInfo> documentationInfoList;

    public int getReturnedDocsCount() {
        return returnedDocsCount;
    }

    public void setReturnedDocsCount(int returnedDocsCount) {
        this.returnedDocsCount = returnedDocsCount;
    }

    public int getTotalDocsCount() {
        return totalDocsCount;
    }

    public void setTotalDocsCount(int totalDocsCount) {
        this.totalDocsCount = totalDocsCount;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public List<DocumentationInfo> getDocumentationInfoList() {
        return documentationInfoList;
    }

    public void setDocumentationInfoList(List<DocumentationInfo> documentationInfoList) {
        this.documentationInfoList = documentationInfoList;
    }

}
