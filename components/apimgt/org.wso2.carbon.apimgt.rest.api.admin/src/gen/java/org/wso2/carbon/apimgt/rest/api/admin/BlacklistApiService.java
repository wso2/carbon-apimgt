package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class BlacklistApiService {
    public abstract Response blacklistConditionIdDelete(String conditionId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response blacklistConditionIdGet(String conditionId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response blacklistConditionIdPut(String conditionId
 ,BlockingConditionDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response blacklistGet(String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response blacklistPost(BlockingConditionDTO body
 , Request request) throws NotFoundException;
}
