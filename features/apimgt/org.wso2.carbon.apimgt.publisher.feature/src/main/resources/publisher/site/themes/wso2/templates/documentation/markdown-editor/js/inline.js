var converter = null;

$(document).ready(function() {
});

$(document).unbind('keypress');

function loadDefaultMarkdownContent(provider,apiName, version, docName) {

    mdEditor();
    jagg.post("/site/blocks/documentation/ajax/docs.jag", {
            action: "getInlineContent",
            provider: provider,
            apiName: apiName,
            version: version,
            docName: docName
        },
        function (json) {
            if (!json.error) {
                var docName = json.doc.provider.docName;
                var docContent = json.doc.provider.content;
                $('#apiDeatils').empty().html('<p><h1> ' + docName + '</h1></p>');
                if (docContent != null) {
                    $('#editor').keyup(function () {
                        var content = $('#editor').val();
                        var startPattern = /<script.*?>/g;
                        var endPattern = /<\/script>/g;
                        content = content.replace(startPattern, "&lt;script&gt;").replace(endPattern, "&lt;\/script&gt;");
                        showdown.setOption('strikethrough', true);
                        showdown.setOption('tables', true);
                        showdown.setOption('smoothLivePreview', true);
                        showdown.setOption('emoji', true);
                        showdown.setOption('openLinksInNewWindow', true);
                        showdown.setOption('simpleLineBreaks', true);
                        converter = converter ? converter : new showdown.Converter();
                        var html = converter.makeHtml(content);
                        $('#preview').empty().append(html);
                    });
                    var startPattern = /<script.*?>/g;
                    var endPattern = /<\/script>/g;
                    docContent = docContent.replace(startPattern, "&lt;script&gt;").replace(endPattern, "&lt;\/script&gt;");

                    showdown.setOption('strikethrough', true);
                    showdown.setOption('tables', true);
                    showdown.setOption('smoothLivePreview', true);
                    showdown.setOption('emoji', true);
                    showdown.setOption('openLinksInNewWindow', true);
                    showdown.setOption('simpleLineBreaks', true);

                    converter = converter ? converter : new showdown.Converter();
                    var html = converter.makeHtml(docContent);
                    $('#editor').val(docContent);
                    $('#preview').empty().append(html);
                }
            } else {
                $('#inlineError').show('fast');
                $('#inlineSpan').html('<strong>' + i18n.t('The contents of this document cannot be loaded.') + '</strong><br />' + result.message);
            }
        }, "json");
}

function saveContent(provider, apiName, apiVersion, docName, mode) {

    var contentDoc =  $('#editor').val();
    //remove extra white spaces
    contentDoc = contentDoc.replace(/(^\s*)|(\s*$)/gi,"");
    contentDoc = contentDoc.replace(/[ ]{2,}/gi," ");

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
                    localStorage.removeItem("doc_auto_save"+apiName+provider+apiVersion+docName+"draft");
                    localStorage.removeItem("doc_auto_time"+apiName+provider+apiVersion+docName+"draft");
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
