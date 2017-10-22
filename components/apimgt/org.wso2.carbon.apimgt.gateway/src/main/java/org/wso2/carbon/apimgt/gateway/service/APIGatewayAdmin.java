package org.wso2.carbon.apimgt.gateway.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.MediationSecurityAdminServiceClient;
import org.wso2.carbon.apimgt.gateway.utils.RESTAPIAdminClient;
import org.wso2.carbon.apimgt.gateway.utils.SequenceAdminServiceClient;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;

public class APIGatewayAdmin extends org.wso2.carbon.core.AbstractAdmin {

    private static Log log = LogFactory.getLog(APIGatewayAdmin.class);

    /**
     * Add the API to the gateway
     *
     * @param apiProviderName
     * @param apiName
     * @param version
     * @param apiConfig
     * @param tenantDomain
     * @throws AxisFault
     */
    public boolean addApiForTenant(String apiProviderName, String apiName, String version, String apiConfig,
                                String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.addApi(apiConfig, tenantDomain);
    }

    protected RESTAPIAdminClient getRestapiAdminClient(String apiProviderName, String apiName, String version) throws AxisFault {
        return new RESTAPIAdminClient(apiProviderName, apiName, version);
    }

    public boolean addApi(String apiProviderName, String apiName, String version, String apiConfig) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.addApi(apiConfig);
    }

    /**
     * Add the API to the gateway
     *
     * @param apiProviderName
     * @param apiName
     * @param version
     * @param apiConfig
     * @param tenantDomain
     * @throws AxisFault
     */
    public boolean addPrototypeApiScriptImplForTenant(String apiProviderName, String apiName, String version,
                                                   String apiConfig, String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.addPrototypeApiScriptImpl(apiConfig, tenantDomain);
    }

    public boolean addPrototypeApiScriptImpl(String apiProviderName, String apiName, String version, String apiConfig)
            throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.addPrototypeApiScriptImpl(apiConfig);
    }

    public boolean addDefaultAPIForTenant(String apiProviderName, String apiName, String version, String apiConfig,
                                       String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.addDefaultAPI(apiConfig, tenantDomain);
    }

    public boolean addDefaultAPI(String apiProviderName, String apiName, String version, String apiConfig)
            throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.addDefaultAPI(apiConfig);
    }

    /**
     * Get API from the gateway
     *
     * @param tenantDomain
     * @return
     * @throws AxisFault
     */
    public org.wso2.carbon.apimgt.gateway.dto.APIData getApiForTenant(String apiProviderName, String apiName,
                                                                      String version, String tenantDomain)
            throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        APIData apiData = restClient.getApi(tenantDomain);
        return convert(apiData);
    }

    public org.wso2.carbon.apimgt.gateway.dto.APIData getApi(String apiProviderName, String apiName, String version)
            throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        APIData apiData = restClient.getApi();
        return convert(apiData);
    }

    public org.wso2.carbon.apimgt.gateway.dto.APIData getDefaultApiForTenant(String apiProviderName, String apiName,
                                                                             String version, String tenantDomain)
            throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        APIData apiData = restClient.getDefaultApi(tenantDomain);
        return convert(apiData);
    }

    public org.wso2.carbon.apimgt.gateway.dto.APIData getDefaultApi(String apiProviderName, String apiName,
                                                                    String version) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        APIData apiData = restClient.getDefaultApi();
        return convert(apiData);
    }

    /**
     * Update the API in the Gateway
     *
     * @param apiProviderName
     * @param tenantDomain
     * @throws AxisFault
     */
    public boolean updateApiForTenant(String apiProviderName, String apiName, String version, String apiConfig,
                                   String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.updateApi(apiConfig, tenantDomain);
    }

    public boolean updateApi(String apiProviderName, String apiName, String version, String apiConfig) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.updateApi(apiConfig);
    }

    /**
     * Update the API in the Gateway
     *
     * @param apiProviderName
     * @param tenantDomain
     * @throws AxisFault
     */
    public boolean updateApiForInlineScriptForTenant(String apiProviderName, String apiName, String version,
                                                  String apiConfig, String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.updateApiForInlineScript(apiConfig, tenantDomain);
    }

    public boolean updateApiForInlineScript(String apiProviderName, String apiName, String version, String apiConfig)
            throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.updateApiForInlineScript(apiConfig);
    }

    public boolean updateDefaultApiForTenant(String apiProviderName, String apiName, String version, String apiConfig,
                                          String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.updateDefaultApi(apiConfig, tenantDomain);
    }

    public boolean updateDefaultApi(String apiProviderName, String apiName, String version, String apiConfig)
            throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.updateDefaultApi(apiConfig);
    }


    /**
     * Delete the API from Gateway
     *
     * @param tenantDomain
     * @throws AxisFault
     */
    public boolean deleteApiForTenant(String apiProviderName, String apiName, String version, String tenantDomain)
            throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        // Delete secure vault alias properties if exists
        try {
            deleteRegistryProperty(apiProviderName, apiName, version, tenantDomain);
        } catch (APIManagementException e) {
            String msg = "Failed to delete secure endpoint password alias " + e.getMessage();
            throw new AxisFault(msg, e);
        }
        return restClient.deleteApi(tenantDomain);
    }

    protected void deleteRegistryProperty(String apiProviderName, String apiName, String version, String tenantDomain) throws APIManagementException {
        GatewayUtils.deleteRegistryProperty(GatewayUtils.getAPIEndpointSecretAlias(apiProviderName, apiName,
                                                                                   version),
                                            APIConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION, tenantDomain);
    }

    public boolean deleteApi(String apiProviderName, String apiName, String version) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        // Delete secure vault alias properties if exists
        try {
            deleteRegistryProperty(apiProviderName, apiName, version, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } catch (APIManagementException e) {
            String msg = "Failed to delete secure endpoint password alias " + e.getMessage();
            throw new AxisFault(msg, e);
        }
        return restClient.deleteApi();
    }


    public boolean deleteDefaultApiForTenant(String apiProviderName, String apiName, String version, String tenantDomain)
            throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.deleteDefaultApi(tenantDomain);
    }

    public boolean deleteDefaultApi(String apiProviderName, String apiName, String version) throws AxisFault {
        RESTAPIAdminClient restClient = getRestapiAdminClient(apiProviderName, apiName, version);
        return restClient.deleteDefaultApi();
    }

    private org.wso2.carbon.apimgt.gateway.dto.APIData convert(
            org.wso2.carbon.rest.api.stub.types.carbon.APIData data) {
        if (data == null) {
            return null;
        }
        org.wso2.carbon.apimgt.gateway.dto.APIData apiData = new org.wso2.carbon.apimgt.gateway.dto.APIData();
        apiData.setContext(data.getContext());
        apiData.setFileName(data.getFileName());
        apiData.setHost(data.getHost());
        apiData.setName(data.getName());
        apiData.setPort(data.getPort());
        org.wso2.carbon.rest.api.stub.types.carbon.ResourceData[] resources = data.getResources();
        List<org.wso2.carbon.apimgt.gateway.dto.ResourceData> resList = new ArrayList<org.wso2.carbon.apimgt.gateway.dto.ResourceData>();
        if (resources != null && resources.length > 0) {
            for (org.wso2.carbon.rest.api.stub.types.carbon.ResourceData res : resources) {
                if (res == null) {
                    continue;
                }
                org.wso2.carbon.apimgt.gateway.dto.ResourceData resource = convert(res);
                resList.add(resource);
            }
            apiData.setResources(resList.toArray(new org.wso2.carbon.apimgt.gateway.dto.ResourceData[0]));
        }

        return apiData;
    }

    private org.wso2.carbon.apimgt.gateway.dto.ResourceData convert(
            org.wso2.carbon.rest.api.stub.types.carbon.ResourceData data) {
        org.wso2.carbon.apimgt.gateway.dto.ResourceData resource = new org.wso2.carbon.apimgt.gateway.dto.ResourceData();
        resource.setContentType(data.getContentType());
        resource.setFaultSequenceKey(data.getFaultSequenceKey());
        resource.setFaultSeqXml(data.getFaultSeqXml());
        resource.setInSequenceKey(data.getInSequenceKey());
        resource.setInSeqXml(data.getInSeqXml());
        resource.setMethods(data.getMethods());
        resource.setOutSequenceKey(data.getOutSequenceKey());
        resource.setOutSeqXml(data.getOutSeqXml());
        resource.setProtocol(data.getProtocol());
        resource.setUriTemplate(data.getUriTemplate());
        resource.setUrlMapping(data.getUrlMapping());
        resource.setUserAgent(data.getUserAgent());
        return resource;

    }


    /**
     * Deploy the sequence to the gateway
     *
     * @param sequence - The sequence element , which to be deployed in synapse
     * @throws AxisFault
     */
    public boolean addSequence(String sequence) throws AxisFault {
        SequenceAdminServiceClient client = getSequenceAdminServiceClient();
        if (sequence != null && !sequence.isEmpty()) {
            OMElement element = null;
            try {
                element = AXIOMUtil.stringToOM(sequence);
                client.addSequence(element);
                return true;
            } catch (XMLStreamException e) {
                log.error("Exception occurred while converting String to an OM.", e);
            }
        }
        return false;
    }

    protected SequenceAdminServiceClient getSequenceAdminServiceClient() throws AxisFault {
        return new SequenceAdminServiceClient();
    }

    /**
     * Deploy the sequence to the gateway
     *
     * @param sequence
     * @param tenantDomain
     * @throws AxisFault
     */
    public boolean addSequenceForTenant(String sequence, String tenantDomain) throws AxisFault {
        SequenceAdminServiceClient client = getSequenceAdminServiceClient();
        if (sequence != null && !sequence.isEmpty()) {
            OMElement element = null;
            try {
                element = AXIOMUtil.stringToOM(sequence);
                client.addSequenceForTenant(element, tenantDomain);
                return true;
            } catch (XMLStreamException e) {
                log.error("Exception occurred while converting String to an OM.", e);
            }
        }
        return false;
    }

    /**
     * Undeploy the sequence from gateway
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
     * @throws AxisFault
     */
    public boolean deleteSequence(String sequenceName) throws AxisFault {
        SequenceAdminServiceClient client = getSequenceAdminServiceClient();
        client.deleteSequence(sequenceName);
        return true;
    }

    public boolean deleteSequenceForTenant(String sequenceName, String tenantDomain) throws AxisFault {
        SequenceAdminServiceClient client = getSequenceAdminServiceClient();
        client.deleteSequenceForTenant(sequenceName, tenantDomain);
        return true;
    }

    /**
     * get the sequence from gateway
     *
     * @param sequenceName -The sequence name
     * @throws AxisFault
     */
    public OMElement getSequence(String sequenceName) throws AxisFault {
        SequenceAdminServiceClient client = getSequenceAdminServiceClient();
        return (OMElement) client.getSequence(sequenceName);
    }

    public OMElement getSequenceForTenant(String sequenceName, String tenantDomain) throws AxisFault {
        SequenceAdminServiceClient client = getSequenceAdminServiceClient();
        return (OMElement) client.getSequenceForTenant(sequenceName, tenantDomain);
    }

    public boolean isExistingSequence(String sequenceName) throws AxisFault {
        SequenceAdminServiceClient client = getSequenceAdminServiceClient();
        return client.isExistingSequence(sequenceName);
    }

    public boolean isExistingSequenceForTenant(String sequenceName, String tenantDomain) throws AxisFault {
        SequenceAdminServiceClient client = getSequenceAdminServiceClient();
        return client.isExistingSequenceForTenant(sequenceName, tenantDomain);
    }

    /**
     * encrypt the plain text password
     *
     * @param plainTextPass
     *            plain text password
     * @return encrypted password
     * @throws APIManagementException
     */
    public String doEncryption(String tenantDomain, String secureVaultAlias, String plainTextPass) throws AxisFault {
        MediationSecurityAdminServiceClient client = getMediationSecurityAdminServiceClient();
        String encodedValue;
        try {
            encodedValue = client.doEncryption(plainTextPass);
            setRegistryProperty(tenantDomain, secureVaultAlias, encodedValue);
        } catch (APIManagementException e) {
            String msg = "Failed to encrypt and store the secured endpoint password, " + e.getMessage();
            throw new AxisFault(msg, e);
        }
        return encodedValue;
    }

    protected void setRegistryProperty(String tenantDomain, String secureVaultAlias, String encodedValue) throws
            APIManagementException {
        GatewayUtils.setRegistryProperty(secureVaultAlias, encodedValue,
                APIConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION, tenantDomain);
    }

    protected MediationSecurityAdminServiceClient getMediationSecurityAdminServiceClient() throws AxisFault {
        return new MediationSecurityAdminServiceClient();
    }

    /**
     * policy is writtent in to files
     *
     * @param content  content to be written
     * @param fileName name of the file
     * @throws AxisFault
     */
    public boolean deployPolicy(String content, String fileName) throws AxisFault {
        File file = new File(APIConstants.POLICY_FILE_FOLDER);      //WSO2Carbon_Home/repository/deployment/server/throttle-config
        //if directory doesn't exist, make onee
        if (!file.exists()) {
            file.mkdir();
        }
        File writeFile = new File(APIConstants.POLICY_FILE_LOCATION + fileName + APIConstants.XML_EXTENSION);  //file folder+/
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(writeFile);
            //if file doesn't exit make one
            if (!writeFile.exists()) {
                writeFile.createNewFile();
            }
            byte[] contentInBytes = content.getBytes();
            fos.write(contentInBytes);
            fos.flush();
            return true;
        } catch (IOException e) {
            log.error("Error occurred writing to " + fileName + ":", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                log.error("Error occurred closing file output stream", e);
            }
        }
        return false;
    }

    /**
     * policy undeploy
     *
     * @param fileNames file names to be deleted
     */
    public boolean undeployPolicy(String[] fileNames) {

        for (int i = 0; i < fileNames.length; i++) {
            File file = new File(APIConstants.POLICY_FILE_LOCATION + fileNames[i] + APIConstants.XML_EXTENSION);
            boolean deleted = file.delete();
            if (deleted) {
                log.info("File : " + fileNames[i] + " is deleted");
            } else {
                log.error("Error occurred in deleting file: " + fileNames[i]);
            }
        }
        return true;
    }
}
