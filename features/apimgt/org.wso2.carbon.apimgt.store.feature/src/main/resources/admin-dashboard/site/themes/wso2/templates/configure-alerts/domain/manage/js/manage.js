var deleteConfiguration = function (domainName, configurationName) {
    jagg.message({
        content:'Are you sure you want to delete this domain configuration? ',
        title:'Confirm Deletion',
        type:'confirm',
        anotherDialog:true,
        okCallback:function(){
            jagg.post("/site/blocks/configure-alerts/domain/manage/ajax/alerts-domain-manage.jag", {
                action:"deleteConfiguration",
                domainName:domainName,
                configurationName:configurationName
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

