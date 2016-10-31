package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.MediationDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class PoliciesApiService {
    public abstract Response policiesMediationGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch);
    public abstract Response policiesMediationMediationPolicyIdDelete(String mediationPolicyId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response policiesMediationMediationPolicyIdGet(String mediationPolicyId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response policiesMediationMediationPolicyIdPut(String mediationPolicyId,MediationDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response policiesMediationPost(MediationDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
}

