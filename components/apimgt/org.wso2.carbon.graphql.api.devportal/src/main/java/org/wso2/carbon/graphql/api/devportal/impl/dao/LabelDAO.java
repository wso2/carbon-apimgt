package org.wso2.carbon.graphql.api.devportal.impl.dao;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelDTO;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelNameDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class LabelDAO {

    public List<LabelDTO> getLabeldata(String name) throws APIManagementException {


        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        List<Label> labels = apiConsumer.getLabelDataFromDAO();


        List<LabelDTO> labelData = new ArrayList<LabelDTO>();

        for (int i = 0; i<labels.size();i++) {
            if(name.equals(labels.get(i).getName())){
                String description = labels.get(i).getDescription();
                List<String> accessUrlsList = labels.get(i).getAccessUrls();
                String accessUrls = accessUrlsList.toString();

                labelData.add(new LabelDTO(name,description,accessUrls));
            }


        }
        return labelData;
    }

    public List<LabelNameDTO> getLabelNames(DevPortalAPI devPortalAPI) throws APIPersistenceException {
        Set<String> labels = devPortalAPI.getGatewayLabels();


        List<LabelNameDTO> labelNameDTOList = new ArrayList<>();
        for (String labelName : labels) {
            labelNameDTOList.add(new LabelNameDTO(devPortalAPI.getId(),labelName));
        }

        return labelNameDTOList;
    }

}
