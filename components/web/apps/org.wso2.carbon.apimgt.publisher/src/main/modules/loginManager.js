var loginManager = {};
(function (deviceManager) {
    loginManager.authenticate = function (env) {
        var redirectURL;
        if(!env.request.getCookieValue("token1")) {
            redirectURL = env.contextPath + "/commons/login";
            sendRedirect(redirectURL);
        }
    };
})(loginManager);