package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TagListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class TagsApiService {
    public abstract Response tagsGet(Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch);

    public abstract String tagsGetGetLastUpdatedTime(Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch);
}

