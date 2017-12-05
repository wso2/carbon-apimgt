package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ThreatProtectionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThreatProtectionPolicyListDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class ThreatProtectionApiService {
    public abstract Response threatProtectionPoliciesGet( Request request) throws NotFoundException;
    public abstract Response threatProtectionPolicyPost(ThreatProtectionPolicyDTO threatProtectionPolicy
  ,Request request) throws NotFoundException;
    public abstract Response threatProtectionPolicyThreatProtectionPolicyIdDelete(String threatProtectionPolicyId
  ,Request request) throws NotFoundException;
    public abstract Response threatProtectionPolicyThreatProtectionPolicyIdGet(String threatProtectionPolicyId
  ,Request request) throws NotFoundException;
    public abstract Response threatProtectionPolicyThreatProtectionPolicyIdPost(String threatProtectionPolicyId
 ,ThreatProtectionPolicyDTO threatProtectionPolicy
  ,Request request) throws NotFoundException;
}
