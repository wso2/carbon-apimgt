package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import java.io.File;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class ImportApiService {
    public abstract Response importApplicationsPost(InputStream fileInputStream, FileInfo fileDetail
  ,Request request) throws NotFoundException;
    public abstract Response importApplicationsPut(InputStream fileInputStream, FileInfo fileDetail
  ,Request request) throws NotFoundException;
}
