package org.wso2.carbon.graphql.api.devportal.mapping;

import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelNameDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LabelMapping {


    public List<LabelDTO> fromLabeltoLabelDTO(List<Label> labels, String name){


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
    public List<LabelNameDTO> fromLabelNametoLabelNameDTO(DevPortalAPI devPortalAPI){
        Set<String> labels = devPortalAPI.getGatewayLabels();
        List<LabelNameDTO> labelNameDTOList = new ArrayList<>();
        for (String labelName : labels) {
            labelNameDTOList.add(new LabelNameDTO(devPortalAPI.getId(),labelName));
        }

        return labelNameDTOList;

    }

}
