/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var validateStep = {};
var validateInline = {};
var stepForwardFrom = {};
var stepBackFrom = {};
var policy = {};
var configuredOperations = [];

// Constants to define platform types available
var platformTypeConstants = {
    "ANDROID": "android",
    "IOS": "ios",
    "WINDOWS": "windows"
};

// Constants to define platform types ids.
var platformIds = {
    "ANDROID": 1,
    "IOS": 3,
    "WINDOWS": 2
};

// Constants to define Android Operation Constants
var androidOperationConstants = {
    "PASSCODE_POLICY_OPERATION": "passcode-policy",
    "PASSCODE_POLICY_OPERATION_CODE": "PASSCODE_POLICY",
    "CAMERA_OPERATION": "camera",
    "CAMERA_OPERATION_CODE": "CAMERA",
    "ENCRYPT_STORAGE_OPERATION": "encrypt-storage",
    "ENCRYPT_STORAGE_OPERATION_CODE": "ENCRYPT_STORAGE",
    "WIFI_OPERATION": "wifi",
    "WIFI_OPERATION_CODE": "WIFI"
};

// Constants to define Android Operation Constants
var windowsOperationConstants = {
    "PASSCODE_POLICY_OPERATION": "passcode-policy",
    "PASSCODE_POLICY_OPERATION_CODE": "PASSCODE_POLICY",
    "CAMERA_OPERATION": "camera",
    "CAMERA_OPERATION_CODE": "CAMERA",
    "ENCRYPT_STORAGE_OPERATION": "encrypt-storage",
    "ENCRYPT_STORAGE_OPERATION_CODE": "ENCRYPT_STORAGE"
};

// Constants to define iOS Operation Constants
var iosOperationConstants = {
    "PASSCODE_POLICY_OPERATION": "passcode-policy",
    "PASSCODE_POLICY_OPERATION_CODE": "PASSCODE_POLICY",
    "RESTRICTIONS_OPERATION": "restrictions",
    "RESTRICTIONS_OPERATION_CODE": "RESTRICTION",
    "WIFI_OPERATION": "wifi",
    "WIFI_OPERATION_CODE": "WIFI",
    "EMAIL_OPERATION": "email",
    "EMAIL_OPERATION_CODE": "EMAIL",
    "AIRPLAY_OPERATION": "airplay",
    "AIRPLAY_OPERATION_CODE": "AIR_PLAY",
    "LDAP_OPERATION": "ldap",
    "LDAP_OPERATION_CODE": "LDAP",
    "CALENDAR_OPERATION": "calendar",
    "CALENDAR_OPERATION_CODE": "CALDAV",
    "CALENDAR_SUBSCRIPTION_OPERATION": "calendar-subscription",
    "CALENDAR_SUBSCRIPTION_OPERATION_CODE": "CALENDAR_SUBSCRIPTION",
    "APN_OPERATION": "apn",
    "APN_OPERATION_CODE": "APN",
    "CELLULAR_OPERATION": "cellular",
    "CELLULAR_OPERATION_CODE": "CELLULAR"
};

/**
 * Method to update the visibility of grouped input.
 * @param domElement HTML grouped-input element with class name "grouped-input"
 */
var updateGroupedInputVisibility = function (domElement) {
    if ($(".parent-input:first", domElement).is(":checked")) {
        if ($(".grouped-child-input:first", domElement).hasClass("disabled")) {
            $(".grouped-child-input:first", domElement).removeClass("disabled");
        }
        $(".child-input", domElement).each(function () {
            $(this).prop('disabled', false);
        });
    } else {
        if (!$(".grouped-child-input:first", domElement).hasClass("disabled")) {
            $(".grouped-child-input:first", domElement).addClass("disabled");
        }
        $(".child-input", domElement).each(function () {
            $(this).prop('disabled', true);
        });
    }
};

validateInline["policy-name"] = function () {
    var policyName = $("input#policy-name-input").val();
    if (policyName && inputIsValidAgainstLength(policyName, 1, 30)) {
        $("#policyNameValidationText").removeClass("inline-warning");
    } else {
        $("#policyNameValidationText").addClass("inline-warning");
    }
};

$("#policy-name-input").focus(function(){
    $("#policyNameValidationText").removeClass("inline-warning");
});

$("#policy-name-input").blur(function(){
    validateInline["policy-name"]();
});

stepForwardFrom["policy-platform"] = function (actionButton) {
    policy["platform"] = $(actionButton).data("platform");
    policy["platformId"] = $(actionButton).data("platform-id");
    // updating next-page wizard title with selected platform
    $("#policy-profile-page-wizard-title").text("ADD " + policy["platform"] + " POLICY");

    var deviceType = policy["platform"];
    var hiddenOperationsByDeviceType = $("#hidden-operations-" + deviceType);
    var hiddenOperationsByDeviceTypeCacheKey = deviceType + "HiddenOperations";
    var hiddenOperationsByDeviceTypeSrc = hiddenOperationsByDeviceType.attr("src");

    setTimeout(
        function () {
            $.template(hiddenOperationsByDeviceTypeCacheKey, hiddenOperationsByDeviceTypeSrc, function (template) {
                var content = template();
                $(".wr-advance-operations").html(content);
                $(".wr-advance-operations li.grouped-input").each(function () {
                    updateGroupedInputVisibility(this);
                });
            });
        },
        250 // time delayed for the execution of above function, 250 milliseconds
    );
};

/**
 * Checks if provided number is valid against a range.
 *
 * @param numberInput Number Input
 * @param min Minimum Limit
 * @param max Maximum Limit
 * @returns {boolean} Returns true if input is within the specified range
 */
var inputIsValidAgainstRange = function (numberInput, min, max) {
    return (numberInput == min || (numberInput > min && numberInput < max) || numberInput == max);
};

/**
 * Checks if provided input is valid against RegEx input.
 *
 * @param regExp Regular expression
 * @param input Input string to check
 * @returns {boolean} Returns true if input matches RegEx
 */
var inputIsValidAgainstRegExp = function (regExp, input) {
    return regExp.test(input);
};

validateStep["policy-profile"] = function () {
    var validationStatusArray = [];
    var validationStatus;
    var operation;

    // starting validation process and updating validationStatus
    if (policy["platform"] == platformTypeConstants["ANDROID"]) {
        if (configuredOperations.length == 0) {
            // updating validationStatus
            validationStatus = {
                "error": true,
                "mainErrorMsg": "You cannot continue. Zero configured features."
            };
            // updating validationStatusArray with validationStatus
            validationStatusArray.push(validationStatus);
        } else {
            // validating each and every configured Operation
            // Validating PASSCODE_POLICY
            if ($.inArray(androidOperationConstants["PASSCODE_POLICY_OPERATION_CODE"], configuredOperations) != -1) {
                // if PASSCODE_POLICY is configured
                operation = androidOperationConstants["PASSCODE_POLICY_OPERATION"];
                // initializing continueToCheckNextInputs to true
                var continueToCheckNextInputs = true;

                // validating first input: passcodePolicyMaxPasscodeAgeInDays
                var passcodePolicyMaxPasscodeAgeInDays = $("input#passcode-policy-max-passcode-age-in-days").val();
                if (passcodePolicyMaxPasscodeAgeInDays) {
                    if (!$.isNumeric(passcodePolicyMaxPasscodeAgeInDays)) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Provided passcode age is not a number.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    } else if (!inputIsValidAgainstRange(passcodePolicyMaxPasscodeAgeInDays, 1, 730)) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Provided passcode age is not with in the range of 1-to-730.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    }
                }

                // validating second and last input: passcodePolicyPasscodeHistory
                if (continueToCheckNextInputs) {
                    var passcodePolicyPasscodeHistory = $("input#passcode-policy-passcode-history").val();
                    if (passcodePolicyPasscodeHistory) {
                        if (!$.isNumeric(passcodePolicyPasscodeHistory)) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Provided passcode history is not a number.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (!inputIsValidAgainstRange(passcodePolicyPasscodeHistory, 1, 50)) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Provided passcode history is not with in the range of 1-to-50.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating CAMERA
            if ($.inArray(androidOperationConstants["CAMERA_OPERATION_CODE"], configuredOperations) != -1) {
                // if CAMERA is configured
                operation = androidOperationConstants["CAMERA_OPERATION"];
                // updating validationStatus
                validationStatus = {
                    "error": false,
                    "okFeature": operation
                };
                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating ENCRYPT_STORAGE
            if ($.inArray(androidOperationConstants["ENCRYPT_STORAGE_OPERATION_CODE"], configuredOperations) != -1) {
                // if ENCRYPT_STORAGE is configured
                operation = androidOperationConstants["ENCRYPT_STORAGE_OPERATION"];
                // updating validationStatus
                validationStatus = {
                    "error": false,
                    "okFeature": operation
                };
                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating WIFI
            if ($.inArray(androidOperationConstants["WIFI_OPERATION_CODE"], configuredOperations) != -1) {
                // if WIFI is configured
                operation = androidOperationConstants["WIFI_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                var wifiSSID = $("input#wifi-ssid").val();
                if (!wifiSSID) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "WIFI SSID is not given. You cannot proceed.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
        }
    }
    if (policy["platform"] == platformTypeConstants["WINDOWS"]) {
        if (configuredOperations.length == 0) {
            // updating validationStatus
            validationStatus = {
                "error": true,
                "mainErrorMsg": "You cannot continue. Zero configured features."
            };
            // updating validationStatusArray with validationStatus
            validationStatusArray.push(validationStatus);
        } else {
            // validating each and every configured Operation
            // Validating PASSCODE_POLICY
            if ($.inArray(windowsOperationConstants["PASSCODE_POLICY_OPERATION_CODE"], configuredOperations) != -1) {
                // if PASSCODE_POLICY is configured
                operation = windowsOperationConstants["PASSCODE_POLICY_OPERATION"];
                // initializing continueToCheckNextInputs to true
                var continueToCheckNextInputs = true;

                // validating first input: passcodePolicyMaxPasscodeAgeInDays
                var passcodePolicyMaxPasscodeAgeInDays = $("input#passcode-policy-max-passcode-age-in-days").val();
                if (passcodePolicyMaxPasscodeAgeInDays) {
                    if (!$.isNumeric(passcodePolicyMaxPasscodeAgeInDays)) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Provided passcode age is not a number.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    } else if (!inputIsValidAgainstRange(passcodePolicyMaxPasscodeAgeInDays, 1, 730)) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Provided passcode age is not with in the range of 1-to-730.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    }
                }

                // validating second and last input: passcodePolicyPasscodeHistory
                if (continueToCheckNextInputs) {
                    var passcodePolicyPasscodeHistory = $("input#passcode-policy-passcode-history").val();
                    if (passcodePolicyPasscodeHistory) {
                        if (!$.isNumeric(passcodePolicyPasscodeHistory)) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Provided passcode history is not a number.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (!inputIsValidAgainstRange(passcodePolicyPasscodeHistory, 1, 50)) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Provided passcode history is not with in the range of 1-to-50.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating CAMERA
            if ($.inArray(windowsOperationConstants["CAMERA_OPERATION_CODE"], configuredOperations) != -1) {
                // if CAMERA is configured
                operation = windowsOperationConstants["CAMERA_OPERATION"];
                // updating validationStatus
                validationStatus = {
                    "error": false,
                    "okFeature": operation
                };
                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating ENCRYPT_STORAGE
            if ($.inArray(windowsOperationConstants["ENCRYPT_STORAGE_OPERATION_CODE"], configuredOperations) != -1) {
                // if ENCRYPT_STORAGE is configured
                operation = windowsOperationConstants["ENCRYPT_STORAGE_OPERATION"];
                // updating validationStatus
                validationStatus = {
                    "error": false,
                    "okFeature": operation
                };
                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }

        }
    } else if (policy["platform"] == platformTypeConstants["IOS"]) {
        if (configuredOperations.length == 0) {
            // updating validationStatus
            validationStatus = {
                "error": true,
                "mainErrorMsg": "You cannot continue. Zero configured features."
            };
            // updating validationStatusArray with validationStatus
            validationStatusArray.push(validationStatus);
        } else {
            // validating each and every configured Operation
            // Validating PASSCODE_POLICY
            if ($.inArray(iosOperationConstants["PASSCODE_POLICY_OPERATION_CODE"], configuredOperations) != -1) {
                // if PASSCODE_POLICY is configured
                operation = iosOperationConstants["PASSCODE_POLICY_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                // validating first input: passcodePolicyMaxPasscodeAgeInDays
                passcodePolicyMaxPasscodeAgeInDays = $("input#passcode-policy-max-passcode-age-in-days").val();
                if (passcodePolicyMaxPasscodeAgeInDays) {
                    if (!$.isNumeric(passcodePolicyMaxPasscodeAgeInDays)) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Provided passcode age is not a number.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    } else if (!inputIsValidAgainstRange(passcodePolicyMaxPasscodeAgeInDays, 1, 730)) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Provided passcode age is not with in the range of 1-to-730.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    }
                }

                // validating second and last input: passcodePolicyPasscodeHistory
                if (continueToCheckNextInputs) {
                    passcodePolicyPasscodeHistory = $("input#passcode-policy-passcode-history").val();
                    if (passcodePolicyPasscodeHistory) {
                        if (!$.isNumeric(passcodePolicyPasscodeHistory)) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Provided passcode history is not a number.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (!inputIsValidAgainstRange(passcodePolicyPasscodeHistory, 1, 50)) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Provided passcode history is not with in the range of 1-to-50.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating RESTRICTIONS
            if ($.inArray(iosOperationConstants["RESTRICTIONS_OPERATION_CODE"], configuredOperations) != -1) {
                // if RESTRICTION is configured
                operation = iosOperationConstants["RESTRICTIONS_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                // getting input values to be validated
                var restrictionsAutonomousSingleAppModePermittedAppIDsGridChildInputs =
                    "div#restrictions-autonomous-single-app-mode-permitted-app-ids .child-input";
                if ($(restrictionsAutonomousSingleAppModePermittedAppIDsGridChildInputs).length > 0) {
                    var childInput;
                    var childInputArray = [];
                    var emptyChildInputCount = 0;
                    var duplicatesExist = false;
                    // looping through each child input
                    $(restrictionsAutonomousSingleAppModePermittedAppIDsGridChildInputs).each(function () {
                        childInput = $(this).val();
                        childInputArray.push(childInput);
                        if (!childInput) {
                            // if child input field is empty
                            emptyChildInputCount++;
                        }
                    });
                    // checking for duplicates
                    var initialChildInputArrayLength = childInputArray.length;
                    if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                        var m, poppedChildInput;
                        for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                            poppedChildInput = childInputArray.pop();
                            var n;
                            for (n = 0; n < childInputArray.length; n++) {
                                if (poppedChildInput == childInputArray[n]) {
                                    duplicatesExist = true;
                                    break;
                                }
                            }
                            if (duplicatesExist) {
                                break;
                            }
                        }
                    }
                    // updating validationStatus
                    if (emptyChildInputCount > 0) {
                        // if empty child inputs are present
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "One or more permitted App ID entries in " +
                                           "Autonomous Single App Mode are empty.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    } else if (duplicatesExist) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Duplicate values exist with permitted App ID entries in " +
                                           "Autonomous Single App Mode.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating WIFI
            if ($.inArray(iosOperationConstants["WIFI_OPERATION_CODE"], configuredOperations) != -1) {
                // if WIFI is configured
                operation = iosOperationConstants["WIFI_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                // getting input values to be validated
                wifiSSID = $("input#wifi-ssid").val();
                var wifiDomainName = $("input#wifi-domain-name").val();
                if (!wifiSSID && !wifiDomainName) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "Both Wi-Fi SSID and Wi-Fi Domain Name are not given. You cannot proceed.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                }

                if (continueToCheckNextInputs) {
                    // getting proxy-setup value
                    var wifiProxyType = $("select#wifi-proxy-type").find("option:selected").attr("value");
                    if (wifiProxyType == "Manual") {
                        // adds up additional fields to be validated
                        var wifiProxyServer = $("input#wifi-proxy-server").val();
                        if (!wifiProxyServer) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Wi-Fi Proxy Server is required. You cannot proceed.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }

                        if (continueToCheckNextInputs) {
                            var wifiProxyPort = $("input#wifi-proxy-port").val();
                            if (!wifiProxyPort) {
                                validationStatus = {
                                    "error": true,
                                    "subErrorMsg": "Wi-Fi Proxy Port is required. You cannot proceed.",
                                    "erroneousFeature": operation
                                };
                                continueToCheckNextInputs = false;
                            } else if (!$.isNumeric(wifiProxyPort)) {
                                validationStatus = {
                                    "error": true,
                                    "subErrorMsg": "Wi-Fi Proxy Port requires a number input.",
                                    "erroneousFeature": operation
                                };
                                continueToCheckNextInputs = false;
                            } else if (!inputIsValidAgainstRange(wifiProxyPort, 0, 65535)) {
                                validationStatus = {
                                    "error": true,
                                    "subErrorMsg": "Wi-Fi Proxy Port is not within the range " +
                                                   "of valid port numbers.",
                                    "erroneousFeature": operation
                                };
                                continueToCheckNextInputs = false;
                            }
                        }
                    }
                }

                if (continueToCheckNextInputs) {
                    // getting encryption-type value
                    var wifiEncryptionType = $("select#wifi-encryption-type").find("option:selected").attr("value");
                    if (wifiEncryptionType != "None") {
                        var wifiPayloadCertificateAnchorUUIDsGridChildInputs =
                            "div#wifi-payload-certificate-anchor-uuids .child-input";
                        if ($(wifiPayloadCertificateAnchorUUIDsGridChildInputs).length > 0) {
                            emptyChildInputCount = 0;
                            childInputArray = [];
                            duplicatesExist = false;
                            // looping through each child input
                            $(wifiPayloadCertificateAnchorUUIDsGridChildInputs).each(function () {
                                childInput = $(this).val();
                                childInputArray.push(childInput);
                                if (!childInput) {
                                    // if child input field is empty
                                    emptyChildInputCount++;
                                }
                            });
                            // checking for duplicates
                            initialChildInputArrayLength = childInputArray.length;
                            if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                                for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                                    poppedChildInput = childInputArray.pop();
                                    for (n = 0; n < childInputArray.length; n++) {
                                        if (poppedChildInput == childInputArray[n]) {
                                            duplicatesExist = true;
                                            break;
                                        }
                                    }
                                    if (duplicatesExist) {
                                        break;
                                    }
                                }
                            }
                            // updating validationStatus
                            if (emptyChildInputCount > 0) {
                                // if empty child inputs are present
                                validationStatus = {
                                    "error": true,
                                    "subErrorMsg": "One or more Payload Certificate " +
                                                   "Anchor UUIDs are empty.",
                                    "erroneousFeature": operation
                                };
                                continueToCheckNextInputs = false;
                            } else if (duplicatesExist) {
                                validationStatus = {
                                    "error": true,
                                    "subErrorMsg": "Duplicate values exist " +
                                                   "with Payload Certificate Anchor UUIDs.",
                                    "erroneousFeature": operation
                                };
                                continueToCheckNextInputs = false;
                            }
                        }

                        if (continueToCheckNextInputs) {
                            var wifiTLSTrustedServerNamesGridChildInputs =
                                "div#wifi-tls-trusted-server-names .child-input";
                            if ($(wifiTLSTrustedServerNamesGridChildInputs).length > 0) {
                                emptyChildInputCount = 0;
                                childInputArray = [];
                                duplicatesExist = false;
                                // looping through each child input
                                $(wifiTLSTrustedServerNamesGridChildInputs).each(function () {
                                    childInput = $(this).val();
                                    childInputArray.push(childInput);
                                    if (!childInput) {
                                        // if child input field is empty
                                        emptyChildInputCount++;
                                    }
                                });
                                // checking for duplicates
                                initialChildInputArrayLength = childInputArray.length;
                                if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                                    for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                                        poppedChildInput = childInputArray.pop();
                                        for (n = 0; n < childInputArray.length; n++) {
                                            if (poppedChildInput == childInputArray[n]) {
                                                duplicatesExist = true;
                                                break;
                                            }
                                        }
                                        if (duplicatesExist) {
                                            break;
                                        }
                                    }
                                }
                                // updating validationStatus
                                if (emptyChildInputCount > 0) {
                                    // if empty child inputs are present
                                    validationStatus = {
                                        "error": true,
                                        "subErrorMsg": "One or more TLS Trusted Server Names are empty.",
                                        "erroneousFeature": operation
                                    };
                                    continueToCheckNextInputs = false;
                                } else if (duplicatesExist) {
                                    validationStatus = {
                                        "error": true,
                                        "subErrorMsg": "Duplicate values exist " +
                                                       "with TLS Trusted Server Names.",
                                        "erroneousFeature": operation
                                    };
                                    continueToCheckNextInputs = false;
                                }
                            }
                        }
                    }
                }

                if (continueToCheckNextInputs) {
                    var wifiRoamingConsortiumOIsGridChildInputs = "div#wifi-roaming-consortium-ois .child-input";
                    if ($(wifiRoamingConsortiumOIsGridChildInputs).length > 0) {
                        emptyChildInputCount = 0;
                        var outOfAllowedLengthCount = 0;
                        var invalidAgainstRegExCount = 0;
                        childInputArray = [];
                        duplicatesExist = false;
                        // looping through each child input
                        $(wifiRoamingConsortiumOIsGridChildInputs).each(function () {
                            childInput = $(this).val();
                            childInputArray.push(childInput);
                            if (!childInput) {
                                // if child input field is empty
                                emptyChildInputCount++;
                            } else if (!inputIsValidAgainstLength(childInput, 6, 6) && !inputIsValidAgainstLength(childInput, 10, 10)) {
                                outOfAllowedLengthCount++;
                            } else if (!inputIsValidAgainstRegExp(/^[a-fA-F0-9]+$/, childInput)) {
                                invalidAgainstRegExCount++;
                            }
                        });
                        // checking for duplicates
                        initialChildInputArrayLength = childInputArray.length;
                        if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                            for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                                poppedChildInput = childInputArray.pop();
                                for (n = 0; n < childInputArray.length; n++) {
                                    if (poppedChildInput == childInputArray[n]) {
                                        duplicatesExist = true;
                                        break;
                                    }
                                }
                                if (duplicatesExist) {
                                    break;
                                }
                            }
                        }
                        // updating validationStatus
                        if (emptyChildInputCount > 0) {
                            // if empty child inputs are present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more Roaming Consortium OIs are empty.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (outOfAllowedLengthCount > 0) {
                            // if outOfMaxAllowedLength input is present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more Roaming Consortium OIs " +
                                               "are out of allowed length.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (invalidAgainstRegExCount > 0) {
                            // if invalid inputs in terms of hexadecimal format are present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more Roaming Consortium OIs " +
                                               "contain non-hexadecimal characters.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (duplicatesExist) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Duplicate values exist with Roaming Consortium OIs.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                if (continueToCheckNextInputs) {
                    var wifiNAIRealmNamesGridChildInputs = "div#wifi-nai-realm-names .child-input";
                    if ($(wifiNAIRealmNamesGridChildInputs).length > 0) {
                        emptyChildInputCount = 0;
                        childInputArray = [];
                        duplicatesExist = false;
                        // looping through each child input
                        $(wifiNAIRealmNamesGridChildInputs).each(function () {
                            childInput = $(this).val();
                            childInputArray.push(childInput);
                            if (!childInput) {
                                // if child input field is empty
                                emptyChildInputCount++;
                            }
                        });
                        // checking for duplicates
                        initialChildInputArrayLength = childInputArray.length;
                        if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                            for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                                poppedChildInput = childInputArray.pop();
                                for (n = 0; n < childInputArray.length; n++) {
                                    if (poppedChildInput == childInputArray[n]) {
                                        duplicatesExist = true;
                                        break;
                                    }
                                }
                                if (duplicatesExist) {
                                    break;
                                }
                            }
                        }
                        // updating validationStatus
                        if (emptyChildInputCount > 0) {
                            // if empty child inputs are present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more NAI Realm Names are empty.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (duplicatesExist) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Duplicate values exist with NAI Realm Names.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                if (continueToCheckNextInputs) {
                    var wifiMCCAndMNCsGridChildInputs = "div#wifi-mcc-and-mncs .child-input";
                    if ($(wifiMCCAndMNCsGridChildInputs).length > 0) {
                        var childInputCount = 0;
                        var stringPair;
                        emptyChildInputCount = 0;
                        outOfAllowedLengthCount = 0;
                        var notNumericInputCount = 0;
                        childInputArray = [];
                        duplicatesExist = false;
                        // looping through each child input
                        $(wifiMCCAndMNCsGridChildInputs).each(function () {
                            childInput = $(this).val();
                            // pushing each string pair to childInputArray
                            childInputCount++;
                            if (childInputCount % 2 == 1) {
                                // initialize stringPair value
                                stringPair = "";
                                // append first part of the string
                                stringPair += childInput;
                            } else {
                                // append second part of the string
                                stringPair += childInput;
                                childInputArray.push(stringPair);
                            }
                            // updating emptyChildInputCount & outOfAllowedLengthCount
                            if (!childInput) {
                                // if child input field is empty
                                emptyChildInputCount++;
                            } else if (!$.isNumeric(childInput)) {
                                notNumericInputCount++;
                            } else if (!inputIsValidAgainstLength(childInput, 3, 3)) {
                                outOfAllowedLengthCount++;
                            }
                        });
                        // checking for duplicates
                        initialChildInputArrayLength = childInputArray.length;
                        if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                            for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                                poppedChildInput = childInputArray.pop();
                                for (n = 0; n < childInputArray.length; n++) {
                                    if (poppedChildInput == childInputArray[n]) {
                                        duplicatesExist = true;
                                        break;
                                    }
                                }
                                if (duplicatesExist) {
                                    break;
                                }
                            }
                        }
                        // updating validationStatus
                        if (emptyChildInputCount > 0) {
                            // if empty child inputs are present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more MCC/MNC pairs are empty.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (notNumericInputCount > 0) {
                            // if notNumeric input is present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more MCC/MNC pairs are not numeric.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (outOfAllowedLengthCount > 0) {
                            // if outOfAllowedLength input is present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more MCC/MNC pairs " +
                                               "do not fulfill the accepted length of 6 digits.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (duplicatesExist) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Duplicate values exist with MCC/MNC pairs.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating EMAIL
            if ($.inArray(iosOperationConstants["EMAIL_OPERATION_CODE"], configuredOperations) != -1) {
                // if EMAIL is configured
                operation = iosOperationConstants["EMAIL_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                var emailAddress = $("input#email-address").val();
                if (emailAddress && !inputIsValidAgainstRegExp(/^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/, emailAddress)) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "Email Address is not valid.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                }

                if (continueToCheckNextInputs) {
                    var emailIncomingMailServerHostname = $("input#email-incoming-mail-server-hostname").val();
                    if (!emailIncomingMailServerHostname) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Incoming Mail Server Hostname is empty. You cannot proceed.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    }
                }

                if (continueToCheckNextInputs) {
                    var emailIncomingMailServerPort = $("input#email-incoming-mail-server-port").val();
                    if (emailIncomingMailServerPort && emailIncomingMailServerPort != '') {
                        if (!$.isNumeric(emailIncomingMailServerPort)) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Incoming Mail Server Port requires a number input.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (!inputIsValidAgainstRange(emailIncomingMailServerPort, 0, 65535)) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Incoming Mail Server Port is not within the range " +
                                               "of valid port numbers.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                if (continueToCheckNextInputs) {
                    var emailOutgoingMailServerHostname = $("input#email-outgoing-mail-server-hostname").val();
                    if (!emailOutgoingMailServerHostname) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Outgoing Mail Server Hostname is empty. You cannot proceed.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    }
                }

                if (continueToCheckNextInputs) {
                    var emailOutgoingMailServerPort = $("input#email-outgoing-mail-server-port").val();
                    if (emailOutgoingMailServerPort) {
                        if (!$.isNumeric(emailOutgoingMailServerPort)) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Outgoing Mail Server Port requires a number input.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (!inputIsValidAgainstRange(emailOutgoingMailServerPort, 0, 65535)) {
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Outgoing Mail Server Port is not within the range " +
                                               "of valid port numbers.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating AIRPLAY
            if ($.inArray(iosOperationConstants["AIRPLAY_OPERATION_CODE"], configuredOperations) != -1) {
                // if AIRPLAY is configured
                operation = iosOperationConstants["AIRPLAY_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                var airplayCredentialsGridChildInputs = "div#airplay-credentials .child-input";
                var airplayDestinationsGridChildInputs = "div#airplay-destinations .child-input";
                if ($(airplayCredentialsGridChildInputs).length == 0 &&
                    $(airplayDestinationsGridChildInputs).length == 0) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "AirPlay settings have zero configurations attached.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                }

                if (continueToCheckNextInputs) {
                    if ($(airplayCredentialsGridChildInputs).length > 0) {
                        childInputCount = 0;
                        childInputArray = [];
                        emptyChildInputCount = 0;
                        duplicatesExist = false;
                        // looping through each child input
                        $(airplayCredentialsGridChildInputs).each(function () {
                            childInputCount++;
                            if (childInputCount % 2 == 1) {
                                // if child input is of first column
                                childInput = $(this).val();
                                childInputArray.push(childInput);
                                // updating emptyChildInputCount
                                if (!childInput) {
                                    // if child input field is empty
                                    emptyChildInputCount++;
                                }
                            }
                        });
                        // checking for duplicates
                        initialChildInputArrayLength = childInputArray.length;
                        if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                            for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                                poppedChildInput = childInputArray.pop();
                                for (n = 0; n < childInputArray.length; n++) {
                                    if (poppedChildInput == childInputArray[n]) {
                                        duplicatesExist = true;
                                        break;
                                    }
                                }
                                if (duplicatesExist) {
                                    break;
                                }
                            }
                        }
                        // updating validationStatus
                        if (emptyChildInputCount > 0) {
                            // if empty child inputs are present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more Device Names of " +
                                               "AirPlay Credentials are empty.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (duplicatesExist) {
                            // if duplicate input is present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Duplicate values exist with " +
                                               "Device Names of AirPlay Credentials.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                if (continueToCheckNextInputs) {
                    if ($(airplayDestinationsGridChildInputs).length > 0) {
                        childInputArray = [];
                        emptyChildInputCount = 0;
                        invalidAgainstRegExCount = 0;
                        duplicatesExist = false;
                        // looping through each child input
                        $(airplayDestinationsGridChildInputs).each(function () {
                            childInput = $(this).val();
                            childInputArray.push(childInput);
                            // updating emptyChildInputCount
                            if (!childInput) {
                                // if child input field is empty
                                emptyChildInputCount++;
                            } else if (!inputIsValidAgainstRegExp(
                                    /([a-z|A-Z|0-9][a-z|A-Z|0-9][:]){5}([a-z|A-Z|0-9][a-z|A-Z|0-9])$/, childInput)) {
                                // if child input field is invalid against RegEx
                                invalidAgainstRegExCount++
                            }
                        });
                        // checking for duplicates
                        initialChildInputArrayLength = childInputArray.length;
                        if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                            for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                                poppedChildInput = childInputArray.pop();
                                for (n = 0; n < childInputArray.length; n++) {
                                    if (poppedChildInput == childInputArray[n]) {
                                        duplicatesExist = true;
                                        break;
                                    }
                                }
                                if (duplicatesExist) {
                                    break;
                                }
                            }
                        }
                        // updating validationStatus
                        if (emptyChildInputCount > 0) {
                            // if empty child inputs are present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more AirPlay Destination fields are empty.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (invalidAgainstRegExCount > 0) {
                            // if invalidAgainstRegEx inputs are present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more AirPlay Destination fields " +
                                               "do not fulfill expected format.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (duplicatesExist) {
                            // if duplicate input is present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Duplicate values exist with AirPlay Destinations.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating LDAP
            if ($.inArray(iosOperationConstants["LDAP_OPERATION_CODE"], configuredOperations) != -1) {
                // if LDAP is configured
                operation = iosOperationConstants["LDAP_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                var ldapAccountHostname = $("input#ldap-account-hostname").val();
                if (!ldapAccountHostname) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "LDAP Account Hostname URL is empty. You cannot proceed.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                }

                if (continueToCheckNextInputs) {
                    var ldapSearchSettingsGridChildInputs = "div#ldap-search-settings .child-input";
                    if ($(ldapSearchSettingsGridChildInputs).length > 0) {
                        childInputCount = 0;
                        childInputArray = [];
                        emptyChildInputCount = 0;
                        duplicatesExist = false;
                        // looping through each child input
                        $(ldapSearchSettingsGridChildInputs).each(function () {
                            childInputCount++;
                            if (childInputCount % 3 == 2) {
                                // if child input is of second column
                                childInput = $(this).find("option:selected").attr("value");
                                stringPair = "";
                                stringPair += (childInput + " ");
                            } else if (childInputCount % 3 == 0) {
                                // if child input is of third column
                                childInput = $(this).val();
                                stringPair += childInput;
                                childInputArray.push(stringPair);
                                // updating emptyChildInputCount
                                if (!childInput) {
                                    // if child input field is empty
                                    emptyChildInputCount++;
                                }
                            }
                        });
                        // checking for duplicates
                        initialChildInputArrayLength = childInputArray.length;
                        if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                            for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                                poppedChildInput = childInputArray.pop();
                                for (n = 0; n < childInputArray.length; n++) {
                                    if (poppedChildInput == childInputArray[n]) {
                                        duplicatesExist = true;
                                        break;
                                    }
                                }
                                if (duplicatesExist) {
                                    break;
                                }
                            }
                        }
                        // updating validationStatus
                        if (emptyChildInputCount > 0) {
                            // if empty child inputs are present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more Search Setting Scope fields are empty.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (duplicatesExist) {
                            // if duplicate input is present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Duplicate values exist with " +
                                               "Search Setting Search Base and Scope pairs.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating CALENDAR
            if ($.inArray(iosOperationConstants["CALENDAR_OPERATION_CODE"], configuredOperations) != -1) {
                // if CALENDAR is configured
                operation = iosOperationConstants["CALENDAR_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                var calendarAccountHostname = $("input#calendar-account-hostname").val();
                if (!calendarAccountHostname) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "Account Hostname URL is empty. You cannot proceed.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                }

                if (continueToCheckNextInputs) {
                    var calendarAccountPort = $("input#calendar-account-port").val();
                    if (!calendarAccountPort) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Account Port is empty. You cannot proceed.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    } else if (!$.isNumeric(calendarAccountPort)) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Account Port requires a number input.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    } else if (!inputIsValidAgainstRange(calendarAccountPort, 0, 65535)) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Account Port is not within the range " +
                                           "of valid port numbers.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating CALENDAR_SUBSCRIPTION
            if ($.inArray(iosOperationConstants["CALENDAR_SUBSCRIPTION_OPERATION_CODE"], configuredOperations) != -1) {
                // if CALENDAR_SUBSCRIPTION is configured
                operation = iosOperationConstants["CALENDAR_SUBSCRIPTION_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                var calendarSubscriptionHostname = $("input#calendar-subscription-hostname").val();
                if (!calendarSubscriptionHostname) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "Account Hostname URL is empty. You cannot proceed.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating APN
            if ($.inArray(iosOperationConstants["APN_OPERATION_CODE"], configuredOperations) != -1) {
                // if APN is configured
                operation = iosOperationConstants["APN_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                var apnConfigurationsGridChildInputs = "div#apn-configurations .child-input";
                if ($(apnConfigurationsGridChildInputs).length == 0) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "APN Settings have zero configurations attached.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                } else if ($(apnConfigurationsGridChildInputs).length > 0) {
                    childInputCount = 0;
                    childInputArray = [];
                    // checking empty APN field count
                    emptyChildInputCount = 0;
                    duplicatesExist = false;
                    // looping through each child input
                    $(apnConfigurationsGridChildInputs).each(function () {
                        childInputCount++;
                        //if (childInputCount % 5 == 1) {
                        // if child input is of first column
                        childInput = $(this).val();
                        childInputArray.push(childInput);
                        // updating emptyChildInputCount
                        if (!childInput) {
                            // if child input field is empty
                            emptyChildInputCount++;
                        }
                        //}
                    });
                    // checking for duplicates
                    initialChildInputArrayLength = childInputArray.length;
                    if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                        for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                            poppedChildInput = childInputArray.pop();
                            for (n = 0; n < childInputArray.length; n++) {
                                if (poppedChildInput == childInputArray[n]) {
                                    duplicatesExist = true;
                                    break;
                                }
                            }
                            if (duplicatesExist) {
                                break;
                            }
                        }
                    }
                    // updating validationStatus
                    if (emptyChildInputCount > 0) {
                        // if empty child inputs are present
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "One or more APN fields of Configurations are empty.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    } else if (duplicatesExist) {
                        // if duplicate input is present
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Duplicate values exist with " +
                                           "APN fields of Configurations.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
            // Validating CELLULAR
            if ($.inArray(iosOperationConstants["CELLULAR_OPERATION_CODE"], configuredOperations) != -1) {
                // if CELLULAR is configured
                operation = iosOperationConstants["CELLULAR_OPERATION"];
                // initializing continueToCheckNextInputs to true
                continueToCheckNextInputs = true;

                var cellularAttachAPNName = $("input#cellular-attach-apn-name").val();
                if (!cellularAttachAPNName) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "Cellular Configuration Name is empty. You cannot proceed.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                }

                if (continueToCheckNextInputs) {
                    var cellularAPNConfigurationsGridChildInputs = "div#cellular-apn-configurations .child-input";
                    if ($(cellularAPNConfigurationsGridChildInputs).length > 0) {
                        childInputCount = 0;
                        childInputArray = [];
                        // checking empty APN field count
                        emptyChildInputCount = 0;
                        duplicatesExist = false;
                        // looping through each child input
                        $(cellularAPNConfigurationsGridChildInputs).each(function () {
                            childInputCount++;
                            if (childInputCount % 6 == 1) {
                                // if child input is of first column
                                childInput = $(this).val();
                                childInputArray.push(childInput);
                                // updating emptyChildInputCount
                                if (!childInput) {
                                    // if child input field is empty
                                    emptyChildInputCount++;
                                }
                            }
                        });
                        // checking for duplicates
                        initialChildInputArrayLength = childInputArray.length;
                        if (emptyChildInputCount == 0 && initialChildInputArrayLength > 1) {
                            for (m = 0; m < (initialChildInputArrayLength - 1); m++) {
                                poppedChildInput = childInputArray.pop();
                                for (n = 0; n < childInputArray.length; n++) {
                                    if (poppedChildInput == childInputArray[n]) {
                                        duplicatesExist = true;
                                        break;
                                    }
                                }
                                if (duplicatesExist) {
                                    break;
                                }
                            }
                        }
                        // updating validationStatus
                        if (emptyChildInputCount > 0) {
                            // if empty child inputs are present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "One or more APN fields of APN Configurations are empty.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        } else if (duplicatesExist) {
                            // if duplicate input is present
                            validationStatus = {
                                "error": true,
                                "subErrorMsg": "Duplicate values exist with " +
                                               "APN fields of APN Configurations.",
                                "erroneousFeature": operation
                            };
                            continueToCheckNextInputs = false;
                        }
                    }
                }

                // at-last, if the value of continueToCheckNextInputs is still true
                // this means that no error is found
                if (continueToCheckNextInputs) {
                    validationStatus = {
                        "error": false,
                        "okFeature": operation
                    };
                }

                // updating validationStatusArray with validationStatus
                validationStatusArray.push(validationStatus);
            }
        }
    }
    // ending validation process

    // start taking specific notifying actions upon validation
    var wizardIsToBeContinued;
    var errorCount = 0;
    var mainErrorMsgWrapper, mainErrorMsg,
        subErrorMsgWrapper, subErrorMsg, subErrorIcon, subOkIcon, featureConfiguredIcon;
    var i;
    for (i = 0; i < validationStatusArray.length; i++) {
        validationStatus = validationStatusArray[i];
        if (validationStatus["error"]) {
            errorCount++;
            if (validationStatus["mainErrorMsg"]) {
                mainErrorMsgWrapper = "#policy-profile-main-error-msg";
                mainErrorMsg = mainErrorMsgWrapper + " span";
                $(mainErrorMsg).text(validationStatus["mainErrorMsg"]);
                $(mainErrorMsgWrapper).removeClass("hidden");
            } else if (validationStatus["subErrorMsg"]) {
                subErrorMsgWrapper = "#" + validationStatus["erroneousFeature"] + "-feature-error-msg";
                subErrorMsg = subErrorMsgWrapper + " span";
                subErrorIcon = "#" + validationStatus["erroneousFeature"] + "-error";
                subOkIcon = "#" + validationStatus["erroneousFeature"] + "-ok";
                featureConfiguredIcon = "#" + validationStatus["erroneousFeature"] + "-configured";
                // hiding featureConfiguredState as the first step
                if (!$(featureConfiguredIcon).hasClass("hidden")) {
                    $(featureConfiguredIcon).addClass("hidden");
                }
                // updating error state and corresponding messages
                $(subErrorMsg).text(validationStatus["subErrorMsg"]);
                if ($(subErrorMsgWrapper).hasClass("hidden")) {
                    $(subErrorMsgWrapper).removeClass("hidden");
                }
                if (!$(subOkIcon).hasClass("hidden")) {
                    $(subOkIcon).addClass("hidden");
                }
                if ($(subErrorIcon).hasClass("hidden")) {
                    $(subErrorIcon).removeClass("hidden");
                }
            }
        } else {
            if (validationStatus["okFeature"]) {
                subErrorMsgWrapper = "#" + validationStatus["okFeature"] + "-feature-error-msg";
                subErrorIcon = "#" + validationStatus["okFeature"] + "-error";
                subOkIcon = "#" + validationStatus["okFeature"] + "-ok";
                featureConfiguredIcon = "#" + validationStatus["okFeature"] + "-configured";
                // hiding featureConfiguredState as the first step
                if (!$(featureConfiguredIcon).hasClass("hidden")) {
                    $(featureConfiguredIcon).addClass("hidden");
                }
                // updating success state and corresponding messages
                if (!$(subErrorMsgWrapper).hasClass("hidden")) {
                    $(subErrorMsgWrapper).addClass("hidden");
                }
                if (!$(subErrorIcon).hasClass("hidden")) {
                    $(subErrorIcon).addClass("hidden");
                }
                if ($(subOkIcon).hasClass("hidden")) {
                    $(subOkIcon).removeClass("hidden");
                }
            }
        }
    }

    wizardIsToBeContinued = (errorCount == 0);
    return wizardIsToBeContinued;
};

stepForwardFrom["policy-profile"] = function () {
    policy["profile"] = operationModule.generateProfile(policy["platform"], configuredOperations);
    // updating next-page wizard title with selected platform
    $("#policy-criteria-page-wizard-title").text("ADD " + policy["platform"] + " POLICY");
    // updating ownership type options according to platform
    if (policy["platform"] == platformTypeConstants["IOS"] ||
        policy["platform"] == platformTypeConstants["WINDOWS"]) {
        var ownershipTypeSelectOptions = $("#ownership-input");
        ownershipTypeSelectOptions.empty();
        ownershipTypeSelectOptions.append($("<option></option>").
                                              attr("value", "BYOD").text("BYOD (Bring Your Own Device)"));
        ownershipTypeSelectOptions.attr("disabled", true);
    }
};

stepBackFrom["policy-profile"] = function () {
    // reinitialize configuredOperations
    configuredOperations = [];
    // clearing already-loaded platform specific hidden-operations html content from the relevant div
    // so that, the wrong content would not be shown at the first glance, in case
    // the user selects a different platform
    $(".wr-advance-operations").html(
        "<div class='wr-advance-operations-init'>" +
        "<br>" +
        "<i class='fw fw-settings fw-spin fw-2x'></i>" +
        "Loading Platform Features . . ." +
        "<br>" +
        "<br>" +
        "</div>"
    );
};

stepForwardFrom["policy-criteria"] = function () {
    $("input[type='radio'].select-users-radio").each(function () {
        if ($(this).is(':radio')) {
            if ($(this).is(":checked")) {
                if ($(this).attr("id") == "users-radio-btn") {
                    policy["selectedUsers"] = $("#users-input").val();
                } else if ($(this).attr("id") == "user-roles-radio-btn") {
                    policy["selectedUserRoles"] = $("#user-roles-input").val();
                }
            }
        }
    });
    policy["selectedNonCompliantAction"] = $("#action-input").find(":selected").data("action");
    policy["selectedOwnership"] = $("#ownership-input").val();
    //updating next-page wizard title with selected platform
    $("#policy-naming-page-wizard-title").text("ADD " + policy["platform"] + " POLICY");
};

/**
 * Checks if provided input is valid against provided length range.
 *
 * @param input Alphanumeric or non-alphanumeric input
 * @param minLength Minimum Required Length
 * @param maxLength Maximum Required Length
 * @returns {boolean} Returns true if input matches the provided minimum length and maximum length
 */
var inputIsValidAgainstLength = function (input, minLength, maxLength) {
    var length = input.length;
    return (length == minLength || (length > minLength && length < maxLength) || length == maxLength);
};

validateStep["policy-criteria"] = function () {
    var validationStatus = {};
    var selectedAssignees;
    var selectedField = "Role(s)";

    $("input[type='radio'].select-users-radio").each(function () {
        if ($(this).is(":checked")) {
            if ($(this).attr("id") == "users-radio-btn") {
                selectedAssignees = $("#users-input").val();
                selectedField = "User(s)";
            } else if ($(this).attr("id") == "user-roles-radio-btn") {
                selectedAssignees = $("#user-roles-input").val();
            }
            return false;
        }
    });

    if (selectedAssignees) {
        validationStatus["error"] = false;
    } else {
        validationStatus["error"] = true;
        validationStatus["mainErrorMsg"] = selectedField + " is a required field. It cannot be empty";
    }

    var wizardIsToBeContinued;
    if (validationStatus["error"]) {
        wizardIsToBeContinued = false;
        var mainErrorMsgWrapper = "#policy-criteria-main-error-msg";
        var mainErrorMsg = mainErrorMsgWrapper + " span";
        $(mainErrorMsg).text(validationStatus["mainErrorMsg"]);
        $(mainErrorMsgWrapper).removeClass("hidden");
    } else {
        wizardIsToBeContinued = true;
    }

    return wizardIsToBeContinued;
};

validateStep["policy-naming"] = function () {
    var validationStatus = {};

    // taking values of inputs to be validated
    var policyName = $("input#policy-name-input").val();
    // starting validation process and updating validationStatus
    if (!policyName) {
        validationStatus["error"] = true;
        validationStatus["mainErrorMsg"] = "Policy name is empty. You cannot proceed.";
    } else if (!inputIsValidAgainstLength(policyName, 1, 30)) {
        validationStatus["error"] = true;
        validationStatus["mainErrorMsg"] =
            "Policy name exceeds maximum allowed length.";
    } else {
        validationStatus["error"] = false;
    }
    // ending validation process

    // start taking specific actions upon validation
    var wizardIsToBeContinued;
    if (validationStatus["error"]) {
        wizardIsToBeContinued = false;
        var mainErrorMsgWrapper = "#policy-naming-main-error-msg";
        var mainErrorMsg = mainErrorMsgWrapper + " span";
        $(mainErrorMsg).text(validationStatus["mainErrorMsg"]);
        $(mainErrorMsgWrapper).removeClass("hidden");
    } else {
        wizardIsToBeContinued = true;
    }

    return wizardIsToBeContinued;
};

validateStep["policy-platform"] = function () {
    return false;
};

validateStep["policy-naming-publish"] = function () {
    var validationStatus = {};

    // taking values of inputs to be validated
    var policyName = $("input#policy-name-input").val();
    // starting validation process and updating validationStatus
    if (!policyName) {
        validationStatus["error"] = true;
        validationStatus["mainErrorMsg"] = "Policy name is empty. You cannot proceed.";
    } else if (!inputIsValidAgainstLength(policyName, 1, 30)) {
        validationStatus["error"] = true;
        validationStatus["mainErrorMsg"] =
            "Policy name exceeds maximum allowed length.";
    } else {
        validationStatus["error"] = false;
    }
    // ending validation process

    // start taking specific actions upon validation
    var wizardIsToBeContinued;
    if (validationStatus["error"]) {
        wizardIsToBeContinued = false;
        var mainErrorMsgWrapper = "#policy-naming-main-error-msg";
        var mainErrorMsg = mainErrorMsgWrapper + " span";
        $(mainErrorMsg).text(validationStatus["mainErrorMsg"]);
        $(mainErrorMsgWrapper).removeClass("hidden");
    } else {
        wizardIsToBeContinued = true;
    }

    return wizardIsToBeContinued;
};

stepForwardFrom["policy-naming-publish"] = function () {
    policy["policyName"] = $("#policy-name-input").val();
    policy["description"] = $("#policy-description-input").val();
    //All data is collected. Policy can now be updated.
    savePolicy(policy, "/mdm-admin/policies/active-policy");
};
stepForwardFrom["policy-naming"] = function () {
    policy["policyName"] = $("#policy-name-input").val();
    policy["description"] = $("#policy-description-input").val();
    //All data is collected. Policy can now be updated.
    savePolicy(policy, "/mdm-admin/policies/inactive-policy");
};

var savePolicy = function (policy, serviceURL) {
    var profilePayloads = [];
    // traverses key by key in policy["profile"]
    var key;
    for (key in policy["profile"]) {
        if (policy["platformId"] == platformIds["WINDOWS"] &&
            key == windowsOperationConstants["PASSCODE_POLICY_OPERATION_CODE"]) {
            policy["profile"][key].enablePassword = true;
        }
        if (policy["profile"].hasOwnProperty(key)) {
            profilePayloads.push({
                "featureCode": key,
                "deviceTypeId": policy["platformId"],
                "content": policy["profile"][key]
            });
        }
    }

    $.each(profilePayloads, function (i, item) {
        $.each(item.content, function (key, value) {
            if (!value) {
                item.content[key] = null;
            }
        });
    });

    var payload = {
        "policyName": policy["policyName"],
        "description": policy["description"],
        "compliance": policy["selectedNonCompliantAction"],
        "ownershipType": policy["selectedOwnership"],
        "profile": {
            "profileName": policy["policyName"],
            "deviceType": {
                "id": policy["platformId"]
            },
            "profileFeaturesList": profilePayloads
        }
    };

    if (policy["selectedUsers"]) {
        payload["users"] = policy["selectedUsers"];
    } else if (policy["selectedUserRoles"]) {
        payload["roles"] = policy["selectedUserRoles"];
    } else {
        payload["users"] = [];
        payload["roles"] = [];
    }

    invokerUtil.post(
        serviceURL,
        payload,
        function () {
            $(".add-policy").addClass("hidden");
            $(".policy-naming").addClass("hidden");
            $(".policy-message").removeClass("hidden");
        },
        function (data) {
        }
    );
};

// Start of HTML embedded invoke methods
var showAdvanceOperation = function (operation, button) {
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    var hiddenOperation = ".wr-hidden-operations-content > div";
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
};

/**
 * Method to slide down a provided pane upon provided value set.
 *
 * @param selectElement Select HTML Element to consider
 * @param paneID HTML ID of div element to slide down
 * @param valueSet Applicable Value Set
 */
var slideDownPaneAgainstValueSet = function (selectElement, paneID, valueSet) {
    var selectedValueOnChange = $(selectElement).find("option:selected").val();
    var i, slideDownVotes = 0;
    for (i = 0; i < valueSet.length; i++) {
        if (selectedValueOnChange == valueSet[i]) {
            slideDownVotes++;
        }
    }
    var paneSelector = "#" + paneID;
    if (slideDownVotes > 0) {
        if (!$(paneSelector).hasClass("expanded")) {
            $(paneSelector).addClass("expanded");
        }
        $(paneSelector).slideDown();
    } else {
        if ($(paneSelector).hasClass("expanded")) {
            $(paneSelector).removeClass("expanded");
        }
        $(paneSelector).slideUp();
        /** now follows the code to reinitialize all inputs of the slidable pane */
            // reinitializing input fields into the defaults
        $(paneSelector + " input").each(
            function () {
                if ($(this).is("input:text")) {
                    $(this).val($(this).data("default"));
                } else if ($(this).is("input:password")) {
                    $(this).val("");
                } else if ($(this).is("input:checkbox")) {
                    $(this).prop("checked", $(this).data("default"));
                    // if this checkbox is the parent input of a grouped-input
                    if ($(this).hasClass("parent-input")) {
                        var groupedInput = $(this).parent().parent().parent();
                        updateGroupedInputVisibility(groupedInput);
                    }
                }
            }
        );
        // reinitializing select fields into the defaults
        $(paneSelector + " select").each(
            function () {
                var defaultOption = $(this).data("default");
                $("option:eq(" + defaultOption + ")", this).prop("selected", "selected");
            }
        );
        // collapsing expanded-panes (upon the selection of html-select-options) if any
        $(paneSelector + " .expanded").each(
            function () {
                if ($(this).hasClass("expanded")) {
                    $(this).removeClass("expanded");
                }
                $(this).slideUp();
            }
        );
        // removing all entries of grid-input elements if exist
        $(paneSelector + " .grouped-array-input").each(
            function () {
                var gridInputs = $(this).find("[data-add-form-clone]");
                if (gridInputs.length > 0) {
                    gridInputs.remove();
                }
                var helpTexts = $(this).find("[data-help-text=add-form]");
                if (helpTexts.length > 0) {
                    helpTexts.show();
                }
            }
        );
    }
};
// End of HTML embedded invoke methods


// Start of functions related to grid-input-view

/**
 * Method to set count id to cloned elements.
 * @param {object} addFormContainer
 */
var setId = function (addFormContainer) {
    $(addFormContainer).find("[data-add-form-clone]").each(function (i) {
        $(this).attr("id", $(this).attr("data-add-form-clone").slice(1) + "-" + (i + 1));
        if ($(this).find(".index").length > 0) {
            $(this).find(".index").html(i + 1);
        }
    });
};

/**
 * Method to set count id to cloned elements.
 * @param {object} addFormContainer
 */
var showHideHelpText = function (addFormContainer) {
    var helpText = "[data-help-text=add-form]";
    if ($(addFormContainer).find("[data-add-form-clone]").length > 0) {
        $(addFormContainer).find(helpText).hide();
    } else {
        $(addFormContainer).find(helpText).show();
    }
};

function formatRepo(user) {
    if (user.loading) {
        return user.text;
    }
    if (!user.username) {
        return;
    }
    var markup = '<div class="clearfix">' +
                 '<div clas="col-sm-8">' +
                 '<div class="clearfix">' +
                 '<div class="col-sm-3">' + user.username + '</div>';
    if (user.firstname) {
        markup += '<div class="col-sm-3"><i class="fa fa-code-fork"></i> ' + user.firstname + '</div>';
    }
    if (user.emailAddress) {
        markup += '<div class="col-sm-2"><i class="fa fa-star"></i> ' + user.emailAddress + '</div></div>';
    }
    markup += '</div></div>';
    return markup;
}

function formatRepoSelection(user) {
    return user.username || user.text;
}

function promptErrorPolicyPlatform(errorMsg) {
    var mainErrorMsgWrapper = "#policy-platform-main-error-msg";
    var mainErrorMsg = mainErrorMsgWrapper + " span";
    $(mainErrorMsg).text(errorMsg);
    $(mainErrorMsgWrapper).removeClass("hidden");
}

// End of functions related to grid-input-view


$(document).ready(function () {
    var enabledPlatforms = $("#supportedPlatforms");
    var isAndroidEnabled = enabledPlatforms.data("android");
    var isWindowsEnabled = enabledPlatforms.data("windows");
    var isIosEnabled = enabledPlatforms.data("ios");
    var androidID = enabledPlatforms.data("android-id");
    var windowsID = enabledPlatforms.data("windows-id");
    var iosID = enabledPlatforms.data("ios-id");

    var androidLink = $(".android-platform");
    if (isAndroidEnabled) {
        androidLink.attr("data-platform-id",androidID);
    } else {
        androidLink.unbind("click");
        androidLink.attr("data-validate","true");
        androidLink.bind("click",function(){
            promptErrorPolicyPlatform("You need to configure Android plugging in order to use android related feature.");
        });

    }
    var windowsLink = $(".windows-platform") ;
    if (isWindowsEnabled) {
        windowsLink.attr("data-platform-id",windowsID);
    } else {
        windowsLink.unbind("click");
        windowsLink.attr("data-validate","true");
        windowsLink.bind("click",function(){
            promptErrorPolicyPlatform("You need to configure Windows plugging in order to use windows related feature.");
        });
    }
    var iosLink = $(".windows-platform");
    if (isIosEnabled) {
        iosLink.attr("data-platform-id",iosID);
    } else {
        iosLink.unbind("click");
        iosLink.attr("data-validate","true");
        iosLink.bind("click",function(){
            promptErrorPolicyPlatform("You need to configure IOS plugging in order to use ios related feature.");
        });
    }

    $("#users-input").select2({
        multiple: true,
        tags: false,
        ajax: {
            url: window.location.origin + "/emm/api/invoker/execute/",
            method: "POST",
            dataType: 'json',
            delay: 250,
            id: function (user) {
                return user.username;
            },
            data: function (params) {
                var postData = {};
                postData.actionMethod = "GET";
                postData.actionUrl = "/mdm-admin/users/view-users?username=" + params.term;
                postData.actionPayload = null;
                return JSON.stringify(postData);
            },
            processResults: function (data, page) {
                var newData = [];
                $.each(data.responseContent, function (index, value) {
                    value.id = value.username;
                    newData.push(value);
                });
                return {
                    results: newData
                };
            },
            cache: true
        },
        escapeMarkup: function (markup) {
            return markup;
        }, // let our custom formatter work
        minimumInputLength: 1,
        templateResult: formatRepo, // omitted for brevity, see the source of this page
        templateSelection: formatRepoSelection // omitted for brevity, see the source of this page
    });

    $("#loading-content").remove();
    $(".policy-platform").removeClass("hidden");
    // Adding initial state of wizard-steps.
    $("#policy-platform-wizard-steps").html($(".wr-steps").html());

    $("select.select2[multiple=multiple]").select2({
        "tags": false
    });

    $("#users-select-field").hide();
    $("#user-roles-select-field").show();

    $("input[type='radio'].select-users-radio").change(function () {
        if ($("#users-radio-btn").is(":checked")) {
            $("#user-roles-select-field").hide();
            $("#users-select-field").show();
        }
        if ($("#user-roles-radio-btn").is(":checked")) {
            $("#users-select-field").hide();
            $("#user-roles-select-field").show();
        }
    });

    // Support for special input type "ANY" on user(s) & user-role(s) selection
    $("#user-roles-input").select2({
        "tags": false
    }).on("select2:select", function (e) {
        if (e.params.data.id == "ANY") {
            $(this).val("ANY").trigger("change");
        } else {
            $("option[value=ANY]", this).prop("selected", false).parent().trigger("change");
        }
    });

    // Maintains an array of configured features of the profile
    var advanceOperations = ".wr-advance-operations";
    $(advanceOperations).on("click", ".wr-input-control.switch", function (event) {
        var operationCode = $(this).parents(".operation-data").data("operation-code");
        var operation = $(this).parents(".operation-data").data("operation");
        var operationDataWrapper = $(this).data("target");
        // prevents event bubbling by figuring out what element it's being called from.
        if (event.target.tagName == "INPUT") {
            var featureConfiguredIcon;
            if ($("input[type='checkbox']", this).is(":checked")) {
                configuredOperations.push(operationCode);
                // when a feature is enabled, if "zero-configured-features" msg is available, hide that.
                var zeroConfiguredOperationsErrorMsg = "#policy-profile-main-error-msg";
                if (!$(zeroConfiguredOperationsErrorMsg).hasClass("hidden")) {
                    $(zeroConfiguredOperationsErrorMsg).addClass("hidden");
                }
                // add configured-state-icon to the feature
                featureConfiguredIcon = "#" + operation + "-configured";
                if ($(featureConfiguredIcon).hasClass("hidden")) {
                    $(featureConfiguredIcon).removeClass("hidden");
                }
            } else {
                //splicing the array if operation is present.
                var index = $.inArray(operationCode, configuredOperations);
                if (index != -1) {
                    configuredOperations.splice(index, 1);
                }
                // when a feature is disabled, clearing all its current configured, error or success states
                var subErrorMsgWrapper = "#" + operation + "-feature-error-msg";
                var subErrorIcon = "#" + operation + "-error";
                var subOkIcon = "#" + operation + "-ok";
                featureConfiguredIcon = "#" + operation + "-configured";

                if (!$(subErrorMsgWrapper).hasClass("hidden")) {
                    $(subErrorMsgWrapper).addClass("hidden");
                }
                if (!$(subErrorIcon).hasClass("hidden")) {
                    $(subErrorIcon).addClass("hidden");
                }
                if (!$(subOkIcon).hasClass("hidden")) {
                    $(subOkIcon).addClass("hidden");
                }
                if (!$(featureConfiguredIcon).hasClass("hidden")) {
                    $(featureConfiguredIcon).addClass("hidden");
                }
                // reinitializing input fields into the defaults
                $(operationDataWrapper + " input").each(
                    function () {
                        if ($(this).is("input:text")) {
                            $(this).val($(this).data("default"));
                        } else if ($(this).is("input:password")) {
                            $(this).val("");
                        } else if ($(this).is("input:checkbox")) {
                            $(this).prop("checked", $(this).data("default"));
                            // if this checkbox is the parent input of a grouped-input
                            if ($(this).hasClass("parent-input")) {
                                var groupedInput = $(this).parent().parent().parent();
                                updateGroupedInputVisibility(groupedInput);
                            }
                        }
                    }
                );
                // reinitializing select fields into the defaults
                $(operationDataWrapper + " select").each(
                    function () {
                        var defaultOption = $(this).data("default");
                        $("option:eq(" + defaultOption + ")", this).prop("selected", "selected");
                    }
                );
                // collapsing expanded-panes (upon the selection of html-select-options) if any
                $(operationDataWrapper + " .expanded").each(
                    function () {
                        if ($(this).hasClass("expanded")) {
                            $(this).removeClass("expanded");
                        }
                        $(this).slideUp();
                    }
                );
                // removing all entries of grid-input elements if exist
                $(operationDataWrapper + " .grouped-array-input").each(
                    function () {
                        var gridInputs = $(this).find("[data-add-form-clone]");
                        if (gridInputs.length > 0) {
                            gridInputs.remove();
                        }
                        var helpTexts = $(this).find("[data-help-text=add-form]");
                        if (helpTexts.length > 0) {
                            helpTexts.show();
                        }
                    }
                );
            }
        }
    });

    // adding support for cloning multiple profiles per feature with cloneable class definitions
    $(advanceOperations).on("click", ".multi-view.add.enabled", function () {
        // get a copy of .cloneable and create new .cloned div element
        var cloned = "<div class='cloned'><hr>" + $(".cloneable", $(this).parent().parent()).html() + "</div>";
        // append newly created .cloned div element to panel-body
        $(this).parent().parent().append(cloned);
        // enable remove action of newly cloned div element
        $(".cloned", $(this).parent().parent()).each(
            function () {
                if ($(".multi-view.remove", this).hasClass("disabled")) {
                    $(".multi-view.remove", this).removeClass("disabled");
                }
                if (!$(".multi-view.remove", this).hasClass("enabled")) {
                    $(".multi-view.remove", this).addClass("enabled");
                }
            }
        );
    });

    $(advanceOperations).on("click", ".multi-view.remove.enabled", function () {
        $(this).parent().remove();
    });

    // enabling or disabling grouped-input based on the status of a parent check-box
    $(advanceOperations).on("click", ".grouped-input", function () {
        updateGroupedInputVisibility(this);
    });

    // add form entry click function for grid inputs
    $(advanceOperations).on("click", "[data-click-event=add-form]", function () {
        var addFormContainer = $("[data-add-form-container=" + $(this).attr("href") + "]");
        var clonedForm = $("[data-add-form=" + $(this).attr("href") + "]").clone().
            find("[data-add-form-element=clone]").attr("data-add-form-clone", $(this).attr("href"));

        // adding class .child-input to capture text-input-array-values
        $("input, select", clonedForm).addClass("child-input");

        $(addFormContainer).append(clonedForm);
        setId(addFormContainer);
        showHideHelpText(addFormContainer);
    });

    // remove form entry click function for grid inputs
    $(advanceOperations).on("click", "[data-click-event=remove-form]", function () {
        var addFormContainer = $("[data-add-form-container=" + $(this).attr("href") + "]");

        $(this).closest("[data-add-form-element=clone]").remove();
        setId(addFormContainer);
        showHideHelpText(addFormContainer);
    });

    $(".wizard-stepper").click(function () {
        // button clicked here can be either a continue button or a back button.
        var currentStep = $(this).data("current");
        var validationIsRequired = $(this).data("validate");
        var wizardIsToBeContinued;

        if (validationIsRequired) {
            wizardIsToBeContinued = validateStep[currentStep]();
        } else {
            wizardIsToBeContinued = true;
        }

        if (wizardIsToBeContinued) {
            // When moving back and forth, following code segment will
            // remove if there are any visible error-messages.
            var errorMsgWrappers = ".alert.alert-danger";
            $(errorMsgWrappers).each(
                function () {
                    if (!$(this).hasClass("hidden")) {
                        $(this).addClass("hidden");
                    }
                }
            );

            var nextStep = $(this).data("next");
            var isBackBtn = $(this).data("is-back-btn");

            // if current button is a continuation...
            if (!isBackBtn) {
                // initiate stepForwardFrom[*] functions to gather form data.
                if (stepForwardFrom[currentStep]) {
                    stepForwardFrom[currentStep](this);
                }
            } else {
                // initiate stepBackFrom[*] functions to rollback.
                if (stepBackFrom[currentStep]) {
                    stepBackFrom[currentStep]();
                }
            }

            // following step occurs only at the last stage of the wizard.
            if (!nextStep) {
                window.location.href = $(this).data("direct");
            }

            // updating next wizard step as current.
            $(".itm-wiz").each(function () {
                var step = $(this).data("step");
                if (step == nextStep) {
                    $(this).addClass("itm-wiz-current");
                } else {
                    $(this).removeClass("itm-wiz-current");
                }
            });

            // adding next update of wizard-steps.
            $("#" + nextStep + "-wizard-steps").html($(".wr-steps").html());

            // hiding current section of the wizard and showing next section.
            $("." + currentStep).addClass("hidden");
            $("." + nextStep).removeClass("hidden");
        }
    });
});
