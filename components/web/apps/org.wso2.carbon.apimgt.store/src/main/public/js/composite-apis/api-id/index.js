$(function () {
    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first");
    $(".blue").insertBefore(prev).css('top','0px').addClass('active');

    $.fn.editable.defaults.mode = 'inline';
    var $apiId = $("#apiId").val();

    var swaggerClient = new SwaggerClient({
        url: swaggerURL,
        success: function (swaggerData) {
            setAuthHeader(swaggerClient);
            swaggerClient["CompositeAPI (Individual)"]
            .get_composite_apis_apiId({"apiId": $apiId}, {"responseContentType": 'application/json'},
            function (jsonData) {
                var api = jsonData.obj;
                api.isStandardAPI = false;
                var mode = "OVERWRITE";
                var headerCallbacks = {
                    onSuccess: function () {},
                    onFailure: function (message, e) {
                        var message = "Error occurred while loading api details";
                        noty({
                            text: message,
                            type: 'error',
                            dismissQueue: true,
                            modal: true,
                            progressBar: true,
                            timeout: 2000,
                            layout: 'top',
                            theme: 'relax',
                            maxVisible: 10,
                        });
                    }
                };

                var infoCallbacks = {
                    onSuccess: function () {
                        $(".setbgcolor").generateBgcolor({
                            definite:true
                        });

                        $(".api-name-icon").each(function() {
                            var $elem = $(this).next().children(".api-name");
                            $(this).nametoChar({
                                nameElement: $elem
                            });
                        });
                    },
                    onFailure: function (message, e) {
                        var message = "Error occurred while loading api details";
                        noty({
                            text: message,
                            type: 'error',
                            dismissQueue: true,
                            modal: true,
                            progressBar: true,
                            timeout: 2000,
                            layout: 'top',
                            theme: 'relax',
                            maxVisible: 10,
                        });
                    }
                }

                var subscriptionCallbacks = {
                    onSuccess: function () {},
                    onFailure: function (message, e) {
                        var message = "Error occurred while loading subscription details";
                        noty({
                            text: message,
                            type: 'error',
                            dismissQueue: true,
                            modal: true,
                            progressBar: true,
                            timeout: 2000,
                            layout: 'top',
                            theme: 'relax',
                            maxVisible: 10,
                        });
                    }
                };

                // Render API header
                UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-header", api, "api-header", mode,
                headerCallbacks);

                // Render API information
                UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-info", api, "api-info", mode,
                infoCallbacks);

                var tabs = [];
                tabs.push({
                    "title": "Resources",
                    "id": "api-resources",
                    "body": [
                        {
                            "inputs": {
                                "api": {}
                            }
                        }
                    ]
                });

                tabs[0].tabClass = "first active";
                tabs[0].contentClass = "in active";

                var context = {};
                context.tabList = tabs;
                var tabListCallbacks = {
                    onSuccess: function (tabData) {
                        $("#tab-list").append(tabData);
                        for (var i = 0; i < tabs.length; i++) {
                            var tab = tabs[i];
                            if (tab.id == "api-resources") {
                                var data = {
                                    name: api.name,
                                    id: $apiId,
                                    isStandardAPI: false,
                                    version: api.version,
                                    context: api.context,
                                    verbs: [ 'get' , 'post' , 'put' , 'delete', 'patch', 'head']
                                };
                                var apiResourcesCallbacks = {
                                    onSuccess: function (tabContent) {
                                        $("#api-resources").append(tabContent);
                                        $.fn.editable.defaults.mode = 'inline';
                                        var _this = this;

                                    	setAuthHeader(this.client);
                                        this.client["CompositeAPI (Individual)"].get_composite_apis_apiId_swagger({"apiId":$apiId},
                                        function(response) {
                                            var apiDoc = response.obj;
                                            var designer = new APIDesigner();
                                            designer.initControllersCall = "";
                                            designer.load_api_document(apiDoc);

                                            // remove registered listener and add new listener for resource save button
                                            $("#save_resources").off("click").click(function() {
                                                var designer = APIDesigner();
                                                _this.client["CompositeAPI (Individual)"].put_composite_apis_apiId_swagger(
                                                {
                                                    "apiId":$apiId,
                                                    "apiDefinition": JSON.stringify(designer.api_doc),
                                                    "Content-Type": "application/json"
                                                }, function (response) {
                                                    api_doc_local = response.obj;
                                                    var designer = new APIDesigner();
                                                    designer.api_doc = api_doc_local;
                                                    designer.initControllersCall = "";
                                                    designer.render_resources();
                                                    var message = "API Resources saved successfully.";
                                                    noty({
                                                        text: message,
                                                        type: 'success',
                                                        dismissQueue: true,
                                                        progressBar: true,
                                                        timeout: 5000,
                                                        layout: 'topCenter',
                                                        theme: 'relax',
                                                        maxVisible: 10
                                                    });
                                                }, function (e) {
                                                    var message;
                                                    if (e.data) {
                                                        message = "Error[" + e.status + "]: " + e.data;
                                                    } else {
                                                        message = e;
                                                    }
                                                    noty({
                                                        text: message,
                                                        type: 'error',
                                                        dismissQueue: true,
                                                        modal: true,
                                                        progressBar: true,
                                                        timeout: 5000,
                                                        layout: 'top',
                                                        theme: 'relax',
                                                        maxVisible: 10
                                                    });
                                                });
                                            });
                                        }, function() {throw "Error occurred while loading api resources"});
                                    },
                                    onFailure: function (message, e) {
                                        var msg = "Error occurred while loading api resource details";
                                        noty({
                                            text: msg,
                                            type: 'error',
                                            dismissQueue: true,
                                            modal: true,
                                            progressBar: true,
                                            timeout: 2000,
                                            layout: 'top',
                                            theme: 'relax',
                                            maxVisible: 10,
                                        });
                                    },
                                    client: swaggerClient
                                };

                                UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-resources",
                                data, apiResourcesCallbacks);
                            } else if (tab.id == "api-implementation") {
                                UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.composite-api-implementation",
                                data, {onSuccess: function (tabContent) {$("#api-implementation").append(tabContent);}, onFailure: function(m, e){}});
                            }
                        }
                    },
                    onFailure: function (message, e) {
                        var msg = "Error occurred while loading api details";
                        noty({
                            text: msg,
                            type: 'error',
                            dismissQueue: true,
                            modal: true,
                            progressBar: true,
                            timeout: 2000,
                            layout: 'top',
                            theme: 'relax',
                            maxVisible: 10,
                        });
                    }
                }

                // Render composite API information tabs
                UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-detail-tab-list",
                context, tabListCallbacks);
            },
            function (jqXHR, textStatus, errorThrown) {
                var msg = "Error occurred while retrieve composite api with id  : " + $apiId;
                noty({
                    text: msg,
                    type: 'error',
                    dismissQueue: true,
                    modal: true,
                    progressBar: true,
                    timeout: 2000,
                    layout: 'top',
                    theme: 'relax',
                    maxVisible: 10,
                });
                if(jqXHR.status==401){
                    redirectToLogin(contextPath);
                }
            });
        },
        failure: function (error) {
            console.log("Error occurred while loading swagger definition");
        }
    });

});