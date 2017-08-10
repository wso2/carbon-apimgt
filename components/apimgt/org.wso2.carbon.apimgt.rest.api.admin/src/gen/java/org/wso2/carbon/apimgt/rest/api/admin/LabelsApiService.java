package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelDTO;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public abstract class LabelsApiService {
    public abstract Response labelsDelete(String labelId
 , Request request) throws NotFoundException;
    public abstract Response labelsGet(String labelId
 ,String accept
 , Request request) throws NotFoundException;
    public abstract Response labelsPost(LabelDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response labelsPut(LabelDTO body
 ,String contentType
 ,String labelId
 , Request request) throws NotFoundException;
}
