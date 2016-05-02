"use strict";
window.SwaggerEditor = angular.module("SwaggerEditor", ["ngSanitize", "ui.router", "ui.ace", "ui.bootstrap", "ngStorage", "ngSanitize", "hc.marked", "ui.layout", "ngFileUpload", "mohsen1.schema-form", "jsonFormatter"]), $(function() {
        function a(a) {
            var b = !/localhost/.test(window.location.host);
            window.SwaggerEditor.$defaults = a, angular.bootstrap(window.document, ["SwaggerEditor"], {
                strictDi: b
            })
        }
        var b = window.$$embeddedDefaults,
            c = window.location.pathname;
        _.endsWith(c, "/") || (c += "/");
        var d = c + "config/defaults.json";
        b ? a(b) : $.getJSON(d).done(a).fail(function(a) {
            console.error("Failed to load defaults.json from", d), console.error(a)
        })
    }), ace.define("ace/theme/atom_dark", ["require", "exports", "module", "ace/lib/dom"], function(a, b, c) {
        b.isDark = !0, b.cssClass = "ace-atom-dark", b.cssText = ".ace-atom-dark .ace_gutter {background: #1a1a1a;color: #868989}.ace-atom-dark .ace_print-margin {width: 1px;background: #1a1a1a}.ace-atom-dark {background-color: #1d1f21;color: #A8FF60}.ace-atom-dark .ace_cursor {color: white}.ace-atom-dark .ace_marker-layer .ace_selection {background: #444444}.ace-atom-dark.ace_multiselect .ace_selection.ace_start {box-shadow: 0 0 3px 0px #000000;border-radius: 2px}.ace-atom-dark .ace_marker-layer .ace_step {background: rgb(102, 82, 0)}.ace-atom-dark .ace_marker-layer .ace_bracket {margin: -1px 0 0 -1px;border: 1px solid #888888}.ace-atom-dark .ace_marker-layer .ace_highlight {border: 1px solid rgb(110, 119, 0);border-bottom: 0;box-shadow: inset 0 -1px rgb(110, 119, 0);margin: -1px 0 0 -1px;background: rgba(255, 235, 0, 0.1);}.ace-atom-dark .ace_marker-layer .ace_active-line {background: #2A2A2A}.ace-atom-dark .ace_gutter-active-line {background-color: #2A2A2A}.ace-atom-dark .ace_stack {background-color: rgb(66, 90, 44)}.ace-atom-dark .ace_marker-layer .ace_selected-word {border: 1px solid #888888}.ace-atom-dark .ace_invisible {color: #343434}.ace-atom-dark .ace_keyword,.ace-atom-dark .ace_meta,.ace-atom-dark .ace_storage,.ace-atom-dark .ace_storage.ace_type,.ace-atom-dark .ace_support.ace_type {color: #96CBFE}.ace-atom-dark .ace_keyword.ace_operator {color: #70C0B1}.ace-atom-dark .ace_constant.ace_character,.ace-atom-dark .ace_constant.ace_language,.ace-atom-dark .ace_constant.ace_numeric,.ace-atom-dark .ace_keyword.ace_other.ace_unit,.ace-atom-dark .ace_support.ace_constant,.ace-atom-dark .ace_variable.ace_parameter {color: #fe73fd}.ace-atom-dark .ace_constant.ace_other {color: #EEEEEE}.ace-atom-dark .ace_invalid {color: #CED2CF;background-color: #DF5F5F}.ace-atom-dark .ace_invalid.ace_deprecated {color: #CED2CF;background-color: #B798BF}.ace-atom-dark .ace_fold {background-color: #7AA6DA;border-color: #DEDEDE}.ace-atom-dark .ace_entity.ace_name.ace_function,.ace-atom-dark .ace_support.ace_function,.ace-atom-dark .ace_variable {color: #7AA6DA}.ace-atom-dark .ace_support.ace_class,.ace-atom-dark .ace_support.ace_type {color: #E7C547}.ace-atom-dark .ace_heading,.ace-atom-dark .ace_markup.ace_heading,.ace-atom-dark .ace_string {color: #B9CA4A}.ace-atom-dark .ace_entity.ace_name.ace_tag,.ace-atom-dark .ace_entity.ace_other.ace_attribute-name,.ace-atom-dark .ace_meta.ace_tag,.ace-atom-dark .ace_string.ace_regexp,.ace-atom-dark .ace_variable {color: #96CBFE}.ace-atom-dark .ace_comment {color: #7a7a7a}.ace-atom-dark .ace_c9searchresults.ace_keyword {color: #C2C280;}.ace-atom-dark .ace_indent-guide {background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAEklEQVQImWNgYGBgYFBXV/8PAAJoAXX4kT2EAAAAAElFTkSuQmCC) right repeat-y}";
        var d = a("../lib/dom");
        d.importCssString(b.cssText, b.cssClass)
    }), SwaggerEditor.config(["$provide", function(a) {
        function b(a) {
            return ["${1:" + a + "}:", "  summary: ${2}", "  description: ${2}", "  responses:", "    ${3:200:}", "      description: ${4:OK}", "${6}"].join("\n")
        }

        function c(a) {
            return ["${1:" + a + "}:", "  description: ${2}", "${3}"].join("\n")
        }
        var d = "get|put|post|delete|options|head|patch";
        a.constant("snippets", [{
            name: "swagger",
            trigger: "sw",
            path: [],
            content: ['swagger: "2.0"', "${1}"].join("\n")
        }, {
            name: "info",
            trigger: "info",
            path: [],
            content: ["info:", "  version: ${1:0.0.0}", "  title: ${2:title}", "  description: ${3:description}", "  termsOfService: ${4:terms}", "  contact:", "    name: ${5}", "    url: ${6}", "    email: ${7}", "  license:", "    name: ${8:MIT}", "    url: ${9:http://opensource.org/licenses/MIT}", "${10}"].join("\n")
        }, {
            name: "paths",
            trigger: "pa",
            path: [],
            content: ["paths:", "  ${1}"].join("\n")
        }, {
            name: "definitions",
            trigger: "def",
            path: [],
            content: ["definitions:", "  ${1}"].join("\n")
        }, {
            name: "path",
            trigger: "path",
            path: ["paths"],
            content: ["/${1}:", "  ${2}"].join("\n")
        }, {
            name: "get",
            trigger: "get",
            path: ["paths", "."],
            content: b("get")
        }, {
            name: "post",
            trigger: "post",
            path: ["paths", "."],
            content: b("post")
        }, {
            name: "put",
            trigger: "put",
            path: ["paths", "."],
            content: b("put")
        }, {
            name: "delete",
            trigger: "delete",
            path: ["paths", "."],
            content: b("delete")
        }, {
            name: "patch",
            trigger: "patch",
            path: ["paths", "."],
            content: b("patch")
        }, {
            name: "options",
            trigger: "options",
            path: ["paths", "."],
            content: b("options")
        }, {
            name: "parameter",
            trigger: "param",
            path: ["paths", ".", ".", "parameters"],
            content: ["- name: ${1:parameter_name}", "  in: ${2:query}", "  description: ${3:description}", "  type: ${4:string}", "${5}"].join("\n")
        }, {
            name: "parameter",
            trigger: "param",
            path: ["paths", ".", "parameters"],
            content: ["- name: ${1:parameter_name}", "  in: ${2:path}", "  required: true", "  description: ${3:description}", "  type: ${4:string}", "${5}"].join("\n")
        }, {
            name: "response",
            trigger: "resp",
            path: ["paths", ".", ".", "responses"],
            content: ["${1:code}:", "  description: ${2}", "  schema: ${3}", "${4}"].join("\n")
        }, {
            name: "200",
            trigger: "200",
            path: ["paths", ".", d, "responses"],
            content: c("200")
        }, {
            name: "300",
            trigger: "300",
            path: ["paths", ".", d, "responses"],
            content: c("300")
        }, {
            name: "400",
            trigger: "400",
            path: ["paths", ".", d, "responses"],
            content: c("400")
        }, {
            name: "500",
            trigger: "500",
            path: ["paths", ".", d, "responses"],
            content: c("500")
        }, {
            name: "model",
            trigger: "mod|def",
            regex: "mod|def",
            path: ["definitions"],
            content: ["${1:ModelName}:", "  properties:", "    ${2}"]
        }])
    }]),
    function(a, b, c, d, e, f, g) {
        a.GoogleAnalyticsObject = e, a[e] = a[e] || function() {
            (a[e].q = a[e].q || []).push(arguments)
        }, a[e].l = 1 * new Date, f = b.createElement(c), g = b.getElementsByTagName(c)[0], f.async = 1, f.src = d, g.parentNode.insertBefore(f, g)
    }(window, document, "script", "//www.google-analytics.com/analytics.js", "ga"), _.templateSettings = {
        interpolate: /\{(.+?)\}/g
    }, SwaggerEditor.controller("MainCtrl", ["$scope", "$rootScope", "$stateParams", "$location", "Editor", "Storage", "FileLoader", "Analytics", "defaults", function(a, b, c, d, e, f, g, h, i) {
        function j() {
            f.load("yaml").then(function(a) {
                var b, e = !1;
                c.import ? (b = c.import, e = Boolean(c["no-proxy"]), d.search("import", null), d.search("no-proxy", null)) : a || (b = i.examplesFolder + i.exampleFiles[0]), b && g.loadFromUrl(b, e).then(k)
            })
        }

        function k(a) {
            a && (f.save("yaml", a), b.editorValue = a)
        }
        h.initialize(), b.$on("$stateChangeStart", e.initializeEditor), b.$on("$stateChangeStart", j), $("body").addClass(i.brandingCssClass), j();
        var l = new FileReader;
        a.draggedFiles = [], a.$watch("draggedFiles", function() {
            a.draggedFiles instanceof File && l.readAsText(a.draggedFiles, "utf-8")
        }), l.onloadend = function() {
            l.result && g.load(l.result).then(k)
        }
    }]), SwaggerEditor.controller("HeaderCtrl", ["$scope", "$modal", "$stateParams", "$state", "$rootScope", "Storage", "Builder", "FileLoader", "Editor", "Codegen", "Preferences", "YAML", "defaults", "strings", "$localStorage", function(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o) {
        function p(a) {
            b.open({
                templateUrl: "templates/code-gen-error-modal.html",
                controller: "GeneralModal",
                size: "large",
                resolve: {
                    data: function() {
                        return a.data ? a.data : a.config
                    }
                }
            })
        }

        function q() {
            var b = "text/plain",
                c = e.editorValue;
            l.load(c, function(d, e) {
                function f(c, d) {
                    d.info.version && (d.info.version = String(d.info.version)), d.swagger && (2 === d.swagger ? d.swagger = "2.0" : d.swagger = String(d.swagger)), d = JSON.stringify(d, null, 4);
                    var e = new Blob([d], {
                        type: b
                    });
                    a.jsonDownloadHref = window.URL.createObjectURL(e), a.jsonDownloadUrl = [b, "swagger.json", a.jsonDownloadHref].join(":");
                    var f = new Blob([c], {
                        type: b
                    });
                    a.yamlDownloadHref = window.URL.createObjectURL(f), a.yamlDownloadUrl = [b, "swagger.yaml", a.yamlDownloadHref].join(":")
                }
                if (!d) {
                    var g = null;
                    try {
                        JSON.parse(c)
                    } catch (a) {
                        g = a
                    }
                    g ? f(c, e) : l.dump(e, function(a, b) {
                        f(b, e)
                    })
                }
            })
        }

        function r() {}
        c.path ? a.breadcrumbs = [{
            active: !0,
            name: c.path
        }] : a.breadcrumbs = [], e.$watch("progressStatus", function(b) {
            var c = n.stausMessages[b],
                d = null;
            /success/.test(b) && (d = "success"), /error/.test(b) && (d = "error"), /working/.test(b) && (d = "working"), a.status = c, a.statusClass = d
        }), o.$default({
            showIntro: !m.disableNewUserIntro
        }), e.showAbout = o.showIntro, a.disableCodeGen = m.disableCodeGen, m.disableCodeGen || (j.getServers().then(function(b) {
            a.servers = b
        }, function() {
            a.serversNotAvailable = !0
        }), j.getClients().then(function(b) {
            a.clients = b
        }, function() {
            a.clientsNotAvailable = !0
        })), a.getSDK = function(a, b) {
            j.getSDK(a, b).then(r, p)
        }, a.showFileMenu = function() {
            return !m.disableFileMenu
        }, a.showHeaderBranding = function() {
            return m.headerBranding
        }, a.newProject = function() {
            h.loadFromUrl("spec-files/guide.yaml").then(function(a) {
                e.editorValue = a, f.save("yaml", a), d.go("home", {
                    tags: null
                })
            })
        }, a.onFileMenuOpen = function() {
            q(), e.$broadcast("toggleWatchers", !1)
        }, a.openImportFile = function() {
            b.open({
                templateUrl: "templates/file-import.html",
                controller: "FileImportCtrl",
                size: "large"
            })
        }, a.openImportUrl = function() {
            b.open({
                templateUrl: "templates/url-import.html",
                controller: "UrlImportCtrl",
                size: "large"
            })
        }, a.openPasteJSON = function() {
            b.open({
                templateUrl: "templates/paste-json.html",
                controller: "PasteJSONCtrl",
                size: "large"
            })
        }, a.openAbout = function() {
            b.open({
                templateUrl: "templates/about.html",
                size: "large",
                controller: "ModalCtrl"
            })
        }, e.toggleAboutEditor = function(a) {
            e.showAbout = a, o.showIntro = a
        }, a.openEditorPreferences = i.showSettings, a.resetSettings = i.resetSettings, a.adjustFontSize = i.adjustFontSize, a.openExamples = function() {
            b.open({
                templateUrl: "templates/open-examples.html",
                controller: "OpenExamplesCtrl",
                size: "large"
            })
        }, a.openPreferences = function() {
            b.open({
                templateUrl: "templates/preferences.html",
                controller: "PreferencesCtrl",
                size: "large"
            })
        }, a.isLiveRenderEnabled = function() {
            return !!k.get("liveRender")
        }, a.capitalizeGeneratorName = function(a) {
            var b = {
                jaxrs: "JAX-RS",
                nodejs: "Node.js",
                scalatra: "Scalatra",
                "spring-mvc": "Spring MVC",
                android: "Android",
                "async-scala": "Async Scala",
                csharp: "C#",
                CsharpDotNet2: "C# .NET 2.0",
                qt5cpp: "Qt 5 C++",
                java: "Java",
                objc: "Objective-C",
                php: "PHP",
                python: "Python",
                ruby: "Ruby",
                scala: "Scala",
                "dynamic-html": "Dynamic HTML",
                html: "HTML",
                swagger: "Swagger JSON",
                "swagger-yaml": "Swagger YAML",
                tizen: "Tizen"
            };
            return b[a] ? b[a] : a.split(/\s+|\-/).map(function(a) {
                return a[0].toUpperCase() + a.substr(1)
            }).join(" ")
        }
    }]), SwaggerEditor.controller("FileImportCtrl", ["$scope", "$modalInstance", "$rootScope", "$localStorage", "$state", "FileLoader", "Storage", function(a, b, c, d, e, f, g) {
        var h;
        a.fileChanged = function(b) {
            f.load(b).then(function(b) {
                a.$apply(function() {
                    h = b
                })
            })
        }, a.ok = function() {
            angular.isString(h) && (c.editorValue = h, g.save("yaml", h), e.go("home", {
                tags: null
            })), b.close()
        }, a.isInvalidFile = function() {
            return null === h
        }, a.isFileSelected = function() {
            return !!h
        }, a.cancel = b.close
    }]), SwaggerEditor.controller("EditorCtrl", ["$scope", "$rootScope", "Editor", "Builder", "Storage", "ExternalHooks", "Preferences", function(a, b, c, d, e, f, g) {
        function h() {
            return _.debounce(i, g.get("keyPressDebounceTime"))
        }

        function i() {
            var a = b.editorValue;
	    var design = parent.APIDesigner();
            e.save("yaml", a), design.yaml = a, f.trigger("code-change", [])
        }
        var j = h();
        g.onChange(function(a) {
            "keyPressDebounceTime" === a && (j = h())
        }), a.aceLoaded = c.aceLoaded, a.aceChanged = function() {
            b.progressStatus = "progress-working", j()
        }, c.ready(function() {
            e.load("yaml").then(function(a) {
		var design = parent.APIDesigner(),
                    a = jsyaml.safeDump(design.api_doc);
                b.editorValue = a, i(!0)
            })
        })
    }]), SwaggerEditor.controller("PreviewCtrl", ["Storage", "Builder", "ASTManager", "Editor", "FocusedPath", "TagManager", "Preferences", "FoldStateManager", "$scope", "$rootScope", "$stateParams", "$sessionStorage", function(a, b, c, d, e, f, g, h, i, j, k, l) {
        function m(a, c) {
            return g.get("liveRender") || c || !i.specs ? void b.buildDocs(a).then(o, p) : (j.isDirty = !0, void(j.progressStatus = "progress-unsaved"))
        }

        function n(a) {
            if (i.$broadcast("toggleWatchers", !0), a.specs && a.specs.securityDefinitions) {
                var b = {};
                _.forEach(a.specs.securityDefinitions, function(a, c) {
                    b[c] = SparkMD5.hash(JSON.stringify(a))
                }), l.securityKeys = b
            }
            j.$apply(function() {
                a.specs && (f.registerTagsFromSpec(a.specs), _.defaultsDeep(a.specs, h.getFoldedTree(j.specs, a.specs)), j.specs = a.specs), j.errors = a.errors || [], j.warnings = a.warnings || []
            })
        }

        function o(a) {
            n(a), j.$apply(function() {
                j.progressStatus = "success-process"
            }), d.clearAnnotation(), _.each(a.warnings, function(a) {
                d.annotateSwaggerError(a, "warning")
            })
        }

        function p(a) {
            n(a), j.$apply(function() {
                angular.isArray(a.errors) ? a.errors[0].yamlError ? (d.annotateYAMLErrors(a.errors[0].yamlError), j.progressStatus = "error-yaml") : a.errors.length ? (j.progressStatus = "error-swagger", a.errors.forEach(d.annotateSwaggerError)) : j.progressStatus = "progress" : j.progressStatus = "error-general"
            })
        }

        function q() {
            m(j.editorValue, !0), j.isDirty = !1
        }

        function r(a, b) {
            a.stopPropagation(), c.positionRangeForPath(j.editorValue, b).then(function(a) {
                d.gotoLine(a.start.line), d.focus()
            })
        }

        function s(a) {
            var b = {
                2: "green",
                3: "blue",
                4: "yellow",
                5: "red"
            };
            return b[Math.floor(+a / 100)] || "default"
        }

        function t(a) {
            return _.startsWith(a, "x-")
        }

        function u(a) {
            return angular.isObject(a)
        }

        function v(a, b) {
            var c = f.getCurrentTags() && f.getCurrentTags().length;
            return t(b) ? !1 : "parameters" === b ? !1 : c ? a.tags && a.tags.length && _.intersection(f.getCurrentTags(), a.tags).length : !0
        }

        function w(a, b) {
            return t(b) ? !1 : _.some(a, v)
        }

        function x() {
            _.each(i.specs.paths, function(a, b) {
                _.isObject(a) && a.$folded === !0 && (a.$folded = !1, h.foldEditor(["paths", b], !1))
            }), _.each(i.specs.paths, function(a, b) {
                _.each(a, function(a, c) {
                    _.isObject(a) && (a.$folded = !0, h.foldEditor(["paths", b, c], !0))
                })
            })
        }

        function y() {
            _.each(i.specs.definitions, function(a, b) {
                _.isObject(a) && (a.$folded = !0, h.foldEditor(["definitions", b], !0))
            })
        }
        i.loadLatest = q, i.tagIndexFor = f.tagIndexFor, i.getAllTags = f.getAllTags, i.tagsHaveDescription = f.tagsHaveDescription, i.getCurrentTags = f.getCurrentTags, i.stateParams = k, i.isVendorExtension = t, i.showOperation = v, i.showDefinitions = u, i.responseCodeClassFor = s, i.focusEdit = r, i.showPath = w, i.foldEditor = h.foldEditor, i.listAllOperation = x, i.listAllDefnitions = y, a.addChangeListener("yaml", m)
    }]), SwaggerEditor.controller("GeneralModal", ["$scope", "$modalInstance", "data", function(a, b, c) {
        a.ok = b.close, a.cancel = b.close, a.data = c
    }]), SwaggerEditor.controller("UrlImportCtrl", ["$scope", "$modalInstance", "$localStorage", "$rootScope", "$state", "FileLoader", "Storage", function(a, b, c, d, e, f, g) {
        function h(b) {
            a.error = null, a.canImport = !1, _.startsWith(b, "http") ? (a.fetching = !0, f.loadFromUrl(b, !a.opts.useProxy).then(function(b) {
                a.$apply(function() {
                    i = b, a.canImport = !0, a.fetching = !1
                })
            }).catch(function(b) {
                a.$apply(function() {
                    a.error = b, a.canImport = !1, a.fetching = !1
                })
            })) : a.error = "Invalid URL"
        }
        var i;
        a.url = null, a.error = null, a.opts = {
            useProxy: !0
        }, a.fetch = _.throttle(h, 200), a.ok = function() {
            angular.isString(i) && (g.save("yaml", i), d.editorValue = i, e.go("home", {
                tags: null
            })), b.close()
        }, a.cancel = b.close
    }]), SwaggerEditor.controller("PasteJSONCtrl", ["$scope", "$modalInstance", "$rootScope", "$state", "Storage", "YAML", "SwayWorker", function(a, b, c, d, e, f, g) {
        var h;
        a.checkJSON = function(b) {
            a.canImport = !1;
            try {
                h = JSON.parse(b)
            } catch (b) {
                return a.error = b.message, void(a.canImport = !1)
            }
            g.run({
                definition: h
            }, function(b) {
                a.canImport = !0, a.error = null, b.errors.length && (a.error = b.errors[0]), a.$digest()
            })
        }, a.ok = function() {
            f.dump(h, function(a, f) {
                e.save("yaml", f), c.editorValue = f, d.go("home", {
                    tags: null
                }), b.close()
            })
        }, a.cancel = b.close
    }]), SwaggerEditor.controller("ErrorPresenterCtrl", ["$scope", "$rootScope", "Editor", "ASTManager", function(a, b, c, d) {
        function e() {
            f().then(function(b) {
                a.$apply(function() {
                    a.errorsAndWarnings = b
                })
            })
        }

        function f() {
            var a = b.errors.map(function(a) {
                return a.level = j, a
            }).concat(b.warnings.map(function(a) {
                return a.level = k, a
            })).map(function(a) {
                return a.type = g(a), a.description = h(a), a
            });
            return Promise.all(a.map(i))
        }

        function g(a) {
            return a.code && a.message && a.path ? a.level > 500 ? "Swagger Error" : "Swagger Warning" : a.yamlError ? "YAML Syntax Error" : a.emptyDocsError ? "Empty Document Error" : "Unknown Error"
        }

        function h(a) {
            return _.isString(a.description) ? a.description : _.isString(a.message) ? _.isString(a.description) ? a.message + "<br>" + a.description : a.message : a.emptyDocsError ? a.emptyDocsError : a.yamlError ? a.yamlError.message.replace("JS-YAML: ", "").replace(/./, function(a) {
                return a.toUpperCase()
            }) : a.resolveError ? a.resolveError : a
        }

        function i(a) {
            if (a.yamlError) return new Promise(function(b) {
                a.lineNumber = a.yamlError.mark.line, b(a)
            });
            if (_.isArray(a.path)) {
                var c = b.editorValue,
                    e = _.clone(a.path);
                return d.positionRangeForPath(c, e).then(function(b) {
                    return a.lineNumber = b.start.line, a
                })
            }
            return a
        }
        var j = 900,
            k = 500;
        a.isCollapsed = !1, a.getErrorsAndWarnings = f, a.errorsAndWarnings = [], b.$watch("errors", e), b.$watch("warnings", e), e(), a.isOnlyWarnings = function(a) {
            return !a.some(function(a) {
                return !a || a.level > k
            })
        }, a.goToLineOfError = function(a) {
            a && (c.gotoLine(a.lineNumber), c.focus())
        }, a.isWarning = function(a) {
            return a && a.level < j
        }, a.toggleCollapse = function() {
            a.isCollapsed = !a.isCollapsed
        }
    }]), SwaggerEditor.controller("OpenExamplesCtrl", ["$scope", "$modalInstance", "$rootScope", "$state", "FileLoader", "Builder", "Storage", "Analytics", "defaults", function(a, b, c, d, e, f, g, h, i) {
        a.files = i.exampleFiles, a.selectedFile = i.exampleFiles[0], a.open = function(a) {
            var f = _.endsWith(location.pathname, "/") ? location.pathname.substring(1) : location.pathname,
                j = "/" + f + i.examplesFolder + a;
            e.loadFromUrl(j).then(function(a) {
                g.save("yaml", a), c.editorValue = a, d.go("home", {
                    tags: null
                }), b.close()
            }, b.close), h.sendEvent("open-example", "open-example:" + a)
        }, a.cancel = b.close
    }]), SwaggerEditor.controller("PreferencesCtrl", ["$scope", "$modalInstance", "Preferences", function(a, b, c) {
        a.keyPressDebounceTime = c.get("keyPressDebounceTime"), a.liveRender = c.get("liveRender"), a.autoComplete = c.get("autoComplete"), a.save = function() {
            var d = parseInt(a.keyPressDebounceTime, 10);
            if (!(d > 0)) throw new Error("$scope.keyPressDebounceTime was not set correctly");
            c.set("keyPressDebounceTime", d), c.set("liveRender", a.liveRender), c.set("autoComplete", a.autoComplete), b.close()
        }, a.close = b.close
    }]), SwaggerEditor.controller("ModalCtrl", ["$scope", "$modalInstance", function(a, b) {
        a.cancel = b.close, a.close = b.close
    }]), SwaggerEditor.controller("SecurityCtrl", ["$scope", "$modal", "AuthManager", function(a, b, c) {
        a.getHumanSecurityType = function(a) {
            var b = {
                basic: "HTTP Basic Authentication",
                oauth2: "OAuth 2.0",
                apiKey: "API Key"
            };
            return b[a]
        }, a.isAuthenticated = c.securityIsAuthenticated, a.authenticate = function(a, d) {
            "basic" === d.type ? b.open({
                templateUrl: "templates/auth/basic.html",
                controller: ["$scope", "$modalInstance", function(b, e) {
                    b.cancel = e.close, b.authenticate = function(b, f) {
                        c.basicAuth(a, d, {
                            username: b,
                            password: f
                        }), e.close()
                    }
                }],
                size: "large"
            }) : "oauth2" === d.type ? b.open({
                templateUrl: "templates/auth/oauth2.html",
                controller: ["$scope", "$modalInstance", function(b, e) {
                    b.cancel = e.close, b.authenticate = function(b) {
                        b && (c.oAuth2(a, d, {
                            accessToken: b
                        }), e.close())
                    }
                }],
                size: "large"
            }) : "apiKey" === d.type ? b.open({
                templateUrl: "templates/auth/api-key.html",
                controller: ["$scope", "$modalInstance", function(b, e) {
                    b.cancel = e.close, b.authenticate = function(b) {
                        b && (c.apiKey(a, d, {
                            apiKey: b
                        }), e.close())
                    }
                }],
                size: "large"
            }) : window.alert("Not yet supported")
        }
    }]), SwaggerEditor.directive("onReadFile", ["$parse", function(a) {
        return {
            restrict: "A",
            scope: !1,
            link: function(b, c, d) {
                var e = a(d.onReadFile);
                c.on("change", function(a) {
                    var c = new FileReader;
                    c.onload = function(a) {
                        b.$apply(function() {
                            e(b, {
                                $fileContent: a.target.result
                            })
                        })
                    }, c.readAsText((a.srcElement || a.target).files[0])
                })
            }
        }
    }]), SwaggerEditor.directive("swaggerOperation", ["defaults", function(a) {
        return {
            restrict: "E",
            replace: !0,
            templateUrl: "templates/operation.html",
            scope: !1,
            link: function(b) {
                function c(a) {
                    if (a.schema) return a;
                    if ("array" === a.type) a.schema = _.pick(a, "type", "items");
                    else {
                        var b = {
                            type: a.type
                        };
                        a.format && (b.format = a.format), a.schema = b
                    }
                    return a.allowEmptyValue === !1 && (a.schema.required = !0), a
                }
                b.isTryOpen = !1, b.enableTryIt = a.enableTryIt, b.toggleTry = function() {
                    b.isTryOpen = !b.isTryOpen
                }, b.getParameters = function() {
                    var a = _.isArray(b.path.parameters),
                        d = _.isArray(b.operation.parameters),
                        e = b.operation.parameters,
                        f = b.path.parameters;
                    return d || a ? (d || (e = []), a || (f = []), e.concat(f).map(c)) : []
                }, b.hasAResponseWithSchema = function(a) {
                    return _.keys(a).some(function(b) {
                        return a[b] && a[b].schema
                    })
                }, b.hasAResponseWithHeaders = function(a) {
                    return _.keys(a).some(function(b) {
                        return a[b] && a[b].headers
                    })
                }, b.hasAResponseWithExamples = function(a) {
                    return _.keys(a).some(function(b) {
                        return a[b] && a[b].examples
                    })
                }
            }
        }
    }]), SwaggerEditor.directive("schemaModel", function() {
        return {
            templateUrl: "templates/schema-model.html",
            restrict: "E",
            replace: !0,
            scope: {
                schema: "="
            },
            link: function(a, b) {
                function c() {
                    var c = new JSONFormatter(a.schema, 1);
                    b.find("td.view.json").html(c.render());
                    var d = new JSONSchemaView(a.schema, 1);
                    b.find("td.view.schema").html(d.render())
                }
                a.mode = "schema", a.switchMode = function() {
                    a.mode = "json" === a.mode ? "schema" : "json"
                }, a.$watch("schema", c), c()
            }
        }
    }), SwaggerEditor.directive("stopEvent", function() {
        return {
            restrict: "A",
            link: function(a, b) {
                b.bind("click", function(a) {
                    a.stopPropagation()
                })
            }
        }
    }), SwaggerEditor.directive("autoFocus", ["$timeout", function(a) {
        return {
            restrict: "A",
            link: function(b, c, d) {
                a(function() {
                    c[0].focus()
                }, d.autoFocus || 1)
            }
        }
    }]), SwaggerEditor.directive("scrollIntoViewWhen", function() {
        return {
            restrict: "A",
            link: function(a, b, c) {
                a.$watch(c.scrollIntoViewWhen, function(a) {
                    a && b.scrollIntoView(100)
                })
            }
        }
    }), SwaggerEditor.directive("collapseWhen", function() {
        var a = 200;
        return {
            restrict: "A",
            link: function(b, c, d) {
                function e() {
                    setTimeout(function() {
                        c.removeAttr("style")
                    }, a)
                }
                var f = null;
                if (d.collapseWhen) {
                    var g = c.clone();
                    g.removeAttr("style"), g.appendTo("body"), f = g.height(), g.remove()
                }
                b.$watch(d.collapseWhen, function(a) {
                    a ? (f = c.height(), c.height(f), c.height(0), c.addClass("c-w-collapsed"), e()) : (c.height(f), c.removeClass("c-w-collapsed"), e())
                })
            }
        }
    }), SwaggerEditor.directive("trackEvent", ["Analytics", function(a) {
        return {
            restrict: "A",
            link: function(b, c, d) {
                c.bind("click", function() {
                    var b = d.trackEvent;
                    if (angular.isString(b)) {
                        var c = "click-item",
                            e = b.split(" ").join("->"),
                            f = window.location.origin;
                        a.sendEvent(c, e, f)
                    }
                })
            }
        }
    }]), SwaggerEditor.config(["$provide", function(a) {
        a.constant("defaults", window.SwaggerEditor.$defaults)
    }]), SwaggerEditor.config(["$provide", function(a) {
        a.constant("strings", {
            stausMessages: {
                "error-connection": "Server connection error",
                "error-general": "Error!",
                "progress-working": "Working...",
                "progress-unsaved": "Unsaved changes",
                "success-process": "Processed with no error",
                "progress-saving": "Saving...",
                "success-saved": "All changes saved",
                "error-yaml": "YAML Syntax Error",
                "error-swagger": "Swagger Error"
            }
        })
    }]), SwaggerEditor.filter("formdata", function() {
        return function(a) {
            var b = [];
            return angular.isObject(a) && Object.keys(a).forEach(function(c) {
                angular.isDefined(a[c]) && b.push(c + ": " + a[c])
            }), b.join("\n")
        }
    }), SwaggerEditor.controller("TryOperation", ["$scope", "formdataFilter", "AuthManager", "SchemaForm", function(a, b, c, d) {
        function e() {
            var b = !1;
            if (t()) try {
                for (var c in a.requestSchema.properties.parameters.properties) {
                    var e = a.requestSchema.properties.parameters.properties[c];
                    "body" === e.in && f(e) && (b = !0)
                }
            } catch (a) {} else b = !1;
            d.options = _.extend(C, b ? D : {})
        }

        function f(a) {
            return a.additionalProperties || _.isEmpty(a.properties) ? !0 : "array" === a.type && (a.items.additionalProperties || _.isEmpty(a.items.properties)) ? !0 : !1
        }

        function g(a) {
            var b = {
                no_additional_properties: !1,
                disable_properties: !1,
                disable_edit_json: !1
            };
            return f(a) && (a.options = b), _.each(a.properties, g), a
        }

        function h() {
            var a = {
                type: "object",
                title: "Request",
                required: ["scheme", "accept"],
                properties: {
                    scheme: {
                        type: "string",
                        title: "Scheme",
                        enum: k("schemes")
                    },
                    accept: {
                        type: "string",
                        title: "Accept",
                        enum: k("produces")
                    }
                }
            };
            if (A.length && (a.properties.security = {
                    title: "Security",
                    description: "Only authenticated security options are shown.",
                    type: "array",
                    uniqueItems: !0,
                    items: {
                        type: "string",
                        enum: A
                    }
                }), t()) {
                var b = ["multipart/form-data", "x-www-form-urlencoded", "application/json"];
                a.properties.contentType = {
                    type: "string",
                    title: "Content-Type",
                    enum: k("consumes") || b
                }
            }
            return z.length && (a.properties.parameters = {
                type: "object",
                title: "Parameters",
                properties: {}
            }, z.map(m).map(j).forEach(function(b) {
                a.properties.parameters.properties[b.name] = b
            })), a
        }

        function i() {
            var a = {
                scheme: k("schemes")[0],
                accept: k("produces")[0]
            };
            return A.length && (a.security = A), t() && (a.contentType = "application/json"), z.length && (a.parameters = {}, z.map(m).map(j).forEach(function(b) {
                var c = {
                    object: {},
                    array: [],
                    integer: 0,
                    string: ""
                };
                if (angular.isDefined(b.default)) a.parameters[b.name] = b.default;
                else if (angular.isDefined(b.minimum)) a.parameters[b.name] = b.minimum;
                else if (angular.isDefined(b.maximum)) a.parameters[b.name] = b.maximum;
                else if (angular.isDefined(c[b.type])) {
                    var d = b.name || b.name;
                    "object" === b.type ? a.parameters[d] = n(b) : a.parameters[d] = c[b.type]
                } else a.parameters[b.name] = ""
            })), a
        }

        function j(a) {
            return !a.title && angular.isString(a.name) && (a.title = a.name), a.type || (a.properties && (a.type = "object"), a.items && (a.type = "array")), "file" === a.type && (a.type = "string", a.format = "file"), g(a)
        }

        function k(b) {
            var c = {
                produces: ["*/*"],
                schemes: ["http"]
            };
            return Array.isArray(a.operation[b]) ? a.operation[b] : Array.isArray(a.specs[b]) ? a.specs[b] : c[b] ? c[b] : void 0
        }

        function l() {
            var b = [];
            return _.isArray(a.operation.security) ? a.operation.security.map(function(a) {
                _.keys(a).forEach(function(a) {
                    b = b.concat(a)
                })
            }) : _.isArray(a.specs.security) && a.specs.security.map(function(a) {
                _.keys(a).forEach(function(a) {
                    b = b.concat(a)
                })
            }), _.unique(b).filter(function(a) {
                return c.securityIsAuthenticated(a)
            })
        }

        function m(a) {
            return a.schema ? _.omit(_.extend(a, a.schema), "schema") : a
        }

        function n(a) {
            if ("object" !== a.type) throw new TypeError("schema should be an object schema.");
            var b = {
                    string: "",
                    integer: 0
                },
                c = {};
            return a.properties ? (Object.keys(a.properties).forEach(function(d) {
                "object" === a.properties[d].type ? c[d] = n(a.properties[d]) : c[d] = b[a.properties[d].type] || null
            }), c) : c
        }

        function o(a) {
            return function(b) {
                return b.in === a
            }
        }

        function p(b, c) {
            b || (b = {});
            var d = a.requestModel.parameters[c.name],
                e = a.requestSchema.properties.parameters.properties[c.name].required === !0;
            if (!e) {
                if (void 0 === d) return b;
                if ("string" === c.type && "" === d) return b
            }
            return b[c.name] = a.requestModel.parameters[c.name], b
        }

        function q() {
            var b, d, e = a.requestModel,
                f = e.scheme,
                g = a.specs.host || window.location.host,
                h = a.specs.basePath || "",
                i = z.filter(o("path")).reduce(p, {}),
                j = z.filter(o("query")).reduce(p, {}),
                k = z.filter(o("query")).some(function(a) {
                    return a.items && a.items.collectionFormat
                }),
                l = /{([^{}]+)}/g;
            return "/" === h && (h = ""), angular.isArray(e.security) && e.security.forEach(function(a) {
                var b = c.getAuth(a);
                if (b && "apiKey" === b.type && "query" === b.security.in) {
                    var d = {};
                    d[b.security.name] = b.options.apiKey, _.extend(j, d)
                }
            }), b = window.decodeURIComponent($.param(j, k)), d = a.pathName.replace(l, function(a) {
                var b = a.substring(1, a.length - 1);
                return angular.isDefined(i[b]) ? i[b] : a
            }), b = b ? "?" + b : "", f + "://" + g + h + d + b
        }

        function r() {
            var b = z.filter(o("header")).reduce(p, {});
            return angular.isArray(a.requestModel.security) && a.requestModel.security.forEach(function(a) {
                var d = c.getAuth(a);
                if (d) {
                    var e = {};
                    "basic" === d.type ? e = {
                        Authorization: "Basic " + d.options.base64
                    } : "apiKey" === d.type && "header" === d.security.in ? e[d.security.name] = d.options.apiKey : "oAuth2" === d.type && (e = {
                        Authorization: "Bearer " + d.options.accessToken
                    }), b = _.extend(b, e)
                }
            }), b
        }

        function s() {
            var b = r(),
                c = a.getRequestBody(),
                d = (a.specs.host || window.location.host).replace(/\:.+/, ""),
                e = {
                    Host: d,
                    Accept: a.requestModel.accept || "*/*",
                    "Accept-Encoding": "gzip,deflate,sdch",
                    "Accept-Language": "en-US,en;q=0.8,fa;q=0.6,sv;q=0.4",
                    "Cache-Control": "no-cache",
                    Connection: "keep-alive",
                    Origin: window.location.origin,
                    Referer: window.location.origin + window.location.pathname,
                    "User-Agent": window.navigator.userAgent
                };
            return b = _.extend(e, b), null !== c && (b["Content-Length"] = c.length, b["Content-Type"] = a.requestModel.contentType), b
        }

        function t() {
            var a = z.filter(o("body")),
                b = z.filter(o("formData"));
            return a.length || b.length
        }

        function u() {
            if (!t()) return null;
            var b = z.filter(o("body"))[0],
                c = z.filter(o("formData"));
            if (b) {
                var d = b.name,
                    e = a.requestModel.parameters[d];
                if ("file" === b.format) {
                    var f = {};
                    return f[B] = e, f
                }
                return e
            }
            return c.reduce(p, {})
        }

        function v() {
            var c = z.filter(o("body"))[0],
                d = u(),
                e = a.requestModel.contentType;
            if (void 0 === d || null === d) return null;
            if (d[B]) {
                var f = c.name,
                    g = new FormData,
                    h = $('input[type="file"][name*="' + f + '"]')[0];
                if (!h) return "No file is selected";
                var i = h.files[0];
                return i ? (g.append(f, i, i.name), g) : "No file is selected"
            }
            return e ? /form\-data/.test(e) ? b(d) : /json/.test(e) ? JSON.stringify(d, null, 2) : /urlencode/.test(e) ? $.param(d) : null : d
        }

        function w() {
            return v() && v().indexOf(B) > -1
        }

        function x(a) {
            var b = {};
            return a.split("\n").forEach(function(a) {
                var c = a.split(":")[0],
                    d = a.split(":")[1];
                c && angular.isString(c) && angular.isString(d) && (b[c.trim()] = d.trim())
            }), b
        }

        function y() {
            a.xhrInProgress = !0, a.error = null;
            var b = ["Host", "Accept-Encoding", "Connection", "Origin", "Referer", "User-Agent", "Cache-Control", "Content-Length"];
            $.ajax({
                url: a.generateUrl(),
                type: a.operationName,
                headers: _.omit(a.getHeaders(), b),
                data: a.getRequestBody(),
                contentType: a.contentType
            }).fail(function(b, c, d) {
                a.xhrInProgress = !1, a.textStatus = c, a.error = d, a.xhr = b, a.$digest()
            }).done(function(b, c, d) {
                a.textStatus = c, a.xhrInProgress = !1, a.responseData = b, a.xhr = d, a.responseHeaders = x(d.getAllResponseHeaders()), a.$digest()
            })
        }
        var z = a.getParameters(),
            A = l(),
            B = " F I L E ";
        a.generateUrl = q, a.makeCall = y, a.xhrInProgress = !1, a.parameters = z, a.getRequestBody = v, a.hasRequestBody = t, a.getHeaders = s, a.requestModel = i(), a.requestSchema = h(), a.hasFileParam = w(), a.httpProtocol = "HTTP/1.1", a.locationHost = window.location.host, e(), a.$watch("specs", function() {
            a.requestModel = i(), a.requestSchema = h()
        }, !0);
        var C = {
                theme: "bootstrap3",
                remove_empty_properties: !0,
                show_errors: "change"
            },
            D = {
                no_additional_properties: !1,
                disable_properties: !1,
                disable_edit_json: !1
            };
        d.options = C, a.prettyPrint = function(a) {
            try {
                return JSON.stringify(JSON.parse(a), null, 2)
            } catch (a) {}
            return a
        }, a.isJson = function(a) {
            if (angular.isObject(a) || angular.isArray(a)) return !0;
            var b;
            try {
                JSON.parse(a)
            } catch (a) {
                b = a
            }
            return !b
        }, a.isType = function(a, b) {
            var c = new RegExp(b);
            return a = a || {}, a["Content-Type"] && c.test(a["Content-Type"])
        }, a.isCrossOrigin = function() {
            return a.specs.host && a.specs.host !== a.locationHost
        }
    }]), SwaggerEditor.service("TagManager", ["$stateParams", function(a) {
        function b(a, b) {
            this.name = a, this.description = b
        }

        function c(a, c) {
            if (a) {
                var e = d.map(function(a) {
                    return a.name
                });
                _.include(e, a) || d.push(new b(a, c))
            }
        }
        var d = [];
        this.resetTags = function() {
            d = []
        }, this.tagIndexFor = function(a) {
            for (var b = 0; b < d.length; b++)
                if (d[b].name === a) return b
        }, this.getAllTags = function() {
            return d
        }, this.tagsHaveDescription = function() {
            return d.some(function(a) {
                return a.description
            })
        }, this.registerTagsFromSpec = function(a) {
            angular.isObject(a) && (d = [], Array.isArray(a.tags) && a.tags.forEach(function(a) {
                a && angular.isString(a.name) && c(a.name, a.description)
            }), _.each(a.paths, function(a) {
                _.each(a, function(a) {
                    _.isObject(a) && _.each(a.tags, c)
                })
            }))
        }, this.getCurrentTags = function() {
            return a.tags ? a.tags.split(",") : []
        }, this.registerTag = c
    }]), SwaggerEditor.service("Autocomplete", ["$rootScope", "snippets", "KeywordMap", "Preferences", "ASTManager", "YAML", function(a, b, c, d, e, f) {
        function g(a) {
            return {
                caption: a.name,
                snippet: a.content,
                meta: "snippet"
            }
        }

        function h(b, c) {
            var d = a.editorValue,
                f = c.substr(0, c.length - 1),
                g = d.split("\n"),
                h = g[b.row];
            return 1 === b.column ? new Promise(function(a) {
                a([])
            }) : ("" === h.replace(f, "").trim() && (h += "a: b", b.column += 1), h += c, g[b.row] = h, d = g.join("\n"), e.pathForPosition(d, {
                line: b.row,
                column: b.column
            }))
        }

        function i(a, b) {
            if (!_.isArray(a) || !_.isArray(b)) return !1;
            if (a.length !== b.length) return !1;
            for (var c = 0, d = a.length; d > c; c++) {
                var e = new RegExp(b[c]).test(a[c]);
                if (!e) return !1;
                if (c === d - 1) return !0
            }
            return !0
        }

        function j(a) {
            return function(b) {
                return i(a, b.path)
            }
        }

        function k(a, b) {
            for (var c, d = Object.keys(a), e = 0; e < d.length; e++)
                if (c = new RegExp(d[e]), c.test(b) && a[d[e]]) return a[d[e]]
        }

        function l(a) {
            var b = c.get(),
                d = a.shift();
            if (!_.isArray(a)) return [];
            for (; d && _.isObject(b);) b = k(b, d), d = a.shift();
            return _.isObject(b) ? _.isArray(b) && b.every(_.isString) ? b.map(m) : (_.isArray(b) && (b = b[0]), _.isObject(b) ? _.keys(b).map(m) : []) : []
        }

        function m(a) {
            return {
                name: a,
                value: a,
                score: 300,
                meta: "keyword"
            }
        }

        function n(a) {
            return b.filter(j(a)).map(g).map(o(a));
        }

        function o(a) {
            return function(b) {
                var c = 1e3;
                return a.forEach(function(a) {
                    b.snippet.indexOf(a) && (c = 500)
                }), b.score = c, b
            }
        }

        function p() {
            return new Promise(function(b) {
                f.load(a.editorValue, function(a, c) {
                    if (a) return b([]);
                    var d = _.keys(c.definitions).map(function(a) {
                            return '"#/definitions/' + a + '"'
                        }),
                        e = _.keys(c.parameters).map(function(a) {
                            return '"#/parameters/' + a + '"'
                        }),
                        f = _.keys(c.responses).map(function(a) {
                            return '"#/responses/' + a + '"'
                        }),
                        g = d.concat(e).concat(f);
                    b(g.map(function(a) {
                        return {
                            name: a,
                            value: a,
                            score: 500,
                            meta: "$ref"
                        }
                    }))
                })
            })
        }
        var q = {
            getCompletions: function(a, b, c, e, f) {
                var g = Date.now();
                return d.get("autoComplete") ? (a.completer.autoSelect = !0, void h(c, e).then(function(a) {
                    var b = l(_.clone(a)),
                        c = n(_.clone(a));
                    if ("$ref" === _.last(a)) return p().then(function(a) {
                        f(null, a)
                    });
                    var e = Date.now() - g;
                    e > 200 && (console.info("autocomplete took " + e + "ms. Turning it off"), d.set("autoComplete", !1), d.set("keyPressDebounceTime", 3 * e)), f(null, b.concat(c))
                })) : f(null, [])
            }
        };
        this.init = function(a) {
            a.completers = [q]
        }
    }]), SwaggerEditor.service("FileLoader", ["$http", "defaults", "YAML", function(a, b, c) {
        function d(d, f) {
            return new Promise(function(g, h) {
                void 0 === f && (f = !1), _.startsWith(d, "http") && !f && (d = b.importProxyUrl + d), a({
                    method: "GET",
                    url: d,
                    headers: {
                        accept: "application/x-yaml,text/yaml,application/json,*/*"
                    }
                }).then(function(a) {
                    angular.isObject(a.data) ? c.dump(a.data, function(a, b) {
                        return a ? h(a) : void g(b)
                    }) : e(a.data).then(g, h)
                }, h)
            })
        }

        function e(a) {
            return new Promise(function(b, d) {
                if (!_.isString(a)) throw new TypeError("load function only accepts a string");
                try {
                    JSON.parse(a)
                } catch (c) {
                    return void b(a)
                }
                c.load(a, function(a, e) {
                    return a ? d(a) : void c.dump(e, function(a, c) {
                        return a ? d(a) : void b(c)
                    })
                })
            })
        }
        this.load = e, this.loadFromUrl = d
    }]), SwaggerEditor.service("Editor", ["Autocomplete", "ASTManager", "LocalStorage", "defaults", "$interval", function(a, b, c, d, e) {
        function f(a) {
            z && a && a.mark && a.reason && z.getSession().setAnnotations([{
                row: a.mark.line,
                column: a.mark.column,
                text: a.reason,
                type: "error"
            }])
        }

        function g(a, b) {}

        function h() {
            z.getSession().clearAnnotations()
        }

        function i(b) {
            window.e = z = b, ace.config.set("basePath", "bower_components/ace-builds/src-noconflict"), a.init(b), z.setOptions({
                fontFamily: "Source Code Pro",
                enableBasicAutocompletion: !0,
                enableLiveAutocompletion: !0,
                enableSnippets: !0
            }), k(), A.forEach(function(a) {
                a(C)
            }), A = new Set;
            var c = z.getSession();
            c.on("changeFold", l), m(c)
        }

        function j() {
            z && c.save("editor-settings", z.getOptions())
        }

        function k() {
            z && c.load("editor-settings").then(function(a) {
                a = a || {
                    theme: E
                }, z.setOptions(a)
            })
        }

        function l(a) {
            B.forEach(function(b) {
                b.call(null, a)
            })
        }

        function m(a) {
            a.setTabSize(2)
        }

        function n(a) {
            angular.isFunction(a) && A.add(a)
        }

        function o() {
            var a = z.getSession(),
                b = null;
            return a.foldAll(), b = a.unfold(), Array.isArray(b) ? b : []
        }

        function p(a) {
            return z.session.getLine(a)
        }

        function q(a) {
            _.isFunction(a) && B.add(a)
        }

        function r(a, b) {
            z && z.getSession().foldAll(a, b)
        }

        function s(a, b) {
            z && z.getSession().unfold(z.getSession().getFoldAt(a, b))
        }

        function t(a) {
            z.gotoLine(a)
        }

        function u() {
            return z ? z.getCursorPosition().row : null
        }

        function v() {
            ace.config.loadModule("ace/ext/settings_menu", function(a) {
                a.init(z), z.showSettingsMenu();
                var b = e(function() {
                    0 === $("#ace_settingsmenu").length && (j(), e.cancel(b), b = void 0)
                }, 300)
            })
        }

        function w() {
            window.confirm("Are you sure?") && z && (z.setOptions(D), j())
        }

        function x(a) {
            if (z) {
                var b = parseInt(z.getOption("fontSize"), 10);
                z.setOption("fontSize", b + a), j()
            }
        }

        function y() {
            z && z.focus()
        }
        var z = null,
            A = new Set,
            B = new Set,
            C = this,
            D = d.editorOptions || {},
            E = D.theme || "ace/theme/atom_dark";
        this.aceLoaded = i, this.ready = n, this.annotateYAMLErrors = f, this.annotateSwaggerError = g, this.clearAnnotation = h, this.getAllFolds = o, this.getLine = p, this.onFoldChanged = q, this.addFold = r, this.removeFold = s, this.gotoLine = t, this.lineInFocus = u, this.showSettings = v, this.saveEditorSettings = j, this.adjustFontSize = x, this.resetSettings = w, this.focus = y
    }]), SwaggerEditor.service("Builder", ["SwayWorker", function(a) {
        function b(b) {
            var d;
            return new Promise(function(e, f) {
                b || f({
                    specs: null,
                    errors: [{
                        emptyDocsError: "Empty Document Error"
                    }]
                });
                try {
                    d = c(b)
                } catch (a) {
                    f({
                        errors: [{
                            yamlError: a
                        }],
                        specs: null
                    })
                }
                if (d && _.isObject(d.definitions))
                    for (var g in d.definitions) _.isObject(d.definitions[g]) && !_.startsWith(g, "x-") && _.isEmpty(d.definitions[g].title) && (d.definitions[g].title = g);
                a.run({
                    definition: d
                }, function(a) {
                    a.errors.length ? f(a) : e(a)
                })
            })
        }
        var c = _.memoize(jsyaml.load);
        this.buildDocs = b
    }]), SwaggerEditor.service("ASTManager", ["YAML", "$log", function(a, b) {
        function c(b, c, d) {
            if ("string" != typeof b) throw new TypeError("yaml should be a string");
            if (!_.isArray(c)) throw new TypeError("path should be an array of strings");
            if ("function" != typeof d) throw new TypeError("cb should be a function.");
            var g = {
                    start: {
                        line: -1,
                        column: -1
                    },
                    end: {
                        line: -1,
                        column: -1
                    }
                },
                h = 0;
            a.compose(b, function(a, b) {
                function i(a) {
                    if (a.tag === e)
                        for (h = 0; h < a.value.length; h++) {
                            var b = a.value[h],
                                j = b[0],
                                k = b[1];
                            if (j.value === c[0]) return c.shift(), i(k)
                        }
                    if (a.tag === f) {
                        var l = a.value[c[0]];
                        l && l.tag && (c.shift(), i(l))
                    }
                    return d(c.length ? g : {
                        start: {
                            line: a.start_mark.line,
                            column: a.start_mark.column
                        },
                        end: {
                            line: a.end_mark.line,
                            column: a.end_mark.column
                        }
                    })
                }
                i(b)
            })
        }

        function d(c, d, g) {
            if ("string" != typeof c) throw new TypeError("yaml should be a string");
            if ("object" != typeof d || "number" != typeof d.line || "number" != typeof d.column) throw new TypeError("position should be an object with line and column properties");
            if ("function" != typeof g) throw new TypeError("cb should be a function.");
            a.compose(c, function(a, c) {
                function h(a) {
                    function b(a) {
                        return a.start_mark.line === a.end_mark.line ? d.line === a.start_mark.line && a.start_mark.column <= d.column && a.end_mark.column >= d.column : d.line === a.start_mark.line ? d.column >= a.start_mark.column : d.line === a.end_mark.line ? d.column <= a.end_mark.column : a.start_mark.line < d.line && a.end_mark.line > d.line
                    }
                    var c = 0;
                    if (!a || -1 === [e, f].indexOf(a.tag)) return g(i);
                    if (a.tag === e)
                        for (c = 0; c < a.value.length; c++) {
                            var j = a.value[c],
                                k = j[0],
                                l = j[1];
                            if (b(k)) return g(i);
                            if (b(l)) return i.push(k.value), h(l)
                        }
                    if (a.tag === f)
                        for (c = 0; c < a.value.length; c++) {
                            var m = a.value[c];
                            if (b(m)) return i.push(c.toString()), h(m)
                        }
                    return g(i)
                }
                if (a) return b.log("Error composing AST", a), g([]);
                var i = [];
                h(c)
            })
        }
        var e = "tag:yaml.org,2002:map",
            f = "tag:yaml.org,2002:seq";
        this.positionRangeForPath = function(a, d) {
            return new Promise(function(b) {
                c(a, d, b)
            }).catch(function(a) {
                b.error("positionRangeForPath error:", a)
            })
        }, this.pathForPosition = function(a, c) {
            return new Promise(function(b) {
                d(a, c, b)
            }).catch(function(a) {
                b.error("pathForPosition error:", a)
            })
        }
    }]), SwaggerEditor.service("Codegen", ["$http", "defaults", "Storage", "YAML", function(a, b, c, d) {
        this.getServers = function() {
            return b.codegen.servers ? a.get(b.codegen.servers).then(function(a) {
                return a.data
            }) : new Promise(function(a) {
                a([])
            })
        }, this.getClients = function() {
            return b.codegen.clients ? a.get(b.codegen.clients).then(function(a) {
                return a.data
            }) : new Promise(function(a) {
                a([])
            })
        }, this.getSDK = function(e, f) {
            var g = b.codegen[e].replace("{language}", f);
            return new Promise(function(b, e) {
                c.load("yaml").then(function(c) {
                    d.load(c, function(c, d) {
                        return c ? e(c) : void a.post(g, {
                            spec: d
                        }).then(function(a) {
                            angular.isObject(a.data) && a.data.link ? (window.location = a.data.link, b()) : e("Bad response from server: " + JSON.stringify(a))
                        }, e)
                    })
                })
            })
        }
    }]), SwaggerEditor.service("FocusedPath", ["ASTManager", "Editor", function(a, b) {
        this.isInFocus = function(c) {
            var d = b.lineInFocus(),
                e = a.pathForPosition(d);
            return Array.isArray(e) && _.isEqual(c, e.slice(0, c.length))
        }
    }]), SwaggerEditor.service("Storage", ["LocalStorage", "Backend", "defaults", function(a, b, c) {
        return c.useBackendForStorage ? b : a
    }]), SwaggerEditor.service("LocalStorage", ["$localStorage", "$rootScope", function(a, b) {
        function c(c, d) {
            null !== d && (Array.isArray(g[c]) && g[c].forEach(function(a) {
                a(d)
            }), _.debounce(function() {
                window.requestAnimationFrame(function() {
                    a[f][c] = d
                }), "yaml" === c && (b.progressStatus = "success-saved")
            }, 100)())
        }

        function d(b) {
            return new Promise(function(c) {
                c(b ? a[f][b] : a[f])
            })
        }

        function e(a, b) {
            angular.isFunction(b) && (g[a] || (g[a] = []), g[a].push(b))
        }
        var f = "SwaggerEditorCache",
            g = {};
        a[f] = a[f] || {}, this.save = c, this.reset = a.$reset, this.load = d, this.addChangeListener = e
    }]), SwaggerEditor.service("Backend", ["$http", "$q", "defaults", "$rootScope", "Builder", "ExternalHooks", "YAML", function(a, b, c, d, e, f, g) {
        function h(b) {
            var g = e.buildDocs(b, {
                resolve: !0
            });
            i("progress", "progress-saving");
            var h = {
                headers: {
                    "content-type": c.useYamlBackend ? "application/yaml; charset=utf-8" : "application/json; charset=utf-8"
                }
            };
            g.error || a.put(r, b, h).then(function() {
                f.trigger("put-success", [].slice.call(arguments)), d.progressStatus = "success-saved"
            }, function() {
                f.trigger("put-failure", [].slice.call(arguments)), d.progressStatus = "error-connection"
            })
        }

        function i(a, b) {
            o[a] = b, Array.isArray(m[a]) && m[a].forEach(function(a) {
                a(b)
            }), "yaml" === a && b && (c.useYamlBackend ? q(b) : g.load(b, function(a, b) {
                a || q(b)
            }))
        }

        function j(b) {
            if ("yaml" !== b) return new Promise(function(a, c) {
                b ? a(o[b]) : c()
            });
            var d = {
                headers: {
                    accept: c.useYamlBackend ? "application/yaml; charset=utf-8" : "application/json; charset=utf-8"
                }
            };
            return a.get(r, d).then(function(a) {
                return c.useYamlBackend ? (o.yaml = a.data, o.yaml) : a.data
            })
        }

        function k(a, b) {
            angular.isFunction(b) && (m[a] || (m[a] = []), m[a].push(b))
        }

        function l() {}
        var m = {},
            n = /^(\/|http(s)?\:\/\/)/,
            o = {},
            p = c.backendThrottle || 200,
            q = _.throttle(h, p, {
                leading: !1,
                trailing: !0
            }),
            r = c.backendEndpoint;
        if (!n.test(r)) {
            var s = _.endsWith(location.pathname, "/") ? location.pathname : location.pathname + "/";
            r = s + c.backendEndpoint, r = r.replace("//", "/")
        }
        this.save = i, this.reset = l, this.load = j, this.addChangeListener = k
    }]), SwaggerEditor.service("KeywordMap", ["defaults", function(a) {
        function b() {
            _.extend(this, {
                title: String,
                type: String,
                format: String,
                default: this,
                description: String,
                enum: [String],
                minimum: String,
                maximum: String,
                exclusiveMinimum: String,
                exclusiveMaximum: String,
                multipleOf: String,
                maxLength: String,
                minLength: String,
                pattern: String,
                not: String,
                $ref: String,
                definitions: {
                    ".": this
                },
                items: [this],
                minItems: String,
                maxItems: String,
                uniqueItems: String,
                additionalItems: [this],
                maxProperties: String,
                minProperties: String,
                required: String,
                additionalProperties: String,
                allOf: [this],
                properties: {
                    ".": this
                }
            })
        }
        var c = new b,
            d = ["http", "https", "ws", "wss"],
            e = {
                description: String,
                url: String
            },
            f = ["text/plain", "text/html", "text/xml", "text/csv", "application/json", "application/octet-stream", "application/xml", "application/vnd.", "application/pdf", "audio/", "image/jpeg", "image/gif", "image/png", "multipart/form-data", "video/avi", "video/mpeg", "video/ogg", "video/mp4"],
            g = {
                name: String,
                description: String
            },
            h = {
                name: String,
                in : ["body", "formData", "header", "path", "query"],
                description: String,
                required: ["true", "false"],
                type: ["string", "number", "boolean", "integer", "array"],
                format: String,
                schema: c
            },
            i = {
                ".": String
            },
            j = {
                description: String,
                schema: c,
                headers: {
                    ".": g
                },
                examples: f
            },
            k = {
                summary: String,
                description: String,
                schemes: {
                    ".": d
                },
                externalDocs: e,
                operationId: String,
                produces: {
                    ".": f
                },
                consumes: {
                    ".": f
                },
                deprecated: Boolean,
                security: i,
                parameters: [h],
                responses: {
                    ".": j
                },
                tags: [String]
            },
            l = {
                type: ["oauth2", "apiKey", "basic"],
                name: String,
                flow: ["application", "implicit", "accessCode"],
                scopes: String,
                tokenUrl: String,
                authorizationUrl: String,
                description: String
            },
            m = {
                swagger: ['"2.0"'],
                info: {
                    version: ["1.0.0", "0.0.0", "0.0.1", "something-we-all-get"],
                    title: String,
                    description: String,
                    termsOfService: String,
                    contact: {
                        name: String,
                        url: String,
                        email: String
                    },
                    license: {
                        name: String,
                        url: String
                    }
                },
                host: String,
                basePath: String,
                schemes: [d],
                produces: [f],
                consumes: [f],
                paths: {
                    "^/.?": {
                        parameters: [h],
                        "get|put|post|delete|options|head|patch": k
                    }
                },
                definitions: {
                    ".": c
                },
                parameters: [h],
                responses: {
                    "[2-6][0-9][0-9]": j
                },
                security: {
                    ".": {
                        ".": String
                    }
                },
                securityDefinitions: {
                    ".": l
                },
                tags: [{
                    name: String,
                    description: String
                }],
                externalDocs: {
                    ".": e
                }
            };
        this.get = function() {
            var b = angular.isObject(a.autocompleteExtension) ? a.autocompleteExtension : {};
            return _.extend(m, b)
        }
    }]), SwaggerEditor.service("Preferences", ["$localStorage", "defaults", function(a, b) {
        function c() {
            a.preferences = f
        }
        var d = [],
            e = {
                liveRender: !0,
                autoComplete: !0,
                keyPressDebounceTime: b.keyPressDebounceTime
            },
            f = _.extend(e, a.preferences);
        this.get = function(a) {
            return f[a]
        }, this.set = function(a, b) {
            if (void 0 === b) throw new Error("value was undefined");
            f[a] = b, c(), d.forEach(function(c) {
                c(a, b)
            })
        }, this.reset = function() {
            f = e, c()
        }, this.getAll = function() {
            return f
        }, this.onChange = function(a) {
            angular.isFunction(a) && d.push(a)
        }
    }]), SwaggerEditor.service("AuthManager", ["$sessionStorage", function(a) {
        a.$default({
            securities: {}
        }), this.basicAuth = function(b, c, d) {
            if ("$$hashKey" !== b) {
                if (!_.isObject(d)) throw new TypeError("Can not authenticate with options");
                d.username = d.username || "", d.password = d.password || "", d.isAuthenticated = !0, d.base64 = window.btoa(d.username + ":" + d.password), d.securityName = b, a.securities[b] = {
                    type: "basic",
                    security: c,
                    options: d
                }
            }
        }, this.oAuth2 = function(b, c, d) {
            "$$hashKey" !== b && (d.isAuthenticated = !0, a.securities[b] = {
                type: "oAuth2",
                security: c,
                options: d
            })
        }, this.apiKey = function(b, c, d) {
            "$$hashKey" !== b && (d.isAuthenticated = !0, a.securities[b] = {
                type: "apiKey",
                security: c,
                options: d
            })
        }, this.getAuth = function(b) {
            return a.securities[b]
        }, this.securityIsAuthenticated = function(b) {
            var c = a.securities[b];
            return c && c.options && c.options.isAuthenticated
        }
    }]), SwaggerEditor.service("Analytics", ["defaults", function(a) {
        var b = !1,
            c = !1,
            d = _.defaults(a, {
                analytics: {
                    google: {
                        id: null
                    }
                }
            }).analytics.google.id;
        this.initialize = function() {
            var a = window.ga;
            return window.ga && d ? void(c || (a("require", "linker"), a("linker:autoLink", ["swagger.io"]), a("create", d, "auto", {
                allowLinker: !0
            }), a("send", "pageview"), c = !0)) : void(b = !0)
        }, this.sendEvent = function() {
            if (!b) {
                if (!arguments.length) throw new Error("sendEvent was called with no arguments");
                Array.prototype.unshift.call(arguments, "event"), Array.prototype.unshift.call(arguments, "send"), window.ga.apply(window.ga, arguments)
            }
        }
    }]), SwaggerEditor.service("ExternalHooks", function() {
        var a = {
            "code-change": [],
            "put-success": [],
            "put-failure": []
        };
        SwaggerEditor.on = function(b, c) {
            if (!angular.isString(b)) throw new TypeError("eventName must be string");
            if (!angular.isFunction(c)) throw new TypeError("callback must be a function");
            if (!a[b]) throw new Error(b + " is not a valid event name");
            var d = a[b].some(function(a) {
                return a === c
            });
            d || a[b].push(c)
        }, this.trigger = function(b, c) {
            if (!angular.isString(b)) throw new TypeError("eventName must be string");
            if (!angular.isArray(c)) throw new TypeError("args must be an array");
            if (!a[b]) throw new Error(b + " is not a valid event name");
            a[b].forEach(function(a) {
                a.apply(null, c)
            })
        }
    }), SwaggerEditor.service("SwayWorker", function() {
        function a(a, c) {
            f.push({
                arg: a,
                cb: c
            }), b()
        }

        function b() {
            f.length && (g || (g = f.shift(), e.postMessage(g.arg)))
        }

        function c(a) {
            g && g.cb(a.data), g = null, b()
        }

        function d(a) {
            g && g.cb(a.data), g = null, b()
        }
        var e = new Worker("bower_components/sway-worker/index.js"),
            f = [],
            g = null;
        e.onmessage = c, e.onerror = d, this.run = a
    });
var compose = _.memoize(yaml.compose);
SwaggerEditor.service("YAML", function() {
        var a = new YAMLWorker("bower_components/yaml-worker/");
        this.load = a.load.bind(a), this.dump = a.dump.bind(a), this.compose = function(a, b) {
            try {
                b(null, compose(a))
            } catch (a) {
                b(a)
            }
        }
    }), SwaggerEditor.service("FoldStateManager", ["ASTManager", "Editor", "$rootScope", function(a, b, c) {
        function d(d, e) {
            a.positionRangeForPath(c.editorValue, d).then(function(a) {
                e ? b.addFold(a.start.line - 1, a.end.line - 1) : b.removeFold(a.start.line - 1, a.end.line - 1)
            })
        }

        function e(b) {
            var d = {
                line: b.data.start.row + 1,
                column: b.data.start.column + 1
            };
            a.pathForPosition(c.editorValue, d).then(function(a) {
                for (var d = "add" === b.action, e = c.specs; a.length && _.isObject(e);) e = e[a.shift()];
                _.isObject(e) && (e.$folded = !!d, c.$apply())
            })
        }

        function f(a, b) {
            if (!a) return a;
            var c = {};
            return _.keys(a).forEach(function(d) {
                _.isObject(a[d]) && _.isObject(b[d]) ? c[d] = f(a[d], b[d]) : "$folded" === d ? c[d] = a[d] : c[d] = b[d]
            }), c
        }
        b.onFoldChanged(e), this.foldEditor = d, this.getFoldedTree = f
    }]), SwaggerEditor.config(["$compileProvider", "$stateProvider", "$urlRouterProvider", "$logProvider", function(a, b, c, d) {
        c.otherwise("/"), b.state("home", {
            url: "/?import&tags&no-proxy",
            views: {
                "": {
                    templateUrl: "views/main.html",
                    controller: "MainCtrl"
                },
                "header@home": {
                    templateUrl: "views/header/header.html",
                    controller: "HeaderCtrl"
                },
                "editor@home": {
                    templateUrl: "views/editor/editor.html",
                    controller: "EditorCtrl"
                },
                "preview@home": {
                    templateUrl: "views/preview/preview.html",
                    controller: "PreviewCtrl"
                }
            }
        }), a.aHrefSanitizationWhitelist(".");
        var e = !/localhost/.test(window.location.host);
        a.debugInfoEnabled(!e), d.debugEnabled(!e)
    }]),
    function(a) {
        a.fn.scrollIntoView = function(b, c, d) {
            function e(b, c) {
                void 0 === c ? a.isFunction(f.complete) && f.complete.call(b) : f.smooth ? a(b).stop().animate({
                    scrollTop: c
                }, f) : (b.scrollTop = c, a.isFunction(f.complete) && f.complete.call(b))
            }
            var f = a.extend({}, a.fn.scrollIntoView.defaults);
            "object" == a.type(b) ? a.extend(f, b) : "number" == a.type(b) ? a.extend(f, {
                duration: b,
                easing: c,
                complete: d
            }) : 0 == b && (f.smooth = !1);
            var g = 1 / 0,
                h = 0;
            1 == this.size() ? null == (g = this.get(0).offsetTop) || (h = g + this.get(0).offsetHeight) : this.each(function(a, b) {
                b.offsetTop < g ? g = b.offsetTop : b.offsetTop + b.offsetHeight > h ? h = b.offsetTop + b.offsetHeight : null
            }), h -= g;
            for (var i = this.commonAncestor().get(0), j = a(window).height(); i;) {
                var k = i.scrollTop,
                    l = i.clientHeight;
                if (l > j && (l = j), 0 == l && "BODY" == i.tagName && (l = j), i.scrollTop != (null == (i.scrollTop += 1) || i.scrollTop) && null != (i.scrollTop -= 1) || i.scrollTop != (null == (i.scrollTop -= 1) || i.scrollTop) && null != (i.scrollTop += 1)) return void(k >= g ? e(i, g) : g + h > k + l ? e(i, g + h - l) : e(i, void 0));
                i = i.parentNode
            }
            return this
        }, a.fn.scrollIntoView.defaults = {
            smooth: !0,
            duration: null,
            easing: a.easing && a.easing.easeOutExpo ? "easeOutExpo" : null,
            complete: a.noop(),
            step: null,
            specialEasing: {}
        }, a.fn.isOutOfView = function(a) {
            var b = !0;
            return this.each(function() {
                var c = this.parentNode,
                    d = c.scrollTop,
                    e = c.clientHeight,
                    f = this.offsetTop,
                    g = this.offsetHeight;
                (a ? f > d + e : f + g > d + e) || (a ? d > f + g : d > f) || (b = !1)
            }), b
        }, a.fn.commonAncestor = function() {
            var b = [],
                c = 1 / 0;
            a(this).each(function() {
                var d = a(this).parents();
                b.push(d), c = Math.min(c, d.length)
            });
            for (var d = 0; d < b.length; d++) b[d] = b[d].slice(b[d].length - c);
            for (var d = 0; d < b[0].length; d++) {
                var e = !0;
                for (var f in b)
                    if (b[f][d] != b[0][d]) {
                        e = !1;
                        break
                    }
                if (e) return a(b[0][d])
            }
            return a([])
        }
    }(jQuery);
