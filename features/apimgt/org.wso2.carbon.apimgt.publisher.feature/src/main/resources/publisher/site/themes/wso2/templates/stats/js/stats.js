var formatTimeChunk = function (t) {
    if (t < 10) {
        t = "0" + t;
    }
    return t;
};
function btnActiveToggle(button){
    $(button).siblings().removeClass('active');
    $(button).addClass('active');
}
var convertTimeString = function(date){
    var d = new Date(date);
    var formattedDate = d.getFullYear() + "-" + formatTimeChunk((d.getMonth()+1)) + "-" + formatTimeChunk(d.getDate())+" "+formatTimeChunk(d.getHours())+":"+formatTimeChunk(d.getMinutes())+":"+formatTimeChunk(d.getSeconds());
    return formattedDate;
};
var convertTimeStringPlusDay = function (date) {
    var d = new Date(date);
    var formattedDate = d.getFullYear() + "-" + formatTimeChunk((d.getMonth() + 1)) + "-" + formatTimeChunk(d.getDate() + 1);
    return formattedDate;
};
var formatTimeChunk = function (t) {
    if (t < 10) {
        t = "0" + t;
    }
    return t;
};
function convertDate(date) {
    var month = date.month() + 1;
    var day = date.date();
    var hour=date.hour();
    var minute=date.minutes();
    return date.year() + '-' + (('' + month).length < 2 ? '0' : '')
        + month + '-' + (('' + day).length < 2 ? '0' : '') + day +" "+ (('' + hour).length < 2 ? '0' : '')
        + hour +":"+(('' + minute).length < 2 ? '0' : '')+ minute;
}

function btnActiveToggle(button){
    $(button).siblings().removeClass('active');
    $(button).addClass('active');
}
function isDataPublishingEnabled(){
    jagg.post("/site/blocks/stats/ajax/stats.jag", { action: "isDataPublishingEnabled"},
        function (json) {
            if (!json.error) {
                statsEnabled = json.usage;
                return statsEnabled;
            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }, "json");
}

function convertDateToLong(date){
    var allSegments=date.split(" ");
    var dateSegments=allSegments[0].split("-");
    var timeSegments=allSegments[1].split(":");
    var newDate = new Date(dateSegments[0],(dateSegments[1]-1),dateSegments[2],timeSegments[0],timeSegments[1],timeSegments[2]);
    return newDate.getTime();
}
