
function generateAndroidSdk(app,lang) {
    $("#cssload-contain").fadeIn();
    jagg.post("/site/blocks/subscription/subscription-list/ajax/subscription-list.jag", {
        action:"generateSdk",
        selectedApp:app,
        language:lang,
    }, function (result) {
        $("#cssload-contain").fadeOut();
        if (!result.error) {
     window.location.href = "../themes/fancy/templates/subscription/subscription-list-element/download.jag?fileName="+result.appName+".zip";
        } else {
            jagg.message({content:result.message,type:"error"});
        }
    }, "json");
}

