package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class EnvironmentsApiService {
    public abstract Response environmentsGet(String apiId);
}

