$.file_input();

/**
 * Execute once the page load is done.
 */
$(function() {
    var client = new API();
    /* Re-use same api client in all the tab show events */
    var api_id = $('input[name="apiId"]').val(); // Constant(immutable) over
    // all the tabs since
    // parsing as event data to
    // event handlers

    $(document).on('click', ".doc-listing-delete", {
        api_client: client,
        api_id: api_id
    }, deleteDocHandler);
    $(document).on('click', ".doc-listing-update", {
        api_client: client,
        api_id: api_id
    }, getAPIDocumentByDocId);
    $(document).on('click', "#add-doc-submit", {
        api_client: client,
        api_id: api_id
    }, createDocHandler);
    $(document).on('click', "#update-doc-submit", {
        api_client: client,
        api_id: api_id
    }, updateAPIDocument);
    $(document).on('click', ".doc-content-View", {
        api_client: client,
        api_id: api_id
    }, viewDocContentHandler);
    $(document).on('click', "#add-new-doc", {}, toggleDocAdder);
});

function _renderActionButtons(data, type, row) {
    if (type === "display") {
        var icon = $("<i>").addClass("fw");
        var icon_circle = $("<i>").addClass("fw fw-circle-outline fw-stack-2x");
        var icon_edit_span = $("<span>").addClass("fw-stack")
            .append(icon.addClass("fw-edit fw-stack-1x"))
            .append(icon_circle);
        var icon_delete_span = $("<span>").addClass("fw-stack")
            .append(icon.clone()
                .removeClass("fw-edit").addClass("fw-delete fw-stack-1x"))
            .append(icon_circle.clone());
            var cssEdit = "cu-reg-btn btn-edit text-warning doc-listing-update btn-sm";
            if(!hasValidScopes("/apis/{apiId}/documents/{documentId}", "put")) {
                cssEdit = "cu-reg-btn btn-edit text-warning doc-listing-update btn-sm not-active";
            }
	    var icon_view_span = $("<span>").addClass("fw-stack")
            .append(icon.clone()
            .removeClass("fw-delete").addClass("fw-view fw-stack-1x"))
            .append(icon_circle.clone());
	    var edit_button = $('<a>', {
                id: data.id,
                href: data.id
            })
            .text('Edit ')
            .addClass(cssEdit);
        edit_button = edit_button.prepend(icon_edit_span);
            var cssDelete = "cu-reg-btn btn-delete text-danger doc-listing-delete btn-sm";
            if(!hasValidScopes("/apis/{apiId}/documents/{documentId}", "delete")) {
                cssDelete = "cu-reg-btn btn-delete text-danger doc-listing-delete btn-sm not-active";
            }
        var delete_button = $('<a>', {
                id: data.id
            })
            .text('Delete ')
            .addClass(cssDelete);
        // .append(icon.clone().removeClass("fw-edit").addClass("fw-delete"));
        delete_button = delete_button.prepend(icon_delete_span);

        var href = "#"
        var target = ""

        if (data.sourceType == "URL") {
            href = data.sourceUrl;
            target = "_blank";
        } else if (data.sourceType == "INLINE") {
        	var api_id = $('input[name="apiId"]').val();
        	href = contextPath + "/apis/" + api_id + "/documents/" + data.documentId + "/docInlineEditor";
        	target = "_blank";
        }

        var view_button = $('<a>', {
                id: data.id,
                href: href,
                target: target
            })
            .text('View ')
            .addClass("cu-reg-btn btn-view text-danger doc-content-View btn-sm");
        // .append(icon.clone().removeClass("fw-edit").addClass("fw-delete"));
        view_button = view_button.prepend(icon_view_span);

        return $('<div></div>').append(edit_button).append(view_button)
            .append(delete_button).html();

    } else {
        return data;
    }
}

function initDataTable(raw_data) {
    $('#doc-table').DataTable({
        ajax: function(data, callback, settings) {
            callback(raw_data);
        },
        columns: [{
                'data': 'documentId'
            },
            {
                'data': 'name'
            },
            {
                'data': 'sourceType'
            },
            {
                'data': null
            }
            // TODO add modified date column once the service implementation is
            // completed.
        ],
        columnDefs: [{
                "targets": [0],
                "visible": false,
                "searchable": false
            },
            {
                targets: ["doc-listing-action"], // class name will
                // be matched on the
                // TH for the column
                searchable: false,
                sortable: false,
                render: _renderActionButtons // Method to render the
                // action buttons per
                // row
            }
        ]
    })
}

/**
 * Jquery event handler on click event for api create submit button
 * 
 * @param event
 */
function createDocHandler(event) {
    var api_id = event.data.api_id;
    var api_client = event.data.api_client;
    var api_documents_data = {
        documentId: "",
        name: $('#docName').val(),
        type: "HOWTO",
        summary: $('#summary').val(),
        sourceType: $('input[name=optionsRadios1]:checked').val(),
        sourceUrl: $('#docUrl').val(),
        inlineContent: "string",
        otherTypeName: $('#specifyBox').val(),
        permission: '[{"groupId" : "1000", "permission" : ["READ","UPDATE"]},{"groupId" : "1001", "permission" : ["READ","UPDATE"]}]',
        visibility: "API_LEVEL"
    };
    var promised_add = api_client.addDocument(api_id, api_documents_data);

    promised_add.catch(function(error) {
        var error_data = JSON.parse(error_response.data);
        var message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
        noty({
            text: message,
            type: 'error',
            dismissQueue: true,
            modal: true,
            closeWith: ['click', 'backdrop'],
            progressBar: true,
            timeout: 5000,
            layout: 'top',
            theme: 'relax',
            maxVisible: 10
        });
        $('[data-toggle="loading"]').loading('hide');
        console.debug(error_response);
    }).then(function(done) {
        var dt_data = done.obj;
        var documentId = dt_data.documentId;
        var name = dt_data.name;
        var sourceType = dt_data.sourceType;
        var docId = dt_data.documentId;

        if (sourceType == "FILE") {
            var file_input = $('#doc-file');
            var file = file_input[0].files[0];
            var promised_add_file = api_client.addFileToDocument(api_id, docId, file);
            promised_add_file.catch(function(error) {}).then(function(done) {
                var addedFile = done;
            });
        }
        
        $('.doc-content').show();
        $('#no-docs-div').hide();
        $('#newDoc').fadeOut();

        var data_table = $('#doc-table').DataTable();
        data_table.row.add({
            documentId,
            name,
            sourceType,
            _renderActionButtons
        }).draw();
    });
}


function deleteDocHandler(event) {
    var data_table = $('#doc-table').DataTable();
    var current_row = data_table.row($(this).parents('tr'));
    var documentId = current_row.data().documentId;
    var doc_name = current_row.data().name;
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;
    noty({
        text: 'Do you want to delete <span class="text-info">' + doc_name + '</span> ?',
        type: 'alert',
        dismissQueue: true,
        layout: "topCenter",
        modal: true,
        theme: 'relax',
        buttons: [{
                addClass: 'btn btn-danger',
                text: 'Ok',
                onClick: function($noty) {
                    $noty.close();
                    let promised_delete = api_client.deleteDocument(api_id, documentId);
                    promised_delete.then(
                        function(response) {
                            if (!response) {
                                return;
                            }
                            current_row.remove();
                            data_table.draw();
                        }
                    );
                }
            },
            {
                addClass: 'btn btn-info',
                text: 'Cancel',
                onClick: function($noty) {
                    $noty.close();
                }
            }
        ]
    });
}

function viewDocContentHandler(event) {
    var data_table = $('#doc-table').DataTable();
    var current_row = data_table.row($(this).parents('tr'));

    var documentId = current_row.data().documentId;
    $('#docId').val(documentId);
    var doc_name = current_row.data().name;

    var api_client = event.data.api_client;
    var api_id = event.data.api_id;
    var sourceType = current_row.data().sourceType;

    if (sourceType == 'FILE') {
        let promised_get_content = api_client.getFileForDocument(api_id, documentId);
        promised_get_content.catch(function(error) {
            var error_data = JSON.parse(error.data);
        }).then(function(done) {
            downloadFile(done);
        });
    }
}

function downloadFile(response) {
    var fileName = "";
    var contentDisposition = response.headers["content-disposition"];

    if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
        var fileNameReg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        var matches = fileNameReg.exec(contentDisposition);
        if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
    }
    var contentType = response.headers["content-type"];
    var blob = new Blob([response.data], {
        type: contentType
    });
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
        window.navigator.msSaveBlob(blob, fileName);
    } else {
        var URL = window.URL || window.webkitURL;
        var downloadUrl = URL.createObjectURL(blob);

        if (fileName) {
            var aTag = document.createElement("a");
            if (typeof aTag.download === 'undefined') {
                window.location = downloadUrl;
            } else {
                aTag.href = downloadUrl;
                aTag.download = fileName;
                document.body.appendChild(aTag);
                aTag.click();
            }
        } else {
            window.location = downloadUrl;
        }

        setTimeout(function() {
            URL.revokeObjectURL(downloadUrl);
        }, 100);
    }
}


function getAPIDocumentByDocId(event) {
    var data_table = $('#doc-table').DataTable();
    var current_row = data_table.row($(this).parents('tr'));
    $('#rowId').val(current_row.index());
    var documentId = current_row.data().documentId;
    $('#docId').val(documentId);
    var doc_name = current_row.data().name;
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;

    let promised_update = api_client.getDocument(api_id, documentId);
    promised_update.then(
        function(response) {
            if (!response) {
                return;
            }

            loadDocumentDataToForm(response);
        }
    );
}



function updateAPIDocument(event) {
    var api_id = event.data.api_id;
    var api_client = event.data.api_client;
    var documentId = $('#docId').val();
    var update_documents_data = {
        documentId: $('#docId').val(),
        name: $('#docName').val(),
        type: "HOWTO",
        summary: $('#summary').val(),
        sourceType: $('input[name=optionsRadios1]:checked').val(),
        sourceUrl: $('#docUrl').val(),
        inlineContent: "",
        otherTypeName: $('#specifyBox').val(),
        permission: '[{"groupId" : "1000", "permission" : ["READ","UPDATE"]},{"groupId" : "1001", "permission" : ["READ","UPDATE"]}]',
        visibility: "API_LEVEL"
    };
    var promised_update = api_client.updateDocument(api_id, documentId, update_documents_data);


    promised_update.catch(function(error) {
        var error_data = JSON.parse(error_response.data);
        var message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
        noty({
            text: message,
            type: 'error',
            dismissQueue: true,
            modal: true,
            closeWith: ['click', 'backdrop'],
            progressBar: true,
            timeout: 5000,
            layout: 'top',
            theme: 'relax',
            maxVisible: 10
        });
        $('[data-toggle="loading"]').loading('hide');
        console.debug(error_response);
    }).then(function(done) {
        var dt_data = done.obj;
        var name = dt_data.name;
        var type = dt_data.type;
        var docId = dt_data.documentId;

        if (dt_data.sourceType == "FILE") {
            var file_input = $('#doc-file');
            var file = file_input[0].files[0];
            var promised_add_file = api_client.addFileToDocument(api_id, docId, file);
            promised_add_file.catch(function(error) {}).then(function(done) {
                var addedFile = done;
            });
        }

        var row_id = $('#rowId').val();
        var data_table = $('#doc-table').DataTable();
        data_table.row(row_id, 0).data(dt_data).draw();
        $('#newDoc').fadeOut();
        $('#doc-header').show();
        $('#updateDoc').hide();
    });
}

function loadDocumentDataToForm(response) {
    $('#add-doc-submit').hide();
    $('#newDoc').fadeIn();
    $('#doc-header').hide();
    $('#updateDoc').show();
    $('#updateDoc').html("<h4> Update Document - " + response.obj.name + "</h4>");
    $('#docName').val(response.obj.name);
    $('#summary').val(response.obj.summary);
    $("input[value='" + response.obj.sourceType + "']").prop("checked", true);
    if (response.obj.sourceType == "URL") {
        $('#sourceUrlDoc').show("slow");
        $('#docUrl').val(response.obj.sourceUrl);
        $('#fileDiv').fadeOut("slow");
        $('#fileNameDiv').fadeOut("slow");
        $('#toggleFileDoc').fadeOut('slow');
    } else if (response.obj.sourceType == "FILE") {
        $('#fileNameDiv').show();
        $('#toggleFileDoc').show('slow');
        $('#fileNameDiv').text(response.obj.fileName);
        $('#sourceUrlDoc').fadeOut("slow");
    } else {
        $('#sourceUrlDoc').fadeOut("slow");
        $('#fileDiv').fadeOut("slow");
        $('#fileNameDiv').fadeOut("slow");
        $('#toggleFileDoc').fadeOut('slow');
    }
    $('#doc-file').val('');
    $('#fileDiv').fadeOut("slow");
    $('#update-doc-submit').fadeIn("slow");
    $('#docName').disabled = true;
}

function cancelDocForm() {
    $('#newDoc').fadeOut('slow');
    $('#doc-header').show();
    $('#updateDoc').hide();
}


function toggleDocAdder() {
    $('#newDoc').toggle();
    $('#docName').val('');
    $('#summary').val('');
    $('#docUrl').val('');
    $('#doc-file-text').val('');
    $('#fileNameDiv').text('');
    $('#optionsRadios1').prop("checked", true);
    $('#toggleFileDoc').hide();
}