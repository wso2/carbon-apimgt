var removeAPI;
$(document).ready(function () {
    removeAPI = function (id, type) {
        $.ajax({
                   url: getDeleteUrl(id, type),
                   type: 'POST',
                   data: {method: 'delete'},
                   success: function (result) {

                      // $('#modal-redirect').modal('show');
                       if(result.result != null && result.result.error != false){
                      console.log(result);
                    
                          BootstrapDialog.show({
                              title: 'Warning!',
                              message: result.result.data.error,
                              type: BootstrapDialog.TYPE_DANGER,
                              buttons: [{
                                  label: 'Close',
                                  action: function(dialogItself){
                                      dialogItself.close();
                                  }
                              }]
                          });
                        }
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