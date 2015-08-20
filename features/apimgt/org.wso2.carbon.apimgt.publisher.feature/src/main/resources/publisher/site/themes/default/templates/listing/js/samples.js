function APISamples(defaultTier) {
    this.sample1_swagger = "{\"paths\":{ \"/*\":{\"get\":{\"parameters\":[{\"description\":\"Name of the City\"," +
        "\"name\":\"q\",\"type\":\"string\",\"required\":false,\"in\":\"query\"}],\"responses\":{\"200\":{ } }," +
        "\"x-auth-type\":\"Application & Application User\", \"x-throttling-tier\":\"" + defaultTier + "\" }}}," +
        "\"swagger\":\"2.0\",\"info\":{ \"title\":\"WeatherAPI\",\"version\":\"1.0.0\"}}";
}


APISamples.prototype.deploySample1 = function (defaultTier) {
    var addAPIUrl = "/site/blocks/item-design/ajax/add.jag";
    var addAPIData = {action: 'sampleDesign', name: 'WeatherAPI', provider: username, version: '1.0.0',
        description: 'The WeatherAPI gives the weather details according to the city name', tags: 'weather',
        visibility: 'public', context: 'weather', swagger: this.sample1_swagger, apiThumb: '/site/themes/default/images/weatherAPI.png'};

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

    //add the sample api
    jagg.post(addAPIUrl, addAPIData,
        function (apiAddResult) {
            if (!apiAddResult.error) {
                var urlDesign = '/site/blocks/item-design/ajax/add.jag';
                jagg.post(urlDesign, {action: "implement", name: "WeatherAPI", version: "1.0.0", provider: username,
                        implementation_methods: "endpoint", endpoint_type: "http",
                        endpoint_config: '{"production_endpoints":{"url":"http://api.openweathermap.org/data/2.5/weather","config":null}, "sandbox_endpoints":{"url":"http://api.openweathermap.org/data/2.5/weather","config":null}, "endpoint_type":"http"}',
                        production_endpoints: "http://api.openweathermap.org/data/2.5/weather", sandbox_endpoints: "http://api.openweathermap.org/data/2.5/weather",
                        endpointType: "nonsecured", swagger: this.sample1_swagger},
                    function (result) {
                        jagg.post(urlDesign, {action: "manage", name: "WeatherAPI", provider: username, version: "1.0.0",
                                default_version_checked: " ", tier: defaultTier, tiersCollection: defaultTier, transport_http: "http",
                                transport_https: "https", swagger: this.sample1_swagger},
                            function (result) {
                                if (isPublishPermitted) {
                                    var urlPublished = "/site/blocks/life-cycles/ajax/life-cycles.jag";
                                    var result2 = jagg.post(urlPublished, {action: "updateStatus", name: "WeatherAPI",
                                            version: "1.0.0", provider: username, status: "PUBLISHED", publishToGateway: true,
                                            requireResubscription: true},
                                        function (result) {
                                            if (!result.error) {
                                                window.location.assign(siteContext + "/site/pages/index.jag");
                                                $(".modal-body").removeClass("loadingButton");
                                                jagg.message({
                                                    content: "Sample WeatherAPI is Deployed Successfully",
                                                    type: "info",
                                                    title: "Success"
                                                });
                                            }
                                        }, 'json');
                                } else {
                                    window.location.assign(siteContext + "/site/pages/index.jag");
                                    $(".modal-body").removeClass("loadingButton");
                                    jagg.message({
                                        content: "Sample WeatherAPI is Deployed Successfully",
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

var deploySampleApi = function (defaultTier) {
    var deployer = new APISamples(defaultTier);
    deployer.deploySample1(defaultTier);
};