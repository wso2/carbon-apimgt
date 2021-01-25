package org.wso2.carbon.graphql.api.devportal.data.tagData;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.graphql.api.devportal.modules.Tag.TagDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TagData {



    public List<TagDTO> getAllTags() throws APIManagementException {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(null);

        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        Set<Tag> tagSet = apiConsumer.getAllTags(requestedTenantDomain);
        List<TagDTO> tagDTOList = new ArrayList<>();
        for(Tag tag: tagSet){
            String value = tag.getName();
            int count  = tag.getNoOfOccurrences();
            tagDTOList.add(new TagDTO(value,count));

        }

        return tagDTOList;
    }
}
