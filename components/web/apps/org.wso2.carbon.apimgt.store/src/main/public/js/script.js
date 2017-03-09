$(window).load(function() {

    if($('.affix').length > 0){
        $('.media').prepend('<div class="affix-spacer hidden-xs" style="width:' + $('.sidebar-wrapper').width() + 'px;float:left;height: 100vh"></div>');
    }

    $(".search-wrap .dropdown-menu li a").click(function() {
        $(".search-wrap .dropdown-toggle").html($(this).text() + ' <span class="caret"></span>');
    });

    $('.btn-launch').hover(function() {
        $(this).parent().find('.image-container .after').show();
    }, function() {
        $(this).parent().find('.image-container .after').hide();
    })


    $('.rating:not(.half) .icon').click(function() {
        var elem = $(this);

        elem.siblings('.icon').removeClass('active');
        elem.addClass('active');

        var rating = ($(this).closest('.rating').find('.icon').length - $(this).index());
        elem.trigger('clicked.rate', [rating]);
    });

    $('.rating:not(.half) .icon').on('clicked.rate', function(e, data) {
        console.log(data);
    });

    $('.rating.half .icon').children().click(function() {
        var elem = $(this);

        elem.closest('.icon').siblings().removeClass('active');
        elem.siblings().removeClass('active');
        elem.closest('.icon').addClass('active');
        elem.addClass('active');

        var rating = function() {
            if (elem.index() > 0) {
                return (elem.closest('.rating').find('.icon').length - elem.closest('.icon').index() - 1) + '.5';
            } else {
                return (elem.closest('.rating').find('.icon').length - elem.closest('.icon').index()) + '.0';
            }
        };

        elem.trigger('clicked.rate.half', [rating()]);
    });

    $('.rating.half .icon').children().on('clicked.rate.half', function(e, data) {
        console.log(data);
    });

    /**
     * Affix spacer when media left is affixed
     */
    $('.sidebar-wrapper').on('affix.bs.affix', function() {
        var sidebarWidth = $(this).width();
        $('.media').prepend('<div class="affix-spacer hidden-xs" style="width:' + sidebarWidth + 'px;float:left;height: 100vh"></div>');
        $('.media .media-left').css({'width': sidebarWidth});
    }).on('affixed-top.bs.affix', function() {
        $('.affix-spacer').remove();
    });

    $('.sign-up-additional').click(function() {
        var el = $('.extended-form'),
            triggerEl = $('.sign-up-additional');
        if (el.is(':hidden')) {
            el.slideDown(function() {
                triggerEl.text('Hide Additional Details');
            });
        } else {
            el.slideUp(function() {
                triggerEl.text('Show Additional Details');
            })
        }
    })
});