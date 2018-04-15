package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public abstract class LabelsApiService {
    public abstract Response labelsGet(String ifNoneMatch
 ,String ifModifiedSince
 ,String labelType
  ,Request request) throws NotFoundException;
}
