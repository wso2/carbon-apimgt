var addGlobalPolicy = function () {

    jagg.post("/site/blocks/policy/global/ajax/global-policy-edit.jag", {
            action:"addGlobalPolicy",
            policyName:$('#policyName').val(),
            description:$('#description').val().trim(),
            requestCount:$('#requestCount').val(),
            unitTime:$('#unitTime').val(),
            timeUnit:$('#timeUnit').val(),
            siddhiQuery:$('#siddhiQuery').val()

        }, function (result) {
            if (result.error == false) {
                location.reload(true);
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        },
        "json");

};


