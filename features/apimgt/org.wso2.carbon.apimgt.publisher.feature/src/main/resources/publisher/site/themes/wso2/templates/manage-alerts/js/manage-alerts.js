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
    $( "#saveBtn" ).click(function() {

        var notChecked = [], checked = [], checkedValues = [];
        $(":checkbox").each(function() {
            id=this.value;
            values = this.id;
            this.checked ? checked.push(id) : notChecked.push(id);
            if(this.checked) {
                checkedValues.push(values);
            }

        });
        var emailList =  $("#emailListTextArea").val();
        saveAlertTypes(checked,emailList,checkedValues);
    });

});
