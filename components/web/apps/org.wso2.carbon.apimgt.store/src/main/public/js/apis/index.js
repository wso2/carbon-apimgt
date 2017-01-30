$(function () {

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".purple").insertBefore(prev).css('top','0px').addClass('active');

    var tagClient = new SwaggerClient({
        url: swaggerURL + "tags",
        success: function(swaggerData) {
            tagClient.setBasePath("");
            tagClient.default.tagsGet(
                {"responseContentType": 'application/json'},
                function(jsonData) {
                    var tags = jsonData.obj;

                    var callbacks = {onSuccess: function () {},onFailure: function (message, e) {}};
                    var mode = "OVERWRITE";
                    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.tag-cloud",tags,
                        "tagCloud", mode, callbacks);
                },
                function(error){
                    console.log("Error occurred")
                }
            );
        },
        failure : function(error){
            console.log("Error occurred while loading swagger definition");
        }
    });

    var apiClient = new SwaggerClient({
        url: swaggerURL + "apis",
        success: function(swaggerData) {
            apiClient.setBasePath("");
            apiClient.default.apisGet(
                {"responseContentType": 'application/json'},
                function(jsonData) {
                    var callbacks = {onSuccess: function () {
                        $(".setbgcolor").generateBgcolor({
                            definite:true
                        });

                        $(".api-name-icon").each(function() {
                            var elem = $(this).next().children(".api-name");
                            $(this).nametoChar({
                                nameElement: elem
                            });
                        });
                    },onFailure: function (message, e) {}};
                    var mode = "OVERWRITE";
                    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.api-listing",jsonData.obj,
                        "api-listing", mode, callbacks);






                }
            );
        },
        failure : function(error){
            console.log("Error occurred while loading swagger definition");
        }
    });

});
