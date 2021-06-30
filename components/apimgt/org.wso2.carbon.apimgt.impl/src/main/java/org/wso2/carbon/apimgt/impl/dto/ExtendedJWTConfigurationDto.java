package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.common.gateway.dto.JWTConfigurationDto;

public class ExtendedJWTConfigurationDto extends JWTConfigurationDto {
    private String jwtGeneratorImplClass = "org.wso2.carbon.apimgt.keymgt.token.JWTGenerator";
    private String claimRetrieverImplClass;
    private boolean tenantBasedSigningEnabled;

    public String getClaimRetrieverImplClass() {

        return claimRetrieverImplClass;
    }

    public void setClaimRetrieverImplClass(String claimRetrieverImplClass) {

        this.claimRetrieverImplClass = claimRetrieverImplClass;
    }

    public String getJwtGeneratorImplClass() {

        return jwtGeneratorImplClass;
    }

    public void setJwtGeneratorImplClass(String jwtGeneratorImplClass) {

        this.jwtGeneratorImplClass = jwtGeneratorImplClass;
    }

    public boolean isTenantBasedSigningEnabled() {

        return tenantBasedSigningEnabled;
    }

    public void setTenantBasedSigningEnabled(boolean tenantBasedSigningEnabled) {

        this.tenantBasedSigningEnabled = tenantBasedSigningEnabled;
    }
}
