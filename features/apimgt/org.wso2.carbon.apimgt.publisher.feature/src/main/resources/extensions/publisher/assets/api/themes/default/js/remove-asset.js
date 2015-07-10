var removeAPI;
$(document).ready(function () {
    removeAPI = function (id, type) {
        $.ajax({
                   url: getDeleteUrl(id, type),
                   type: 'POST',
                   data: {_method: 'delete'},
                   success: function (result) {
                       $('#modal-redirect').modal('show');
                       setTimeout(function () {
                           window.location.reload();
                       }, 2000);
                   },
                   error: function (result) {
                       //alert("Unable to delete the API.");
                       BootstrapDialog.show({
                                                type: BootstrapDialog.TYPE_DANGER,
                                                title: 'Error',
                                                message: 'Unable to delete the API.',
                                                buttons: [
                                                    {
                                                        label: 'OK',
                                                        action: function (dialogRef) {
                                                            dialogRef.close();
                                                        }
                                                    }
                                                ]
                                            });
                   }
               });
    };

    function getDeleteUrl(id, type) {
        return caramel.context + '/apis/assets/' + id + '?type=' + type;
    }
});