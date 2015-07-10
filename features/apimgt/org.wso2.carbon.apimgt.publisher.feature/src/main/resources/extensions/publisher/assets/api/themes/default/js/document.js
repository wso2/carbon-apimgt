/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */


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


    $("input:radio[name=typeOptionRadio]:first", '#form-document-create').attr('checked', true);
    $("input:radio[name=sourceOptionRadio]:first", '#form-document-create').attr('checked', true);

	$('#addDocHref').on('click',function(){
		$('#doc-add-container').css('display','inline');
		$('#overview_name').val("");
		$('#doc_summary').val("");
		
		$('.mceEditor').css('display','none');
		
		
		$('#doc_summary').show('slow');
	});


	$('#cancel-doc-btn').on('click',function(){
		$('#doc-add-container').show('slow');
		$('#overview_name').val("");
		$('#doc_summary').val('');
		var pageId = $('#addDocPageId').val();
		clearDocs();
		//window.location.href = caramel.context+'/assets/api/docs/'+pageId;

	});

//doc type selection
	$("input[name='typeOptionRadio']").change(function(){
    	var docType = $('input[name=typeOptionRadio]:checked', '#form-document-create').val();
    	if(docType == null){
    		return;
    	}
		if(docType == "Other"){
			$('#otherNameControl').show('slow');
		}else{
			$('#otherNameControl').hide('slow');
		}

		if((docType == "Public Forum" || docType == "Support Forum")){
			
			 $('#sourceDocUrl').show('slow');
             $('#docUrl').show('slow');
 
             $('#sourceOptionRadio1').attr('checked', false);
             $('#sourceOptionRadio3').attr('checked', false);
             $('#sourceOptionRadio2').attr('checked', true);
		}

		if(docType == "Other" || docType == "How To" || docType == "Samples"){
			 $('#sourceOptionRadio2').attr('checked', false);
             $('#sourceOptionRadio3').attr('checked', false);
			 $('#sourceOptionRadio1').prop("checked", true);
			 $('#sourceDocUrl').hide('slow');
			 $('#sourceFile').hide('slow');
		}
	});
//source selection
	$("input[name='sourceOptionRadio']").change(function(){
    	var sourceType = $('input[name=sourceOptionRadio]:checked', '#form-document-create').val();
    	if(sourceType == null){
    		return;
    	}
		if(sourceType == "URL"){
			$('#sourceFile').hide('slow');
			$('#sourceDocUrl').show('slow');
			

		}else if(sourceType == "File"){
			//$('#sourceDocUrl').fadeIn().css('display','none');
			$('#sourceDocUrl').hide('slow');
			$('#sourceFile').show('slow');
			$('#docUrl').val("");

		}else{
			$('#sourceDocUrl').hide('slow');
			$('#sourceFile').hide('slow');
			$('#docUrl').val("");
		}
		
	});



	$('#add-doc-btn').on('click',function(){
		//validatet name
		var nameInputElement = $('#overview_name');
		var docName = nameInputElement.val();
		var requiredMsg = $('#errorMsgRequired').val();
		if(docName == ""){
			nameInputElement.css("border", "1px solid red");
			nameInputElement.parent().append('<label class="error">' + requiredMsg + '</label>');
			return;
		}

		//validate source type
		var sourceType = $('input[name=sourceOptionRadio]:checked', '#form-document-create').val();
		if(sourceType == 'URL'){
			var docUrlElement = $("#docUrl");
	    	sourceURL= docUrlElement.val();
	    	var docValid = validInputUrl(docUrlElement);
	    	if(!docValid){
	    		 docUrlElement.css("border", "1px solid red");
	    		return;
	    	}
		}	

		var docType = $('input[name=typeOptionRadio]:checked', '#form-document-create').val();
		if(docType == 'Other'){
			var otherElement = $('#overview_other_name');
			var otherVal = otherElement.val();
			if(otherVal == ""){
				otherElement.css("border", "1px solid red");
				otherElement.parent().append('<label class="error">' + requiredMsg + '</label>');
				return;
			}
		}

		if($('#docAction').val() == "updateDocument"){
			saveOrUpdate("updateDocument");
		}else if($('#docAction').val() == "createDocument"){
			saveOrUpdate("createDocument");
			
		}		

	});


	$('ul.art-vmenu li').on("click", function(){
 

	});

});

var saveOrUpdate = function(action){
	var ajaxURL;
	var successMsg;
	var errorMsg;
	if(action == "createDocument"){
		successMsg = 'Successfully Created New Document';
		errorMsg = 'Error Occured while Create New Document';
	}else if(action == "updateDocument"){
		successMsg = 'Successfully Updated Document';
		errorMsg = 'Error Occured while Update Document';
	}

	ajaxURL = caramel.context + '/assets/api/apis/addDoc';
	$('#doc-add-container').css('display','none');
	
	var apiName = $('#addDocName').val();
	var provider = $('#addDocProvider').val();
	var version = $('#addDocVersion').val();

	var pageId = $('#addDocPageId').val();
	var docType = $('input[name=typeOptionRadio]:checked', '#form-document-create').val();
	var sourceType = $('input[name=sourceOptionRadio]:checked', '#form-document-create').val();
	var nameInputElement = $('#overview_name');
	var docName = nameInputElement.val();
	
	var summary = $('#doc_summary').val();	
	var otherTypeName;
	var visibility;
	var sourceURL;
	var showVisibility = $('#showVisibility').val();
	if(showVisibility == "true"){
		visibility = $("#docVisibility option:selected").text();
	}
	
	var filePath="";
	var contentType=""; 
	if(docType != null && docType=="Other"){
		otherTypeName = $('#overview_other_name').val();

	}

	if (sourceType == 'File') {
		filePath = $("#fileUpload").val();
        var fileExtension = getExtension(filePath);
        contentType = getMimeType(fileExtension);
        $('<input>').attr('type', 'hidden')
            .attr('name', 'mimeType').attr('value', contentType).prependTo('#form-document-create');

            


    }else if(sourceType == 'URL'){
    	var docUrlElement = $("#docUrl");
    	sourceURL= docUrlElement.val();
    	
    }

    $('#form-document-create').ajaxSubmit({
			    type: "POST",
			    url: ajaxURL,
			    data: {
			        action:action,
			        name:apiName,
			        version:version,
			        provider:provider,
			        docType:docType,
			        sourceType:sourceType,
			        docName:docName,
			        summary:summary,
			        otherTypeName:otherTypeName,
			        visibility:visibility,
			        filePath:filePath,
			        contentType:contentType,
			        sourceURL:sourceURL
			        
		
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
				                window.location.href = caramel.context+'/assets/api/docs/'+pageId;
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
				                window.location.href = caramel.context+'/assets/api/docs/'+pageId;
			                }
			            
		            	}]

		            });
               	},
			          
			   
			    dataType: "json"
	}); 

}



var updateDocumentation = function(docName, docType, summary, sourceType, docUrl, filePath, otherTypeName,visibility,updateTxt){
	$('.mceEditor').css('display','none');
	$('#doc-add-container').css('display','none');
	$('#addOrUpdateDoc').css('display','none');
	$('#doc-list-container').css('display','none');
	$('#doc_summary').show('slow');
	
	var topic = $('#docTopic').text();
	$('#docTopic').text(topic+' '+docName)
	$('#add-doc-btn').text(updateTxt);
	$('#add-doc-btn').val(updateTxt);
	$('#doc-add-container').show('slow');
	$('#docTopic').show('slow');
	$('#docAction').val('updateDocument');

	$('#overview_name').val(docName);
	$('#doc_summary').val(summary);
	if($('#showVisibility').val() == "true"){
	
		$("#docVisibility").val(visibility);
	}

	//populate with previously selected data
	 if (sourceType == "INLINE") {
            $('#sourceOptionRadio1').attr('checked', true);
     } else if(sourceType == "URL"){
            if (docUrl != "{}") {
                $('#docUrl').val(docUrl);
                $('#sourceOptionRadio2').attr('checked', true);
                $('#sourceDocUrl').show('slow');
                $('#docUrl').show('slow');
            }
     }else {
            $('#sourceOptionRadio3').attr('checked', true);
            $('#sourceFile').show('slow');
            if(filePath){
            	$("#fileUpload").val(filePath);
                $('#uploadFileName').text(filePath.split("documentation/files/")[1]);
                $('#uploadFileName').show('slow');
            }
     }

    if(docType == "How To"){
    	 $('#typeOptionRadio1').attr('checked', true);
    }else if(docType == "Samples"){
    	$('#typeOptionRadio2').attr('checked', true);
    }else if(docType == "Public Forum"){
    	$('#typeOptionRadio3').attr('checked', true);
    }else if(docType == "Support Forum"){
    	$('#typeOptionRadio4').attr('checked', true);
    }else if(docType == "Other"){
    	$('#typeOptionRadio5').attr('checked', true);
    }


};

var  editDocumentation = function(url, filePath, editContent){
	$('.mceEditor').css('display','none');
	if(url != null){
		window.open(url);
	}

};

var getInlineContent = function(provider, apiName, version,docName, mode,tenantDomain){
	var content = {};
	var action = 'getInlineContent';
	var ajaxURL = caramel.context + '/assets/api/apis/addDoc';
	var errorMsg = 'Error occurred while retrieve Inline Content';
	 $.ajax({
			    type: "GET",
			    url: ajaxURL,
			    data: {
			        action:action,
			        name:apiName,
			        version:version,
			        provider:provider,			      
			        docName:docName

			    },
			    success: function (result) {
			        content = result.data;
			        tinyMCE.activeEditor.setContent(content, {format : 'raw'});
			        editInlineContent(provider, apiName, version,docName, mode,tenantDomain);
	               
			           
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

	return content;

}

var editInlineContent	 = function (provider, apiName, version, docName, mode,tenantDomain) {

	$('#addOrUpdateDoc').hide();
	$('#doc-add-container').hide();
	$('#doc-list-container').hide();
	$('#InlineShowVisibility').val($('#showVisibility').val());
	$('#InlineDocVisibility').val($('#docVisibility').val());
	$('#inline-editor-container').show('slow');
	
	$('#inlineDocName').val(docName);
	$('.inlineDocName').each(function(){            //iterates all elements having stick class
         $(this).html(docName);       //inside the callback the 'this' is the current html element. etc ...
     });
	$('#inlineApiName').val(apiName);
  	$('#inlineApiProvider').val(provider);
  	$('#inlineApiVersion').val(version);
	$('#inlineDocPageId').val($('#addDocPageId').val());
	$('#inlineButtonGroup').show('fast');
	$('.mceEditor').css('display','inline');

};

function saveContent(provider, apiName, apiVersion, mode) {

	var contentDoc = tinyMCE.activeEditor.getContent({format:'raw'});//tinyMCE.activeEditor.getBody().textContent;//tinyMCE.get('inlineEditor').getContent();
  var docName = $('#inlineDocName').val();
  var apiName = $('#inlineApiName').val();
  var provider = $('#inlineApiProvider').val();
  var version = $('#inlineApiVersion').val();

  var pageId = $('#inlineDocPageId').val();
  if(mode == 'cancel'){
  	clearDocs();
    // window.location.href = caramel.context+'/assets/api/docs/'+pageId;
  }
  var visibility={};
  var showVisibility = $('#InlineShowVisibility').val();
  if(showVisibility == "true"){
    visibility = $('#InlineDocVisibility').val();
  }
  var inlineContent = contentDoc;
  var action = "editInlineContent";
  var successMsg = 'Successfully Edited Inline Content';
  var errorMsg = 'Error Occured while Edit Inline Content';

  var ajaxURL = caramel.context + '/assets/api/apis/addDoc';

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
                          window.location.href = caramel.context+'/assets/api/docs/'+pageId;
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
                          window.location.href = caramel.context+'/assets/api/docs/'+pageId;
                        }
                      }
                  
                  }]

                });
                },
                
         
          dataType: "json"
  }); 
};

var removeDocumentation = function(provider, apiName, version, docName, docType){
	var action = 'deleteDocument';
	var ajaxURL = caramel.context + '/assets/api/apis/addDoc';
	var errorMsg = 'Error occurred while Deleting Document';
	var successMsg = 'Successfully Deleted Document';
	 $.ajax({
			    type: "POST",
			    url: ajaxURL,
			    data: {
			        action:action,
			        name:apiName,
			        version:version,
			        provider:provider,			      
			        docName:docName,
			        docType:docType

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
				                //window.location.href = caramel.context+'/assets/api/docs/'+pageId;
				                clearDocs();
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
                        //window.location.href = caramel.context+'/assets/api/docs/'+pageId;
                        clearDocs();
                      }
                  
                  }]

                  });
               	},
			          
			   
			    dataType: "json"
	
	}); 
};

var hideMsg=function () {
    $('#docAddMessage').hide("fast");
}



var validInputUrl = function(docUrlDiv) {
    if (docUrlDiv) {
        var docUrlD;
        if (docUrlDiv.val().indexOf("http") == -1) {
            docUrlD = "http://" + docUrlDiv.val();
        } else {
            docUrlD = docUrlDiv.val();
        }
        var erCondition = validUrl(docUrlD);
        var errorMsgDocUrl = $('#errorMsgDocUrl').val();
        return validInput(docUrlDiv, errorMsgDocUrl, erCondition);
    }
};

var validInput = function(divId, message, condition) {
    if (condition) {
        divId.addClass('error');
        if (!divId.next().hasClass('error')) {
            divId.parent().append('<label class="error">' + message + '</label>');
        } else {
            divId.next().show();
            divId.next().text(message);
        }
        return false;
    } else {
        divId.removeClass('error');
        divId.next().hide();
        return true;
    }

};
var validUrl = function(url) {
    var invalid = true;
    var regex = /^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i;
    if (regex.test(url)) {
        invalid= false;
    }
    return invalid;
};


var CONTENT_MAP = {
    'js': 'application/javascript',
    'css': 'text/css',
    'csv': 'text/csv',
    'html': 'text/html',
    'json': 'application/json',
    'png': 'image/png',
    'jpeg': 'image/jpeg',
    'gif': 'image/gif',
    'svg': 'image/svg+xml',
    'ttf': 'application/x-font-ttf',
    'eot': 'application/vnd.ms-fontobject',
    'woff': 'application/font-woff',
    'otf': 'application/x-font-otf',
    'zip': 'application/zip',
    'xml': 'application/xml',
    'xhtml': 'application/xhtml+xml',
    'pdf': 'application/pdf',
    'txt': 'text/plain',
    'doc': 'application/msword',
    'ppt': 'application/vnd.ms-powerpoint',
    'docx': 'application/msword',
    'pptx': 'application/vnd.ms-powerpoint',
    'xls' : 'application/vnd.ms-excel',
    'wsdl' : 'application/api-wsdl',
    'xlsx' : 'application/vnd.ms-excel'
};

var getExtension = function(baseFileName) {
    var baseNameComponents = baseFileName.split('.');
    if (baseNameComponents.length > 1) {
        var extension = baseNameComponents[baseNameComponents.length - 1];
        return extension;
    } else {
        return 'txt';
    }

};

var getMimeType = function(extension) {
    var type=CONTENT_MAP[extension];
    if(!type){type="application/octet-stream";}
    return type;
};

var clearDocs = function () {
    window.location.reload();

};

