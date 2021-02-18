package org.wso2.carbon.apimgt.impl.dto;

import java.util.HashSet;
import java.util.Set;

public class GatewayCleanupSkipList {

    private Set<String> apis = new HashSet<>();
    private Set<String> endpoints = new HashSet();
    private Set<String> localEntries = new HashSet<>();
    private Set<String> sequences = new HashSet<>();

    public Set<String> getApis() {

        return apis;
    }

    public void setApis(Set<String> apis) {

        this.apis = apis;
    }

    public Set<String> getEndpoints() {

        return endpoints;
    }

    public void setEndpoints(Set<String> endpoints) {

        this.endpoints = endpoints;
    }

    public Set<String> getLocalEntries() {

        return localEntries;
    }

    public void setLocalEntries(Set<String> localEntries) {

        this.localEntries = localEntries;
    }

    public Set<String> getSequences() {

        return sequences;
    }

    public void setSequences(Set<String> sequences) {

        this.sequences = sequences;
    }
}
