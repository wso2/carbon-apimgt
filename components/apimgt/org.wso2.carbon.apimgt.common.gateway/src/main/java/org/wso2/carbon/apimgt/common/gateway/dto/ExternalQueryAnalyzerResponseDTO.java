package org.wso2.carbon.apimgt.common.gateway.dto;

import java.util.ArrayList;
import java.util.List;

public class ExternalQueryAnalyzerResponseDTO {
    boolean isVulnerable;

    List<String> vulList = new ArrayList<>();

    public boolean isVulnerable() {
        return isVulnerable;
    }

    public void setVulnerable(boolean vulnerable) {
        isVulnerable = vulnerable;
    }

    public List<String> getVulList() {
        return vulList;
    }

    public void addVulToList(String vul){
        this.vulList.add(vul);
    }


}
