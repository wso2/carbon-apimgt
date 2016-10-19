/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.msf4j.MicroservicesRunner;

/**
 * Application entry point.
 *
 * @since 1.0.0-SNAPSHOT
 */
public class Application {
    public static void main(String[] args) {
        new MicroservicesRunner(8080)
                .deploy(new ApisApi()).deploy(new ApplicationsApi())
                .start();
        /*new MicroservicesRunner(8081)
        .deploy(new ApplicationsApi())
        .start();
        new MicroservicesRunner()
        .deploy(new EnvironmentsApi())
        .start();
        new MicroservicesRunner()
        .deploy(new SubscriptionsApi())
        .start();*/
    }
}
