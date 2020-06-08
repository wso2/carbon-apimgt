var deleteSubscriptionPolicy = function (policyObject) {
    $("#messageModal div.modal-footer").html("");
    jagg.message({
        content:i18n.t('Policy deletion might affect current subscriptions. Are you sure you want to delete this policy?'),
        title:i18n.t('Confirm Deletion'),
        type:'confirm',
        anotherDialog:true,
        okCallback:function(){
            jagg.post("/site/blocks/policy/subscription/manage/ajax/subscription-policy-manage.jag", {
                    action:"deleteSubscriptionPolicy",
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
	$('#subscription-policy').datatables_extended({
	     "fnDrawCallback": function(){
	       if(this.fnSettings().fnRecordsDisplay()<=$("#subscription-policy_length option:selected" ).val()
	     || $("#subscription-policy_length option:selected" ).val()==-1)
	       $('#subscription-policy_paginate').hide();
	       else $('#subscription-policy_paginate').show();
	     } ,
         "aoColumns": [
         null,
         null,
         null,
         null,
         null,
         { "bSortable": false },
         { "bSortable": false }
         ]
	});

});
