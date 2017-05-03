$(document).ready(function(){
    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".yellow").insertBefore(prev).css('top','0px').addClass('active');
    $(".yellow").find('.left-menu-item i').removeClass('fw-down').addClass('fw-up');
});