var logout = function () {
    jagg.post("/site/blocks/user/login/ajax/login.jag", {action:"logout"}, function (result) {
        if (!result.error) {
            var current = window.location.pathname;
            if (current.indexOf(".jag") >= 0) {
                location.href = "login.jag";
            } else {
                location.href = 'site/pages/login.jag';
            }
        } else {
            jagg.message({content:result.message,type:"error"});
        }
    }, "json");
};
$(document).ready(function(){
    $('#userInfoMenu').prev().click(function(){
        if($('#userInfoMenu').is(':visible')){
            $('#userInfoMenu').hide();
        }else{
            $('#userInfoMenu').show();
        }
    });
    $('html').click(function() {
        if($('#userInfoMenu').is(':visible')){
            $('#userInfoMenu').hide();
        }
    });

    $('#userMenu').click(function(event) {
        event.stopPropagation();
    });
});

var getAPIStoreURL = function (username,isSuperTenant) {
    jagg.post("/site/blocks/user/login/ajax/login.jag", {action:"getAPIStoreURL"}, function (result) {
        if (!result.error) {
            if(isSuperTenant){location.href = result.url;}
            else{
                var tenantDomain=username.split("@")[1];
                location.href = result.url+"?tenant="+tenantDomain;
            }


        } else {
            jagg.message({content:result.message,type:"error"});
        }
    }, "json");
};



