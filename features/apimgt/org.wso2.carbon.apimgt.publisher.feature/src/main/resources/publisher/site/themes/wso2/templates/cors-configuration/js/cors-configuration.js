$(document).ready(function(){
                    $('input').on('change', function() {
                      var corsenable;
                      var accessControlAllowMethods;
                      var enableAllowCredentials;
                      if($('#toggleCors').attr('checked')) {
                        corsenable = true;
                        } else {
                        corsenable = false;
                        } 
                        if($('#allowCredentials').attr('checked')) {
                        enableAllowCredentials = true;
                        } else {
                        enableAllowCredentials = false;
                        }
                        var allowOrigins = $('#accessOrigin').val();
                        var allowHeadersValues = $('#allowHeaders').val(); 
                        var allowMethodsValues = $('#allowMethods').val();
                        if(allowOrigins!=""){
                          allowOrigins = allowOrigins.replace(/,/g,"','");
                          allowOrigins= "['"+allowOrigins+"']";  
                        }else{
                           allowOrigins = "[]"; 
                        }
                        if(allowHeadersValues!=""){
                          allowHeadersValues = allowHeadersValues.replace(/,/g,"','");
                          allowHeadersValues= "['"+allowHeadersValues+"']";  
                        }else{
                           allowHeadersValues = "[]"; 
                        }
                         if(allowMethodsValues != ""){
                                                  allowMethodsValues = allowMethodsValues.replace(/,/g,"','");
                                                  allowMethodsValues = "['" + allowMethodsValues + "']";
                                                }else{
                                                   allowMethodsValues = "[]";
                                                }
  var corsJsonString = "{'corsConfigurationEnabled':"+corsenable+",'accessControlAllowOrigins':"+allowOrigins+",'accessControlAllowCredentials':"+enableAllowCredentials+",'accessControlAllowHeaders':"+allowHeadersValues+",'accessControlAllowMethods':"+allowMethodsValues+"}";
                    $('#corsConfiguration').val(corsJsonString);
                    });
});