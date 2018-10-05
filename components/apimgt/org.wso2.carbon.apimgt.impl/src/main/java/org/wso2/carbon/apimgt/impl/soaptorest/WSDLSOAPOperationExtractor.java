/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.soaptorest;

import org.wso2.carbon.apimgt.impl.soaptorest.exceptions.APIMgtWSDLException;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLInfo;

/**
 * Interface to extend different the wsdl operation extractor implementations.
 */
public interface WSDLSOAPOperationExtractor {

    /**
     * Initializes the wsdl content and definition
     *
     * @param wsdlContent parsed wsdl content
     * @return status of the initialization
     * @throws APIMgtWSDLException throws when error happens at initialization
     */
    boolean init(byte[] wsdlContent) throws APIMgtWSDLException;

    /**
     * Initialize the processor based on a provided file path which contains WSDL files.
     *
     * @param path File path with WSDL files
     * @return true if initialization successful
     * @throws APIMgtWSDLException Unexpected error while initialization
     */
    boolean initPath(String path) throws APIMgtWSDLException;

    /**
     * Populates the wsdl info object by reading the wsdl
     *
     * @return {@link WSDLInfo} object
     * @throws APIMgtWSDLException
     */
    WSDLInfo getWsdlInfo() throws APIMgtWSDLException;

    /**
     * Returns whether this WSDL processor can process the provided WSDL content bytes or WSDL file path .
     * To be called after calling {@link #init(byte[])} or  {@link #initPath(String)}.
     *
     * @return true if WSDL can be processed by this processor
     */
    boolean canProcess();
}
