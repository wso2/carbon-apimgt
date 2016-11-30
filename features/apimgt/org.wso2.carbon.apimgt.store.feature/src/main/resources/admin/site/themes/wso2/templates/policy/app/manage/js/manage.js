var deleteAppPolicy = function (policyObject) {
    $("#messageModal div.modal-footer").html("");
    jagg.message({
        content: i18n.t('Policy deletion might affect current subscriptions. Are you sure you want to delete this policy? '),
        title: i18n.t('Confirm Deletion'),
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

$(function(){

    /***********************************************************
     *  data-tables config
     ***********************************************************/
	$('#app-policy').datatables_extended({
	     "fnDrawCallback": function(){
	       if(this.fnSettings().fnRecordsDisplay()<=$("#app-policy_length option:selected" ).val()
	     || $("#app-policy_length option:selected" ).val()==-1)
	       $('#app-policy_paginate').hide();
	       else $('#app-policy_paginate').show();
	     } ,
         "aoColumns": [
         null,
         null,
         null,
         { "bSortable": false },
         { "bSortable": false }
         ]
	});

});
