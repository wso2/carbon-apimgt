$(function () {
    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first");
    $(".blue").insertBefore(prev).css('top','0px').addClass('active');

    var searchApis = function (query) {
        query = !query ? "" : query;
        var swaggerClient = new SwaggerClient({
            url: swaggerURL,
            success: function (swaggerData) {
                setAuthHeader(swaggerClient);
                swaggerClient["CompositeAPI (Collection)"].get_composite_apis(query,
                {"responseContentType": 'application/json'}, function (jsonData) {
                    var callbacks = {
                        onSuccess: function () {
                            $(".setbgcolor").generateBgcolor({
                                definite: true
                            });

                            $(".api-name-icon").each(function () {
                                var elem = $(this).next().children(".api-name");
                                $(this).nametoChar({
                                    nameElement: elem
                                });
                            });
                        }, onFailure: function (message, e) {
                        }
                    };
                    var mode = "OVERWRITE";
                    //Render APIs listing page
                    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.composite-api-listing",
                    jsonData.obj, "api-listing", mode, callbacks);
                }, function (error) {
                    if (error.status == 401) {
                        redirectToLogin(contextPath);
                    }
                });

            },
            failure: function (error) {
                console.log("Error occurred while loading swagger definition");
            }
        });

    };

    /*
    * Generate a backend friendly search query base on user input.
    */
    var generateQuery = function(){
        //Get the user search input
        var queryInput = $('#searchApiQuery').val();
        var query = "";
        //Get the lifecycle dropdown selection
        var searchApiLifecycle = $('#searchApiLifecycle').val();

        if(searchApiLifecycle != ""){
            query = "current_lc_status:"+searchApiLifecycle;
        }

        // when All is not selected on the dropdown we get rid of the full text search and append the
        // name: param at the front
        if(queryInput != "" && queryInput.search(":") == -1 && searchApiLifecycle != "" ) {
            query =  "name:" + queryInput + "," + query;
        } else if(queryInput != "" && queryInput.search(":") != -1 && searchApiLifecycle != "" ){
            query =  queryInput + "," + query;
        } else if(queryInput != "" && searchApiLifecycle == "" ){
            query = queryInput;
        }
        return query;
    };
    $("#searchApiQuery").on('keyup', function (e) {
        if (e.keyCode == 13) {
            searchApis({query:generateQuery()});
        }
    });

    $('#searchApi').click(function () {
        searchApis({query:generateQuery()});
    });

    $('#searchApiLifecycleList li a').click(function(){
        $('#searchApiLifecycle').val($(this).attr('data-lifecycle'));
        searchApis({query:generateQuery()});
    });

    searchApis();

});
