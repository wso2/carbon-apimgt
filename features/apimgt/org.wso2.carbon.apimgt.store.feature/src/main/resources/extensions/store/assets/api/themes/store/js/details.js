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
                return;      
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
                noty({
	        text: "Congratulations! You have successfully subscribed to the API. Please go to 'My Subscriptions' page to review your subscription and generate keys.", 
                layout: 'topRight',
                type: 'confirm',          
                closeWith: ['click', 'hover'],         
	        buttons: [
		{addClass: 'btn btn-primary', text: 'Go to My Subscriptions', onClick: function($noty) {
				$noty.close();
                                window.location.href =caramel.context+ '/asts/api/my_subscriptions';
				
			}
		},
		{addClass: 'btn btn-other', text: 'Stay on this page', onClick: function($noty) {
				$noty.close();
                                window.location.href = window.location.href;				
			}
		}
	]
});             
              
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


    }
);
$('#application-list').change(
            function(){
                if($(this).val() == "createNewApp"){                   
                    window.location.href =caramel.context+ '/asts/api/my_applications';
                }
            }
            );

});
