var addGlobalPolicy = function () {

    jagg.post("/site/blocks/policy/global/ajax/global-policy-edit.jag", {
            action:"addGlobalPolicy",
            globalPolicyString:$('#globalPolicyString').val()

        }, function (result) {
            if (result.error == false) {
                location.href = 'site/pages/global-policy-edit.jag'
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        },
        "json");

};


