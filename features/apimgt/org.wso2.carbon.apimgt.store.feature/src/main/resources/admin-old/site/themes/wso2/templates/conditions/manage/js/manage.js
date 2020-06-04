$(function () {

  /***********************************************************
   *  data-tables config
   ***********************************************************/
  $('#manage-tiers').datatables_extended({
    "fnDrawCallback": function (data) {
      if (this.fnSettings().fnRecordsDisplay() <= $("#manage-tiers_length option:selected").val()
        || $("#manage-tiers_length option:selected").val() == -1)
        $('#manage-tiers_paginate').hide();
      else $('#manage-tiers_paginate').show();

      $('.js_conditionValue').each(function () {
        if ((new RegExp('^{.*}$', 'g')).test($(this).text())) {
          var conditionValue = JSON.parse($(this).text());
          if (conditionValue) {
            $(this).text('');
            for (var x in conditionValue) {
              $(this).append($(
                '<div><span style="color:blue">' + x + ' : </span>' +
                '<span>' + conditionValue[x] + '</span></div>'
              ))
            }
          }
        }
      })
    },
    "aoColumns": [
      null,
      null,
      null,
      null,
      { "bSortable": false },
    ]
  });

});
