
$(document).ready(function() {

$("#subscription-actions").each(function(){
    var source   = $("#subscription-actions").html();
    var subscription_actions = Handlebars.compile(source);

    var sub_list = $('#subscription-table').datatables_extended({
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
            { "data": "apiName",
              "render": function ( data, type, rec, meta ) {
                  return subscription_actions(rec);
              }
            }
    	],  
    });

    $('#subscription-table').on( 'click', 'a.deleteApp', function () {
        sub_list
        .row( $(this).parents('tr') )
        .remove()
        .draw();
    });
});    

$("#application-actions").each(function(){
    var source   = $("#application-actions").html();
    var application_actions = Handlebars.compile(source);


    var app_list = $('#application-table').datatables_extended({
        "ajax": {
            "url": "/store/site/blocks/application/application-list/ajax/application-list.jag?action=getApplications",
            "dataSrc": function ( json ) {
                if(json.applications.length > 0){
                    $('#application-table-wrap').removeClass("hide");                  
                }
                else{
                    $('#application-table-nodata').removeClass("hide"); 
                }
                return json.applications
            }
        },
        "columns": [
            { "data": "name",
              "render": function(data, type, rec, meta){
                if(rec.groupId !="" && rec.groupId != undefined)
                    return data+ " (Shared)";
                else
                    return data;
              }
            },
            { "data": "tier" },
            { "data": "status",
              "render": function(status, type, rec, meta){
                var result;        
                if(status=='APPROVED'){
                    result='ACTIVE';
                } else if(status=='REJECTED') {
                    result='REJECTED';
                } else{
                    result='INACTIVE';
                }
                return new Handlebars.SafeString(result);  
              }
            },
            { "data": "apiCount" },
            { "data": "name",
              "render": function ( data, type, rec, meta ) {
                  rec.isActive = false;
                  if(rec.status=='APPROVED'){
                      rec.isActive = true;
                  }
                  return application_actions(rec);
              }
            },
        ],  
    });

    $('#application-table').on( 'click', 'a.deleteApp', function () {
        app_list
        .row( $(this).parents('tr') )
        .remove()
        .draw();
    });
});


});