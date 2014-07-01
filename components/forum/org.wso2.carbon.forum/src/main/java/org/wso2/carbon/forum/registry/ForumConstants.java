/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.forum.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForumConstants {

    public static final String OVERVIEW_TOPIC_ID = "overview_id";

    public static final String OVERVIEW_SUBJECT = "overview_title";

    public static final String OVERVIEW_TOPIC_OWNER = "overview_owner";

    public static final String OVERVIEW_TOPIC_OWNER_TENANT_DOMAIN = "overview_tenantDomain";

    public static final String OVERVIEW_RESOURCE_IDENTIFIER = "overview_location";

    public static final String OVERVIEW_CREATED_DATE = "overview_createdDate";

    public static final String OVERVIEW_REPLY_ID = "overview_id";

    public static final String OVERVIEW_REPLY_TOPIC_ID = "overview_topicId";

    public static final String OVERVIEW_CREATED_BY = "overview_createdBy";

    public static final String OVERVIEW_CREATOR_TENANT_DOMAIN = "overview_tenantDomain";

    public static final String OVERVIEW_REPLY_TIMESTAMP = "overview_timestamp";

    public static final String FORUM_DATE_FORMAT = "yyyy/MM/dd";

    public static final String FORUM_DATE_TIME_FORMAT = "yyyy/MM/dd:HH/mm/ss";

    public static final String OVERVIEW_REPLY_COUNT = "overview_replyCount";

    public static final String OVERVIEW_LAST_REPLY_BY = "overview_lastReplyBy";

    public static final String OVERVIEW_LAST_REPLY_TIMESTAMP = "overview_lastReplyTimestamp";

    public static final String OVERVIEW_TOPIC_TIMESTAMP = "overview_timestamp";

    public static void main(String args[]){

        List<String> dates = new ArrayList<String>();
        dates.add("2014/05/01:14/22");
        dates.add("2014/05/01:14/21");
        dates.add("2014/05/01:14/19");
        dates.add("2014/10/01:13/55");
        dates.add("2014/12/01:13/55");

        Collections.sort(dates);
        for(String date : dates){
            System.out.println(date);
        }
    }

}
