var saveAlertTypes = function (alertTypesIDs, emailList, checkedValues) {


    jagg.post("/site/blocks/manage-alerts/ajax/manage-alerts.jag", {
        action: "saveAlertTypes",
        checkedList: alertTypesIDs.toString(),
        emailList: emailList,
        checkedValues: checkedValues.toString()

    }, function (result) {

        //console.log(result);
        if (!result.error) {

            jagg.message({content: i18n.t("Successfully saved"), type: "info"});
            $("#unsubscribeBtn").show();

        } else {
            jagg.message({content: result.message, type: "error"});
        }
    }, "json");

}


var unSubscribeAlerts = function() {

    jagg.post("/site/blocks/manage-alerts/ajax/manage-alerts.jag", {
        action: "unSubscribe"
    }, function (result) {

        //console.log(result);
        if (!result.error) {

            jagg.message({content: i18n.t("Successfully saved"), type: "info"});


            $(":checkbox").each(function () {
                $(this).removeAttr('checked');
            });
            
            $("#tokenfield").tagsinput('removeAll');
            $("#unsubscribeBtn").hide();

        }else {
            if (result.message == "AuthenticateError") {
                jagg.showLogin();
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }
    }, "json");

}

$(document).ready(function () {

    var notChecked = [], checked = [], checkedValues = [];
    $(":checkbox").each(function () {
        id = this.value;
        values = this.id;
        this.checked ? checked.push(id) : notChecked.push(id);
        if (this.checked) {
            checkedValues.push(values);
        }

    });

    if (checkedValues.length > 0) {

        $("#unsubscribeBtn").show();
    }

    $('#tokenfield').on('beforeItemAdd', function (event) {
        /* Validate url */

        var re = /\S+@\S+\.\S+/
        var valid = re.test(event.item)

        if (!valid) {
            event.cancel = true;
        }
        else {
            event.cancel = false;
        }
    });

    $("#cancelBtn").click(function () {

        location.reload();

    });

    $("#unsubscribeBtn").click(function () {

        unSubscribeAlerts();

    });
    $("#saveBtn").click(function () {

        if ($(".token").hasClass("invalid")) {
            jagg.message({content: i18n.t("Could not save. You have entered an invalid email address.") , type: "error"});
        } else {

            var notChecked = [], checked = [], checkedValues = [];
            $(":checkbox").each(function () {
                id = this.value;
                values = this.id;
                this.checked ? checked.push(id) : notChecked.push(id);
                if (this.checked) {
                    checkedValues.push(values);
                }

            });

            if (checkedValues.length > 0) {
                var emailList = $("#tokenfield").val();
                if(emailList) {
                    saveAlertTypes(checked, emailList, checkedValues);
                }else{
                    jagg.message({content: i18n.t("Please enter at least one email address") , type: "error"});
                }
            } else {
                jagg.message({content: i18n.t("Please select at least one alert type") , type: "error"});
            }
        }
    });

});
