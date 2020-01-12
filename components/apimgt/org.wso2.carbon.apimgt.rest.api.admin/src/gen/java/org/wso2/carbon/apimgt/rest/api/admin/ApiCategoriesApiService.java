package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ApiCategoriesApiService {
    public abstract Response apiCategoriesApiCategoryIdDelete(String apiCategoryId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apiCategoriesApiCategoryIdPut(String apiCategoryId,APICategoryDTO body);
    public abstract Response apiCategoriesGet();
    public abstract Response apiCategoriesPost(APICategoryDTO body);
}

