package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.util.impl.ExportApi;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.core.Response;


public class ExportApiServiceImpl implements ExportApiService {
    /**
     * Exports an API from API Manager for a given API ID. Meta information, API icon, documentation, WSDL
     * and sequences are exported. This service generates a zipped archive which contains all the above mentioned
     * resources for a given API.
     *
     * @param name           Name of the API that needs to be exported
     * @param version        Version of the API that needs to be exported
     * @param providerName   Provider name of the API that needs to be exported
     * @param format         Format of output documents. Can be YAML or JSON
     * @param preserveStatus Preserve API status on export
     * @return Zipped file containing exported API
     */

    public Response exportApiGet(String name, String version, String providerName, String format, Boolean preserveStatus, MessageContext messageContext) throws APIManagementException {
        ExportApi exportApi = new ExportApi();
        return exportApi.exportApiGet(name, version, providerName, format, preserveStatus);
    }
}
