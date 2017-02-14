$(function () {
    var client = new API();
    var api_id = $('input[name="apiId"]').val(); // Constant(immutable) over all the tabs since parsing as event data to event handlers
    client.get(api_id).then(loadOverview);
    /* TODO: Need to handle error path ~tmkb*/
    $('#bodyWrapper').on('click', 'button', function (e) {
        var elementName = $(this).attr('data-name');
        if (elementName == "editApiButton") {
            $('#apiOverviewForm').toggleClass('edit').toggleClass('view');
        }
    });
    loadFromHash();
    $('#tab-7').bind('show.bs.tab', mediationTabHandler);
    $('#tab-2').bind('show.bs.tab', {api_client: client, api_id: api_id}, lifecycleTabHandler);
    $(document).on('click', ".lc-state-btn", {api_client: client, api_id: api_id}, updateLifecycleHandler);
});

function loadOverview(jsonData) {
    //Manipulating data for the UI
    var context = jsonData.obj;
    if (context.endpointConfig) {
        var endpointConfig = $.parseJSON(context.endpointConfig);
        context.productionEndpoint = endpointConfig.production_endpoints.url;
    }
    // Grab the template script TODO: Replace with UUF client
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

/**
 * Event handler for API Lifecycle detail tab onclick event;Get the current API lifecycle tab HTML via UUFClient and display.
 * @param event {object} Click event of the LC state tab
 */
function lifecycleTabHandler(event) {
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;

    function renderLCTab(response) {
        var mode = "OVERWRITE"; // Available modes [OVERWRITE,APPEND, PREPEND]
        var api_data = JSON.parse(response.data);
        var callbacks = {
            onSuccess: function (data) {
            }, onFailure: function (data) {
            }
        };
        var data = {
            lifeCycleStatus: api_data.lifeCycleStatus,
            isPublished: api_data.lifeCycleStatus.toLowerCase() === "published",
        };
        UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-lifecycle", data, "api-tab-lc-content", mode, callbacks);
    }

    api_client.get(api_id).then(renderLCTab);
}

function mediationTabHandler(event) {

}

/**
 * Do the life cycle update when user clicks on the relevant life cycle state button.
 * @param event {object} click event of the lc state button
 */
function updateLifecycleHandler(event) {
    var api_client = event.data.api_client;
    var new_state = $(this).data("lcstate");
    var api_id = event.data.api_id;
    var promised_update = api_client.updateLcState(api_id, new_state);
    var event_data = {
        data: {
            api_client: api_client,
            api_id: api_id
        }
    };
    promised_update.then(
        (response, event = event_data) => {
            let message_element = $("#general-alerts").find(".alert-success");
            message_element.find(".alert-message").html("Life cycle state updated successfully!");
            message_element.fadeIn("slow");
            lifecycleTabHandler(event);
        }
    ).catch(
        (response, event = event_data) => {
            let message_element = $("#general-alerts").find(".alert-danger");
            message_element.find(".alert-message").html(response.statusText);
            message_element.fadeIn("slow");
            lifecycleTabHandler(event);
        }
    );
}