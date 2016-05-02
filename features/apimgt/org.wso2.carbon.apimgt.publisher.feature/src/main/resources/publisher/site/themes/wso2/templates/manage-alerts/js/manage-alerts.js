var saveAlertTypes = function (alertTypesIDs, emailList, checkedValues) {


    jagg.post("/site/blocks/manage-alerts/ajax/manage-alerts.jag", {
        action: "saveAlertTypes",
        checkedList: alertTypesIDs.toString(),
        emailList: emailList,
        checkedValues: checkedValues.toString()

    }, function (result) {

        //console.log(result);
        if (!result.error) {

            jagg.message({content: i18n.t("info.successfullySaved"), type: "info"});

        } else {
            jagg.message({content: result.message, type: "error"});
        }
    }, "json");

}


$(document).ready(function () {

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

    $("#saveBtn").click(function () {

        if ($(".token").hasClass("invalid")) {
            jagg.message({content: i18n.t("errorMsgs.invalidEmailEntered") , type: "error"});
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
                    jagg.message({content: i18n.t("errorMsgs.atleastOneEmailNeeded") , type: "error"});
                }
            } else {
                jagg.message({content: i18n.t("errorMsgs.atLeastOneAlertTypeNeeded") , type: "error"});
            }
        }
    });

});
