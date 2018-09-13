function changeAppNameMode(linkObj){
    jagg.sessionAwareJS({redirect:'site/pages/applications.jag'});
    var theTr = $(linkObj).parent().parent();
    var appName = $(theTr).attr('data-value');
    $('td:first',theTr).html('<div class="row-fluid"> <input title="app_name_new" class="wrap-text app_name_new form-control"  value="'
        +'" type="text" /> </div> ');
    $('td:first',theTr).find(".app_name_new").val(theTr.attr('data-value'));
    $('td:eq(3)',theTr).html('<div class="row-fluid"> <input title="callback_new" class="wrap-text callback_new validInput form-control"  value="'
        +'" type="text" /> </div> ');
    $('td:eq(3)',theTr).find(".callback_new").val(theTr.attr('callback-value'));
    $('td:eq(4)',theTr).html('<div class="row-fluid"><input title="description-new" class="description-new form-control"  value="'
        +'" type="text" /> </div> ');
    $('td:eq(4)',theTr).find(".description-new").val(theTr.attr('description-value'));
    //Hide the Edit link
    $("td:eq(4)", theTr).children("a").hide();
    $("td:eq(5)", theTr).children("a").hide();
    //Show the Save and Cancel buttons
    $("td:eq(4)", theTr).children("div").show();
    $("td:eq(5)", theTr).children("div").show();

    $('input.app_name_new',theTr).focus();
    $('input.app_name_new',theTr).keyup(function(){
        var error = "";
        var illegalChars = /([~!#$;%^*+={}\|\\<>\"\'\/,])/;
        if($(this).val() == ""){
            error = i18n.t("This field is required.");
        }else if($(this).val().length>70){
            error = i18n.t("Name exceeds the character limit (70)");
        }else if(/(["\'])/g.test($(this).val())){
            error = i18n.t("Name contains one or more illegal characters ")+'( " \' )';
        } else if ($(this).val().search(illegalChars) != -1) {
            error = i18n.t("Name contains one or more illegal characters ");
        }
        if(error != ""){
            $(this).addClass('error');
            if(!$(this).next().hasClass('error')){
                $(this).parent().append('<label class="error">'+error+'</label>');
            }else{
                $(this).next().show().html(error);
            }
        }else{
            $(this).removeClass('error');
            $(this).next().hide();
        }
    });
    $('input.callback_new',theTr).focus();
    $('input.callback_new',theTr).keyup(function(){
        var error = "";
        var illegalChars = /([<>\"\'])/;
        if(/(["\'])/g.test($(this).val())){
            error = i18n.t("Name contains one or more illegal characters ")+'( " \' )';
        } else if ($(this).val().search(illegalChars) != -1) {
            error = i18n.t("Name contains one or more illegal characters ");
        }
        if(error != ""){
            $(this).addClass('error');
            if(!$(this).next().hasClass('error')){
                //@todo: param_string
                $(this).parent().append('<label class="error">'+error+'</label>');
            }else{
                $(this).next().show().html(error);
            }
        }else{
            $(this).removeClass('error');
            $(this).next().hide();
        }
    });
    var row = $(linkObj).parent().parent();
    $("td:eq(1)", theTr).children("select").prop('disabled',false);
    $("td:eq(1)", theTr).children("select").selectpicker('refresh');
}
function updateApplication_reset(linkObj){
    jagg.sessionAwareJS({redirect:'site/pages/applications.jag'});
    var theTr = $(linkObj).parent().parent().parent();
    var appName = $(theTr).attr('data-value');
    var tier = $(theTr).attr('tier-value');
    var callbackUrl = $(theTr).attr('callback-value');
    var description = $(theTr).attr('description-value');
    $('td:first',theTr).text(appName);
    $("td:eq(1)", theTr).children("select").val(tier);
    $("td:eq(1)", theTr).children("select").prop('disabled',true);
    $("td:eq(1)", theTr).children("select").selectpicker('refresh');
    $('td:eq(3)',theTr).text(callbackUrl);
    $('td:eq(4)',theTr).text(description);
    //Hide the Save and Cancel buttons
    $("td:eq(5)", theTr).children("div").hide();
    //Show the Edit link
    $("td:eq(5)", theTr).children("a").show();
}
function updateApplication(linkObj){
    jagg.sessionAwareJS({redirect:'site/pages/applications.jag'});
    var theTr = $(linkObj).parent().parent().parent();
    var applicationOld = $(theTr).attr('data-value');
    var applicationNew = $('input.app_name_new',theTr).val();
    var callbackUrlNew = $('input.callback_new',theTr).val();
    var descriptionNew = $('input.description-new',theTr).val();
    var tier = $("td:eq(1)", theTr).children("select").val();
    var error = "";
    var illegalChars = /([~!#$;%^*+={}\|\\<>\"\'\/,])/;
    if (applicationNew == "") {
        error =  i18n.t("This field is required.");
    } else if (applicationNew.length > 70) {
        error = i18n.t("Name exceeds the character limit (70)");
    } else if (/(["\'])/g.test(applicationNew)) {
        error = i18n.t("Name contains one or more illegal characters ")+'( " \' )';
    }else if (applicationNew.search(illegalChars)!=-1) {
        error = i18n.t("Name contains one or more illegal characters ");
    }
    if(error != ""){
        return;
    }
        jagg.post("/site/blocks/application/application-update/ajax/application-update.jag", {
            action:"updateApplication",
            applicationOld:applicationOld,
            applicationNew:applicationNew,
            tier:tier,
            callbackUrlNew:callbackUrlNew,
            descriptionNew:descriptionNew
        }, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
}

function deleteApp(linkObj) {
    jagg.sessionAwareJS({redirect:'site/pages/applications.jag'});
    var theTr = $(linkObj).parent().parent();
    var appName = $(theTr).attr('data-value');
    var apiCount = $(theTr).attr('api-count');
    $('#messageModal').html($('#confirmation-data').html());
    if(apiCount > 0){
        $('#messageModal h3.modal-title').html(i18n.t("Confirm Delete"));
        $('#messageModal div.modal-body').text('\n\n' +i18n.t("This application is subscribed to ")
            + apiCount + i18n.t(" APIs. ") +i18n.t("Confirm Delete")+'"' + appName + '"'+i18n.t("? This will dissociate all the existing subscriptions and keys associated with the application. "));
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

}

function hideMsg() {
    $('#applicationTable tr:last').css("background-color", "");
    $('#appAddMessage').hide("fast");
}
$(document).ready(function() {
    if ($.cookie('highlight') != null && $.cookie('highlight') == "true") {
        $.cookie('highlight', "false");

        $('#applicationTable tr:last').css("background-color", "#d1dce3");
        if($.cookie('lastAppStatus')=='CREATED'){
        $('#appAddPendingMessage').show();
        $('#applicationPendingShowName').text($.cookie('lastAppName'));
        var t = setTimeout("hideMsg()", 3000);
        }else{
        $('#appAddMessage').show();
        $('#applicationShowName').text($.cookie('lastAppName'));
        var t = setTimeout("hideMsg()", 3000);

        }
    }
});
