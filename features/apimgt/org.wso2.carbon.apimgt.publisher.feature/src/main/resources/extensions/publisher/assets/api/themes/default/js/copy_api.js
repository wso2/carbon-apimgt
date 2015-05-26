
$(function(){

	Handlebars.registerHelper('json', function(context) {
    	return JSON.stringify(context);
	});

	

	$('#copy-api-button').on('click',function(){
		$('#copy-api-container').css('display','inline');
	});

	$('#copy-api-cancel-button').on('click',function(){
		$('#copy-api-container').css('display','none');
	});

	$('#copyAPIDefaultVersion').change(function(){
	    var defaultVersionSelected = this.checked ? "default_version" : "";
	    $('#copyAPIDefaultVersionValue').val(defaultVersionSelected);
	    
	});

	$('#copy-api-done-button').on('click',function(){
		$('#copy-api-container').css('display','none');
		var newVersion = $('#copy-api-new-version-txt').val();
		var apiName = $('#overviewAPIName').val();
		var provider = $('#copyAPIProvider').val();
		var version = $('#overviewAPIVersion').val();
		var defaultVersionValue = $('#copyAPIDefaultVersionValue').val();
		if(newVersion == null || (!newVersion.trim())){
			var msgTxt = $('#copyAPIEmptyVersion').val();
             BootstrapDialog.show({
                type: BootstrapDialog.TYPE_DANGER,
                title: 'Error',
                message: msgTxt,
                buttons: [{
                
	                label: 'Close',
	                action: function(dialogItself){
	                    dialogItself.close();
	                }
	            
            	}]

            });

            return;	
		}
		
		$('#copyAPINewVersion').val(newVersion);
		var ajaxURL = caramel.context + '/asts/api/apis/copyAPI';
		

		 $.ajax({
				    type: "POST",
				    url: ajaxURL,
				    data: {
				        action:"createNewAPIVersion",
				        name:apiName,
				        version:version,
				        provider:provider,
				        newVersion:newVersion,
				        defaultVersionValue:defaultVersionValue
			
				    },
				    success: function (result) {
				        
				        if (result.data.error == false) {
				                var message ={};
				                message.text = '<div><i class="icon-briefcase"></i> Successfully New API Version Created.</div>';
				                message.type = 'success';
				                message.layout = 'topRight';
				                noty(message);                
				              
				        } else {
				           var message ={};
				                message.text = '<div><i class="icon-briefcase"></i> New API Version creation process failed.</div>';
				                message.type = 'error';
				                message.layout = 'topRight';
				                noty(message);
				        }
				          
				        },
				    dataType: "json"
				}); 


	});
});