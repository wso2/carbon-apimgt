function tierChanged(element){
    var index = element.selectedIndex;
    var selectedDesc = $("#tierDescriptions").val().split(",")[index];
    $("#tierHelpStr em").text(selectedDesc);
}

$(document).ready(function () {
    $.ajaxSetup({
      contentType: "application/x-www-form-urlencoded; charset=utf-8"
    });

    //var application = $("#application-name").val("");

     $.validator.addMethod('validateSpecialChars', function(value, element) {
        return /(^[a-zA-Z0-9 ._-]*$)/g.test(value);
     }, i18n.t("The name contains one or more illegal characters") + '( &nbsp;&nbsp; " &nbsp;&nbsp; \' &nbsp;&nbsp; )');

    $.validator.addMethod('checkForSpaces', function(value, element) {
        return (value.length == value.trim().length);
    }, i18n.t('Application name cannot contain leading or trailing white spaces'));

    $("#appAddForm").validate({
        submitHandler: function(form) {
            updateApplication();
            return false;
        }
    });


    var updateApplication = function(){
        var application = $("#application-name").val();
        var tier = $("#appTier").val();
        var apiPath = $("#apiPath").val();
        var goBack = $("#goBack").val();
        var description = $("#description").val();
        var status='';
        var applicationOld = $("#application-name-old").val();
        var groupIdNew = $("#groupId").val();
        var groupIdOld = $("#groupId-old").val();
        var numberOfAttributes = $("#numberOfAttributes").val();
        var applicationAttributesNew = {};
        var attributeNew;
        var attributeKeyNew;

        for (var i = 0; i < parseInt(numberOfAttributes); i++) {
            attributeKeyNew = $("#attributeKey_" + i.toString()).val();
            attributeNew = $("#attribute_" + i.toString()).val();
            applicationAttributesNew[attributeKeyNew] = attributeNew;
        }
        var tokenType = $("#tokenType").val();
        jagg.post("/site/blocks/application/application-update/ajax/application-update.jag", {
            action:"updateApplication",
            applicationNew:application,
            applicationOld:applicationOld,
            tier:tier,
            descriptionNew:description,
            groupIdOld:groupIdOld,
            groupIdNew:groupIdNew,
            applicationAttributeNew:JSON.stringify(applicationAttributesNew),
            groupIdNew:groupIdNew,
            tokenType:tokenType
        }, function (result) {
            if (result.error == false) {                
                window.location = jagg.url("/site/pages/application.jag?name="+application);
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
    };


    $("#application-name").charCount({
			allowed: 70,
			warning: 50,
			counterText: i18n.t("Characters left: ")
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

