/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axiom.om.OMElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsData;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsDataPublisher;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.fail;

/**
 * Test class for APIMgtGoogleAnalyticsUtils
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIMgtGoogleAnalyticsUtilsTestCase.class, APIMgtGoogleAnalyticsUtils.class,
        PrivilegedCarbonContext.class, ServiceReferenceHolder.class, GoogleAnalyticsDataPublisher.class})
public class APIMgtGoogleAnalyticsUtilsTestCase {

    @Before
    public void setup() {
        System.setProperty("carbon.home", "jhkjn");
    }

    @Test
    public void testGetCacheBusterId() {
        try {
            Assert.assertNotNull(APIMgtGoogleAnalyticsUtils.getCacheBusterId());
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPublishGATrackingData() {
        APIMgtGoogleAnalyticsUtils apiMgtGoogleAnalyticsUtils = new APIMgtGoogleAnalyticsUtils();
        GoogleAnalyticsData.DataBuilder dataBuilder = Mockito.mock(GoogleAnalyticsData.DataBuilder.class);

        //test when gaConfig == null
        apiMgtGoogleAnalyticsUtils.publishGATrackingData(dataBuilder, "abc", "567r637r6");

        //test when gaConfig !=  null
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        Resource resource = Mockito.mock(Resource.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        try {
            Mockito.when(resource.getContentStream()).thenReturn(new ByteArrayInputStream(Charset.forName("UTF-16").encode("<GoogleAnalyticsTracking>\n" +
                    "\t<!--Enable/Disable Google Analytics Tracking -->\n" +
                    "\t<Enabled>false</Enabled>\n" +
                    "\n" +
                    "\t<!-- Google Analytics Tracking ID -->\n" +
                    "\t<TrackingID>UA-XXXXXXXX-X</TrackingID>\n" +
                    "\n" +
                    "</GoogleAnalyticsTracking>").array()));
            Mockito.when(userRegistry.get("/apimgt/statistics/ga-config.xml")).thenReturn(resource);
        } catch (RegistryException e) {
            fail("RegistryException is thrown when tesing .");
            e.printStackTrace();
        }
        try {
            Mockito.when(registryService.getGovernanceSystemRegistry()).thenReturn(userRegistry);
        } catch (RegistryException e) {
            fail("RegistryException is thrown.");
        }

        apiMgtGoogleAnalyticsUtils.init("abc.com");
        //test when gaconfig.enabled=false
        apiMgtGoogleAnalyticsUtils.publishGATrackingData(dataBuilder, "ishara", "jhgy");

        //test when gaconfig.enabled=true annonymous
        try {
            Mockito.when(resource.getContentStream()).thenReturn(new ByteArrayInputStream(Charset.forName("UTF-16").encode("<GoogleAnalyticsTracking>\n" +
                    "\t<!--Enable/Disable Google Analytics Tracking -->\n" +
                    "\t<Enabled>true</Enabled>\n" +
                    "\n" +
                    "\t<!-- Google Analytics Tracking ID -->\n" +
                    "\t<TrackingID>UA-XXXXXXXX-X</TrackingID>\n" +
                    "\n" +
                    "</GoogleAnalyticsTracking>").array()));
            apiMgtGoogleAnalyticsUtils.init("abc.com");
            GoogleAnalyticsData.DataBuilder dataBuilder1 = Mockito.mock(GoogleAnalyticsData.DataBuilder.class);
            Mockito.when(dataBuilder.setProtocolVersion("1")).thenReturn(dataBuilder);
            Mockito.when(dataBuilder.setTrackingId("UA-XXXXXXXX-X")).thenReturn(dataBuilder);
            Mockito.when(dataBuilder.setClientId("0x05a823c101178dd5")).thenReturn(dataBuilder);
            Mockito.when(dataBuilder.setHitType("pageview")).thenReturn(dataBuilder);
            GoogleAnalyticsData data = Mockito.mock(GoogleAnalyticsData.class);
            Mockito.when(dataBuilder.build()).thenReturn(data);

            PowerMockito.mockStatic(GoogleAnalyticsDataPublisher.class);
            PowerMockito.when(GoogleAnalyticsDataPublisher.buildPayloadString(data)).thenReturn("payload");
        } catch (RegistryException e) {
            fail(e.getMessage());
        }
        apiMgtGoogleAnalyticsUtils.publishGATrackingData(dataBuilder, "ishara", "Autorization ishara");

    }
}
