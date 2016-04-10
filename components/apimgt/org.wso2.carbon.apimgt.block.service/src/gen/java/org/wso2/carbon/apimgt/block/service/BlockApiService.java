package org.wso2.carbon.apimgt.block.service;

import org.wso2.carbon.apimgt.block.service.*;
import org.wso2.carbon.apimgt.block.service.dto.*;

import org.wso2.carbon.apimgt.block.service.dto.BlockConditionsDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class BlockApiService {
    public abstract Response blockGet();
}

