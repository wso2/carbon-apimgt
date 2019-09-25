var container;
$(document).ready(function() {
    $.validator.addMethod("matchPasswords", function(value) {
		return value == $("#newPassword").val();
	}, i18n.t("The passwords you entered do not match."));

    $.validator.addMethod('noSpace', function(value, element) {
            return !/\s/g.test(value);
    }, i18n.t('The name contains white spaces.'));

    var purposes = document.getElementById("consentPurposes").value;
    if (purposes != undefined && purposes != null && purposes != "") {
      purposes = JSON.parse(document.getElementById("consentPurposes").value);
    }
    var hasPurposes = document.getElementById("hasConsentPurposes").value;

    $("#sign-up").validate({
     submitHandler: function(form) {
    	var fieldCount = document.getElementById('fieldCount').value;
	var allFieldsValues;
 	for(var i = 0; i < fieldCount; i++) {
		var value = document.getElementById( i + '.0cliamUri').value;
		if ( i == 0) {
			allFieldsValues = value;
		} else {
			allFieldsValues = allFieldsValues + "|" + value;
		}
	}
        var tenantDomain = document.getElementById('hiddenTenantDomain').value;
        var fullUserName;
        if(tenantDomain == "null" || tenantDomain == "carbon.super") {
            fullUserName = document.getElementById('newUsername').value;
        } else {
            fullUserName = document.getElementById('newUsername').value + "@" 
                    + tenantDomain;
        }

    	jagg.post("/site/blocks/user/sign-up/ajax/user-add.jag", {
            action:"addUser",
            username:fullUserName,
            password:$('#newPassword').val(),
            allFieldsValues:allFieldsValues
        }, function (result) {
            if (result.error == false) {
                if(result.showWorkflowTip) {
                    jagg.message({content: i18n.t("User account awaiting Administrator approval.") ,type:"info",
                        cbk:function() {
                            if (hasPurposes == 'true') {
                                var receipt = addReceiptInformation(container);
                                $('<input />').attr('type', 'hidden')
                                    .attr('name', "consent")
                                    .attr('value', JSON.stringify(receipt))
                                    .appendTo('#signUpRedirectForm');
                            }
                            $('#signUpRedirectForm').submit();
                        }
                    });
                } else {
                    jagg.message({content: i18n.t("User added successfully. You can now sign into the API store using the new user account."), type:"info",
                        cbk:function() {
                            if (hasPurposes == 'true') {
                                var receipt = addReceiptInformation(container);
                                $('<input />').attr('type', 'hidden')
                                    .attr('name', "consent")
                                    .attr('value', JSON.stringify(receipt))
                                    .appendTo('#signUpRedirectForm');
                            }
                            $('#signUpRedirectForm').submit();
                        }
                    });
                }
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
     }
    });
    $("#newPassword").keyup(function() {
        $(this).valid();
    });
    $('#newPassword').focus(function(){
        $('#password-help').show();
        $('.password-meter').show();
    });
    $('#newPassword').blur(function(){
        $('#password-help').hide();
        $('.password-meter').hide();
    });

    var agreementChk = $(".agreement-checkbox input");
    var registrationBtn = $("#registrationSubmit");

    if (agreementChk.length > 0) {
        registrationBtn.prop("disabled", true).addClass("disabled");
    }
    agreementChk.click(function() {
        if ($(this).is(":checked")) {
            registrationBtn.prop("disabled", false).removeClass("disabled");
        } else {
           registrationBtn.prop("disabled", true).addClass("disabled");
        }
   });
   if (hasPurposes == 'true') {
    renderReceiptDetails(purposes);
   }
});

function renderReceiptDetails(data) {

    var treeTemplate =
        '<div id="html1">' +
        '<ul><li class="jstree-open" data-jstree=\'{"icon":"icon-book"}\'>All' +
        '<ul>' +
        '{{#purposes}}' +
        '<li data-jstree=\'{"icon":"icon-book"}\' purposeid="{{purposeId}}">{{purpose}}{{#if description}} : {{description}}{{/if}}<ul>' +
        '{{#piiCategories}}' +
        '<li data-jstree=\'{"icon":"icon-user"}\' piicategoryid="{{piiCategoryId}}">{{#if displayName}}{{displayName}}{{else}}{{piiCategory}}{{/if}}</li>' +
        '</li>' +
        '{{/piiCategories}}' +
        '</ul>' +
        '{{/purposes}}' +
        '</ul></li>' +
        '</ul>' +
        '</div>';

    var tree = Handlebars.compile(treeTemplate);
    var treeRendered = tree(data);

    $("#tree-table").html(treeRendered);

    container = $("#html1").jstree({
        plugins: ["table", "sort", "checkbox", "actions", "wholerow"],
        checkbox: { "keep_selected_style" : false },
    });

}

function addReceiptInformation(container) {
    var newReceipt = {};
    var services = [];
    var service = {};

    var selectedNodes = container.jstree(true).get_selected('full',true);
    var undeterminedNodes = container.jstree(true).get_undetermined('full',true);

    if (!selectedNodes || selectedNodes.length < 1 ) {
        return;
    }
    selectedNodes = selectedNodes.concat(undeterminedNodes);
    var relationshipTree = unflatten(selectedNodes); //Build relationship tree
    var purposes = relationshipTree[0].children;
    var newPurposes =[];

    for (var i = 0; i < purposes.length; i++) {
        var purpose = purposes[i];
        var newPurpose = {};
        newPurpose["purposeId"]  =  purpose.li_attr.purposeid;
        newPurpose['piiCategory'] = [];
        newPurpose['purposeCategoryId'] = [1];
        var piiCategory = [];
        var categories = purpose.children;
        for (var j = 0; j < categories.length; j++) {
            var category = categories[j];
            var c = {};
            c['piiCategoryId']  =  category.li_attr.piicategoryid;
            piiCategory.push(c);
        }
        newPurpose['piiCategory'] = piiCategory;
        newPurposes.push(newPurpose);
    }
    service['purposes'] = newPurposes;
    services.push(service);
    newReceipt['services'] = services;

    return newReceipt;
}

function unflatten(arr) {
    var tree = [],
        mappedArr = {},
        arrElem,
        mappedElem;

    // First map the nodes of the array to an object -> create a hash table.
    for (var i = 0, len = arr.length; i < len; i++) {
        arrElem = arr[i];
        mappedArr[arrElem.id] = arrElem;
        mappedArr[arrElem.id]['children'] = [];
    }

    for (var id in mappedArr) {
        if (mappedArr.hasOwnProperty(id)) {
            mappedElem = mappedArr[id];
            // If the element is not at the root level, add it to its parent array of children.
            if (mappedElem.parent && mappedElem.parent != "#" && mappedArr[mappedElem['parent']]) {
                mappedArr[mappedElem['parent']]['children'].push(mappedElem);
            } else { // If the element is at the root level, add it to first level elements array.
                tree.push(mappedElem);
            }
        }
    }
    return tree;
}

var showMoreFields = function () {
	$('#moreFields').show();
	$('#moreFieldsLink').hide();
	$('#hideFieldsLink').show();
}
var hideMoreFields = function () {
	$('#moreFields').hide();
	$('#hideFieldsLink').hide();
	$('#moreFieldsLink').show();
}
