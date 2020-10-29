package org.wso2.carbon.apimgt.persistence.dto;

import org.wso2.carbon.apimgt.api.model.DocumentationType;

import java.io.Serializable;

public class DocumentationInfo implements Serializable {
    private String id;
    private String name;
    private Documentation.DocumentSourceType sourceType;
    private DocumentationType type;

    public enum DocumentSourceType {
        INLINE("In line"), MARKDOWN("Markdown"), URL("URL"), FILE("File");

        private String type;

        private DocumentSourceType(String type) {
            this.type = type;
        }
    }

    public DocumentationInfo(DocumentationType type, String name) {
        this.type = type;
        this.name = name;
    }

    public DocumentationType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentationInfo.DocumentSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(DocumentationInfo.DocumentSourceType sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentationInfo that = (DocumentationInfo) o;

        return name.equals(that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
