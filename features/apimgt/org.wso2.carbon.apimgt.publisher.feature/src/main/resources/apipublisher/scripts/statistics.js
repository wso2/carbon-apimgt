/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

//TODO add proper introduction to the module
var statistics = {};

(function (statistics) {
    var log = new Log("jaggery-modules.api-manager.publisher-statistics");
    var APIPublisherUsageServiceImpl = Packages.org.wso2.carbon.apimgt.usage.client.service.impl.APIPublisherUsageServiceImpl;

    function APIPublisherUsage(username) {
        this.username = username;
        this.impl = new APIPublisherUsageServiceImpl(this.username);
    }

    statistics.instance = function (username) {
        return new APIPublisherUsage(username);
    };

    APIPublisherUsage.prototype.getProviderAPIVersionUsage = function (providerName, apiName) {
        var usage = [];
        var providerVersionUsages;
        try {
            providerVersionUsages = this.impl.getProviderAPIVersionUsage(providerName, apiName);

            for (var i = 0 ; i < providerVersionUsages.size(); i++) {
                var providerVersionUsage = providerVersionUsages.get(i);
                usage.push({
                               "version":providerVersionUsage.getApiVersion(),
                               "count": providerVersionUsage.getCount()
                           });
            }

            if (log.isDebugEnabled()) {
                log.debug("getProviderAPIVersionUsage for" + providerName);
            }

            return {
                error: false,
                usage: usage
            };
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }

    };

    APIPublisherUsage.prototype.getSubscriberCountByAPIVersions = function (apiName, providerName) {
        var usage = [];
        var subscriberCounts;
        try {
            subscriberCounts = this.impl.getSubscriberCountByAPIVersions(apiName, providerName);

            for (var i = 0 ; i < subscriberCounts.size(); i++) {
                var subscriberCount = subscriberCounts.get(i);
                usage.push({
                               "apiVersion":subscriberCount.getApiVersion(),
                               "count": subscriberCount.getCount()
                           });
            }

            if (log.isDebugEnabled()) {
                log.debug("getSubscriberCountByAPIVersions for : " + apiName);
            }
            return {
                error: false,
                usage: usage
            };
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getProviderAPIUsage = function (providerName, from, to) {
        var usage = [];
        var usageout = [];
        try {
            usage = this.impl.getProviderAPIUsage(providerName, from, to);
            if (log.isDebugEnabled()) {
                log.debug("getProviderAPIUsage for : " + server);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {

                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getProviderAPIUserUsage = function (providerName, apiName) {
        var usage = [];
        var providerUsages;
        try {
            providerUsages = this.impl.getProviderAPIUserUsage(providerName, apiName);

            for (var i = 0 ; i < providerUsages.size(); i++) {
                var providerUsage = providerUsages.get(i);
                usage.push({
                               "apiName":providerUsage.getApiName(),
                               "count": providerUsage.getCount()
                           });
            }

            if (log.isDebugEnabled()) {
                log.debug("getProviderAPIUserUsage for : " + server);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {
                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getAPIUsageByResourcePath = function (providerName, from, to) {
        var usage = [];
        var usageout = [];
        try {
            usage = this.impl.getAPIUsageByResourcePath(providerName, from, to);
            if (log.isDebugEnabled()) {
                log.debug("getAPIUsageByResourcePath for : " + providerName);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {

                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getAPIUsageByDestination = function (providerName, from, to) {
        var usage = [];
        var usageout = [];
        try {
            usage = this.impl.getAPIUsageByDestination(providerName, from, to);
            if (log.isDebugEnabled()) {
                log.debug("getAPIUsageByDestination for : " + providerName);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {

                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getAPIUsageByUser = function (providerName, from, to) {
        var usage = [];
        var usageout = [];
        try {
            usage = this.impl.getAPIUsageByUser(providerName, from, to);
            if (log.isDebugEnabled()) {
                log.debug("getAPIUsageByUser for : " + providerName);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {

                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getProviderAPIVersionUserLastAccess = function (providerName, from, to) {
        var usage = [];
        try {
            usage = this.impl.getProviderAPIVersionUserLastAccess(providerName, from, to);

            if (log.isDebugEnabled()) {
                log.debug("getProviderAPIVersionUserLastAccess for : " + server);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {
                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getProviderAPIServiceTime = function (providerName, from, to) {
        var usage = [];
        var usageout = [];
        try {
            usage = this.impl.getProviderAPIServiceTime(providerName, from, to);
            if (log.isDebugEnabled()) {
                log.debug("getProviderAPIServiceTime for : " + server);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {

                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getSubscriberCountByAPIs = function (providerName) {
        var usage = [];
        var usageout = [];
        try {
            usage = this.impl.getSubscriberCountByAPIs(providerName);
            if (log.isDebugEnabled()) {
                log.debug("getSubscriberCountByAPIs for : " + providerName);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {
                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getProviderAPIVersionUserUsage = function (providerName, apiName, version) {
        var usage = [];
        var providerUsageByVersions;
        try {
            providerUsageByVersions = this.impl.getProviderAPIVersionUserUsage(providerName, apiName, version);

            for (var i = 0 ; i < providerUsageByVersions.size(); i++) {
                var providerUsageByVersion = providerUsageByVersions.get(i);
                usage.push({
                               "user":providerUsageByVersion.getUsername(),
                               "count": providerUsageByVersion.getCount()
                           });
            }

            if (log.isDebugEnabled()) {
                log.debug("getProviderAPIVersionUserUsage for : " + providerName);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {
                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getAPIResponseFaultCount = function (providerName, from, to) {
        var usage = [];
        var usageout = [];
        try {
            usage = this.impl.getAPIResponseFaultCount(providerName, from, to);
            if (log.isDebugEnabled()) {
                log.debug("getAPIResponseFaultCount for : " + providerName);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {
                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getAPIFaultyAnalyzeByTime = function (providerName) {
        var usage = [];
        var usageout = [];
        try {
            usage = this.impl.getAPIFaultyAnalyzeByTime(providerName);
            if (log.isDebugEnabled()) {
                log.debug("getAPIFaultyAnalyzeByTime for : " + providerName);
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {
                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getFirstAccessTime = function (providerName) {
        var usage = [];
        var usageout = [];
        try {
            usage = this.impl.getFirstAccessTime(providerName);
            if (log.isDebugEnabled()) {
                log.debug("getProviderAPIFirstAccessTime");
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {

                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.isDataPublishingEnabled = function (providerName) {
        var usage = [];
        var usageout = [];
        try {
            usage = this.impl.isDataPublishingEnabled();
            if (log.isDebugEnabled()) {
                log.debug("isDataPublishingEnabled check");
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {

                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getUserAgentSummaryForALLAPIs = function () {
        var usage = [];
        try {
            usage = this.impl.getUserAgentSummaryForALLAPIs();
            if (log.isDebugEnabled()) {
                log.debug("getUserAgentSummaryForALLAPIs check");
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {
                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getAPIRequestsPerHour = function (from, to, api) {
        var usage = [];
        try {
            usage = this.impl.getAPIRequestsPerHour(from, to, api);
            if (log.isDebugEnabled()) {
                log.debug("getAPIRequestsPerHour check");
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {
                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    };

    APIPublisherUsage.prototype.getAPIsFromAPIRequestsPerHourTable = function (from, to) {
        var usage = [];
        try {
            usage = this.impl.getAPIsFromAPIRequestsPerHourTable(from, to);
            if (log.isDebugEnabled()) {
                log.debug("getAPIRequestsPerHour check");
            }
            if (usage == null) {
                return {
                    error: true
                };

            } else {
                return {
                    error: false,
                    usage: usage
                };
            }
        } catch (e) {
            log.error(e.message);
            return {
                error: e,
                usage: null

            };
        }
    }
})(statistics);

