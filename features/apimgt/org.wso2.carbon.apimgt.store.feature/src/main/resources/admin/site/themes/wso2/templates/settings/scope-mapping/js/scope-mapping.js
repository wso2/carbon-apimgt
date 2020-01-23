
var toggleStuff = function($tr){
    //Toggling inputs
    $('input.mappingRolesEdit', $tr).toggleClass('hidden');
    $('.mappingRolesView', $tr).toggleClass('hidden');

    //Toggling buttons
    $('.mappingEditBtn', $tr).toggleClass('hidden');
    $('.mappingSaveBtn', $tr).toggleClass('hidden');
    $('.mappingCancelBtn', $tr).toggleClass('hidden');
}

$(document).ready(function () {
    $('.mappingEditBtn').click(function () {
        var $tr = $(this).closest('tr');

        toggleStuff($tr);
    });
    $('.mappingCancelBtn').click(function () {
        var $tr = $(this).closest('tr');
        var oldRoles = $tr.attr('data-roles');

        $('input.mappingRolesEdit', $tr).val(oldRoles);
        toggleStuff($tr);

    });

    $('.mappingSaveBtn').click(function () {
        var $tr = $(this).closest('tr');
        var scopeName = $tr.attr('data-name');

        var newRoles = $('input.mappingRolesEdit', $tr).val();
        jagg.post("/site/blocks/settings/scope-mapping/ajax/scope-mapping.jag", 
            { 
                newRoles: newRoles, 
                scopeName: scopeName,
                action: 'saveTenantConfig',
            },
            function (json) {
                if (!json.error) {
                    toggleStuff($tr);
                    $tr.attr('data-roles', newRoles);
                    $('input.mappingRolesEdit', $tr).val(newRoles);
                    $('span.mappingRolesView', $tr).text(newRoles);
                    jagg.message({content:'Successfully updated ' + scopeName, type:"info"});
                } else {
                    jagg.message({content:'Error updating ' + scopeName, type:"error"});
                }
            }, "json");

        

    });
});
