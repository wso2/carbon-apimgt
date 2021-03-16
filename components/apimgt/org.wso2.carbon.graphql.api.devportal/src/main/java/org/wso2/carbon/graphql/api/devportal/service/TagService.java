package org.wso2.carbon.graphql.api.devportal.service;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.graphql.api.devportal.mapping.TagMapping;
import org.wso2.carbon.graphql.api.devportal.modules.tag.TagDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TagService {


    private static final String ANONYMOUS_USER = "__wso2.am.anon__";

    public List<TagDTO> getAllTags() throws APIManagementException {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        //String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(ANONYMOUS_USER);
        Set<Tag> tagSet = apiConsumer.getAllTags(requestedTenantDomain);
        TagMapping tagMapping = new TagMapping();
        List<TagDTO> tagDTOList = tagMapping.fromTagToTagDTO(tagSet);
        return tagDTOList;
    }
}
