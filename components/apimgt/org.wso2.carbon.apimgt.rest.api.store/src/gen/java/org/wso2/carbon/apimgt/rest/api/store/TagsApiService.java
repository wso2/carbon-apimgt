package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import org.wso2.carbon.apimgt.rest.api.store.dto.TagList;
import org.wso2.carbon.apimgt.rest.api.store.dto.Error;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T10:59:23.111+05:30")
public abstract class TagsApiService {
    public abstract Response tagsGet(Integer limit ,Integer offset ,String xWSO2Tenant ,String accept ,String ifNoneMatch ) throws NotFoundException;
}
