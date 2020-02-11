
var toggleStuff = function($tr){
    //Toggling inputs
    $('input.mappingRolesEdit', $tr).toggleClass('hidden');
    $('.mappingRolesView', $tr).toggleClass('hidden');

    //Toggling buttons
    $('.mappingEditBtn', $tr).toggleClass('hidden');
    $('.mappingSaveBtn', $tr).toggleClass('hidden');
    $('.mappingCancelBtn', $tr).toggleClass('hidden');
}

var renderRoleMappings = function() {
    var roleMappingsElement = $("#role_mappings");
    var roleMappingsValue = roleMappingsElement.val();

    var roleMappingsJSON = JSON.parse(roleMappingsValue);
    var roleMappings = {"roleMappings" : roleMappingsJSON};

    if (roleMappingsJSON != null && Object.keys(roleMappingsJSON).length == 0) {
        roleMappings = null;
    }

    var roleMappingsOutput = Handlebars.partials['role-mappings-add-template'](roleMappings);
    $('#roleListing').html(roleMappingsOutput);
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

    propertiesTemplate = $("#role-mappings-add-template").html();
    if (propertiesTemplate) {
        Handlebars.partials['role-mappings-add-template'] = Handlebars.compile(propertiesTemplate);
        renderRoleMappings();
    }

    $(document).on("click", '#role_mapping_add', function () {
        var originalRoleVal = $("#original_role").val();
        if (!originalRoleVal || originalRoleVal.trim() == "") {
            $("#original_role_error").text(i18n.t("Original role cannot be empty.")).removeClass("hidden");
            return;
        }
        var mappedRoleVal = $("#mapped_role").val();
        if (!mappedRoleVal || mappedRoleVal.trim() == "") {
            $("#mapped_role_error").text(i18n.t("Mapped role(s) list cannot be empty.")).removeClass("hidden");
            return;
        }
        originalRoleVal = originalRoleVal.trim();
        mappedRoleVal = mappedRoleVal.trim();

        //update tenant-conf.json
        jagg.post("/site/blocks/settings/scope-mapping/ajax/scope-mapping.jag",
            {
                originalRole: originalRoleVal,
                mappedRoles: mappedRoleVal,
                action: 'saveRoleMappingConfig',
            },
            function (json) {
                if (!json.error) {
                    jagg.message({content:'Successfully updated role mapping for ' + originalRoleVal, type:"info"});
                } else {
                    jagg.message({content:'Error while updating role mapping for ' + originalRoleVal, type:"error"});
                }
            }, "json");
        $("#original_role").val("");
        $("#mapped_role").val("");

        //render role mappings table
        var addMappingJSON = JSON.parse($("#role_mappings").val()) != null ? JSON.parse($("#role_mappings").val()) : {};
        addMappingJSON[originalRoleVal] = mappedRoleVal;
        $("#role_mappings").val(JSON.stringify(addMappingJSON).replace(" ", ""));
        renderRoleMappings();
    });

    $("#original_role").on("change", function () {
        $("#original_role_error").addClass("hidden");
        $("#mapped_role_error").addClass("hidden");
    });

    $("#mapped_role").on("change", function () {
        $("#original_role_error").addClass("hidden");
        $("#mapped_role_error").addClass("hidden");
    });

    $(document).on("click", ".delete-role-mappings", function (event) {
        $("#messageModal div.modal-footer").html("");
        var key = $(this).attr('data-key');
        var val = $(this).attr('data-value');
        jagg.message({
            content: i18n.t("Do you want to remove ") + "'" + key + "' : '" + val + "'" + i18n.t(" from role mappings list ? "),
            type: 'confirm',
            title: i18n.t("Remove Role Mapping"),
            okCallback: function () {
                var deleteMappingJSON = JSON.parse($("#role_mappings").val());
                delete deleteMappingJSON[key];
                $("#role_mappings").val(JSON.stringify(deleteMappingJSON).replace(" ", ""));
                jagg.post("/site/blocks/settings/scope-mapping/ajax/scope-mapping.jag",
                    {
                        originalRole: key,
                        mappedRoles: null,
                        action: 'saveRoleMappingConfig',
                    },
                    function (json) {
                        if (!json.error) {
                            jagg.message({content:'Successfully deleted role mapping for ' + originalRoleVal, type:"info"});
                        } else {
                            jagg.message({content:'Error while deleting role mapping for ' + originalRoleVal, type:"error"});
                        }
                    }, "json");
                renderRoleMappings();
            }
        });
    });
});
