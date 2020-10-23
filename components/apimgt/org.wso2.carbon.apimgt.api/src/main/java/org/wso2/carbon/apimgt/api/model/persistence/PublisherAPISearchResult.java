package org.wso2.carbon.apimgt.api.model.persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result returned when searching APIs from the persistence layer, to be displayed in the Publisher.
 */
public class PublisherAPISearchResult {
    int returnedAPIsCount;
    int totalAPIsCount;
    int start;
    int offset;
    List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<>();
}
