var selectedGrants = "";

var GrantTypes = function (available) {
    //order will be preserved in the response map
    this.config = {
        "authorization_code":"Code",
        "implicit":"Implicit",
        "refresh_token":"Refresh Token", 
        "password":"Password", 
        "iwa:ntlm":"IWA-NTLM", 
        "client_credentials":"Client Credentials",
        "urn:ietf:params:oauth:grant-type:saml2-bearer":"SAML2",
        "urn:ietf:params:oauth:grant-type:jwt-bearer":"JWT",
        "kerberos":"Kerberos"
    }

    this.available = {};
    for(var i=0;i < available.length;i++){
        this.available[available[i]] = this.config[available[i]];
    }
};

GrantTypes.prototype.getMap = function(selected){
    var grants = [];
    if(selected !=undefined)
        grants = selected.split(" ");
    var map = [];
    for(var grant in this.available){
        var disabled = false;
        if(grant == "authorization_code" || grant == "implicit")
            disabled = true;
        var selected = grants.indexOf(grant) > -1;
        map.push({ key: grant , name:this.available[grant], "selected" : selected, "disabled" : disabled});
    }

    return map;
};

(function ( $, window, document, undefined ) {

    var pluginName = "codeHighlight";

    // The actual plugin constructor
    function Plugin( element, options ) {
        this.element = $(element);
        this._name = pluginName;
        this.init();
    }

    Plugin.prototype = {

        init: function() {
            this.editor = CodeMirror.fromTextArea(this.element[0], {
              mode:  "shell",
              readOnly: true,
              lineWrapping: true
            });
        },

        refresh: function(){
            this.editor.refresh();
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


;(function ( $, window, document, undefined ) {

    var source = $("#keys-template").html();    
    var template;
    if(source != undefined && source !="" ){
        template = Handlebars.compile(source);
    }   

    var jwt_source = $('#jwt-modal').html();
    var jwt_template;    

    if (jwt_source != undefined && jwt_source !="" ) {
        jwt_template = Handlebars.compile(jwt_source);
    }  

    var Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(e){var t="";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t="";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/[^A-Za-z0-9\+\/\=]/g,"");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/\r\n/g,"\n");var t="";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t="";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}}
    // Create the defaults once
    var pluginName = "keyWidget",
        defaults = {
            username: "Username",
            password: "Password"           
        };

    Handlebars.registerHelper('ifCond', function(v1, v2, options) {
        if(v1 === v2) {
            return options.fn(this);
        }
        return options.inverse(this);
    });

    // The actual plugin constructor
    function Plugin( element, options ) {
        this.element = $(element);

        this.app = options.app;
        this.app.type = options.type;
        this.type = options.type;
        this.app.show_keys = ( $.cookie('OAuth_key_visibility') === 'true');
        this.grants = new GrantTypes(options.grant_types);
        this.app.grants = this.grants.getMap(this.app.grants);

        var i;
        for (i = 0; i < this.app.grants.length; ++i) {
            if(this.app.grants[i].key == "client_credentials" && this.app.grants[i].selected == true){
                this.app.ClientCredentials = true;
                break;
            }
        }

        this.options = $.extend( {}, defaults, options) ;

        this._defaults = defaults;
        this._name = pluginName;

        this.init();
    }

    Plugin.prototype = {

        init: function() {
            this.selectDefaultGrants();

            this.render();

            //register actions
            this.element.on( "click", ".regenerate", $.proxy(this.regenerateToken, this));
            this.element.on( "click", ".generatekeys", $.proxy(this.generateKeys, this));
            this.element.on( "click", ".provide_keys", $.proxy(this.provideKeys, this));
            this.element.on( "click", ".provide_keys_save", $.proxy(this.provideKeysSave, this));
            this.element.on( "click", ".provide_keys_cancel", $.proxy(this.provideKeysCancel, this));
            this.element.on( "click", ".show_keys", $.proxy(this.toggleKeyVisibility, this));
            this.element.on( "click", ".generateAgainBtn", $.proxy(this.generateAgainBtn, this));
            this.element.on( "click", ".update_grants", $.proxy(this.updateGrants, this));
            this.element.on( "change", ".callback_url", $.proxy(this.change_callback_url, this));
            this.element.on( "click", ".regenerate_consumer_secret", $.proxy(this.regenerateConsumerSecret, this));
            this.element.on( "click", ".copy-btn", $.proxy(this.copyText, this));
        },

        change_callback_url: function(e){
            this.app.callbackUrl = $(e.currentTarget).val();
            this.selectDefaultGrants();
            this.render();
        },

        copyText: function(e) {
            var text = $(e.currentTarget).attr("data-clipboard-text");
            function handler (e) {
                e.clipboardData.setData('text/plain', text);
                e.preventDefault();
                document.removeEventListener('copy', handler, true);
            }

            document.addEventListener('copy', handler, true);
            document.execCommand('copy');
        },

        toggle_regenerate_button: function(e){
            if(selectedGrants.indexOf("client_credentials") == -1){
                $(this.element.find('.regenerate')).attr("disabled", true);
            } else {
                $(this.element.find('.regenerate')).attr("disabled", false);
            }
            return false;
        },

        selectDefaultGrants: function(){
            /* If keys are not generated select grants by default */
            if(this.app.ConsumerKey == undefined || this.app.ConsumerKey == ""){
                for(var i =0 ;i < this.app.grants.length;i++){
                    if((this.app.callbackUrl == undefined || this.app.callbackUrl =="" ) &&
                        (this.app.grants[i].key == "authorization_code" || this.app.grants[i].key == "implicit") ){
                        this.app.grants[i].selected = false;
                        this.app.grants[i].disabled = true;
                    }else{
                        this.app.grants[i].selected = true;
                        delete this.app.grants[i].disabled
                    }
                }
            } else {
                for(var i =0 ;i < this.app.grants.length;i++){
                    if((this.app.callbackUrl == undefined || this.app.callbackUrl =="" ) &&
                        (this.app.grants[i].key == "authorization_code" || this.app.grants[i].key == "implicit") ){
                        this.app.grants[i].selected = false;
                        this.app.grants[i].disabled = true;
                    }else{
                        delete this.app.grants[i].disabled
                    }
                }
            }
        },

        toggleKeyVisibility: function(el, options) {
            this.app.show_keys = !this.app.show_keys;
            $.cookie('OAuth_key_visibility', this.app.show_keys );
            this.render();
            return false;
        },

        provideKeys: function(){
            this.app.provide_keys_form = true;
            this.render();
            return false;
        },

        provideKeysCancel: function(){
            this.app.provide_keys_form = false;
            this.render();
            return false;
        },

        provideKeysSave: function(){
            var client_id = this.element.find("#ConsumerKey").val();
            var client_secret = this.element.find("#ConsumerSecret").val();

            var oJsonParams = {
                "key_type" : this.type,
                "client_secret":client_secret
            };

            jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                action: "mapExistingOauthClient",
                applicationName: this.app.name,
                keytype: this.type,
                callbackUrl: this.app.callbackUrl,
                jsonParams: JSON.stringify(oJsonParams),
                client_id : client_id,
                validityTime: 3600 //set a default value.
            }, $.proxy(function (result) {
                if (!result.error) {
                    if ((typeof(result.data.key.appDetails) != 'undefined') ||  (result.data.key.appDetails != null)){
                        var appDetails = JSON.parse(result.data.key.appDetails);
                        this.app.grants = this.grants.getMap(appDetails.grant_types);
                    }
                    selectedGrants = appDetails.grant_types;
                    this.app.ConsumerKey = client_id;
                    this.app.ConsumerSecret = client_secret;
                    this.app.Key = result.data.key.accessToken;
                    this.app.callbackUrl = appDetails.redirect_uris;
                    this.app.KeyScope = result.data.key.tokenScope;
                    if (result.data.key.validityTime !== 0){
                        this.app.ValidityTime = result.data.key.validityTime;
                    }
                    this.app.keyState = result.data.key.keyState;
                    this.selectDefaultGrants();
                    this.render();
                    this.toggle_regenerate_button();
                } else {
                    jagg.message({content: i18n.t("Error occurred while saving OAuth application. Please check if you have provided valid Consumer Key & Secret."), type: "error"});
                }
            },this), "json");
            return false;
        },

        generateAgainBtn: function(){

            var elem = this.element.find(".generateAgainBtn");
            var keyType = elem.attr("data-keyType");
            var applicationName = elem.attr("data-applicationName");

            jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                action:"cleanUpApplicationRegistration",
                applicationName:applicationName,
                keyType:keyType
            }, function (result) {
                if (!result.error) {
                    location.reload();
                } else {
                    jagg.message({content:result.message,type:"error"});
                }
            }, "json");
        },

        generateKeys: function(){
            var validity_time = this.element.find(".validity_time").val();
            var selected = this.element.find(".grants:checked")
                           .map(function(){ return $( this ).val();}).get().join(",");
	        selectedGrants = selected;
            var scopes = $('#scopes option:selected')
                            .map(function(){ return $( this ).val();}).get().join(" ");

            this.element.find('.generatekeys').buttonLoader('start');
            jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                action: "generateApplicationKey",
                application: this.app.name,
                keytype: this.type,
                callbackUrl: this.app.callbackUrl,
                validityTime: validity_time,
                tokenScope: scopes,
                jsonParams:'{"grant_types":"'+selected+'"}',
            }, $.proxy(function (result) {
                this.element.find('.generatekeys').buttonLoader('stop');
                if (!result.error) {
                    if ((typeof(result.data.key.appDetails) != 'undefined') ||  (result.data.key.appDetails != null)){
                        var appDetails = JSON.parse(result.data.key.appDetails);
                        this.app.grants = this.grants.getMap(appDetails.grant_types);
                    }
                    this.app.ConsumerKey = result.data.key.consumerKey;
                    this.app.ConsumerSecret = result.data.key.consumerSecret;
                    this.app.Key = result.data.key.accessToken;
                    this.app.KeyScope = result.data.key.tokenScope;
                    if(result.data.key.validityTime !== 0){
                        this.app.ValidityTime = result.data.key.validityTime;
                    }
                    this.app.keyState = result.data.key.keyState;
                    this.render();
                    if (isHashEnabled == 'true') {
                        $('#generateModal').modal('show');
                    }
                    if (this.app.tokenType == 'JWT') {
                        var out = jwt_template(this.app);
                        $("#jwt_modal_placeholder").html(out);
                        $('#generateJWTModal').modal('show');
                    }
                    this.toggle_regenerate_button();
                } else {
                    jagg.message({content: result.message, type: "error"});
                }
            },this), "json");
            return false;
        },

        regenerateToken: function(){
            var validity_time = this.element.find(".validity_time").val();
            var scopes = "";
            if(this.element.find("select.scope_select").val() != null) {
                scopes = this.element.find("select.scope_select").val().join(" ");
            }
            var selected = this.element.find(".grants:checked")
                           .map(function(){ return $( this ).val();}).get().join(",");
            selectedGrants = selected;

            this.element.find('.regenerate').buttonLoader('start');
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
                this.element.find('.regenerate').buttonLoader('stop');
                if (!result.error) {
                    this.app.Key = result.data.key.accessToken;
                    this.app.ValidityTime = result.data.key.validityTime;
                    this.app.KeyScope = result.data.key.tokenScope.join();
                    this.app.grants = this.grants.getMap(selectedGrants.split(",").join(" "));
                    var i;
                    for (i = 0; i < this.app.grants.length; ++i) {
                        if(this.app.grants[i].key == "client_credentials" && this.app.grants[i].selected == true){
                            this.app.ClientCredentials = true;
                            break;
                        }
                    }
                    this.render();
                    if (this.app.tokenType == 'JWT') {
                        var out = jwt_template(this.app);
                        $("#jwt_modal_placeholder").html(out);
                        $('#generateJWTModal').modal('show');
                    }
                    this.element.find('input.access_token').animate({ opacity: 0.1 }, 500).animate({ opacity: 1 }, 500);
                } else {
                    jagg.message({content:result.message,type:"error"});
                }

            }, this), "json");
            return false;
        },

        regenerateConsumerSecret: function() {
            var validity_time = this.element.find(".validity_time").val();
            this.element.find('.regenerate_consumer_secret').buttonLoader('start');
            jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                action:"regenerateConsumerSecret",
                clientId:this.app.ConsumerKey
            }, $.proxy(function (result) {
                this.element.find('.regenerate_consumer_secret').buttonLoader('stop');
                if (!result.error) {
                    this.app.ConsumerSecret = result.data.key,
                    this.render();
                    if (isHashEnabled == 'true') {
                        $('#regenerateModal').modal('show');
                    }
                    if (this.app.tokenType == 'JWT') {
                        var out = jwt_template(this.app);
                        $("#jwt_modal_placeholder").html(out);
                        $('#generateJWTModal').modal('show');
                    }
                } else {
                    jagg.message({content:result.data, type:"error"});
                }

            }, this), "json");
            return false;
        },

        updateGrants: function(){
            this.element.find('.update_grants').buttonLoader('start');
            var selected = this.element.find(".grants:checked")
                           .map(function(){ return $( this ).val();}).get().join(",");
            selectedGrants = selected;
            jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                action:"updateClientApplication",
                application:this.app.name,
                keytype:this.type,
                jsonParams:'{"grant_types":"'+selected+'"}',
                callbackUrl:this.app.callbackUrl
            }, $.proxy(function (result) {
                this.element.find('.update_grants').buttonLoader('stop');
                if (!result.error) {
                    this.app.grants = this.grants.getMap(selectedGrants.split(",").join(" "));
                    this.toggle_regenerate_button();
                } else {
                    //@todo: param_string
                    jagg.message({content:result.message,type:"error"});
                }
            }, this), "json");
            return false;
        },

        render: function(){
            this.app.basickey = Base64.encode(this.app.ConsumerKey+":"+this.app.ConsumerSecret);
            this.app.username = this.options.username;
            this.app.password = this.options.password;
            this.app.provide_keys = this.options.provide_keys;
            this.app.not_jwt = this.app.tokenType !== "JWT";
            this.element.html(template(this.app));
            this.element.find(".selectpicker").selectpicker({dropupAuto:false});
            this.element.find(".curl_command").codeHighlight();
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
    source   = $("#subscription-api-name").html();
    var subscription_api_name = Handlebars.compile(source);    

    var sub_list = $('#subscription-table').datatables_extended({
        "ajax": {
            "url": jagg.getBaseUrl()+ "/site/blocks/subscription/subscription-list/ajax/subscription-list.jag?action=getSubscriptionByApplication&app="+$("#subscription-table").attr('data-app')+"&groupId="+$("#subscription-table").attr('data-grp'),
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
                  return subscription_api_name(rec);
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
        $('#messageModal').html($('#confirmation-data').html());
        $('#messageModal h3.modal-title').html(i18n.t("Confirm Delete"));
        $('#messageModal div.modal-body').html('\n\n'+i18n.t("Are you sure you want to unsubscribe from ") +'<b>"' + record.apiName + '-' + record.apiVersion + '</b>"?');
        $('#messageModal a.btn-primary').html(i18n.t("Yes"));
        $('#messageModal a.btn-other').html(i18n.t("No"));
        $('#messageModal a.btn-primary').click(function() {
	        jagg.post("/site/blocks/subscription/subscription-remove/ajax/subscription-remove.jag", {
	            action:"removeSubscription",
	            name: record.apiName,
	            version: record.apiVersion,
	            provider:record.apiProvider,
	            applicationId: $("#subscription-table").attr('data-appid')
	           }, function (result) {
	            if (!result.error) {
	            	window.location.reload(true);
	            	urlPrefix = "name=" + $("#subscription-table").attr('data-app') + "&" + urlPrefix;
                    location.href = "../../site/pages/application.jag?" + urlPrefix+"#subscription";
	            } else {
	                jagg.message({content:result.message,type:"error"});
	            }
	        }, "json"); });
        $('#messageModal a.btn-other').click(function() {
            return;
        });
        $('#messageModal').modal();
    });        
});    

$("#application-actions").each(function(){
    var source   = $("#application-actions").html();
    var application_actions = Handlebars.compile(source);

    source   = $("#application-name").html();
    var application_name = Handlebars.compile(source);    

    var grpIdList = false;

    var app_list = $('#application-table').datatables_extended({
        serverSide: true,
        processing: true,
        paging: true,
        "ajax": {
            "url": jagg.url("/site/blocks/application/application-list/ajax/application-list.jag?action=getApplicationsWithPagination"),
            "dataSrc": function ( json ) {
                if(json.applications.length > 0){
                    $('#application-table-wrap').removeClass("hide");
                    $('#application-table-nodata').addClass("hide");
                }
                else{
                    $('#application-table-nodata').removeClass("hide");
                }
                grpIdList = json.grpIdList;
                return json.applications
            }
        },
        "columns": [
            { "data": "name",
              "render": function(data, type, rec, meta){
                var context = rec ;
                context.grpIdList = grpIdList;
                if(rec.groupId !="" && rec.groupId != undefined && !context.grpIdList)
                    context.shared = true;
                else
                    context.shared = false;
                var value = application_name(context);
                if(rec.isBlacklisted == 'true' || rec.isBlacklisted == true){
                    value = value.replace((">"+rec.name+"<"),("><font color='red'>"+rec.name+ i18n.t(" (Blacklisted)") + "<"));

                }

                  value = value.replace("> "+rec.owner+"/","> <font color=\"#00008b\">"+rec.owner+"/</font>");
                return  value;
              }
            },
            { "data": "tier",
              "render": function(tier, type, rec, meta){
                tier = Handlebars.Utils.escapeExpression(tier)
                return new Handlebars.SafeString(tier);  
              }
            },
            { "data": "status",
              "render": function(status, type, rec, meta){
                var result;        
                if(status=='APPROVED'){
                    result='ACTIVE';
                } else if(status=='REJECTED') {
                    result='REJECTED';
                } else{
                    result='INACTIVE <p><i>Waiting for approval</i></p>';
                }
                return new Handlebars.SafeString(result);  
              }
            },
            { "data": "apiCount" },
            { "data": "name",
              "render": function ( data, type, rec, meta ) {
                  rec.isOwner = true;
                  if (loggedInUser.toLowerCase() !== rec.owner.toLowerCase()) {
                      rec.isOwner = false;
                  }

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
    	var appName = $(this).attr("data-id");
    	var apiCount = $(this).attr("data-count");
    	$('#messageModal').html($('#confirmation-data').html());
        if(apiCount > 0){
            $('#messageModal h3.modal-title').html(i18n.t("Confirm Delete"));
            $('#messageModal div.modal-body').text('\n\n' +i18n.t("This application is subscribed to ")
                + apiCount + i18n.t(" APIs. ") +i18n.t("Are you sure you want to remove the application ")+'"' + appName + '"'+i18n.t("? This will dissociate all the existing subscriptions and keys associated with the application. "));
        } else {
            $('#messageModal h3.modal-title').html(i18n.t("Confirm Delete"));
            $('#messageModal div.modal-body').text('\n\n'+i18n.t("Are you sure you want to remove the application ")+'"' + appName + '" ?');
        }
        $('#messageModal a.btn-primary').html(i18n.t("Yes"));
        $('#messageModal a.btn-other').html(i18n.t("No"));
        $('#messageModal a.btn-primary').click(function() {
            jagg.post("/site/blocks/application/application-remove/ajax/application-remove.jag", {
                action:"removeApplication",
                application:appName
            }, function (result) {
                if (!result.error) {
                	window.location.reload(true);
                } else {
                    jagg.message({content:result.message,type:"error"});
                }
            }, "json");
        });
        $('#messageModal a.btn-other').click(function() {
            window.location.reload(true);
        });
        $('#messageModal').modal();
        
        
    });    
});

$(document).on('shown.bs.tab', 'a[data-toggle="tab"]', function (e) {
    $(".curl_command").each(function(){ $(this).data("plugin_codeHighlight").editor.refresh()});
});

});
