package org.wso2.carbon.throttle.service;

import org.wso2.carbon.throttle.service.*;
import org.wso2.carbon.throttle.service.dto.*;

import org.wso2.carbon.throttle.service.dto.ErrorDTO;
import org.wso2.carbon.throttle.service.dto.KeyManagerDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class KeymanagersApiService {
    public abstract Response keymanagersGet();
}

