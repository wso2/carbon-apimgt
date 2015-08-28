var messages = {};
messages.alertSuccess = function(value){
    $.notify(value, {
        globalPosition: 'top center',
        className: 'success'
    });
};
messages.alertError = function(value){
    $.notify(value, {
        globalPosition: 'top center',
        className: 'error'
    });
};
messages.alertInfo = function(value){
    $.notify(value, {
        globalPosition: 'top center',
        className: 'info'
    });
};
messages.alertInfoLoader = function(value){
    $.notify.addStyle('happyblue', {
        html: "<div><span data-notify-html/></div>",
        classes: {
            base: {
                "white-space": "nowrap",
                "background-color": "lightblue",
                "padding": "5px"
            },
            superblue: {
                "color": "white",
                "background-color": "blue"
            }
        }
    });

    $.notify(value, {
        globalPosition: 'right bottom',
        elementPosition: 'right bottom',
        className: 'info',
        autoHide: false,
        style: 'happyblue'
    });

};
messages.alertWarn = function(value){
    var value = params.value;
    $.notify(value, {
        globalPosition: 'top center',
        className: 'warn'
    });
};