package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelListDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class LabelsApiService {
    public abstract Response labelsGet(String labelId
 ,String accept
 , Request request) throws NotFoundException;
    public abstract Response labelsLabelIdDelete(String labelId
 , Request request) throws NotFoundException;
    public abstract Response labelsLabelIdPut(String labelId
 ,LabelDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response labelsPost(LabelDTO body
 ,String contentType
 , Request request) throws NotFoundException;
}
