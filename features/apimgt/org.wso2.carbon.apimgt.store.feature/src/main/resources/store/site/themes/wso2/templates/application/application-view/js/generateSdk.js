
function generateAndroidSdk(app,lang) {
    $("#cssload-contain").fadeIn();
    jagg.post("/site/blocks/subscription/subscription-list/ajax/subscription-list.jag?action=generateSdk&selectedApp=&language=", {
        action:"generateSdk",
        selectedApp:app,
        language:lang,
    }, function (result) {
        $("#cssload-contain").fadeOut();
        if (result.error) {
            jagg.message({content:result.message,type:"error"});
        }
    }, "json");
}

