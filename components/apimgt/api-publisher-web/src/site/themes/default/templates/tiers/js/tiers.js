var updatePermissions = function (tierName, n) {
	
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
        	$('#statusUpdateMsg' + n).show();
        	var t = setTimeout("hideMsg("+ n +")", 1000);
        } else {
            jagg.message({content:result.message, type:"error"});
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

