/*
 *
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.models.Component;
import org.wso2.carbon.apimgt.core.models.Event;

//import java.io.BufferedReader;
//import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Implementation which observes to the API Manager events and trigger corresponding lambda function that belongs to
 * the user and particular event occurred.
 */
public class LambdaFunctionTrigger implements EventObserver {


    private static final Logger log = LoggerFactory.getLogger(EventObserver.class);

    private static final LambdaFunctionTrigger LAMBDA_FUNCTION_TRIGGER = new LambdaFunctionTrigger();

    @Override
    public void captureEvent(Component component, Event event, String username) {

        log.info("Testing 1 - Send Http GET request");
        try {
            this.sendGet();
        } catch (Exception e) {
            log.error("Cannot invoke URI.");
        }
    }

    /*public static void main(String[] args) throws Exception {

        LambdaFunctionTrigger http = new LambdaFunctionTrigger();

        log.info("Testing 1 - Send Http GET request");
        http.sendGet();

    }*/

    // HTTP GET request
    private void sendGet() throws Exception {

        //final String userAgent = "Mozilla/50.1.0";
        String url = "http://wso2.com/products/api-manager";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        //con.setRequestProperty("User-Agent", userAgent);

        int responseCode = con.getResponseCode();
        log.info("\nSending 'GET' request to URL : " + url);
        log.info("Response Code : " + responseCode);

        String[] browsers = {"firefox", "opera", "chrome"}; // common browser names
        String browser = null;
        for (int count = 0; count < browsers.length && browser == null; count++) {
            if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                browser = browsers[count]; // have found a browser
            }
        }
        Runtime.getRuntime().exec(new String[]{browser, url}); // open using a browser

       /* BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        log.info(response.toString());*/
    }

    public static LambdaFunctionTrigger getLambdaFunctionTriggerObject() {
        return LAMBDA_FUNCTION_TRIGGER;
    }
}
