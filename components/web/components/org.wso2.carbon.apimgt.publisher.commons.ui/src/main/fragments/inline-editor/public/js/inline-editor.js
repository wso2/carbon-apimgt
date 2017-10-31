$(document).ready(function () {
    var client = new API();
    var api_id = document.getElementById("apiId").value;
    var document_id = document.getElementById("docId").value;

    //$(document).on('click', "#btn-add-new-version", {api_id:api_id,api_client:client}, createNewVersion);
    
    loadDefaultTinyMCEContent(api_id, document_id);
});

function loadDefaultTinyMCEContent(apiId, documentId) {
	tinymce.init({ 
    	selector:'textarea',
        plugins: [
                  'advlist autolink lists link image charmap print preview anchor',
                  'searchreplace visualblocks autosave code fullscreen spellchecker',
                  'insertdatetime media table contextmenu paste code'
              ],
        autosave_interval: "1s",
        autosave_retention: "1440m",
        autosave_restore_when_empty: true,
        autosave_ask_before_unload: false,
        toolbar1: 'insertfile undo redo | styleselect | bold italic underline | alignleft aligncenter alignright alignjustify fontselect fontsizeselect formatselect | bullist numlist outdent indent | link unlink image',
        toolbar2: 'cut copy past | forecolor backcolor | insertdatetime | spellchecker removeformat | subscript superscript | charmap preview'
    		
    
    });
}

function saveContent(apiId, documentId) {
	
}