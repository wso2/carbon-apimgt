(function () {

    var WARN_MSG = '[WARN]';
    var INFO_MSG = '[INFO]';
    var ERR_MSG = '[ERR]';

    var main = function () {

        if (checkConsoleObject()) {
            init(console);
            return;
        }

        initEmptyLogger(console);
    };

    var checkConsoleObject = function () {
        if (typeof console==undefined) {
            return false;
        }
        return true;
    };

    var init = function (console) {

        console.info = function (msg) {
            console.log(INFO_MSG + msg);
        };

        console.warn = function (msg) {
            console.log(WARN_MSG + msg);
        };

        console.err = function (msg) {
            console.log(ERR_MSG + msg);
        };
    }

    var initEmptyLogger = function (console) {
        console={};
        console.info = function (msg) {

        };

        console.debug = function (msg) {

        };

        console.warn = function (msg) {

        };

        console.log = function (msg) {

        };
    }

    main();
})();