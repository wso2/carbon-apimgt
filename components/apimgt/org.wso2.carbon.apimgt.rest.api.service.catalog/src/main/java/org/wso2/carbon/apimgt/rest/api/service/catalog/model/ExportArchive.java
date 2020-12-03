package org.wso2.carbon.apimgt.rest.api.service.catalog.model;

public class ExportArchive {
    private String archiveName;
    private String eTag;

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

}
