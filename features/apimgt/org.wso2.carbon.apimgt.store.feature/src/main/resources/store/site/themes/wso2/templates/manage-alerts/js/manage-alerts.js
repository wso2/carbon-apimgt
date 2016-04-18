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

    $('#tokenfield')

        .on('tokenfield:createtoken', function (e) {
            var data = e.attrs.value.split('|')
            e.attrs.value = data[1] || data[0]
            e.attrs.label = data[1] ? data[0] + ' (' + data[1] + ')' : data[0];

        })

        .on('tokenfield:createdtoken', function (e) {
            // Über-simplistic e-mail validation
            var re = /\S+@\S+\.\S+/
            var valid = re.test(e.attrs.value)
            if (!valid) {
                $(e.relatedTarget).addClass('invalid');
            }
        })

        .on('tokenfield:edittoken', function (e) {
            if (e.attrs.label !== e.attrs.value) {
                var label = e.attrs.label.split(' (')
                e.attrs.value = label[0] + '|' + e.attrs.value
            }
        })

        .on('tokenfield:removedtoken', function (e) {
            //alert('Token removed! Token value was: ' + e.attrs.value)
        })

        .tokenfield()


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
