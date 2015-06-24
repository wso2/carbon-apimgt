$(function(){
    tinyMCE.init({
                     mode : "textareas",
                     theme : "advanced",
                     plugins : "inlinepopups",
                     theme_advanced_buttons1 : "newdocument,|,bold,italic,underline,link,unlink,|,justifyleft,justifycenter,justifyright,fontselect,fontsizeselect,formatselect",
                     theme_advanced_buttons2 : "cut,copy,paste,|,bullist,numlist,|,outdent,indent,|,undo,redo,|,forecolor,backcolor",
                     theme_advanced_buttons3 : "insertdate,inserttime,|,spellchecker,advhr,,removeformat,|,sub,sup,|,charmap,emotions",
                     theme_advanced_toolbar_location : "top",
                     theme_advanced_toolbar_align : "left",
                     theme_advanced_resizing : true

                 });
});


/*function loadDefaultTinyMCEContent(provider,apiName, version, docName) {
    jagg.post("/site/blocks/documentation/ajax/docs.jag", { action:"getInlineContent", provider:provider,apiName:apiName,version:version,docName:docName },
              function (json) {
                  if (!json.error) {
                      var docName = json.doc.provider.docName;
                      var apiName = json.doc.provider.apiName;
                      var docContent = json.doc.provider.content;
                      $('#apiDeatils').empty().html('<p><h1> ' + docName + '</h1></p>');
                      tinyMCE.activeEditor.setContent(docContent);
                  } else {
                      $('#inlineError').show('fast');
                      $('#inlineSpan').html('<strong>'+ i18n.t('errorMsgs.inlineContent')+'</strong><br />'+result.message);
                  }
              }, "json");



}*/

function saveContent(provider, apiName, apiVersion, mode) {
	var contentDoc = tinyMCE.get('inlineEditor').getContent();
  var docName = $('#inlineDocName').val();
  var apiName = $('#inlineApiName').val();
  var provider = $('#inlineApiProvider').val();
  var version = $('#inlineApiVersion').val();
  alert(docName);

  var pageId = $('#inlineDocPageId').val();
  var visibility={};
  var showVisibility = $('#InlineShowVisibility').val();
  alert(showVisibility);
  if(showVisibility == "true"){
    visibility = $('#InlineDocVisibility').val();
  }
  alert(contentDoc);
  var inlineContent = contentDoc;
  alert(inlineContent);
  var action = "editInlineContent";
  var successMsg = 'Successfully Edited Inline Content';
  var errorMsg = 'Error Occured while Edit Inline Content';

  var ajaxURL = caramel.context + '/asts/api/apis/addDoc';

    $('#form-inline-editor').ajaxSubmit({
          type: "POST",
          url: ajaxURL,
          data: {
              action:action,
              name:apiName,
              version:version,
              provider:provider,
              docName:docName,
              visibility:visibility,
              inlineContent:inlineContent

              
    
          },
          success: function (result) {
              
                  BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_SUCCESS,
                    title: 'success',
                    message: successMsg,
                    buttons: [{
                    
                      label: 'Close',
                      action: function(dialogItself){
                        dialogItself.close();
                        if(mode == 'save'){
                          window.location.href = caramel.context+'/asts/api/docs/'+pageId;
                        }
                        
                      }
                  
                  }]

                });
                 
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
                        if(mode == 'save'){
                          window.location.href = caramel.context+'/asts/api/docs/'+pageId;
                        }
                      }
                  
                  }]

                });
                },
                
         
          dataType: "json"
  }); 
}

var hideMsg=function () {
    $('#docAddMessage').hide("fast");
}

