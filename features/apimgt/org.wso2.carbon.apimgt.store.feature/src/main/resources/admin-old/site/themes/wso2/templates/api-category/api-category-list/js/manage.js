var deleteAPICategory = function (uuid) {
    $("#messageModal div.modal-footer").html("");
    jagg.message({
        content:i18n.t('Are you sure you want to delete this API Category?'),
        title:i18n.t('Confirm Deletion'),
        type:'confirm',
        anotherDialog:true,
        okCallback:function() {
            jagg.post("/site/blocks/api-category/api-category-list/ajax/api-category-list.jag", {
                    action:"deleteAPICategory",
                    uuid:uuid
                }, function (result) {
                    if (result.error == false) {
                        window.location.reload(true);
                    } else {
                        jagg.message({content:result.message,type:"error"});
                    }
                },
                "json"
            );
        }
    });
};
