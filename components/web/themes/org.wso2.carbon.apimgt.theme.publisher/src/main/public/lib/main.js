$(document).ready(function(){

    $('[rel="popover"]').popover({
        container: 'body',
        html: true,
        trigger:'focus',
        content: function () {
            var clone = $($(this).data('popover-content')).clone(true).removeClass('hide');
            return clone;
        }
    }).click(function(e) {
        e.preventDefault();
    });

    var mediaQuery = window.matchMedia('(max-width: 767px)');

    if (mediaQuery.matches) {
        $.sidebar_toggle('hide', '.sidebar-nav');
        $('[data-toggle=sidebar]').trigger('click')
    }

    mediaQuery.addListener(function(e) {
        if (e.matches) {
            $.sidebar_toggle('hide', '.sidebar-nav');
            $('[data-toggle=sidebar]').trigger('click')
        } else {
            $.sidebar_toggle('show', '.sidebar-nav')
            $('[data-toggle=sidebar]').trigger('click')
        }
    });

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