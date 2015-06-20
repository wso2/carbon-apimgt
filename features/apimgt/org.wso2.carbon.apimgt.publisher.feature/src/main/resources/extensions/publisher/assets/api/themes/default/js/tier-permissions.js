var updatePermissions = function (tierName, n, btn) {
    $('.' + btn).buttonLoader('start');
    var permissiontype, roles;
    permissiontype = getRadioValue($('input[name=permissionType'+n+']:radio:checked'));
    roles = document.getElementById('roles'+n).value;
    var tierUrl = caramel.context + "/asts/api/apis/tiers";
    $.post(tierUrl, {
        action:"updatePermissions",
        tierName:tierName,
        permissiontype:permissiontype,
        roles:roles
    }, function (result) {
        if (!result.error) {
            $('.tierPermission'+n).buttonLoader('stop');
            $('#statusUpdateMsg' + n).show();
            var t = setTimeout("hideMsg("+ n +")", 1000);
        } else {
            BootstrapDialog.show({
                                     type: BootstrapDialog.TYPE_DANGER,
                                     title: 'Error',
                                     message:  result.message,
                                     buttons: [{
                                                   label: 'Ok',
                                                   action: function(dialogItself){
                                                       dialogItself.close();
                                                   }
                                               }]
                                 });
        }


    });

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

var validateRoleUrl = caramel.context + "/asts/api/apis/validation";
var validateRoles=function(roles){
    var valid = false;
    $.post(validateRoleUrl, { action:"validateRoles", roles:roles },
                  function (result) {
                      if (!result.error) {
                          valid = result.response;
                      }
                      return valid;
                  });
    return valid;
};

$(document).ready(function(){
    $(".rolesInput").change(function( event ) {
        var divId=event.currentTarget.id;
        var increment=divId.split("roles")[1];
        var input=$( "#"+divId+"");
        if(input.val()==''){
            $("#errorTier"+increment).show();
            $("#errorTierRoles"+increment).hide();
        }else{
            $("#errorTier"+increment).hide();
            var valid1=validateRoles(input.val());
            if(!valid1){
                $("#errorTierRoles"+increment).show();
                $("#errorTier"+increment).hide();
            }else{
                $("#errorTierRoles"+increment).hide();
                $("#errorTier"+increment).hide();
            }

        }
    })

});
