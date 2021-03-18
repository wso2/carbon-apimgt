package org.wso2.carbon.apimgt.impl.dto;

import java.io.Serializable;

public class ThirdPartyEnvironment extends org.wso2.carbon.apimgt.api.model.Environment implements Serializable {

    private String organization;
    private String provider;

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThirdPartyEnvironment that = (ThirdPartyEnvironment) o;
        // return Objects.equals(environmentName, that.environmentName) && Objects.equals(organization, that.organization) && Objects.equals(provider, that.provider);
        if (!getName().equals(that.getName())) return false;
        if (!provider.equals(that.getProvider())) return false;
        if (!organization.equals(that.getOrganization())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        //return Objects.hash(environmentName, organization, provider);
        int result = provider.hashCode();
        return  31 * result + getName().hashCode();
    }
}
