$(document).ready(function(){
    $( document ).on( "click focused", "button.check_url_valid", function() {
        var btn = this;
        $(btn).prop('disabled', true);
        var url = $(this).parent().parent().find('input:first').val();
        var type = '';
        var attr = $(this).attr('url-type');
        var thisID = $(this).attr('id');
        var providerName = $(this).attr('providerName');
        var apiName = $(this).attr('apiName');
        var apiVersion = $(this).attr('apiVersion');
        if (attr) {
            type = $(btn).attr('url-type');
        } else {
            if (thisID == "prototype_test") {
                type = "http";
            }
            else {
            	if ($("#endpoint_config").val() && $("#endpoint_config").val() != "") {
            		type = $.parseJSON($("#endpoint_config").val())['endpoint_type'];
            	}                
            }
        }
        if (!providerName && !apiName && !apiVersion) {
            providerName = "";
            apiName = "";
            apiVersion = "";
        }

        $(btn).parent().parent().parent().find('.url_validate_label').remove();
        $(btn).addClass("loadingButton-small");
        $(btn).val(i18n.t('Validating..'));

        if (url == '') {
            $(btn).parent().parent().after(' <span class="label label-danger url_validate_label">' +
                '<i class="fw fw-cancel icon-white" title="invalid url"></i> ' +
                ' <span class ="url_validate_message"></span></span>');
            jQuery( '.url_validate_message' ).text( i18n.t('Invalid') + '. ' +  result.response.response);
            var toFade = $(btn).parent().parent().parent().find('.url_validate_label');
            $(btn).removeClass("loadingButton-small");
            $(btn).val(i18n.t('Test URI'));
            var foo = setTimeout(function(){$(toFade).hide()},3000);
            $(btn).prop('disabled', false);
            return;
        }
        if (!type) {
            type = "";
        }
        jagg.post("/site/blocks/item-add/ajax/add.jag", { action:"isURLValid", type:type, url:url, providerName:providerName, apiName:apiName, apiVersion:apiVersion },
                  function (result) {
                      if (!result.error) {
                          if (result.response.response == "success") {
                              $(btn).parent().parent().after(' <span class="label label-success url_validate_label"><i class="fw fw-check icon-white" title="valid url"></i> ' + i18n.t('Valid') + '</span>');

                          } else {
                              if (result.response.isConnectionError) {
                                if (result.response.response == null) {
                                    $(btn).parent().parent().after(' <span class="label label-danger url_validate_label"><i class="fw fw-cancel icon-white" title="error-in-correction"></i> ' + i18n.t('Invalid') + i18n.t(' - Error connecting to backend') + '</span>');
                                } else { //When an exception is thrown from jsFunction_isURLValid
                                    $(btn).parent().parent().after(' <span class="label label-danger url_validate_label"><i class="fw fw-cancel icon-white" title="invalid url"></i> ' + i18n.t('Invalid') + '</span>');
                                }
                              } else {
                                    if (result.response.statusCode == null) { //When an exception is thrown from sendHttpHEADRequest method
                                        $(btn).parent().parent().after(' <span class="label label-danger url_validate_label"><i class="fw fw-cancel icon-white" title="invalid url"></i> ' + i18n.t('Invalid') + '. ' +  result.response.response + '</span>');
                                    } else {
                                        if (result.response.isContainUriTemplatesOnly) {
                                            $(btn).parent().parent().after(' <span class="label label-danger url_validate_label"><i class="fw fw-cancel icon-white" title="missing-complete-url>"></i> ' + i18n.t('Cannot test the endpoint provided. Please specify the full URL for testing.') + '</span>');
                                        } else {
                                            $(btn).parent().parent().after(' <span class="label label-warning url_validate_label"><i class="fw fw-cancel icon-white" title="warning"></i> ' + i18n.t('Warning') + '. ' + result.response.statusCode + ' - ' + result.response.reasonPhrase + '</span>');
                                        }
                                    }
                              }

                          }
                          var toFade = $(btn).parent().parent().find('.url_validate_label');
                          var foo = setTimeout(function() {
                                $(toFade).hide();
                          }, 3000);
                      }else {
                          if (result.message == "timeout") {
                              jagg.showLogin();
                          }
                      }

                      $(btn).removeClass("loadingButton-small");
                      $(btn).val(i18n.t('Test URI'));
                      $(btn).prop('disabled', false);
                  }, "json");

    });
});
