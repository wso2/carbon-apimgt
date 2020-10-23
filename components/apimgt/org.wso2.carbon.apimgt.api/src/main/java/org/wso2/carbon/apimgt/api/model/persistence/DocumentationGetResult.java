package org.wso2.carbon.apimgt.api.model.persistence;

import java.util.List;

public class DocumentationGetResult {
    int returnedDocsCount;
    int totalDocsCount;
    int start;
    int offset;
    List<DocumentationInfo> documentationInfoList;
}
