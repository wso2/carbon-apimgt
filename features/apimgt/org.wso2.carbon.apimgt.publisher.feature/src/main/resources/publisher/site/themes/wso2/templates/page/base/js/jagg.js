var jagg = jagg || {};

(function () {
    try {
        var locale = navigator.languages && navigator.languages[0] || // Chrome / Firefox
            navigator.language ||   // All browsers
            navigator.userLanguage; // IE <= 10

        locale = "_" + locale;
        if (locale.toLowerCase() == "_en-us" || locale.toLowerCase() == "_en") {
            locale = "";
        }
    } catch (err) {
        console.error("Error occurred while detecting browser locale");
    }

    // getAsync: false option is provided to make sure localization will load before calling other UI js.
    var option = {
        resGetPath: requestURL + '/site/conf/locales/js/i18nResources' + locale + '.json',
        getAsync: false
    };
    i18n.init(option);

    if (!window.console) {
        window.console = {
            log: function(obj){},
            info: function(obj){}
        };
    }


    jagg.post = function () {
        var args = Array.prototype.slice.call(arguments);
        args[0] = this.site.context + args[0];
        $.post.apply(this, args);
    };

    jagg.syncPost = function(url, data, callback, type) {
        url = this.site.context + url;
        return jQuery.ajax({
                               type: "POST",
                               url: url,
                               data: data,
                               async:false,
                               success: callback,
                               dataType:"json"
        });
    },

    jagg.messageDisplay = function (params) {
        $('#messageModal').html($('#confirmation-data').html());
        if (params.title == undefined) {
            $('#messageModal h3.modal-title').html('API Publisher');
        } else {
            $('#messageModal h3.modal-title').html(params.title);
        }
        $('#messageModal div.modal-body').html(params.content);
        if (params.buttons != undefined) {
            $('#messageModal a.btn-primary').hide();
            for (var i = 0; i < params.buttons.length; i++) {
                $('#messageModal div.modal-footer').append($('<a class="btn ' + params.buttons[i].cssClass + '">' + params.buttons[i].name + '</a>').click(params.buttons[i].cbk));
            }
        } else {
            $('#messageModal a.btn-primary').html(i18n.t('OK')).click(function() {
                $('#messageModal').modal('hide');
            });
        }
        $('#messageModal a.btn-other').hide();
        $('#messageModal').modal();
    };
    /*
     usage
     Show info dialog
     jagg.message({content:'foo',type:'info'});

     Show warning
     dialog jagg.message({content:'foo',type:'warning'});

     Show error dialog
     jagg.message({content:'foo',type:'error'});

     Show confirm dialog
     jagg.message({content:'foo',type:'confirm',okCallback:function(){},cancelCallback:function(){}});
     */
    jagg.message = function(params) {
        if (params.type == "custom") {
            jagg.messageDisplay(params);
            return;
        }
        if (params.type == "confirm") {
            if (params.title == undefined) {
                params.title = i18n.t("API Publisher")
            }
            jagg.messageDisplay({content:params.content,title:params.title ,buttons:[
                {name:i18n.t("Yes"),cssClass:"btn btn-primary",cbk:function() {
                    if(!params.anotherDialog){
                        $('#messageModal').modal('hide');
                    }
                    if (typeof params.okCallback == "function") {
                        params.okCallback()
                    }


                }},
                {name:i18n.t("No"),cssClass:"btn",cbk:function() {
                    $('#messageModal').modal('hide');
                    if (typeof params.cancelCallback == "function") {
                        params.cancelCallback()
                    }

                }}
                
            ]
            });
            return;
        }
        params.content = '<table><tr><td style="vertical-align:top"><img src="' + siteRoot + '/images/' + params.type + '.png" align="center" hspace="10" alt="'+i18n.t('Error Message Thumbnail')+'" /></td><td><span class="messageText">' + params.content + '</span></td></tr></table>';
        var type = "";
        if (params.title == undefined) {
            if (params.type == "info") {
                type = i18n.t("Notification")
            }
            if (params.type == "warning") {
                type = i18n.t("Warning")
            }
            if (params.type == "error") {
                type = i18n.t("Error")
            }
        }
        jagg.messageDisplay({content:params.content,title:"API Publisher - " + type,buttons:[
            {name:i18n.t("OK"),cssClass:"btn btn-primary",cbk:function() {
                $('#messageModal').modal('hide');
                $(".modal-backdrop.fade.in").remove();
                if (params.cbk && typeof params.cbk == "function")
                    params.cbk();
            }}
        ]
        });
    };

    jagg.fillProgress = function (chartId){
        if(t_on[chartId]){
            var progressBar = $('#'+chartId+' div.progress-striped div.bar')[0];
            if(progressBar == undefined){
                t_on[chartId] = 0 ;
                return;
            }
            var time = Math.floor((Math.random() * 400) + 800);
            var divider = Math.floor((Math.random() * 2) + 2);
            var currentWidth = parseInt(progressBar.style.width.split('%')[0]);
            var newWidth = currentWidth + parseInt((100 - currentWidth) / divider);
            newWidth += "%";
            $(progressBar).css('width', newWidth);
            var t = setTimeout('jagg.fillProgress("'+chartId+'")', time);
        }
    };

    jagg.showLogin = function(params){
        location.reload();
    };
    jagg.login = function (username, password, params) {
        if(username == "" || password == ""){
            $('#loginErrorBox').show();
            $('#loginErrorMsg').html(i18n.t('Username and Password fields are empty.'));
            $('#username').focus();
            return;
        }
        jagg.post("/site/blocks/user/login/ajax/login.jag", { action:"login", username:username, password:password },
                 function (result) {
                     if (result.error == false) {
                         $('#messageModal').modal('hide');
                         if(params.redirect != undefined ){
                             window.location.href = params.redirect;
                         }else if(params.callback != undefined && typeof params.callback == "function"){
                             params.callback();
                         }
                     } else {
                         $('#loginErrorBox').show();
                         $('#loginErrorMsg').html(result.message);

                     }
                 }, "json");
    };

    jagg.sessionExpired = function (){
        var sessionExpired = false;
        jagg.syncPost("/site/blocks/user/login/ajax/sessionCheck.jag", { action:"sessionCheck" },
                 function (result) {
                     if(result!=null){
                         if (result.message == "timeout") {
                             sessionExpired = true;
                         }
                     }
                 }, "json");
        return sessionExpired;
    };

    jagg.sessionAwareJS = function(params){


        if(jagg.sessionExpired()){
            if(ssoEnabled){
                window.location.reload();
            } else {
                if (params.e != undefined) {  //Canceling the href call
                    if (params.e.preventDefault) {
                        params.e.preventDefault();

                        // otherwise set the returnValue property of the original event to false (IE)
                    } else {
                        params.e.returnValue = false;
                    }
                }

                jagg.showLogin(params);
            }
        }else if(params.callback != undefined && typeof params.callback == "function"){
             params.callback();
         }
    };
    jagg.printDate = function(){

        $('.dateFull').each(function(){
            var timeStamp = parseInt($(this).html());
            $(this).html(new Date(timeStamp).toLocaleString());
        });
    };
    jagg.getDate = function(timestamp){
         return new Date(timestamp).toLocaleString();
    };

    jagg.url = function(url){
        //append the site context
        url = this.site.context + url;

        //add tenant param
        //check if tenant param already set
        if (this.site.tenant) {
            //check if url has query params
            if (/\?/.test(url)) {
                if (/\?.*tenant=/.test(url)) {
                    //do nothing
                } else {
                    url = url + "&tenant=" + this.site.tenant;
                }
            } else {
                //if url do not have query params
                url = url + "?tenant=" + this.site.tenant;
            }
        }

        return url;
    };
}());

$(document).ready(function(){
    //this can go to main js
    $('a.help_popup').popover({
        html : true,
        container: 'body',
        content: function() {
          var msg = $('#'+$(this).attr('help_data')).html();
          return msg;
        },
        template: '<div class="popover default-popover" role="tooltip"><div class="arrow"></div><div class="popover-content"></div></div>'
    });

    $('html').on('click', function(e) {
      if (typeof $(e.target).data('original-title') == 'undefined' &&
         !$(e.target).parents().is('.popover.in')) {
        $('[data-original-title]').popover('hide');
      }
    });

    $('.more-options').click(function(){
        var id = $(this).attr('ref');
        var div = $('#'+id);
        if(div.is(":visible")){
            $(this).text(i18n.t("Show More Options"));
        }
        else{
            $(this).text(i18n.t("Show fewer options"));
        }
        div.toggle('fast');
        return false; 
    });

    $('.manage-certs').click(function() {
        var id = $(this).attr('ref');
        var div = $('#'+id);
        div.toggle('fast');
        return false;
    });

});
