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
package org.wso2.carbon.apimgt.usage.client.bean;

public class ExecutionTimeOfAPIValues {
    private String api;
    private String version;
    private String time;
    private String mediationName;
    private String apiPublisher;
    private String context;
    private int year;
    private int hour;
    private int month;
    private int day;
    private int minutes;
    private int seconds;
    private long executionTime;

    private long apiResponseTime;

    private long securityLatency;

    private long throttlingLatency;

    private long requestMediationLatency;

    private long responseMediationLatency;

    private long backendLatency;

    private long otherLatency;

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMediationName() {
        return mediationName;
    }

    public void setMediationName(String mediationName) {
        this.mediationName = mediationName;
    }

    public String getApiPublisher() {
        return apiPublisher;
    }

    public void setApiPublisher(String apiPublisher) {
        this.apiPublisher = apiPublisher;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
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

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public long getApiResponseTime() {
        return apiResponseTime;
    }

    public void setApiResponseTime(long apiResponseTime) {
        this.apiResponseTime = apiResponseTime;
    }

    public long getSecurityLatency() {
        return securityLatency;
    }

    public void setSecurityLatency(long securityLatency) {
        this.securityLatency = securityLatency;
    }

    public long getThrottlingLatency() {
        return throttlingLatency;
    }

    public void setThrottlingLatency(long throttlingLatency) {
        this.throttlingLatency = throttlingLatency;
    }

    public long getRequestMediationLatency() {
        return requestMediationLatency;
    }

    public void setRequestMediationLatency(long requestMediationLatency) {
        this.requestMediationLatency = requestMediationLatency;
    }

    public long getResponseMediationLatency() {
        return responseMediationLatency;
    }

    public void setResponseMediationLatency(long responseMediationLatency) {
        this.responseMediationLatency = responseMediationLatency;
    }

    public long getBackendLatency() {
        return backendLatency;
    }

    public void setBackendLatency(long backendLatency) {
        this.backendLatency = backendLatency;
    }

    public long getOtherLatency() {
        return otherLatency;
    }

    public void setOtherLatency(long otherLatency) {
        this.otherLatency = otherLatency;
    }
}
