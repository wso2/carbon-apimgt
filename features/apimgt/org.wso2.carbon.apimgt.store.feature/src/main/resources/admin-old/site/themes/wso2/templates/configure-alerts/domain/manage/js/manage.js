var deleteConfiguration = function (domainName, configurationName) {
    jagg.message({
        content:i18n.t('Are you sure you want to deactivate this configuration? '),
        title:i18n.t('Confirm Deactivation'),
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

