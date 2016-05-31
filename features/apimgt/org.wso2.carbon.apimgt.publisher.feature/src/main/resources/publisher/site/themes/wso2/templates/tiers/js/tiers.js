var updatePermissions = function (tierName, n, btn) {
    $('.' + btn).buttonLoader('start');
    var permissiontype, roles;
    permissiontype = getRadioValue($('input[name=permissionType'+n+']:radio:checked'));
    roles = document.getElementById('roles'+n).value;

    jagg.post("/site/blocks/tiers/ajax/tiers.jag", {
        action:"updatePermissions",
        tierName:tierName,
        permissiontype:permissiontype,
        roles:roles,
    }, function (result) {
        if (!result.error) {
            $('.tierPermission'+n).buttonLoader('stop');
            $('#statusUpdateMsg' + n).show();
            var t = setTimeout("hideMsg("+ n +")", 1000);
        } else {
            if (result.message == "timeout") {
                jagg.showLogin();
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }


    }, "json");

};

var getRadioValue = function (radioButton) {
    if (radioButton.length > 0) {
        return radioButton.val();
    }
    else {
        return 0;
    }
};

var hideMsg = function(n) {
    $('#statusUpdateMsg' +n).hide("slow");
}

var validateRoles=function(roles){
    var valid = false;
    jagg.syncPost("/site/blocks/item-add/ajax/add.jag", { action:"validateRoles", roles:roles },
        function (result) {
            if (!result.error) {
                valid = result.response;
            }
            return valid;
        });
    return valid;
}
$(document).ready(function(){
    $(".rolesInput").change(function( event ) {
        var divId=event.currentTarget.id;
        var increment=divId.split("roles")[1];
        var input=$( "#"+divId+"");
        if(input.val()==''){
            $("#errorTier"+increment).show();
            $("#errorTierRoles"+increment).hide();
            $("#addNewAPIButton-"+increment).prop('disabled', true);
        }else{
            $("#errorTier"+increment).hide();
            var valid1=validateRoles(input.val());
            if(!valid1){
                $("#errorTierRoles"+increment).show();
                $("#errorTier"+increment).hide();
                $("#addNewAPIButton-"+increment).prop('disabled', true);
            }else{
                $("#errorTierRoles"+increment).hide();
                $("#errorTier"+increment).hide();
                $("#addNewAPIButton-"+increment).prop('disabled', false);
            }

        }
    })

});