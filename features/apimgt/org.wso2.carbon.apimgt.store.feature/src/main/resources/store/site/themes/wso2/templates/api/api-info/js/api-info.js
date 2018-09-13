function triggerSubscribe() {
	$.ajaxSetup({
        contentType: "application/x-www-form-urlencoded; charset=utf-8"
    });
    jagg.sessionAwareJS({redirect:'/site/pages/index.jag'});
    if (!jagg.loggedIn) {
        return;
    }
    var applicationId = $("#application-list").val();
    var applicationName = $("#application-list option:selected").text();
    if (applicationId == "-" || applicationId == "createNewApp") {
        jagg.message({content:i18n.t('Please select an application before subscribing'),type:"info"});
        return;
    }
    var api = jagg.api;
    var tier = $("#tiers-list").val();
    var subscribeButtonIconHtml = '<span class="icon fw-stack"><i class="fw fw-subscribe fw-stack-1x"></i><i class="fw fw-circle-outline fw-stack-2x"></i></span>';
    $("#subscribe-button").html(
        subscribeButtonIconHtml + i18n.t('Subscribing...') 
        + '<span class="spinner"><i class="fw fw-loader5" title="button-loader"></i></span>'
    ).attr('disabled', 'disabled');

    jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
        action:"addSubscription",
        applicationId:applicationId,
        name:api.name,
        version:api.version,
        provider:api.provider,
        tier:tier,
        tenant: jagg.site.tenant
    }, function (result) {
        $("#subscribe-button").html(subscribeButtonIconHtml + i18n.t('Subscribe'));
        $("#subscribe-button").removeAttr('disabled');
        if (result.error == false) {
            if(result.status.subscriptionStatus == 'REJECTED')    {
                $('#messageModal').html($('#confirmation-data').html());
                $('#messageModal h3.modal-title').html(i18n.t('Subscription Rejected'));
                $('#messageModal div.modal-body').html('\n\n' + i18n.t('Your subscription has been rejected, since it does not satisfy the authentication requirements. Please contact the API publisher for more information.'));
                $('#messageModal a.btn-primary').html(i18n.t('OK'));
                $('#messageModal a.btn-primary').click(function() {
                    window.location.reload();
                });
            } else {
                var jsonPayload = result.status.workflowResponse.jsonPayload;
                if(jsonPayload != null && jsonPayload != ""){
                   var jsonObj = JSON.parse(jsonPayload);
                   var additionalParameters = jsonObj.additionalParameters; 
                       //add another condition to prevent unnecessary redirection
		   if (jsonObj.redirectUrl != null) {
		      if(jsonObj.redirectConfirmationMsg == null){
                   if(additionalParameters != null && Object.keys(additionalParameters).length > 0) {
                              var params = "";
                              for (var key in additionalParameters) {
                                if(params != ""){
                                  params = params.concat("&");
                                }
                                if (additionalParameters.hasOwnProperty(key)) {
                                  params = params.concat((key.concat("=")).concat(additionalParameters[key]));
                                }
                              }
                               location.href=jsonObj.redirectUrl + "?" + params;
                              }else{
                                location.href=jsonObj.redirectUrl;
                              }
                    }else{
                      $('#messageModal').html($('#confirmation-data').html());
                      $('#messageModal h3.modal-title').html("Redirection");
                      $('#messageModal div.modal-body').html(jsonObj.redirectConfirmationMsg);
                      $('#messageModal a.btn-primary').html(i18n.t('OK'));
                      $('#messageModal a.btn-other').html(i18n.t('Cancel Subscription'));
                      $('#messageModal a.btn-primary').click(function () {
                         if(additionalParameters != null && Object.keys(additionalParameters).length > 0) {
                             var params = "";
                             for (var key in additionalParameters) {
                               if(params != ""){
                                  params = params.concat("&");
                                }
                               if (additionalParameters.hasOwnProperty(key)) {
                                  params = params.concat((key.concat("=")).concat(additionalParameters[key]));
                               }
                              }
                             location.href=jsonObj.redirectUrl + "?" + params;
                            }
                         });
                       $('#messageModal a.btn-other').click(function () {
                         jagg.post("/site/blocks/subscription/subscription-remove/ajax/subscription-remove.jag", {
                              action:"removeSubscription",
                              name:api.name,
                              version:api.version,
                              provider:api.provider,
                              applicationId:applicationId
                         }, function (result) {
                            if (!result.error) {
                              $('#messageModal').modal("hide");
                                window.location.reload();
                            } else {
                              jagg.message({content:result.message,type:"error"});
                            }
                          }, "json");;
                       });
                         $('#messageModal').modal(); 
                     }
                  }
               }else {
                 $('#messageModal').html($('#confirmation-data').html());
                 $('#messageModal h3.modal-title').html(i18n.t('Subscription Successful'));
                 if (result.status.subscriptionStatus == 'ON_HOLD') {
                    $('#application-list :selected').remove();
                    $('#messageModal h3.modal-title').html(i18n.t('Subscription Awaiting Approval'));
                    $('#messageModal div.modal-body').html('\n\n' + i18n.t('Your subscription request has been submitted and is now awaiting approval.'));
                 } else {
                    $('#application-list :selected').remove();
                    $('#messageModal div.modal-body').html('\n\n' + i18n.t('You have successfully subscribed to the API.'));
                }
                $('#messageModal a.btn-primary').html(i18n.t('View Subscriptions'));
                $('#messageModal a.btn-other').html(i18n.t('Stay on this page'));
                $('#messageModal a.btn-other').click(function() {
                    window.location.reload();
                });
                $('#messageModal a.btn-primary').click(function() {
                    urlPrefix = "name=" + applicationName + "&" + urlPrefix;
                    location.href = "../site/pages/application.jag?" + urlPrefix+"#subscription";
                 });
                   $('#messageModal').modal();
                }
              }
        
       } else {
          jagg.message({content:result.message,type:"error"});
        //$('#messageModal').html($('#confirmation-data').html());
        /*$('#messageModal h3.modal-title').html('API Provider');
          $('#messageModal div.modal-body').html('\n\nSuccessfully subscribed to the API.\n Do you want to go to the subscription page?');
          $('#messageModal a.btn-primary').html('Yes');
          $('#messageModal a.btn-other').html('No');
         */
         /*$('#messageModal a.btn-other').click(function(){
             v.resetForm();
         });*/
         /*
           $('#messageModal a.btn-primary').click(function() {
           var current = window.location.pathname;
           if (current.indexOf(".jag") >= 0) {
              location.href = "index.jag";
           } else {
              location.href = 'site/pages/index.jag';
           }
          });*/
         //                        $('#messageModal').modal();
        }
    }, "json");

    runSuccessMessageHint(api.name);
}

var enjoyhint_instance_onMessage = null;

var intervalId;

function runSuccessMessageHint(apiName) {
    intervalId = setInterval(function () {
        runEnjoyHint_subscription_message_Script(apiName)
    }, 1000);
}

function runEnjoyHint_subscription_message_Script(apiName) {
    if (isEnjoyHintEnabled()) {
        stopSuccessMessageHint();
        var storeStep = "subscribed";
        localStorage.setItem("storeStep", storeStep);
        localStorage.setItem("apiName", apiName);
        runEnjoyHintScript(enjoyhint_instance_onMessage, goto_mysubscription_message_script_data);
    }
}

function stopSuccessMessageHint() {
    clearInterval(intervalId);
}

$(document).ready(function () {

$('input.rate_save').on('change', function () {
    var api = jagg.api;
    jagg.post("/site/blocks/api/api-info/ajax/api-info.jag", {
        action:"addRating",
        name:api.name,
        version:api.version,
        provider:api.provider,
        rating:$(this).val()
    }, function (result) {
        if (result.error == false) {
            if($('.average-rating').length > 0){
            $('.average-rating').text(parseFloat(result.rating).toFixed(1));
            $('.average-rating').show();
            }else{
                $('.rate_td').before("<td><div class='average-rating'>"+result.rating+"</div></td>");
                $('.rate_td').attr("colspan",1);
            }
            $('.your_rating').text(parseInt(result.rating)+"/5");
        } else {
            jagg.message({content:result.message,type:"error"});
        }
    }, "json");  
});

$('.remove_rating').on("click",function(){
    $('input.rate_save').val(0);
    $('input.rate_save').rating('rate', 0);
    var api = jagg.api;
    jagg.post("/site/blocks/api/api-info/ajax/api-info.jag", {
        action:"removeRating",
        name:api.name,
        version:api.version,
        provider:api.provider
    }, function (result) {
        if (!result.error) {
            $('.average-rating').hide();
            $('.your_rating').text("N/A");
        } else {
            jagg.message({content:result.message,type:"error"});
        }
    }, "json");    
});

$('.rating-tooltip-manual').rating({
  extendSymbol: function () {
    var title;
    $(this).tooltip({
      container: 'body',
      placement: 'bottom',
      trigger: 'manual',
      title: function () {
        return title;
      }
    });
    $(this).on('rating.rateenter', function (e, rate) {
      title = rate;
      $(this).tooltip('show');
    })
    .on('rating.rateleave', function () {
      $(this).tooltip('hide');
    });
  }
});


    $('#application-list').change(
        function () {
            var keyType = $('option:selected', this).attr('keyType');
            if (keyType == 'JWT') {
                jagg.message({
                    content: i18n.t('The tokens already generated for this application will not work for the new subscription. Please regenerate tokens after subscribing to the application'),
                    type: "info"
                });
            }
            if ($(this).val() == "createNewApp") {
                //$.cookie('apiPath','foo');
                window.location.href = '../site/pages/application-add.jag?goBack=yes&' + urlPrefix;
            }
        }
    );
});
