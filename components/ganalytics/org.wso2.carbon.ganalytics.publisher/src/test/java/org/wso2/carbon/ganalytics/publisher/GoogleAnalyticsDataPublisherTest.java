/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.ganalytics.publisher;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.http.NameValuePair;
import org.junit.Test;

import java.util.List;

public class GoogleAnalyticsDataPublisherTest extends TestCase {

    public GoogleAnalyticsDataPublisherTest(String name) {
        super(name);
    }

    @Test
    public void testQueryParamBuilderPageView() throws Exception {
        GoogleAnalyticsData data = new GoogleAnalyticsData
                .DataBuilder("trackingId", "1", "clientId", GoogleAnalyticsConstants.HIT_TYPE_PAGEVIEW)
                .setDocumentPath("/")
                .setDocumentHostName("localhost.com")
                .setDocumentTitle("Home Page")
                .build();

        Assert.assertEquals("v=1&tid=trackingId&cid=clientId&t=pageview&dh=localhost.com&dp=%2F&dt=Home+Page", GoogleAnalyticsDataPublisher.buildPayloadString(data));
    }

    @Test
    public void testPublishGETHTTP() throws Exception {
        GoogleAnalyticsData data = new GoogleAnalyticsData
                .DataBuilder("UA-50303033-1", "1", "35009a79-1a05-49d7-b876-2b884d0fsadfa", GoogleAnalyticsConstants.HIT_TYPE_PAGEVIEW)
                .setDocumentPath("/testpageGET")
                .setDocumentHostName("localhost.com")
                .setDocumentTitle("HTTP GET")
                .build();

        String payload = GoogleAnalyticsDataPublisher.buildPayloadString(data);
        Assert.assertTrue(GoogleAnalyticsDataPublisher.publishGET(payload, "Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0", false));
    }

    @Test
    public void testPublishGETHTTPS() throws Exception {
        GoogleAnalyticsData data = new GoogleAnalyticsData
                .DataBuilder("UA-50303033-1", "1", "35009a79-1a05-49d7-b876-2b884d0fsadfa", GoogleAnalyticsConstants.HIT_TYPE_PAGEVIEW)
                .setDocumentPath("/testpageGET")
                .setDocumentHostName("localhost.com")
                .setDocumentTitle("HTTPS GET")
                .build();

        String payload = GoogleAnalyticsDataPublisher.buildPayloadString(data);
        Assert.assertTrue(GoogleAnalyticsDataPublisher.publishGET(payload, "Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0", true));
    }

    @Test
    public void testPublishPOSTHTTP() throws Exception {
        GoogleAnalyticsData data = new GoogleAnalyticsData
                .DataBuilder("UA-50303033-1", "1", "35009a79-1a05-49d7-b876-2b884d0fsadfa", GoogleAnalyticsConstants.HIT_TYPE_PAGEVIEW)
                .setDocumentPath("/testpagePOST")
                .setDocumentHostName("localhost.com")
                .setDocumentTitle("HTTP POST")
                .build();

        List<NameValuePair> payload = GoogleAnalyticsDataPublisher.buildPayload(data);
        Assert.assertTrue(GoogleAnalyticsDataPublisher.publishPOST(payload, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1944.0 Safari/537.36", false));
    }

    @Test
    public void testPublishPOSTHTTPS() throws Exception {
        GoogleAnalyticsData data = new GoogleAnalyticsData
                .DataBuilder("UA-50303033-1", "1", "35009a79-1a05-49d7-b876-2b884d0fsadfa", GoogleAnalyticsConstants.HIT_TYPE_PAGEVIEW)
                .setDocumentPath("/testpagePOST")
                .setDocumentHostName("localhost.com")
                .setDocumentTitle("HTTPS POST")
                .build();

        List<NameValuePair> payload = GoogleAnalyticsDataPublisher.buildPayload(data);
        Assert.assertTrue(GoogleAnalyticsDataPublisher.publishPOST(payload, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1944.0 Safari/537.36", true));
    }
}