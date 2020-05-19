package org.wso2.carbon.apimgt.impl.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class ClaimMappingDto implements Serializable {

    @SerializedName("remoteClaim")
    private String remoteClaim;
    @SerializedName("localClaim")
    private String localClaim;

    public ClaimMappingDto() {

    }

    public ClaimMappingDto(String remoteClaim, String localClaim) {

        this.remoteClaim = remoteClaim;
        this.localClaim = localClaim;
    }

    public String getRemoteClaim() {

        return remoteClaim;
    }

    public void setRemoteClaim(String remoteClaim) {

        this.remoteClaim = remoteClaim;
    }

    public String getLocalClaim() {

        return localClaim;
    }

    public void setLocalClaim(String localClaim) {

        this.localClaim = localClaim;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimMappingDto that = (ClaimMappingDto) o;
        return Objects.equals(localClaim, that.localClaim);
    }

    @Override
    public int hashCode() {

        return Objects.hash(localClaim);
    }
}
