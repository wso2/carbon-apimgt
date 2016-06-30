/**
* @description Random background color generator for thumbs
* @random - boolean, sets when to generate random or predefined colors. If false, palette will be ignored
* @palette - array, an array of hex values to be randomized
*/
(function ( $ ) {
$.fn.generateBgcolor = function(options){

    var defaultPalette = [
    "#F44336","#D32F2F","#B71C1C","#E91E63","#F06292","#D81B60","#AD1457","#880E4F","#9C27B0","#BA68C8",
    "#FF8A80","#FF5252","#FF1744","#D50000","#FF80AB","#FF4081","#F50057","#C51162","#EA80FC","#E040FB",
    "#D500F9","#AA00FF","#673AB7","#9575CD","#7E57C2","#5E35B1","#512DA8","#4527A0","#B388FF","#7C4DFF",
    "#651FFF","#6200EA","#3F51B5","#5C6BC0","#3F51B5","#303F9F","#1A237E","#536DFE","#3D5AFE","#304FFE",
    "#448AFF","#2979FF","#2962FF","#2196F3","#64B5F6","#1E88E5","#1976D2","#1565C0","#0D47A1","#03A9F4",
    "#4FC3F7","#29B6F6","#039BE5","#0288D1","#0277BD","#01579B","#40C4FF","#00B0FF","#0091EA","#00BCD4",
    "#4DD0E1","#00ACC1","#0097A7","#00E5FF","#00B8D4","#009688","#4DB6AC","#00897B","#00796B","#00695C",
    "#004D40","#00BFA5","#4CAF50","#66BB6A","#43A047","#388E3C","#2E7D32","#1B5E20","#8BC34A","#9CCC65",
    "#7CB342","#558B2F","#33691E","#CDDC39","#D4E157","#AFB42B","#9E9D24","#827717","#FFEB3B","#FFEE58",
    "#FDD835","#FBC02D","#F9A825","#F57F17","#FFC107","#FFCA28","#FFB300","#FF8F00","#FF6F00","#FF9800",
    "#FFA726","#F57C00","#E65100","#FF6D00","#FF5722","#E64A19","#D84315","#BF360C","#DD2C00","#795548",
    "#3E2723","#607D8B","#455A64","#37474F","#263238"
    ];

    var settings = $.extend({
        //defaults
       definite: true,
       palette: defaultPalette
    }, options );

    return this.each(function(){

        //traversal used inside iteration to avoid multiple plugin calls
        var strAstName = $(this).children().next().children(".api-name").text(),

        strAstName = strAstName.trim().split(" ");


        try {
            var firstChar = strAstName[0][0];

        }
        catch(err) {
            //console.log(err.message);
            if(typeof firstChar === 'undefined'){firstChar = "";}
        }


        if(settings.definite){
        /*predefined set of random colors **var colorD = settings.palette[Math.floor(Math.random()*settings.palette.length)];***/
            //predefined set of indexed colors
            var magicNumber = getNumberFromString(firstChar) % settings.palette.length,
                colorD = settings.palette[magicNumber];
            $(this).css('background', colorD);
        }else{
            //undefined set of random colors
            var letters = '0123456789ABCDEF'.split(''),
                colorR = '#';
            for (var i = 0; i < 6; i++ ) {
                colorR += letters[Math.floor(Math.random() * 16)];
            }
            $(this).css('background', colorR);
        }
    });

    function getNumberFromString(str) {
        var out = 0, len = str.length, pos = len;
        while (--pos > -1) {
            out += (str.charCodeAt(pos) - 64) * Math.pow(26, len - 1 - pos);
        }
        //ignore signed values for numbers in the string
        return Math.abs(out);
    }
};
}( jQuery ));

/**
* @description function to extract first two letters from a string
* @nameElement which element to extract the string from
*/
(function ( $ ) {
    $.fn.nametoChar = function( options ) {

        var settings = $.extend({
            // defaults
            nameElement: ""
        }, options );

        var str = $(settings.nameElement).text();
        str = str.trim().split(" ");
        try {
            var firstChar = str[0].charAt(0);
            var secondChar = str[1].charAt(0).toLowerCase();
        }
        catch(err) {
            if(typeof secondChar === 'undefined'){
                secondChar = "";
            }
        }
        return this.text(firstChar+secondChar);

    };
}( jQuery ));

/***********************************************************
 *  accordion config
 ***********************************************************/

$('#accordion1').on('shown.bs.collapse', function (e,f) {
    var elem = $(this).children().children('.in');
    var icon = $(elem).prev().find('.status').children();

    if($(elem).hasClass('in')){
        $(elem).prev().find('input[type=radio]').prop('checked',true);
    }
    icon.removeClass('fw-down');
    icon.addClass('fw-up');
});

$('#accordion1').on('hidden.bs.collapse', function (e,f) {
    var elem = $(this).children().children();
    var icon = $(elem).prev().find('.status').children();

    icon.removeClass('fw-up');
    icon.addClass('fw-down');
});

/***********************************************************
 *  Secondary toggle navigation
 ***********************************************************/

$('.secondary-nav').on('shown.bs.collapse', function(e){
    $('.secondary-nav-toggle').removeClass('fw-down').addClass('fw-up');
}).on('hidden.bs.collapse',function(e){
    $('.secondary-nav-toggle').removeClass('fw-up').addClass('fw-down');
})
