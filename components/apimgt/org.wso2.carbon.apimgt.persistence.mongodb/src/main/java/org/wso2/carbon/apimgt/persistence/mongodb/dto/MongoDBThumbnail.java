package org.wso2.carbon.apimgt.persistence.mongodb.dto;

import org.bson.types.ObjectId;

public class MongoDBThumbnail {
    private ObjectId thumbnailReference;
    private String contentType;
    private String name;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObjectId getThumbnailReference() {
        return thumbnailReference;
    }

    public void setThumbnailReference(ObjectId thumbnailReference) {
        this.thumbnailReference = thumbnailReference;
    }

}
