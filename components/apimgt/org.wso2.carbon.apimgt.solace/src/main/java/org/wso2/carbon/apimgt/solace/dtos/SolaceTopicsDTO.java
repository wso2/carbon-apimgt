/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.apimgt.solace.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is th DTO of Solace Topic Object
 */
public class SolaceTopicsDTO {
    private List<String> publishTopics = new ArrayList<String>();
    private List<String> subscribeTopics = new ArrayList<String>();

    public List<String> getPublishTopics() {
        return publishTopics;
    }

    public void setPublishTopics(List<String> publishTopics) {
        this.publishTopics = publishTopics;
    }

    public List<String> getSubscribeTopics() {
        return subscribeTopics;
    }

    public void setSubscribeTopics(List<String> subscribeTopics) {
        this.subscribeTopics = subscribeTopics;
    }
}
