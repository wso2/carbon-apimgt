$(function(){
    if(authManager.getAuthStatus()){
        route.routTo(loginRedirectUri);
    }
    var doLogin = function(){
        var loginPromise = authManager.login();
        loginPromise.then(function(data){
            authManager.setAuthStatus(true);
            authManager.setUserName('admin');//data.user.username;
            authManager.setUserScope(data.scope);//data.user.role;
            $.cookie('token', data.access_token);
            $.cookie('user', 'admin');
            $.cookie('userScope', data.scope);
            route.routTo(loginRedirectUri);
        });
    };
    $('#loginForm').on('keydown','input.form-control',function(e){
        console.info(e);
        if(e.keyCode == 13){
            doLogin();
        }
    });
    $('#loginButton').click(doLogin);
});
