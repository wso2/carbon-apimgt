function showEnableAnalyticsMsg() {
    $('.stat-page').html("");
    var msg=  "<div class='message message-warning'>"+
            "<h4><i class='icon fw fw-warning'></i>" + i18n.t('Not Configured') + "</h4>" +
            "<p> <a href='https://docs.wso2.com/display/AM260/Configuring+APIM+Analytics' target='_blank'" +
            "title= '"+i18n.t("WSO2 wiki documentation on APIM Analytics")+"' class='warningLink'>" +
            i18n.t('Refer our documentation to correctly configure API Manager Analytics ') + "</a></p>" +
            "</div>";
    $('.stat-page').append($(msg));
}

function showNoDataAnalyticsMsg() {
    $('.stat-page').html("");
    var msg=  "<div class='message message-info'>"+
            "<h4><i class='icon fw fw-warning'></i>" + i18n.t('Data Publishing Enabled') + "</h4>" +
            "<p> " + i18n.t('Generate some traffic to see statistics') + "</p>" +
            "</div>";
    $('.stat-page').append($(msg));
}
function getDate() {
    var date = new Date();
    return new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(),date.getSeconds());
}

var convertTimeStringUTC = function(date){
    var d = new Date(date);
    var formattedDate = d.getUTCFullYear() + "-" + formatTimeChunk((d.getUTCMonth()+1)) + "-" + formatTimeChunk(d.getUTCDate())+" "+formatTimeChunk(d.getUTCHours())+":"+formatTimeChunk(d.getUTCMinutes());
    return formattedDate;
};
