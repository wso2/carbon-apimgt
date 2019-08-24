var saveEmailList = function (emailList) {
    jagg.post("/site/blocks/bot-detection-email-configuration/ajax/bot-detection-email-configuration.jag", {
        action: "saveEmailList",
        emailList: emailList
    }, function (result) {
        if (!result.error) {
            jagg.message({content: i18n.t("Successfully saved"), type: "info"});
            $("#tokenfield").val('');

        } else {
            jagg.message({content: result.message, type: "error"});
        }
        setTimeout(function() { window.location=window.location;},1600);
    }, "json");

}

var deleteEmails = function (uuid) {
    $("#messageModal div.modal-footer").html("");
    jagg.message({
        content:i18n.t('Are you sure you want to delete this email?'+uuid),
        title:i18n.t('Confirm Deletion'),
        type:'confirm',
        anotherDialog:true,
        okCallback:function(){
            jagg.post("/site/blocks/bot-detection-email-configuration/ajax/bot-detection-email-configuration.jag", {
                    action:"deleteEmails",
                    uuid:uuid
                }, function (result) {
                    if (result.error == false) {
                        window.location.reload(true);
                    } else {
                        jagg.message({content:result.message,type:"error"});
                    }
                },
                "json");
        }
    });
};

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

    $("#deleteBtn").click(function () {

        deleteEmails();

    });
    $("#saveBtn").click(function () {

        if ($(".token").hasClass("invalid")) {
            jagg.message({content: i18n.t("Could not save. You have entered an invalid email address.") , type: "error"});
        } else {
               var emailList = $("#tokenfield").val();
               if(emailList) {
                  saveEmailList(emailList);
                }
                 else{
                     jagg.message({content: i18n.t("Please enter at least one email address") , type: "error"});
                }
        }
    });

});