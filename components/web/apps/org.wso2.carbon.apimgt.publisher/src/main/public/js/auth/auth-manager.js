var authManager = {};
authManager.isLogged = false;
authManager.user = {};
authManager.getAuthStatus = function () {
    this.isLogged = !(!$.cookie('token') && !$.cookie('user'));
    return this.isLogged;
};
authManager.setAuthStatus = function (status) {
    this.isLogged = status;
};
authManager.setUserName = function (username) {
    this.user.username = username;
};
authManager.getUserName = function () {
    return this.user.username;
};
authManager.setUserScope = function (scope) {
    this.user.scope = scope;
};
authManager.getUserScope = function () {
    return this.user.scope;
};
authManager.login = function () {
    var params = {
        username: $('#username').val(),
        password: $('#password').val(),
        grant_type: 'password',
        scope: 'apim:api_view'
    };

    return $.ajax({
        type: 'POST',
        url: 'https://apis.wso2.com/apimanager/token',
        data: params,
        headers: {
            'Authorization': 'Basic dmRNdk05d1R4NkZ5NjNXa2xfb0FEaTdMQXZnYTp3UHNIQkJFS1FPbWU0bGZxYlJRZW1GY3ZqSUlh==',
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    });
};
authManager.logout = function () {
    if (this.getAuthStatus()) {
        this.setAuthStatus(false);
        delete this.user;
        $.cookie("token", null);
        $.cookie("user", null);
        $.cookie("userRole", null);
        //TODO revoke the token
        route.routTo(loginPageUri);
    } else {
        route.routTo(loginPageUri);
    }
};
authManager.tokenInterceptor = function ($q, $window) {
    return {
        request: function (config) {
            config.headers = config.headers || {};
            if ($.cookie('token')) {
                config.headers['X-Access-Token'] = $.cookie('token');
                config.headers['X-Key'] = $.cookie('user');
                config.headers['Content-Type'] = "application/json";
            }
            return config || $q.when(config);
        },
        response: function (response) {
            return response || $q.when(response);
        }
    };
};

