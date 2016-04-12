function APISamples(defaultTier) {
    this.sample_swagger = "{\"paths\":{\"/order\":{\"post\":{\"x-auth-type\":\"Application & Application User\"," +
   "\"x-throttling-tier\":\"" + defaultTier + "\",\"description\":\"Create a new Order\",\"parameters\"" +
   ":[{\"schema\":{\"$ref\":\"#/definitions/Order\"},\"description\":" +
   "\"Order object that needs to be added\",\"name\":\"body\",\"required\":true,\"in\":\"body\"}]," +
   "\"responses\":{\"201\":{\"headers\":{\"ETag\":{\"description\":" +
   "\"Entity Tag of the response resource. Used by caches, or in conditional request\",\"type\":" +
   "\"string\"},\"Location\":{\"description\":\"The URL of the newly created resource.\",\"type\":" +
   "\"string\"},\"Content-Type\":{\"description\":\"The content type of the body.\",\"type\":" +
   "\"string\"}},\"schema\":{\"$ref\":\"#/definitions/Order\"},\"description\":\"Created." +
   " Successful response with the newly created object as entity in the body. Location header" +
   " contains URL of newly created entity.\"},\"400\":{\"schema\":{\"$ref\":" +
   "\"#/definitions/Error\"},\"description\":\"Bad Request. Invalid request or validation error.\"}" +
   ",\"415\":{\"schema\":{\"$ref\":\"#/definitions/Error\"},\"description\":" +
   "\"Unsupported Media Type. The entity of the request was in a not supported format.\"}}}}," +
   "\"/menu\":{\"get\":{\"x-auth-type\":\"Application & Application User\",\"x-throttling-tier\":" +
   "\"" + defaultTier + "\",\"description\":\"Return a list of available menu items\",\"parameters\":" +
   "[{\"default\":25,\"description\":\"Maximum size of menu items to return.\",\"name\":\"limit\"" +
   ",\"format\":\"double\",\"type\":\"number\",\"in\":\"query\"},{\"default\":0,\"description\":" +
   "\"Starting point of the item list.\",\"name\":\"offset\",\"format\":\"double\",\"type\":" +
   "\"number\",\"in\":\"query\"},{\"description\":\"Search by menu item name or ingredients\\n\"" +
   ",\"name\":\"query\",\"type\":\"string\",\"in\":\"query\"}],\"responses\":{\"200\":{\"headers\"" +
   ":{\"ETag\":{\"description\":\"Entity Tag of the response resource. Used by caches, or " +
   "in conditional requests.\",\"type\":\"string\"},\"Content-Type\":{\"description\":" +
   "\"The content type of the body.\",\"type\":\"string\"}},\"schema\":{\"title\":\"Menu\"," +
   "\"properties\":{\"previous\":{\"description\":\"Link for previous page. Undefined if no " +
   "previous page.\",\"type\":\"string\"},\"count\":{\"type\":\"string\"},\"next\":" +
   "{\"description\":\"Link for next page. Undefined if no next page.\",\"type\":\"string\"}," +
   "\"list\":{\"items\":{\"$ref\":\"#/definitions/MenuItem\"},\"type\":\"array\"}},\"type\":" +
   "\"object\"},\"description\":\"OK. List of APIs is returned.\"},\"304\":{\"description\":" +
   "\"Not Modified. Empty body because the client has already the latest version of the requested " +
   "resource.\"},\"406\":{\"schema\":{\"$ref\":\"#/definitions/Error\"},\"description\":" +
   "\"Not Acceptable. The requested media type is not supported\"}}}},\"/order/{orderId}\"" +
   ":{\"put\":{\"x-auth-type\":\"Application & Application User\",\"x-throttling-tier\":" +
   "\"" + defaultTier + "\",\"description\":\"Update an existing Order\",\"parameters\":[{\"description\"" +
   ":\"Order Id\",\"name\":\"orderId\",\"format\":\"integer\",\"type\":\"number\",\"required\"" +
   ":true,\"in\":\"path\"},{\"schema\":{\"$ref\":\"#/definitions/Order\"},\"description\":\"" +
   "Order object that needs to be added\",\"name\":\"body\",\"required\":true,\"in\":\"body\"}]," +
   "\"responses\":{\"200\":{\"headers\":{\"ETag\":{\"description\":\"Entity Tag of the response " +
   "resource. Used by caches, or in conditional request\",\"type\":\"string\"},\"Last-Modified\"" +
   ":{\"description\":\"Date and time the resource has been modifed the last time. Used by caches," +
   " or in conditional reuquests.\",\"type\":\"string\"},\"Location\":{\"description\":\"The URL " +
   "of the newly created resource.\",\"type\":\"string\"},\"Content-Type\":{\"description\":\"The" +
   " content type of the body.\",\"type\":\"string\"}},\"schema\":{\"$ref\":" +
   "\"#/definitions/Order\"},\"description\":\"OK. Successful response with updated Order\"}," +
   "\"400\":{\"schema\":{\"$ref\":\"#/definitions/Error\"},\"description\":\"Bad Request. " +
   "Invalid request or validation error\"},\"404\":{\"schema\":{\"$ref\":\"#/definitions/Error\"}" +
   ",\"description\":\"Not Found. The resource to be updated does not exist.\"},\"412\":{\"schema\"" +
   ":{\"$ref\":\"#/definitions/Error\"},\"description\":\"Precondition Failed. The request has " +
   "not been performed because one of the preconditions is not met.\"}}},\"get\":{\"x-auth-type\"" +
   ":\"Application & Application User\",\"x-throttling-tier\":\"" + defaultTier + "\",\"description\":\"" +
   "Get details of an Order\",\"parameters\":[{\"description\":\"Order Id\",\"name\":\"orderId\"," +
   "\"format\":\"integer\",\"type\":\"number\",\"required\":true,\"in\":\"path\"}],\"responses\":" +
   "{\"200\":{\"schema\":{\"$ref\":\"#/definitions/Order\"},\"headers\":{\"ETag\":{\"description\"" +
   ":\"Entity Tag of the response resource. Used by caches, or in conditional requests.\",\"type\"" +
   ":\"string\"},\"Last-Modified\":{\"description\":\"Date and time the resource has been modifed " +
   "the last time. Used by caches, or in conditional reuquests.\",\"type\":\"string\"}," +
   "\"Content-Type\":{\"description\":\"The content type of the body.\",\"type\":\"string\"}}," +
   "\"description\":\"OK Requested Order will be returned\"},\"304\":{\"description\":" +
   "\"Not Modified. Empty body because the client has already the latest version of the " +
   "requested resource.\"},\"404\":{\"schema\":{\"$ref\":\"#/definitions/Error\"},\"description\"" +
   ":\"Not Found. Requested API does not exist.\"},\"406\":{\"schema\":{\"$ref\":" +
   "\"#/definitions/Error\"},\"description\":\"Not Acceptable. The requested media type is" +
   " not supported\"}}},\"delete\":{\"x-auth-type\":\"Application & Application User\"," +
   "\"x-throttling-tier\":\"" + defaultTier + "\",\"description\":\"Delete an existing Order\"," +
   "\"parameters\":[{\"description\":\"Order Id\",\"name\":\"orderId\",\"format\":\"integer\"," +
   "\"type\":\"number\",\"required\":true,\"in\":\"path\"}],\"responses\":{\"200\":{\"description\"" +
   ":\"OK. Resource successfully deleted.\"},\"404\":{\"schema\":{\"$ref\":\"#/definitions/Error\"}" +
   ",\"description\":\"Not Found. Resource to be deleted does not exist.\"},\"412\":{\"schema\"" +
   ":{\"$ref\":\"#/definitions/Error\"},\"description\":\"Precondition Failed. The request has " +
   "not been performed because one of the preconditions is not met.\"}}}}},\"schemes\":[\"https\"]" +
   ",\"produces\":[\"application/json\"],\"swagger\":\"2.0\",\"definitions\":{\"ErrorListItem\":" +
   "{\"title\":\"Description of individual errors that may have occored during a request.\"," +
   "\"properties\":{\"message\":{\"description\":\"Description about individual errors occored\"," +
   "\"type\":\"string\"},\"code\":{\"format\":\"int64\",\"type\":\"integer\"}},\"required\":" +
   "[\"code\",\"message\"]},\"MenuItem\":{\"title\":\"Pizza menu Item\",\"properties\":" +
   "{\"price\":{\"type\":\"string\"},\"description\":{\"type\":\"string\"},\"name\":{\"type\":" +
   "\"string\"},\"image\":{\"type\":\"string\"}},\"required\":[\"name\"]},\"Order\":{\"title\":" +
   "\"Pizza Order\",\"properties\":{\"customerName\":{\"type\":\"string\"},\"delivered\":{\"" +
   "type\":\"boolean\"},\"address\":{\"type\":\"string\"},\"pizzaType\":{\"type\":\"string\"}," +
   "\"creditCardNumber\":{\"type\":\"string\"},\"quantity\":{\"type\":\"number\"},\"orderId\":" +
   "{\"type\":\"integer\"}},\"required\":[\"orderId\"]},\"Error\":{\"title\":\"Error object" +
   " returned with 4XX HTTP status\",\"properties\":{\"message\":{\"description\":\"Error " +
   "message.\",\"type\":\"string\"},\"error\":{\"items\":{\"$ref\":\"#/definitions/ErrorListItem\"}" +
   ",\"description\":\"If there are more than one error list them out. Ex. list out validation" +
   " errors by each field.\",\"type\":\"array\"},\"description\":{\"description\":\"A detail " +
   "description about the error message.\",\"type\":\"string\"},\"code\":{\"format\":\"int64\"," +
   "\"type\":\"integer\"},\"moreInfo\":{\"description\":\"Preferably an url with more details" +
   " about the error.\",\"type\":\"string\"}},\"required\":[\"code\",\"message\"]}},\"consumes\"" +
   ":[\"application/json\"],\"info\":{\"title\":\"PizzaShackAPI\",\"description\":\"This document" +
   " describe a RESTFul API for Pizza Shack online pizza delivery store.\\n\",\"license\":{\"name\"" +
   ":\"Apache 2.0\",\"url\":\"http://www.apache.org/licenses/LICENSE-2.0.html\"},\"contact\":" +
   "{\"email\":\"architecture@PizzaShack.com\",\"name\":\"PizzaShack\",\"url\":" +
   "\"http://PizzaShack.lk\"},\"version\":\"1.0.0\"}}";
    }

APISamples.prototype.deploySample = function (defaultTier, gatewayURL) {
    var addAPIUrl = "/site/blocks/item-design/ajax/add.jag";
    var addAPIData = {action: 'sampleDesign', name: 'PizzaShackAPI', provider: username,
        version: '1.0.0', description: 'This is a simple API for Pizza Shack online pizza delivery store.',
        tags: 'pizza', visibility: 'public', context: 'pizzashack',
        swagger: this.sample_swagger, apiThumb: '/site/themes/default/images/pizzaShackAPIImage.jpg'};

    jagg.message({
        content:"" ,
        type:"info",
        title:""
    });
    $('#messageModal').modal({backdrop: 'static', keyboard: false });
    $(".modal-header .close").hide();
    $(".modal-footer").html("");
    $(".modal-title").html("Please wait");
    $(".modal-body").addClass("loadingButton");
    $(".modal-body").css({"margin-left": 25});
    $(".modal-body").html("Sample API is Deploying");
    $(".modal").css({width: 550});

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
                            tier: defaultTier, tiersCollection: defaultTier,
                            transport_http: "http", transport_https: "https",
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
                                                window.location.assign(siteContext + "/site/pages/index.jag");
                                                $(".modal-body").removeClass("loadingButton");
                                                jagg.message({
                                                    content: "Sample PizzaShackAPI is Deployed Successfully",
                                                    type: "info",
                                                    title: "Success"
                                                });
                                            }
                                        }, 'json');
                                } else {
                                    window.location.assign(siteContext + "/site/pages/index.jag");
                                    $(".modal-body").removeClass("loadingButton");
                                    jagg.message({
                                        content: "Sample PizzaShackAPI is Deployed Successfully",
                                        type: "info",
                                        title: "Success"
                                    });
                                }
                            }, 'json');
                    }, 'json');
            } else {
                $(".modal-body").removeClass("loadingButton");
                jagg.message({
                    content: "Error occurred while adding sample API",
                    type: "error",
                    title: "Error"
                });
            }
        }, 'json');
};

var deploySampleApi = function (defaultTier, gatewayURL) {
    var deployer = new APISamples(defaultTier, gatewayURL);
    deployer.deploySample(defaultTier, gatewayURL);
};