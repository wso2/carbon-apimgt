var updateSubscription = function (apiName, version, provider, appId, newstatus, n, link) {
    //var ahrefId = $('#' + apiName + provider + appId);
    //n is a integer value. But, when this parameter getting passed from the jaggery, it getting passed as 1.0, 2.0 etc instead of 1, 2.
    // Math.round function is called on 'n' to avoid the adding decimals into n.
    n = Math.round(n);
    var ahrefId = $(link);
    var status = ahrefId.text();
    var blockType = $('#blockType' + n + '  option:selected').val();
    var newStatus;
    if (status.trim().toUpperCase() == 'Unblock'.toUpperCase()) {
        newStatus = 'UNBLOCKED';
    } else if(blockType == 'blockProduction') {
    	newStatus = 'PROD_ONLY_BLOCKED';
    } else {
        newStatus = 'BLOCKED';
    }
    jagg.post("/site/blocks/users-api/ajax/subscriptions.jag", {
        action:"updateSubscription",
        apiName:apiName,
        version:version,
        provider:provider,
        appId:appId,
        newStatus:newStatus
    }, function (result) {
        if (!result.error) {
            if (newStatus == 'UNBLOCKED') {
            	$('#blockType' + n).prop('disabled', false);
                ahrefId.html('<span class="icon fw-stack"><i class="fw fw-block fw-stack-1x" title="' + i18n.t("Block") + '"></i></span> <span class="hidden-xs">' + i18n.t("Block") + '</span>');
            } else {
            	$('#blockType' + n).prop('disabled', true);
            	ahrefId.html('<span class="icon fw-stack"><i class="fw fw-check fw-stack-1x" title="' + i18n.t("Unblock") + '"></i></span> <span class="hidden-xs">' + i18n.t("Unblock") + '</span>');
            }

        } else {
            if (result.message == "AuthenticateError") {
                jagg.showLogin();
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }


    }, "json");


}

var getRadioValue = function (radioButton) {
    if (radioButton.length > 0) {
        return radioButton.val();
    }
    else {
        return 0;
    }
};

$(function(){
    /***********************************************************
     *  data-tables config
     ***********************************************************/
	$('#manage-subscriptions').datatables_extended({
	     "fnDrawCallback": function(){
	       if(this.fnSettings().fnRecordsDisplay()<=$("#manage-subscriptions_length option:selected" ).val()
	     || $("#manage-subscriptions_length option:selected" ).val()==-1)
	       $('#manage-subscriptions_paginate').hide();
	       else $('#manage-subscriptions_paginate').show();
	     } ,
         "aoColumns": [
         { "bSortable": true },
         null,
         { "bSortable": true },
         { "bSortable": true }
         ]
	});

});
    