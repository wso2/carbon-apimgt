$(document).ready(function() {
    $.validator.addMethod('contextExists', function(value, element) {
        if (value.charAt(0) != "/") {
            value = "/" + value;
        }
        var contextExist = false;
        var oldContext=$('#spanContext').text();
        jagg.syncPost("/site/blocks/item-add/ajax/add.jag", { action:"isContextExist", context:value,oldContext:oldContext },
                      function (result) {
                          if (!result.error) {
                              contextExist = result.exist;
                          }
                      });
        return this.optional(element) || contextExist != "true";
    }, i18n.t('Context exists.'));
    $.validator.addMethod('tenantContextExists', function(value, element) {
        if (value.charAt(0) != "/") {
            value = "/" + value;
        }
        var illegalChars = /(\/t\/|\/t$)/;
        return !illegalChars.test(value)
    }, '/t/ not allowed in the context field.');
    $.validator.addMethod('apiNameExists', function(value, element) {
        var apiNameExist = false;
        jagg.syncPost("/site/blocks/item-add/ajax/add.jag", { action:"isAPINameExist", apiName:value },
                      function (result) {
                          if (!result.error) {
                              apiNameExist = result.exist;
                          }
                      });
        return this.optional(element) || apiNameExist != "true";
    }, i18n.t('Duplicate API name.'));

    $.validator.addMethod('validContext', function (value, element) {
        return value != "/";
    }, i18n.t('Invalid context format'));

    $.validator.addMethod('selected', function(value, element) {
        return value!="";
    }, i18n.t('Select a value for the tier.'));

    $.validator.addMethod('validRegistryName', function(value, element) {
        var illegalChars = /([~!@#;%^*()+={}|\<>"',&])/;
        return !illegalChars.test(value);
    }, i18n.t('Name contains one or more illegal characters  (~ ! @ #  ; % ^ & * + = { } () | &lt; &gt;, \' " \\ ) .'));

    $.validator.addMethod('validAPIName', function(value, element) {
        var illegalChars = /([~!@#;:%^*()+={}|\\<>"',&\/$])/;
        return !illegalChars.test(value);
    }, i18n.t('Name contains one or more illegal characters  (~ ! @ #  ; : % ^ & * + = { } () | &lt; &gt;, \' " \\ $) .'));

    $.validator.addMethod('validContextTemplate', function(value, element) {
        var illegalChars = /([~!@#;%^&*+=\|\\<>\"\',])/;
        return !illegalChars.test(value);
    }, i18n.t('Name contains one or more illegal characters  (~ ! @ #  ; % ^ & * + = | &lt; &gt;, \' " \\ ) .'));

    $.validator.addMethod('validateVersionOnlyContext', function (value, element) {
        if (value == "{version}" || value == "/{version}") {
            return false;
        }
        return true;
    }, i18n.t('Invalid context format'));

    $.validator.addMethod('validTemplate', function(value, element) {
        return value.indexOf("{}") == -1
    }, i18n.t('Invalid context format'));

    $.validator.addMethod('validateUrl', function(value, element){
        var validUrlRegex = /^(http|https):\/\/(.)+/g;
        value = value.replace(/^\s+|\s+$/g, "");
        if(value != ""){
            return validUrlRegex.test(value);
        }
        return true;
    }, i18n.t('Please provide a valid URL.'));

    $.validator.addMethod('noSpace', function(value, element) {
        return !/\s/g.test(value);
    },i18n.t('Name contains white spaces.'));

    $.validator.addMethod('validInput', function(value, element) {
        var illegalChars = /([<>\"\'])/;
        return !illegalChars.test(value);
    }, i18n.t('Input contains one or more illegal characters  (& &lt; &gt; \'  " '));

    $.validator.addMethod('validateRoles', function(value, element) {
        var valid = false;
        var oldContext=$('#spanContext').text();
        jagg.syncPost("/site/blocks/item-add/ajax/add.jag", { action:"validateRoles", roles:value },
                      function (result) {
                          if (!result.error) {
                              valid = result.response;
                          }
                      });
        return this.optional(element) || valid == true;
    }, i18n.t('Invalid role name[s]'));

    $.validator.addMethod('validateUserRoles', function(value, element) {
        var valid = false;
        jagg.syncPost("/site/blocks/item-add/ajax/add.jag", { action:"validateRoles", roles:value , validate:true},
            function (result) {
                if (!result.error) {
                    valid = result.response;
                }
            });
        return this.optional(element) || valid == true;
    }, i18n.t('Role[s] provided must be associated with the API creator.'));

    $.validator.addMethod('validateEndpoints', function (value, element){
        return APP.is_production_endpoint_specified() || APP.is_sandbox_endpoint_specified();
    }, i18n.t('A Production or Sandbox URL must be provided.'));

    $.validator.addMethod('validateDefaultEndpoint', function (value, element){
        var endpointType = $('#endpoint_type').val();
        if(endpointType == "default")   {
            if(($('#inSequence :selected').text() != "None") || $('#inSeqFile').val() != "")  {
                return true;
            } else  {
                return false;
            }
        }
        return true;
    }, i18n.t('You must upload or select a message mediation policy'));
    
    $.validator.addMethod('validateProdWSDLService', function (value, element){
    	if (APP.is_production_endpoint_specified()) {
    		return APP.is_production_wsdl_endpoint_service_specified();
    	} 
    	return true;        
    }, i18n.t('Service Name must be provided for WSDL endpoint.'));

    $.validator.addMethod('validateProdWSDLPort', function (value, element){
    	if (APP.is_production_endpoint_specified()) {
    		return APP.is_production_wsdl_endpoint_port_specified();
    	} 
    	return true;   
    }, i18n.t('Service Port must be provided for WSDL endpoint.'));
    
    $.validator.addMethod('validateSandboxWSDLService', function (value, element){
    	if (APP.is_sandbox_endpoint_specified()) {
    		return APP.is_sandbox_wsdl_endpoint_service_specified();
    	}
        return true;
    }, i18n.t('Service Name must be provided for WSDL endpoint.'));
    
    $.validator.addMethod('validateSandboxWSDLPort', function (value, element){
    	if (APP.is_sandbox_endpoint_specified()) {
    		return APP.is_sandbox_wsdl_endpoint_port_specified();
    	}
    	return true;
    }, i18n.t('Service Port must be provided for WSDL endpoint.'));

    $.validator.addMethod('validateImageFile', function (value, element) {
        if ($(element).val() == "") {
            return true;
        }
        else {
            var validFileExtensions = ["jpg", "jpeg", "bmp", "gif", "png"];
            var ext = $(element).val().split('.').pop().toLowerCase();
            return ($.inArray(ext, validFileExtensions)) > -1;
        }
        return true;
    }, i18n.t('Invalid file format. Valid formats are .jpg, .jpeg, .bmp, .gif and .png'));

    $.validator.addMethod('validateForwardSlashAtEnd', function(value, element) {
        var regexForwardSlashAtEnd = /.+\/$/;
        return !regexForwardSlashAtEnd.test(value);
    }, i18n.t('Input should not contain / at the end'));

    $.validator.addMethod('validateAPIVersion', function(value, element)    {
        var illegalChars = /([\]\[\{\}\(\)\`~!@#;%^&*+=\|\\<>\"\'\/,])/;
        return !illegalChars.test(value);
    }, i18n.t('Version contains one or more illegal characters  ( [ ] { } ( ) ` ~ ! @ #  ; % ^ & * + = | &lt; &gt;, \' " \\) .'));

    $.validator.addMethod('validateDescriptionLength', function(value, element) {
        return value.length <= 20000;
    }, i18n.t('Description cannot exceed 2000 characters'));
    
    $.validator.addMethod('email-validation', function(value, element) {
        var validationQuery = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
        return validationQuery.test(value)
    }, 'Please enter a valid email address.');
});
