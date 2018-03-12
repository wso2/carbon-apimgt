var inSequencesLoaded = false;

;(function ( $, window, document, undefined ) {
    Handlebars.logger.level = 0;
    var source = $("#resource-policy-ui-template").html();    
    var template;
    if(source != undefined && source !="" ){
        template = Handlebars.compile(source);
    }  

    Handlebars.registerHelper('ifCond', function (v1, operator, v2, options) {
        switch (operator) {
            case '==':
                return (v1 == v2) ? options.fn(this) : options.inverse(this);
            case '===':
                return (v1 === v2) ? options.fn(this) : options.inverse(this);
            case '<':
                return (v1 < v2) ? options.fn(this) : options.inverse(this);
            case '<=':
                return (v1 <= v2) ? options.fn(this) : options.inverse(this);
            case '>':
                return (v1 > v2) ? options.fn(this) : options.inverse(this);
            case '>=':
                return (v1 >= v2) ? options.fn(this) : options.inverse(this);
            case '&&':
                return (v1 && v2) ? options.fn(this) : options.inverse(this);
            case '||':
                return (v1 || v2) ? options.fn(this) : options.inverse(this);
            default:
                return options.inverse(this);
        }
    });    

    var pluginName = "resourceTierSelect";

    var defaults = {
         
    };

    // The actual plugin constructor
    function Plugin( element, options ) {
        this.element = $(element);
        // do not extend the api config
        this.config = options.config;
        this.options = $.extend( {}, defaults, options) ;        
        this._name = pluginName;
        this.init();
    }

    Plugin.prototype = {

        init: function() { 
            this.render();
            this.attach_events();
        },

        attach_events: function(){            
            this.element
            .on("change",".select_resource_policy", $.proxy(this.select_resource_policy, this))
            .on("click",".select_adv_policy_for_resource", $.proxy(this.select_adv_policy_for_resource, this));
        },

        select_resource_policy:function(e){
            var designer = APIDesigner();
            var path = $(e.currentTarget).attr("data-path");
            var method = $(e.currentTarget).attr("data-method");
            designer.api_doc.paths[path][method]["x-throttling-tier"] = $(e.currentTarget).val();
            $(".throttling_select[data-path='$.paths."+path+"."+method+"']").text($(e.currentTarget).val());
        },

        select_adv_policy_for_resource: function(e){
            this.render();
            this.element.find("#resource_policy_modal").modal('show');
        },

        render: function(){
            var designer = new APIDesigner();
            var context = { doc : designer.api_doc , tiers: this.options.tiers };
            this.element.html(template(context));            
        },

    };
    // A really lightweight plugin wrapper around the constructor,
    // preventing against multiple instantiations
    $.fn[pluginName] = function ( options ) {
        return this.each(function () {
            if (!$.data(this, "plugin_" + pluginName)) {
                $.data(this, "plugin_" + pluginName,
                new Plugin( this, options ));
            }
        });
    };

})( jQuery, window, document );

//hack to validate tiers
var tier_error = $("#tier_error").text();
function validate_tiers(){
    var selectedValues = [];
    $("input[name='tier']:checked").each(function() {
        selectedValues.push($(this).val());
    });    
    if(selectedValues && selectedValues.length > 0 ){
        $("#tier_error").addClass("hide");
        return true;
    }
    $("#tier_error").removeClass("hide").show().text(tier_error);
    return false;
}

function validateGatewaysSelected(){
    var atLeastOneIsSelected = false;
    var gateway_error = $("#gateway_error").text();
    $("input[name='gateways']:not(:checked)").each(function() {
        atLeastOneIsSelected = true;
    });
    if(atLeastOneIsSelected){
        $("#gateway_error").removeClass("hide").show().text(gateway_error);

    } else {
        $("#gateway_error").addClass("hide");
    }

}

function validateSubscription() {
    var subscriptionType = $('select#subscriptions').val();
    if (subscriptionType == 'specific_tenants') {
        var tenants = $('#tenants').val().trim();
        $("#subscriptions_error").remove();
        if (tenants.length > 0) {
            return true;
        }
        //set error
        $("#tenants").after('<label id="subscriptions_error" class="error" for="tenants" generated="true" style="display: block;">This field is required.</label>').focus();
        return false;
    } else {
        return true;
    }
}

$(document).ready(function(){
    $(".backend_tps").click(function() {     
        if($("input[name=backend_tps]:checked").val() == "unlimited"){
            $(".tps_boxes").hide();
        }
        else{
            $(".tps_boxes").removeClass("hide");
            $(".tps_boxes").show();
        }
    });

    $("#resource_adv_policy").click(function(){

        return false;
    });

    
    $("#resource-policy-select").resourceTierSelect({ tiers : TIERS });    

    //$('.multiselect').multiselect();

    $('.env').change(validateGatewaysSelected);
    $('#tier').change(validate_tiers);
    $('#transport_http').change(validate_Transports);
    $('#transport_https').change(validate_Transports);

    $("#manage_form").submit(function (e) {
      e.preventDefault();
    });

    $('select#subscriptions').change(function(e){
        var subscription = $('select#subscriptions').find(":selected").val();
        if (subscription == "current_tenant" || subscription == "all_tenants"){
            $('#tenantsDiv').hide();
        } else {
            $('#tenantsDiv').show();
        }
    });
    
    $('.default_version_check').change(function(){
        if($(this).is(":checked")){
            $('#default_version_checked').val($(this).val());
        }else{
            $('#default_version_checked').val("");
        }
    });

    validateAPITier();
    validateGatewaysSelected();

    $("select[name='apiTier']").change(function(){
    	       validateAPITier();
    	       $('.throttling_select').hide();
        
    });


    $("input[name='tier']").click(function() {
        // multipleValues will be an array
        var multipleValues = [];
        $("input[name='tier']:checked").each(function() {
            multipleValues.push($(this).val());
        });
        var countLength = $('#tiersCollection').length;
        if (countLength == 0) {
            $('<input>').attr('type', 'hidden')
                    .attr('name', 'tiersCollection')
                    .attr('id', 'tiersCollection')
                    .attr('value', multipleValues)
                    .appendTo('#manage_form');
        } else {
            $('#tiersCollection').attr('value', multipleValues);

        }
        validate_tiers();
    });

    $("select[name='apiPolicy']").change(function() {
            // multipleValues will be an array
            var multipleValues = $(this).val() || [];
            var countLength = $('#apiPolicyCollection').length;
            if (countLength == 0) {

                $('<input>').attr('type', 'hidden')
                        .attr('name', 'apiPolicyCollection')
                        .attr('id', 'apiPolicyCollection')
                        .attr('value', multipleValues)
                        .appendTo('#manage_form');
            } else {
                $('#apiPolicyCollection').attr('value', multipleValues);

            }

        });
    $("select[name='subPolicy']").change(function() {
            // multipleValues will be an array
            var multipleValues = $(this).val() || [];
            var countLength = $('#subPolicyCollection').length;
            if (countLength == 0) {

                $('<input>').attr('type', 'hidden')
                        .attr('name', 'subPolicyCollection')
                        .attr('id', 'subPolicyCollection')
                        .attr('value', multipleValues)
                        .appendTo('#manage_form');
            } else {
                $('#subPolicyCollection').attr('value', multipleValues);

            }

     });

    if ( $("#toggleSequence").attr('checked') ) {
	$('#toggleSequence').parent().next().show();
    } 
    else {
	$('#toggleSequence').parent().next().hide();
    }
    
    if( $("#toggleThrottle").is(":checked")) {
    	$(this).parent().parent().parent().next().children().next().children().show();
    } 
    else {
    	$(this).parent().parent().parent().next().children().next().children().hide();
    }
    validate_Transports();
});

$('.js_hidden_section_title').click(function(){
        var $next = $(this).next();
        var $i = $('i',this);
        if($next.is(":visible")){
            $next.hide();
            $i.removeClass('glyphicon glyphicon-chevron-down');
            $i.addClass('glyphicon glyphicon-chevron-right');
        }else{
            $next.show();
            $i.removeClass('glyphicon glyphicon-chevron-right');
            $i.addClass('glyphicon glyphicon-chevron-down');
        }
    });

$("#toggleThrottle").change(function(e){
    if($(this).is(":checked")){
        $(this).parent().parent().parent().next().children().next().children().show();
    }else{
    	$(this).parent().parent().parent().next().children().next().children().hide();
    	$('#productionTps').val('');
    	$('#sandboxTps').val('');
    }
});

$(".api_level_policy").click(function() {
    showHideResourceLevelTierSelection();
});

function showHideResourceLevelTierSelection() {
    if($("input[name=api_level_policy]:checked").val() == "api_level_policy"){
        $('#api-level-policy-section').show();
        $("#resource-policy-select").addClass("hide");
        $('#enableApiLevelPolicy').val("true");
        $('.throttling_select').hide();
    }
    else{
        $('#api-level-policy-section').hide();
        $("#resource-policy-select").removeClass("hide");
        $('#enableApiLevelPolicy').val("false");
        $('.throttling_select').show();
    }
}

var transport_error = $("#transport_error").text();
function validate_Transports(){
    var checkedHttpTransport=$('#transport_http').is(":checked");
    var checkedHttpsTransport=$('#transport_https').is(":checked");
    if(checkedHttpTransport || checkedHttpsTransport){
        $("#transport_error").addClass("hide");
        return true;
    }
    $("#transport_error").removeClass("hide").show().text(transport_error);
    return false;
}

function validateAPITier(){
     var apiTier = $( "#api_level_policy").prop('checked');
        var designer = APIDesigner();
        if(apiTier){
            designer.setApiLevelPolicy(true);
            designer.render_resources();
        }else{
            designer.setApiLevelPolicy(false);
            designer.render_resources();
        }
}
