var AppkeyTemplate, client;
var grantTypes = {
    "0": {"key": "refresh_token", "grantType": "Refresh Token"},
    "1": {"key": "urn:ietf:params:oauth:grant-type:saml2-bearer", "grantType": "SAML2"},
    "2": {"key": "implicit", "grantType": "Implicit"},
    "3": {"key": "password", "grantType": "Password"},
    "4": {"key": "iwa:ntlm", "grantType": "IWA-NTLM"},
    "5": {"key": "client_credentials", "grantType": "Client Credential"},
    "6": {"key": "authorization_code", "grantType": "Code"}
};

$(function () {

    $('.selectpicker').selectpicker({
        style: 'btn-info',
        size: 4
    });

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".green").insertBefore(prev).css('top','0px').addClass('active');

     client = new SwaggerClient({

        url: swaggerURL,
        success: function () {
            var id = document.getElementById("appid").value;
            setAuthHeader(client);
            client["Application (individual)"].get_applications_applicationId
            ({"applicationId": id},
                function (data) {
                    renderAppDetails(data);
                    client["Subscription Collection"].get_subscriptions({
                        "apiId": "",
                        "applicationId": id,
                        "apiType": "STANDARD",
                        "responseContentType": 'application/json'
                    },
                        function (subscriptionData) {
                            var subData = {};
                            subData.data = subscriptionData.obj.list
                            var mode = "PREPEND";
                            var context = {
                                "subscriptionsAvailable": subData.data.length>0?true:false,
                                "contextPath":contextPath

                            };

                            //Render APIs listing page
                            UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.subscription-listing", context, {
                                onSuccess: function (data) {
                                    $("#subscription").append(data);


                                    $('#subscription-table').DataTable({
                                        ajax: function (raw_data, callback, settings) {
                                            callback(subData);
                                        },
                                        columns: [
                                            {
                                                "data": "apiIdentifier",
                                                "render": function (data, type, row, meta) {
                                                    if (type === 'display') {
                                                        var api = row.apiName + " - "+ row.apiVersion;
                                                        return $('<a>')
                                                            .attr('href', contextPath + "/apis/" + data)
                                                            .text(api)
                                                            .wrap('<div></div>')
                                                            .parent()
                                                            .html();

                                                    } else {
                                                        return subData;
                                                    }
                                                }
                                            },
                                            {'data': 'policy'},
                                            {'data': 'lifeCycleStatus'},
                                            {'data': 'subscriptionId'}
                                        ],
                                        columnDefs: [
                                            {
                                                targets: ["subscription-listing-action"], //class name will be matched on the TH for the column
                                                searchable: false,
                                                sortable: false,
                                                render: _renderActionButtons // Method to render the action buttons per row
                                            }
                                        ]
                                    });
                                }, onFailure: function (message, e) {
                                    var message = "Error occurred while getting subscription details subscription." + message;
                                    noty({
                                        text: message,
                                        type: 'error',
                                        dismissQueue: true,
                                        modal: true,
                                        progressBar: true,
                                        timeout: 2000,
                                        layout: 'top',
                                        theme: 'relax',
                                        maxVisible: 10,
                                    });
                                }
                            });

                            $(document).on('click', 'a.deleteSub', function () {
                                var subId = $(this).attr("data-id");
                                var type="alert";
                                var layout="topCenter";
                                noty({
                                    text : "Do you want to un subscribe",
                                    type : type,
                                    dismissQueue: true,
                                    layout : layout,
                                    theme : 'relax',
                                    buttons : [
                                        {addClass: 'btn btn-primary', text: 'Ok', onClick: function ($noty) {
                                            $noty.close();

                                            setAuthHeader(client);
                                            client["Subscription (individual)"].delete_subscriptions_subscriptionId({"subscriptionId": subId},
                                                function (success) {
                                                    var message = "Subscription removed successfully";
                                                    noty({
                                                        text: message,
                                                        type: 'success',
                                                        dismissQueue: true,
                                                        modal: true,
                                                        progressBar: true,
                                                        timeout: 2000,
                                                        layout: 'top',
                                                        theme: 'relax',
                                                        maxVisible: 10,
                                                    });
                                                    //TODO: Reload element only
                                                    setTimeout(function(){ window.location.reload(true); }, 3000);

                                                },
                                                function (error) {
                                                    var message = "Error occurred while deleting subscription";
                                                    noty({
                                                        text: message,
                                                        type: 'warning',
                                                        dismissQueue: true,
                                                        modal: true,
                                                        progressBar: true,
                                                        timeout: 2000,
                                                        layout: 'top',
                                                        theme: 'relax',
                                                        maxVisible: 10,
                                                    });
                                                });
                                        }
                                        },
                                        {addClass: 'btn btn-danger', text: 'Cancel', onClick: function ($noty) {
                                            $noty.close();
                                        }
                                        }
                                    ]
                                });
                            })
                        },
                        function (error) {
                            if(error.status==401){
                                redirectToLogin(contextPath);
                            }
                        });

                        // get registered composite API for this applications.
                        // Note: one application can only have one composite API
                        client["Subscription Collection"].get_subscriptions({
                            "apiId": "",
                            "applicationId": id,
                            "apiType": "COMPOSITE",
                            "responseContentType": 'application/json'
                        }, function (data) {
                            var compositeAPIId = undefined;

                            if (data.obj.count > 0) {
                                compositeAPIId = data.obj.list[0].apiIdentifier; // 0 - there is only one composite api
                            }

                            if (compositeAPIId) {
                                client["CompositeAPI (Individual)"].get_composite_apis_apiId({
                                    "apiId": compositeAPIId,
                                    "responseContentType": 'application/json'
                                }, function (apiData) {
                                    renderCompositeApi(apiData.obj);
                                }, function (error) {
                                    if (error.status == 401){
                                        redirectToLogin(contextPath);
                                    } else {
                                        var message = "Error occurred while loading application details";
                                        noty({
                                            text: message,
                                            type: 'error',
                                            dismissQueue: true,
                                            modal: true,
                                            progressBar: true,
                                            timeout: 2000,
                                            layout: 'top',
                                            theme: 'relax',
                                            maxVisible: 10,
                                        });
                                    }
                                });
                            } else {
                                renderCompositeApi();
                            }

                        });
                },
            function (error) {
                if(error.status == 401){
                    redirectToLogin(contextPath);
                }
            });
        },
        error: function (e) {
            alert("Error occurred while creating client");
            if(error.status==401){
                redirectToLogin(contextPath);
            }
        }
    });

    var renderAppDetails = function (data) {
        var callbacks = {onSuccess: function (
        ) {
            renderApplicationKeys(data);
        },onFailure: function (message, e) {
            var message = "Error occurred while viewing application details";
            noty({
                text: message,
                type: 'error',
                dismissQueue: true,
                modal: true,
                progressBar: true,
                timeout: 2000,
                layout: 'top',
                theme: 'relax',
                maxVisible: 10,
            });
        }};
        var mode = "PREPEND";
        var isApplicationActive = true;
        if (data.obj.lifeCycleStatus != "APPROVED") {
            isApplicationActive = false;
        }
        var context = {
            "name": data.obj.name,
            "tier": data.obj.throttlingTier,
            "status": data.obj.lifeCycleStatus,
            "description": data.obj.description,
            "isApplicationActive": isApplicationActive,
            "applicationId": data.obj.applicationId
        };
        UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.application-navbar", context, 
            "application-navbar", mode, callbacks);
        UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.application-details",context,
            "application-details", mode, callbacks);
    };

    var renderApplicationKeys = function (data) {
        //TODO : Following part is commented due to issue in uuf client render fragment method. Once that is fixed
        //TODO : we need to use following code and we need to remove the Handlebars.compile(keyTemplateScript);
        //var keyTemplateScript = result;
        //AppkeyTemplate = Handlebars.compile(keyTemplateScript);
        var  context;

        if (typeof data.obj.keys[0] !== 'undefined') {

            for (var i = 0; i < data.obj.keys.length; i++) {
                var keyType = data.obj.keys[i].keyType;

                for (var j = 0; j < Object.keys(grantTypes).length; j++) {
                    if ((data.obj.callbackUrl == undefined || data.obj.callbackUrl == "" ) &&
                        (grantTypes[j].key == "authorization_code" || grantTypes[j].key == "implicit")) {
                        grantTypes[j].selected = false;
                        grantTypes[j].disabled = true;
                    } else {
                        //TODO check with supportedGrantTypes
                        grantTypes[j].selected = true;
                        grantTypes[j].disabled = false;
                    }
                }
                context = {
                    "keyType": keyType,
                    "callbackUrl": data.obj.callbackUrl,
                    "grantTypes": grantTypes,
                    "name": data.obj.name,
                    "show_keys": false,
                    "Key": data.obj.keys[i].token.accessToken,
                    "ConsumerKey": data.obj.keys[i].consumerKey,
                    "ConsumerSecret": data.obj.keys[i].consumerSecret,
                    "username": "Username",
                    "password": "Password",
                    "basickey": window.btoa(data.obj.keys[i].consumerKey + ":" + data.obj.keys[i].consumerSecret),
                    "ValidityTime": data.obj.keys[i].token.validityTime,
                    "Scopes": "",
                    "tokenScopes": data.obj.keys[i].token.tokenScopes,
                    "provide_keys_form": false,
                    "provide_keys": false,
                    "gatewayurlendpoint": "(gatewayurl)/token"

                };
                UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.application-keys", context, {
                    onSuccess: function (renderedData) {
                        if (this.keyType.toLowerCase() == "production") {
                            $("#production").append(renderedData);
                            registerClipBoardClients();
                            if (data.obj.keys.length == 1) {
                                context = setDefaultContext(data);
                                //compiledHtml = AppkeyTemplate(context);
                                UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.application-keys", context, {
                                    onSuccess: function (renderedData) {
                                        $("#sandbox").append(renderedData);
                                        registerClipBoardClients();
                                    }, onFailure: function (message, e) {
                                        var message = "Error occurred while getting default application key details." + message;
                                        noty({
                                            text: message,
                                            type: 'error',
                                            dismissQueue: true,
                                            modal: true,
                                            progressBar: true,
                                            timeout: 2000,
                                            layout: 'top',
                                            theme: 'relax',
                                            maxVisible: 10,
                                        });
                                    }
                                })
                            }
                        }
                        else {
                            $("#sandbox").append(renderedData);
                            registerClipBoardClients();
                            if (data.obj.keys.length == 1) {
                                context = setDefaultContext(data);
                                UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.application-keys", context, {
                                    onSuccess: function (renderedData) {
                                        $("#production").append(renderedData);
                                        registerClipBoardClients();
                                    }, onFailure: function (message, e) {
                                        var message = "Error occurred while getting default application key details." + message;
                                        noty({
                                            text: message,
                                            type: 'error',
                                            dismissQueue: true,
                                            modal: true,
                                            progressBar: true,
                                            timeout: 2000,
                                            layout: 'top',
                                            theme: 'relax',
                                            maxVisible: 10,
                                        });
                                    }
                                })

                            }
                        }
                        $('.selectpicker').selectpicker('refresh');

                    }.bind(context), onFailure: function (message, e) {
                        var message = "Error occurred while getting subscription details subscription." + message;
                        noty({
                            text: message,
                            type: 'error',
                            dismissQueue: true,
                            modal: true,
                            progressBar: true,
                            timeout: 2000,
                            layout: 'top',
                            theme: 'relax',
                            maxVisible: 10,
                        });
                    }
                    //compiledHtml = AppkeyTemplate(context);

                })
            }
        }else {
            context = setDefaultContext(data);
            UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.application-keys", context, {
                onSuccess: function (renderedData) {
                    $("#production").append(renderedData);
                    $("#sandbox").append(renderedData);
                    $('.selectpicker').selectpicker('refresh');
                }, onFailure: function (message, e) {
                    var message = "Error occurred while getting default application key details." + message;
                    noty({
                        text: message,
                        type: 'error',
                        dismissQueue: true,
                        modal: true,
                        progressBar: true,
                        timeout: 2000,
                        layout: 'top',
                        theme: 'relax',
                        maxVisible: 10,
                    });
                }
            })


        }

    };

    var registerClipBoardClients = function () {
        $('.copy-button').each(function(){
            var ClipboardClient = new ZeroClipboard($(this));
            ClipboardClient.on('ready', function (event) {
                ClipboardClient.on('copy', function (event) {
                    event.clipboardData.setData('text/plain', event.target.value);
                });
            });

            ClipboardClient.on('error', function (event) {
                alert('ZeroClipboard error of type "' + event.name + '": ' + event.message);
                ZeroClipboard.destroy();
            })
        });
    };

    var setDefaultContext = function (data) {

        var ifCreatedKeystate, ifRejectedKeystate, ifCompletedKeystate, keyState;

        for (var j = 0; j < Object.keys(grantTypes).length; j++) {
            if ((data.obj.callbackUrl == undefined || data.obj.callbackUrl == "" ) &&
                (grantTypes[j].key == "authorization_code" || grantTypes[j].key == "implicit")) {
                grantTypes[j].selected = false;
                grantTypes[j].disabled = true;
            } else {
                grantTypes[j].selected = true;
                grantTypes[j].disabled = false;
            }
        }

        if (data.obj.status == 'CREATED') {
            keyState = true;
            ifCreatedKeystate = true;
            ifRejectedKeystate = false;
            ifCompletedKeystate = false;
        }
        else if (data.obj.status == 'REJECTED') {
            keyState = true;
            ifCreatedKeystate = false;
            ifRejectedKeystate = true;
            ifCompletedKeystate = false;
        }
        else if (data.obj.status == 'COMPLETED') {
            keyState = true;
            ifCreatedKeystate = false;
            ifRejectedKeystate = false;
            ifCompletedKeystate = true;
        }
        else {
            keyState = false;
            ifCreatedKeystate = false;
            ifRejectedKeystate = false;
            ifCompletedKeystate = false;
        }

        var context = {
            "callbackUrl": data.obj.callbackUrl,
            "grantTypes": grantTypes,
            "name": data.obj.name,
            "keyState": keyState,
            "show_keys": false,
            "username": "Username",
            "password": "Password",
            "provide_keys_form": false,
            "provide_keys": false,
            "ifCreatedKeystate": ifCreatedKeystate,
            "ifRejectedKeystate": ifRejectedKeystate,
            "ifCompletedKeystate": ifCompletedKeystate
        };

        return context;
    };
});

var generateKeys = function () {

    var tabid = $('#tabs li.active').attr("id");
    var keyType;
    if (tabid == "production-keys-tab") {
        keyType = "PRODUCTION";
    }
    else {
        keyType = "SANDBOX";
    }

    var id = document.getElementById("appid").value;
    setAuthHeader(client);
    client["Generate Keys"].post_applications_applicationId_generate_keys(
        {
            "applicationId": id,
            "Content-Type": "application/json",
            "body": {
                "keyType": keyType,
                "callbackUrl": document.getElementById("callbackUrl").value,
                "grantTypesToBeSupported": ["client_credentials", "password"]
                //TODO should be able to send supported grant types taken from UI
            }
        }, function (keys) {
            if(keys.status == 200){
                var jsonData = JSON.parse(keys.data)
                client["Generate Application Token"].post_applications_applicationId_generate_token(
                    {
                        "applicationId": id,
                        "Content-Type": "application/json",
                        "body": {
                            "consumerKey": jsonData.consumerKey,
                            "consumerSecret": jsonData.consumerSecret,
                            "validityPeriod": document.getElementById("validitytime").value,
                            "scopes": "",
                            "revokeToken": null
                        }
                    }, function (tokens) {
                        renderGeneratedKeys(keys, tokens, keyType);
                    }
                );
            }
        }
    );
};

var updateClick = function () {
    var message = "This functionality is not supported yet";
    noty({
        text: message,
        type: 'warning',
        dismissQueue: true,
        modal: true,
        progressBar: true,
        timeout: 3000,
        layout: 'top',
        theme: 'relax',
        maxVisible: 10,
    });
};

var renderGeneratedKeys = function (keys, tokens, keyType) {
    var compiledHtml, context;

    for (var j = 0; j < Object.keys(grantTypes).length; j++) {
        if ((keys.obj.callbackUrl == undefined || keys.obj.callbackUrl == "" ) &&
            (grantTypes[j].key == "authorization_code" || grantTypes[j].key == "implicit")) {
            grantTypes[j].selected = false;
            grantTypes[j].disabled = true;
        } else {
            grantTypes[j].selected = true;
            grantTypes[j].disabled = false;
        }
    }

    var jsonKeyData = JSON.parse(keys.data);
    var jsonTokenData = JSON.parse(tokens.data);
    context = {
        "keyType": keyType,
        "callbackUrl": jsonKeyData.callbackUrl,
        "grantTypes": grantTypes,
        "show_keys": false,
        "Key": jsonTokenData.accessToken,
        "ConsumerKey": jsonKeyData.consumerKey,
        "ConsumerSecret": jsonKeyData.consumerSecret,
        "username": "Username",
        "password": "Password",
        "basickey": window.btoa(jsonKeyData.consumerKey + ":" + jsonKeyData.consumerSecret),
        "ValidityTime": jsonTokenData.validityTime,
        "tokenScopes": jsonTokenData.tokenScopes,
        "provide_keys_form": false,
        "provide_keys": false,
        "gatewayurlendpoint": "(gatewayurl)/token"

    };

    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.application-keys", context, {
        onSuccess: function (renderedData) {
            if (context.keyType.toLowerCase() == "production") {
                $("#production").html(renderedData);
            } else {
                $("#sandbox").html(renderedData);
            }
        }, onFailure: function (message, e) {
        }
    })
};

var show_Keys = function (obj) {
    var parentCont = $(obj).parent().parent();
    if ($('#ConsumerKey', parentCont)[0].type == 'password') {
        $('#ConsumerKey', parentCont)[0].type = 'text';
        $('#ConsumerSecret', parentCont)[0].type = 'text';
        $('#Key', parentCont)[0].type = 'text';
        $('#show_keys', parentCont).html("Hide Keys");
    }
    else {
        $('#ConsumerKey', parentCont)[0].type = 'password';
        $('#ConsumerSecret', parentCont)[0].type = 'password';
        $('#Key', parentCont)[0].type = 'password';
        $('#show_keys', parentCont).html("Show Keys");
    }
};

function _renderActionButtons(data, type, row) {
    var btnClass = "btn btn-sm padding-reduce-on-grid-view deleteSub";
    if(!hasValidScopes("/subscriptions/{subscriptionId}", "delete")) {
      btnClass = "btn btn-sm padding-reduce-on-grid-view deleteSub not-active";
    }
    if (type === "display") {

        var deleteIcon1 = $("<i>").addClass("fw fw-ring fw-stack-2x");
        var deleteIcon2 = $("<i>").addClass("fw fw-delete fw-stack-1x");
        var deleteSpanIcon = $("<span>").addClass("fw-stack").append(deleteIcon1).append(deleteIcon2);
        var deleteSpanText = $("<span>").addClass("hidden-xs").text("Unsubscribe");
        var delete_button = $('<a>', {id: data, href: '#', 'data-id': data, title: 'delete'})
            .addClass(btnClass)
            .append(deleteSpanIcon)
            .append(deleteSpanText);
        return $('<div></div>').append(delete_button).html();
    } else {
        return data;
    }
}

var renderCompositeApi = function (data) {
    var id, name, version, provider;
    if (data) {
        id = data.id;
        name = data.name;
        version = data.version;
        provider = data.provider;
    }

    var callbacks = {
        onSuccess: function (renderedData) {
            $("#composite-api").html(renderedData);
            $(".api-name-icon").each(function () {
                var elem = $(this).next().children(".api-name");
                $(this).nametoChar({
                    nameElement: elem
                });
            });
        },
        onFailure: function (message, e) {
            var message = "Error occurred while listing composite api";
            noty({
            text: message,
            type: 'error',
            dismissQueue: true,
            modal: true,
            progressBar: true,
            timeout: 2000,
            layout: 'top',
            theme: 'relax',
            maxVisible: 10,
            });
        }
    };

    // draw create api view if composite API is not available for this application
    // draw api thumbnail view if composite API is available for this application
    var context = {
        compositeAPIAvailable: data != undefined,
        id: id,
        name: name,
        version: version,
        provider: provider
    };

    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.composite-api-tab", context, callbacks);
}

var createCompositeAPI = function () {
    var apiName = $("#composite-api-name").val();
    var apiContext = $("#composite-api-context").val();
    var apiVersion = $("#composite-api-version").val();
    var api = {
        name: apiName,
        context: apiContext,
        version: apiVersion,
        applicationId: document.getElementById("appid").value
    };

    setAuthHeader(client);
    client["CompositeAPI (Collection)"].post_composite_apis({
        "body": api,
        "Content-Type": "application/json"
    }, function (data) {
        if (data.status == 201) {
            renderCompositeApi(data.obj);
        }
    });
}
