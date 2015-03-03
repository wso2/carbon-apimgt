//This is the default place holder
var multi_urls = [];
var template;

$("#add_url").click(function () {
    if ($("#eventReceiverURL").val() == "") {
        jagg.message({content: "URL cannot be empty.", type: "error"});
        return;
    }
    var url = $("#eventReceiverURL").val();
    multi_urls.push('{' + url + '}');
    showReceiverURLs();
});

function createMultiUrlArrayAndView(urlGroup){
    multi_urls = urlGroup.split("},");
    for (var i = 0; (i+1) < multi_urls.length; i++) {
        multi_urls[i]= multi_urls[i] + "}" ;
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

    var v = $("#configuration_form").validate({
        contentType : "application/x-www-form-urlencoded;charset=utf-8",
        dataType: "json",
	    onkeyup: false,
        submitHandler: function(form) {
            var url_groups_str = "";
            for (var i = 0; i < multi_urls.length; i++) {
                var url_groups_str = url_groups_str + multi_urls[i] + "," ;
            }
            $('#event_receiver_url_groups').val(url_groups_str.replace(/(^,)|(,$)/g, ""));

            if ($("#event_receiver_url_groups").val() == "") {
                jagg.message({content: "Event Receiver URL cannot be empty.", type: "error"});
                return;
            }

            $(form).ajaxSubmit({
                success:function(responseText, statusText, xhr, $form){
                    $('.ui_message').innerHTML = "Configurations Saved!!!";
                    $('.ui_message').fadeIn('slow');
                    $('.ui_message').delay(4000).fadeOut('slow');
                    if (!responseText.error) {
                        $( "body" ).trigger( "conf_saved" );
                    } else {
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
        } else {
            disableAnalytics();
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

    function disableAnalytics(){
        $.post("site/blocks/analytics-task/ajax/enable.jag",
           {
               enable: "false"
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
        if (multi_urls[i] === value) {
            multi_urls.splice(i, 1);
            i--;
        }
    }
    $('#event_receiver_url').html(template(multi_urls));
    if(multi_urls.length==0){
        $('#event_receiver_url').hide();
    }
}