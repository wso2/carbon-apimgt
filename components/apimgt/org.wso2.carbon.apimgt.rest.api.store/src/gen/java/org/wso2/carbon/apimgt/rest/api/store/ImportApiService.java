package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ImportApiService {
    public abstract Response importApplicationsPost(InputStream fileInputStream,Attachment fileDetail,Boolean preserveOwner,Boolean addSubscriptions);

    public abstract String importApplicationsPostGetLastUpdatedTime(InputStream fileInputStream,Attachment fileDetail,Boolean preserveOwner,Boolean addSubscriptions);
}

