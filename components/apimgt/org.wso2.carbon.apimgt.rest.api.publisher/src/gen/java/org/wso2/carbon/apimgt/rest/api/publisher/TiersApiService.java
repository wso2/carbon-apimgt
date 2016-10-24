package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierList;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Error;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Tier;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T13:00:17.095+05:30")
public abstract class TiersApiService {
    public abstract Response tiersTierLevelGet(String tierLevel ,Integer limit ,Integer offset ,String xWSO2Tenant ,String accept ,String ifNoneMatch ) throws NotFoundException;
    public abstract Response tiersTierLevelTierNameGet(String tierName ,String tierLevel ,String xWSO2Tenant ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
}
