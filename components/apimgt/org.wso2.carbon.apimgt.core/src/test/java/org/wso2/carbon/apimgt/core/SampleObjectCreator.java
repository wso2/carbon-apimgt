/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
 */

package org.wso2.carbon.apimgt.core;

import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;

import java.util.Date;


public class SampleObjectCreator {

    public static API.APIBuilder getMockAPIObject() {
        return new API.APIBuilder("admin", "Sample", "1.0.0").context("/sample/v1").lifecycleInstanceId
                ("7a2298c4-c905-403f-8fac-38c73301631f").apiDefinition("paths:\n" +
                "  /order:\n" +
                "    post:\n" +
                "      description: Create a new Order\n" +
                "      parameters:\n" +
                "        - schema:\n" +
                "            $ref: '#/definitions/Order'\n" +
                "          description: Order object that needs to be added\n" +
                "          name: body\n" +
                "          required: true\n" +
                "          in: body\n" +
                "      responses:\n" +
                "        '201':\n" +
                "          headers:\n" +
                "            Location:\n" +
                "              description: The URL of the newly created resource.\n" +
                "              type: string\n" +
                "            Content-Type:\n" +
                "              description: The content type of the body.\n" +
                "              type: string\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Order'\n" +
                "          description: >-\n" +
                "            Created. Successful response with the newly created object as entity\n" +
                "            in the body. Location header contains URL of newly created entity.\n" +
                "        '400':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Error'\n" +
                "          description: Bad Request. Invalid request or validation error.\n" +
                "        '415':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Error'\n" +
                "          description: >-\n" +
                "            Unsupported Media Type. The entity of the request was in a not\n" +
                "            supported format.\n" +
                "  /menu:\n" +
                "    get:\n" +
                "      description: Return a list of available menu items\n" +
                "      parameters: []\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          headers: {}\n" +
                "          schema:\n" +
                "            title: Menu\n" +
                "            properties:\n" +
                "              list:\n" +
                "                items:\n" +
                "                  $ref: '#/definitions/MenuItem'\n" +
                "                type: array\n" +
                "            type: object\n" +
                "          description: OK. List of APIs is returned.\n" +
                "        '304':\n" +
                "          description: >-\n" +
                "            Not Modified. Empty body because the client has already the latest\n" +
                "            version of the requested resource.\n" +
                "        '406':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Error'\n" +
                "          description: Not Acceptable. The requested media type is not supported\n" +
                "  '/order/{orderId}':\n" +
                "    put:\n" +
                "      description: Update an existing Order\n" +
                "      parameters:\n" +
                "        - description: Order Id\n" +
                "          name: orderId\n" +
                "          format: integer\n" +
                "          type: number\n" +
                "          required: true\n" +
                "          in: path\n" +
                "        - schema:\n" +
                "            $ref: '#/definitions/Order'\n" +
                "          description: Order object that needs to be added\n" +
                "          name: body\n" +
                "          required: true\n" +
                "          in: body\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          headers:\n" +
                "            Location:\n" +
                "              description: The URL of the newly created resource.\n" +
                "              type: string\n" +
                "            Content-Type:\n" +
                "              description: The content type of the body.\n" +
                "              type: string\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Order'\n" +
                "          description: OK. Successful response with updated Order\n" +
                "        '400':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Error'\n" +
                "          description: Bad Request. Invalid request or validation error\n" +
                "        '404':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Error'\n" +
                "          description: Not Found. The resource to be updated does not exist.\n" +
                "        '412':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Error'\n" +
                "          description: >-\n" +
                "            Precondition Failed. The request has not been performed because one\n" +
                "            of the preconditions is not met.\n" +
                "    get:\n" +
                "      description: Get details of an Order\n" +
                "      parameters:\n" +
                "        - description: Order Id\n" +
                "          name: orderId\n" +
                "          format: integer\n" +
                "          type: number\n" +
                "          required: true\n" +
                "          in: path\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Order'\n" +
                "          headers: {}\n" +
                "          description: OK Requested Order will be returned\n" +
                "        '304':\n" +
                "          description: >-\n" +
                "            Not Modified. Empty body because the client has already the latest\n" +
                "            version of the requested resource.\n" +
                "        '404':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Error'\n" +
                "          description: Not Found. Requested API does not exist.\n" +
                "        '406':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Error'\n" +
                "          description: Not Acceptable. The requested media type is not supported\n" +
                "    delete:\n" +
                "      description: Delete an existing Order\n" +
                "      parameters:\n" +
                "        - description: Order Id\n" +
                "          name: orderId\n" +
                "          format: integer\n" +
                "          type: number\n" +
                "          required: true\n" +
                "          in: path\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: OK. Resource successfully deleted.\n" +
                "        '404':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Error'\n" +
                "          description: Not Found. Resource to be deleted does not exist.\n" +
                "        '412':\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/Error'\n" +
                "          description: >-\n" +
                "            Precondition Failed. The request has not been performed because one\n" +
                "            of the preconditions is not met.\n" +
                "schemes:\n" +
                "  - https\n" +
                "produces:\n" +
                "  - application/json\n" +
                "swagger: '2.0'\n" +
                "definitions:\n" +
                "  ErrorListItem:\n" +
                "    title: Description of individual errors that may have occored during a request.\n" +
                "    properties:\n" +
                "      message:\n" +
                "        description: Description about individual errors occored\n" +
                "        type: string\n" +
                "      code:\n" +
                "        format: int64\n" +
                "        type: integer\n" +
                "    required:\n" +
                "      - code\n" +
                "      - message\n" +
                "  MenuItem:\n" +
                "    title: Pizza menu Item\n" +
                "    properties:\n" +
                "      price:\n" +
                "        type: string\n" +
                "      description:\n" +
                "        type: string\n" +
                "      name:\n" +
                "        type: string\n" +
                "      image:\n" +
                "        type: string\n" +
                "    required:\n" +
                "      - name\n" +
                "  Order:\n" +
                "    title: Pizza Order\n" +
                "    properties:\n" +
                "      customerName:\n" +
                "        type: string\n" +
                "      delivered:\n" +
                "        type: boolean\n" +
                "      address:\n" +
                "        type: string\n" +
                "      pizzaType:\n" +
                "        type: string\n" +
                "      creditCardNumber:\n" +
                "        type: string\n" +
                "      quantity:\n" +
                "        type: number\n" +
                "      orderId:\n" +
                "        type: integer\n" +
                "    required:\n" +
                "      - orderId\n" +
                "  Error:\n" +
                "    title: Error object returned with 4XX HTTP status\n" +
                "    properties:\n" +
                "      message:\n" +
                "        description: Error message.\n" +
                "        type: string\n" +
                "      error:\n" +
                "        items:\n" +
                "          $ref: '#/definitions/ErrorListItem'\n" +
                "        description: >-\n" +
                "          If there are more than one error list them out. Ex. list out\n" +
                "          validation errors by each field.\n" +
                "        type: array\n" +
                "      description:\n" +
                "        description: A detail description about the error message.\n" +
                "        type: string\n" +
                "      code:\n" +
                "        format: int64\n" +
                "        type: integer\n" +
                "      moreInfo:\n" +
                "        description: Preferably an url with more details about the error.\n" +
                "        type: string\n" +
                "    required:\n" +
                "      - code\n" +
                "      - message\n" +
                "consumes:\n" +
                "  - application/json\n" +
                "info:\n" +
                "  title: PizzaShackAPI\n" +
                "  description: >\n" +
                "    This document describe a RESTFul API for Pizza Shack online pizza delivery\n" +
                "    store.\n" +
                "  license:\n" +
                "    name: Apache 2.0\n" +
                "    url: 'http://www.apache.org/licenses/LICENSE-2.0.html'\n" +
                "  contact:\n" +
                "    email: architecture@pizzashack.com\n" +
                "    name: John Doe\n" +
                "    url: 'http://www.pizzashack.com'\n" +
                "  version: 1.0.0").createdTime(new Date()).lastUpdatedTime
                (new Date());
    }

    public static LifecycleState getMockLifecycleStateObject() {
        LifecycleState lifecycleState = new LifecycleState();
        lifecycleState.setLcName("API_LIFECYCLE");
        lifecycleState.setLifecycleId("7a2298c4-c905-403f-8fac-38c73301631f");
        lifecycleState.setState("PUBLISH");
        return lifecycleState;
    }
    public static API getMockApiSummaryObject(){
        return new API.APIBuilder("admin","Sample","1.0.0").build();
    }
}
