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

public abstract class ThreatProtectionPoliciesApiService {
    public abstract Response threatProtectionPoliciesGet( Request request) throws NotFoundException;
    public abstract Response threatProtectionPoliciesPost(ThreatProtectionPolicyDTO threatProtectionPolicy
  ,Request request) throws NotFoundException;
    public abstract Response threatProtectionPoliciesThreatProtectionPolicyIdDelete(String threatProtectionPolicyId
  ,Request request) throws NotFoundException;
    public abstract Response threatProtectionPoliciesThreatProtectionPolicyIdGet(String threatProtectionPolicyId
  ,Request request) throws NotFoundException;
    public abstract Response threatProtectionPoliciesThreatProtectionPolicyIdPost(String threatProtectionPolicyId
 ,ThreatProtectionPolicyDTO threatProtectionPolicy
  ,Request request) throws NotFoundException;
}
