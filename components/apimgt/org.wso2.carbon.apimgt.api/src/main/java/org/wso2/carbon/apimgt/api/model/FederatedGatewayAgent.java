package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.FileInputStream;

public interface FederatedGatewayAgent {

    /**
     * Deploy API to the federated gateway
     *
     * @param apiArchive    API archive file
     * @throws APIManagementException
     */
    void deployAPI(FileInputStream apiArchive) throws APIManagementException;

    /**
     * Undeploy API from the federated gateway
     *
     * @param apiId
     * @throws APIManagementException
     */
    void undeployAPI(String apiId) throws APIManagementException;
}
