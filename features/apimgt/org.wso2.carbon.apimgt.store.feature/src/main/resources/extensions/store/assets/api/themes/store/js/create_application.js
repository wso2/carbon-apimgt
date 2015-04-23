$(function(){
	var obtainFormMeta=function(formId){
		return $(formId).data();
	};

    var refreshApplicationList = function () {
        $.ajax({
            url: "https://localhost:9443/store/apis/applications", success: function (result) {
                var partial = 'list_applications';
                var container = 'list_applications';
                var data = {};
                data.applications = result;
                renderPartial(partial, container, result);
            }
        });
    };

    var partial = function (name) {
        return '/extensions/assets/api/themes/' + caramel.themer + '/partials/' + name + '.hbs';
    };
    var id = function (name) {
        return '#' + name;
    };

    var renderPartial = function (partialName, containerName, data, fn) {
        fn = fn || function () {
        };
        if (!partialName) {
            throw 'A template name has not been specified for template key ' + partialKey;
        }
        if (!containerName) {
            throw 'A container name has not been specified for container key ' + containerKey;
        }
        var obj = {};
        obj[partialName] = partial(partialName);
        caramel.partials(obj, function () {
            var template = Handlebars.partials[partialName](data);
            $(id(containerName)).html(template);
            fn(containerName);
        });
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
                refreshApplicationList();
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