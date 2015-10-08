package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.dto.*;

import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class EnvironmentsApiService {
    public abstract Response environmentsGet(String apiId);
}

