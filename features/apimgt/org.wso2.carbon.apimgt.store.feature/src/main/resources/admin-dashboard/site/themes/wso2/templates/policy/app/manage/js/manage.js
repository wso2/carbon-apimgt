var deleteAppPolicy = function (policyObject) {
    jagg.message({
        content:'Policy deletion might affect current subscriptions. Are you sure you want to delete this policy? ',
        title:'Confirm Deletion',
        type:'confirm',
        anotherDialog:true,
        okCallback:function(){
           jagg.post("/site/blocks/policy/app/manage/ajax/app-policy-manage.jag", {
                action:"deleteAppPolicy",
                policy:policyObject
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
