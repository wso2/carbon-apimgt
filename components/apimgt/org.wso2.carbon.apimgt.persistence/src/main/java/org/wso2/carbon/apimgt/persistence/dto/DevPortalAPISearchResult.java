package org.wso2.carbon.apimgt.persistence.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result returned when searching APIs from the persistence layer, to be displayed in the Dev Portal.
 */
public class DevPortalAPISearchResult {
    private int returnedAPIsCount;
    private int totalAPIsCount;

    private List<DevPortalAPI> devPortalAPIList = new ArrayList<>();

    public void setDevPortalAPIList(List<DevPortalAPI> devPortalAPIList){
        this.devPortalAPIList = devPortalAPIList;
    }
    public List<DevPortalAPI> getDevPortalAPIList(){
        return devPortalAPIList;
    }

    private List<DevPortalAPIInfo> devPortalAPIInfoList= new ArrayList<>();

    public int getTotalAPIsCount() {
        return totalAPIsCount;
    }

    public void setTotalAPIsCount(int totalAPIsCount) {
        this.totalAPIsCount = totalAPIsCount;
    }


    public List<DevPortalAPIInfo> getDevPortalAPIInfoList() {
        return devPortalAPIInfoList;
    }

    public void setDevPortalAPIInfoList(List<DevPortalAPIInfo> devPortalAPIInfoList) {
        this.devPortalAPIInfoList = devPortalAPIInfoList;
    }

    public int getReturnedAPIsCount() {
        return returnedAPIsCount;
    }

    public void setReturnedAPIsCount(int returnedAPIsCount) {
        this.returnedAPIsCount = returnedAPIsCount;
    }

    @Override
    public String toString() {
        return "DevPortalAPISearchResult [returnedAPIsCount=" + returnedAPIsCount + ", totalAPIsCount=" + totalAPIsCount
                + ", devPortalAPIInfoList=" + devPortalAPIInfoList + "]";
    }
}
