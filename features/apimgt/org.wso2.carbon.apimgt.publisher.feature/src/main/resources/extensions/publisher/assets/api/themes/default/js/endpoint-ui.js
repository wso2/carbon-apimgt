// Define the form jsons
// We use jsonform to generate the form html

if (typeof APP === 'undefined') {
    var APP = { form : {} };
}

$(document).ready(function () {

    APP.form.advance_endpoint_config =
    {
        schema: {
            format: {
                type: 'string',
                title: 'endpointUi.Format',
                'enum': ['soap11', 'soap12', 'POX', 'REST', 'GET', 'leave-as-is'],
                description:'Message format for the endpoint.',
                'default': 'leave-as-is'
            },
            optimize: {
                type: 'string',
                title: 'endpointUi.Optimize',
                'enum': ['SWA', 'MTOM', 'leave-as-is'],
                description:'Method to optimize the attachments.',
                'default': 'leave-as-is'
            },
            suspendErrorCode: {
                title: 'endpointUi.Error_Codes',
                type: 'array',
                description: 'A list of error codes.If these error codes are received from the endpoint, the endpoint will be suspended.',
                items:{
                    'enum': [ "101507", "101508", "101505", "101506", "101509", "101500", "101510", "101001", "101000", "101503", "101504" ,"101501"],
                    type: 'string'
                }

            },
            suspendDuration: {
                title: 'endpointUi.Intial Duration (Millis)',
                description:'The duration that the endpoint is suspended for the first time after the receiving the suspend error codes.',
                type: 'number'
            },
            suspendMaxDuration: {
                title: 'endpointUi.Max Duration (Millis)',
                description:'The maximum duration that the endpoint is suspended after the receiving the suspend error codes.',
                type: 'number'
            },
            factor: {
                title: 'endpointUi.Factor',
                description:'The duration to suspend can vary from the first time suspension to the subsequent time. The factor value decides the suspense duration variance between subsequent suspensions.',
                type: 'number'
            },
            retryErroCode: {
                title: 'endpointUi.Error Codes',
                type: 'array',
                description:'A list of error codes. If these error codes are received from the endpoint, the request will be subjected to a timeout.',
                items:{
                    'enum': [ "101507", "101508", "101505", "101506", "101509", "101500", "101510", "101001", "101000", "101503", "101504" ,"101501"],
                    type: 'string'
                }
            },
            retryTimeOut: {
                title: 'endpointUi.Retries Before Suspension',
                description:'The number of re-tries in case of a timeout, caused by the above listed error codes.',
                type: 'number'
            },
            retryDelay: {
                title: 'endpointUi.Retry Delay(Millis)',
                description:'The delay between retries, in milliseconds.',
                type: 'number'
            },
            actionSelect: {
                title: 'endpointUi.Action',
                type: 'string',
                description:'The action to be done at a timeout situation. You can select from: 1) Never Timeout 2) Discard Message 3) Execute Fault Sequence',
                'enum': [ "neverTimeout", "discard", "fault" ]
            },
            actionDuration: {
                title: 'endpointUi.Duration (Millis)',
                description:'The duration in milliseconds before considering a request as timeout.',
                type: 'number'
            }
        },
        form: [{
            "type": "fieldset",
            "htmlClass": "ae_message_content",
            "title": 'endpointUi.Message Content',
            "items": [{
                key: 'format',
                'titleMap': {
                    'soap11': "SOAP 1.1",
                    'soap12': "SOAP 1.2",
                    'POX': 'endpointUi.pox',
                    'REST': 'endpointUi.rest',
                    'GET': "GET",
                    'leave-as-is': 'endpointUi.Leave As-Is'
                }
            }, {
                key: 'optimize',
                'titleMap': {
                    'soap11': "SwA",
                    'soap12': "MTOM",
                    'leave-as-is': "endpointUi.Leave As-Is"
                }
            }]
        }, {
            "type": "fieldset",
            "title": "endpointUi.Endpoint Suspend State",
            "items": [ {
                key:'suspendErrorCode',
                type:'multiselect',
                fieldHtmlClass:'error_codes_selection',
                'titleMap':{
                    "101507": 	"endpointUi.101507",
                    "101508": 	"endpointUi.101508",
                    "101505": 	"endpointUi.101505",
                    "101506": 	"endpointUi.101506",
                    "101509": 	"endpointUi.101509",
                    "101500": 	"endpointUi.101500" ,
                    "101510": 	"endpointUi.101510",
                    "101001": 	"endpointUi.101001",
                    "101000": 	"endpointUi.101000",
                    "101503": 	"endpointUi.101503",
                    "101504": 	"endpointUi.101504",
                    "101501": 	"endpointUi.101501"
                }
            },
                { key:'suspendDuration'},
                { key:'suspendMaxDuration'},
                { key:'factor'}]

        }, {
            "type": "fieldset",
            "title": "endpointUi.Endpoint Timeout State",
            "items": [ {
                key:'retryErroCode',
                type:'multiselect',
                fieldHtmlClass:'error_codes_selection',
                'titleMap':{
                    "101507": 	"endpointUi.101507",
                    "101508": 	"endpointUi.101508",
                    "101505": 	"endpointUi.101505",
                    "101506": 	"endpointUi.101506",
                    "101509": 	"endpointUi.101509",
                    "101500": 	"endpointUi.101500" ,
                    "101510": 	"endpointUi.101510",
                    "101001": 	"endpointUi.101001",
                    "101000": 	"endpointUi.101000",
                    "101503": 	"endpointUi.101503",
                    "101504": 	"endpointUi.101504",
                    "101501": 	"endpointUi.101501"
                }
            },
                'retryTimeOut', 'retryDelay']
        }, {
            "type": "fieldset",
            "title": "endpointUi.Connection Timeout",
            "items": [
                {
                    key: 'actionSelect',
                    'titleMap': {
                        "neverTimeout": "endpointUi.Never timeout",
                        "discard": "endpointUi.Discard message",
                        "fault": "endpointUi.Execute fault sequence"
                    },
                    value:'fault'
                },{
                    key:'actionDuration',
                    value: '30000'
                }
            ]
        }
        ]
    };

    APP.form.http_endpoint = {
        "schema": {

            "production_endpoints": {
                "title": "endpointUi.Production Endpoint",
                "type": "endpoint",
                "fieldHtmlClass": "input-xlarge validateEndpoints"
            },
            sandbox_endpoints: {
                title: "endpointUi.Sandbox Endpoint",
                type: 'endpoint',
                "fieldHtmlClass": "input-xlarge validateEndpoints"
            }
        },
        form: [
            'production_endpoints', 'sandbox_endpoints'
        ]
    };

    APP.form.address_endpoint = {
        "schema": {

            "production_endpoints": {
                "title": "endpointUi.Production Endpoint",
                "type": "endpoint"
            },
            sandbox_endpoints: {
                title: "endpointUi.Sandbox Endpoint",
                type: 'endpoint'

            }
        },
        form: [{
            key: 'production_endpoints',
            "fieldHtmlClass": "input-xlarge validateEndpoints"
        }, {
            key: 'sandbox_endpoints',
            "fieldHtmlClass": "input-xlarge validateEndpoints"
        }]
    };

    APP.form.wsdl_endpoint = {
        "schema": {

            "production_endpoints": {
                "title": "endpointUi.Production WSDL",
                "type": "endpoint",
                "urlType":"wsdl",
                "fieldHtmlClass": "input-xlarge validateEndpoints"
            },
            'wsdlendpointService': {
                title: "endpointUi.Service",
                type: 'text'
            },
            'wsdlendpointPort': {
                title: "endpointUi.Port",
                type: 'text'
            },
            "sandbox_endpoints": {
                title: "endpointUi.Sandbox WSDL",
                type: 'endpoint',
                "urlType":"wsdl",
                "fieldHtmlClass": "input-xlarge validateEndpoints"
            },
            'wsdlendpointServiceSandbox': {
                title: "endpointUi.Service",
                type: 'text'
            },
            'wsdlendpointPortSandbox': {
                title: "endpointUi.Port",
                type: 'text'
            }
        },
        form: [
            'production_endpoints',
            {
                key : 'wsdlendpointService',
                "fieldHtmlClass":"validateProdWSDLService"
            },{
                key:'wsdlendpointPort',
                "fieldHtmlClass":"validateProdWSDLPort"
            }
            , 'sandbox_endpoints',
            {
                key : 'wsdlendpointServiceSandbox',
                "fieldHtmlClass":"validateSandboxWSDLService"
            },{
                key:'wsdlendpointPortSandbox',
                "fieldHtmlClass":"validateSandboxWSDLPort"
            }
        ]
    };

    APP.form.failover_endpoint = {
        "schema": {
            "production_endpoints": {
                "title": "endpointUi.Production Endpoint",
                "type": "endpoint",
                "fieldHtmlClass": "input-xlarge"
            },
            "production_failovers": {
                "title": "endpointUi.Production Fail-over Endpoints",
                "type": "array",
                "items": {
                    "type": "endpoint",
                    "title": "endpointUi.Endpoint"+" {{idx}} )"
                }
            },
            sandbox_endpoints: {
                title: "endpointUi.Sandbox Endpoint",
                type: 'endpoint'
            },
            "sandbox_failovers": {
                "title": "endpointUi.Sandbox Fail-over Endpoints",
                "type": "array",
                "items": {
                    "type": "endpoint",
                    "title": "endpointUi.Endpoint"+" {{idx}} )"
                }
            }
        },
        form: [
            'production_endpoints', 'production_failovers', 'sandbox_endpoints', 'sandbox_failovers'
        ]
    };

    APP.form.load_balance_endpoint = {
        "schema": {
            algoCombo: {
                type: 'string',
                title: "endpointUi.Algorithm",
                'enum': ['org.apache.synapse.endpoints.algorithms.RoundRobin', 'other'],
                'default': 'org.apache.synapse.endpoints.algorithms.RoundRobin'
            },
            algoClassName: {
                type: 'string',
                title: "endpointUi.AlgorithmOther"
            },
            sessionManagement: {
                type: 'string',
                title: "endpointUi.Session Management",
                'enum': [ 'http', 'soap', 'simpleClientSession','none']
            },
            sessionTimeOut: {
                title: "endpointUi.Session Timeout (Mills) ",
                type: 'number'
            },
            failOver: {
                title: "failOver.title",
                'enum': [ 'True', 'False']
            },
            "production_endpoints": {
                "title": "endpointUi.Production Endpoints",
                "type": "array",
                "items": {
                    "type": "endpoint",
                    "title": "endpointUi.Endpoint"+" {{idx}} )"
                }
            },
            "sandbox_endpoints": {
                "title": "endpointUi.Sandbox Endpoints",
                "type": "array",
                "items": {
                    "type": "endpoint",
                    "title": "endpointUi.Endpoint"+" {{idx}} )"
                }
            }
        },
        form: [
            'production_endpoints',
            {
                key:'algoCombo',
                titleMap:{
                    'org.apache.synapse.endpoints.algorithms.RoundRobin' : "endpointUi.Round-robin",
                    "other":"Other"
                },
                "onChange": function (evt) {
                    if($(evt.target).val() == 'other'){
                        $('.algo_class_field').val('');
                        $('.algo_class_name').show('fast');
                    }
                    else{
                        $('.algo_class_name').hide('fast');
                        $('.algo_class_field').val($(evt.target).val());
                    }
                }
            }
			,{
		            key:"failOver",
		            value:'True'
		        }
			,{
                key:"algoClassName",
                htmlClass: "hide algo_class_name",
                "fieldHtmlClass": "required algo_class_field",
                value:'org.apache.synapse.endpoints.algorithms.RoundRobin'
            },
            {
                key:'sessionManagement',
                titleMap:{
                    'http':"endpointUi.Transport",
                    'soap':"endpointUi.SOAP",
                    'simpleClientSession':"endpointUi.Client ID",
		    		'none':"endpointUi.none",
                },
                value:'none'
            },
            {
                key:'sessionTimeOut'
            },
            'sandbox_endpoints'
        ]
    };

// this will convert the config in to json form value
    APP.endpointConfig2JsonForm = function(config){

        var value = jQuery.extend({}, config);
        delete value.production_endpoints;
        delete value.sandbox_endpoints;

        if(config.production_endpoints)
            if( config.production_endpoints instanceof Array){
                value.production_endpoints = [];
                for (var i = 0; i < config.production_endpoints.length; i++) {
                    value.production_endpoints[i] = config.production_endpoints[i].url
                }
            }
            else{
                value.production_endpoints = config.production_endpoints.url;
            }

        if(config.sandbox_endpoints)
            if(config.sandbox_endpoints instanceof Array){
                value.sandbox_endpoints = [];
                for (var i = 0; i < config.sandbox_endpoints.length; i++) {
                    value.sandbox_endpoints[i] = config.sandbox_endpoints[i].url
                }
            }
            else{
                value.sandbox_endpoints = APP.endpoint_config.sandbox_endpoints.url;
            }

        if(config.production_failovers)
            if( config.production_failovers != undefined){
                value.production_failovers = [];
                for (var i = 0; i < config.production_failovers.length; i++) {
                    value.production_failovers[i] = config.production_failovers[i].url
                }
            }

        if(config.sandbox_failovers)
            if(config.sandbox_failovers != undefined){
                value.sandbox_failovers = [];
                for (var i = 0; i < config.sandbox_failovers.length; i++) {
                    value.sandbox_failovers[i] = config.sandbox_failovers[i].url
                }
            }

        return value;
    }

    APP.populateAdvanceData = function(config){
        if(config.production_endpoints)
            if( config.production_endpoints instanceof Array){
                for (var i = 0; i < config.production_endpoints.length; i++) {
                    $(".advance_endpoint_config[field-name='production_endpoints["+i+"]']").attr('ep-config-data',JSON.stringify( config.production_endpoints[i].config ));
                }
            }
            else{
                $(".advance_endpoint_config[field-name='production_endpoints']").attr('ep-config-data',JSON.stringify( config.production_endpoints.config ));
            }

        if(config.sandbox_endpoints)
            if(config.sandbox_endpoints instanceof Array){
                for (var i = 0; i < config.sandbox_endpoints.length; i++) {
                    $(".advance_endpoint_config[field-name='sandbox_endpoints["+i+"]']").attr('ep-config-data',JSON.stringify(config.sandbox_endpoints[i].config));
                }
            }
            else{
                $(".advance_endpoint_config[field-name='sandbox_endpoints']").attr('ep-config-data',JSON.stringify(config.sandbox_endpoints.config));
            }

        if(config.production_failovers)
            if( config.production_failovers instanceof Array){
                for (var i = 0; i < config.production_failovers.length; i++) {
                    $(".advance_endpoint_config[field-name='production_failovers["+i+"]']").attr('ep-config-data',JSON.stringify( config.production_failovers[i].config ));
                }
            }

        if(config.sandbox_failovers)
            if(config.sandbox_failovers instanceof Array){
                for (var i = 0; i < config.sandbox_failovers.length; i++) {
                    $(".advance_endpoint_config[field-name='sandbox_failovers["+i+"]']").attr('ep-config-data',JSON.stringify(config.sandbox_failovers[i].config));
                }
            }
    }

    if($('#endpoint_config').val() != undefined && $('#endpoint_config').val()!=''){
        APP.endpoint_config = JSON.parse($('#endpoint_config').val());
    }

    $('#endpoint_type').change(function(){
        var type = $(this).find("option:selected").attr('value');
        $('#endpoint_form').hide().html('');

        if(APP.endpoint_config != undefined && APP.endpoint_config.endpoint_type == type){
            APP.form[type+'_endpoint'].value = APP.endpointConfig2JsonForm(APP.endpoint_config);
        }
        APP.ep_form = $('#endpoint_form').jsonForm(APP.form[type+'_endpoint']);
        $('#endpoint_form').show('fast');

        if(APP.endpoint_config != undefined && APP.endpoint_config.endpoint_type == type){
            APP.populateAdvanceData(APP.endpoint_config);
        }

        //hide the wsdl field if the endpoint type is wsdl
        if(type == 'wsdl'){
            $('.api_wsdl').hide();
        }
        else{
            $('.api_wsdl').show();
        }
    });

    // first load for edit page
    if(APP.endpoint_config != undefined && APP.endpoint_config.endpoint_type != undefined){
        $('#endpoint_type').val(APP.endpoint_config.endpoint_type);
    }
    $('#endpoint_type').trigger('change');

    $( "#endpoint_form" ).on( "click", ".advance_endpoint_config", function() {
        $('form#advance_form').html('');

        //APP.form.advance_endpoint_config.value = jQuery.parseJSON($(this).attr('ep-config-data'));
        APP.form.advance_endpoint_config.value = jQuery.parseJSON("{}");
        //alert(APP.form.advance_endpoint_config);
        $('form#advance_form').jsonForm(APP.form.advance_endpoint_config);
        $('.error_codes_selection').multiselect({
            buttonText: function(options, select) {
                if (options.length == 0) {
                    return this.nonSelectedText + ' <b class="caret"></b>';
                }
                else {
                    if (options.length > 3) {

                        return options.length + ' ' + this.nSelectedText + ' <b class="caret"></b>';
                    }
                    else {
                        var selected = '';
                        options.each(function() {
                            var label = ($(this).attr('label') !== undefined) ? $(this).attr('label') : $(this).val();
                            selected += label + ', ';
                        });
                        return selected.substr(0, selected.length - 2) + ' <b class="caret"></b>';
                    }
                }
            }
        });

        if($('#endpoint_type').val() == 'address'){
            $(".ae_message_content").show();
        }
        else{
            $(".ae_message_content").hide();
        }

        $('#advance_endpoint_config').modal('show');

        APP.advance_endpoint_button = $(this);

    });



    APP.form.advance_endpoint_config.onSubmit = function (errors, values) {
        if (errors) {
            $('#res').show();
            $('#res').html('<span>Please correct the error fields.</span>');
        } else {
            $('#res').hide();
            $('#advance_endpoint_config').modal('hide');
        }
        console.log(JSON.stringify(values));
        APP.advance_endpoint_button.attr('ep-config-data',JSON.stringify(values));
        console.log(APP.advance_endpoint_button.attr('ep-config-data'));
        return false;
    };


    $("#advance_ep_submit").click(function(){
        $('#advance_form').submit();
    });

    // when the add api or
    $('#addNewAPIButton , #updateButton , .manageSaveButton').bind('click',
        function() {
            var ec = APP.ep_form.getValues();
            ec.endpoint_type = $('#endpoint_type').val();
            $('.advance_endpoint_config').each(function(index, el){
                var ep_config = jQuery.parseJSON($(el).attr('ep-config-data'));
                var name = $(el).attr('field-name');
                var field = name.replace(/\[([0-9]*)\]$/, '');
                var value_index = name.replace(/([a-zA-Z0-9_]*)/, '').replace('[','').replace(']','');
                if(value_index == ''){
                    if(ec[field] != undefined && ec[field] !="")
                        ec[field] = { url: ec[field] , config: ep_config };
                    else
                        ec[field] = undefined;
                }
                else{
                    if(ec[field][value_index] != undefined && ec[field][value_index] !="")
                        ec[field][value_index] = { url: ec[field][value_index] , config: ep_config };
                    else{
                        ec[field].splice(value_index,1);
                    }
                }
                return true;
            });

            //clear undefined urls
            if(ec.production_endpoints instanceof Array && ec.production_endpoints.length == 0){
                ec.production_endpoints = undefined;
            }
            if(ec.sandbox_endpoints instanceof Array && ec.sandbox_endpoints.length == 0){
                ec.sandbox_endpoints = undefined;
            }

            $('#endpoint_config').val(JSON.stringify(ec));

            if(ec.endpoint_type == 'wsdl' && ec.production_endpoints != null){
                $('#wsdl').val(ec.production_endpoints.url);
            }

            return true;
        }
    );

    APP.update_ep_config = function() {
        var ec = APP.ep_form.getValues();
        ec.endpoint_type = $('#endpoint_type').val();
       $('.advance_endpoint_config').each(function(index, el){
        console.log($(el).attr('ep-config-data'));
           var ep_config = jQuery.parseJSON($(el).attr('ep-config-data'));
            var name = $(el).attr('field-name');
            var field = name.replace(/\[([0-9]*)\]$/, '');
            var value_index = name.replace(/([a-zA-Z0-9_]*)/, '').replace('[','').replace(']','');
            if(value_index == ''){
                if(ec[field] != undefined && ec[field] !="")
                    ec[field] = { url: ec[field] , config: ep_config };
                else
                    ec[field] = undefined;
            }
            else{
                if(ec[field][value_index] != undefined && ec[field][value_index] !="")
                    ec[field][value_index] = { url: ec[field][value_index] , config: ep_config };
                else{
                    ec[field].splice(value_index,1);
                }
            }
            return true;
        });

        //clear undefined urls
        if(ec.production_endpoints instanceof Array && ec.production_endpoints.length == 0){
            ec.production_endpoints = undefined;
        }
        if(ec.sandbox_endpoints instanceof Array && ec.sandbox_endpoints.length == 0){
            ec.sandbox_endpoints = undefined;
        }

        $('#endpoint_config').val(JSON.stringify(ec));

        if(ec.endpoint_type == 'wsdl' && ec.production_endpoints != null){
            $('#wsdl').val(ec.production_endpoints.url);
        }

        return true;
    };
});


APP.is_production_endpoint_specified = function(){
    APP.update_ep_config();
    var endpoint_config = jQuery.parseJSON($('#endpoint_config').val());
    console.log(endpoint_config);
    return endpoint_config.production_endpoints != undefined
};

APP.is_sandbox_endpoint_specified = function(){
    APP.update_ep_config();
    var endpoint_config = jQuery.parseJSON($('#endpoint_config').val());
    console.log(endpoint_config);
    return endpoint_config.sandbox_endpoints != undefined
};

APP.is_production_wsdl_endpoint_service_specified = function(){
    APP.update_ep_config();
    var endpoint_config = jQuery.parseJSON($('#endpoint_config').val());
    return endpoint_config.wsdlendpointService != "" && endpoint_config.wsdlendpointService != undefined
   
};

APP.is_production_wsdl_endpoint_port_specified = function(){
    APP.update_ep_config();
    var endpoint_config = jQuery.parseJSON($('#endpoint_config').val());
    return endpoint_config.wsdlendpointPort != "" && endpoint_config.wsdlendpointPort != undefined
};

APP.is_sandbox_wsdl_endpoint_service_specified = function(){
    APP.update_ep_config();
    var endpoint_config = jQuery.parseJSON($('#endpoint_config').val());
    return endpoint_config.wsdlendpointServiceSandbox != "" && endpoint_config.wsdlendpointServiceSandbox != undefined
};

APP.is_sandbox_wsdl_endpoint_port_specified = function(){
    APP.update_ep_config();
    var endpoint_config = jQuery.parseJSON($('#endpoint_config').val());
    return endpoint_config.wsdlendpointPortSandbox != "" && endpoint_config.wsdlendpointPortSandbox != undefined
};
