$(document).ready(
    function() {
        var date = new Date();
        var currentYear = date.getFullYear();
        var currentMonth = date.getMonth(); // This is the index. getMonth() starts with 0th index

        var yearSel = $('#year-selector');
        var monthSel = $('#month-selector');
        yearSel.val(currentYear);
        yearSel.attr('max', currentYear);
        monthSel.prop("selectedIndex", currentMonth);

        $('#downloadPDFForm').validate({
            submitHandler: function(form) {
                var monthOfYear = yearSel.val() + '-' + monthSel.val();
                form.action = '/admin/site/themes/wso2/templates/reports/download.jag?month=' + monthOfYear;
                form.submit();
            }
        });
    }
);
