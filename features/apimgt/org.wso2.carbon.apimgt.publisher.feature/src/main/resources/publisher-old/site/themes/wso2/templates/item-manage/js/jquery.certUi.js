(function ($, window, document, undefined) {
    var deleteConfirmation = i18n.t("Do you really want to delete the certificate with alias");
    var aliasEPValidationMessage = i18n.t("Alias should not be empty");
    var certFileError = i18n.t("You must upload a certificate file");
    var certSource = $("#certificate-ui-template").html();
    var certFormSource = $(".cert-upload-form-content").html();
    var certTemplate;
    var certFormTemplate;
    var file;
    var certString;
    var name = "certUi";
    var certUi;

    if (certSource !== undefined && certSource !== "") {
        certTemplate = Handlebars.compile(certSource);
    }

    if (certFormSource !== undefined && certFormSource !== "") {
        certFormTemplate = Handlebars.compile(certFormSource);
    }

    var defaults = {};

    function CertUi(element, options) {
        this.element = $(element);
        this.options = $.extend({}, defaults, options);
        this.config = {"certificates": this.options.config.cert_data};
        this._name = name;
        certUi = this;
        this.init();
    }

    CertUi.prototype = {
        init: function () {
            this.render();
            this.attach_events();
        },

        /**
         * Register event handlers.
         * */
        attach_events: function () {
            this.element
                .on("click", ".delete-cert", $.proxy(this._on_click_delete, this))
                .on("click", ".add-cert-btn", $.proxy(this._on_click_add, this))
                .on("click", ".cert-upload-btn", $.proxy(this._on_upload_click, this))
                .on("click", ".browse", $.proxy(this._on_browse_click, this))
                .on("change", ".cert-upload", $.proxy(this._on_file_change, this))
                .on("click", ".modal-close", $.proxy(this._on_modal_close, this));
        },

        /**
         * Renders the certificate listing.
         */
        render: function () {
            var context = $.extend({}, this.config);
            this.element.html(certTemplate(context));
        },

        /**
         * Renders the Upload Certificate Modal.
         * If the user has not entered any endpoint, an alert will be shown,
         * */
        renderForm: function () {
            var context = $.extend({}, this.context);

            var x = this.element.find('.cert-upload-form').html(certFormTemplate(context));
            this.element.find('#upload-cert-modal').modal({backdrop: 'static', keyboard: false});
        },


        /**
         * Handles the Browse button click. This will trigger the input onClick event to open the file browser.
         * */
        _on_browse_click: function (e) {
            this.element.find('.cert-upload').click();
        },

        /**
         * Handles the Add [Certificate for Endpoint] button click.
         * */
        _on_click_add: function (e) {
            this.renderForm();
        },

        /**
         * File input change handler.
         * Reads the file to a BASE64 encoded string.
         * */
        _on_file_change: function (e) {
            file = e.currentTarget.files[0];

            var fr = new FileReader();
            fr.readAsArrayBuffer(file);
            fr.onload = (function () {
                certString = btoa(String.fromCharCode.apply(null, new Uint8Array(fr.result)));
                $(".cert-content").val(file.name);
            });
        },

        /**
         * Handles the close button action in Modal.
         * */
        _on_modal_close: function (e) {
            $('.certAlias').val('');
            $('.cert-content').val("");
            file = null;
            this.element.find('#upload-cert-modal').modal('hide');
        },

        /**
         * Handles delete certificate action.
         * */
        _on_click_delete: function (e) {
            $("#messageModal div.modal-footer").html("");
            var dataAlias = $(e.currentTarget).attr("data-alias");
            var apiName = $(e.currentTarget).attr("data-apiname");
            var apiVersion = $(e.currentTarget).attr("data-apiversion");
            var apiProvider = $(e.currentTarget).attr("data-apiprovider");
            var certUi = this;

            jagg.message({
                content: deleteConfirmation + " " + dataAlias + "? <br/> <strong>" +
                i18n.t("This action cannot be reversed") + ".</strong>",
                type: "confirm",
                title: i18n.t("Delete certificate with alias") + " " + dataAlias,
                okCallback: function () {
                    jagg.post("/site/blocks/item-design/ajax/add.jag",
                        {
                            action: "deleteClientCert",
                            alias: dataAlias,
                            name : apiName,
                            version : apiVersion,
                            provider : apiProvider
                        },
                        function (result) {
                            $("#messageModal div.modal-footer").html("");
                            if (!result.error) {
                                if (result.message.code !== 1) {
                                    jagg.message({
                                        content: getMessage(result.message),
                                        type: "error"
                                    });
                                    return;
                                }
                                certUi.config.certificates = certUi.config.certificates.filter(function (certificate) {
                                    return certificate.alias !== dataAlias;
                                });
                                jagg.message({
                                    content: getMessage(result.message),
                                    type: "warning"
                                });
                                certUi.render();
                            } else {
                                $('.modal-backdrop').remove();
                                jagg.message({
                                    content: getMessage(result.message),
                                    type: "error"
                                });
                            }
                        }, "json"
                    );
                }
            });
        },

        /**
         * Handles the upload certificate action.
         * */
        _on_upload_click: function () {
            $("#messageModal div.modal-footer").html("");
            var alias = $('.certAlias').val();
            var name = $('#certificate_api_name').val();
            var tierName = $('#tierName').find(":selected").attr("data-value");
            var version = $('#certificate_api_version').val();
            var provider = $('#certificate_api_provider').val();
            var existingCertificates = certUi.config.certificates;

            /**
             * Validates the alias is not null.
             * */
            if (alias === "" || alias === undefined) {
                jagg.message({
                    content: aliasEPValidationMessage,
                    type: "error"
                });
                return;
            }

            /**
             * Validates the certificate file is uploaded.
             * */
            if (!file) {
                jagg.message({
                    content: certFileError,
                    type: "error"
                });
                return;
            }

            var cert_payload = {"alias": alias, "tier" : tierName};
            /**
             * Checks whether a certificate is already uploaded for the given alias and endpoint.
             * */
            var aliasMatched = existingCertificates.filter(function (certificate) {
                return certificate.alias === alias;
            });


            if (aliasMatched.length > 0) {
                jagg.message({
                    type: "error",
                    content: i18n.t("Could not add certificate for alias") + ", '" + alias + "'. " +
                        i18n.t("Alias exists in trust store") + ". " + i18n.t("Please provide a different alias name.")
                });
                return;
            }
            /**
             * Calls the api with payload to upload the certificate.
             * */
            jagg.post("/site/blocks/item-design/ajax/add.jag",
                {
                    action: "addClientCertificate",
                    alias: alias,
                    certificate: certString,
                    name : name,
                    version : version,
                    provider : provider,
                    tierName : tierName
                },
                function (result) {
                    if (!result.error) {
                        if (result.message.code !== 1) {
                            jagg.message({
                                content: getMessage(result.message),
                                type: "error"
                            });
                            return;
                        }
                        certUi.config.certificates.push(cert_payload);
                        jagg.message({
                            content: getMessage(result.message),
                            type: "info"
                        });
                        certUi._on_modal_close();
                        certUi.render();
                    } else {
                        jagg.message({
                            content: getMessage(result.message),
                            type: "error"
                        });
                    }
                }, "json");
        }
    };

    $.fn[name] = function (options) {
        return this.each(function () {
            if (!$.data(this, "plugin_" + name)) {
                $.data(this, "plugin_" + name,
                    new CertUi(this, options));
            }
        });
    };

    /**
     * Returns specific message based on the response code.
     * 1: Success
     * 2: Internal Server error
     * 3: Certificate exists in trust store.
     * 4: Certificate not found.
     * */
    var getMessage = function (msgObject) {
        switch (msgObject.code) {
            case (1) : {
                if (msgObject.action === "add") {
                    return i18n.t("The certificate was added successfully.");
                } else {
                    return i18n.t("The certificate was deleted successfully.")
                }
            }
            case (2) : {
                if (msgObject.action === "add") {
                    return i18n.t("Failed to add certificate due to an Internal Server Error.");
                } else {
                    return i18n.t("Failed to delete certificate due to an Internal Server Error");
                }
            }
            case (3) : {
                return i18n.t("Failed to add certificate in to the publisher trust store. Certificate exists for the " +
                    "alias");
            }
            case (4) : {
                return i18n.t("Failed to delete the certificate. Certificate could not be found for the given alias." +
                    " Hence the entry is removed from the database.");
            }
            case (6) : {
                return i18n.t("Failed to add certificate. Certificate expired");
            }
        }
    };
})(jQuery, window, document);
