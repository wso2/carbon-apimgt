package org.wso2.carbon.apimgt.api.model.persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result returned when searching APIs from the persistence layer, to be displayed in the Dev Portal.
 */
public class DevPortalAPISearchResult {
    int returnedAPIsCount;
    int totalAPIsCount;
    int start;
    int offset;
    List<DevPortalAPIInfo> devPortalAPIInfoList= new ArrayList<>();
}
