$(document).ready(function() {
    var docUrlDiv=$('#docUrl');
    docUrlDiv.click(function() {
        docUrlDiv.removeClass('error');
        docUrlDiv.next().hide();
    });

    docUrlDiv.change(function() {
        validInputUrl(docUrlDiv);
    });

    $('input[name=optionsRadios1]:radio:checked').change(function() {
        if (getRadioValue($('input[name=optionsRadios1]:radio:checked')) == "inline") {
            $('#docUrl').removeClass('error');
            $('#docUrl').next().hide();
        }
    });

    var docId = $("#docName");
    docId.change(function () {
        var apiName = $("#docAPIName").val();

        //check for illegal characters in doc name
        var illegalChars = /([~!&@#$;%^*+={}\|\\<>\"\',])/;
        var illegalCharsCondition = illegalChars.test(docId.val());
        // @todo: param_string
        validInput(docId, 'Name contains one or more illegal characters  (~ ! & @ # $ ; % ^ & * + = { } | &lt; &gt;, \' " \\ ) .', illegalCharsCondition);

        if(!illegalCharsCondition) {
            //Check the doc name is duplicated
            var errorCondition = isAvailableDoc(apiName + "-" + docId.val());
            validInput(docId, 'Duplicate document name.', errorCondition);
        }

    });

    // docTable column count differs based on permission.
    // As we have a pre-defined structure here, added following condition to
    // change the table structure based on column count
    var docTableColCount = $( "#docTable tr:nth-child(1) th" ).length;
    var docTableColumnStructure = null;
    if (docTableColCount == 3) {
        docTableColumnStructure = [
        null,
        null,
        { "bSortable": false }
        ];
    } else if (docTableColCount == 4){
        docTableColumnStructure = [
        null,
        null,
        null,
        { "bSortable": false }
        ];
    } else if (docTableColCount == 5){
        docTableColumnStructure = [
            null,
            null,
            null,
            null,
            { "bSortable": false }
        ];
    }

    $('#docTable').datatables_extended({
	     "fnDrawCallback": function(){
	       if(this.fnSettings().fnRecordsDisplay()<=$("#docTable_length option:selected" ).val()
	     || $("#docTable option:selected" ).val()==-1)
	       $('#docTable_paginate').hide();
	       else $('#docTable_paginate').show();
	     } ,
        "aoColumns": docTableColumnStructure
	});


});

var newDocFormToggle = function(){
    $('#newDoc').toggle('slow');
    $('#docName').removeAttr("disabled").val('');
    $('#summary').val('');
    $('#docUrl').val('');
    $("#docLocation").val('');
    $('#specifyBox').val('');
    $('#optionsRadios6').attr("checked","checked");
    $('#optionsRadios1').attr("checked","checked");
    $('#sourceUrlDoc').hide();
};

var removeDocumentation = function (provider, apiName, version, docName, docType) {
    $('#messageModal').html($('#confirmation-data').html());
    $('#messageModal h3.modal-title').html(i18n.t('Confirm Deletion'));
    $('#messageModal div.modal-body').html('\n\n' + i18n.t('Are you sure you want to delete the file') + '<b>"' + docName + '</b>"?');
    $('#messageModal a.btn-primary').html(i18n.t('Yes'));
    $('#messageModal a.btn-other').html(i18n.t('No'));
    $('#messageModal a.btn-primary').click(function() {
        jagg.post("/site/blocks/documentation/ajax/docs.jag", { action:"removeDocumentation",provider:provider,
                apiName:apiName, version:version,docName:docName,docType:docType},
            function (result) {
                if (!result.error) {
                    $('#messageModal').modal('hide');
                    $('#' + apiName + '-' + docName.replace(/ /g,'__')).remove();
                    window.location.reload();
                    if ($('#docTable tr').length == 1) {
                        $('#docTable').append($('<tr><td colspan="6">' + i18n.t('No documentation associated with the API') + '</td></tr>'));
                    }
                } else {
                    if (result.message == "AuthenticateError") {
                        jagg.showLogin();
                    } else {
                        jagg.message({content:result.message,type:"error"});
                    }
                }
            }, "json");
    });
    $('#messageModal a.btn-other').click(function() {
        return;
    });
    $('#messageModal').modal();
};

var updateDocumentation = function (rowId, docName, docType, summary, sourceType, docUrl, filePath, otherTypeName,visibility) {
    //$("#docTable").hide('fast');
    $('#newDoc .btn-primary').text('Update');
    $('#newDoc .btn-primary').val('Update');
    $('#addDoc').hide('fast');
    $('#updateDoc h4')[0].innerHTML = "Update Document - " + docName;
    $('#updateDoc').show('fast');
    $('#newDoc').show('fast');
    $('#newDoc #docName').val(docName);
    $('#newDoc #docName').attr('disabled', 'disabled');
    $("#docVisibility").val(visibility);
    if (summary != "{}" && summary != 'null') {
        $('#newDoc #summary').val(summary);
    }
    if(docType == "Swagger API Definition"){
        $('#newDoc #summary').attr("disabled",true);
        $('#optionsRadios5').attr('checked', true);
        $('#optionsRadios1').attr('disabled', true);
        $('#optionsRadios2').attr('disabled', true);
        $('#optionsRadios3').attr('disabled', true);
        $('#optionsRadios4').attr('disabled', true);
        $('#optionsRadios5').attr('disabled', true);
        $('#optionsRadios6').attr('disabled', true);
        $('#optionsRadios7').attr('disabled', true);
        $('#optionsRadios8').attr('disabled', true);


        $('#optionsRadios6').attr('checked', true);
    }

    else{
        if (docType == "Public Forum" || docType == "Support Forum") {
            disableInline("forum");
        }
        if (sourceType == "INLINE") {
            $('#optionsRadios6').attr('checked', true);
            $('#sourceUrlDoc').hide('slow');
            $('#docUrl').val('');
        } else if(sourceType == "URL"){
            if (docUrl != "{}") {
                $('#newDoc #docUrl').val(docUrl);
                $('#optionsRadios7').attr('checked', true);
                $('#sourceUrlDoc').show('slow');
            }
        }else {
            $('#optionsRadios8').attr('checked', true);
            $('#toggleFileDoc').show('slow');
            if(filePath){
                $('#fileNameDiv').text(filePath.split("documentation/files/")[1]);
                $('#fileNameDiv').show('slow');
            }

        }

        for (var i = 1; i <= 5; i++) {
            if ($('#optionsRadios' + i).val().toUpperCase().indexOf(docType.toUpperCase()) >= 0) {
                $('#optionsRadios' + i).attr('checked', true);
                if(docType.toLowerCase() == 'other'){
                    $('#specifyBox').val(otherTypeName);
                    $('#otherTypeDiv').show();
                }
            }
        }
    }
};

var editInlineContent = function (provider, apiName, version, docName, mode,tenantDomain) {
    var current = window.location.pathname;
    if (current.indexOf("item-info.jag") >= 0) {
        window.open("inline-editor.jag?docName=" + docName + "&apiName=" + apiName + "&version=" + version + "&provider=" + provider + "&mode=" + mode+tenantDomain);
    } else {
        window.open("site/pages/inline-editor.jag?docName=" + docName + "&apiName=" + apiName + "&version=" + version + "&provider=" + provider + "&mode=" + mode+tenantDomain);
    }

};

var clearDocs = function () {
	$('#newDoc').hide();
    $('#addDoc').show('fast');
    $('#updateDoc').hide();
    $('#newDoc .btn-primary').text(i18n.t('Add Document'));
    $('#newDoc .btn-primary').val('');
};

var submitDoc = function() {
    $('#addNewDoc').ajaxSubmit({
        success:function (result) {
            if (!result.error) {
                $.cookie("tab", "docsLink");
                window.location.reload();
            } else {
                if (result.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content:result.message,type:"error"});
                }
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            jagg.message({content:(JSON.parse(jqXHR.responseText)).message,type:"error"});
        }
    });
}

var saveDoc=function(){
    var sourceType = getRadioValue($('input[name=optionsRadios1]:radio:checked'));
    var docId = $("#docName");
    var summaryDiv = $("#summary");
    var docUrlDiv = $("#docUrl");
    var fileDiv = $("#docLocation");
    var apiName = $("#docAPIName").val();
    var errCondition = docUrlDiv.val() == "";
    var isFilePathEmpty = fileDiv.val() == "";
    var isSummaryEmpty = summaryDiv.val().trim() == "";
    var isOtherTypeNameEmpty = $('#specifyBox').val() == null || $('#specifyBox').val() == '';
    var docType = getRadioValue($('input[name=optionsRadios]:radio:checked'));
    var docVisibility=$("#docVisibility option:selected").val();
    var docName = $("#docName").val();
    var errorCondition = false;
    var illegalChars = /([~!&@#$;%^*+={}\|\\<>\"\',])/;
    var illegalCharsCondition = illegalChars.test(docId.val());

    // @todo: param_string
    if (!validInput(docId, 'Name contains one or more illegal characters  (~ ! & @ # $ ; % ^ & * + = { } | &lt; &gt;, \' " \\ ) .', illegalCharsCondition)) {
        return;
    }
    if($('#saveDocBtn').val() != "Update"){
        errorCondition = isAvailableDoc(apiName + "-" + docId.val());
    }
    if (apiName && !validInput(docId, i18n.t('Duplicate document name.'), errorCondition)) {
        return;
    } else if (!validInput(summaryDiv, i18n.t('This field is required.'), isSummaryEmpty)) {
        return;
    } else if (sourceType == 'url' && !validInput(docUrlDiv, i18n.t('This field is required.'), errCondition)) {
        return;
    } else if (sourceType == 'url' && !validInputUrl(docUrlDiv)) {
        return;
    }else if(sourceType == 'file' && !validInput(fileDiv, i18n.t('This field is required.'), isFilePathEmpty) && $('#saveDocBtn').val() != "Update") {
        return;
    }else if(docType.toLowerCase() == 'other' && !validInput($('#specifyBox'), i18n.t('This field is required.'), isOtherTypeNameEmpty) && docName!="Swagger API Definition"){
        return;
    }

    if($('#saveDocBtn').val() == "Update" && $("#docLocation").val() == ""){
        $("#docLocation").removeClass('required');
    }

    $("#addNewDoc").validate();
    if ($("#addNewDoc").valid()) {
        var version = $("#docAPIVersion").val();
        var provider = $("#spanProvider").text();

        var summary = $("#summary").val();

        var docUrl = docUrlDiv.val();
        if (docUrl.indexOf("http") == -1) {
            docUrl = "http://" + docUrl;
        }

        if (sourceType == 'file') {

            var fileExtension = getExtension($("#fileName").val());

            var mimeType = getMimeType(fileExtension);

            $('<input>').attr('type', 'hidden')
                .attr('name', 'mimeType').attr('value', mimeType).prependTo('#addNewDoc');
        }


        var mode = $('#newDoc .btn-primary').val();
        $('<input>').attr('type', 'hidden')
            .attr('name', 'provider').attr('value', provider).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'action').attr('value', "addDocumentation").prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'apiName').attr('value', apiName).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'version').attr('value', version).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'docName').attr('value', docName).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'docType').attr('value', docType).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'summary').attr('value', summary).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'sourceType').attr('value', sourceType).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'docUrl').attr('value', docUrl).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'mode').attr('value', mode).prependTo('#addNewDoc');
        if(docVisibility){
            $('<input>').attr('type', 'hidden')
                .attr('name', 'docVisibility').attr('value', docVisibility).prependTo('#addNewDoc');
        }
        if(docType.toLowerCase()=='other'){
            $('<input>').attr('type', 'hidden')
                .attr('name', 'newType').attr('value', $('#specifyBox').val()).prependTo('#addNewDoc');
        }
        if($('#saveDocBtn').val() == "Update"){
            var fileNameToBeUpload = $("#docName").val();
            if(getFileName($("#docLocation").val()) == $("#fileNameDiv").html()){
                jagg.message({
                    // @todo: param_string
                    content:"There is a file named "+fileNameToBeUpload+" with the same name. You are going to" +
                    " overwrite existing file. Are you sure you want to overwrite existing file" ,
                    type:"confirm",
                    title: i18n.t("File already exists..!"),
                    anotherDialog:true,
                    okCallback:function(){
                        submitDoc();
                    }
                });
                return false;
            }
        }
        submitDoc();
    }
};

var getRadioValue = function (radioButton) {
    if (radioButton.length > 0) {
        return radioButton.val();
    }
    else {
        return 0;
    }
};

var disableInline = function(type) {
    if (type == 'forum') {
        document.getElementById("optionsRadios6").disabled = true;
        document.getElementById("optionsRadios7").checked = true;
        $('#sourceUrlDoc').show('slow');
        $('#sourceFile').hide('slow');
    } else {
        document.getElementById("optionsRadios6").disabled = false;
        document.getElementById("optionsRadios6").checked = true;
        $('#sourceUrlDoc').hide('slow');
        $('#sourceFile').hide('slow');
    }
};

var isAvailableDoc = function(id) {
    var docEntry = $("#docTable #" + id).text();
    if (docEntry != "") {
        return true;
    }
};

var validInput = function(divId, message, condition) {
    if (condition) {
        divId.addClass('error');
        if (!divId.next().hasClass('error')) {
            divId.parent().append('<label class="error" id="error_label">' + message + '</label>');
            setTimeout(function() {
                $("#error_label").fadeOut("fast", function() {
                    $(this).remove();
                });
            }, 1000);
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

var validInputUrl = function(docUrlDiv) {
    if (docUrlDiv) {
        var docUrlD;
        if (docUrlDiv.val().indexOf("http") == -1) {
            docUrlD = "http://" + docUrlDiv.val();
        } else {
            docUrlD = docUrlDiv.val();
        }
        var erCondition = validUrl(docUrlD);
        return validInput(docUrlDiv, i18n.t('Invalid document URL format.'), erCondition);
    }
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

var getFileName = function(baseFileName) {
    var baseNameComponents = baseFileName.split('\\');
    if(baseNameComponents.length > 1) {
        var fileName = baseNameComponents[baseNameComponents.length - 1];
        return fileName;
    } else {
        return null;
    }
}

var getMimeType = function(extension) {
    var type=CONTENT_MAP[extension];
    if(!type){type="application/octet-stream";}
    return type;
};




