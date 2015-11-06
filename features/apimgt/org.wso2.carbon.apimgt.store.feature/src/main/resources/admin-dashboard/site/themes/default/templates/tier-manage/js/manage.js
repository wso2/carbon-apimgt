var deleteTier = function (tierObject) {
    jagg.message({
        content : i18n.t('confirmMsgs.tierDeleteConfirmMsg'),
        title : i18n.t('confirmMsgs.tierDeleteTitle'),
        type : 'confirm',
        okCallback:function(){
            var tier;
            jagg.post("/site/blocks/tier-manage/ajax/tier-manage.jag", {
                    action:"deleteTier",
                    tier:tierObject
                }, function (result) {
                    if (result.error == false) {
                        window.location.reload(true);
                    } else {
                        jagg.message({content:result.message,type:"error"});
                    }
                },
                "json");
            window.location.reload(true);
        }
    });
};
