var deleteLabel = function (uuid) {
    $("#messageModal div.modal-footer").html("");
    jagg.message({
        content:i18n.t('Are you sure you want to delete this label?'),
        title:i18n.t('Confirm Deletion'),
        type:'confirm',
        anotherDialog:true,
        okCallback:function(){
            jagg.post("/site/blocks/label/label-list/ajax/label-manage.jag", {
                    action:"deleteLabel",
                    uuid:uuid
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
