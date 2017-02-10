var mode = "OVERWRITE";

function tierChanged(element) {
    var index = element.selectedIndex;
    var selectedDesc = $("#tierDescriptions").val().split(",")[index];
    $("#tierHelpStr em").text(selectedDesc);
}

$(function () {

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".green").insertBefore(prev).css('top','0px').addClass('active');
    _renderTopNavBar();
    _renderApplicationAddPage();

    $(document).on('click', '#application-add-button', function () {
        $("#appAddForm").validate({
            submitHandler: function (form) {
                addApplication();
            }
        });
    });
});

var _renderTopNavBar = function() {
    var mode = "OVERWRITE";
    var data = {};
    data.isApplicationAdd = true;
    //Render Applications listing page
    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.top-navbar", data,
        "top-navbar", mode, {
            onSuccess: function () {

            }, onFailure: function (message, e) {
            }
        });
}

var _renderApplicationAddPage = function(){
    var swaggerClient = new SwaggerClient({
        url: swaggerURL,
        success: function (data) {
            swaggerClient.setSchemes(["http"]);
            swaggerClient.setHost("localhost:9090");

            //TODO:Replace this once the tierList retrieval api is implemented
            var tier = {};
            tier.name = "50PerMin";
            var list = [];
            list.push(tier);
            //TODO:Replace this once the tierList retrieval api is implemented

            UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.application-add", {"list":list},
                "application-add", mode, {
                    onSuccess: function () {
                    }, onFailure: function (message, e) {
                        alert(e);
                    }
                });
        }
    });
}

var addApplication = function () {

    var swaggerClient = new SwaggerClient({
        url: swaggerURL,
        success: function (swaggerData) {
            swaggerClient.setSchemes(["http"]);
            swaggerClient.setHost("localhost:9090");

            var applicationName = $("#application-name").val();
            var tier = $("#appTier").val();
            var goBack = $("#goBack").val();
            var description = $("#description").val();

            var application = {
                name: applicationName,
                throttlingTier: tier,
                description: description
            };

            swaggerClient["Application (individual)"].post_applications({
                    "body": application,
                    "Content-Type": "application/json"
                },
                function (data) {
                    window.location = "/store/applications/" + data.obj.applicationId;
                },
                function (error) {
                    alert("Error occurred while adding Application : " + applicationName + " " + error.obj.message);
                });
        }
    });
};


