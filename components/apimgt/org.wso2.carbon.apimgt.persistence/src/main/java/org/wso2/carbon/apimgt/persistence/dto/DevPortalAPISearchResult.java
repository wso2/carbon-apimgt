package org.wso2.carbon.apimgt.persistence.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result returned when searching APIs from the persistence layer, to be displayed in the Dev Portal.
 */
public class DevPortalAPISearchResult {
    private int returnedAPIsCount;
    private int totalAPIsCount;
    private int start;
    private int offset;

    public int getTotalAPIsCount() {
        return totalAPIsCount;
    }

    public void setTotalAPIsCount(int totalAPIsCount) {
        this.totalAPIsCount = totalAPIsCount;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public List<DevPortalAPIInfo> getDevPortalAPIInfoList() {
        return devPortalAPIInfoList;
    }

    public void setDevPortalAPIInfoList(List<DevPortalAPIInfo> devPortalAPIInfoList) {
        this.devPortalAPIInfoList = devPortalAPIInfoList;
    }

    private List<DevPortalAPIInfo> devPortalAPIInfoList= new ArrayList<>();

    public int getReturnedAPIsCount() {
        return returnedAPIsCount;
    }

    public void setReturnedAPIsCount(int returnedAPIsCount) {
        this.returnedAPIsCount = returnedAPIsCount;
    }
}
