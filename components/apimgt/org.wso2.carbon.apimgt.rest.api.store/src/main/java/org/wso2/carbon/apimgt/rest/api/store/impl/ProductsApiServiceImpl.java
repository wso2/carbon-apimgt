

/*
 *
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIProductListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class ProductsApiServiceImpl extends ProductsApiService {
    @Override
    public Response productsGet(Integer limit,Integer offset,String xWSO2Tenant,String query,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
