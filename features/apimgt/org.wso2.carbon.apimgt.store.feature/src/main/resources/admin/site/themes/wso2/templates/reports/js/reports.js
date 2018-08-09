$(document).ready(
   function() {
       $('#downloadPDFForm').validate({
           submitHandler: function(form) {
                var month = $("#datepickerVal").val();
                window.location.href = '/admin/site/themes/wso2/templates/reports/download.jag?month=' + month;
           }
       });

    chart = "";
    chartData = "";
    var d = new Date();
    var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(),d.getHours(),d.getMinutes());

    $('#date-range').daterangepicker({
        singleDatePicker: true,
        timePicker: false,
        timePickerIncrement: 30,
        format: 'YYYY-MM-DD HH:mm:ss',
        startDate: moment().subtract(1, 'month'),
        endDate: moment().add(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
        opens: 'right'

    }).on('hide.daterangepicker', function (ev, picker) {
      $('.table-condensed tbody tr:nth-child(2) td').click();
      var option = picker.startDate.format('YYYY-MM');
      $('#datepickerVal').val(option);
    });

   }
);
