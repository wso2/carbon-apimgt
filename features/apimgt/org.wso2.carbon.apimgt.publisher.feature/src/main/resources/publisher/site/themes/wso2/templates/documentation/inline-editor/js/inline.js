$(document).ready(function() {
    tinyMCE.init({
    	selector: 'textarea',
	    plugins: [
				'advlist autolink lists link image charmap print preview anchor',
				'searchreplace visualblocks code fullscreen spellchecker',
				'insertdatetime media table contextmenu paste code'
		      ],
		toolbar1: 'insertfile undo redo | styleselect | bold italic underline | alignleft aligncenter alignright alignjustify fontselect fontsizeselect formatselect | bullist numlist outdent indent | link unlink image',
		toolbar2: 'cut copy past | forecolor backcolor | insertdatetime | spellchecker removeformat | subscript superscript | charmap preview',
    });
});


function loadDefaultTinyMCEContent(provider,apiName, version, docName) {
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
                      $('#inlineSpan').html('<strong>'+ i18n.t('Sorry. The content of this document cannot be loaded.')+'</strong><br />'+result.message);
                  }
              }, "json");



}

function saveContent(provider, apiName, apiVersion, docName, mode) {
	var contentDoc = tinyMCE.get('inlineEditor').getContent();
	if (docName == "Swagger API Definition") {
		/* Remove html tags */
		contentDoc = contentDoc.replace(/(<([^>]+)>)/ig,"");
		/* Remove &nbsp */
	  	contentDoc = contentDoc.replace(/&nbsp;/gi,'');
	}
    jagg.post("/site/blocks/documentation/ajax/docs.jag", { action:"addInlineContent",provider:provider,apiName:apiName,version:apiVersion,docName:docName,content:contentDoc},
              function (result) {
                  if (result.error) {
                      if (result.message == "AuthenticateError") {
                          jagg.showLogin();
                      } else {
                          jagg.message({content:result.message,type:"error"});
                      }
                  } else {
                      if (mode == "save") {
                         /* $('#messageModal').html($('#confirmation-data').html());
                          $('#messageModal h3.modal-title').html('Document Content Addition Successful');
                          $('#messageModal div.modal-body').html('\n\n Successfully saved the documentation content and you will be moved away from this tab.');
                          $('#messageModal a.btn-primary').html('OK');
                          $('#messageModal a.btn-other').hide();
                          $('#messageModal a.btn-primary').click(function() {*/
                              window.close();
                          /*});
                          $('#messageModal').modal();*/
                      } else {
                           $('#docAddMessage').show();
                           setTimeout("hideMsg()", 3000);
                      }
                  }
              }, "json");
}

var hideMsg=function () {
    $('#docAddMessage').hide("fast");
}

function navigateBack(tabName) {
  jagg.sessionAwareJS({redirect:'<%= apiUrl%>', e:event})
  $.cookie("selectedTab", tabName, {path: "/"});
}
