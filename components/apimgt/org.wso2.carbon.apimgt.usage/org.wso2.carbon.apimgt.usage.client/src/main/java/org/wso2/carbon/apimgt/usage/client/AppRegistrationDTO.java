/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.usage.client;
/**
 * This class is used as a DTO to represent develoers over time
 */
public class AppRegistrationDTO {

    private long x;
    private long y;

    public long getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }



    AppRegistrationDTO(long x, long y){

        this.x = x;
        this.y = y;
    }


    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }
}
