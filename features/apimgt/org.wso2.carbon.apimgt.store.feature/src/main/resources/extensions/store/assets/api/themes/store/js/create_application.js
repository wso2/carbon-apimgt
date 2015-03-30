$(function(){
	var obtainFormMeta=function(formId){
		return $(formId).data();
	};
	$(document).ready(function(){
		var appName = $('#overview_name').val();
		$('#form-application-create').ajaxForm({
			success:function(){
				var options=obtainFormMeta('#form-application-create');
				var message = {};
				message.text = '<div><i class="icon-briefcase"></i> Application: '+appName+' has been created.</div>';
				message.type = 'success';
				message.layout ='topRight';
				noty(message);
			},
			error:function(){
				var message ={};
				message.text = '<div><i class="icon-briefcase"></i> Application: '+appName+' has not been created.</div>';
				message.type = 'error';
				message.layout = 'topRight';
				noty(message);
			}
		});
	});

});