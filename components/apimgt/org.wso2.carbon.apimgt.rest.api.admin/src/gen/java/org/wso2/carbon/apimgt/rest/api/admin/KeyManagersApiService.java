package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.KeyManagerDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class KeyManagersApiService {
    public abstract Response keyManagersGet();
    public abstract Response keyManagersKeyManagerIdDelete(String keyManagerId);
    public abstract Response keyManagersKeyManagerIdGet(String keyManagerId);
    public abstract Response keyManagersKeyManagerIdPut(String keyManagerId,KeyManagerDTO body);
    public abstract Response keyManagersPost(KeyManagerDTO body) throws APIManagementException;
}

