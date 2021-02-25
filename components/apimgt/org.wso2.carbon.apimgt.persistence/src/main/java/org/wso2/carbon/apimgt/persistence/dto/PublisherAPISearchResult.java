package org.wso2.carbon.apimgt.persistence.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result returned when searching APIs from the persistence layer, to be displayed in the Publisher.
 */
public class PublisherAPISearchResult {
    int returnedAPIsCount;
    int totalAPIsCount;
    List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<>();

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

    public List<PublisherAPIInfo> getPublisherAPIInfoList() {
        return publisherAPIInfoList;
    }

    public void setPublisherAPIInfoList(List<PublisherAPIInfo> publisherAPIInfoList) {
        this.publisherAPIInfoList = publisherAPIInfoList;
    }
}
