var saveEmailList = function (emailList) {
    jagg.post("/site/blocks/honeypot-api-email-alert-configuration/ajax/honeypot-api-email-alert-configuration.jag", {
        action: "saveEmailList",
        emailList: emailList

    }, function (result) {

        console.log(result);
        if (!result.error) {

            jagg.message({content: i18n.t("Successfully saved"), type: "info"});
          // $("#unsubscribeBtn").show();

        } else {
            jagg.message({content: result.message, type: "error"});
        }
    }, "json");

}

var deleteEmails = function() {

    jagg.post("/site/blocks/honeypot-api-email-alert-configuration/ajax/honeypot-api-email-alert-configuration.jag", {
        action: "deleteEmails"
    }, function (result) {

        //console.log(result);
        if (!result.error) {

            jagg.message({content: i18n.t("Successfully saved"), type: "info"});


            $(":checkbox").each(function () {
                $(this).removeAttr('checked');
            });

            $("#tokenfield").tagsinput('removeAll');
            $("#unsubscribeBtn").show();

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

    $('#tokenfield').on('beforeItemRemove', function(event){
        //deleteEmails();
        //event.cancel = true;
    });

    $("#cancelBtn").click(function () {

        location.reload();

    });

    $("#unsubscribeBtn").click(function () {

        deleteEmails();

    });
    $("#saveBtn").click(function () {
        //alert($("#tokenfield").val());

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

           //if (checkedValues.length > 0) {
                 var emailList = $("#tokenfield").val();
                if(emailList) {
                    saveEmailList(emailList);
                 }
                 else{
                    jagg.message({content: i18n.t("Please enter at least one email address") , type: "error"});
                }
        //  } else {
        //         jagg.message({content: i18n.t("Please select at least one alert type") , type: "error"});
        //      }
        }
    });

});