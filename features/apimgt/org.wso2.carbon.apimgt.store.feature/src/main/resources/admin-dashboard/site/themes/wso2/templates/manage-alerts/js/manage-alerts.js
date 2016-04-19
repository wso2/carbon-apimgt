var saveAlertTypes = function (alertTypesIDs,emailList,checkedValues) {


    jagg.post("/site/blocks/manage-alerts/ajax/manage-alerts.jag", {
        action:"saveAlertTypes",
        checkedList:alertTypesIDs.toString(),
        emailList:emailList,
        checkedValues : checkedValues.toString()

    }, function (result) {

        //console.log(result);
        if (!result.error) {

            jagg.message({content:"Successfully saved", type:"info"});

        } else {
            jagg.message({content:result.message, type:"error"});
        }


    }, "json");

}


$( document ).ready(function() {

    $('#tokenfield').on('beforeItemAdd', function(event) {
        /* Validate url */

        var re = /\S+@\S+\.\S+/
        var valid = re.test(event.item)

        if (!valid)
        {
            event.cancel = true;
        }
        else
        {
            event.cancel = false;
        }
    });

    $( "#saveBtn" ).click(function() {

        if ($(".token").hasClass("invalid")) {
            jagg.message({content:"Could not save. You have entered an invalid email address.", type:"error"});
        }else {


            var notChecked = [], checked = [], checkedValues = [];
            $(":checkbox").each(function () {
                id = this.value;
                values = this.id;
                this.checked ? checked.push(id) : notChecked.push(id);
                if (this.checked) {
                    checkedValues.push(values);
                }

            });
            var emailList = $("#tokenfield").val();
            saveAlertTypes(checked, emailList, checkedValues);
        }
    });

});
