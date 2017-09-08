package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.core.impl.ApiStoreSdkGenerationManager;
import org.wso2.carbon.apimgt.rest.api.store.SdkGenLanguagesApiService;


import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.util.Set;

import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;

public class SdkGenLanguagesApiServiceImpl extends SdkGenLanguagesApiService {
    @Override
    public Response sdkGenLanguagesGet(Request request) throws NotFoundException {
        ApiStoreSdkGenerationManager sdkGenerationManager = new ApiStoreSdkGenerationManager();
        Set<String> languageList = sdkGenerationManager.getSdkGenLanguages().keySet();
        return Response.ok().entity(languageList).build();
    }
}
