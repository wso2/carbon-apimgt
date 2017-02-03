$(function () {
    var client = new API();
    var apiId = $('input[name="apiId"]').val();
    client.get(apiId, loadOverview);
    $('#bodyWrapper').on('click', 'button', function (e) {
        var elementName = $(this).attr('data-name');
        if (elementName == "editApiButton") {
            $('#apiOverviewForm').toggleClass('edit').toggleClass('view');
        }
    });
    loadFromHash();

});

function loadOverview(jsonData) {
    //Manipulating data for the UI
    var context = jsonData.obj;
    if (context.endpointConfig) {
        var endpointConfig = $.parseJSON(context.endpointConfig);
        context.productionEndpoint = endpointConfig.production_endpoints.url;
    }
    // Grab the template script
    $.get('/editor/public/components/root/base/templates/api/{id}Overview.hbs', function (templateData) {
        var template = Handlebars.compile(templateData);
        // Pass our data to the template
        var theCompiledHtml = template(context);
        // Add the compiled html to the page
        $('#overview-content').html(theCompiledHtml);
    }, 'html');
}

/**
 * Append page name (with hash i.e: #resources-tab )to URL
 * @param event {object} Browser click event.
 */
function addHashToURL(event) {
    window.location.hash = event.target.hash;
    window.scrollTo(0, 0);
}

/**
 * Javascript to enable link to tab. Change hash for page-reload
 */
function loadFromHash() {
    var hash = document.location.hash;
    if (hash) {
        $('a[href="#' + hash.substr(1) + '"]').tab('show');
        window.scrollTo(0, 0);
    }
    // Register tab click event to append hashed page name to current URL
    $('a[role="tab"]').on('shown.bs.tab', addHashToURL);
}