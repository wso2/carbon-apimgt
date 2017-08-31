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

package org.wso2.carbon.apimgt.core.impl;

import org.wso2.carbon.apimgt.core.api.WSDLProcessor;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.util.APIMWSDLUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This factory class provides WSDL 1.1 or 2.0 processor instances based on the WSDL content
 */
public class WSDLProcessFactory {

    private List<String> wsdlProcessorClasses;
    private static volatile WSDLProcessFactory instance;

    private WSDLProcessFactory() {
        wsdlProcessorClasses = new ArrayList<>();
        wsdlProcessorClasses.addAll(APIMConfigurationService.getInstance().getApimConfigurations().getWsdlProcessors());
    }

    /**
     * Returns a {@link WSDLProcessFactory} instance
     * 
     * @return a {@link WSDLProcessFactory} instance
     */
    public static WSDLProcessFactory getInstance() {
        if (instance == null) {
            synchronized (WSDLProcessFactory.class) {
                if (instance == null) {
                    instance = new WSDLProcessFactory();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the appropriate WSDL 1.1 or 2.0 processor based on the content of the provided {@code wsdlUrl}.
     *
     * @param wsdlUrl URL of the WSDL
     * @return WSDL 1.1 or 2.0 processor for the provided content
     * @throws APIMgtWSDLException If an error occurs while determining the processor
     */
    public WSDLProcessor getWSDLProcessor(String wsdlUrl) throws APIMgtWSDLException {
        byte[] wsdlContent = APIMWSDLUtils.getWSDL(wsdlUrl);
        return getWSDLProcessor(wsdlContent);
    }

    /**
     * Returns the appropriate WSDL 1.1 or 2.0 processor based on the file path {@code wsdlPath}.
     *
     * @param wsdlPath File path containing WSDL files and dependant files
     * @return WSDL 1.1 or 2.0 processor for the provided content
     * @throws APIMgtWSDLException If an error occurs while determining the processor
     */
    public WSDLProcessor getWSDLProcessorForPath(String wsdlPath) throws APIMgtWSDLException {
        for (String clazz : getWSDLProcessorClasses()) {
            WSDLProcessor processor;
            try {
                processor = (WSDLProcessor) Class.forName(clazz).newInstance();
                boolean canProcess = processor.initPath(wsdlPath);
                if (canProcess) {
                    return processor;
                }
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new APIMgtWSDLException("Error while instantiating " + clazz, e,
                        ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
            }
        }

        //no processors found if this line reaches
        throw new APIMgtWSDLException("No WSDL processor found to process WSDL content",
                ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT);
    }

    /**
     * Returns the appropriate WSDL 1.1 or 2.0 processor based on the content {@code wsdlContent}.
     *
     * @param wsdlContent Content of the WSDL
     * @return WSDL 1.1 or 2.0 processor for the provided content
     * @throws APIMgtWSDLException If an error occurs while determining the processor
     */
    public WSDLProcessor getWSDLProcessor(byte[] wsdlContent) throws APIMgtWSDLException {
        for (String clazz : getWSDLProcessorClasses()) {
            WSDLProcessor processor;
            try {
                processor = (WSDLProcessor) Class.forName(clazz).newInstance();
                boolean canProcess = processor.init(wsdlContent);
                if (canProcess) {
                    return processor;
                }
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new APIMgtWSDLException("Error while instantiating " + clazz, e,
                        ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
            }
        }

        //no processors found if this line reaches
        throw new APIMgtWSDLException("No WSDL processor found to process WSDL content",
                ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT);
    }

    /**
     * Retrieves the list of WSDL processor classes which are currently in use
     * 
     * @return The list of WSDL processor classes which are currently in use
     */
    public List<String> getWSDLProcessorClasses() {
        return wsdlProcessorClasses;
    }
}
