package org.wso2.carbon.apimgt.gateway.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertificateDataHolder {

    private static final CertificateDataHolder Instance = new CertificateDataHolder();
    private Map<String, List<String>> apiToCertificatesMap = new HashMap();

    private CertificateDataHolder() {

    }

    public Map<String, List<String>> getApiToCertificatesMap() {

        return apiToCertificatesMap;
    }

    public void setApiToCertificatesMap(Map<String, List<String>> apiToCertificatesMap) {

        this.apiToCertificatesMap = apiToCertificatesMap;
    }

    public static CertificateDataHolder getInstance() {

        return Instance;
    }

    public void addApiToAliasList(String apiId, List<String> aliasList) {

        apiToCertificatesMap.put(apiId, aliasList);
    }

    public List<String> getCertificateAliasListForAPI(String apiId) {
       return apiToCertificatesMap.get(apiId);
    }
}
