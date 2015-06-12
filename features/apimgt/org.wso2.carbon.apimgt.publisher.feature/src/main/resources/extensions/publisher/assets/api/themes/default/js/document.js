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

	$('#addDocHref').on('click',function(){
		$('#doc-add-container').css('display','inline');
		$('#overview_name').val("");
		$('#overview_summary').val("");
	});


	$('#cancel-doc-btn').on('click',function(){
		$('#doc-add-container').css('display','none');
		$('#overview_name').val("");
		$('#overview_summary').val('');

	});

	$("input[name='typeOptionRadio']").change(function(){
    	var docType = $('input[name=typeOptionRadio]:checked', '#form-document-create').val();
		if(docType != null && docType == "Other"){
			$('#otherNameControl').css('display','inline');
		}else{
			$('#otherNameControl').css('display','none');
		}
	});



	$('#add-doc-btn').on('click',function(){
		$('#doc-add-container').css('display','none');
		var ajaxURL = caramel.context + '/asts/api/apis/addDoc';
		var apiName = $('#addDocName').val();
		var provider = $('#addDocProvider').val();
		var version = $('#addDocVersion').val();

		var pageId = $('#addDocPageId').val();
		var docType = $('input[name=typeOptionRadio]:checked', '#form-document-create').val();
		var sourceType = $('input[name=sourceOptionRadio]:checked', '#form-document-create').val();
		alert(sourceType);
		var docName = $('#overview_name').val();
		var summary = $('#overview_summary').val();	
		var otherTypeName;
		var visibility;
		if(docType != null && docType=="Other"){
			otherTypeName = $('#overview_other_name').val();

		}
		

		 $.ajax({
				    type: "POST",
				    url: ajaxURL,
				    data: {
				        action:"createDocument",
				        name:apiName,
				        version:version,
				        provider:provider,
				        docType:docType,
				        sourceType:sourceType,
				        docName:docName,
				        summary:summary,
				        otherTypeName:otherTypeName,
				        visibility:visibility

				        
			
				    },
				    success: function (result) {
				        
		                BootstrapDialog.show({
			                type: BootstrapDialog.TYPE_SUCCESS,
			                title: 'success',
			                message: 'Successfully Created New API Version',
			                buttons: [{
			                
				                label: 'Close',
				                action: function(dialogItself){
					                dialogItself.close();
					                window.location.href = caramel.context+'/asts/api/docs/'+pageId;
				                }
				            
			            	}]

			            });
				           
				        },
				    error : function(result) {		                
		                
		                BootstrapDialog.show({
			                type: BootstrapDialog.TYPE_DANGER,
			                title: 'Error',
			                message: 'Error Occured while Create New Version',
			                buttons: [{
			                
				                label: 'Close',
				                action: function(dialogItself){
					                dialogItself.close();
					                window.location.href = caramel.context+'/asts/api/docs/'+pageId;
				                }
				            
			            	}]

			            });
                   	},
				          
				   
				    dataType: "json"
		}); 


	});
});