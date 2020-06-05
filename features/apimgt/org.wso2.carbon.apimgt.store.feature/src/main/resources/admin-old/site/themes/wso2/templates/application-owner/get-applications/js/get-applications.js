$( document ).ready(function() {
    $('#get-app input').bind('keyup', function(e) {
        if (e.keyCode == 13) {
            table.search( this.value ).draw();
        }
    });

    $("#application-actions").each(function(){
        var source   = $("#application-actions").html();
        var application_actions = Handlebars.compile(source);
        /***********************************************************
         *  data-tables config
         ***********************************************************/
        $('#get-app').datatables_extended({
            
            "serverSide": true,
            "paging": true,
            ajax: {
                "url" : "site/blocks/application-owner/get-applications/ajax/get-applications.jag?action=getApplicationsByTenantIdWithPagination",
                "type": "POST",
                "dataSrc": function ( json ) {
                    if(json.response.length > 0){
                        console.log("success")
                    }
                    return json.response
                }
            },
            "columns": [
                {"data": "name"},
                {"data": "owner"},
                {"data": "name", "render": function ( data, type, rec, meta ) {
                  return application_actions(rec);
              }
            }
            ]
        });
    });
});
