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



