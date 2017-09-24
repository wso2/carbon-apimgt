package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.core.impl.ApiStoreSdkGenerationManager;
import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;
import java.util.Set;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
/**
 * Implementation of SDK generation language list retrieval resource
 */
public class SdkGenApiServiceImpl extends SdkGenApiService {

    /**
     * Retrieve a list of languages that support SDK generation.
     *
     * @param request     msf4j request object
     * @return A list of supported languages
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response sdkGenLanguagesGet( Request request) throws NotFoundException {
        ApiStoreSdkGenerationManager sdkGenerationManager = new ApiStoreSdkGenerationManager();
        Set<String> languageList = sdkGenerationManager.getSdkGenLanguages().keySet();
        return Response.ok().entity(languageList).build();
    }
}
