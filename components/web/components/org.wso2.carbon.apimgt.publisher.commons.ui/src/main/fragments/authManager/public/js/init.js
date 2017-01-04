$(function () {
    debugger;
    // TODO: Temporally Skip editor related URLs ~tmkb
    var request_path =  window.location.pathname.split('/');
    if (request_path.includes('editor')) {
        return false;
    }
    if(!authManager.getAuthStatus()){
        route.routTo(loginPageUri);
    }
   $('#logoutLink').click(function(){
       authManager.logout();
   })
});