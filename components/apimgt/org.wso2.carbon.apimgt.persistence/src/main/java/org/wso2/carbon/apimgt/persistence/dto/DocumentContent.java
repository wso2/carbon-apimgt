package org.wso2.carbon.apimgt.persistence.dto;

import org.wso2.carbon.apimgt.api.model.ResourceFile;

public class DocumentContent {
    ResourceFile resourceFile;
    String textContent;
    private ContentSourceType sourceType;

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public ContentSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ContentSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public ResourceFile getResourceFile() {
        return resourceFile;
    }

    public void setResourceFile(ResourceFile resourceFile) {
        this.resourceFile = resourceFile;
    }

    public enum ContentSourceType {
        INLINE("In line"), MARKDOWN("Markdown"), FILE("File");

        private String type;

        private ContentSourceType(String type) {
            this.type = type;
        }
    }
}
