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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var loganalyzerDashboard = {
    "id": "log-analyzer",
        "title": "Log Analyzer for APIM",
        "description": "",
        "permissions": {
        "viewers": ["Internal/everyone"],
            "editors": ["Internal/everyone"]
    },
    "pages": [{
        "id": "page0",
        "title": "Overview",
        "layout": {
            "content": {
                "loggedIn": {
                    "blocks": [{
                        "id": "overview-date-range-picker",
                        "x": 0,
                        "y": 0,
                        "width": 12,
                        "height": 2,
                        "banner": false
                    },
                        {
                            "id": "overview-log-event-chart",
                            "x": 0,
                            "y": 2,
                            "width": 12,
                            "height": 4,
                            "banner": false
                        }]
                }
            },
            "fluidLayout": false
        },
        "isanon": false,
        "content": {
            "default": {
                "overview-date-range-picker": [{
                    "id": "Date_Range_Picker-overview",
                    "content": {
                        "id": "Date_Range_Picker",
                        "title": "Date Range Picker",
                        "type": "gadget",
                        "thumbnail": "store://gadget/Date_Range_Picker/index.png",
                        "data": {"url": "store://gadget/Date_Range_Picker/index.xml"},
                        "notify": {
                            "range-selected": {
                                "type": "date-range",
                                "description": "This notifies selected state"
                            }
                        },
                        "listen": {
                            "chart-zoomed": {
                                "type": "date-range",
                                "description": "This notifies message generated in publisher"
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Date Range Picker"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Date Range Picker"},
                        "settings": {}
                    }
                }],
                "overview-log-event-chart": [{
                    "id": "logEvents-overview",
                    "content": {
                        "id": "logEvents",
                        "title": "Log Event Chart",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogEvents/img/thumbnail.png",
                        "data": {"url": "store://gadget/LogEvents/index.xml"},
                        "listen": {
                            "subscriber": {
                                "type": "date-range",
                                "description": "Used to listen to any date range-selected",
                                "on": [{
                                    "from": "Date_Range_Picker-overview",
                                    "event": "range-selected"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Log Events"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Log Events"},
                        "settings": {}
                    }
                }]
            }, "anon": {}
        }
    }, {
        "id": "page1",
        "title": "Application Errors",
        "layout": {
            "content": {
                "loggedIn": {
                    "blocks": [{
                        "id": "application-errors-date-range-picker",
                        "x": 0,
                        "y": 0,
                        "width": 12,
                        "height": 2,
                        "banner": false
                    }, {
                        "id": "application-errors-log-error-bar-chart",
                        "x": 0,
                        "y": 2,
                        "width": 12,
                        "height": 6,
                        "banner": false
                    }, {
                        "id": "application-errors-filtered-message",
                        "x": 0,
                        "y": 8,
                        "width": 12,
                        "height": 7,
                        "banner": false
                    }, {
                        "id": "application-errors-log-viewer",
                        "x": 0,
                        "y": 15,
                        "width": 12,
                        "height": 6,
                        "banner": false
                    }]
                }
            }, "fluidLayout": false
        },
        "isanon": false,
        "content": {
            "default": {
                "application-errors-date-range-picker": [{
                    "id": "Date_Range_Picker-application-errors",
                    "content": {
                        "id": "Date_Range_Picker",
                        "title": "Date Range Picker",
                        "type": "gadget",
                        "thumbnail": "store://gadget/Date_Range_Picker/index.png",
                        "data": {"url": "store://gadget/Date_Range_Picker/index.xml"},
                        "notify": {
                            "range-selected": {
                                "type": "date-range",
                                "description": "This notifies selected state"
                            }
                        },
                        "listen": {
                            "chart-zoomed": {
                                "type": "date-range",
                                "description": "This notifies message generated in publisher"
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Date Range Picker"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Date Range Picker"},
                        "settings": {}
                    }
                }],
                "application-errors-log-error-bar-chart": [{
                    "id": "LogErrorBarChart-application-errors",
                    "content": {
                        "id": "LogErrorBarChart",
                        "title": "Log Error Bar Chart",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogErrorBarChart/img/thumbnail.png",
                        "data": {"url": "store://gadget/LogErrorBarChart/index.xml"},
                        "notify": {
                            "publisher": {
                                "type": "filter",
                                "description": "This notifies selected filter"
                            }
                        },
                        "listen": {
                            "subscriber": {
                                "type": "date-range",
                                "description": "Used to listen to any date range-selected",
                                "on": [{
                                    "from": "Date_Range_Picker-application-errors",
                                    "event": "range-selected"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Errors Distribution"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Errors Distribution"},
                        "settings": {}
                    }
                }],
                "application-errors-filtered-message": [{
                    "id": "filteredLogMessages-application-errors",
                    "content": {
                        "id": "filteredLogMessages",
                        "title": "APIM Filtered Messages",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogErrorFilteredMessage/img/thumbnail.png",
                        "data": {"url": "store://gadget/LogErrorFilteredMessage/index.xml"},
                        "notify": {
                            "publisher": {
                                "type": "filter-range",
                                "description": "This notifies selected filter"
                            }
                        },
                        "listen": {
                            "subscriber": {
                                "type": "filter",
                                "description": "Used to listen to any filters",
                                "on": [{
                                    "from": "LogErrorBarChart-application-errors",
                                    "event": "publisher"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Filtered Messages"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Filtered Messages"},
                        "settings": {}
                    }
                }],
                "application-errors-log-viewer": [{
                    "id": "logViewer-application-errors",
                    "content": {
                        "id": "logViewer",
                        "title": "Log Viewer",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogViewer/index.png",
                        "data": {"url": "store://gadget/LogViewer/index.xml"},
                        "listen": {
                            "subscriber": {
                                "type": "filter-range",
                                "description": "Used to listen to any filters",
                                "on": [{
                                    "from": "filteredLogMessages-application-errors",
                                    "event": "publisher"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Log Viewer"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Log Viewer"},
                        "settings": {}
                    }
                }]
            }, "anon": {}
        }
    }, {
        "id": "page2",
        "title": "Artifact Deployment Stats",
        "layout": {
            "content": {
                "loggedIn": {
                    "blocks": [{
                        "id": "artifact-deployment-stats-date-range-picker",
                        "x": 0,
                        "y": 0,
                        "width": 12,
                        "height": 2,
                        "banner": false
                    }, {
                        "id": "artifact-deployment-stats-artifact-deployment",
                        "x": 0,
                        "y": 2,
                        "width": 6,
                        "height": 6,
                        "banner": false
                    }, {
                        "id": "artifact-deployment-stats-artifact-deleted",
                        "x": 6,
                        "y": 2,
                        "width": 6,
                        "height": 6,
                        "banner": false
                    }]
                }
            }, "fluidLayout": false
        },
        "isanon": false,
        "content": {
            "default": {
                "artifact-deployment-stats-date-range-picker": [{
                    "id": "Date_Range_Picker-artifact-deployment-stats",
                    "content": {
                        "id": "Date_Range_Picker",
                        "title": "Date Range Picker",
                        "type": "gadget",
                        "category": "Widgets",
                        "thumbnail": "store://gadget/Date_Range_Picker/index.png",
                        "data": {"url": "store://gadget/Date_Range_Picker/index.xml"},
                        "notify": {
                            "range-selected": {
                                "type": "date-range",
                                "description": "This notifies selected state"
                            }
                        },
                        "listen": {
                            "chart-zoomed": {
                                "type": "date-range",
                                "description": "This notifies message generated in publisher"
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Date Range Picker"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Date Range Picker"},
                        "settings": {}
                    }
                }],
                "artifact-deployment-stats-artifact-deployment": [{
                    "id": "logAPIMArtifactDeployed-artifact-deployment-stats",
                    "content": {
                        "id": "logAPIMArtifactDeployed",
                        "title": "Deployed Artifacts",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogAPIMArtifactDeployed/img/thumbnail.png",
                        "data": {"url": "store://gadget/LogAPIMArtifactDeployed/index.xml"},
                        "listen": {
                            "subscriber": {
                                "type": "date-range",
                                "description": "Used to listen to any date range-selected",
                                "on": [{
                                    "from": "Date_Range_Picker-artifact-deployment-stats",
                                    "event": "range-selected"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Deployed Artifacts"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Deployed Artifacts"},
                        "settings": {}
                    }
                }],
                "artifact-deployment-stats-artifact-deleted": [{
                    "id": "logAPIMArtifactDeleted-artifact-deployment-stats",
                    "content": {
                        "id": "logAPIMArtifactDeleted",
                        "title": "APIM Artifact Deleted",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogAPIMArtifactDeleted/img/thumbnail.png",
                        "data": {"url": "store://gadget/LogAPIMArtifactDeleted/index.xml"},
                        "listen": {
                            "subscriber": {
                                "type": "date-range",
                                "description": "Used to listen to any date range-selected",
                                "on": [{
                                    "from": "Date_Range_Picker-artifact-deployment-stats",
                                    "event": "range-selected"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Deleted Artifacts"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Deleted Artifacts"},
                        "settings": {}
                    }
                }]
            }, "anon": {}
        }
    }, {
        "id": "page3",
        "title": "Login Stats",
        "layout": {
            "content": {
                "loggedIn": {
                    "blocks": [{
                        "id": "logging-stats-date-range-picker",
                        "x": 0,
                        "y": 0,
                        "width": 12,
                        "height": 2,
                        "banner": false
                    }, {
                        "id": "logging-stats-invalid-logging-count",
                        "x": 0,
                        "y": 2,
                        "width": 12,
                        "height": 5,
                        "banner": false
                    }]
                }
            }, "fluidLayout": false
        },
        "isanon": false,
        "content": {
            "default": {
                "logging-stats-date-range-picker": [{
                    "id": "Date_Range_Picker-logging-stats",
                    "content": {
                        "id": "Date_Range_Picker",
                        "title": "Date Range Picker",
                        "type": "gadget",
                        "category": "Widgets",
                        "thumbnail": "store://gadget/Date_Range_Picker/index.png",
                        "data": {"url": "store://gadget/Date_Range_Picker/index.xml"},
                        "notify": {
                            "range-selected": {
                                "type": "date-range",
                                "description": "This notifies selected state"
                            }
                        },
                        "listen": {
                            "chart-zoomed": {
                                "type": "date-range",
                                "description": "This notifies message generated in publisher"
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Date Range Picker"
                        },
                        "options": {},
                        "locale_titles": {}
                    }
                }],
                "logging-stats-invalid-logging-count": [{
                    "id": "invalidLoggingCount-logging-stats",
                    "content": {
                        "id": "invalidLoggingCount",
                        "title": "Invalid Login Count",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogInvalidLoggingCount/img/thumbnail.png",
                        "data": {"url": "store://gadget/LogInvalidLoggingCount/index.xml"},
                        "notify": {
                            "publisher": {
                                "type": "filter",
                                "description": "This notifies selected filter"
                            }
                        },
                        "listen": {
                            "subscriber": {
                                "type": "date-range",
                                "description": "Used to listen to any date range-selected",
                                "on": [{
                                    "from": "Date_Range_Picker-logging-stats",
                                    "event": "range-selected"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Invalid Login Count"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Invalid Login Count"},
                        "settings": {}
                    }
                }]
            }, "anon": {}
        }
    }, {
        "id": "page4",
        "title": "Number of API Failures",
        "layout": {
            "content": {
                "loggedIn": {
                    "blocks": [{
                        "id": "number-of-failures-date-range-picker",
                        "x": 0,
                        "y": 0,
                        "width": 12,
                        "height": 2,
                        "banner": false
                    }, {
                        "id": "number-of-failures-message-processing",
                        "x": 0,
                        "y": 2,
                        "width": 12,
                        "height": 5,
                        "banner": false
                    }]
                }
            }, "fluidLayout": false
        },
        "isanon": false,
        "content": {
            "default": {
                "number-of-failures-date-range-picker": [{
                    "id": "Date_Range_Picker-number-of-failures",
                    "content": {
                        "id": "Date_Range_Picker",
                        "title": "Date Range Picker",
                        "type": "gadget",
                        "category": "Widgets",
                        "thumbnail": "store://gadget/Date_Range_Picker/index.png",
                        "data": {"url": "store://gadget/Date_Range_Picker/index.xml"},
                        "notify": {
                            "range-selected": {
                                "type": "date-range",
                                "description": "This notifies selected state"
                            }
                        },
                        "listen": {
                            "chart-zoomed": {
                                "type": "date-range",
                                "description": "This notifies message generated in publisher"
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Date Range Picker"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Date Range Picker"},
                        "settings": {}
                    }
                }],
                "number-of-failures-message-processing": [{
                    "id": "logAPIMMessageProcessing-number-of-failures",
                    "content": {
                        "id": "logAPIMMessageProcessing",
                        "title": "ERRORs in API Message Processing",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogAPIMMessageProcessing/img/thumbnail.png",
                        "data": {"url": "store://gadget/LogAPIMMessageProcessing/index.xml"},
                        "listen": {
                            "subscriber": {
                                "type": "date-range",
                                "description": "Used to listen to any date range-selected",
                                "on": [{
                                    "from": "Date_Range_Picker-number-of-failures",
                                    "event": "range-selected"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "ERRORs in API Message Processing"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "ERRORs in API Message Processing"},
                        "settings": {}
                    }
                }]
            }, "anon": {}
        }
    }, {
        "id": "page5",
        "title": "Access Token Errors",
        "layout": {
            "content": {
                "loggedIn": {
                    "blocks": [{
                        "id": "access-token-errors-date-range-picker",
                        "x": 0,
                        "y": 0,
                        "width": 12,
                        "height": 2,
                        "banner": false
                    }, {
                        "id": "access-token-errors-table",
                        "x": 0,
                        "y": 6,
                        "width": 12,
                        "height": 4,
                        "banner": false
                    }, {
                        "id": "access-token-errors-chart",
                        "x": 0,
                        "y": 2,
                        "width": 6,
                        "height": 4,
                        "banner": false
                    }, {
                        "id": "access-token-errors-log-viewer",
                        "x": 0,
                        "y": 10,
                        "width": 12,
                        "height": 4,
                        "banner": false
                    }]
                }
            }, "fluidLayout": false
        },
        "isanon": false,
        "content": {
            "default": {
                "access-token-errors-date-range-picker": [{
                    "id": "Date_Range_Picker-access-token-errors",
                    "content": {
                        "id": "Date_Range_Picker",
                        "title": "Date Range Picker",
                        "type": "gadget",
                        "category": "Widgets",
                        "thumbnail": "store://gadget/Date_Range_Picker/index.png",
                        "data": {"url": "store://gadget/Date_Range_Picker/index.xml"},
                        "notify": {
                            "range-selected": {
                                "type": "date-range",
                                "description": "This notifies selected state"
                            }
                        },
                        "listen": {
                            "chart-zoomed": {
                                "type": "date-range",
                                "description": "This notifies message generated in publisher"
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Date Range Picker"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "Date Range Picker"},
                        "settings": {}
                    }
                }],
                "access-token-errors-chart": [{
                    "id": "logApiTokenByStatus-access-token-errors",
                    "content": {
                        "id": "logApiTokenByStatus",
                        "title": "API Token Status",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogApiTokenByStatus/img/thumbnail.png",
                        "data": {"url": "store://gadget/LogApiTokenByStatus/index.xml"},
                        "notify": {
                            "publisher": {
                                "type": "api-key-filter",
                                "description": "This notifies selected API Key filter"
                            }
                        },
                        "listen": {
                            "subscriber": {
                                "type": "date-range",
                                "description": "Used to listen to any date range-selected",
                                "on": [{
                                    "from": "Date_Range_Picker-access-token-errors",
                                    "event": "range-selected"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "API Token Status"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "API Token Status"},
                        "settings": {}
                    }
                }],
                "access-token-errors-table": [{
                    "id": "logApiTokenStatusTable-access-token-errors",
                    "content": {
                        "id": "logApiTokenStatusTable-access-token-errors",
                        "title": "API Key Status Table",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogApiTokenByStatusTable/img/thumbnail.png",
                        "data": {"url": "store://gadget/LogApiTokenByStatusTable/index.xml"},
                        "notify": {
                            "publisher": {
                                "type": "filter-range",
                                "description": "This notifies the log time range selected"
                            }
                        },
                        "listen": {
                            "api-keys-subscriber": {
                                "type": "api-key-filter",
                                "description": "Used to listen to any filters",
                                "on": [{
                                    "from": "logApiTokenByStatus-access-token-errors",
                                    "event": "publisher"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "titlePosition": "left",
                            "title": "API Key Status"
                        },
                        "options": {},
                        "locale_titles": {"en-US": "API Key Status"},
                        "settings": {}
                    }
                }],
                "access-token-errors-log-viewer": [{
                    "id": "logViewer-access-token-errors",
                    "content": {
                        "id": "logViewer",
                        "title": "Log Viewer",
                        "type": "gadget",
                        "thumbnail": "store://gadget/LogViewer/index.png",
                        "data": {"url": "store://gadget/LogViewer/index.xml"},
                        "listen": {
                            "subscriber": {
                                "type": "filter-range",
                                "description": "Used to listen to any filters",
                                "on": [{
                                    "from": "logApiTokenStatusTable-access-token-errors",
                                    "event": "publisher"
                                }]
                            }
                        },
                        "styles": {
                            "no_heading": false,
                            "hide_gadget": false,
                            "borders": false,
                            "titlePosition": "left",
                            "title": "Log Viewer"
                        },
                        "locale_titles": {"en-US": "Log Viewer"},
                        "settings": {}
                    }
                }]
            }, "anon": {}
        }
    }],
        "identityServerUrl": "",
        "accessTokenUrl": "",
        "apiKey": "",
        "apiSecret": "",
        "theme": "",
        "isUserCustom": false,
        "isEditorEnable": true,
        "banner": {"globalBannerExists": false, "customBannerExists": false},
    "landing": "landing",
        "isanon": false
};