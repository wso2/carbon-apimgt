$(document).ready(function() {
    $('#apiSearch').keydown(function(event) {
        if (event.which == 13) {
            event.preventDefault();
            $("#searchAPI")[0].click();
        }
    });

    $('a.help_popup').popover({
        html : true,
        container: 'body',
        content: function() {
          var msg = $('#'+$(this).attr('help_data')).html();
          return msg;
        },
        template: '<div class="popover default-popover" role="tooltip"><div class="arrow"></div><div class="popover-content"></div></div>'
    });
});