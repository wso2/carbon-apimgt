
$(document).ready(function () {


    var wrapper         = $(".input_fields_wrap"); //Fields wrapper
    var wrapperContacts         = $(".input_fields_wrap_contacts"); //Fields wrapper

    var sandboxApplication = {

        init: function init(){
            $(".cDivTabSectionSandbox").hide();
            $(".cDivSpecificClientDetailsSandbox").hide();
            $("#iDivSandBoxSaveSectionWrapper").show();

            $('input:radio[name="registeringModeSandBox"]').change(
                function(){
                    if($(this).val() == "semimanual"){
                        $(".cDivTabCreateClientSandbox").show();
                        $(".cDivSpecificClientDetailsSandbox").hide();
                    }else if($(this).val() == "manual"){

                        $(".cDivSpecificClientDetailsSandbox").show();
                        $(".cDivTabCreateClientSandbox").hide();

                    }

                });
        },
        addNewCallBackURLelementSandbox :function addNewCallBackURLelementSandbox(){
            $(".cDivPlusIconSandBox").click(function(e){ //on add input button click

                $(".input_fields_wrap_sandbox").append('<div class="cDivCallBackUrlElementSandBox"><input type="text" class="input-large icallbackURLsSandBox" name="callbackURLsSandBox[]" title="callbackURLsSandBox"/><div class="cDivRemoveIconSandBox remove_field_sandbox">' + i18n.t("Remove") + '</div></div>');

            });
        },
        addNewContactElementSandbox :function addNewContactElementSandbox(){
            $(".cDivPlusIconSandBoxContact").click(function(e){ //on add input button click
                e.preventDefault();
                $(".input_fields_wrap_sandbox_contacts").append('<div class="cDivContactSandBoxElement"><input type="text" class="input-large iContactsSandBox" name="iSandboxAuthAppContact[]" title="SandboxAuthAppContact"/><div class="cDivRemoveIconSandBox remove_field_sandbox_contact">' + i18n.t("Remove") + '</div></div>');

            });
        },
        addNewScopeSandBox :function addNewScopeSandBox(){
            $(".cDivPlusIconScopeSandBox").click(function(e){ //on add input button click

                //alert($(".input_fields_wrap_scope").html());

                $(".input_fields_wrap_scope_sandbox").append('<div class="cDivScopeElementSandBox"><input type="text" class="input-large iScopeSandBox" name="iProductionAppScopeSandBox[]" title="ProductionAppScopeSandBox"/><div class="cDivRemoveIcon remove_field_scope_sandbox">' + i18n.t("Remove") + '</div></div>');

            });
        },
        deleteCallbackURLElementSandbox :function deleteCallbackURLElementSandbox(){
            $(".input_fields_wrap_sandbox").on("click",".remove_field_sandbox", function(e){ //user click on remove text
                e.preventDefault();
                $(this).parent('div').remove(); ;
            })
        },
        deleteContactsElementSandBox :function deleteContactsElementSandBox(){
            $(".input_fields_wrap_sandbox_contacts").on("click",".remove_field_sandbox_contact", function(e){
                e.preventDefault();
                $(this).parent('div').remove(); ;
            })
        },
        deleteScopeElementSandBox :function deleteScopeElementSandBox(){

            $(".input_fields_wrap_scope_sandbox").on("click",".remove_field_scope_sandbox", function(e){

                e.preventDefault();
                $(this).parent('div').remove();
            })
        },
        registerClientSandBox: function registerClientSandBox(){

            /* register client in OIDC server  */
            $(".btnRegisterClientSandbox").click(function(){
                //alert("dsd");
                var keyTypeSandBox = "SANDBOX";
                var applicationSandbox = $("#iHiddenApplicationNameSandBox").val();
                var clientNameSanBox = $("#iSandboxAuthAppName").val();
                var requestModeSandBox = $("#iHiddenRequestModeSandBox").val();
                var consumerKeySandBox = $(".sandBoxConsumerKey").val();

                //alert(keyType);

                var callbackUrlsArraySandBox = new Array();
                $('.icallbackURLsSandBox').each(function(){
                    if($(this).val().length >0){
                        callbackUrlsArraySandBox.push($(this).val());
                    }
                });

                //console.log(callbackUrlsArraySandBox);

                var contactArraySandBox = new Array();
                $('.iContactsSandBox').each(function(){
                    if($(this).val().length >0){
                        contactArraySandBox.push($(this).val());
                    }
                });
                //console.log(contactArraySandBox);

                var scopeArraySandBox = new Array();
                $('.iScopeSandBox').each(function(){
                    if($(this).val().length >0){
                        scopeArraySandBox.push($(this).val());
                    }
                });

                var grantArraySandBox = new Array();
                $('.iGrantsSandBox').each(function(){
                    if($(this).is(':checked')){
                        grantArraySandBox.push($(this).val());
                    }
                });


                var responseArraySandBox = new Array();
                $('.iResponseTypeSandBox').each(function(){
                    if($(this).is(':checked')){
                        responseArraySandBox.push($(this).val());
                    }
                });

                var authMethod = null;

                $(".iAuthMethodSandBox").each(function(){
                    if (this.checked)
                    {
                        authMethod = this.value;
                    }
                });

                if(callbackUrlsArraySandBox.length != 0) {
                    var oJsonParams =
                        {
                            //"application":applicationSandbox,
                            "client_name": clientNameSanBox,
                            //"key_type" : keyTypeSandBox,
                            "callback_url": callbackUrlsArraySandBox,
                            "client_id": consumerKeySandBox,
                            "contact": contactArraySandBox,
                            "scope": scopeArraySandBox,
                            "grant_types": grantArraySandBox,
                            "response_types": responseArraySandBox,
                            "token_endpoint_auth_method": authMethod
                        }
                        ;
                    //console.log(oJsonParams);
                    jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                        action: requestModeSandBox,
                        application: applicationSandbox,
                        key_type: keyTypeSandBox,
                        jsonParams: JSON.stringify(oJsonParams)

                    }, function (result) {
                        if (!result.error) {
                            location.reload();
                        } else {
                            jagg.message({content: result.message, type: "error"});
                        }

                    }, "json");
                }else{
                    alert(i18n.t("Callback URLs cannot be empty"));
                }

            });

        },
        updateActionClickSandBox : function updateActionClickSandBox(){
            $("#iDivUpdateActionSandbox").click(function(){
                $(".cDivTabSectionSandbox").show();
                $(".cDivAuthAppListSandBox").hide();
            });
        },
        deleteActionClickSandBox : function deleteActionClickSandBox(){
            $("#iDivDeleteActionSandbox").click(function(){

                var r = confirm(i18n.t("Do you want to delete this sandbox auth app from OIDC"));
                if (r == true) {
                    sandboxApplication.deleteAuthApplicationSandBox();
                }

            });
        },
        deleteAuthApplicationSandBox : function deleteAuthApplicationSandBox(){
            var consumerKey = $(".sandBoxConsumerKey").val();

            jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                action:"deleteAuthApplication",
                consumerKey:consumerKey

            }, function (result) {
                if (!result.error) {
                    location.reload();
                }else {
                    jagg.message({content:result.message,type:"error"});
                }

            }, "json");

        },
        cancleBtnClickSandBox : function cancleBtnClickSandBox(){
            $(".btnCancleUpdateSandBox").click(function(){
                $(".cDivTabSectionSandbox").hide();
                $(".cDivAuthAppListSandBox").show();

            });
        },
        addDefaultScope : function addDefaultScope(){

            if(!$(".sandBoxConsumerKey").val()){
                var scopeArraySandBox = ["phone", "openid", "offline_access", "address", "email", "profile"];
                var arrayLength = scopeArraySandBox.length;
                for (var i = 0; i < arrayLength; i++) {
                    $(".input_fields_wrap_scope_sandbox").append('<div class="cDivScopeElementSandBox"><input type="text" class="input-large iScopeSandBox" value="'+scopeArraySandBox[i]+'" name="iProductionAppScopeSandBox[]"/><div class="cDivRemoveIcon remove_field_scope_sandbox">' + i18n.t("Remove") + '</div></div>');
                }

            }
        },
        toogleSandBoxSave : function toogleSandBoxSave(){
            $( "#iSandBoxToogleBtn" ).click(function() {
                $("#iDivSandBoxSaveSectionWrapper").show();
                $( "#iSandBoxToogle").hide();
            });
            ;
        },
        cancelCreateSandboxApp: function cancelCreateSandboxApp(){
            $(".btnCancleCreateSandBox").click(function(){
                //$( "#iDivSandBoxSaveSectionWrapper").hide();
                //$( "#iSandBoxToogle").show();
            });
        },
        iBtnCancleUpdateSandbox: function iBtnCancleUpdateSandbox(){
            $(".iBtnCancleUpdateSandbox").click(function(){

                $( ".cDivTabSectionSandbox").hide();
                $( ".cDivAuthAppListSandBox").show();
            });
        },
        iBtnCancelCreateApp:function iBtnCancelCreateApp(){
            $(".iBtnCancelCreateApp").click(function(){
                $(".cDivTabSection").hide();
                $(".cDivAuthAppList").show();
            });
        }

    };

    sandboxApplication.init();
    sandboxApplication.addNewCallBackURLelementSandbox();
    sandboxApplication.deleteCallbackURLElementSandbox();
    sandboxApplication.addNewContactElementSandbox();
    sandboxApplication.deleteContactsElementSandBox();
    sandboxApplication.registerClientSandBox();
    sandboxApplication.addNewScopeSandBox();
    sandboxApplication.deleteScopeElementSandBox();
    sandboxApplication.addDefaultScope();
    sandboxApplication.updateActionClickSandBox();
    sandboxApplication.cancleBtnClickSandBox();
    sandboxApplication.deleteActionClickSandBox();
    sandboxApplication.toogleSandBoxSave();
    sandboxApplication.cancelCreateSandboxApp();
    sandboxApplication.iBtnCancleUpdateSandbox();

    var productionApplication = {

        init: function init(){
            $(".cDivTabSection").hide();
            $(".cDivSpecificClientDetails").hide();
            $("#iDivProductionSaveSectionWrapper").show();

            $('input:radio[name="registeringMode"]').change(
                function(){

                    if($(this).val() == "semimanual"){
                        $(".cDivTabCreateClient").show();
                        $(".cDivSpecificClientDetails").hide();
                    }else if($(this).val() == "manual"){
                        $(".cDivSpecificClientDetails").show();
                        $(".cDivTabCreateClient").hide();

                    }

                });
        },
        toogleProductionSave : function toogleProductionSave(){
            $( "#iDivProductionToogle" ).click(function() {
                $("#iDivProductionSaveSectionWrapper").show();
                $( "#iDivProductionToogle").hide();
            });
            ;
        },
        cancelCreateApp: function cancelCreateApp(){
            $(".btnCancleCreateProduction").click(function(){

                //$( "#iDivProductionSaveSectionWrapper").hide();
                //$( "#iDivProductionToogle").show();
            });
        },

        addNewCallBackURLelementProduction :function addNewCallBackURLelementProduction(){
            $(".cDivPlusIcon").click(function(e){ //on add input button click

                $(wrapper).append('<div class="cDivCallBackUrlElement"><input type="text" class="input-large iCallBackUrls" name="callbackURLs[]"/><div class="cDivRemoveIcon remove_field">' + i18n.t("Remove") + '</div></div>');

            });
        },
        addNewContactElement :function addNewContactElement(){
            $(".cDivPlusIconContact").click(function(e){ //on add input button click
                e.preventDefault();
                $(wrapperContacts).append('<div class="cDivContactElement"><input type="text" class="input-large iContacts" name="iProductionAuthAppContact[]"/><div class="cDivRemoveIcon remove_field_contact">' + i18n.t("Remove") + '</div></div>');

            });
        },
        addNewScope :function addNewScope(){
            $(".cDivPlusIconScope").click(function(e){ //on add input button click

                //alert($(".input_fields_wrap_scope").html());

                $(".input_fields_wrap_scope").append('<div class="cDivScopeElement"><input type="text" class="input-large iScope" name="iProductionAppScope[]"/><div class="cDivRemoveIcon remove_field_scope">' + i18n.t("Remove") + '</div></div>');

            });
        },
        deleteCallbackURLElement :function deleteCallbackURLElement(){
            $(wrapper).on("click",".remove_field", function(e){ //user click on remove text
                e.preventDefault();
                $(this).parent('div').remove(); ;
            })
        },
        deleteContactsElement :function deleteContactsElement(){
            $(wrapperContacts).on("click",".remove_field_contact", function(e){
                e.preventDefault();
                $(this).parent('div').remove(); ;
            })
        },
        deleteScopeElement :function deleteScopeElement(){

            $(".input_fields_wrap_scope").on("click",".remove_field_scope", function(e){

                e.preventDefault();
                $(this).parent('div').remove();
            })
        },
        registerClientProduction: function registerClientProduction(){


            $(".btnRegisterClientProduction").click(function(){
                var keyType = "PRODUCTION";
                var allowDomains = $('#allowedDomainsPro').val();
                var application = $("#iHiddenApplicationName").val();
                var clientName = $("#iProductionAuthAppName").val();
                var requestMode = $("#iHiddenRequestMode").val();
                var consumerKey = $(".prodConsumerKey").val();

                //alert(allowDomains);

                var callbackUrlsArray = new Array();
                $('.iCallBackUrls').each(function(){
                    if($(this).val().length >0){
                        callbackUrlsArray.push($(this).val());
                    }
                });

                var contactArray = new Array();
                $('.iContacts').each(function(){
                    if($(this).val().length >0){
                        contactArray.push($(this).val());
                    }
                });

                var scopeArray = new Array();
                $('.iScope').each(function(){
                    if($(this).val().length >0){
                        scopeArray.push($(this).val());
                    }
                });
                var grantArray = new Array();
                $('.iGrants').each(function(){
                    if($(this).is(':checked')){
                        grantArray.push($(this).val());
                    }
                });
                var responseArray = new Array();
                $('.iResponseType').each(function(){
                    if($(this).is(':checked')){
                        responseArray.push($(this).val());
                    }
                });

                var authMethod = null;

                $(".iAuthMethod").each(function(){
                    if (this.checked)
                    {
                        authMethod = this.value;
                    }
                });

                if(callbackUrlsArray.length != 0) {
                    var oJsonParams =
                        {
                            //"application":application,
                            "client_name": clientName,
                            "allowDomains": allowDomains,
                            "callback_url": callbackUrlsArray,
                            "client_id": consumerKey,
                            "contact": contactArray,
                            "scope": scopeArray,
                            "grant_types": grantArray,
                            "response_types": responseArray,
                            "token_endpoint_auth_method": authMethod
                        }
                        ;
                    //console.log(oJsonParams);
                    jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                        action: requestMode,
                        application: application,
                        key_type: keyType,
                        jsonParams: JSON.stringify(oJsonParams)

                    }, function (result) {
                        if (!result.error) {
                            location.reload();
                        } else {
                            jagg.message({content: result.message, type: "error"});
                        }

                    }, "json");
                }else{
                    alert(i18n.t("Callback URLs cannot be empty"));
                }

            });

        },
        updateActionClick : function updateActionClick(){
            $("#iDivUpdateAction").click(function(){

                $(".cDivTabSection").show();
                $(".cDivAuthAppList").hide();

            });
        },
        deleteActionClick : function deleteActionClick(){
            $("#iDivDeleteAction").click(function(){

                var r = confirm(i18n.t("Do you want to delete this sandbox auth app from OIDC"));
                if (r == true) {
                    productionApplication.deleteAuthApplication();
                }

            });
        },
        deleteAuthApplication : function deleteAuthApp(){
            var consumerKey = $(".prodConsumerKey").val();

                jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
                    action:"deleteAuthApplication",
                    consumerKey:consumerKey

                }, function (result) {
                    if (!result.error) {
                        location.reload();
                    }else {
                        jagg.message({content:result.message,type:"error"});
                    }

                }, "json");

        },
        cancleBtnClick : function cancleBtnClick(){
            $(".btnCancleUpdate").click(function(){

                $(".cDivTabSection").hide();
                $(".cDivAuthAppList").show();

            });
        },
        addDefaultScope : function addDefaultScope(){

            if(!$(".prodConsumerKey").val()){
                var scopeArray = ["phone", "openid", "offline_access", "address", "email", "profile"];
                var arrayLength = scopeArray.length;
                for (var i = 0; i < arrayLength; i++) {
                    $(".input_fields_wrap_scope").append('<div class="cDivScopeElement"><input type="text" class="input-large iScope" value="'+scopeArray[i]+'" name="iProductionAppScope[]"/><div class="cDivRemoveIcon remove_field_scope">' + i18n.t("Remove") + '</div></div>');
                }

            }
        },
        iBtnCancelCreateApp:function iBtnCancelCreateApp(){
            $(".iBtnCancelCreateApp").click(function(){
                $(".cDivTabSection").hide();
                $(".cDivAuthAppList").show();
            });
        }
    };
    productionApplication.init();
    productionApplication.addNewCallBackURLelementProduction();
    productionApplication.addNewContactElement();
    productionApplication.deleteCallbackURLElement();
    productionApplication.deleteContactsElement();
    productionApplication.registerClientProduction();
    productionApplication.updateActionClick();
    productionApplication.deleteActionClick();
    //productionApplication.deleteAuthApp();
    productionApplication.cancleBtnClick();
    productionApplication.addNewScope();
    productionApplication.deleteScopeElement();
    productionApplication.addDefaultScope();
    productionApplication.toogleProductionSave();
    productionApplication.cancelCreateApp();
    productionApplication.iBtnCancelCreateApp();

});