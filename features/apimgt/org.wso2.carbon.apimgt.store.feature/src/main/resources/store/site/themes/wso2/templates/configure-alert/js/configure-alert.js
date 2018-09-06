$(function() {
    var appsElement = $("#appName");
    var apisElement = $("#apiName");
    populateAppList();

    /**
     * Populate Application Name 'select' element with subscribed application list
    **/
    function populateAppList() {
        var params = {action: "getApplications"};
        
        $.get("../blocks/application/application-list/ajax/application-list.jag", params, function(data) {
            if (data && data.error == false) {
                $.each(data.applications, function() {
                    appsElement.append($("<option />").val(this.id).text(this.name));
                });

                appsElement.selectpicker('refresh');
                populateApiList();
            }
        });
    }

    /**
     * Populate API Name 'select' element with subscribed list of APIs
     * provider--apiName--apiVersion format is used to list the APIs. This is to
     * avoid complexity in the UI and the logic required to show the API list
    **/
    function populateApiList() {
        var appId = $("#appName option:selected").text();
        var params = {action: "getSubscriptionByApplication", app: appId, groupId: ""};
        apisElement.empty();

        $.get("../blocks/subscription/subscription-list/ajax/subscription-list.jag", params, function(data) {
            if (data && data.error == false) {
                $.each(data.apis, function() {
                    var apiId = this.apiName + '--' + this.apiVersion;
                    apisElement.append($("<option />").val(apiId).text(apiId));
                });

                apisElement.selectpicker('refresh');
            }
        });
    }

    // Register on change event listener for Application Name 'select' element
    appsElement.on('changed.bs.select', function (e) {
        populateApiList();
    });

});
