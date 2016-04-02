
$(document).ready(function() {

    $('#subscription-table').datatables_extended({
        "ajax": {
            "url": "/store/site/blocks/subscription/subscription-list/ajax/subscription-list.jag?action=getSubscriptionByApplication&app="+$("#subscription-table").attr('data-app'),
            "dataSrc": function ( json ) {
            	if(json.apis.length > 0){
            		$('#subscription-table-wrap').removeClass("hide");            		
            	}
            	else{
            		$('#subscription-table-nodata').removeClass("hide"); 
            	}
            	return json.apis
            }
        },
        "columns": [
            { "data": "apiName", 
			  "render": function ( data, type, rec, meta ) {
			  	  console.log(rec);
			      return '<a href="'+data+'">'+rec.apiName +' - '+ rec.apiVersion +'</a>';
			  }
			},
            { "data": "subscribedTier" },
            { "data": "subStatus" },
    	],  
    });

});