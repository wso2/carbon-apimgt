package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public abstract class LabelsApiService {
    public abstract Response labelsGet(String labelType
 , String accept
 , String ifNoneMatch
 , String ifModifiedSince
 , Request request) throws NotFoundException;
}
