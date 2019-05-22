package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;


public class SearchApiServiceImpl implements SearchApiService {
    public Response searchGet(Integer limit, Integer offset, String query, String ifNoneMatch,
            MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }
}
