$(document).ready(function(){

    $('a.help_popup').popover({
        html : true,
        container: 'body',
        content: function() {
          var msg = $('#'+$(this).attr('help_data')).html();
          return msg;
        },
        template: '<div class="popover default-popover" role="tooltip"><div class="arrow"></div><div class="popover-content"></div></div>'
    });

    $(".implementation_methods").change(function(event){
        $(".implementation_method_"+$(this).val()).removeClass('hide');
        $(".implementation_method").hide();
        $(".implementation_method_"+$(this).val()).show();
    });

    if($('#toggleThrottle').attr('checked')){
        $('#toggleThrottle').parent().next().show();
    } else {
        $('#toggleThrottle').parent().next().hide();
    }
   if( $("#toggleCorsManaged").attr('checked') ) {
        $('#corsTableManaged').show();
       } 
      else {
        $('#corsTableManaged').hide();
      }
   if($('#toggleallOriginManaged').attr('checked')) {
       $('#allowCredentialsManaged').attr("checked",false);
       $('#allowCredentialsManaged').hide();
       $('.originContainerManaged').hide();
      } else {
        $('#allowCredentialsManaged').show();
        $('#allOriginContainerManaged').hide();
        $('.originContainerManaged').show();
        } 
 
     $('#endpointType').on('change',function() {
        var endpointType = $('#endpointType').find(":selected").val();
        if (endpointType == "secured") {
            var endpointAuthType = $('#endpointAuthType').find(":selected").val();
            $('#endpointAuthType').show();
            $('#credentials').show();
        } else {
            $('#endpointAuthType').hide();
            $('#credentials').hide();
        }
    });
    $('#endpointType').trigger('change');

    /*$("#implement_form").submit(function (e) {
      e.preventDefault();
    });*/


   var previousClicked = "";
    $('.api-implement-type').click(function(){
        $($(this).attr('value')).slideToggle();
        if(previousClicked !="" && previousClicked != $(this).attr('value')){
            $(previousClicked).slideUp();
        }
        previousClicked=$(this).attr('value');
    });


    var v = $("#implement_form").validate({
        submitHandler: function(form) {        
        var designer = APIDesigner();
        APP.update_ep_config("managed");
        $('.swagger').val(JSON.stringify(designer.api_doc));

        $('#'+thisID).buttonLoader('start');
        $(form).ajaxSubmit({
            success:function(responseText, statusText, xhr, $form) {
             if (!responseText.error) {
                var designer = APIDesigner();
                designer.saved_api = {};
                designer.saved_api.name = responseText.data.apiName;
                designer.saved_api.version = responseText.data.version;
                designer.saved_api.provider = responseText.data.provider;
                $('#'+thisID).buttonLoader('stop');
                $( "body" ).trigger( "api_saved" );
                if(!$("#hiddenGoToManage").val()){
                    $('#apiSaved').show();
                }
                setTimeout("hideMsg()", 3000);
             } else {
                 if (responseText.message == "timeout") {
                     if (ssoEnabled) {
                         var currentLoc = window.location.pathname;
                         var queryString=encodeURIComponent(window.location.search);
                         if (currentLoc.indexOf(".jag") >= 0) {
                             location.href = "login.jag?requestedPage=" + currentLoc + queryString;
                         } else {
                             location.href = 'site/pages/login.jag?requestedPage=' + currentLoc + queryString;
                         }
                     } else {
                         jagg.showLogin();
                     }
                 } else {
                     jagg.message({content:responseText.message,type:"error"});
                 }
                 $('#'+thisID).buttonLoader('stop');
             }
            }, dataType: 'json'
        });
        },
        errorPlacement: function (error, element) {
             if (element.parent().hasClass("input-append")){
                error.insertAfter(element.parent());
             }else{
                error.insertAfter(element);
             }
        },
    });
    
    var v = $("#prototype_form").validate({
        submitHandler: function(form) {        
        var designer = APIDesigner();
        var endpoint_config = {"production_endpoints":{"url": $("#prototype_endpoint").val(),"config":null},"endpoint_type":"http","implementation_status":"prototyped"}
        $('.swagger').val(JSON.stringify(designer.api_doc));
        $('.prototype_config').val(JSON.stringify(endpoint_config));

        $('#'+thisID).buttonLoader('start');

        $(form).ajaxSubmit({
            success:function(responseText, statusText, xhr, $form) {
             if (!responseText.error) {
                var designer = APIDesigner();
                designer.saved_api = {};
                designer.saved_api.name = responseText.data.apiName;
                designer.saved_api.version = responseText.data.version;
                designer.saved_api.provider = responseText.data.provider;
                $('#'+thisID).buttonLoader('stop');
                $( "body" ).trigger( "prototype_saved" );
                $('#apiSaved').removeClass('hide');
                setTimeout("hideMsg()", 3000);
             } else {
                 if (responseText.message == "timeout") {
                     if (ssoEnabled) {
                         var currentLoc = window.location.pathname;
                         var queryString=encodeURIComponent(window.location.search);
                         if (currentLoc.indexOf(".jag") >= 0) {
                             location.href = "login.jag?requestedPage=" + currentLoc + queryString;
                         } else {
                             location.href = 'site/pages/login.jag?requestedPage=' + currentLoc + queryString;
                         }
                     } else {
                         jagg.showLogin();
                     }
                 } else {
                              var message=responseText.message;
                              showGatewayFailure(message);
                         }
                 $('#'+thisID).buttonLoader('stop');
             }
            }, dataType: 'json'
        });
        }
    });
    
    $("#prototyped_api").click(function(e){
        $("body").on("prototype_saved", function(e){
            $("body").unbind("prototype_saved");
                var designer = APIDesigner();            
                $.ajax({
                    type: "POST",
                    url: jagg.site.context + "/site/blocks/life-cycles/ajax/life-cycles.jag",
                    data: {
                        action :"updateStatus",
                        name:designer.saved_api.name,
                        version:designer.saved_api.version,
                        provider: designer.saved_api.provider,
                        status: "Deploy as a Prototype",
                        publishToGateway:true,
                        requireResubscription:true
                    },
                    success: function(responseText){
                        if (!responseText.error) {
                             $("#prototype-success").modal('show');
                        }else{
                             if (responseText.message == "timeout") {
                                 if (ssoEnabled) {
                                     var currentLoc = window.location.pathname;
                                     if (currentLoc.indexOf(".jag") >= 0) {
                                         location.href = "index.jag";
                                     } else {
                                    	 location.href = 'site/pages/index.jag';
                                     }
                                 } else {
                                     jagg.showLogin();
                                 }
                             }else {
                              var message=responseText.message;
                              showGatewayFailure(message);
                         }
                        }
                    },
                    dataType: "json"
                });               
            });
        $("#prototype_form").submit();
	return false;                         
    });    
    

    if( $("#toggleCorsPrototyped").attr('checked') ) {
        $('#corsTablePrototyped').show();
    }
    else {
        $('#corsTablePrototyped').hide();
    }
    if($('#toggleallOriginPrototyped').attr('checked')) {
        $('#allowCredentialsPrototyped').attr("checked",false);
        $('#allowCredentialsPrototyped').hide();
        $('.originContainerPrototyped').hide();
    } else {
        $('#allowCredentialsPrototyped').show();
        $('#allOriginContainerPrototyped').hide();
        $('.originContainerPrototyped').show();
    }

    // last saved implementation state
   if ($('#endpoint_config').val() != "") {
	    var endpoint_config = jQuery.parseJSON($('#endpoint_config').val());
	    if ($('#endpoint_config').val()){
		if(endpoint_config.implementation_status == "managed"){
		    $('#prototype').hide();
		    $('#managed-api').slideDown();
		}else if(endpoint_config.implementation_status == "prototyped"){
		    $('#managed-api').hide();
		    $('#prototype').slideDown();
		}
	    }
    }

    loadInSequences();
    loadOutSequences();
    loadFaultSequences();

    if ( $("#toggleSequence").attr('checked') ) {
        $('#toggleSequence').parent().next().show();
    }
    else {
        $('#toggleSequence').parent().next().hide();
    }
    
    $('#upload_sequence').attr('disabled','disabled');
    $('.toggleRadios input[type=radio]').click(function(){
        if (typeof jsonFile != 'undefined') {
            $('#upload_sequence').removeAttr("disabled");
        } else {
            $('#upload_sequence').attr('disabled','disabled');
        }
    });

    $('#sequence_file').change(function (event) {
        var file = event.target.files[0];
        var fileReader = new FileReader();
        fileReader.addEventListener("load", function (event) {
            jsonFile = event.target;
        });
        $('#upload_sequence').removeAttr("disabled");
    });

    $('#upload_sequence').click(function () {
    	
    	$('#upload_sequence').buttonLoader('start');
        	var type = $('.toggleRadios input[type=radio]:checked').val();
        	uploadSequence(type);
            $('#upload_sequence').buttonLoader('stop');
    });

});

var thisID='';
$('#saveBtn').click(function(e){
    thisID = $(this).attr('id');
});

$('#savePrototypeBtn').click(function(e){
    thisID = $(this).attr('id');
});

$('#prototyped_api').click(function(e){
    thisID = $(this).attr('id');
});

$('#go_to_manage').click(function(e){
    thisID = $(this).attr('id');
});

$('#save_policies').click(function(e){
    thisID = $(this).attr('id');
});


function uploadSequence (type) {
	var file = $('#sequence_file').get(0).files[0];
	var formData = new FormData();
	formData.append('file', file);
	formData.append('name', $('#name').val());
	formData.append('version', $('#version').val());
	formData.append('provider', $('#provider').val());
	formData.append('seqType', type);
	formData.append('action', "uploadSequence");
	$.ajax({
                type: "POST",
                url: jagg.site.context + "/site/blocks/item-design/ajax/add.jag",
                data: formData,
                processData: false,
                contentType: false,
                success: function(responseText){
                    if (!responseText.error) {
                    	if (type == "in") {
                    		if ($("#inSequence option[value='" + responseText.fileName + "']").length == 0) {
                    			$('#inSequence').append($("<option></option>").attr("value",responseText.fileName).text(responseText.fileName));
                    		}                    		
                    		$("#inSequence option[value='" + responseText.fileName + "']").attr("selected", "selected");
                    	} else if (type == "out") {
                    		if ($("#outSequence option[value='" + responseText.fileName + "']").length == 0) {
                    			$('#outSequence').append($("<option></option>").attr("value",responseText.fileName).text(responseText.fileName));
                    		}                    		
                    		$("#outSequence option[value='" + responseText.fileName + "']").attr("selected", "selected");
                    	} else if (type == "fault") {
                    		if ($("#faultSequence option[value='" + responseText.fileName + "']").length == 0) {
                    			$('#faultSequence').append($("<option></option>").attr("value",responseText.fileName).text(responseText.fileName));
                    		}                    		
                    		$("#faultSequence option[value='" + responseText.fileName + "']").attr("selected", "selected");
                    	}
                    	$("#sequenceUpload").modal('hide');
                    	$('#sequence_file_value').val('');
                    	$('#sequence_file_help').addClass('hide');
                    }else{
                         if (responseText.message == "timeout") {
                             if (ssoEnabled) {
                                 var currentLoc = window.location.pathname;
                                 if (currentLoc.indexOf(".jag") >= 0) {
                                     location.href = "index.jag";
                                 } else {
                                	 location.href = 'site/pages/index.jag';
                                 }
                             } else {
                                 jagg.showLogin();
                             }
                         } else {
                          var message = responseText.message;
                          $('#sequence_file_help').text(responseText.message);
                          $('#sequence_file_help').removeClass('hide');
                     }
                    }
                },
                dataType: "json"
            });               
return true;                         
}

var hideMsg = function () {
    $('#apiSaved').hide("slow");
}


function showGatewayFailure(message) {
    if (message.split("||")[1] == "warning") {
        var environmentsFailed = JSON.parse(message.split("||")[0]);
        var failedToPublishEnvironments = environmentsFailed.PUBLISHED;
        var failedToUnpublishedEnvironments = environmentsFailed.UNPUBLISHED;
        var divPublish = "", divUnpublished = "";
        for (i = 0; i < failedToPublishEnvironments.split(",").length; i++) {
            var splitPublished = (failedToPublishEnvironments.split(",")[i]).split(":");
            divPublish += splitPublished[0] + "<br>" + splitPublished[1] + "<br>";
        }
        for (i = 0; i < failedToUnpublishedEnvironments.split(",").length; i++) {
            var splitUnPublished = (failedToUnpublishedEnvironments.split(",")[i]).split(":");

            divUnpublished += splitUnPublished[0] + "<br>" + splitUnPublished[1] + "<br>";
        }
        $("#modal-published-content").empty();
        $("#modal-unpublished-content").empty();
        $("#modal-published-content").append(divPublish);
        $("#modal-unpublished-content").append(divUnpublished);
        if (failedToPublishEnvironments == "") {
            $("#modal-published-header").hide();
            $("#modal-published-content").hide();
        }
        if (failedToUnpublishedEnvironments == "") {
            $("#modal-unpublished-header").hide();
            $("#modal-unpublished-content").hide();
        }
        $("#retryType").val("manage");
        $("#environmentsRetry-modal").modal('show');
    }
    else {

        jagg.message({content: responseText.message, type: "error"});
    }
}

function loadInSequences() {

    if(inSequencesLoaded){
        return;
    }

        jagg.post("/site/blocks/item-add/ajax/add.jag", {
                action : "getCustomInSequences", provider:apiProvider, apiName:apiName, apiVersion:apiVersion
            },
              function(result) {
                  if (!result.error) {
                      var arr = [];
                      if (result.sequences.length == 0) {
                          var msg = "No defined sequences";
                          $('<input>').
                                  attr('type', 'hidden').
                                  attr('name', 'inSeq').
                                  attr('id', 'inSeq').
                                  attr('value', msg).
                                  appendTo('#manage_form');
                      } else {
                          for ( var j = 0; j < result.sequences.length; j++) {
                              arr.push(result.sequences[j]);
                          }
                          for ( var i = 0; i < arr.length; i++) {
                              if(result.sequences[i] == insequence){
                                  $('#inSequence').append('<option value="'+result.sequences[i]+'" selected="selected">'+result.sequences[i]+'</option>');
                              }else{
                                  $('#inSequence').append('<option value="'+result.sequences[i]+'">'+result.sequences[i]+'</option>');
                              }
                              $('<input>').
                                      attr('type', 'hidden').
                                      attr('name', 'inSeq').
                                      attr('id', 'inSeq').
                                      attr('value', result.sequences[i]).
                                      appendTo('#manage_form');

                          }
                      }
                      inSequencesLoaded = true;
                  }else {
                      if (result.message == "timeout") {
                          jagg.showLogin();
                      }
                  }
              }, "json");
}

function loadOutSequences() {

    if(outSequencesLoaded){
        return;
    }

    jagg.post("/site/blocks/item-add/ajax/add.jag", {
                action : "getCustomOutSequences" , provider:apiProvider, apiName:apiName, apiVersion:apiVersion
            },
              function(result) {
                  if (!result.error) {
                      var arr = [];
                      if (result.sequences.length == 0) {
                          var msg = "No defined sequences";
                          $('<input>').
                                  attr('type', 'hidden').
                                  attr('name', 'outSeq').
                                  attr('id', 'outSeq').
                                  attr('value', msg).
                                  appendTo('#manage_form');
                      }else {
                          for ( var j = 0; j < result.sequences.length; j++) {
                              arr.push(result.sequences[j]);
                          }
                          for(var i=0; i<arr.length; i++){
                              if(result.sequences[i] == outsequence){
                                  $('#outSequence').append('<option value="'+result.sequences[i]+'" selected="selected">'+result.sequences[i]+'</option>');
                              }
                              else{
                                  $('#outSequence').append('<option value="'+result.sequences[i]+'">'+result.sequences[i]+'</option>');
                              }
                              $('<input>').
                                      attr('type', 'hidden').
                                      attr('name', 'outSeq').
                                      attr('id', 'outSeq').
                                      attr('value', result.sequences[i]).
                                      appendTo('#manage_form');

                          }
                      }
                      outSequencesLoaded = true;
                  }else {
                      if (result.message == "timeout") {
                          jagg.showLogin();
                      }
                  }
              }, "json");
}

function loadFaultSequences() {

    if(faultSequencesLoaded){
        return;
    }

    jagg.post("/site/blocks/item-add/ajax/add.jag", {
                action : "getCustomFaultSequences" , provider:apiProvider, apiName:apiName, apiVersion:apiVersion
            },
              function(result) {
                  if (!result.error) {
                      var arr = [];
                      if (result.sequences.length == 0) {
                          var msg = "No defined sequences";
                          $('<input>').
                                  attr('type', 'hidden').
                                  attr('name', 'faultSeq').
                                  attr('id', 'faultSeq').
                                  attr('value', msg).
                                  appendTo('#manage_form');
                      }else {
                          for ( var j = 0; j < result.sequences.length; j++) {
                              arr.push(result.sequences[j]);
                          }
                          for(var i=0; i<arr.length; i++){
                              if(result.sequences[i] == faultsequence){
                                  $('#faultSequence').append('<option value="'+result.sequences[i]+'" selected="selected">'+result.sequences[i]+'</option>');
                              }
                              else{
                                  $('#faultSequence').append('<option value="'+result.sequences[i]+'">'+result.sequences[i]+'</option>');
                              }
                              $('<input>').
                                      attr('type', 'hidden').
                                      attr('name', 'faultSeq').
                                      attr('id', 'faultSeq').
                                      attr('value', result.sequences[i]).
                                      appendTo('#manage_form');

                          }
                      }
                      faultSequencesLoaded = true;
                  }else {
                      if (result.message == "timeout") {
                          jagg.showLogin();
                      }
                  }
              }, "json");
}



$("#toggleSequence").change(function(e){
    if($(this).is(":checked")){
        $('#seqTable').show();
        $('#uploadSeqDiv').show();
        loadInSequences();
        loadOutSequences();
        loadFaultSequences();
    }else{
    	$('#seqTable').hide();
    	$('#uploadSeqDiv').hide();
        $('#faultSequence').val('');
        $('#inSequence').val('') ;
        $('#outSequence').val('');
    }
});
$("#toggleCorsManaged").change(function(e){
    if($(this).is(":checked")){
        $(this).parent().parent().parent().next().children().next().children().show();
    }else{
    	$(this).parent().parent().parent().next().children().next().children().hide();
    }
});

$("#toggleCorsPrototyped").change(function(e){
    if($(this).is(":checked")){
        $(this).parent().parent().parent().next().children().next().children().show();
    }else{
    	$(this).parent().parent().parent().next().children().next().children().hide();
    }
});
