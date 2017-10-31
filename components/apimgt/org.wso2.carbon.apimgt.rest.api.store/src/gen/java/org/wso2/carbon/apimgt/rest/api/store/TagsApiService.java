package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

<<<<<<< HEAD
=======
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

>>>>>>> upstream/master
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TagListDTO;

import java.util.List;
<<<<<<< HEAD

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
=======
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;
>>>>>>> upstream/master

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class TagsApiService {
    public abstract Response tagsGet(Integer limit
 ,Integer offset
 ,String ifNoneMatch
  ,Request request) throws NotFoundException;
}
