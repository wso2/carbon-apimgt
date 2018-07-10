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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.RegistrationSummary;
import org.wso2.carbon.apimgt.rest.api.core.dto.CredentialsDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.LabelInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationSummaryDTO;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public class GatewaysApiServiceImplTestCase {

    private final static String DSS_USERNAME = "dssAdmin";
    private final static String DSS_PASSWORD = "dssAdmin";
    private final static String KEY_MANAGER_USERNAME = "keyManagerAdmin";
    private final static String KEY_MANAGER_PASSWORD = "keyManagerPwd";
    private final static String THROTTLE_SERVER_USERNAME = "throttleServerAdmin";
    private final static String THROTTLE_SERVER_PASSWORD = "throttleServerPwd";

    @Test
    public void gatewaysRegisterPostTest() throws Exception {

        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        RegistrationDTO registrationDTO = Mockito.mock(RegistrationDTO.class);
        LabelInfoDTO labelInfoDTO = Mockito.mock(LabelInfoDTO.class);
        Mockito.when(registrationDTO.getLabelInfo()).thenReturn(labelInfoDTO);

        RegistrationSummary registrationSummary = Mockito.mock(RegistrationSummary.class);
        Mockito.when(adminService.getRegistrationSummary()).thenReturn(registrationSummary);

        RegistrationSummary.AnalyticsInfo analyticsInfo = Mockito.mock(RegistrationSummary.AnalyticsInfo.class);
        Mockito.when(registrationSummary.getAnalyticsInfo()).thenReturn(analyticsInfo);
        RegistrationSummary.Credentials dssCredentials = Mockito.mock(RegistrationSummary.Credentials.class);
        Mockito.when(registrationSummary.getAnalyticsInfo().getDasServerCredentials()).thenReturn(dssCredentials);
        Mockito.when(dssCredentials.getUsername()).thenReturn(DSS_USERNAME);
        Mockito.when(dssCredentials.getPassword()).thenReturn(DSS_PASSWORD);

        RegistrationSummary.JWTInfo jwtInfo = Mockito.mock(RegistrationSummary.JWTInfo.class);
        Mockito.when(registrationSummary.getJwtInfo()).thenReturn(jwtInfo);

        RegistrationSummary.KeyManagerInfo keyManagerInfo = Mockito.mock(RegistrationSummary.KeyManagerInfo.class);
        Mockito.when(registrationSummary.getKeyManagerInfo()).thenReturn(keyManagerInfo);
        RegistrationSummary.Credentials keyManagerCredentials = Mockito.mock(RegistrationSummary.Credentials.class);
        Mockito.when(registrationSummary.getKeyManagerInfo().getCredentials()).thenReturn(keyManagerCredentials);
        Mockito.when(keyManagerCredentials.getUsername()).thenReturn(KEY_MANAGER_USERNAME);
        Mockito.when(keyManagerCredentials.getPassword()).thenReturn(KEY_MANAGER_PASSWORD);

        RegistrationSummary.ThrottlingInfo throttlingInfo = Mockito.mock(RegistrationSummary.ThrottlingInfo.class);
        Mockito.when(registrationSummary.getThrottlingInfo()).thenReturn(throttlingInfo);
        RegistrationSummary.ThrottlingInfo.DataPublisher dataPublisher = Mockito
                .mock(RegistrationSummary.ThrottlingInfo.DataPublisher.class);
        Mockito.when(registrationSummary.getThrottlingInfo().getDataPublisher()).thenReturn(dataPublisher);

        RegistrationSummary.Credentials throttlingServerCredentials = Mockito
                .mock(RegistrationSummary.Credentials.class);
        Mockito.when(registrationSummary.getThrottlingInfo().getDataPublisher().getCredentials())
                .thenReturn(throttlingServerCredentials);
        Mockito.when(throttlingServerCredentials.getUsername()).thenReturn(THROTTLE_SERVER_USERNAME);
        Mockito.when(throttlingServerCredentials.getPassword()).thenReturn(THROTTLE_SERVER_PASSWORD);

        RegistrationSummary.GoogleAnalyticsTrackingInfo googleAnalyticsTrackingInfo = Mockito.mock
                (RegistrationSummary.GoogleAnalyticsTrackingInfo.class);
        Mockito.when(registrationSummary.getGoogleAnalyticsTrackingInfo()).thenReturn(googleAnalyticsTrackingInfo);

        GatewaysApiServiceImpl gatewaysApiService = new GatewaysApiServiceImpl(adminService);
        Response response = gatewaysApiService.gatewaysRegisterPost(registrationDTO, "application/json", getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        CredentialsDTO analyticsInfoResponseCredentials = ((RegistrationSummaryDTO) response.getEntity())
                .getAnalyticsInfo().getCredentials();
        CredentialsDTO keyManagerInfoResponseCredentials = ((RegistrationSummaryDTO) response.getEntity())
                .getKeyManagerInfo().getCredentials();
        CredentialsDTO throttleServerInfoResponseCredentials = ((RegistrationSummaryDTO) response.getEntity())
                .getThrottlingInfo().getCredentials();

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(analyticsInfoResponseCredentials.getUsername(), DSS_USERNAME);
        Assert.assertEquals(analyticsInfoResponseCredentials.getPassword(), DSS_PASSWORD);
        Assert.assertEquals(keyManagerInfoResponseCredentials.getUsername(), KEY_MANAGER_USERNAME);
        Assert.assertEquals(keyManagerInfoResponseCredentials.getPassword(), KEY_MANAGER_PASSWORD);
        Assert.assertEquals(throttleServerInfoResponseCredentials.getUsername(), THROTTLE_SERVER_USERNAME);
        Assert.assertEquals(throttleServerInfoResponseCredentials.getPassword(), THROTTLE_SERVER_PASSWORD);

    }

    @Test
    public void gatewaysRegisterPostLabelsNullTest() throws Exception {

        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        GatewaysApiServiceImpl gatewaysApiService = new GatewaysApiServiceImpl(adminService);
        RegistrationDTO registrationDTO = Mockito.mock(RegistrationDTO.class);
        Response response = gatewaysApiService.gatewaysRegisterPost(registrationDTO, "application/json", getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void gatewaysRegisterPostExceptionTest() throws Exception {
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        RegistrationSummary summary = Mockito.mock(RegistrationSummary.class);

        RegistrationSummary.KeyManagerInfo keyManagerInfo = Mockito.mock(RegistrationSummary.KeyManagerInfo.class);
        Mockito.when(summary.getKeyManagerInfo()).thenReturn(keyManagerInfo);

        RegistrationSummary.Credentials credentials = Mockito.mock(RegistrationSummary.Credentials.class);
        Mockito.when(keyManagerInfo.getCredentials()).thenReturn(credentials);

        RegistrationSummary.AnalyticsInfo analyticsInfo = Mockito.mock(RegistrationSummary.AnalyticsInfo.class);
        Mockito.when(summary.getAnalyticsInfo()).thenReturn(analyticsInfo);

        RegistrationSummary.Credentials dasCredentials = Mockito.mock(RegistrationSummary.Credentials.class);
        Mockito.when(analyticsInfo.getDasServerCredentials()).thenReturn(dasCredentials);

        RegistrationSummary.JWTInfo jwtInfo = Mockito.mock(RegistrationSummary.JWTInfo.class);
        Mockito.when(summary.getJwtInfo()).thenReturn(jwtInfo);

        RegistrationSummary.ThrottlingInfo throttlingInfo = Mockito.mock(RegistrationSummary.ThrottlingInfo.class);
        Mockito.when(summary.getThrottlingInfo()).thenReturn(throttlingInfo);

        RegistrationSummary.ThrottlingInfo.DataPublisher dataPublisher =
                Mockito.mock(RegistrationSummary.ThrottlingInfo.DataPublisher.class);
        Mockito.when(throttlingInfo.getDataPublisher()).thenReturn(dataPublisher);

        RegistrationSummary.Credentials publisherCredentials = Mockito.mock(RegistrationSummary.Credentials.class);
        Mockito.when(dataPublisher.getCredentials()).thenReturn(publisherCredentials);

        RegistrationSummary.GoogleAnalyticsTrackingInfo trackingInfo =
                Mockito.mock(RegistrationSummary.GoogleAnalyticsTrackingInfo.class);
        Mockito.when(summary.getGoogleAnalyticsTrackingInfo()).thenReturn(trackingInfo);

        Mockito.when(adminService.getRegistrationSummary()).thenReturn(summary);
        RegistrationDTO registrationDTO = Mockito.mock(RegistrationDTO.class);
        LabelInfoDTO labelInfoDTO = Mockito.mock(LabelInfoDTO.class);
        Mockito.when(registrationDTO.getLabelInfo()).thenReturn(labelInfoDTO);

        //Mockito.(adminService.registerGatewayLabels(Mockito.anyListOf(Label.class), Mockito.anyString()));
        Mockito.doThrow(new APIManagementException("", ExceptionCodes.APIMGT_DAO_EXCEPTION)).
                when(adminService).registerGatewayLabels(Mockito.anyListOf(Label.class), Mockito.anyString());
        //.
        //        thenThrow(new APIManagementException("", ExceptionCodes.APIMGT_DAO_EXCEPTION));

        GatewaysApiServiceImpl gatewaysApiService = new GatewaysApiServiceImpl(adminService);
        Response response = gatewaysApiService.gatewaysRegisterPost(registrationDTO, "application/json", getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private Request getRequest() throws Exception {
        return Mockito.mock(Request.class);
    }

}
