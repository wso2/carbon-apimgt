var deleteAPIPolicy = function (policyObject) {
    $("#messageModal div.modal-footer").html("");
    jagg.message({
        content: i18n.t('Policy deletion might affect current subscriptions. Are you sure you want to delete this policy? '),
        title: i18n.t('Confirm Deletion'),
        type:'confirm',
        anotherDialog:true,
        okCallback:function(){
           jagg.post("/site/blocks/policy/resource/policy-list/ajax/api-policy-manage.jag", {
                action:"deleteAPIPolicy",
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
	$('#api-policy').datatables_extended({
	     "fnDrawCallback": function(){
	       if(this.fnSettings().fnRecordsDisplay()<=$("#api-policy_length option:selected" ).val()
	     || $("#api-policy_length option:selected" ).val()==-1)
	       $('#api-policy_paginate').hide();
	       else $('#api-policy_paginate').show();
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