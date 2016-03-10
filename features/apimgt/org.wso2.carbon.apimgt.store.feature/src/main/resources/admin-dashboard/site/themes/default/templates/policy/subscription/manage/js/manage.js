var deleteTier = function (policyObject) {
    jagg.message({
        content:'Tier deletion might affect current subscriptions. Are you sure you want to delete this tier? ',
        title:'Confirm Deletion',
        type:'confirm',
        anotherDialog:true,
        okCallback:function(){
           /* jagg.post("/site/blocks/policy/app/manage/ajax/app-policy-manage.jag", {
                action:"deleteTier",
                tier:tierObject
                }, function (result) {
                    if (result.error == false) {
                        window.location.reload(true);
                    } else {
                        jagg.message({content:result.message,type:"error"});
                    }
                },
            "json");*/
        }
    });
};
