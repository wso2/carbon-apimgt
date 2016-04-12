function tierChanged(element){
    var index = element.selectedIndex;
    var selectedDesc = $("#tierDescriptions").val().split(",")[index];
    $("#tierHelpStr").text(selectedDesc);
}

$(document).ready(function () {
    $.ajaxSetup({
      contentType: "application/x-www-form-urlencoded; charset=utf-8"
    });

    //var application = $("#application-name").val("");

     $.validator.addMethod('validateSpecialChars', function(value, element) {
        return !/(["\'])/g.test(value);
     }, 'The Name contains one or more illegal characters' + '( &nbsp;&nbsp; " &nbsp;&nbsp; \' &nbsp;&nbsp; )');

    $("#appAddForm").validate({
        submitHandler: function(form) {
            updateApplication();
            return false;
        }
    });


    var updateApplication = function(){
        var application = $("#application-name").val();
        var tier = $("#appTier").val();
        var callbackUrl = $("#callback-url").val();
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
            callbackUrlNew:callbackUrl,
            descriptionNew:description
        }, function (result) {
            if (result.error == false) {                
                window.location = "/store/site/pages/application.jag?name="+application;
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
    };


    $("#application-name").charCount({
			allowed: 70,
			warning: 50,
			counterText: 'Characters left: '
		}); 

});

