var token = getCookie("WSO2_AM_TOKEN_1");
var request_path = window.location.pathname.split('/');
if (!(request_path.includes('editor') || request_path.includes('auth') )&& !token) {
    //window.location = loginURI; /* TODO: This(loginURI) variable is assume to set in auth-manager.hbs by passing the @config.loginPageUri from configurations*/
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

//JQuery ajax() method to call /dcr endpoint and get the ID and Callback uri and redirect to IS login page by making the uri
//Recheck
function loginRedirect(){
    $.ajax({
        type: "GET",
        url: "https://localhost:9292/store/oauth/dcr"
        succss:function(){
            alert('Redirected to DCR Login Page!');
        },
        error:function(error){
            alert('Error while Redirecting!');
        }
    });
}
