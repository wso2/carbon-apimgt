$(document).ready(function() {
    $('.storeCheck').change(function () {
        var checkedStores = $('#externalAPIStores').val();
        if (checkedStores == "REMOVEALL") {
            checkedStores = "";
        }
        if ($(this).is(":checked")) {
            $('#externalAPIStores').val(checkedStores + "::" + $(this).val());
        } else {
            var storeValsWithoutUnchecked = "";
            var checkStoresArray = checkedStores.split("::");
            for (var k = 0; k < checkStoresArray.length; k++) {
                if (!(checkStoresArray[k] == $(this).val())) {
                    storeValsWithoutUnchecked += checkStoresArray[k] + "::";
                }
            }
            if (storeValsWithoutUnchecked == "") {
                storeValsWithoutUnchecked = "REMOVEALL";
            }
            $('#externalAPIStores').val(storeValsWithoutUnchecked);
        }
    });
});

var submitExternalStores = function() {
    $('#externalAPIStoresForm').ajaxSubmit({
        success:function (result) {
            if (!result.error) {
            	$('#externalPublishStatus').show();
            	setTimeout("hideMsg()", 2000);
            } else {
                if (result.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content:result.message,type:"error"});
                }
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            jagg.message({content:(JSON.parse(jqXHR.responseText)).message,type:"error"});
        }
    });
}

var hideMsg=function () {
    $('#externalPublishStatus').hide("fast");
}

publishToExternalStores = function() {
	submitExternalStores();
}

$('.storeCheck').change(function () {
    var checkedStores = $('#externalAPIStores').val();
    if (checkedStores == "REMOVEALL") {
        checkedStores = "";
    }
    if ($(this).is(":checked")) {
        $('#externalAPIStores').val(checkedStores + "::" + $(this).val());
    } else {
        var storeValsWithoutUnchecked = "";
        var checkStoresArray = checkedStores.split("::");
        for (var k = 0; k < checkStoresArray.length; k++) {
            if (!(checkStoresArray[k] == $(this).val())) {
                storeValsWithoutUnchecked += checkStoresArray[k] + "::";
            }
        }
        if (storeValsWithoutUnchecked == "") {
            storeValsWithoutUnchecked = "REMOVEALL";
        }
        $('#externalAPIStores').val(storeValsWithoutUnchecked);
    }
});


