package org.wso2.carbon.apimgt.persistence.utils;

import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIInfo;

import java.util.Comparator;

public class PublisherAPISearchResultComparator implements Comparator<Object>  {
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
        String name1 = null;
        String name2 = null;
        String version1 = null;
        String version2 = null;
        String provider1 = null;
        String provider2 = null;

        if (o1 instanceof PublisherAPIInfo) {
            name1 = ((PublisherAPIInfo) o1).getApiName();
            version1 = ((PublisherAPIInfo) o1).getVersion();
            provider1 = ((PublisherAPIInfo) o1).getProviderName();
        }

        if (o2 instanceof PublisherAPIInfo) {
            name2 = ((PublisherAPIInfo) o2).getApiName();
            version2 = ((PublisherAPIInfo) o2).getVersion();
            provider2 = ((PublisherAPIInfo) o2).getProviderName();
        }

        Provider provider = new Provider(provider1, provider2);
        Name name = new Name(name1, name2);
        Version version = new Version(version1, version2);

        return compareFields(provider, name, version);
    }

    private int compareFields(Provider provider, Name name, Version version) {
        if (name.getFirst().equalsIgnoreCase(name.getSecond())) {
            if (version.getFirst().equals(version.getSecond())) {
                //only compare provider
                return provider.getFirst().compareToIgnoreCase(provider.getSecond());
            } else {
                //only compare API version
                return version.getFirst().compareToIgnoreCase(version.getSecond());
            }
        } else {
            //only compare API name
            return name.getFirst().compareToIgnoreCase(name.getSecond());
        }
    }
}
