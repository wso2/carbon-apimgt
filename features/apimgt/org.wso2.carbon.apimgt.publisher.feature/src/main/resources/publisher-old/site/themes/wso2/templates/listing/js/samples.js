function APISamples () {
    this.sample_swagger = "{\"paths\":{\"/order\":{\"post\":{\"x-auth-type\":\"Application & Application User\"," +
    "\"x-throttling-tier\":\"" + "$.{defaultResourceLevelTier}"  + "\",\"description\":\"Create a new Order\"," +
    "\"parameters\":[{\"schema\":{\"$ref\":\"#/definitions/Order\"},\"description\":" +
    "\"Order object that needs to be added\",\"name\":\"body\",\"required\":true,\"in\":\"body\"}]," +
    "\"responses\":{\"201\":{\"headers\":{\"Location\":{\"description\":\"The URL of the newly created resource.\",\"type\":" +
    "\"string\"},\"Content-Type\":{\"description\":\"The content type of the body.\",\"type\":" +
    "\"string\"}},\"schema\":{\"$ref\":\"#/definitions/Order\"},\"description\":\"Created." +
    " Successful response with the newly created object as entity in the body. Location header" +
    " contains URL of newly created entity.\"},\"400\":{\"schema\":{\"$ref\":" +
    "\"#/definitions/Error\"},\"description\":\"Bad Request. Invalid request or validation error.\"}" +
    ",\"415\":{\"schema\":{\"$ref\":\"#/definitions/Error\"},\"description\":" +
    "\"Unsupported Media Type. The entity of the request was in a not supported format.\"}},\"security\":" +
    "[{\"pizzashack_auth\":[\"write:order\",\"read:order\"]}]}}," +
    "\"/menu\":{\"get\":{\"x-auth-type\":\"Application & Application User\",\"x-throttling-tier\":" +
    "\"" + "$.{defaultResourceLevelTier}" + "\",\"description\":\"Return a list of available menu items\"," +
    "\"parameters\":" +
    "[],\"responses\":{\"200\":{\"headers\"" + ":{},\"schema\":{\"items\":{\"$ref\":\"#/definitions/MenuItem\"}," +
    "\"type\":\"array\"},\"description\":\"OK. List of APIs is returned.\"},\"304\":{\"description\":" +
    "\"Not Modified. Empty body because the client has already the latest version of the requested " +
    "resource.\"},\"406\":{\"schema\":{\"$ref\":\"#/definitions/Error\"},\"description\":" +
    "\"Not Acceptable. The requested media type is not supported\"}},\"security\":[{\"pizzashack_auth\":" +
    "[\"read:menu\"]}]}},\"/order/{orderId}\"" +
    ":{\"put\":{\"x-auth-type\":\"Application & Application User\",\"x-throttling-tier\":" +
    "\"" + "$.{defaultResourceLevelTier}" + "\",\"description\":\"Update an existing Order\"," +
    "\"parameters\":[{\"description\"" +
    ":\"Order Id\",\"name\":\"orderId\",\"format\":\"string\",\"type\":\"string\",\"required\"" +
    ":true,\"in\":\"path\"},{\"schema\":{\"$ref\":\"#/definitions/Order\"},\"description\":\"" +
    "Order object that needs to be added\",\"name\":\"body\",\"required\":true,\"in\":\"body\"}]," +
    "\"responses\":{\"200\":{\"headers\":{\"Location\":{\"description\":\"The URL " +
    "of the newly created resource.\",\"type\":\"string\"},\"Content-Type\":{\"description\":\"The" +
    " content type of the body.\",\"type\":\"string\"}},\"schema\":{\"$ref\":" +
    "\"#/definitions/Order\"},\"description\":\"OK. Successful response with updated Order\"}," +
    "\"400\":{\"schema\":{\"$ref\":\"#/definitions/Error\"},\"description\":\"Bad Request. " +
    "Invalid request or validation error\"},\"404\":{\"schema\":{\"$ref\":\"#/definitions/Error\"}" +
    ",\"description\":\"Not Found. The resource to be updated does not exist.\"},\"412\":{\"schema\"" +
    ":{\"$ref\":\"#/definitions/Error\"},\"description\":\"Precondition Failed. The request has " +
    "not been performed because one of the preconditions is not met.\"}},\"security\":[{\"pizzashack_auth\":" +
    "[\"write:order\",\"read:order\"]}]},\"get\":{\"x-auth-type\"" +
    ":\"Application & Application User\",\"x-throttling-tier\":\"" + "$.{defaultResourceLevelTier}" + "\"," +
    "\"description\":\"" +
    "Get details of an Order\",\"parameters\":[{\"description\":\"Order Id\",\"name\":\"orderId\"," +
    "\"format\":\"string\",\"type\":\"string\",\"required\":true,\"in\":\"path\"}],\"responses\":" +
    "{\"200\":{\"schema\":{\"$ref\":\"#/definitions/Order\"},\"headers\":{}," +
    "\"description\":\"OK Requested Order will be returned\"},\"304\":{\"description\":" +
    "\"Not Modified. Empty body because the client has already the latest version of the " +
    "requested resource.\"},\"404\":{\"schema\":{\"$ref\":\"#/definitions/Error\"},\"description\"" +
    ":\"Not Found. Requested API does not exist.\"},\"406\":{\"schema\":{\"$ref\":" +
    "\"#/definitions/Error\"},\"description\":\"Not Acceptable. The requested media type is" +
    " not supported\"}},\"security\":[{\"pizzashack_auth\":[\"write:order\", \"read:order\"]}]},\"delete\":" +
    "{\"x-auth-type\":\"Application & Application User\"," +
    "\"x-throttling-tier\":\"" + "$.{defaultResourceLevelTier}" + "\",\"description\":\"Delete an existing Order\"," +
    "\"parameters\":[{\"description\":\"Order Id\",\"name\":\"orderId\",\"format\":\"string\"," +
    "\"type\":\"string\",\"required\":true,\"in\":\"path\"}],\"responses\":{\"200\":{\"description\"" +
    ":\"OK. Resource successfully deleted.\"},\"404\":{\"schema\":{\"$ref\":\"#/definitions/Error\"}" +
    ",\"description\":\"Not Found. Resource to be deleted does not exist.\"},\"412\":{\"schema\"" +
    ":{\"$ref\":\"#/definitions/Error\"},\"description\":\"Precondition Failed. The request has " +
    "not been performed because one of the preconditions is not met.\"}},\"security\":[{\"pizzashack_auth\":" +
    "[\"write:order\",\"read:order\"]}]}}},\"schemes\":[\"https\"]" +
    ",\"produces\":[\"application/json\"],\"swagger\":\"2.0\", \"securityDefinitions\"" +
    ":{\"pizzashack_auth\":{\"type\":\"oauth2\",\"authorizationUrl\": \"http://wso2.swagger.io/api/oauth/dialog\"," +
    "\"flow\": \"implicit\", \"scopes\":{\"write:order\": \"modify order in your account\",\"read:order\":" +
    "\"read your order\", \"read:menu\": \"read your menu\"}}},\"definitions\":{\"ErrorListItem\":" +
    "{\"title\":\"Description of individual errors that may have occored during a request.\"," +
    "\"properties\":{\"message\":{\"description\":\"Description about individual errors occored\"," +
    "\"type\":\"string\"},\"code\":{\"format\":\"int64\",\"type\":\"integer\"}},\"required\":" +
    "[\"code\",\"message\"]},\"MenuItem\":{\"title\":\"Pizza menu Item\",\"properties\":" +
    "{\"price\":{\"type\":\"string\"},\"description\":{\"type\":\"string\"},\"name\":{\"type\":" +
    "\"string\"},\"image\":{\"type\":\"string\"}},\"required\":[\"name\"]},\"Order\":{\"title\":" +
    "\"Pizza Order\",\"properties\":{\"customerName\":{\"type\":\"string\"},\"delivered\":{\"" +
    "type\":\"boolean\"},\"address\":{\"type\":\"string\"},\"pizzaType\":{\"type\":\"string\"}," +
    "\"creditCardNumber\":{\"type\":\"string\"},\"quantity\":{\"type\":\"number\"},\"orderId\":" +
    "{\"type\":\"string\"}},\"required\":[\"orderId\"]},\"Error\":{\"title\":\"Error object" +
    " returned with 4XX HTTP status\",\"properties\":{\"message\":{\"description\":\"Error " +
    "message.\",\"type\":\"string\"},\"error\":{\"items\":{\"$ref\":\"#/definitions/ErrorListItem\"}" +
    ",\"description\":\"If there are more than one error list them out. Ex. list out validation" +
    " errors by each field.\",\"type\":\"array\"},\"description\":{\"description\":\"A detail " +
    "description about the error message.\",\"type\":\"string\"},\"code\":{\"format\":\"int64\"," +
    "\"type\":\"integer\"},\"moreInfo\":{\"description\":\"Preferably an url with more details" +
    " about the error.\",\"type\":\"string\"}},\"required\":[\"code\",\"message\"]}},\"consumes\"" +
    ":[\"application/json\"],\"info\":{\"title\":\"PizzaShackAPI\",\"description\":\"This" +
    " is a RESTFul API for Pizza Shack online pizza delivery store.\\n\",\"license\":{\"name\"" +
    ":\"Apache 2.0\",\"url\":\"http://www.apache.org/licenses/LICENSE-2.0.html\"},\"contact\":" +
    "{\"email\":\"architecture@pizzashack.com\",\"name\":\"John Doe\",\"url\":" +
    "\"http://www.pizzashack.com\"},\"version\":\"1.0.0\"}}";
    }

APISamples.prototype.deploySampleApi = function (gatewayURL) {
    var _this = this;
    var addAPIUrl = "/site/blocks/item-add/ajax/add.jag";
    jagg.post(addAPIUrl, {action: "getTiers"},
        function (apiLevelTierResult) {
            if (!apiLevelTierResult.error) {
                defaultApiLevelTier = apiLevelTierResult.tiers[0].tierName;
                jagg.post(addAPIUrl, {action: "getResourceTiers"},
                    function (resourceTierResult) {
                        if (!resourceTierResult.error) {
                            defaultResourceLevelTier = resourceTierResult.tiers[0].tierName;
                            _this.sample_swagger = _this.sample_swagger.split("$.{defaultResourceLevelTier}")
                                .join(defaultResourceLevelTier);
                            _this.deploySampleApiToBackend(gatewayURL, defaultApiLevelTier);
                        } else {
                            $(".modal-body").removeClass("loadingButton");
                            jagg.message({
                                content: i18n.t("Error occurred while loading resource level tiers"),
                                type: "error",
                                title: i18n.t("Error")
                            });
                        }
                    }, "json");
            } else {
                $(".modal-body").removeClass("loadingButton");
                jagg.message({
                    content: i18n.t("Error occurred while loading API level tiers"),
                    type: "error",
                    title: i18n.t("Error")
                });
            }
        }, "json");
};

APISamples.prototype.deploySampleApiToBackend = function (gatewayURL, defaultApiLevelTier) {
    var addAPIUrl = "/site/blocks/item-design/ajax/add.jag";
    var addAPIData = {action: 'sampleDesign', name: 'PizzaShackAPI', provider: username,
        version: '1.0.0', type: 'http', description: 'This is a simple API for Pizza Shack online pizza delivery store.', tags: 'pizza',
        visibility: 'public', context: 'pizzashack', accessControl:"all",
        swagger: this.sample_swagger, apiThumb: '/site/themes/wso2/images/pizzaShackAPIImage.jpg'};

    jagg.message({
        content:"" ,
        type:"info",
        title:""
    });
    $('#messageModal').modal({backdrop: 'static', keyboard: false });
    $(".modal-header .close").hide();
    $(".modal-footer").html("");
    $(".modal-title").html(i18n.t("Please wait"));
    $(".modal-body").addClass("loadingButton");
    $(".modal-body").css({"margin-left": 25});
    $(".modal-body").html(i18n.t("Sample API is Deploying"));

    var _this = this;
    //add the sample api
    jagg.post(addAPIUrl, addAPIData,
        function (apiAddResult) {
            if (!apiAddResult.error) {
                var urlDesign = '/site/blocks/item-design/ajax/add.jag';
                var prodEndpoint = gatewayURL + "/am/sample/pizzashack/v1/api/";
                var sandboxEndpoint = gatewayURL + "/am/sample/pizzashack/v1/api/";
                var implementation = {
                    action: "implement",
                    name: "PizzaShackAPI",
                    version: "1.0.0",
                    provider: username,
                    implementation_methods: "endpoint",
                    endpoint_type: "http",
                    endpoint_config: "{" +
                    "\"production_endpoints\": {" +
                            "\"url\": \"" + prodEndpoint + "\"," +
                            "\"config\": null" +
                        "}," +
                        "\"sandbox_endpoints\": {" +
                            "\"url\": \"" + sandboxEndpoint + "\"," +
                            "\"config\": null" +
                        "}," +
                        "\"endpoint_type\": \"http\" " +
                    "}",
                    production_endpoints: prodEndpoint,
                    sandbox_endpoints: sandboxEndpoint,
                    endpointType: "nonsecured",
                    swagger: _this.sample_swagger
                };
                jagg.post(urlDesign, implementation,
                    function (result) {
                        jagg.post(urlDesign, {action: "manage", name: "PizzaShackAPI",
                            provider: username, version: "1.0.0", default_version_checked: " ",
                            tier: defaultApiLevelTier, tiersCollection: defaultApiLevelTier,
                            transport_http: "http", transport_https: "https",
                            bizOwner: "Jane Roe",
                            bizOwnerMail: "marketing@pizzashack.com",
                            techOwner: "John Doe",
                            techOwnerMail: "architecture@pizzashack.com",
                            swagger: _this.sample_swagger},
                            function (result) {
                                if (isPublishPermitted) {
                                    var urlPublished = "/site/blocks/life-cycles/ajax/life-cycles.jag";
                                    var result2 = jagg.post(urlPublished, {action: "updateStatus",
                                        name: "PizzaShackAPI", version: "1.0.0", provider: username,
                                        status: "Publish", publishToGateway: true,
                                        requireResubscription: true},
                                        function (result) {
                                            if (!result.error) {
                                                $(".modal-body").removeClass("loadingButton");
                                                jagg.message({
                                                    content: i18n.t("Sample PizzaShackAPI is deployed successfully"),
                                                    type: "info",
                                                    title: i18n.t("Success"),
                                                    cbk:function(){window.location.assign(siteContext + "/site/pages/index.jag");}
                                                });
                                                //Add document for the published sample
                                                _this.addSampleAPIDoc();
                                            }
                                        }, 'json');
                                } else {
                                    $(".modal-body").removeClass("loadingButton");
                                    jagg.message({
                                        content: i18n.t("Sample PizzaShackAPI is created successfully"),
                                        type: "info",
                                        title: i18n.t("Success"),
                                        cbk:function(){window.location.assign(siteContext + "/site/pages/index.jag");}
                                    });
                                    //Add document for the created sample
                                    _this.addSampleAPIDoc();
                                }
                            }, 'json');
                    }, 'json');
            } else {

                if (result.message == "timeout") {
                    jagg.showLogin();
                }else {
                    $(".modal-body").removeClass("loadingButton");
                    jagg.message({
                        content: i18n.t("Error occurred while adding the sample API"),
                        type: "error",
                        title: i18n.t("Error")
                    });
                }
            }
        }, 'json');
};

APISamples.prototype.addSampleAPIDoc = function () {
    var addDocUrl = "/site/blocks/documentation/ajax/docs.jag";
    var addDocData = {action: 'addSampleDocumentation', provider: username,
        apiName: 'PizzaShackAPI',version: '1.0.0',
        docName: 'PizzaShack API Documentation',
        docType: 'how to', sourceType: 'file',
        summary: 'This is the API documentation for Pizza Shack API',
        docLocation: '/samples/PizzaShack/PizzaShackAPIDoc.pdf'
    };
    jagg.post(addDocUrl, addDocData,
        function (apiDocResult) {
            if (apiDocResult.error) {
                $(".modal-body").removeClass("loadingButton");
                jagg.message({
                    content: i18n.t("Error occurred while adding the sample API documentation"),
                    type: "error",
                    title: i18n.t("Error")
                });
            }
        }, 'json');
};

var deploySampleApi = function (gatewayURL) {
    var deployer = new APISamples();
    deployer.deploySampleApi(gatewayURL);
};
