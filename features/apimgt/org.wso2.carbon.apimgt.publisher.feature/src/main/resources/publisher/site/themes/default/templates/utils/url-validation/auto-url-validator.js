//This validates wsdl endpoints automatically and adds error class and display a text error
//add the css class is_url_valid to the input

$(document).ready(function () {
    $("body").delegate("input.is_url_valid", "keyup", function () {
        var btn = this;
        var url = $(this).val();
        var type = '';
        var attr = $(this).attr('url-type');
        if (typeof attr !== typeof undefined && attr !== false) {
            type = $(btn).attr('url-type');
        } else {
            type = "";
        }

        if (url == '') {
            $(this).addClass('error');
            $('.wsdlError').show();
            return;
        }
        jagg.post("/site/blocks/item-add/ajax/add.jag", { action: "isURLValid", type: type, url: url },
            function (result) {
                if (!result.error) {
                    if (result.response == "success") {
                        $('.is_url_valid').removeClass('error');
                        $('.wsdlError').hide();
                    } else {
                        $('.is_url_valid').addClass('error');
                        $('.wsdlError').show();
                    }
                }
            }, "json");
    });
});
