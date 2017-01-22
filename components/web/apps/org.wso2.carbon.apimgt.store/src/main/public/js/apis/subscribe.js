$(function () {
    $('.page-content').on('click', 'button', function (e) {
        var element = e.target;
        if (element.id == "subscribe-button") {
            var applicationId = $("#application-list option:selected").val();
            if (applicationId == "-" || applicationId == "createNewApp") {
                alert('Please select an application before subscribing')
                return;
            }
            $(this).html(i18n.t('Please wait...')).attr('disabled', 'disabled');
            var tier = $("#tiers-list").val();
            var apiIdentifier = $("#apiId").val();

            var client = new SwaggerClient({
                url: 'https://apis.wso2.com/api/am/store/v0.10/swagger.json',
                success: function (swaggerData) {
                    var subscriptionData = {};
                    subscriptionData.tier = tier;
                    subscriptionData.applicationId = applicationId;
                    subscriptionData.apiIdentifier = apiIdentifier;

                    client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", "Bearer 12770569-28a9-3864-9f7b-c3fcdc16b890", "header"));
                    client["Subscription (individual)"].post_subscriptions({
                            "body": subscriptionData,
                            "Content-Type": "application/json"
                        },
                        function (jsonData) {
                            $("#subscribe-button").html('Subscribe');
                            $("#subscribe-button").removeAttr('disabled');
                            var subscription = jsonData.obj;

                            location.href = "../site/pages/application.jag?" + urlPrefix+"#subscription";

                            //TODO : Embedding message model




                        },
                        function (error) {
                            alert("Error occurred while adding Application : " + applicationName);
                        });


                    alert("ee")
                }
            });
        }
    });
});