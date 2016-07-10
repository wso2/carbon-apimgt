$(document).ready(function(){
    var cors_config;
    if($('#corsConfigurationManaged').val() != ""){
        cors_config = jQuery.parseJSON($('#corsConfigurationManaged').val());
    }

    $(".cors-ui-container").corsUi({
        config : cors_config
    });

    $(".cors-ui-prototype-container").corsUi({
        config : cors_config
    });

    var endpoint_config;
    if($('#endpoint_config').val() != ""){
        endpoint_config = jQuery.parseJSON($('#endpoint_config').val());
    }
    $("#endpoint-ui").apimEndpointUi({
        config : endpoint_config
    });

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
   //if( $("#toggleCorsManaged").attr('checked') ) {
   //     $('#corsTableManaged').show();
   //    }
   //   else {
   //     $('#corsTableManaged').hide();
   //   }
   if($('#toggleallOriginManaged').attr('checked')) {
       $('#allowCredentialsManaged').attr("checked",false);
       $('#allowCredentialsManaged').hide();
       $('.originContainerManaged').hide();
      } else {
        $('#allowCredentialsManaged').show();
        $('#allOriginContainerManaged').hide();
        $('.originContainerManaged').show();
        } 
 
    // @Todo: remove code
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
        if(!$("#endpoint-ui").data("plugin_apimEndpointUi").validate()){
            return;
        }
        $('#endpoint_config').val(JSON.stringify($("#endpoint-ui").data("plugin_apimEndpointUi").get_endpoint_config()));
        $('.swagger').val(JSON.stringify(designer.api_doc));
        $('#corsConfigurationManaged').val(JSON.stringify($(".cors-ui-container").data("plugin_corsUi").get_cors_config()));



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
        $('#corsConfigurationPrototyped').val(JSON.stringify($(".cors-ui-prototype-container").data("plugin_corsUi").get_cors_config()));
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
        	var type = $(".modal-body #flow_id").val();
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
                success: $.proxy(function(responseText){        
                    if (!responseText.error) {
                    	if (this.type == "in") {
                    		if ($("#inSequence option[value='" + responseText.fileName + "']").length == 0) {
                    			$('#inSequence').append($("<option></option>").attr("value",responseText.fileName).text(responseText.fileName));
                    		}                    		
                    		$("#inSequence option[value='" + responseText.fileName + "']").attr("selected", "selected");
                            $('#inSequence').selectpicker('render');
                    	} else if (this.type == "out") {
                    		if ($("#outSequence option[value='" + responseText.fileName + "']").length == 0) {
                    			$('#outSequence').append($("<option></option>").attr("value",responseText.fileName).text(responseText.fileName));
                    		}                    		
                    		$("#outSequence option[value='" + responseText.fileName + "']").attr("selected", "selected");
                            $('#outSequence').selectpicker('render');
                    	} else if (this.type == "fault") {
                    		if ($("#faultSequence option[value='" + responseText.fileName + "']").length == 0) {
                    			$('#faultSequence').append($("<option></option>").attr("value",responseText.fileName).text(responseText.fileName));
                    		}                    		
                    		$("#faultSequence option[value='" + responseText.fileName + "']").attr("selected", "selected");
                            $('#faultSequence').selectpicker('render');
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
                }, { "type": type }),
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
                action : "getCustomCombinedInSequences", provider:apiProvider, apiName:apiName, apiVersion:apiVersion
            },
              function(result) {
                  if (!result.error) {
                      var arr = [];
                      var arrUserDefined = [];
                      if (result.sequences.defaultSequences.length == 0 && result.sequences.userDefinedSequences.length == 0) {
                          var msg = "No defined sequences";
                          $('<input>').
                                  attr('type', 'hidden').
                                  attr('name', 'inSeq').
                                  attr('id', 'inSeq').
                                  attr('value', msg).
                                  appendTo('#manage_form');
                      } else {
                          for ( var j = 0; j < result.sequences.defaultSequences.length; j++) {
                              arr.push(result.sequences.defaultSequences[j]);
                          }

                          for ( var k = 0; k < result.sequences.userDefinedSequences.length; k++) {
                              arrUserDefined.push(result.sequences.userDefinedSequences[k]);
                          }

                          for ( var i = 0; i < arr.length; i++) {
                              if(arr[i] == insequence){
                                  $('#inSequenceExistingOptGroup').append('<option value="'+arr[i]+'" selected="selected">'+arr[i]+'</option>');
                              }else{
                                  $('#inSequenceExistingOptGroup').append('<option value="'+arr[i]+'">'+arr[i]+'</option>');
                              }
                              $('<input>').
                                      attr('type', 'hidden').
                                      attr('name', 'inSeq').
                                      attr('id', 'inSeq').
                                      attr('value', arr[i]).
                                      appendTo('#manage_form');

                          }

                          for ( var i = 0; i < arrUserDefined.length; i++) {
                              if(arrUserDefined[i] == insequence){
                                  $('#inSequenceUserAddedOptGroup').append('<option value="'+arrUserDefined[i]+'" selected="selected">'+arrUserDefined[i]+'</option>');
                              }else{
                                  $('#inSequenceUserAddedOptGroup').append('<option value="'+arrUserDefined[i]+'">'+arrUserDefined[i]+'</option>');
                              }
                              $('<input>').
                              attr('type', 'hidden').
                              attr('name', 'inSeq').
                              attr('id', 'inSeq').
                              attr('value', arrUserDefined[i]).
                              appendTo('#manage_form');

                          }
                      }
                      inSequencesLoaded = true;
                      $("#inSequence").selectpicker('refresh');
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
                action : "getCustomCombinedOutSequences" , provider:apiProvider, apiName:apiName, apiVersion:apiVersion
            },
              function(result) {
                  if (!result.error) {
                      var arr = [];
                      var arrUserDefined = [];
                      if (result.sequences.defaultSequences.length == 0 && result.sequences.userDefinedSequences.length == 0) {
                          var msg = i18n.t("No defined sequences");
                          $('<input>').
                                  attr('type', 'hidden').
                                  attr('name', 'outSeq').
                                  attr('id', 'outSeq').
                                  attr('value', msg).
                                  appendTo('#manage_form');
                      }else {
                          for ( var j = 0; j < result.sequences.defaultSequences.length; j++) {
                              arr.push(result.sequences.defaultSequences[j]);
                          }

                          for ( var k = 0; k < result.sequences.userDefinedSequences.length; k++) {
                              arrUserDefined.push(result.sequences.userDefinedSequences[k]);
                          }

                          for ( var i = 0; i < arr.length; i++) {
                              if(arr[i] == outsequence){
                                  $('#outSequenceExistingOptGroup').append('<option value="'+arr[i]+'" selected="selected">'+arr[i]+'</option>');
                              }else{
                                  $('#outSequenceExistingOptGroup').append('<option value="'+arr[i]+'">'+arr[i]+'</option>');
                              }
                              $('<input>').
                              attr('type', 'hidden').
                              attr('name', 'inSeq').
                              attr('id', 'inSeq').
                              attr('value', arr[i]).
                              appendTo('#manage_form');

                          }

                          for ( var i = 0; i < arrUserDefined.length; i++) {
                              if(arrUserDefined[i] == outsequence){
                                  $('#outSequenceUserAddedOptGroup').append('<option value="'+arrUserDefined[i]+'" selected="selected">'+arrUserDefined[i]+'</option>');
                              }else{
                                  $('#outSequenceUserAddedOptGroup').append('<option value="'+arrUserDefined[i]+'">'+arrUserDefined[i]+'</option>');
                              }
                              $('<input>').
                              attr('type', 'hidden').
                              attr('name', 'inSeq').
                              attr('id', 'inSeq').
                              attr('value', arrUserDefined[i]).
                              appendTo('#manage_form');

                          }
                      }
                      outSequencesLoaded = true;
                      $("#outSequence").selectpicker('refresh');
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
                action : "getCustomCombinedFaultSequences" , provider:apiProvider, apiName:apiName, apiVersion:apiVersion
            },
              function(result) {
                  if (!result.error) {
                      var arr = [];
                      var arrUserDefined = [];
                      if (result.sequences.defaultSequences.length == 0 && result.sequences.userDefinedSequences.length == 0) {
                          var msg = i18n.t("No defined sequences");
                          $('<input>').
                                  attr('type', 'hidden').
                                  attr('name', 'faultSeq').
                                  attr('id', 'faultSeq').
                                  attr('value', msg).
                                  appendTo('#manage_form');
                      }else {
                          for ( var j = 0; j < result.sequences.defaultSequences.length; j++) {
                              arr.push(result.sequences.defaultSequences[j]);
                          }

                          for ( var k = 0; k < result.sequences.userDefinedSequences.length; k++) {
                              arrUserDefined.push(result.sequences.userDefinedSequences[k]);
                          }

                          for ( var i = 0; i < arr.length; i++) {
                              if(arr[i] == faultsequence){
                                  $('#faultSequenceExistingOptGroup').append('<option value="'+arr[i]+'" selected="selected">'+arr[i]+'</option>');
                              }else{
                                  $('#faultSequenceExistingOptGroup').append('<option value="'+arr[i]+'">'+arr[i]+'</option>');
                              }
                              $('<input>').
                              attr('type', 'hidden').
                              attr('name', 'inSeq').
                              attr('id', 'inSeq').
                              attr('value', arr[i]).
                              appendTo('#manage_form');

                          }

                          for ( var i = 0; i < arrUserDefined.length; i++) {
                              if(arrUserDefined[i] == faultsequence){
                                  $('#faultSequenceUserAddedOptGroup').append('<option value="'+arrUserDefined[i]+'" selected="selected">'+arrUserDefined[i]+'</option>');
                              }else{
                                  $('#faultSequenceUserAddedOptGroup').append('<option value="'+arrUserDefined[i]+'">'+arrUserDefined[i]+'</option>');
                              }
                              $('<input>').
                              attr('type', 'hidden').
                              attr('name', 'inSeq').
                              attr('id', 'inSeq').
                              attr('value', arrUserDefined[i]).
                              appendTo('#manage_form');

                          }
                      }
                      faultSequencesLoaded = true;
                      $("#faultSequence").selectpicker('refresh');
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
