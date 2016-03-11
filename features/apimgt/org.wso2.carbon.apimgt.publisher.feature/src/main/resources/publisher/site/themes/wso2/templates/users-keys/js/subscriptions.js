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
    jagg.post("/site/blocks/users-keys/ajax/subscriptions.jag", {
        action:"updateSubscription",
        apiName:apiName,
        version:version,
        provider:provider,
        appId:appId,
        newStatus:newStatus
    }, function (result) {
        if (!result.error) {
            if (newStatus == 'UNBLOCKED') {
            	$('input[name=blockType'+n+']').removeAttr('disabled');
                ahrefId.html('<span class="icon fw-stack"><i class="fw fw-block fw-stack-1x"></i></span> Block');
            } else {
            	$('input[name=blockType'+n+']').attr('disabled', 'disabled');
            	ahrefId.html('<span class="icon fw-stack"><i class="fw fw-check fw-stack-1x"></i></span> Unblock');
            }

        } else {
            jagg.message({content:result.message, type:"error"});
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
    	responsive:true,
    	 "order": [[ 3, "asc" ]]
    });
});
    