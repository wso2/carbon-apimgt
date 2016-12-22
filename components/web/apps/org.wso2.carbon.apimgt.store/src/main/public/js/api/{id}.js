$(function () {

    var apiId = $("#apiId").val();
    var client = new SwaggerClient({
        url: 'https://apis.wso2.com/api/am/store/v0.10/swagger.json',
        success: function (swaggerData) {

            client["API (individual)"].get_apis_apiId({"apiId": apiId},
                {"responseContentType": 'application/json'},
                function (jsonData) {
                    var api = jsonData.obj;
                    var context = {api: api};

                    $.get('/store/public/components/root/base/templates/api/{id}APIHeader.hbs', function (templateData) {
                        var apiHeaderTemplate = Handlebars.compile(templateData);

                        // Pass our data to the template
                        var apiHeaderCompilesTemplate = apiHeaderTemplate(context);

                        // Add the compiled html to the page
                        $('.page-header').html(apiHeaderCompilesTemplate);
                    }, 'html');

                    $.get('/store/public/components/root/base/templates/api/{id}APIThumbnail.hbs', function (templateData) {

                        var apiThumbnailTemplate = Handlebars.compile(templateData);
                        // Inject template data
                        var apiThumbnailCompiledTemplate = apiThumbnailTemplate(context);

                        // Append compiled template into page
                        $('#api-thumbnail').append(apiThumbnailCompiledTemplate);
                    }, 'html');

                    $.get('/store/public/components/root/base/templates/api/{id}APIInfo.hbs', function (templateData) {

                        var apiOverviewTemplate = Handlebars.compile(templateData);
                        // Inject template data
                        var compiledTemplate = apiOverviewTemplate(context);

                        // Append compiled template into page
                        $('#api-info').append(compiledTemplate);
                    }, 'html');

                    $.get('/store/public/components/root/base/templates/api/{id}APITierList.hbs', function (templateData) {

                        var apiOverviewTemplate = Handlebars.compile(templateData);
                        // Inject template data
                        var compiledTemplate = apiOverviewTemplate(context);

                        // Append compiled template into page
                        $('#tier-list').append(compiledTemplate);
                    }, 'html');

                    $.get('/store/public/components/root/base/templates/api/{id}APISubscriptions.hbs', function (templateData) {

                        var apiOverviewTemplate = Handlebars.compile(templateData);
                        // Inject template data
                        var compiledTemplate = apiOverviewTemplate(context);

                        // Append compiled template into page
                        $(compiledTemplate).insertAfter("#tier-list");
                    }, 'html');

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

                    $.get('/store/public/components/root/base/templates/api/{id}APIDetailTabList.hbs', function (templateData) {
                        context.tabList = tabs;
                        var apiOverviewTemplate = Handlebars.compile(templateData);
                        // Inject template data
                        var compiledTemplate = apiOverviewTemplate(context);

                        // Append compiled template into page
                        $("#tab-list").append(compiledTemplate);

                        //Load each tab content
                        for (var i = 0; i < tabs.length; i++) {
                            var tab = tabs[i];
                            if(tab.id == "api-overview"){
                                $.get('/store/public/components/root/base/templates/api/{id}APIOverview.hbs', function (templateData) {
                                    for (var environment in api.endpointURLs){
                                        if(environment.type == "production"){
                                            environment.title = "Production "
                                        }
                                    }
                                    var apiOverviewTemplate = Handlebars.compile(templateData);
                                    // Inject template data
                                    var compiledTemplate = apiOverviewTemplate(context);

                                    // Append compiled template into page
                                    $("#api-overview").append(compiledTemplate);

                                }, 'html');
                            }

                            if(tab.id == "api-documentation"){
                                $.get('/store/public/components/root/base/templates/api/{id}APIDocumentation.hbs', function (templateData) {
                                    context.tabList = tabs;
                                    var apiOverviewTemplate = Handlebars.compile(templateData);
                                    // Inject template data
                                    var compiledTemplate = apiOverviewTemplate(context);

                                    // Append compiled template into page
                                    $("#api-documentation").append(compiledTemplate);

                                }, 'html');
                            }

                        }

                    }, 'html')


                },
                function (jqXHR, textStatus, errorThrown) {
                    alert("Error occurred while retrieve api with id  : " + apiId);
                });

            client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", "Bearer a006a4d8-5273-32ac-b111-0c85895ac054", "header"));
            client["Application Collection"].get_applications({"responseContentType": 'application/json'},
                function (jsonData) {
                    var context = {};
                    var applications = jsonData.obj.list;
                    if (applications.length > 0) {
                        client["Subscription Collection"].get_subscriptions({
                                "apiId": apiId,
                                "applicationId": "",
                                "responseContentType": 'application/json'
                            },
                            function (jsonData) {
                                var availableApplications = [], subscription = {};
                                var isSubscribedToDefault = false;
                                var subscriptions = jsonData.obj.list;
                                var application = {};

                                applicationsLoop:
                                    for (var i = 0; i < applications.length; i++) {
                                        application = applications[i];
                                        for (var j = 0; j < subscriptions.length; j++) {
                                            subscription = subscriptions[j];
                                            if (subscription.applicationId === application.applicationId) {
                                                continue applicationsLoop;
                                            }
                                        }
                                        if (application.name == "DefaultApplication") {
                                            isSubscribedToDefault = true;
                                            application.isDefault = true;
                                        }
                                        availableApplications.push(application);
                                    }

                                $.get('/store/public/components/root/base/templates/api/{id}APIApplicationsList.hbs', function (templateData) {
                                    var applicationsTemplate = Handlebars.compile(templateData);
                                    // Define our data object
                                    var context = {};
                                    context.applications = availableApplications;
                                    context.isSubscribedToDefault = isSubscribedToDefault;

                                    // Pass our data to the template
                                    var applicationsCompiledTemplate = applicationsTemplate(context);

                                    // Add the compiled html to the page
                                    $('#applications-list').append(applicationsCompiledTemplate);
                                }, 'html');
                            },
                            function (error) {
                                alert("Error occurred while retrieve Applications" + error);
                            });
                    }

                },
                function (error) {
                    alert("Error occurred while retrieve Applications" + erro);
                });
        }
    });

});




































