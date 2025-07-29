package org.wso2.carbon.apimgt.persistence.dto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

public class FileResult {
    private static final Log log = LogFactory.getLog(FileResult.class);

    private InputStream content;
    private String metadata;

    public FileResult(InputStream content, String metadata) {
        log.debug("Creating a new FileResult with content and metadata");
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
        log.debug("Setting content for FileResult");
        this.content = content;
    }

    public void setMetadata(String metadata) {
        log.debug("Setting metadata for FileResult");
        this.metadata = metadata;
    }
}
