var deleteGlobalPolicy = function (policyObject) {
    jagg.message({
        content: i18n.t('Policy deletion might affect current subscriptions. Are you sure you want to delete this policy?'),
        title: i18n.t('Confirm Deletion'),
        type:'confirm',
        anotherDialog:true,
        okCallback:function(){
           jagg.post("/site/blocks/policy/global/manage/ajax/global-policy-manage.jag", {
                action:"deleteGlobalPolicy",
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
	$('#global-policy').datatables_extended({
	     "fnDrawCallback": function(){
	       if(this.fnSettings().fnRecordsDisplay()<=$("#global-policy_length option:selected" ).val()
	     || $("#global-policy_length option:selected" ).val()==-1)
	       $('#global-policy_paginate').hide();
	       else $('#app-policy_paginate').show();
	     } ,
         "aoColumns": [
         null,
         null,
         { "bSortable": false },
         { "bSortable": false }
         ]
	});

});