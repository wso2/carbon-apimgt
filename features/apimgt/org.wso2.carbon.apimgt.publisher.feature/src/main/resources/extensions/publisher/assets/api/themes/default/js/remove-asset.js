var removeAPI;

$(document).ready(function () {
    removeAPI = function (id, type, apiName, provider, version) {
     var count = getSubscriptionCount(apiName, provider, version);
     
     if(count != null && count > 0){
        return;
     }
        $.ajax({
                   url: getDeleteUrl(id, type),
                   type: 'POST',
                   data: {method: 'delete'},
                   success: function (result) {

                      // $('#modal-redirect').modal('show');
                      
                       setTimeout(function () {
                           window.location.reload();
                       }, 2000);
                   },
                   error: function (result) {
                       //alert("Unable to delete the API.");
                       BootstrapDialog.show({
                                                type: BootstrapDialog.TYPE_DANGER,
                                                title: 'Error',
                                                message: 'Error While deleting API ' ,
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
     }
    

    function getDeleteUrl(id, type) {
        return caramel.context + '/apis/assets/' + id + '?type=' + type;
    }

    function getSubscriptionCount(apiName, provider, version){
      var count = 0;
      var action = 'getSubscriptionCount';
      var ajaxURL = caramel.context + '/assets/api/apis/api-subscriptions/getSubscriptionCount';
      var errorMsg = 'Error occurred while retrieve Subscription count';
       $.ajax({
              type: "GET",
              url: ajaxURL,
              async: false,
              data: {
                  action:action,
                  name:apiName,
                  version:version,
                  provider:provider

              },
              success: function (result) {
                  count = result.data.count;
                  
                  if(count != null && count > 0){
                    BootstrapDialog.show({
                                  title: 'Error',
                                  message: 'Cannot remove the API. Active Subscriptions Exist',
                                  type: BootstrapDialog.TYPE_DANGER,
                                  buttons: [{
                                      label: 'Close',
                                      action: function(dialogItself){
                                         
                                          dialogItself.close();
                                      }
                                  }]
                    });
                            
                  }
                   
              },
            
              error : function(result) {                    
                    BootstrapDialog.show({
                        type: BootstrapDialog.TYPE_DANGER,
                        title: 'Error',
                        message: errorMsg,
                        buttons: [{
                        
                          label: 'Close',
                          action: function(dialogItself){
                            dialogItself.close();
                            
                          }
                      
                      }]

                      });
              },
                    
             
              dataType: "json"
      
          }); 

          return count;


    }
});