$(function () {

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".green").insertBefore(prev).css('top','0px').addClass('active');

    var bearerToken = "Bearer f8009e9c-ae7a-3bc4-a39b-56b058f84743";
    var client = new SwaggerClient({
        url: 'https://apis.wso2.com/api/am/store/v0.10/swagger.json',
        success: function (swaggerData) {

            //List Applications
            client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
            client["Application Collection"].get_applications({"responseContentType": 'application/json'},
                function (data) {
                    $.ajax({
                        url: '/store/public/components/root/base/templates/application/appListRow.hbs',
                        type: 'GET',
                        success: function (result) {
                            var theTemplateScript = result;
                            var theTemplate = Handlebars.compile(theTemplateScript);
                            var theCompiledHtml, context,ifNotApprovedStatus,ifNotRejectedStatus;

                            if(data.obj.status != "APPROVED"){
                                ifNotApprovedStatus = true;
                            }
                            else{
                                ifNotApprovedStatus = false;
                            }
                            if(data.obj.status != "REJECTED"){
                                ifNotRejectedStatus = true;
                            }
                            else{
                                ifNotRejectedStatus = false;
                            }
                            for (var i in data.obj.list) {
                                context = {
                                    "name": data.obj.list[i].name,
                                    "applicationId": data.obj.list[i].applicationId,
                                    "tier": data.obj.list[i].throttlingTier,
                                    "status": data.obj.list[i].status,
                                    "subscriptions": data.obj.list[i].subscriptions || 0,
                                    "ifNotApprovedStatus": ifNotApprovedStatus,
                                    "ifNotRejectedStatus": ifNotRejectedStatus
                                };
                                theCompiledHtml = theTemplate(context);
                                $("tbody").append(theCompiledHtml);
                            }
                        },
                        error: function (e) {
                            alert("Error occurred while listing applications");
                        }
                    });
                });

            //Delete Application
            $('#application-table').on('click', 'a.deleteApp', function () {
                alert("Are you sure you want to delete Application");

                var appId = $(this).attr("data-id")
                client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
                client["Application (individual)"].delete_applications_applicationId({"applicationId": appId},
                    function (success) {
                        //TODO: Reload element only
                        window.location.reload(true);
                    },
                    function (error) {
                        alert("Error occurred while deleting application")
                    });
            });

            //Get available tiers
            client["Tier Collection"].get_tiers_tierLevel({"tierLevel": "application"},
                function (jsonData) {
                    var tierList = jsonData.obj.list;
                    $.get('/store/public/components/root/base/templates/application/appTiersTemplate.hbs', function (templateData) {
                        var template = Handlebars.compile(templateData);
                        // Define our data object
                        var context = {
                            "appTiers": tierList
                        };

                        // Pass our data to the template
                        var theCompiledHtml = template(context);

                        // Add the compiled html to the page
                        $('#appTierList').html(theCompiledHtml)
                    }, 'html');

                },
                function (error) {
                    console.log('failed with the following: ' + error.statusText);
                });


            $("#appAddForm").validate({
                submitHandler: function (form) {
                    applicationAdd();
                }
            });

            var applicationAdd = function () {
                var applicationName = $("#application-name").val();
                var tier = $("#appTier").val();
                var goBack = $("#goBack").val();
                var description = $("#description").val();

                var application = {
                    name: applicationName,
                    throttlingTier: tier,
                    description: description
                };

                client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
                client["Application (individual)"].post_applications({
                        "body": application,
                        "Content-Type": "application/json"
                    },
                    function (success) {
                        window.location = "/store/application/" + applicationName;
                    },
                    function (error) {
                        alert("Error occurred while adding Application : " + applicationName);
                    });


            };
        }
    });

    $("#application-name").charCount({
        allowed: 70,
        warning: 50,
        counterText: i18n.t('Characters left: ')
    });
    $("#application-name").val('');

});



































