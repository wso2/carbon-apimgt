$(document).ready(function() {
    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".orange").insertBefore(prev).css('top', '0px').addClass('active');
});