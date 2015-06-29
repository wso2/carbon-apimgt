
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
		var ajaxURL = caramel.context + '/assets/api/apis/copyAPI';
		

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
				        
		                BootstrapDialog.show({
			                type: BootstrapDialog.TYPE_SUCCESS,
			                title: 'success',
			                message: 'Successfully Created New API Version',
			                buttons: [{
			                
				                label: 'Close',
				                action: function(dialogItself){
					                dialogItself.close();
					                window.location.href = caramel.context+'/assets/api/list';
				                }
				            
			            	}]

			            });
				           
				        },
				    error : function(result) {		                
		                
		                BootstrapDialog.show({
			                type: BootstrapDialog.TYPE_DANGER,
			                title: 'Error',
			                message: 'Error Occured while Create New Version',
			                buttons: [{
			                
				                label: 'Close',
				                action: function(dialogItself){
					                dialogItself.close();
					                window.location.href = caramel.context+'/assets/api/list';
				                }
				            
			            	}]

			            });
                   	},
				          
				   
				    dataType: "json"
		}); 


	});
});