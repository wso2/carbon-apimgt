package org.wso2.carbon.apimgt.impl.config;

import org.apache.axiom.om.OMElement;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;

public interface APIMConfigService {

    public void addExternalStoreConfig(String organization, String externalStoreConfig) throws APIManagementException;

    public void updateExternalStoreConfig(String organization, String externalStoreConfig) throws APIManagementException;

    public String getExternalStoreConfig(String organization) throws APIManagementException;

    public void addTenantConfig(String organization, String tenantConfig) throws APIManagementException;

    public String getTenantConfig(String organization) throws APIManagementException;

    public void updateTenantConfig(String organization, String tenantConfig) throws APIManagementException;

    public String getWorkFlowConfig(String organization) throws APIManagementException;

    public void updateWorkflowConfig(String organization, String workflowConfig) throws APIManagementException;

    public void addWorkflowConfig(String organization, String workflowConfig) throws APIManagementException;

    public String getGAConfig(String organization) throws APIManagementException;

    public void updateGAConfig(String organization, String workflowConfig) throws APIManagementException;

    public void addGAConfig(String organization, String workflowConfig) throws APIManagementException;

    public String getSelfSighupConfig(String organization) throws APIManagementException;

    public void updateSelfSighupConfig(String organization, String workflowConfig) throws APIManagementException;

    public void addSelfSighupConfig(String organization, String workflowConfig) throws APIManagementException;

}
