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
            if(result.status == 'REJECTED')    {
                $('#messageModal').html($('#confirmation-data').html());
                $('#messageModal h4.modal-title').html(i18n.t('info.subscriptionRejectTitle'));
                $('#messageModal div.modal-body').html('\n\n' + i18n.t('info.subscriptionRejected'));
                $('#messageModal a.btn-primary').html(i18n.t('info.OK'));
                $('#messageModal a.btn-primary').click(function() {
                    window.location.reload();
                });
            } else  {
                $('#messageModal').html($('#confirmation-data').html());
                $('#messageModal h4.modal-title').html(i18n.t('info.subscription'));
                if(result.status == 'ON_HOLD'){
                    $('#messageModal div.modal-body').html('\n\n' + i18n.t('info.subscriptionPending'));
                }else{
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
            }
            $('#messageModal').modal();


        } else {
            jagg.message({content:result.message,type:"error"});
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
            alert(result);
            if (result.error == false) {                                
                if(result.rating){
                    $('.rate_td').prepend("<td><div class='average-rating'>"+result.rating+"</div></td>");
                    $('.rate_td').attr('colspan',1);
                    $('.your_rating').text(result.rating+"/5");
                }else{
                    $('.average-rating').remove();
                    $('.rate_td').attr('colspan',2);
                    $('.your_rating').text("N/A");
                }
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
    }, function (api) {
		removeRating(api);
    }, jagg.api);


});

