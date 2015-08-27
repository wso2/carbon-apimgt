var asset = {};

(function (asset) {
    asset.process = function (type, path, destination) {
        if(!store.user) {
            $('#modal-login').modal('show');
            return;
        }
        //location.href = caramel.context + '/extensions/assets/' + type + '/process?asset=' + path + '&destination=' + encodeURIComponent(location.href);
        location.href = caramel.context + '/apis/subscriptions?type=' + type + '&asset=' + path + '&destination=' + encodeURIComponent(location.href);
    };
}(asset));