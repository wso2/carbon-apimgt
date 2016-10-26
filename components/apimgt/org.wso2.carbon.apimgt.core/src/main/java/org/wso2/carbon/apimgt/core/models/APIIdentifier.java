//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.wso2.carbon.apimgt.core.models;

public class APIIdentifier {
    private final String providerName;
    private final String apiName;
    private final String version;
    private String tier;
    private String applicationId;

    public String getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getTier() {
        return this.tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public APIIdentifier(String providerName, String apiName, String version) {
        this.providerName = providerName;
        this.apiName = apiName;
        this.version = version;
    }

    public String getProviderName() {
        return this.providerName;
    }

    public String getApiName() {
        return this.apiName;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            APIIdentifier that = (APIIdentifier) o;
            return this.apiName.equals(that.apiName) && this.providerName.equals(that.providerName) && this.version.equals(that.version);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.providerName.hashCode();
        result = 31 * result + this.apiName.hashCode();
        result = 31 * result + this.version.hashCode();
        return result;
    }

    public String toString() {
        return this.getProviderName() + '-' + this.getApiName() + '-' + this.getVersion();
    }
}
