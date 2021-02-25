package org.wso2.carbon.graphql.api.devportal.service;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.mapping.LabelMapping;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelDTO;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelNameDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class LabelService {

    public List<LabelDTO> getLabelDetails(String name) throws APIManagementException {


        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        List<Label> labels = apiConsumer.getLabelDataFromDAO();
        LabelMapping labelMapping = new LabelMapping();
        List<LabelDTO> labelData = labelMapping.fromLabeltoLabelDTO(labels,name);
        return labelData;
    }

    public List<LabelNameDTO> getLabelNames(DevPortalAPI devPortalAPI) throws APIPersistenceException {
        LabelMapping labelMapping = new LabelMapping();
        List<LabelNameDTO> labelNameDTOList = labelMapping.fromLabelNametoLabelNameDTO(devPortalAPI);
        return labelNameDTOList;
    }

}
