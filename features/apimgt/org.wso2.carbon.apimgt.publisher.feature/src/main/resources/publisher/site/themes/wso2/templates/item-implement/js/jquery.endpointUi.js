;(function ( $, window, document, undefined ) {

    var source = $("#endpoint-ui-template").html();    
    var template;
    if(source != undefined && source !="" ){
        template = Handlebars.compile(source);
    }        

    source = $("#advance-endpoint-ui-template").html();    
    var template2;
    if(source != undefined && source !="" ){
        template2 = Handlebars.compile(source);
    }      

    Handlebars.registerHelper("inc", function(value, options)
    {
        return parseInt(value) + 1;
    });

    Handlebars.registerHelper( 'concat', function(param1, param2) {
        return param1 + param2;
    });

    Handlebars.registerHelper("contains", function( value, array, options ){
        // fallback...
        array = ( array instanceof Array ) ? array : [array];
        return (array.indexOf(value) > -1) ? options.fn( this ) : "";
    });

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

    var pluginName = "apimEndpointUi";

    var defaults = {
         
    };

    // The actual plugin constructor
    function Plugin( element, options ) {
        this.element = $(element);
        this.options = $.extend( {}, defaults, options) ;

        this.config = {"production_endpoints": {"url": "","config": null},"sandbox_endpoints": {"url": "","config": null},"endpoint_type": "http" };
        if(this.options.config != undefined && this.options.config != "")
            this.config = $.extend( {}, this.config, this.options.config);

        //following is a workaround to make prod & sand box an array
        if(!(this.config.production_endpoints instanceof Array)){
            this.config.production_endpoints = [ this.config.production_endpoints ];
        }
        if(!(this.config.sandbox_endpoints instanceof Array)){
            this.config.sandbox_endpoints = [ this.config.sandbox_endpoints ];
        }

        this._name = pluginName;
        this.init();
    }

    Plugin.prototype = {

        init: function() {
            this.render();
            if(this._get_production_endpoint_type())
                this._set_selected_ep(this._get_production_endpoint_type());
            if(ws == true || ws == "true")
                this._set_selected_ep("ws");
            this.attach_events();
        },        

        attach_events: function(){
            this.element
            .on("change","#endpoint_type", $.proxy(this._on_endpoint_changed, this))
            .on("change","#failover", $.proxy(this._on_failover_checked, this))
            .on("change","#load_balance", $.proxy(this._on_load_balance_checked, this))
            .on("click",".add_ep", $.proxy(this._on_add_ep, this))
            .on("click",".remove_ep", $.proxy(this._on_remove_ep, this))
            .on("change",".load_balance_property", $.proxy(this._load_balance_property_change, this))
            .on("change",".endpoint_input", $.proxy(this._on_endpoint_change, this))
            .on("click",".ad_config", $.proxy(this._on_advance_endpoint, this))
            .on("click","#advance_ep_submit", $.proxy(this._on_advance_endpoint_submit, this));
        },

        render: function(){
            var context = jQuery.extend({}, this.config);
            context[this.config.endpoint_type] = true;
            //if loadbalanced and failover is enables set the ep type
            context[this._get_selected_ep()] = true;
            if(this.config.failOver == "False") {
                context.failOver = false;
            }
            this.element.html(template(context));

            $('a.help_popup').popover({
                html : true,
                container: 'body',
                content: function() {
                    var msg = $('#'+$(this).attr('help_data')).html();
                    return msg;
                },
                template: '<div class="popover default-popover" role="tooltip"><div class="arrow"></div><div class="popover-content"></div></div>'
            });

        },        

        render_advance: function(selectedEndpointType, selectedEndpointIndex){
            var context = jQuery.extend({}, this.config[selectedEndpointType][selectedEndpointIndex].config);
            context[this.config.endpoint_type] = true;
            var model = this.element.find("#advance_form").html(template2(context));
            this.element.find("#advance_endpoint_config").find('.selectpicker').selectpicker();
            this.element.find("#advance_endpoint_config").modal('show');
        },        

        _on_endpoint_changed : function(e){
            this.config.endpoint_type = $(e.currentTarget).val();
            this.selected_ep_type = $(e.currentTarget).val();
            if(this.config.endpoint_type == "default"){
                this.config.production_endpoints = [{"url":"default"}];
                this.config.sandbox_endpoints = [{"url":"default"}];
            }else{
                this.config.production_endpoints = [{"url":"", endpoint_type:this._get_selected_ep() }];
                this.config.sandbox_endpoints = [{"url":"", endpoint_type:this._get_selected_ep() }];
            }
            delete this.config.failOver;
            delete this.config.algoCombo ;
            delete this.config.algoClassName ;
            delete this.config.sessionManagement ;
            delete this.config.sessionTimeOut ;
            delete this.config.production_failovers;
            delete this.config.sandbox_failovers;
            this.render();
        },

        _on_failover_checked:function(e){
            if(e.target.checked && this.config.endpoint_type != "load_balance"){
                $('#load_balance').prop('checked', false);
                this.config.endpoint_type = "failover";
                if(this.config.production_endpoints.length > 0) {
                    this.config.production_endpoints = [this.config.production_endpoints[0]];
                }

                if(this.config.sandbox_endpoints.length > 0) {
                    this.config.sandbox_endpoints = [this.config.sandbox_endpoints[0]];
                }

                this.config.production_failovers = [ { url:"", endpoint_type:this._get_selected_ep() }];
                this.config.sandbox_failovers = [ { url:"", endpoint_type:this._get_selected_ep() }];
            }else if(e.target.checked && this.config.endpoint_type == "load_balance"){
                this.config.failOver ="True";
                this.config.endpoint_type = "failover";
                if(this.config.production_endpoints.length > 0) {
                    this.config.production_endpoints = [this.config.production_endpoints[0]];
                }

                if(this.config.sandbox_endpoints.length > 0) {
                    this.config.sandbox_endpoints = [this.config.sandbox_endpoints[0]];
                }

                this.config.production_failovers = [ { url:"", endpoint_type:this._get_selected_ep() }];
                this.config.sandbox_failovers = [ { url:"", endpoint_type:this._get_selected_ep() }];
            }else if(!e.target.checked && this.config.endpoint_type == "load_balance"){
                this.config.failOver ="False";
            }else{
                this.config.endpoint_type = this.element.find("#endpoint_type").val();
                delete this.config.production_failovers;
                delete this.config.sandbox_failovers
            }
            this.render();
        },

        _on_load_balance_checked: function(e){
            if(e.target.checked){
                $('#failover').prop('checked', false);
                if(this.config.endpoint_type == "failover"){
                    this.config.failOver ="False";
                }

                if(this.config.production_endpoints.length > 0) {
                    this.config.production_endpoints = [this.config.production_endpoints[0]];
                }

                if(this.config.sandbox_endpoints.length > 0) {
                    this.config.sandbox_endpoints = [this.config.sandbox_endpoints[0]];
                }

                this.config.endpoint_type = "load_balance";
                this.config.algoCombo = "org.apache.synapse.endpoints.algorithms.RoundRobin";
                this.config.algoClassName = "";
                this.config.sessionManagement = "";
                this.config.sessionTimeOut = "";
            } else {
                this.config.endpoint_type = this.element.find("#endpoint_type").val();
                if(this.config.failOver === "True"){
                    this.config.endpoint_type = "failover";
                    this.config.production_endpoints = [ this.config.production_endpoints[0] ];
                    this.config.sandbox_endpoints = [ this.config.sandbox_endpoints[0] ];
                    this.config.production_failovers = [ { url:"", endpoint_type:this._get_selected_ep() }];
                    this.config.sandbox_failovers = [ { url:"", endpoint_type:this._get_selected_ep() }];
                }
                delete this.config.failOver;
                delete this.config.algoCombo ;
                delete this.config.algoClassName ;
                delete this.config.sessionManagement ;
                delete this.config.sessionTimeOut ;                
            }
            this.render();
        },

        _on_add_ep : function(e){
            var type = $(e.currentTarget).attr("data-type");
            if(this.config[type] == undefined)
                this.config[type] = [];            
            this.config[type].push({ url :"", endpoint_type:this._get_selected_ep()});
            this.render();
        },

        _on_remove_ep: function(e){
            var type = $(e.currentTarget).attr("data-type");
            if(this.config[type].length > 1){
                this.config[type].pop();
            }
            this.render();
        },

        _load_balance_property_change:function(e){            
            var property = $(e.currentTarget).attr("name");
            this.config[property] = $(e.target).val();
            if(property == "algoCombo" && this.config[property] =="other"){
                this.config.algoClassName = "";
            }
            this.render();       
        },

        _on_endpoint_change: function(e){
            var index = parseInt($(e.currentTarget).attr("data-index"));
            var ep_type = $(e.currentTarget).attr("data-type");
            if(this.config[ep_type] == undefined) this.config[ep_type] = [];
            if(this.config[ep_type][index] == undefined) this.config[index] = { url:"" , endpoint_type:this._get_selected_ep() };            
            this.config[ep_type][index].url = $(e.target).val();

            //validate for templates
            var re = /\{.*\}/; 
            if (this.config[ep_type][index].endpoint_type == "address" && (m = re.exec(this.config[ep_type][index].url)) !== null) {
                this.config[ep_type][index].template_not_supported = true;
                this.invalid = true;
            }else{
                this.config[ep_type][index].template_not_supported = false;
                this.invalid = false;
            }   

            this.validate_for_endpoint_change();
        },     

        get_endpoint_config: function(){
            if(ws=="true")
                this.config.endpoint_type = "ws";
            var config = jQuery.extend({}, this.config);
            //sanitize
            delete config.invalid;
            //clean empty endpoints
            config.production_endpoints  = this._clean_empty_endpoints(config.production_endpoints);
            config.sandbox_endpoints  = this._clean_empty_endpoints(config.sandbox_endpoints);
            config.production_failovers  = this._clean_empty_endpoints(config.production_failovers);
            config.sandbox_failovers  = this._clean_empty_endpoints(config.sandbox_failovers);

            // if other than load balanced convert prod and sand ep to object
            if(config.endpoint_type != "load_balance"){
                if(config.production_endpoints)
                    config.production_endpoints = config.production_endpoints[0];
                if(config.sandbox_endpoints)
                    config.sandbox_endpoints = config.sandbox_endpoints[0];
            }else{
                config.algoClassName = config.algoCombo ;                
            }
            return config;
        },

        _clean_empty_endpoints: function(endpoints){
            if(endpoints instanceof Array){
                var cleaned = [];
                for(var i=0; i< endpoints.length; i++){
                    if(endpoints[i].url != undefined & endpoints[i].url !=""){
                        endpoints[i].url = endpoints[i].url.trim();
                        cleaned.push(endpoints[i]);
                    }
                }
                if(cleaned.length > 0)
                    return cleaned;
                else
                    return undefined;
            }
        },

        _on_advance_endpoint: function(e){
            this.selected_ep_index = parseInt($(e.currentTarget).attr("data-index"));
            this.selected_ep_type = $(e.currentTarget).attr("data-type");
            this.render_advance(this.selected_ep_type,this.selected_ep_index);
        },

        _on_advance_endpoint_submit: function(e){
            var config = this.element.find("#advance_form").find( ":input" ).serializeArray();
            config = this._convert_to_object(config);

            if(this.config[this.selected_ep_type] == undefined) this.config[this.selected_ep_type] = [];
            if(this.config[this.selected_ep_type][this.selected_ep_index] == undefined) this.config[this.selected_ep_index] = { url:"" , config:"", endpoint_type:this._get_selected_ep()};  
            this.config[this.selected_ep_type][this.selected_ep_index].config = config;

            this.element.find("#advance_endpoint_config").modal('hide');
        },

        _convert_to_object:function(arr){
            var obj = {};
            for(var i=0; i < arr.length; i++){
                if(arr[i].value == undefined || arr[i].value == "") {
                    continue;
                }

                if(obj[arr[i].name] != undefined ){
                    if(obj[arr[i].name] instanceof Array){
                        obj[arr[i].name].push(arr[i].value);
                    }else{
                        obj[arr[i].name] = [ obj[arr[i].name] ];
                        obj[arr[i].name].push(arr[i].value);
                    }
                }else{
                    obj[arr[i].name] = arr[i].value;
                }
            }
            return obj;
        },

        validate: function(){
            this.config.invalid = {};
            var r = true;
            if((this.config.production_endpoints != undefined
                && this.config.production_endpoints[0].url != "") ||
               (this.config.sandbox_endpoints != undefined
                && this.config.sandbox_endpoints[0].url != "")
              ){
                delete this.config.invalid.endpoint;                
            }else{                
                this.config.invalid.endpoint = true;
                r = false;
            }

            this.render();            

            //if template is given for address endpoint
            if(this.invalid != undefined && this.invalid){
                return false;
            }

            return r;            
        },

        validate_for_endpoint_change: function(){
            this.config.invalid = {};
            var r = true;
            if((this.config.production_endpoints != undefined
                && this.config.production_endpoints[0].url != "") ||
                (this.config.sandbox_endpoints != undefined
                && this.config.sandbox_endpoints[0].url != "")
            ){
                delete this.config.invalid.endpoint;
            }else{
                this.config.invalid.endpoint = true;
                r = false;
            }

            //if template is given for address endpoint
            if(this.invalid != undefined && this.invalid){
                return false;
            }

            return r;
        },

        _get_selected_ep: function(){
            return this.element.find("#endpoint_type").val();
        },

        _set_selected_ep: function(val){
            this.element.find("#endpoint_type").val(val);
        },

        _get_production_endpoint_type: function(){
            if(this.config.production_endpoints[0].endpoint_type != undefined )
                return this.config.production_endpoints[0].endpoint_type;
            else
                return false;
        }
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
