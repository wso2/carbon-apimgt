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
        return !/(["\'])/g.test(value);
     }, i18n.t("The Name contains one or more illegal characters") + '( &nbsp;&nbsp; " &nbsp;&nbsp; \' &nbsp;&nbsp; )');

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
        jagg.post("/site/blocks/application/application-update/ajax/application-update.jag", {
            action:"updateApplication",
            applicationNew:application,
            applicationOld:applicationOld,
            tier:tier,
            descriptionNew:description
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

});

