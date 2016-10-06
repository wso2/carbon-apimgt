/*
 *
 *  * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.factories.ProductsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.dto.APIProductListDTO;


import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/products")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/products", description = "the products API")
public class ProductsApi  {

   private final ProductsApiService delegate = ProductsApiServiceFactory.getProductsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve/Search API Products\n", notes = "This operation provides you a list of available API Products qualifying under a given search condition.\n\nEach retrieved API Product is represented with a minimal amount of attributes. If you want to get complete details of an API, you need to use **Get details of an API** operation.\n\nThis operation supports retriving API Productss of other tenants. The required tenant domain need to be specified as a header `X-WSO2-Tenant`. If not specified super tenant's API Productss will be retrieved. If you used an Authorization header, the user's tenant associated with the access token will be used.\n\n**NOTE:**\n* By default, this operation retrieves Published API Productss. In order to retrieve Prototyped API Productss, you need to use **query** parameter and specify **status:PROTOTYPED**.\n* This operation does not require an Authorization header by default. But if it is provided, it will be validated and checked for permissions of the user, hence you may be able to see APIs which are restricted for special permissions/roles.\n", response = APIProductListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of qualifying API Productss is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response productsGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "**Search condition**.\n\nYou can search in attributes by using an **\"<attribute>:\"** modifier.\n\nEg.\n\"provider:wso2\" will match an API Product if the provider of the API Product is exactly \"wso2\".\n\nAdditionally you can use wildcards.\n\nEg.\n\"provider:wso2*\" will match an API Product if the provider of the API Product starts with \"wso2\".\n\nSupported attribute modifiers are [**version, context, status,\ndescription, subcontext, doc, provider, tag**]\n\nIf no advanced attribute modifier has been specified, search will match the\ngiven query string against API Product Name.\n") @QueryParam("query") String query,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.productsGet(limit,offset,xWSO2Tenant,query,accept,ifNoneMatch);
    }
}

