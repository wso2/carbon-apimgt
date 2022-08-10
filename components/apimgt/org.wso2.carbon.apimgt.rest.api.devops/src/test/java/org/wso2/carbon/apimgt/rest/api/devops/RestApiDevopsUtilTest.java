/*
 *
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigDTO;
import org.wso2.carbon.apimgt.rest.api.devops.DevopsAPIUtils;
import org.wso2.carbon.apimgt.rest.api.devops.dto.CorrelationComponentDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.CorrelationComponentsListDTO;

import static org.mockito.Mockito.when;
import static org.wso2.carbon.h2.osgi.utils.CarbonConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
public class RestApiDevopsUtilTest {
    @Test
    public void testValidateLogLevelTestcase() throws JsonProcessingException {
        boolean logLevelOff = DevopsAPIUtils.validateLogLevel(APIConstants.APILogHandler.OFF);
        Assert.assertEquals("Log level naming for OFF is changed", true, logLevelOff);

        boolean logLevelBasic = DevopsAPIUtils.validateLogLevel(APIConstants.APILogHandler.BASIC);
        Assert.assertEquals("Log level naming for BASIC is changed", true, logLevelBasic);

        boolean logLevelStandard = DevopsAPIUtils.validateLogLevel(APIConstants.APILogHandler.STANDARD);
        Assert.assertEquals("Log level naming for STANDARD is changed", true, logLevelStandard);

        boolean logLevelFull = DevopsAPIUtils.validateLogLevel(APIConstants.APILogHandler.FULL);
        Assert.assertEquals("Log level naming for FULL is changed", true, logLevelFull);

        boolean logLevelSomething = DevopsAPIUtils.validateLogLevel("abc");
        Assert.assertEquals("Log level 'abc' should not be valid log level", false, logLevelSomething);
    }


    @Test
    public void testValidateCorrelationComponentList() throws APIManagementException {
        List<CorrelationComponentDTO> correlationComponentDTOList = new ArrayList<>();
        for (int i = 0; i < DevopsAPIUtils.CORRELATION_DEFAULT_COMPONENTS.length; i++) {
            CorrelationComponentDTO correlationComponentDTO = new CorrelationComponentDTO();
            correlationComponentDTO.setName(DevopsAPIUtils.CORRELATION_DEFAULT_COMPONENTS[i]);
            correlationComponentDTO.setEnabled("true");
            correlationComponentDTOList.add(correlationComponentDTO);
        }
        CorrelationComponentsListDTO correlationComponentsListDTO = new CorrelationComponentsListDTO();
        correlationComponentsListDTO.setComponents(correlationComponentDTOList);
        Boolean valid = DevopsAPIUtils.validateCorrelationComponentList(correlationComponentsListDTO);
        Assert.assertTrue(valid);

        CorrelationComponentDTO correlationComponentDTO = new CorrelationComponentDTO();
        correlationComponentDTO.setName("abc");
        correlationComponentDTOList.add(correlationComponentDTO);
        correlationComponentsListDTO.setComponents(correlationComponentDTOList);
        try {
            valid = DevopsAPIUtils.validateCorrelationComponentList(correlationComponentsListDTO);
        } catch (APIManagementException e) {
            return;
        }
        Assert.assertTrue("validateCorrelationComponentList did not throw an exception", false);


    }
}
