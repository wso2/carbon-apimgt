package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.PublishStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.MonetizationUsagePublishInfoDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class MonetizationApiService {
    public abstract Response monetizationPublishUsagePost();
    public abstract Response monetizationPublishUsageStatusGet();
}

