;(function( $ ) {
    $.fn.zclip = function() {
        if(typeof ZeroClipboard == 'function'){
            var client = new ZeroClipboard( this );
            client.on( "ready", function( readyEvent ) {
              client.on( "aftercopy", function( event ) {
                var target = $(event.target);
                target.attr("title","Copied!")
                target.tooltip('enable');
                target.tooltip("show");
                target.tooltip('disable');
              });
            });
        }else{
            console.warn('Warning : Dependency missing - ZeroClipboard Library');
        }
        return this;
    };
}( jQuery ));


;(function ( $, window, document, undefined ) {

    var source = $("#keys-template").html();    
    var template;
    if(source != undefined && source !="" ){
        template = Handlebars.compile(source);
    }        

    var Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(e){var t="";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t="";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/[^A-Za-z0-9\+\/\=]/g,"");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/\r\n/g,"\n");var t="";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t="";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}}
    // Create the defaults once
    var pluginName = "keyWidget",
        defaults = {
            propertyName: "value"            
        };

    // The actual plugin constructor
    function Plugin( element, options ) {
        this.element = $(element);

        this.app = options.app;
        this.type = options.type;

        this.options = $.extend( {}, defaults, options) ;

        this._defaults = defaults;
        this._name = pluginName;

        this.init();
    }

    Plugin.prototype = {

        init: function() {
            this.render();

            //register actions
            this.element.on( "click", ".regenerate", $.proxy(this.regenerateToken, this));
            this.element.on( "click", ".generatekeys", $.proxy(this.generateKeys, this));
        },

        yourOtherFunction: function(el, options) {
            // some logic
        },

        generateKeys: function(){
            var validity_time = this.element.find(".validity_time").val();
            jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                action: "generateApplicationKey",
                application: this.app.name,
                keytype: this.type,
                callbackUrl: this.app.callbackUrl,
                validityTime: validity_time,
                tokenScope:"",
            }, $.proxy(function (result) {
                if (!result.error) {
                    this.app.ConsumerKey = result.data.key.consumerKey,
                    this.app.ConsumerSecret = result.data.key.consumerSecret,
                    this.app.Key = result.data.key.accessToken,
                    this.app.KeyScope = result.data.key.tokenScope,
                    this.app.ValidityTime = result.data.key.validityTime
                    this.render();
                } else {
                    jagg.message({content: result.message, type: "error"});
                }
            },this), "json");
            return false;
        },

        regenerateToken: function(){            
            var validity_time = this.element.find(".validity_time").val();
            var scopes = this.element.find(".scope_select").val().join(" ");
            jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                action:"refreshToken",
                application:this.app.name,
                keytype:this.type,
                oldAccessToken:this.app.Key,
                clientId:this.app.ConsumerKey,
                clientSecret: this.app.ConsumerSecret,
                validityTime:validity_time,
                tokenScope:scopes
            }, $.proxy(function (result) {
                if (!result.error) {
                    console.log(result);
                    this.app.Key = result.data.key.accessToken;
                    this.app.ValidityTime = result.data.key.validityTime;
                    this.app.KeyScope = result.data.key.tokenScope.join();                    
                    this.render();                    
                } else {
                    jagg.message({content:result.message,type:"error"});
                }

            }, this), "json");
            return false;
        },

        render: function(){                   
            this.app.basickey = Base64.encode(this.app.ConsumerKey+":"+this.app.ConsumerSecret);
            this.element.html(template(this.app));
            this.element.find(".copy-button").zclip();
            this.element.find(".selectpicker").selectpicker();
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




$(document).ready(function() {


$("#subscription-actions").each(function(){
    var source   = $("#subscription-actions").html();
    var subscription_actions = Handlebars.compile(source);

    var sub_list = $('#subscription-table').datatables_extended({
        "ajax": {
            "url": "/store/site/blocks/subscription/subscription-list/ajax/subscription-list.jag?action=getSubscriptionByApplication&app="+$("#subscription-table").attr('data-app'),
            "dataSrc": function ( json ) {
            	if(json.apis.length > 0){
            		$('#subscription-table-wrap').removeClass("hide");            		
            	}
            	else{
            		$('#subscription-table-nodata').removeClass("hide"); 
            	}
            	return json.apis
            }
        },
        "columns": [
            { "data": "apiName", 
			  "render": function ( data, type, rec, meta ) {
			  	  console.log(rec);
			      return '<a href="'+data+'">'+rec.apiName +' - '+ rec.apiVersion +'</a>';
			  }
			},
            { "data": "subscribedTier" },
            { "data": "subStatus" },
            { "data": "apiName",
              "render": function ( data, type, rec, meta ) {
                  return subscription_actions(rec);
              }
            }
    	],  
    });

    $('#subscription-table').on( 'click', 'a.deleteApp', function () {
        var row = sub_list.row( $(this).parents('tr') );
        var record = row.data();
        jagg.post("/site/blocks/subscription/subscription-remove/ajax/subscription-remove.jag", {
            action:"removeSubscription",
            name: record.apiName,
            version: record.apiVersion,
            provider:record.apiProvider,
            applicationId: $("#subscription-table").attr('data-appid')
           }, function (result) {
            if (!result.error) {
                row.remove().draw();
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json"); 
    });        
});    

$("#application-actions").each(function(){
    var source   = $("#application-actions").html();
    var application_actions = Handlebars.compile(source);


    var app_list = $('#application-table').datatables_extended({
        "ajax": {
            "url": "/store/site/blocks/application/application-list/ajax/application-list.jag?action=getApplications",
            "dataSrc": function ( json ) {
                if(json.applications.length > 0){
                    $('#application-table-wrap').removeClass("hide");                  
                }
                else{
                    $('#application-table-nodata').removeClass("hide"); 
                }
                return json.applications
            }
        },
        "columns": [
            { "data": "name",
              "render": function(data, type, rec, meta){
                if(rec.groupId !="" && rec.groupId != undefined)
                    return data+ " (Shared)";
                else
                    return data;
              }
            },
            { "data": "tier" },
            { "data": "status",
              "render": function(status, type, rec, meta){
                var result;        
                if(status=='APPROVED'){
                    result='ACTIVE';
                } else if(status=='REJECTED') {
                    result='REJECTED';
                } else{
                    result='INACTIVE';
                }
                return new Handlebars.SafeString(result);  
              }
            },
            { "data": "apiCount" },
            { "data": "name",
              "render": function ( data, type, rec, meta ) {
                  rec.isActive = false;
                  if(rec.status=='APPROVED'){
                      rec.isActive = true;
                  }
                  return application_actions(rec);
              }
            },
        ],  
    });

    $('#application-table').on( 'click', 'a.deleteApp', function () {
        app_list
        .row( $(this).parents('tr') )
        .remove()
        .draw();
    });    
});

});




