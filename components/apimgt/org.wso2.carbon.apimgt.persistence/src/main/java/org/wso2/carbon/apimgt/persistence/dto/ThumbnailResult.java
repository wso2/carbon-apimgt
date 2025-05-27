package org.wso2.carbon.apimgt.persistence.dto;

import java.io.InputStream;

public class ThumbnailResult {
    private InputStream content;
    private String metadata;

    public ThumbnailResult(InputStream content, String metadata) {
        this.content = content;
        this.metadata = metadata;
    }

    public InputStream getContent() {
        return content;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
