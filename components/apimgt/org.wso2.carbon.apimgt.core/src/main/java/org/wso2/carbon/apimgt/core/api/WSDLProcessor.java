/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.WSDLInfo;

/**
 * Interface class that contains the common operations for WSDL 1.1 and 2.0
 */
public interface WSDLProcessor {

    // ------------- Single WSDL file related operations --------------------
    
    /**
     * Initializes the processor with a single WSDL file content.
     * 
     * @param wsdlContent Content bytes of WSDL file
     * @return true if initialization successful
     * @throws APIMgtWSDLException Unexpected error while initialization
     */
    boolean init(byte[] wsdlContent) throws APIMgtWSDLException;

    /**
     * This method will be called after initialization of the processor by {@link #init(byte[])}.
     * Retrieves the current WSDL's content bytes.
     * 
     * @return Content bytes of the current state of the WSDL
     * @throws APIMgtWSDLException error while reading WSDL content
     */
    byte[] getWSDL() throws APIMgtWSDLException;

    /**
     * This method will be called after initialization of the processor by {@link #init(byte[])}.
     * Updates the WSDL and retrieves based on the provided API and Label. This is the content shown in API Store for
     * the particular API's WSDL.
     * 
     * @param api Provided API object
     * @param label Provided label object
     * @return Updated WSDL
     * @throws APIMgtWSDLException Error while updating the WSDL
     */
    byte[] getUpdatedWSDL(API api, Label label) throws APIMgtWSDLException;

    // ------------- Multiple WSDLs related operations --------------------
    
    /**
     * Initialize the processor based on a provided file path which contains WSDL files.
     * 
     * @param path File path with WSDL files
     * @return true if initialization successful
     * @throws APIMgtWSDLException Unexpected error while initialization
     */
    boolean initPath(String path) throws APIMgtWSDLException;

    /**
     * This method will be called after initialization of the processor by {@link #initPath(String)}.
     * Updates all WSDL files in the provided path while initialization based on the provided API and Label.
     * This is the content shown in API Store for the particular API's WSDL.
     * 
     * @param api Provided API object
     * @param label Provided label object
     * @return path of the WSDL files (same as the path the processor was initialized by {@link #initPath(String)}
     * @throws APIMgtWSDLException  Error while updating the WSDL files
     */
    String getUpdatedWSDLPath(API api, Label label) throws APIMgtWSDLException;

    // ------------- Common operations --------------------
    
    /**
     * Returns whether this WSDL processor can process the provided WSDL content bytes or WSDL file path .
     * To be called after calling {@link #init(byte[])} or  {@link #initPath(String)}.
     * 
     * @return true if WSDL can be processed by this processor
     */
    boolean canProcess();

    /**
     * Returns WSDL information including WSDL version, endpoints.
     * 
     * @return WSDL information
     * @throws APIMgtWSDLException Error occurred while retrieving WSDL information
     */
    WSDLInfo getWsdlInfo() throws APIMgtWSDLException;
}
