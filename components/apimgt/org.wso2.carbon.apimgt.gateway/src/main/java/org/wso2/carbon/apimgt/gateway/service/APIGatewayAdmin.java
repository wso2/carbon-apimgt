package org.wso2.carbon.apimgt.gateway.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.utils.MediationSecurityAdminServiceClient;
import org.wso2.carbon.apimgt.gateway.utils.RESTAPIAdminClient;
import org.wso2.carbon.apimgt.gateway.utils.SequenceAdminServiceClient;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;

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
    public void addApiForTenant(String apiProviderName, String apiName, String version, String apiConfig,
                                String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.addApi(apiConfig, tenantDomain);
    }

    public void addApi(String apiProviderName, String apiName, String version, String apiConfig) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.addApi(apiConfig);
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
    public void addPrototypeApiScriptImplForTenant(String apiProviderName, String apiName, String version,
                                                   String apiConfig, String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.addPrototypeApiScriptImpl(apiConfig, tenantDomain);
    }

    public void addPrototypeApiScriptImpl(String apiProviderName, String apiName, String version, String apiConfig)
            throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.addPrototypeApiScriptImpl(apiConfig);
    }

    public void addDefaultAPIForTenant(String apiProviderName, String apiName, String version, String apiConfig,
                                       String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.addDefaultAPI(apiConfig, tenantDomain);
    }

    public void addDefaultAPI(String apiProviderName, String apiName, String version, String apiConfig)
            throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.addDefaultAPI(apiConfig);
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
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        APIData apiData = restClient.getApi(tenantDomain);
        return convert(apiData);
    }

    public org.wso2.carbon.apimgt.gateway.dto.APIData getApi(String apiProviderName, String apiName, String version)
            throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        APIData apiData = restClient.getApi();
        return convert(apiData);
    }

    public org.wso2.carbon.apimgt.gateway.dto.APIData getDefaultApiForTenant(String apiProviderName, String apiName,
                                                                             String version, String tenantDomain)
            throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        APIData apiData = restClient.getDefaultApi(tenantDomain);
        return convert(apiData);
    }

    public org.wso2.carbon.apimgt.gateway.dto.APIData getDefaultApi(String apiProviderName, String apiName,
                                                                    String version) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
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
    public void updateApiForTenant(String apiProviderName, String apiName, String version, String apiConfig,
                                   String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.updateApi(apiConfig, tenantDomain);
    }

    public void updateApi(String apiProviderName, String apiName, String version, String apiConfig) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.updateApi(apiConfig);
    }

    /**
     * Update the API in the Gateway
     *
     * @param apiProviderName
     * @param tenantDomain
     * @throws AxisFault
     */
    public void updateApiForInlineScriptForTenant(String apiProviderName, String apiName, String version,
                                                  String apiConfig, String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.updateApiForInlineScript(apiConfig, tenantDomain);
    }

    public void updateApiForInlineScript(String apiProviderName, String apiName, String version, String apiConfig)
            throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.updateApiForInlineScript(apiConfig);
    }

    public void updateDefaultApiForTenant(String apiProviderName, String apiName, String version, String apiConfig,
                                          String tenantDomain) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.updateDefaultApi(apiConfig, tenantDomain);
    }

    public void updateDefaultApi(String apiProviderName, String apiName, String version, String apiConfig)
            throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.updateDefaultApi(apiConfig);
    }


    /**
     * Delete the API from Gateway
     *
     * @param tenantDomain
     * @throws AxisFault
     */
    public void deleteApiForTenant(String apiProviderName, String apiName, String version, String tenantDomain)
            throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.deleteApi(tenantDomain);
    }

    public void deleteApi(String apiProviderName, String apiName, String version) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.deleteApi();
    }


    public void deleteDefaultApiForTenant(String apiProviderName, String apiName, String version, String tenantDomain)
            throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.deleteDefaultApi(tenantDomain);
    }

    public void deleteDefaultApi(String apiProviderName, String apiName, String version) throws AxisFault {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(apiProviderName, apiName, version);
        restClient.deleteDefaultApi();
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
    public void addSequence(String sequence) throws AxisFault {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        if (sequence != null && !sequence.isEmpty()) {
            OMElement element = null;
            try {
                element = AXIOMUtil.stringToOM(sequence);
                client.addSequence(element);
            } catch (XMLStreamException e) {
                log.error("Exception occurred while converting String to an OM.");
            }
        }
    }

    /**
     * Deploy the sequence to the gateway
     * @param sequence
     * @param tenantDomain
     * @throws AxisFault
     */
    public void addSequenceForTenant(String sequence, String tenantDomain) throws AxisFault {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        if (sequence != null && !sequence.isEmpty()) {
            OMElement element = null;
            try {
                element = AXIOMUtil.stringToOM(sequence);
                client.addSequenceForTenant(element, tenantDomain);
            } catch (XMLStreamException e) {
                log.error("Exception occurred while converting String to an OM.");
            }
        }
    }

    /**
     * Undeploy the sequence from gateway
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
     * @param tenantDomain
     * @throws AxisFault
     */
    public void deleteSequence(String sequenceName) throws AxisFault {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        client.deleteSequence(sequenceName);
    }

    public void deleteSequenceForTenant(String sequenceName, String tenantDomain) throws AxisFault {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        client.deleteSequenceForTenant(sequenceName, tenantDomain);
    }

    /**
     * get the sequence from gateway
     *
     * @param sequenceName -The sequence name,
     * @param tenantDomain
     * @throws AxisFault
     */
    public OMElement getSequence(String sequenceName) throws AxisFault {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        return (OMElement) client.getSequence(sequenceName);
    }

    public OMElement getSequenceForTenant(String sequenceName, String tenantDomain) throws AxisFault {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        return (OMElement) client.getSequenceForTenant(sequenceName, tenantDomain);
    }

    public boolean isExistingSequence(String sequenceName) throws AxisFault {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        return client.isExistingSequence(sequenceName);
    }

    public boolean isExistingSequenceForTenant(String sequenceName, String tenantDomain) throws AxisFault {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        return client.isExistingSequenceForTenant(sequenceName, tenantDomain);
    }

    /**
     * encrypt the plain text password
     *
     * @param cipher        init cipher
     * @param plainTextPass plain text password
     * @return encrypted password
     * @throws APIManagementException
     */
    public String doEncryption(String plainTextPass) throws AxisFault {
        MediationSecurityAdminServiceClient client = new MediationSecurityAdminServiceClient();
        String encodedValue = null;
        try {
            encodedValue = client.doEncryption(plainTextPass);
        } catch (Exception e) {
            String msg = "Failed to encrypt the secured endpoint password, " + e.getMessage();
            throw new AxisFault(msg, e);
        }
        return encodedValue;
    }


}
