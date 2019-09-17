package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Identifier;

import java.util.Comparator;

public class APIAPIProductNameComparator implements Comparator<Object> {

    private static class Provider {
        private String provider1;
        private String provider2;

        Provider(String provider1, String provider2) {
            this.provider1 = provider1;
            this.provider2 = provider2;
        }

        String getFirst() {
            return provider1;
        }

        String getSecond() {
            return provider2;
        }
    }

    private static class Name {
        private String name1;
        private String name2;

        Name(String name1, String name2) {
            this.name1 = name1;
            this.name2 = name2;
        }

        String getFirst() {
            return name1;
        }

        String getSecond() {
            return name2;
        }
    }

    private static class Version {
        private String version1;
        private String version2;

        Version(String version1, String version2) {
            this.version1 = version1;
            this.version2 = version2;
        }

        String getFirst() {
            return version1;
        }

        String getSecond() {
            return version2;
        }
    }

    @Override
    public int compare(Object o1, Object o2) {
        Identifier identifier1;
        Identifier identifier2;

        if (o1 instanceof API) {
            identifier1 = ((API) o1).getId();
        } else {
            identifier1 = ((APIProduct) o1).getId();
        }

        if (o2 instanceof API) {
            identifier2 = ((API) o2).getId();
        } else {
            identifier2 = ((APIProduct) o2).getId();
        }

        Provider provider = new Provider(identifier1.getProviderName(), identifier2.getProviderName());
        Name name = new Name(identifier1.getName(), identifier2.getName());
        Version version = new Version(identifier1.getVersion(), identifier2.getVersion());

        return compareFields(provider, name, version);
    }

    private int compareFields(Provider provider, Name name, Version version) {
        if (provider.getFirst().equalsIgnoreCase(provider.getSecond())) {
            if (name.getFirst().equals(name.getSecond())) {
                //only compare version
                return version.getFirst().compareToIgnoreCase(version.getSecond());
            } else {
                //only compare API name
                return name.getFirst().compareToIgnoreCase(name.getSecond());
            }
        } else {
            //only compare provider name
            return provider.getFirst().compareToIgnoreCase(provider.getSecond());
        }
    }
}
