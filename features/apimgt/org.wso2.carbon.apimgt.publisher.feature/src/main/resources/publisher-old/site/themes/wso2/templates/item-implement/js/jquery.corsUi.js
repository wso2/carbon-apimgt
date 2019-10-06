;(function ( $, window, document, undefined ) {

    var source = $("#cors-ui-template").html();
    var template;
    if(source != undefined && source !="" ){
        template = Handlebars.compile(source);
    }

    var pluginName = "corsUi";

    var defaults = {

    };

    // The actual plugin constructor
    function Plugin( element, options ) {
        this.element = $(element);
        this.options = $.extend( {}, defaults, options) ;
        this.config = this.options.config;
        this._name = pluginName;
        this.init();
    }

    Plugin.prototype = {

        init: function () {
            this.render();
            this.attach_events();
        },

        attach_events: function() {
            this.element
                .on("change.tagsinput", ".tagsinput", $.proxy(this._on_change_data, this))
                .on("change", ".allowCredentialsManaged", $.proxy(this._on_change_allowCredentialsManaged, this))
                .on("change", ".toggleCorsManaged", $.proxy(this._on_change_toggleCorsManaged, this))
                .on("change", ".allowAll", $.proxy(this._on_allow_all, this));

        },

        _on_change_toggleCorsManaged:function(e){
            if(e.target.checked) {
                this.config.corsConfigurationEnabled = true;
            }else{
                this.config.corsConfigurationEnabled = false;
            }
            this.render();
        },

        _on_change_allowCredentialsManaged:function(e){
            if(e.target.checked) {

                this.config[$(e.currentTarget).attr("name")] = "true";
            }else{

                this.config[$(e.currentTarget).attr("name")]= "false";
            }

        },

        _on_change_data:function(e){
            this.config[$(e.currentTarget).attr("name")] = $(e.currentTarget).val().split(",");
            if(this.config.accessControlAllowOrigins[0] === "*" )
                this.render();
        },

        _on_allow_all : function(e){
            if(e.target.checked) {
                this.config.allowAll = true;
                this.config.accessControlAllowOrigins = ["*"];
            }
            else{
                this.config.allowAll = false;
                this.config.accessControlAllowOrigins = [];
            }
            this.render();
        },

        render: function () {
            this.element.off(".tagsinput");
            var context = jQuery.extend({}, this.config);

            if(context.accessControlAllowOrigins == "*"){
                context.allowAll = true;
            }else{
                context.allowAll = false;
            }
            this.element.html(template(context));
            this.element.find(".tagsinput").tagsinput({ confirmKeys: [13, 44] });
            this.element.on("change.tagsinput", ".tagsinput", $.proxy(this._on_change_data, this));


        },

        get_cors_config: function(){
            return this.config;
        }
    }

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
