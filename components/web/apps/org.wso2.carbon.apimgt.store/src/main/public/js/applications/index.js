$(function () {

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".green").insertBefore(prev).css('top','0px').addClass('active');

    var bearerToken = "Basic YWRtaW46YWRtaW4=";
    var applicationClient = new SwaggerClient({
        url: swaggerURL + "applications",
        success: function (swaggerData) {
            applicationClient.setBasePath("");
            //List Applications
            applicationClient.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
            applicationClient.default.applicationsGet({"responseContentType": 'application/json'},
                function (data) {
                    $.ajax({
                        url: '/store/public/components/root/base/templates/applications/appListRow.hbs',
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
                applicationClient.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
                applicationClient.default.applicationsApplicationIdDelete({"applicationId": appId},
                    function (success) {
                        //TODO: Reload element only
                        window.location.reload(true);
                    },
                    function (error) {
                        alert("Error occurred while deleting application")
                    });
            });
        }
    });
});