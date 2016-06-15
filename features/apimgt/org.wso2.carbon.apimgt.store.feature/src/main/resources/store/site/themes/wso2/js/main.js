$(document).ready(function(){

    $('a[rel="popover"]').popover({
        container: 'body',
        html: true,
        trigger:'hover',
        content: function () {
            var clone = $($(this).data('popover-content')).clone(true).removeClass('hide');
            return clone;
        }
    }).click(function(e) {
        e.preventDefault();
    });


    $('select').selectpicker();


    $('.rating-tooltip-manual').rating({
      extendSymbol: function () {
        var title;
        $(this).tooltip({
          container: 'body',
          placement: 'bottom',
          trigger: 'manual',
          title: function () {
            return title;
          }
        });
        $(this).on('rating.rateenter', function (e, rate) {
          title = rate;
          $(this).tooltip('show');
        })
        .on('rating.rateleave', function () {
          $(this).tooltip('hide');
        });
      }
    });

});