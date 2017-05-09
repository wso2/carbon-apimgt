$(function () {
    $.fn.editable.defaults.mode = 'inline';
    var apiId = $("#apiId").val();
    var swaggerClient = new SwaggerClient({
        url: swaggerURL,
        success: function (swaggerData) {
            var policies = [];

            setAuthHeader(swaggerClient);
            swaggerClient["API (individual)"].get_apis_apiId({"apiId": apiId},
                {"responseContentType": 'application/json'},
                function (jsonData) {
                    var api = jsonData.obj;
                    policies = api.policies;
                    var callbacks = {onSuccess: function () {},onFailure: function (message, e) {}};
                    var mode = "OVERWRITE";

                    //Render Api header
                    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-header",api,
                        "api-header", mode, callbacks);

                    //Render Api information
                    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-info",api,
                        "api-info", mode, {onSuccess: function () {
                            $(".setbgcolor").generateBgcolor({
                                definite:true
                            });

                            $(".api-name-icon").each(function() {
                                var elem = $(this).next().children(".api-name");
                                $(this).nametoChar({
                                    nameElement: elem
                                });
                            });
                        },onFailure: function (message, e) {}});

                    //Get application details
                    setAuthHeader(swaggerClient);
                    swaggerClient["Application Collection"].get_applications({},
                        function (jsonData) {
                            var applications = jsonData.obj.list;
                                swaggerClient["Subscription Collection"].get_subscriptions({
                                        "apiId": apiId,
                                        "applicationId": "",
                                        "responseContentType": 'application/json'
                                    }, requestMetaData(),
                                    function (jsonData) {
                                        var availableApplications = [], subscription = {};
                                        var subscriptions = jsonData.obj.list;
                                        var application = {};
                                        var subscribedApp = false;
                                        for (var i = 0; i < applications.length; i++) {
                                               subscribedApp = false;
                                                application = applications[i];
                                                if (application.lifeCycleStatus != "APPROVED") {
                                                    continue;
                                                }
                                                for (var j = 0; j < subscriptions.length; j++) {
                                                    subscription = subscriptions[j];
                                                    if (subscription.applicationId === application.applicationId) {
                                                        subscribedApp = true;
                                                        continue;
                                                    }
                                                }
                                                if(!subscribedApp) {
                                                    availableApplications.push(application);
                                                }
                                            }
                                        var context = {};
                                        context.applications = availableApplications;
                                        context.policies = policies;

                                        //Render api subscribe pane
                                        UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-subscribe",context,
                                            "api-subscribe", mode, callbacks);
                                    },
                                    function (error) {
                                        var message = "Error occurred while retrieve Applications";
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
                                        if(error.status==401){
                                            redirectToLogin(contextPath);
                                        }
                                    });
                        },
                        function (error) {
                            var message = "Error occurred while retrieve Applications";
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
                            if(error.status==401){
                                redirectToLogin(contextPath);
                            }
                        });

                    //TODO: Embed actual values
                    var isAPIConsoleEnabled = true;
                    var apiType = "REST";
                    var isForumEnabled = true;

                    var tabs = [];
                    tabs.push({
                        "title": "Overview",
                        "id": "api-overview",
                        "body": [
                            {
                                "inputs": {
                                    "api": {},
                                    "user": {}
                                }
                            }
                        ]
                    });
                    tabs.push({
                        "title": "Documentation",
                        "id": "api-documentation",
                        "body": [
                            {
                                "inputs": {
                                    "api": {}
                                }
                            }
                        ]
                    });
                    if (isAPIConsoleEnabled && apiType.toUpperCase() != "WS") {
                        tabs.push({
                            "title": "API Console",
                            "id": "api-swagger",
                            "body": [
                                {
                                    "inputs": {
                                        "api": {},
                                        "subscriptions": []
                                    }
                                }
                            ]
                        })

                    }

                    if (isForumEnabled) {
                        tabs.push({
                            "title": "Forum",
                            "id": "forum-list",
                            "body": [
                                {
                                    "inputs": {
                                        "api": {},
                                        "uriTemplates": {}
                                    }
                                }
                            ]
                        });
                    }

                    for (var i = 0; i < tabs.length; i++) {
                        if (i == 0) {
                            tabs[i].tabClass = "first active";
                            tabs[i].contentClass = "in active";
                        } else if (i == tabs.length - 1) {
                            tabs[tabs.length - 1].tabclass = "last"
                        }
                    }

                        var context = {};
                        context.tabList = tabs;
                    var callback = {onSuccess: function ( tabdata
                    ) {
                        $("#tab-list").append(tabdata);
                        //Load each tab content
                        for (var i = 0; i < tabs.length; i++) {
                            var tab = tabs[i];
                            if (tab.id == "api-overview") {

                                var label_names = api.labels.join(',');
                                var label_data = {};
                                setAuthHeader(swaggerClient);
                                swaggerClient["Label (Collection)"].get_label_info({
                                        "labels": label_names
                                    },
                                    function (jsonData) {
                                        label_data = jsonData.obj.list;
                                        context.api = api;
                                        context.labels = label_data;
                                        UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-overview", context, {
                                            onSuccess: function (renderedData) {
                                                $("#api-overview").append(renderedData);
                                                setOverviewTabData();
                                                renderCommentsView();

                                            }.bind(context), onFailure: function (message, e) {
                                                var message = "Error occurred while getting api overview details." + message;
                                                noty({
                                                    text: message,
                                                    type: 'error',
                                                    dismissQueue: true,
                                                    modal: true,
                                                    progressBar: true,
                                                    timeout: 2000,
                                                    layout: 'top',
                                                    theme: 'relax',
                                                    maxVisible: 10
                                                });
                                            }
                                        });
                                    },
                                    function (error) {
                                        var message = "Error occurred while retrieving labels";
                                        noty({
                                            text: message,
                                            type: 'error',
                                            dismissQueue: true,
                                            modal: true,
                                            progressBar: true,
                                            timeout: 2000,
                                            layout: 'top',
                                            theme: 'relax',
                                            maxVisible: 10
                                        });
                                        if(error.status==401){
                                            redirectToLogin(contextPath);
                                        }
                                    });
                            }

                            if (tab.id == "api-documentation") {
                                var doc_instance = new DOC();
                                var docs = doc_instance.getAll(getDOCsCallback,apiId);
                            }

                            if (tab.id == "api-swagger") {
                            	var bearerToken = "Bearer " + getCookie("WSO2_AM_TOKEN_1");
                            	setAuthHeader(swaggerClient);
                            	//Get Swagger definition of the API
                                swaggerClient["API (individual)"].get_apis_apiId_swagger({"apiId": apiId},
                                    function (jsonData) {
                                		//by default get the first label to load the swagger UI
                                		var label = {};
                                		var swaggerURL = jsonData.url;
                                		if (label_data.length > 0) {
                                			swaggerURL = jsonData.url + "?labelName=" + label_data[0].name + "&scheme=" + location.protocol;
                                		}
                                		$(document).ready(function(){
                                			window.swaggerUi = new SwaggerUi({
	                                			url: swaggerURL,
	                                			dom_id: "swagger-ui-container",
	                                			authorizations: {
	                                				key: new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header")
	                                			},
	                                			supportedSubmitMethods: ['get', 'post', 'put', 'delete', 'patch', 'head'],
	                                			onComplete: function(swaggerApi, swaggerUi){
	                                				console.log("Loaded SwaggerUI");
	                                			},
	                                			onFailure: function(data) {
	                                				console.log("Unable to Load SwaggerUI");
	                                			},
	                                			docExpansion: "list",
	                                			jsonEditor: false,
	                                			defaultModelRendering: 'schema',
	                                			showRequestHeaders: true,
	                                			validatorUrl: null
                                			});
                                			window.swaggerUi.load();
                                		});
                                		UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-console",context, {
		                        			onSuccess: function (renderedData) {
		                        				$("#api-swagger").append(renderedData);
		                        				setOverviewTabData();
		                        			}.bind(context), onFailure: function (message, e) {
		                        				var message = "Error occurred while loading API console." + message;
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
                                		});


                                		//Retrieve API subscriptions and keys for API Console
                                		swaggerClient["Application Collection"].get_applications({},
	                                        function (jsonData) {
	                                            var context = {};
	                                            var applications = jsonData.obj.list;
	                                            swaggerClient["Subscription Collection"].get_subscriptions({
	                                                        "apiId": apiId,
	                                                        "applicationId": "",
	                                                        "responseContentType": 'application/json'
	                                                    }, requestMetaData(),
	                                                    function (jsonData) {
	                                                        var subscribedApplications = [], subscribedAppsAndKeys = [], subscription = {};
	                                                        var subscriptions = jsonData.obj.list;
	                                                        var application = {};
	                                                        for (var i = 0; i < applications.length; i++) {
	                                                                application = applications[i];
	                                                                for (var j = 0; j < subscriptions.length; j++) {
	                                                                    subscription = subscriptions[j];
	                                                                    if (subscription.applicationId === application.applicationId) {
	                                                                    	subscribedApplications.push(application);
	                                                                        continue;
	                                                                    }
	                                                                }
	                                                            }
	                                                        for (var i =0; i<subscribedApplications.length; i++) {
	                                                        	var app = subscribedApplications[i];
	                                                        	swaggerClient["Application (individual)"].get_applications_applicationId({
	                                                                "applicationId": app.applicationId,
	                                                                "responseContentType": 'application/json'
	                                                            }, requestMetaData(),
	                                                            function (jsonData) {
	                                                            	var app = jsonData.obj;
	                                                            	var keys = jsonData.obj.keys;
	                                                            	for (var j = 0; j < keys.length; j++) {
	                                                            		if (keys[j].keyType == "PRODUCTION") {
	                                                            			app["prodKey"] = keys[j].token.accessToken;
	                                                            		} else {
	                                                            			app["sandBKey"] = keys[j].token.accessToken;
	                                                            		}
	                                                            	}
	                                                            	subscribedAppsAndKeys.push(app);
	                                                            	context.subscribedAppsAndKeys = subscribedAppsAndKeys;
	                                                            	context.label_data = label_data;

	    	                                                        UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-console-auth",context, {
	    	                                                			onSuccess: function (renderedData) {
	    	                                                				$("#authorizations").html(renderedData);
	    	                                                				$(".subapp").change(change_token);
	    	                                                				$(".keytype").change(change_token);
	    	                                                				$(".env_name").change(select_environment);
	    	                                                				change_token();
	    	                                                			}.bind(context), onFailure: function (message, e) {
	    	                                                				var message = "Error occurred while loading API console." + message;
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
	    	                                                		});
	                                                            },
	                                                            function (error) {
	                                                                var message = "Error occurred while retrieving Application details";
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
	                                                                if(error.status==401){
	                                                                    redirectToLogin(contextPath);
	                                                                }
	                                                            });
	                                                        }

	                                        },
	                                        function (error) {
	                                            var message = "Error occurred while retrieving Subscriptions";
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
	                                            if(error.status==401){
	                                                redirectToLogin(contextPath);
	                                            }
	                                        });


	                              },
	                              function (error) {
	                            	  var message = "Error occurred while retrieving Applications";
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
	                                  if(error.status==401){
	                                	  redirectToLogin(contextPath);
	                                  }
	                                });

                            },
                            function (error) {
                                var message = "Error occurred while retrieve Applications";
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
                                if(error.status==401){
                                    redirectToLogin(contextPath);
                                }
                            });
                            }

                        }
                    }.bind(context),onFailure: function (message, e) {
                        var message = "Error occurred while viewing API details";
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
                    }};
                    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-detail-tab-list",context,
                         callback);


                },
                function (jqXHR, textStatus, errorThrown) {
                    var message = "Error occurred while retrieve api with id  : " + apiId;
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
                    if(jqXHR.status==401){
                        redirectToLogin(contextPath);
                    }
                });

            $('.page-content').on('click', 'button', function (e) {
                var element = e.target;
                if (element.id == "subscribe-button") {
                    var applicationId = $("#application-list option:selected").val();
                    if (applicationId == "-" || applicationId == "createNewApp") {
                        var message = "Please select an application before subscribing";
                        noty({
                            text: message,
                            type: 'warning',
                            dismissQueue: true,
                            modal: true,
                            progressBar: true,
                            timeout: 2000,
                            layout: 'top',
                            theme: 'relax',
                            maxVisible: 10,
                        });
                        return;
                    }
                    $(this).html(i18n.t('Please wait...')).attr('disabled', 'disabled');
                    var tier = $("#tiers-list").val();
                    var apiIdentifier = $("#apiId").val();


                    var subscriptionData = {};
                    subscriptionData.policy = tier;
                    subscriptionData.applicationId = applicationId;
                    subscriptionData.apiIdentifier = apiIdentifier;
                    setAuthHeader(swaggerClient);
                    swaggerClient["Subscription (individual)"].post_subscriptions({
                            "body": subscriptionData,
                            "Content-Type": "application/json"
                        },
                        function (jsonData) {
                            $("#subscribe-button").html('Subscribe');
                            $("#subscribe-button").removeAttr('disabled');
                            var subscription = jsonData.obj;

                            if (jsonData.status == 202) {
                                //workflow related code
                                var jsonResponse;
                                if (subscription.jsonPayload) {                                 
                                    jsonResponse = JSON.parse(subscription.jsonPayload);
                                }                          
                                if (jsonResponse && jsonResponse.redirectUrl) {
                                    var message = jsonResponse.redirectConfirmationMsg;
                                    noty({
                                        text: message,
                                        layout: "top",
                                        theme: 'relax',
                                        dismissQueue: true,
                                        type: "alert",
                                        buttons: [
                                            {addClass: 'btn btn-primary', text: 'Leave page', onClick: function($noty) {
                                                $noty.close();
                                                location.href = jsonResponse.redirectUrl;
                                              }
                                            },
                                            {addClass: 'btn btn-default', text: 'Stay on this page', onClick: function($noty) {
                                                $noty.close();
                                                location.href = contextPath + "/apis/" + apiId;
                                              }
                                            }
                                          ]
                                    });
                                } else {
                                    var message = "Request has been submitted and is now awaiting approval.";
                                    noty({
                                        text: message,
                                        layout: "top",
                                        theme: 'relax',
                                        dismissQueue: true,
                                        type: "alert",
                                        buttons: [
                                            {addClass: 'btn btn-primary', text: 'View Subscriptions', onClick: function($noty) {
                                                $noty.close();
                                                location.href = contextPath + "/applications/" + applicationId + "#subscription";
                                              }
                                            },
                                            {addClass: 'btn btn-default', text: 'Stay on this page', onClick: function($noty) {
                                                $noty.close();
                                                location.href = contextPath + "/apis/" + apiId;
                                              }
                                            }
                                          ]
                                    });
                                }
                            } else {
                                var message = "You have successfully subscribed to the API.";
                                noty({
                                    text: message,
                                    layout: "top",
                                    theme: 'relax',
                                    dismissQueue: true,
                                    type: "alert",
                                    buttons: [
                                        {addClass: 'btn btn-primary', text: 'View Subscriptions', onClick: function($noty) {
                                            $noty.close();
                                            location.href = contextPath + "/applications/" + applicationId + "#subscription";
                                          }
                                        },
                                        {addClass: 'btn btn-default', text: 'Stay on this page', onClick: function($noty) {
                                            $noty.close();
                                            location.href = contextPath + "/apis/" + apiId;
                                          }
                                        }
                                      ]
                                });
                            }


                            //TODO : Embedding message model


                        },
                        function (error) {
                            var message = "Error occurred while adding Application : " + applicationName;
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
                        });
                } else if (element.id == "comment-add-button") {
                    var apiId = $("#apiId").val();
                    var commentDetals = {};
                    commentDetals.apiId = apiId;
                    commentDetals.commentText = $("#comment-text").val();
                    setAuthHeader(swaggerClient);
                    swaggerClient["API (individual)"].post_apis_apiId_comments({
                            "apiId": apiId,
                            "body": commentDetals,
                            "Content-Type": "application/json"
                        },
                        function (jsonData) {
                            var comment = jsonData.obj;
                            if (jsonData.status == 201) {
                                var message = "Comment added successfully.";
                                noty({
                                    text: message,
                                    type: 'success',
                                    dismissQueue: true,
                                    modal: true,
                                    progressBar: true,
                                    timeout: 2000,
                                    layout: 'top',
                                    theme: 'relax',
                                    maxVisible: 10
                                });
                                $("#comment-text").val("");
                                renderCommentsView();
                            }
                        },
                        function (error) {
                            var message = "Error occurred while adding Comment";
                            noty({
                                text: message,
                                type: 'error',
                                dismissQueue: true,
                                modal: true,
                                progressBar: true,
                                timeout: 2000,
                                layout: 'top',
                                theme: 'relax',
                                maxVisible: 10
                            });
                        });
                }
            });

            var renderCommentsView = function () {
                $("#comment-add-button").attr('disabled', 'disabled');

                $("#comment-text").on('keyup', function () {
                    if ($("#comment-text").val().trim() == "") {
                        $("#comment-add-button").attr('disabled', 'disabled');
                    } else {
                        $("#comment-add-button").removeAttr('disabled');
                    }
                });

                swaggerClient["Retrieve"].get_apis_apiId_comments({
                        "apiId": apiId
                    },
                    function (jsonData) {
                        var comments = jsonData.obj;
                        if (jsonData.status == 200) {
                            var callbacks = {
                                onSuccess: function (data) {
                                    $('#comment-list').html(data);
                                    $('#comment-list').find('.comment-text').editable({
                                        success: updateComment
                                    });

                                    disableComment();

                                    $('.delete_comment').on('click', function (event) {
                                        var commentId = $(this).attr("data-comment-id");
                                        noty({
                                            text: 'Do you want to delete the comment ?',
                                            type: 'alert',
                                            dismissQueue: true,
                                            layout: "topCenter",
                                            modal: true,
                                            theme: 'relax',
                                            buttons: [
                                                {
                                                    addClass: 'btn btn-danger', text: 'Ok', onClick: function ($noty) {
                                                    $noty.close();
                                                    swaggerClient["API (individual)"].delete_apis_apiId_comments_commentId({
                                                            "apiId": apiId,
                                                            "commentId": commentId
                                                        },
                                                        function (jsonData) {
                                                            if (jsonData.status == 200) {
                                                                var message = "Comment deleted successfully.";
                                                                noty({
                                                                    text: message,
                                                                    type: 'success',
                                                                    dismissQueue: true,
                                                                    modal: true,
                                                                    progressBar: true,
                                                                    timeout: 2000,
                                                                    layout: 'top',
                                                                    theme: 'relax',
                                                                    maxVisible: 10
                                                                });
                                                                renderCommentsView();
                                                            }
                                                        },
                                                        function (error) {
                                                            var message = "Error occurred while retrieving Comment";
                                                            noty({
                                                                text: message,
                                                                type: 'error',
                                                                dismissQueue: true,
                                                                modal: true,
                                                                progressBar: true,
                                                                timeout: 2000,
                                                                layout: 'top',
                                                                theme: 'relax',
                                                                maxVisible: 10
                                                            });
                                                        });
                                                    }
                                                },
                                                {
                                                    addClass: 'btn btn-info',
                                                    text: 'Cancel',
                                                    onClick: function ($noty) {
                                                        $noty.close();
                                                    }
                                                }
                                            ]
                                        });
                                    });
                                    disableDelete();
                                }, onFailure: function (data) {
                                    var message = "Error occurred while viewing API comments";
                                    noty({
                                        text: message,
                                        type: 'error',
                                        dismissQueue: true,
                                        modal: true,
                                        progressBar: true,
                                        timeout: 2000,
                                        layout: 'top',
                                        theme: 'relax',
                                        maxVisible: 10
                                    });
                                }
                            };
                            //Render Api comments
                            UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-comments", comments, callbacks);
                        }
                    },
                    function (error) {
                        var message = "Error occurred while retrieving Comment";
                        noty({
                            text: message,
                            type: 'error',
                            dismissQueue: true,
                            modal: true,
                            progressBar: true,
                            timeout: 2000,
                            layout: 'top',
                            theme: 'relax',
                            maxVisible: 10
                        });
                    });
            };

            //Method to update a comment
            var updateComment = function (resource, newValue) {
                var commentId = $(this).attr("data-comment-id");
                var updateBody = {};
                updateBody.commentId = commentId;
                updateBody.apiId = apiId;
                updateBody.commentText = newValue;
                swaggerClient["API (individual)"].put_apis_apiId_comments_commentId({
                        "apiId": apiId,
                        "commentId": commentId,
                        "body": updateBody,
                        "Content-Type": "application/json"
                    },
                    function (jsonData) {
                        if (jsonData.status == 200) {
                            var message = "Comment updated successfully.";
                            noty({
                                text: message,
                                type: 'success',
                                dismissQueue: true,
                                modal: true,
                                progressBar: true,
                                timeout: 2000,
                                layout: 'top',
                                theme: 'relax',
                                maxVisible: 10
                            });
                            renderCommentsView();
                        }
                    },
                    function (error) {
                        var message = "Error occurred while updating Comment";
                        noty({
                            text: message,
                            type: 'error',
                            dismissQueue: true,
                            modal: true,
                            progressBar: true,
                            timeout: 2000,
                            layout: 'top',
                            theme: 'relax',
                            maxVisible: 10
                        });
                    });
            };

            //Method to disable the comment to restrict other users from editing
            var disableComment = function () {
                var loggedInUser = window.localStorage.getItem("user");
                $('.comment-text').each(function () {
                    var commentOwner = $(this).attr("data-username");
                    if (commentOwner != loggedInUser) {
                        $(this).editable('toggleDisabled');
                    }
                });
            };

            //Method to remove the delete icons from other users
            var disableDelete = function () {
                var loggedInUser = window.localStorage.getItem("user");
                $('.delete_comment').each(function () {
                    var commentOwner = $(this).attr("data-username");
                    if (commentOwner != loggedInUser) {
                        $(this).remove();
                    }
                });
            }
        },
        failure : function(error){
            console.log("Error occurred while loading swagger definition");
        }



    });
});


/**
 * Callback method to handle apis data after receiving them via the REST API
 * @param response {object} Raw response object returned from swagger client
 */
function getDOCsCallback(jsonData) {
    var documentationList = jsonData.obj.list, length, documentations = {}, doc, obj,docsObj=[];

    if (documentationList != null) {
        length = documentationList.length;
    }

    var docsObj = {};
    for (var i = 0; i < length; i++) {
        doc = documentationList[i];
        if (doc.sourceType == "INLINE") {
            doc.isInLine = true;
        } else if (doc.sourceType == "FILE") {
            doc.isFile = true;
        } else if (doc.sourceType == "URL") {
            doc.isURL = true;
        }
        var groupName = doc.type;
        if (!docsObj[groupName]) {
            docsObj[groupName] = [];
        }
        docsObj[groupName].push(doc);
    }
    var docsArray = [];
    for (var type in docsObj) {
        var icon = null, typeName = null;
        if (type.toUpperCase() == "HOWTO") {
            icon = "fw-info";
            typeName = "HOW TO";
        } else if (type.toUpperCase() == "PUBLIC_FORUM") {
            icon = "fw-forum";
            typeName = "PUBLIC FORUM";
        } else if (type.toUpperCase() == "SUPPORT_FORUM") {
            icon = "fw-forum";
            typeName = "SUPPORT FORUM";
        } else if (type.toUpperCase() == "SAMPLES") {
            icon = "fw-api";
            typeName = "SAMPLES";
        } else {
            icon = "fw-text";
            typeName = "OTHER";
        }
        docsArray.push({type: groupName, icon:icon, typeName:typeName, docs: docsObj[groupName]});
    }
    var docs ={};
    docs.documentations = docsArray;
    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-documentations",docs, {
        onSuccess: function (renderedData) {
            $("#api-documentation").append(renderedData);

        }, onFailure: function (message, e) {
            var message = "Error occurred while getting api documentation details." + message;
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
    });
}

function applicationSelectionChange() {
    var apiId = $("#apiId").val();
    var selectedVal = $('#application-list option:selected').val();
    if(selectedVal == "createNewApp") {
        location.href = contextPath + "/applications/add?goBack=yes&apiId="+ apiId;
    }
}

function setOverviewTabData() {
    var link = window.location+'';
    $('#api_mailto').on("click",function(){
        location.href = "mailto:?Subject="+encodeURIComponent(document.title)+"&body=Link : "+ encodeURIComponent(window.location);
    });
    $('#embed_iframe').text('<iframe width="450" height="120" src="'+link.replace('info','widget')+'" frameborder="0" allowfullscreen></iframe>');
    $('#embed-copy').attr('data-clipboard-text', '<iframe width="450" height="120" src="'+link.replace('info','widget')+'" frameborder="0" allowfullscreen></iframe>');

    $('.share_links,#api_mailto').click(function(){
        $('.share_links,#api_mailto').parent().removeClass('active');
        $(this).parent().addClass('active');
        $('.share_dives').hide();
        $('#share_div_' + $(this).attr('ref')).show();
        return false;
    });
    var api_url = encodeURIComponent(window.location+'');
    var description = document.title + " : try this API at "+window.location;
    $("#facebook").attr("href","http://www.facebook.com/sharer.php?u="+api_url);
    $("#twitter").attr("href","http://twitter.com/share?url="+api_url+"&text="+encodeURIComponent(description));
    $("#googleplus").attr("href","https://plus.google.com/share?url="+api_url);
    $("#digg").attr("href","http://www.digg.com/submit?url="+api_url);
}

var change_token = function() {
    $(".notoken").hide();
    var option = $("#sub_app_list option:selected");
    var type = $("#key_type").val();
    var key = option.attr("data-" + type);
    if(key == "null") {
    	$(".notoken").show("slow");
    	$("#access_token").val("");
    } else {
    	$("#access_token").val(key);
    	//Set changed token as the token for API calls
    	if(key && key.trim() != "") {
    		swaggerUi.api.clientAuthorizations.add("key", new SwaggerClient.ApiKeyAuthorization("Authorization", "Bearer "+ key, "header"));
    	} else{
    		swaggerUi.api.clientAuthorizations.add("key", new SwaggerClient.ApiKeyAuthorization("Authorization", "Bearer ", "header"));
    	}
    }
  };


var select_environment = function(){
    var selectedEnvironment = $("#environment_name");
    var name = selectedEnvironment.val();

    //Token to load the swagger definition
    var bearerToken = "Bearer " + getCookie("WSO2_AM_TOKEN_1");
    swaggerUi.api.clientAuthorizations.add("key", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));

    //Set Swagger URL
    if (window.swaggerUi.api.url.indexOf("?") != -1) {
    	if (window.swaggerUi.api.url.indexOf("labelName") != -1) {
    		window.swaggerUi.updateSwaggerUi({ "url" : swaggerUi.api.url.split("labelName")[0] + "labelName=" + name + "&scheme=" + location.protocol});
    	} else {
            window.swaggerUi.updateSwaggerUi({ "url" : swaggerUi.api.url + "&labelName=" + name + "&scheme=" + location.protocol});
    	}
    } else {
    	window.swaggerUi.updateSwaggerUi({ "url" : swaggerUi.api.url + "?labelName=" + name + "&scheme=" + location.protocol});
    }
    change_token();
  };

function checkOnKeyPress(e) {
	if (e.which == 13 ||e.keyCode == 13) {
		return false;
		}
	}
