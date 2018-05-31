package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class LabelsApiService {
    public abstract Response labelsGet();
    public abstract Response labelsLabelIdDelete(String labelId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response labelsLabelIdPut(String labelId,LabelDTO body);
    public abstract Response labelsPost(LabelDTO body);
}

