package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APIInfoListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ImportApiService {
    public abstract Response importApiPost(InputStream fileInputStream,Attachment fileDetail,Boolean preserveProvider,Boolean overwrite);
    public abstract Response importApplicationsPost(InputStream fileInputStream,Attachment fileDetail,Boolean preserveOwner,Boolean skipSubscriptions,String appOwner,Boolean skipApplicationKeys,Boolean update);
}

