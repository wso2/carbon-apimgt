var deleteTier = function (tierObject) {
    jagg.message({
        content:'Tier deletion might affect current subscriptions. Are you sure you want to delete this tier? ',
        title:'Confirm Deletion',
        type:'confirm',
        anotherDialog:true,
        okCallback:function(){
            jagg.post("/site/blocks/tier/manage/ajax/tier-manage.jag", {
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
        }
    });
};

$(function(){

    /***********************************************************
     *  data-tables config
     ***********************************************************/
	$('#manage-tiers').datatables_extended({
	     "fnDrawCallback": function(){
	       if(this.fnSettings().fnRecordsDisplay()<=$("#manage-tiers_length option:selected" ).val()
	     || $("#manage-tiers_length option:selected" ).val()==-1)
	       $('#manage-tiers_paginate').hide();
	       else $('#manage-tiers_paginate').show();
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
