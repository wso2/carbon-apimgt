
var print_date = function(data, type, full, meta){
    return moment(data.substring(0, 19)).format("YYYY-MM-DD, h:mm:ss a");;
}

var show_apps = function(data, type, row){
	return "<a href='/analytics/site/pages/all-statistics.jag?page=applications-list&stat=all-stat&withSubscriptions=true&user="+row["user_id"]+"'>Show Applications | </a>"
	+ "<a href='/analytics/site/pages/all-statistics.jag?page=apis-list&stat=all-stat&withSubscriptions=true&user="+row["user_id"]+"'>Show APIs</a>";
}


var table = null; 
$(document).ready(function() {

    table = $('#example').DataTable( {
        ajax: "/analytics/site/blocks/stats/developers-list/ajax/stats.jag",
        "columns": [
            { "title": "Name" , "data":"user_id"},
            { "title": "Email Address", "data":"email_address" },
            { "title": "Subscribed Time", "data" : "created_time", render: print_date },
            { "title": "", "class": "center", data : "apps" , "render": show_apps }
        ]
    } );   

    $('#today-btn').on('click',function(){
        table.ajax.url("/analytics/site/blocks/stats/developers-list/ajax/stats.jag").load();
    });
    
});