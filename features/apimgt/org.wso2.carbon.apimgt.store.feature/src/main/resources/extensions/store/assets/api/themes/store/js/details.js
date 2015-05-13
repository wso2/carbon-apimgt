$(document).ready(function () {
     $("#subscribe-button").click( function()
           {
    var applicationId = $("#application-list").val();
    var applicationName = $("#application-list option:selected").text();
    var apiName=$("#apiname").val();
    var version=$("#version").val();
    var provider=$("#provider").val();
    if (applicationId == "-" || applicationId == "createNewApp") {
        var message ={};
                message.text = '<div><i class="icon-briefcase"></i> Select an application.</div>';
                message.type = 'error';
                message.layout = 'topRight';
                noty(message);       
    }
     var tier = $("#tiers-list").val();
    $.ajax({
    type: "POST",
    url: caramel.context + '/apis/apisubscriptions',
    data: {
        action:"addSubscription",
        applicationId:applicationId,
        name:apiName,
        version:version,
        provider:provider,
        tier:tier
    },
    success: function (result) {
        $("#subscribe-button").html('Subscribe');
        $("#subscribe-button").removeAttr('disabled');
        if (result.data.error == false) {
                var message ={};
                message.text = '<div><i class="icon-briefcase"></i> Successfully API subscribed.</div>';
                message.type = 'success';
                message.layout = 'topRight';
                noty(message);                
              
        } else {
           var message ={};
                message.text = '<div><i class="icon-briefcase"></i> API subscribe process failed.</div>';
                message.type = 'error';
                message.layout = 'topRight';
                noty(message);

        }
          
        },
    dataType: "json"
    });   
    $('#application-list').change(
            function(){
                if($(this).val() == "createNewApp"){                   
                    window.location.href =caramel.context+ '/asts/api/my_applications';
                }
            }
            );
});

