var removeAPI = function(name, version, provider) {
    jagg.message({
        content:"Are you sure you want to delete the API - " + name + " - " + version ,
        type:"confirm",
        title:"Confirm Delete",
        anotherDialog:true,
        okCallback:function(){

            jagg.post("/site/blocks/item-add/ajax/remove.jag", { action:"removeAPI", name:name, version:version, provider:provider },
                      function (result) {
                          if (result.message == "timeout") {
                              if (ssoEnabled) {
                                  var current = window.location.pathname;
                                  if (current.indexOf(".jag") >= 0) {
                                      location.href = "index.jag";
                                  } else {
                                      location.href = 'site/pages/index.jag';
                                  }
                              } else {
                                  jagg.showLogin();
                              }
                          }
                  else if (!result.error) {
                      window.location.reload();
                  }else{
                       jagg.message({content:result.message,type:"error"});
                  }
              }, "json");

    }});

};

var selectUserTab = function(path){
    $.cookie("selectedTab","users");
    location.href = path;
};
$(document).ready(
         function() {
             if (($.cookie("selectedTab") != null)) {
                 $.cookie("selectedTab", null);
             }

         }
);

