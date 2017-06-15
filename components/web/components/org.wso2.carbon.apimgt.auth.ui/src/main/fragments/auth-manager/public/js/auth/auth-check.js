/**
 * JQuery ajax() method to call DCR endpoint
 */
function loginRedirect(){
    $.ajax({
        type: "GET",
        url: "https://localhost:9292/store/auth/apis/login/dcr",
        dataType: "json",
        success:function(data){
            console.log(this.data);
            var client_id = data.client_id;
            var callback_URL = data.callback_URL;
            var scopes = data.scopes;
            // Add URL to config
            window.location = "https://localhost:9443/oauth2/authorize?response_type=code&client_id="+client_id+"&redirect_uri="+callback_URL+"&scope="+scopes;
            alert('Redirected to IS Login Page!');
        },
        error:function(error){
            alert('Error while Redirecting to IS Login Page!');
        }
    });
}

var token = getCookie("WSO2_AM_TOKEN_1");
var request_path = window.location.pathname.split('/');
if (!(request_path.includes('editor') || request_path.includes('auth') )&& !token) {
    // Do SSO enabled or not checking & redirection accordingly
    /*if (grantType == "password") {
        // Call Default Login
        window.location = loginURI; *//* TODO: This(loginURI) variable is assume to set in auth-manager.hbs by passing the @config.loginPageUri from configurations*//*
    }
    if (grantType == "code") {
        // Call SSO Login
    }*/
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
