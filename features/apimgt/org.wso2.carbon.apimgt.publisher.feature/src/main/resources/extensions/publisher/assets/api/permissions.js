var tenantLoad = function(ctx) {
    var log = new Log();
    var Utils = ctx.utils;
    var Permissions = ctx.permissions;
    var rxtManager = ctx.rxtManager;
    var DEFAULT_ROLE = 'Internal/publisher';
    var tenantId = ctx.tenantId;
    var createPermission = function(type) {
        return '/permission/admin/manage/resources/govern/' + type + '/add';
    };
    var listPermission = function(type) {
        return '/permission/admin/manage/resources/govern/' + type + '/list';
    };
    var assignAllPermissionsToDefaultRole = function() {
        var types = rxtManager.listRxtTypes();
        var type;
        var permissions;
        //Type specific permissions
        for (var index = 0; index < types.length; index++) {
            type = types[index];
            permissions = {};
            permissions.ASSET_CREATE = createPermission(type);
            permissions.ASSET_LIST = listPermission(type);
            Utils.addPermissionsToRole(permissions, DEFAULT_ROLE, tenantId);
        }
        //Non asset type specific permissions
        permissions = {};
        permissions.ASSET_LIFECYCLE = '/permission/admin/manage/resources/govern/lifecycles';
        Utils.addPermissionsToRole(permissions, DEFAULT_ROLE, tenantId);
    };
    Permissions.ASSET_CREATE = function(ctx) {
        if (!ctx.type) {
            throw 'Unable to resolve type to determine the ASSET_CREATE permission';
        }
        return '/permission/admin/manage/resources/govern/' + ctx.type + '/add';
    };
    Permissions.ASSET_LIST = function(ctx) {
        if (!ctx.type) {
            throw 'Unable to resolve type to determine the ASSET_LIST permission';
        }
        return '/permission/admin/manage/resources/govern/' + ctx.type + '/list';
    };
    Permissions.ASSET_LIFECYCLE = '/permission/admin/manage/resources/govern/lifecycles';
    assignAllPermissionsToDefaultRole();
};