package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.dto.LabelInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.store.LabelInfoApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.mappings.LabelMappingUtil;

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-09T12:36:56.084+05:30")
public class LabelInfoApiServiceImpl extends LabelInfoApiService {

    private static final Logger log = LoggerFactory.getLogger(LabelInfoApiServiceImpl.class);

    @Override
    public Response labelInfoGet(LabelInfoListDTO body
, String contentType
, String accept
, String ifNoneMatch
, String ifModifiedSince
, String minorVersion
 ) throws NotFoundException {

        String username = RestApiUtil.getLoggedInUsername();
        LabelListDTO labelListDTO;
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            List<Label> labelList = apiStore.getLabelInfo(LabelMappingUtil.fromLabelInfoListDTO(body));
            labelListDTO = LabelMappingUtil.toLabelListDTO(labelList);

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving label information";
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().entity(labelListDTO).build();
    }
}
