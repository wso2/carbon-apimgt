/**
 * JQuery ajax() method to call DCR endpoint
 */
function loginRedirect(){
    $.ajax({
        type: "GET",
        url: window.location.origin + "/login/login/" + app_name,
        dataType: "json",
        success:function(data){
            console.log(this.data);
            var client_id = data.client_id;
            var callback_URL = data.callback_url;
            var scopes = data.scopes;
            var is_sso_enabled = data.is_sso_enabled;
            var authorizationEndpoint = data.authorizationEndpoint;
            if(is_sso_enabled) {
                // Call SSO Login (When grantType = "authorization_code")
                // TODO: Add URL to config
                window.location = authorizationEndpoint+"?response_type=code&client_id="+client_id+"&redirect_uri="+callback_URL+"&scope="+scopes;
            }
            else {
                // Call Default Login (When grantType = "password")
                window.location = loginURI; // TODO: This(loginURI) variable is assume to set in auth-manager.hbs by passing the @config.loginPageUri from configurations
            }
        },
        error:function(error){
            alert('Error while Redirecting to Login Page!');
        }
    });
}

var token = getCookie("WSO2_AM_TOKEN_1");
var user = getCookie("LOGGED_IN_USER");
window.localStorage.setItem("user", user);
var request_path = window.location.pathname.split('/');
var app_name = request_path[1];
if (!(request_path.includes('editor') || request_path.includes('auth') )&& !token) {
    loginRedirect();
}

/**
 * Get the cookie from browser cookie storage by giving the name of the cookie
 * @param name {String} Name of the cookie which needs to retrieve
 * @returns {String} Corresponding value of the given cookie
 */
function getCookie(name) {
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
}
