$(function () {

    //Initializing the designer
    var designer = new APIMangerAPI.APIDesigner();
    designer.set_partials('implement');

    var swaggerUrl = caramel.context + "/asts/api/apis/swagger?action=swaggerDoc&provider=" + store.publisher.api.provider + "&name=" + store.publisher.api.name + "&version=" + store.publisher.api.version;

    $(document).ready(function () {
        $.ajaxSetup({
                        contentType: "application/x-www-form-urlencoded; charset=utf-8"
                    });

        $.get(swaggerUrl, function (data) {
            designer.load_api_document(data.data);
            designer.set_default_management_values();
            designer.render_resources();
            $("#swaggerUpload").modal('hide');
        });

        function doGatewayAction() {
            var type = $("#retryType").val();
            if (type == "manage") {
                $("#environmentsRetry-modal").modal('hide');
                $("body").trigger("api_saved");
                location.href = "";//TODO
            } else {
                location.href = "";//TODO
            }
        }

        //hack to validate tiers
        function validate_tiers() {
            var selectedValues = $('#tier').val();
            if (selectedValues && selectedValues.length > 0) {
                $("button.multiselect").removeClass('error-multiselect');
                $("#tier_error").remove();
                return true;
            }
            //set error
            $("button.multiselect").addClass('error-multiselect').after('<label id="tier_error" class="error" for="tenants" generated="true" style="display: block;">This field is required.</label>').focus();
            return false;
        }
    });
});
