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

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.core.api.WSDLProcessor;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.util.APIMWSDLUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class WSDLProcessFactory {

    private List<String> wsdlProcessorClasses;
    private static WSDLProcessFactory instance;

    private WSDLProcessFactory() {
        wsdlProcessorClasses = new ArrayList<>();
        wsdlProcessorClasses.add("org.wso2.carbon.apimgt.core.impl.WSDL11ProcessorImpl");
    }

    public static WSDLProcessFactory getInstance() {
        if (instance == null) {
            instance = new WSDLProcessFactory();
        }
        return instance;
    }

    public WSDLProcessor getWSDLProcessor(String wsdlUrl) throws APIMgtWSDLException {
        byte[] wsdlContent = APIMWSDLUtils.getWSDL(wsdlUrl);
        return getWSDLProcessor(wsdlContent);
    }

    public WSDLProcessor getWSDLProcessorForPath(String wsdlPath) throws APIMgtWSDLException {
        for (String clazz : wsdlProcessorClasses) {
            WSDLProcessor processor;
            try {
                processor = (WSDLProcessor) Class.forName(clazz).newInstance();
                processor.initPath(wsdlPath);
                if (processor.canProcess()) {
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

    public WSDLProcessor getWSDLProcessor(InputStream inputStream) throws APIMgtWSDLException {
        byte[] wsdlContent;
        try {
            wsdlContent = IOUtils.toByteArray(inputStream);
            return getWSDLProcessor(wsdlContent);
        } catch (IOException e) {
            throw new APIMgtWSDLException("Cannot convert WSDL to byte array.", e,
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT);
        }
    }

    public WSDLProcessor getWSDLProcessor(byte[] wsdlContent) throws APIMgtWSDLException {
        for (String clazz : wsdlProcessorClasses) {
            WSDLProcessor processor;
            try {
                processor = (WSDLProcessor) Class.forName(clazz).newInstance();
                processor.init(wsdlContent);
                if (processor.canProcess()) {
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
}
