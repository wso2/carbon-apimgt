var tenantLoad = function (ctx) {

    var Utils = ctx.utils;
    var DEFAULT_ROLE = 'Internal/store';
    var permissions;
    permissions = {};

    var subscriberPermission = function () {
        return '/permission/admin/manage/api/subscribe';
    };

    permissions.SUBSCIBER = subscriberPermission();
    Utils.addPermissionsToRole(permissions, DEFAULT_ROLE, tenantId);

};
