var route = {};
route.routTo = function(location){
    location = contextPath + location;
    if(location == uri) return;
    window.location.href = location;
};