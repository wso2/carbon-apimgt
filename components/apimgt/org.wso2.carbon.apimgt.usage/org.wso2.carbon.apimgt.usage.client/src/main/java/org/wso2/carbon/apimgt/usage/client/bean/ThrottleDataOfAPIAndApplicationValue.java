/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.usage.client.bean;

public class ThrottleDataOfAPIAndApplicationValue {
    private String api;
    private String api_version;
    private String context;
    private String apiPublisher;
    private String applicationName;
    private String tenantDomain;
    private int year;
    private int month;
    private int day;
    private int week;
    private String time;
    private int success_request_count;
    private int throttleout_count;
    private long max_request_time;

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getApi_version() {
        return api_version;
    }

    public void setApi_version(String api_version) {
        this.api_version = api_version;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getApiPublisher() {
        return apiPublisher;
    }

    public void setApiPublisher(String apiPublisher) {
        this.apiPublisher = apiPublisher;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSuccess_request_count() {
        return success_request_count;
    }

    public void setSuccess_request_count(int success_request_count) {
        this.success_request_count = success_request_count;
    }

    public int getThrottleout_count() {
        return throttleout_count;
    }

    public void setThrottleout_count(int throttleout_count) {
        this.throttleout_count = throttleout_count;
    }

    public long getMax_request_time() {
        return max_request_time;
    }

    public void setMax_request_time(long max_request_time) {
        this.max_request_time = max_request_time;
    }
}
