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
        jagg.message({content:i18n.t('info.appSelect'),type:"info"});
        return;
    }
    var api = jagg.api;
    var tier = $("#tiers-list").val();
    $(this).html(i18n.t('info.wait')).attr('disabled', 'disabled');

    jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
        action:"addSubscription",
        applicationId:applicationId,
        name:api.name,
        version:api.version,
        provider:api.provider,
        tier:tier,
        tenant: jagg.site.tenant
    }, function (result) {
        $("#subscribe-button").html('Subscribe');
        $("#subscribe-button").removeAttr('disabled');
        if (result.error == false) {
            if(result.status.subscriptionStatus == 'REJECTED')    {
                $('#messageModal').html($('#confirmation-data').html());
                $('#messageModal h3.modal-title').html(i18n.t('info.subscriptionRejectTitle'));
                $('#messageModal div.modal-body').html('\n\n' + i18n.t('info.subscriptionRejected'));
                $('#messageModal a.btn-primary').html(i18n.t('info.OK'));
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
                      $('#messageModal a.btn-primary').html(i18n.t('info.OK'));
                      $('#messageModal a.btn-other').html(i18n.t('info.cancelSubs'));
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
                 $('#messageModal h3.modal-title').html(i18n.t('info.subscription'));
                 if (result.status.subscriptionStatus == 'ON_HOLD') {
                    $('#application-list :selected').remove();
                    $('#messageModal h3.modal-title').html(i18n.t('info.subscriptionPendingTitle'));
                    $('#messageModal div.modal-body').html('\n\n' + i18n.t('info.subscriptionPending'));
                 } else {
                    $('#application-list :selected').remove();
                    $('#messageModal div.modal-body').html('\n\n' + i18n.t('info.subscriptionSuccess'));
                }
                $('#messageModal a.btn-primary').html(i18n.t('info.gotoSubsPage'));
                $('#messageModal a.btn-other').html(i18n.t('info.stayPage'));
                $('#messageModal a.btn-other').click(function() {
                    window.location.reload();
                });
                $('#messageModal a.btn-primary').click(function() {
                    urlPrefix = "selectedApp=" + applicationName + "&" + urlPrefix;
                    location.href = "../site/pages/subscriptions.jag?" + urlPrefix;
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
            $('.average-rating').text(result.rating);
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


    $("select[name='tiers-list']").change(function() {
        var selectedIndex = document.getElementById('tiers-list').selectedIndex;
        var selectedTier = $(this).val();
        var api = jagg.api;
        var tiers = api.tiers;
        for (var i = 0; i < tiers.length; i++) {
            var tierDesc = tiers[i].tierDescription;
            var tierAttrs=tiers[i].tierAttributes;
            if (selectedTier == tiers[i].tierName) {
                if (tierDesc != null) {
                    $("#tierDesc").text(tierDesc);
                }if(tierAttrs!=null){
                    var tierAttr=tierAttrs.split(',')
                    for(var k=0;k<tierAttr.length;k++){
                        var tierAttrName=tierAttr[k].split("::")[0];
                        var tierAttrValue=tierAttr[k].split("::")[1];
                        if(tierAttrName!='' && tierAttrValue!=''){
                            $('#tierDesc').append("<br\/>"+tierAttrName +" :    "+tierAttrValue);
                        }
                    }

                }


            }
        }

    });

    $('#application-list').change(
            function(){
                if($(this).val() == "createNewApp"){
                    //$.cookie('apiPath','foo');
                    window.location.href = '../site/pages/applications.jag?goBack=yes&'+urlPrefix;
                }
            }
    );

    jagg.initStars($(".api-info"), function (rating, api) {
        jagg.post("/site/blocks/api/api-info/ajax/api-info.jag", {
            action:"addRating",
            name:api.name,
            version:api.version,
            provider:api.provider,
            rating:rating
        }, function (result) {
            if (result.error == false) {
                addRating(result.rating,rating);
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
    }, function (api) {
		removeRating(api);
    }, jagg.api);


});

var addRating = function (newRating, userRating) {
    var tableRow = $('div.api-info').find('table.table > tbody > tr:nth-child(1)');
    var firstHeader = tableRow.find('th');
    var lastCell;
    if (user) {
        var averageRating = tableRow.find('div.average-rating');
        if (averageRating.length > 0) {
            averageRating.html(newRating);
        } else {
            $("<td></td>").append('<div class="average-rating">' + newRating + '</div>').insertAfter(firstHeader);
        }
        lastCell = tableRow.find('td:last')
        lastCell.attr('colspan', 1);
        if (user) {
            $.getScript(context + '/site/themes/' + theme + '/utils/ratings/star-generator.js', function () {
                lastCell.find('div.star-ratings').html(getDynamicStars(userRating));
                jagg.initStars($(".api-info"), function (rating, api) {
                    jagg.post("/site/blocks/api/api-info/ajax/api-info.jag", {
                        action:"addRating",
                        name:api.name,
                        version:api.version,
                        provider:api.provider,
                        rating:rating
                    }, function (result) {
                        if (result.error == false) {
                            addRating(result.rating, rating);
                        } else {
                            jagg.message({content:result.message, type:"error"});
                        }
                    }, "json");
                }, function (api) {
					removeRating(api);
                }, jagg.api);

            });
        }
    }
};


var removeRating = function(api) {
    jagg.post("/site/blocks/api/api-info/ajax/api-info.jag", {
        action:"removeRating",
        name:api.name,
        version:api.version,
        provider:api.provider
    }, function (result) {
        if (!result.error) {
            removeStars(result.rating);
        } else {
            jagg.message({content:result.message,type:"error"});
        }
    }, "json");

};
var removeStars = function (newRating) {
    var tableRow = $('div.api-info').find('table.table > tbody > tr:nth-child(1)');
    var firstHeader = tableRow.find('th');
    var lastCell = tableRow.find('td:last');
    if (user) {
        var averageRating = tableRow.find('div.average-rating');
        if (averageRating.length > 0) {
            if (newRating > 0) {
                averageRating.html(newRating);
            } else {
                lastCell.attr('colspan', 2);
                averageRating.parent().remove();
            }
        }

        if (user) {
            $.getScript(context + '/site/themes/' + theme + '/utils/ratings/star-generator.js', function () {
                lastCell.find('div.star-ratings').html(getDynamicStars(0));

                jagg.initStars($(".api-info"), function (rating, api) {
                    jagg.post("/site/blocks/api/api-info/ajax/api-info.jag", {
                        action:"addRating",
                        name:api.name,
                        version:api.version,
                        provider:api.provider,
                        rating:rating
                    }, function (result) {
                        if (result.error == false) {
                            addRating(result.rating, rating);
                        } else {
                            jagg.message({content:result.message, type:"error"});
                        }
                    }, "json");
                }, function (api) {
	   				removeRating(api);
                }, jagg.api);

            });
        }
    }
};
