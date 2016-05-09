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
		 null,
         { "bSortable": false },
         ]
	});

});
