//This is the default place holder
var multi_urls = [];
var template;

$("#add_url").click(function () {
    if ($("#eventReceiverURL").val() == "") {
        jagg.message({content: "'URL Group' cannot be empty.", type: "error"});
        return;
    }
    if ($("#eventReceiverUsername").val() == "") {
        jagg.message({content: "'Username' cannot be empty.", type: "error"});
        return;
    }
    if ($("#eventReceiverPassword").val() == "") {
        jagg.message({content: "'Password' cannot be empty.", type: "error"});
        return;
    }
    if (multi_urls.length > 0 && (multi_urls[0].username !== $("#eventReceiverUsername").val() ||
                                  (multi_urls[0].password !== $("#eventReceiverPassword").val()))){
        jagg.message({content: "'Username' and 'Password' values should be the same for all URL Groups.", type: "error"});
        return;
    }
    var url_group = '{' + $("#eventReceiverURL").val() + '}';
    var username = $("#eventReceiverUsername").val();
    var password = $("#eventReceiverPassword").val();
    var receiver = {"url_group":url_group, "username":username, "password":password};
    multi_urls.push(receiver);
    showReceiverURLs();
});

function createMultiUrlArrayAndView(urlGroup, username, password){
    multi_urls = [];
    var multi_url_set = urlGroup.split("},");
    for (var i = 0; i < multi_url_set.length; i++) {
        var url_group;
        if(i == multi_url_set.length-1){
            url_group= multi_url_set[i];
        } else {
            url_group = multi_url_set[i] + "}";
        }
        var receiver = {"url_group":url_group, "username":username, "password":password};
        multi_urls[i]= receiver ;
    }

    showReceiverURLs();
}

function showReceiverURLs() {
    var source = $("#event-receiver-url-template").html().replace(/\[\[/g, '{{').replace(/\]\]/g, '}}');
    template = Handlebars.compile(source);
    $('#event_receiver_url').html(template(multi_urls));
    $('#event_receiver_url').show();
    return true;
}

$(document).ready(function(){

    $(".ui_message").hide();

    var v = $("#configuration_form").validate({
        contentType : "application/x-www-form-urlencoded;charset=utf-8",
        dataType: "json",
	    onkeyup: false,
        submitHandler: function(form) {
            var url_groups_str = "";
            for (var i = 0; i < multi_urls.length; i++) {
                var url_groups_str = url_groups_str + multi_urls[i].url_group + "," ;
            }
            $('#event_receivers').val(url_groups_str.replace(/(^,)|(,$)/g, ""));
            if ($("#event_receivers").val() == "") {
                jagg.message({content: "Please add at least one URL Group", type: "error"});
                return false;
            }

            $('#eventReceiverUsername').val(multi_urls[0].username);
            $('#eventReceiverPassword').val(multi_urls[0].password);

            $('.ui_message').html(i18n.t('analyticsConfigSaving.inProgress'));
            $('.ui_message').fadeIn('slow');
            $(form).ajaxSubmit({
                success:function(responseText, statusText, xhr, $form){
                    if (!responseText.error) {
                        $('.ui_message').html(i18n.t('analyticsConfigSaving.success'));
                        $('.ui_message').delay(4000).fadeOut('slow');
                    } else {
                        $('.ui_message').html(i18n.t('analyticsConfigSaving.error'));
                        if (responseText.message == "timeout") {
                            if (ssoEnabled) {
                                 var currentLoc = window.location.pathname;
                                 if (currentLoc.indexOf(".jag") >= 0) {
                                     location.href = "index.jag";
                                 } else {
                                     location.href = 'site/pages/index.jag';
                                 }
                            } else {
                                 jagg.showLogin();
                            }
                        } else {
                            jagg.message({content:responseText.message,type:"error"});
                        }
                    }
                }, dataType: 'json'
            });
        }
    });

    $('.stats-enabled').click(function(){
        var id = $(this).attr('ref');
        var form = $('#'+id);
        form.toggle("blind");
        if ($(this).is(':checked')) {
            enableAnalytics('true');
        } else {
            enableAnalytics('false');
        }
        return true;
    });
});

    function expandViewIfAnalyticsEnabled(enableAnalytics){
        if (enableAnalytics == "true") {
            $('#configuration_form').show();
            $('#enableStats').prop('checked', true);
        } else {
            $('#enableStats').prop('checked', false);
        }
        return true;
    }

    function enableAnalytics(enable){
        $.post("site/blocks/analytics-task/ajax/enable.jag",
           {
               enable: enable
           },
           function(data,status){
               if (!data.error) {
                   $( "body" ).trigger( "conf_saved" );
               } else {
                   if (data.message == "timeout") {
                       if (ssoEnabled) {
                           var currentLoc = window.location.pathname;
                           if (currentLoc.indexOf(".jag") >= 0) {
                               location.href = "index.jag";
                           } else {
                               location.href = 'site/pages/index.jag';
                           }
                       } else {
                           jagg.showLogin();
                       }
                   } else {
                       jagg.message({content:data.message,type:"error"});
                   }
               }
           });
    }

function removeUrlSet(index) {
    //$("#urlSetContainer" + index).hide();
    var value = document.getElementById("urlSet" + index).innerHTML;
    for (var i = 0; i < multi_urls.length; i++) {
        if (multi_urls[i].url_group === value) {
            multi_urls.splice(i, 1);
            i--;
        }
    }
    $('#event_receiver_url').html(template(multi_urls));
    if(multi_urls.length==0){
        $('#event_receiver_url').hide();
    }
}