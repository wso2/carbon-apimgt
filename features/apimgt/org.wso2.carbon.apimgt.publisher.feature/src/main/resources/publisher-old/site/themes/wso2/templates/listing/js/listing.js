var removeAPI = function(name, version, provider, buttonElement) {
    $(".modal-footer").html("");
    var apiThumbnail = $(buttonElement).closest(".thumbnail");
    jagg.message({
        content: i18n.t("Are you sure you want to delete the API") + " - " + name + " - " + version ,
        type:"confirm",
        title: i18n.t("Confirm Deletion"),
        anotherDialog:true,
        okCallback:function(){
            $('#messageModal').modal({backdrop: 'static', keyboard: false });
            $(".modal-header .close").hide();
            $(".modal-footer").html("");
            $(".modal-title").html(i18n.t("Please wait"));
            $(".modal-body").addClass("loadingButton");
            $(".modal-body").css({"margin-left":25});
            $(".modal-body").html(i18n.t("Deleting API") +" : "+ name + " - " + version );

            buttonElement.hidden = true;
            apiThumbnail.hide();

            jagg.post("/site/blocks/item-add/ajax/remove.jag", { action:"removeAPI", name:name, version:version, provider:provider },
                      function (result) {

                          $(".modal-header .close").show();
                          $(".modal-body").css({"margin-left":0});
                          $(".modal-body").html("");
                          $(".modal-body").removeClass("loadingButton");
                          $("#messageModal").hide();

                          if (result.message == "timeout") {
                              if (ssoEnabled) {
                                  var current = window.location.pathname;
                                  if (current.indexOf(".jag") >= 0) {
                                      location.href = "index.jag";
                                  } else {
                                      location.href = 'site/pages/index.jag';
                                  }
                              } else {
                                  $("#messageModal").show();
                                  jagg.showLogin();
                              }
                          }
                  else if (!result.error) {
                      window.location.reload();
                  }else{
                      if (result.message == "timeout") {
                          jagg.showLogin();
                      }else {
                          $("#messageModal").show();
                          jagg.message({content: result.message, type: "error"});
                          buttonElement.hidden = false;
                          apiThumbnail.show();
                      }
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

