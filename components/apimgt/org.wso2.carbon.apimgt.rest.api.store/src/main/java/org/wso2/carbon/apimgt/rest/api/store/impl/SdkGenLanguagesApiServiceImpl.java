package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.configuration.models.SdkLanguageConfigurations;
import org.wso2.carbon.apimgt.rest.api.store.SdkGenLanguagesApiService;


import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.util.Set;

import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;

public class SdkGenLanguagesApiServiceImpl extends SdkGenLanguagesApiService {
    @Override
    public Response sdkGenLanguagesGet(Request request) throws NotFoundException {
        SdkLanguageConfigurations sdkLanguageConfigurations = APIMConfigurationService
                .getInstance()
                .getApimConfigurations()
                .getSdkLanguageConfigurations();
        Set<String> languageList = sdkLanguageConfigurations.getSdkGenLanguages().keySet();
        return Response.ok().entity(languageList).build();
    }
}
