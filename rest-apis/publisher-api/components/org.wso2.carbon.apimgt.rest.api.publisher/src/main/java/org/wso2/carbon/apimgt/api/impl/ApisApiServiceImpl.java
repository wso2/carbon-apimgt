package org.wso2.carbon.apimgt.api.impl;

import org.wso2.carbon.apimgt.api.*;

import org.wso2.carbon.apimgt.model.APIList;
import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.API;
import org.wso2.carbon.apimgt.model.DocumentList;
import org.wso2.carbon.apimgt.model.Document;
import java.io.File;
import org.wso2.carbon.apimgt.model.FileInfo;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class ApisApiServiceImpl extends ApisApiService {
      @Override
      public Response apisGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch){
      // do some magic!
      return Response.ok().entity("{\n" +
              "   \"id\": \"7a2298c4-c905-403f-8fac-38c73301631f\",\n" +
              "   \"name\": \"PizzaShackAPI\",\n" +
              "   \"description\": \"This document describe a RESTFul API for Pizza Shack online pizza delivery store.\\r\\n\",\n" +
              "   \"context\": \"/pizzashack\",\n" +
              "   \"version\": \"1.0.0\",\n" +
              "   \"provider\": \"admin\",\n" +
              "   \"apiDefinition\": \"{\\\"paths\\\":{\\\"/order\\\":{\\\"post\\\":{\\\"x-auth-type\\\":\\\"Application & Application User\\\",\\\"x-throttling-tier\\\":\\\"Unlimited\\\",\\\"description\\\":\\\"Create a new Order\\\",\\\"parameters\\\":[{\\\"schema\\\":{\\\"$ref\\\":\\\"#/definitions/Order\\\"},\\\"description\\\":\\\"Order object that needs to be added\\\",\\\"name\\\":\\\"body\\\",\\\"required\\\":true,\\\"in\\\":\\\"body\\\"}],\\\"responses\\\":{\\\"201\\\":{\\\"headers\\\":{\\\"Location\\\":{\\\"description\\\":\\\"The URL of the newly created resource.\\\",\\\"type\\\":\\\"string\\\"}},\\\"schema\\\":{\\\"$ref\\\":\\\"#/definitions/Order\\\"},\\\"description\\\":\\\"Created.\\\"}}}},\\\"/menu\\\":{\\\"get\\\":{\\\"x-auth-type\\\":\\\"Application & Application User\\\",\\\"x-throttling-tier\\\":\\\"Unlimited\\\",\\\"description\\\":\\\"Return a list of available menu items\\\",\\\"parameters\\\":[],\\\"responses\\\":{\\\"200\\\":{\\\"headers\\\":{},\\\"schema\\\":{\\\"title\\\":\\\"Menu\\\",\\\"properties\\\":{\\\"list\\\":{\\\"items\\\":{\\\"$ref\\\":\\\"#/definitions/MenuItem\\\"},\\\"type\\\":\\\"array\\\"}},\\\"type\\\":\\\"object\\\"},\\\"description\\\":\\\"OK.\\\"}}}}},\\\"schemes\\\":[\\\"https\\\"],\\\"produces\\\":[\\\"application/json\\\"],\\\"swagger\\\":\\\"2.0\\\",\\\"definitions\\\":{\\\"MenuItem\\\":{\\\"title\\\":\\\"Pizza menu Item\\\",\\\"properties\\\":{\\\"price\\\":{\\\"type\\\":\\\"string\\\"},\\\"description\\\":{\\\"type\\\":\\\"string\\\"},\\\"name\\\":{\\\"type\\\":\\\"string\\\"},\\\"image\\\":{\\\"type\\\":\\\"string\\\"}},\\\"required\\\":[\\\"name\\\"]},\\\"Order\\\":{\\\"title\\\":\\\"Pizza Order\\\",\\\"properties\\\":{\\\"customerName\\\":{\\\"type\\\":\\\"string\\\"},\\\"delivered\\\":{\\\"type\\\":\\\"boolean\\\"},\\\"address\\\":{\\\"type\\\":\\\"string\\\"},\\\"pizzaType\\\":{\\\"type\\\":\\\"string\\\"},\\\"creditCardNumber\\\":{\\\"type\\\":\\\"string\\\"},\\\"quantity\\\":{\\\"type\\\":\\\"number\\\"},\\\"orderId\\\":{\\\"type\\\":\\\"integer\\\"}},\\\"required\\\":[\\\"orderId\\\"]}},\\\"consumes\\\":[\\\"application/json\\\"],\\\"info\\\":{\\\"title\\\":\\\"PizzaShackAPI\\\",\\\"description\\\":\\\"This document describe a RESTFul API for Pizza Shack online pizza delivery store.\\\\n\\\",\\\"license\\\":{\\\"name\\\":\\\"Apache 2.0\\\",\\\"url\\\":\\\"http://www.apache.org/licenses/LICENSE-2.0.html\\\"},\\\"contact\\\":{\\\"email\\\":\\\"architecture@pizzashack.com\\\",\\\"name\\\":\\\"John Doe\\\",\\\"url\\\":\\\"http://www.pizzashack.com\\\"},\\\"version\\\":\\\"1.0.0\\\"}}\",\n" +
              "   \"wsdlUri\": null,\n" +
              "   \"status\": \"CREATED\",\n" +
              "   \"responseCaching\": \"Disabled\",\n" +
              "   \"cacheTimeout\": 300,\n" +
              "   \"destinationStatsEnabled\": null,\n" +
              "   \"isDefaultVersion\": false,\n" +
              "   \"transport\":    [\n" +
              "      \"http\",\n" +
              "      \"https\"\n" +
              "   ],\n" +
              "   \"tags\": [\"pizza\"],\n" +
              "   \"tiers\": [\"Unlimited\"],\n" +
              "   \"maxTps\":    {\n" +
              "      \"sandbox\": 5000,\n" +
              "      \"production\": 1000\n" +
              "   },\n" +
              "   \"thumbnailUri\": null,\n" +
              "   \"visibility\": \"PUBLIC\",\n" +
              "   \"visibleRoles\": [],\n" +
              "   \"visibleTenants\": [],\n" +
              "   \"endpointConfig\": \"{\\\"production_endpoints\\\":{\\\"url\\\":\\\"https://localhost:9443/am/sample/pizzashack/v1/api/\\\",\\\"config\\\":null},\\\"sandbox_endpoints\\\":{\\\"url\\\":\\\"https://localhost:9443/am/sample/pizzashack/v1/api/\\\",\\\"config\\\":null},\\\"endpoint_type\\\":\\\"http\\\"}\",\n" +
              "   \"endpointSecurity\":    {\n" +
              "      \"username\": \"user\",\n" +
              "      \"type\": \"basic\",\n" +
              "      \"password\": \"pass\"\n" +
              "   },\n" +
              "   \"gatewayEnvironments\": \"Production and Sandbox\",\n" +
              "   \"sequences\": [],\n" +
              "   \"subscriptionAvailability\": null,\n" +
              "   \"subscriptionAvailableTenants\": [],\n" +
              "   \"businessInformation\":    {\n" +
              "      \"businessOwnerEmail\": \"marketing@pizzashack.com\",\n" +
              "      \"technicalOwnerEmail\": \"architecture@pizzashack.com\",\n" +
              "      \"technicalOwner\": \"John Doe\",\n" +
              "      \"businessOwner\": \"Jane Roe\"\n" +
              "   },\n" +
              "   \"corsConfiguration\":    {\n" +
              "      \"accessControlAllowOrigins\": [\"*\"],\n" +
              "      \"accessControlAllowHeaders\":       [\n" +
              "         \"authorization\",\n" +
              "         \"Access-Control-Allow-Origin\",\n" +
              "         \"Content-Type\",\n" +
              "         \"SOAPAction\"\n" +
              "      ],\n" +
              "      \"accessControlAllowMethods\":       [\n" +
              "         \"GET\",\n" +
              "         \"PUT\",\n" +
              "         \"POST\",\n" +
              "         \"DELETE\",\n" +
              "         \"PATCH\",\n" +
              "         \"OPTIONS\"\n" +
              "      ],\n" +
              "      \"accessControlAllowCredentials\": false,\n" +
              "      \"corsConfigurationEnabled\": false\n" +
              "   }\n" +
              "}").build();
  }
      @Override
      public Response apisPost(API body,String contentType){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisChangeLifecyclePost(String action,String apiId,String lifecycleChecklist,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisCopyApiPost(String newVersion,String apiId){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdPut(String apiId,API body,String contentType,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdDelete(String apiId,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdDocumentsGet(String apiId,Integer limit,Integer offset,String accept,String ifNoneMatch){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdDocumentsPost(String apiId,Document body,String contentType){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdDocumentsDocumentIdPut(String apiId,String documentId,Document body,String contentType,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdDocumentsDocumentIdDelete(String apiId,String documentId,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdDocumentsDocumentIdContentGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdDocumentsDocumentIdContentPost(String apiId,String documentId,String contentType,FormDataContentDisposition fileDetail,String inlineContent,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdSwaggerGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdSwaggerPut(String apiId,String apiDefinition,String contentType,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdThumbnailGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response apisApiIdThumbnailPost(String apiId,FormDataContentDisposition fileDetail,String contentType,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
