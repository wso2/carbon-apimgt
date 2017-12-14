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
        return !/(["\'])/g.test(value);
     }, i18n.t('The Name contains one or more illegal characters') + '( &nbsp;&nbsp; " &nbsp;&nbsp; \' &nbsp;&nbsp; )');

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
        var status='';
        jagg.post("/site/blocks/application/application-add/ajax/application-add.jag", {
            action:"addApplication",
            application:application,
            tier:tier,
            callbackUrl:callbackUrl,
            description:description,
            groupId:groupId
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

            if(/([~!@#;%^&*+=\|\\<>\"\'\/,])/.test(tagName)){
                $tag.val( $tag.val().replace(/[^a-zA-Z0-9_ -]/g, function(str) {
                    $('.tags-error').show();
                    $('.add-tags-error').hide();
                    $('.add-tags-error').html('');
                    $('.tags-error').html('Group Id contains one or more illegal characters  (~ ! @ #  ; % ^ & *' +
                        ' + = { } | &lt; &gt;, \' " \\ \/ ) .');
                    return '';
                }));
            }

            if(tagName.length > 30){
                $tag.val(tagName.substring(0, 30));
                $('.tags-error').html(i18n.t('A Group Id can only have a maximum of 30 characters.'));
            }

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

});

