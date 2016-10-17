package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.*;

import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.EnvironmentList;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public abstract class EnvironmentsApiService {
      public abstract Response environmentsGet(String apiId);
}
