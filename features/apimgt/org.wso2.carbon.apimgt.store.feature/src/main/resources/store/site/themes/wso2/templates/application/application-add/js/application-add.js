function tierChanged(element){
    var index = element.selectedIndex;
    var selectedDesc = $("#tierDescriptions").val().split(",")[index];
    $("#tierHelpStr em").text(selectedDesc);
}

$(document).ready(function () {
    $.ajaxSetup({
      contentType: "application/x-www-form-urlencoded; charset=utf-8"
    });

    var application = $("#application-name").val("");

     $.validator.addMethod('validateSpecialChars', function(value, element) {
        return /(^[a-zA-Z0-9 ._-]*$)/g.test(value);
     }, i18n.t('The name contains one or more illegal characters') + '( &nbsp;&nbsp; " &nbsp;&nbsp; \' &nbsp;&nbsp; )');

    $.validator.addMethod('checkForSpaces', function(value, element) {
        return (value.length == value.trim().length);
    }, i18n.t('Application name cannot contain leading or trailing white spaces'));

    $("#appAddForm").validate({
        submitHandler: function(form) {
            applicationAdd();
        }
    });
    var applicationAdd = function(){
        var application = $("#application-name").val();
        var tier = $("#appTier").val();
        var callbackUrl = $("#callback-url").val();
        var apiPath = $("#apiPath").val();
        var goBack = $("#goBack").val();
        var description = $("#description").val();
        var groupId = $("#groupId").val();
        var tokenType = $("#tokenType").val();
        var status='';
        var attribute;
        var attributeKey;
        var applicationAttributes = {};

        // Create json object with keys and values of custom attributes of an application
        // Ex : {"External reference id" : "###" , "Billing tier" : "####" }
        var numberOfAttributes = $("#numberOfAttributes").val();
        for (var i = 0; i < numberOfAttributes; i++) {
            attributeKey = $("#attributeKey_" + i.toString()).val();
            attribute = $("#attribute_" + i.toString()).val();
            applicationAttributes[attributeKey] = attribute;
        }
        jagg.post("/site/blocks/application/application-add/ajax/application-add.jag", {
            action:"addApplication",
            application:application,
            tier:tier,
            callbackUrl:callbackUrl,
            description:description,
            groupId:groupId,
            tokenType:tokenType,
            groupId:groupId,
            applicationAttributes:JSON.stringify(applicationAttributes)
        }, function (result) {
            if (result.error == false) {
                status=result.status;
                var date = new Date();
                date.setTime(date.getTime() + (3 * 1000));
                $.cookie('highlight','true',{ expires: date});
                $.cookie('lastAppName',application,{ expires: date});
                $.cookie('lastAppStatus',status,{ expires: date});
                if(goBack == "yes"){
                    jagg.message({content:i18n.t('Return to API detail page?'),type:'confirm',okCallback:function(){
                    window.location.href = apiViewUrl + "?" +  apiPath;
                    },cancelCallback:function(){
                        window.location = jagg.url("/site/pages/application.jag?name=" + application );
                    }});
                } else{
                    window.location =  jagg.url("/site/pages/application.jag?name=" + application );
                }

            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
    };


    $("#application-name").charCount({
			allowed: 70,
			warning: 50,
			counterText: i18n.t('Characters left: ')
		});
    $("#application-name").val('');

    /*$('#application-name').keydown(function(event) {
         if (event.which == 13) {
               applicationAdd();
            }
        });*/

    $("#appAddForm").keypress(function(e){
        $('.tagContainer .bootstrap-tagsinput input').keyup(function(e) {
            var tagName = $(this).val();
            $tag = $(this);
            
        });

        $('.tags-error').html('');

        $("#tags").on('itemAdded', function(event) {
            $('.tags-error').hide();
            $('.add-tags-error').hide();
            $('.tags-error').html('');
            $('.add-tags-error').html('');
        });
    });

    $('.tagContainer .bootstrap-tagsinput input').blur(function() {
        if($(this).val().length > 0){
            $('.tags-error').hide();
            $('.add-tags-error').show();
            $('.add-tags-error').html(i18n.t('Please press Enter to add the Group Id.'))
            $('.tags-error').html('');
        }
        else if($(this).val().length == 0){
            $('.add-tags-error').hide();
            $('.add-tags-error').html('');
        }
    });

    $('#tokenType').change(
        function () {
            var keyType = $('option:selected', this).attr('title');
            if (keyType == 'JWT') {
                $('#jwt-token-type-warning').removeClass('hidden');
            } else {
                $('#jwt-token-type-warning').addClass('hidden');
            }

        }
    );

});

