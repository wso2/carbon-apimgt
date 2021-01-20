/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */
package org.wso2.carbon.apimgt.api.model;

public class Mediation {

    private String uuid;
    private String name;
    private String type;
    private String config;
    private boolean isGlobal; 

    public Mediation(){}

    public void setUuid(String id){
        this.uuid=id;
    }
    public String getUuid(){return uuid;}

    public void setName(String name){this.name=name;}

    public String getName(){return name;}

    public void setType(String mType){this.type=mType;}

    public String getType(){return  type;}

    public void setConfig(String mConfig){this.config=mConfig;}

    public String getConfig(){return config;}

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }
}
