$(function () {
    if(!authManager.getAuthStatus()){
        route.routTo(loginPageUri);
    }
   $('#logoutLink').click(function(){
       authManager.logout();
   })
});