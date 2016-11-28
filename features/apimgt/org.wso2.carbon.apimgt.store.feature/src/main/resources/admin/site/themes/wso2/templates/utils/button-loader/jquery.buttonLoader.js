/*A jQuery plugin which add loading indicators into buttons*/

(function ($) {
    $('.has-spinner').attr("disabled", false);
    $.fn.buttonLoader = function (action) {
        var self = $(this);
        if (action == 'start') {
            if ($(self).attr("disabled") == "disabled") {
                return false;
            }
            $('.has-spinner').attr("disabled", true);
            $(self).html('<span class="spinner"><i class="fw fw-loader5 fw-2x" title="button-loader"></i></span>Saving');
            $(self).addClass('active');
        }
        if (action == 'stop') {
            $(self).removeClass('active');
            $('.has-spinner').attr("disabled", false);
            $(self).html('<span class="icon fw-stack"><i class="fw fw-save fw-stack-1x"></i><i class="fw fw-circle-outline fw-stack-2x"></i></span> &nbsp;Save');
        }
    }
})(jQuery);
