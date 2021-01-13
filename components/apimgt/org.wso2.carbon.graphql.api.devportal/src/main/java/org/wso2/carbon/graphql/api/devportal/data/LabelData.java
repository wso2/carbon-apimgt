package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.LabelDTO;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil.getLabelsFromAPIGovernanceArtifact;

public class LabelData {

    public List<LabelDTO> getLabeldata(String Id) throws RegistryException, APIManagementException, APIPersistenceException, UserStoreException {

        ArtifactData artifactData = new ArtifactData();
        GenericArtifact apiArtifact = artifactData.getDevportalApis(Id);
        String providerName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        List<Label> labels = getLabelsFromAPIGovernanceArtifact(apiArtifact,providerName);

        List<LabelDTO> labelData = new ArrayList<LabelDTO>();

        for (int i = 0; i<labels.size();i++){
            String name = labels.get(i).getName();
            String description = labels.get(i).getDescription();
            List<String> accessUrlsList = labels.get(i).getAccessUrls();
            String accessUrls = accessUrlsList.toString();

            labelData.add(new LabelDTO(name,description,accessUrls));

        }
        return labelData;
    }
}
