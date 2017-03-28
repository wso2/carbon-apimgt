package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.TagsApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.TagListDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.TagMappingUtil;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:48:55.078+05:30")
public class TagsApiServiceImpl extends TagsApiService {

    private static final Logger log = LoggerFactory.getLogger(TagsApiServiceImpl.class);

    /**
     * Retrieve tags of APIs
     * 
     * @param limit Maximum number of tags to return
     * @param offset Starting position of the pagination
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param minorVersion minor version header
     * @return A list of qualifying tags as the response
     * @throws NotFoundException
     */
    @Override
    public Response tagsGet(Integer limit, Integer offset, String accept, String ifNoneMatch,
                            String minorVersion) throws NotFoundException {
        TagListDTO tagListDTO = null;
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            List<Tag> tagList = apiStore.getAllTags();
            tagListDTO = TagMappingUtil.fromTagListToDTO(tagList, limit, offset);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving tags";
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().entity(tagListDTO).build();
    }
}
