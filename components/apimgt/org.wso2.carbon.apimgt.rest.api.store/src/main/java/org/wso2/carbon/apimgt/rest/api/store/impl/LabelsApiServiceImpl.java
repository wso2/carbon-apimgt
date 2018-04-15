package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.LabelMappingUtil;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;


public class LabelsApiServiceImpl extends LabelsApiService {

    private static final Logger log = LoggerFactory.getLogger(LabelsApiServiceImpl.class);

    /**
     * Get all the labels.
     *
     * @param labelType       the type of the labels to be fetched
     * @param ifNoneMatch     If-None-Match header value
     * @param request         ms4j request object
     * @return Lable List     List of labels in type (gateway,store)
     * @throws NotFoundException If failed to get the label values
     */
    @Override
    public Response labelsGet(String labelType, String ifNoneMatch, String ifModifiedSince,
                              Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            List<Label> labels;
            if (labelType == null) {
                labels = apiStore.getAllLabels();
            } else{
                labels = apiStore.getLabelsByType(labelType);
            }
            LabelListDTO labelListDTO = LabelMappingUtil.toLabelListDTO(labels);
            return Response.ok().entity(labelListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Labels";
            HashMap<String, String> paramList = new HashMap<String, String>();
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

    }
}
