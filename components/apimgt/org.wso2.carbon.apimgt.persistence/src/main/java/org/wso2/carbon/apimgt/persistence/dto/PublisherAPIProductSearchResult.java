package org.wso2.carbon.apimgt.persistence.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result returned when searching API products from the persistence layer, to be displayed in the Publisher.
 */
public class PublisherAPIProductSearchResult {
    int returnedAPIsCount;
    int totalAPIsCount;
    List<PublisherAPIProductInfo> publisherAPIProductInfoList = new ArrayList<>();

    public int getReturnedAPIsCount() {
        return returnedAPIsCount;
    }

    public void setReturnedAPIsCount(int returnedAPIsCount) {
        this.returnedAPIsCount = returnedAPIsCount;
    }

    public int getTotalAPIsCount() {
        return totalAPIsCount;
    }

    public void setTotalAPIsCount(int totalAPIsCount) {
        this.totalAPIsCount = totalAPIsCount;
    }

    public List<PublisherAPIProductInfo> getPublisherAPIProductInfoList() {
        return publisherAPIProductInfoList;
    }

    public void setPublisherAPIProductInfoList(List<PublisherAPIProductInfo> publisherAPIProductInfoList) {
        this.publisherAPIProductInfoList = publisherAPIProductInfoList;
    }

}
