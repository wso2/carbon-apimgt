// Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;
import ballerina/internal;
import ballerina/io;

// Subscription filter to validate the subscriptions which is available in the  jwt token
// This filter should only be engaged when jwt token is is used for authentication. For oauth2
// OAuthnFilter will handle the subscription validation as well.
@Description {value:"Representation of the Subscription filter"}
@Field {value:"filterRequest: request filter method which attempts to validate the subscriptions"}
public type SubscriptionFilter object {

    @Description {value:"filterRequest: Request filter function"}
    public function filterRequest (http:Request request, http:FilterContext filterContext) returns http:FilterResult {
        string authScheme = runtime:getInvocationContext().authContext.scheme;
        if(authScheme == AUTH_SCHEME_JWT ){
            string jwtToken = runtime:getInvocationContext().authContext.authToken;
            string currentAPIContext = getContext(filterContext);
            match getEncodedJWTPayload(jwtToken) {
                string  jwtPayload => {
                    match getDecodedJWTPayload(jwtPayload) {
                        json decodedPayload => {
                            json subscribedAPIList = decodedPayload.subscribedAPIs;
                            int numOfSubscriptions = lengthof subscribedAPIList;
                            int count =0;
                            while(count < numOfSubscriptions) {
                                if(subscribedAPIList[count].context.toString() == currentAPIContext ) {
                                    return createFilterResult(true, 200, "Successfully validated subscriptions");
                                }
                                count++;
                            }
                            return createFilterResult(false, 403, "Subscription validation failed");
                        }
                        error err => {
                            log:printError("Error while decoding jwt token with payload : " +
                                    jwtPayload, err = err);
                            return createFilterResult(false, 500, "Error while decoding jwt token with payload : " +
                                    jwtPayload);
                        }
                    }
                }
                error err => {
                    log:printError(err.message);
                    return createFilterResult(false, 403, err.message);
                }
            }
        }
        return createFilterResult(true, 200, "Successfully validated subscriptions");
    }

};
