function mdEditor () {

  //Check if AngularJs and Showdown is defined and only load ng-Showdown if both are present
  if (typeof angular !== 'undefined' && typeof showdown !== 'undefined') {
    (function (module, showdown) {
      'use strict';

      module
          .provider('$showdown', ngShowdown)
          .directive('sdModelToHtml', ['$showdown', '$sanitize', '$sce', sdModelToHtmlDirective]) //<-- DEPRECATED: will be removed in the next major version release
          .directive('markdownToHtml', ['$showdown', '$sanitize', '$sce', markdownToHtmlDirective])
          .filter('sdStripHtml', ['$showdown', stripHtmlFilter]) //<-- DEPRECATED: will be removed in the next major version release
          .filter('stripHtml', ['$showdown', stripHtmlFilter]);

      /**
       * Angular Provider
       * Enables configuration of showdown via angular.config and Dependency Injection into controllers, views
       * directives, etc... This assures the directives and filters provided by the library itself stay consistent
       * with the user configurations.
       * If the user wants to use a different configuration in a determined context, he can use the "classic" Showdown
       * object instead.
       */
      function ngShowdown() {

        // Configuration parameters for Showdown
        var config = {
          extensions: [],
          sanitize: false
        };

        /**
         * Sets a configuration option
         *
         * @param {string} key Config parameter key
         * @param {string} value Config parameter value
         */
        /* jshint validthis: true */
        this.setOption = function (key, value) {
          config[key] = value;
          return this;
        };

        /**
         * Gets the value of the configuration parameter specified by key
         *
         * @param {string} key The config parameter key
         * @returns {string|null} Returns the value of the config parameter. (or null if the config parameter is not set)
         */
        this.getOption = function (key) {
          if (config.hasOwnProperty(key)) {
            return config[key];
          } else {
            return undefined;
          }
        };

        /**
         * Loads a Showdown Extension
         *
         * @param {string} extensionName The name of the extension to load
         */
        this.loadExtension = function (extensionName) {
          config.extensions.push(extensionName);

          return this;
        };

        function SDObject() {
          var converter = new showdown.Converter(config);

          /**
           * Converts a markdown text into HTML
           *
           * @param {string} markdown The markdown string to be converted to HTML
           * @returns {string} The converted HTML
           */
          this.makeHtml = function (markdown) {
            //alert(markdown)
            return converter.makeHtml(markdown);
          };

          this.makeMarkdown = function (html) {
            return converter.makeMarkdown(html);
          };

          /**
           * Strips a text of it's HTML tags. See http://stackoverflow.com/questions/17289448/angularjs-to-output-plain-text-instead-of-html
           *
           * @param {string} text
           * @returns {string}
           */
          this.stripHtml = function (text) {
            return String(text).replace(/<[^>]+>/gm, '');
          };

          /**
           * Gets the value of the configuration parameter of CONVERTER specified by key
           * @param {string} key The config parameter key
           * @returns {*}
           */
          this.getOption = function (key) {
            return converter.getOption(key);
          };

          /**
           * Gets the converter configuration params
           * @returns {*}
           */
          this.getOptions = function () {
            return converter.getOptions();
          };

          /**
           * Sets a configuration option
           *
           * @param {string} key Config parameter key
           * @param {string} value Config parameter value
           * @returns {SDObject}
           */
          this.setOption = function (key, value) {
            converter.setOption(key, value);
            return this;
          };

          /**
           * Get showdown's default options
           *
           * @param simple
           */
          this.getDefaultOptions = function(simple) {
            if (typeof showdown.getDefaultOptions !== 'undefined') {
              return showdown.getDefaultOptions(simple);
            } else {
              return null;
            }

          }
        }

        // The object returned by service provider
        this.$get = function () {
          return new SDObject();
        };
      }

      /**
       * @deprecated
       * Legacy AngularJS Directive to Md to HTML transformation
       *
       * Usage example:
       * <div sd-model-to-html="markdownText" ></div>
       *
       * @param {showdown.Converter} $showdown
       * @param {$sanitize} $sanitize
       * @param {$sce} $sce
       * @returns {*}
       */
      function sdModelToHtmlDirective($showdown, $sanitize, $sce) {
        return {
          restrict: 'A',
          link: getLinkFn($showdown, $sanitize, $sce),
          scope: {
            model: '=sdModelToHtml'
          },
          template: '<div ng-bind-html="trustedHtml"></div>'
        };
      }

      /**
       * AngularJS Directive to Md to HTML transformation
       *
       * Usage example:
       * <div markdown-to-html="markdownText" ></div>
       *
       * @param {showdown.Converter} $showdown
       * @param {$sanitize} $sanitize
       * @param {$sce} $sce
       * @returns {*}
       */
      function markdownToHtmlDirective($showdown, $sanitize, $sce) {
        return {
          restrict: 'A',
          link: getLinkFn($showdown, $sanitize, $sce),
          scope: {
            model: '=markdownToHtml'
          },
          template: '<div ng-bind-html="trustedHtml"></div>'
        };
      }

      function getLinkFn($showdown, $sanitize, $sce) {
        return function (scope, element, attrs) {
          scope.$watch('model', function (newValue) {
            var showdownHTML;
            if (typeof newValue === 'string') {
              showdownHTML = $showdown.makeHtml(newValue);
              //scope.trustedHtml = ($showdown.getOption('sanitize')) ? $sanitize(showdownHTML) : $sce.trustAsHtml(showdownHTML);
              scope.trustedHtml = showdownHTML;

            } else {
              scope.trustedHtml = typeof newValue;
            }
          });
        };
      }

      /**
       * AngularJS Filter to Strip HTML tags from text
       *
       * @returns {Function}
       */
      function stripHtmlFilter($showdown) {
        return function (text) {
          return $showdown.stripHtml(text);
        };
      }

    })(angular.module('ng-showdown', ['ngSanitize']), showdown);

  } else {
    document.cookie = 'version=develop';
    throw new Error('ng-showdown was not loaded because one of its dependencies (AngularJS or Showdown) was not met');
  }


  var app = angular.module('showdown.editor', ['ng-showdown', 'pageslide-directive', 'ngAnimate', 'ngRoute', 'ngCookies', 'ngSanitize']);


  app.controller('editorCtrl', ['$scope', '$showdown', '$http', '$cookies', '$sanitize', function ($scope, $showdown, $http, $cookies, $sanitize) {

    $scope.versions = ['develop', 'master'];
    $scope.version = $cookies.get('version') || 'develop';
    $scope.showModal = false;
    $scope.hashTxt = '';
    $scope.checked = false;
    $scope.firstLoad = true;
    $scope.text = '';
    $scope.checkOpts = [];
    $scope.numOpts = [];
    $scope.textOpts = [];

    var text = '';
    var savedCheckOpts = $cookies.getObject('checkOpts') || [];
    var savedNumOpts = $cookies.getObject('numOpts') || [];
    var savedTextOpts = $cookies.getObject('textOpts') || [];
    var defaultOpts = $showdown.getDefaultOptions(false);
    var checkOpts = {
      'omitExtraWLInCodeBlocks': true,
      'noHeaderId': false,
      'parseImgDimensions': true,
      'simplifiedAutoLink': true,
      'literalMidWordUnderscores': true,
      'strikethrough': true,
      'tables': true,
      'tablesHeaderId': false,
      'ghCodeBlocks': true,
      'tasklists': true,
      'smoothLivePreview': true,
      'prefixHeaderId': false,
      'disableForced4SpacesIndentedSublists': false,
      'ghCompatibleHeaderId': true,
      'smartIndentationFix': false
    };
    var numOpts = {
      'headerLevelStart': 3
    };
    var textOpts = {};

    if (defaultOpts !== null) {
      for (var opt in defaultOpts) {
        if (defaultOpts.hasOwnProperty(opt)) {
          var nOpt = (defaultOpts[opt].hasOwnProperty('defaultValue')) ? defaultOpts[opt].defaultValue : true;
          if (defaultOpts[opt].type === 'boolean') {
            if (!checkOpts.hasOwnProperty(opt)) {
              checkOpts[opt] = nOpt;
            }
          } else if (defaultOpts[opt].type === 'integer') {
            if (!numOpts.hasOwnProperty(opt)) {
              numOpts[opt] = nOpt;
            }
          } else {
            if (!textOpts.hasOwnProperty(opt)) {
              // fix bug in showdown's older version that specifies 'ghCompatibleHeaderId' as a string instead of boolean
              if (opt === 'ghCompatibleHeaderId') {
                continue;
              }
              if (!nOpt) {
                nOpt = '';
              }
              textOpts[opt] = nOpt;
            }
          }
        }
      }
    }

    for (opt in checkOpts) {
      if (checkOpts.hasOwnProperty(opt)) {
        $scope.checkOpts.push({name: opt, value: checkOpts[opt]});
      }
    }

    for (opt in numOpts) {
      if (numOpts.hasOwnProperty(opt)) {
        $scope.numOpts.push({name: opt, value: numOpts[opt]});
      }
    }

    for (opt in textOpts) {
      if (textOpts.hasOwnProperty(opt)) {
        $scope.textOpts.push({name: opt, value: textOpts[opt]});
      }
    }

    for (var i = 0; i < $scope.checkOpts.length; ++i) {
      for (var ii = 0; ii < savedCheckOpts.length; ++ii) {
        if ($scope.checkOpts[i].name === savedCheckOpts[ii].name) {
          $scope.checkOpts[i].value = savedCheckOpts[ii].value;
          break;
        }
      }
    }

    for (i = 0; i < $scope.numOpts.length; ++i) {
      for (ii = 0; ii < savedNumOpts.length; ++ii) {
        if ($scope.numOpts[i].name === savedNumOpts[ii].name) {
          $scope.numOpts[i].value = savedNumOpts[ii].value;
          break;
        }
      }
    }

    for (i = 0; i < $scope.textOpts.length; ++i) {
      for (ii = 0; ii < savedTextOpts.length; ++ii) {
        if ($scope.textOpts[i].name === savedTextOpts[ii].name) {
          $scope.textOpts[i].value = savedTextOpts[ii].value;
          break;
        }
      }
    }

    $scope.toggleMenu = function () {
      $scope.firstLoad = false;
      $scope.checked = !$scope.checked;
    };

    $scope.getHash = function () {
      $scope.hashTxt = document.location.origin + document.location.pathname + '#!/' + encodeURIComponent($scope.text);
      $scope.showModal = true;
    };

    $scope.closeModal = function () {
      $scope.showModal = false;
    };

    $scope.loadVersion = function () {
      $cookies.put('version', $scope.version);
      sessionStorage.setItem("text", $scope.text);
      location.reload();
    };

    $scope.updateOptions = function () {
      for (var i = 0; i < $scope.checkOpts.length; ++i) {
        $showdown.setOption($scope.checkOpts[i].name, $scope.checkOpts[i].value);
      }

      for (i = 0; i < $scope.numOpts.length; ++i) {
        if ($scope.numOpts[i].name === 'headerLevelStart') {
          if (isNaN($scope.numOpts[i].value) || $scope.numOpts[i].value < 1) {
            $scope.numOpts[i].value = 1;
          } else if ($scope.numOpts[i].value > 6) {
            $scope.numOpts[i].value = 6;
          }
        }
        $showdown.setOption($scope.numOpts[i].name, $scope.numOpts[i].value);
      }

      for (i = 0; i < $scope.textOpts.length; ++i) {
        $showdown.setOption($scope.textOpts[i].name, $scope.textOpts[i].value);
      }

      $cookies.putObject('checkOpts', $scope.checkOpts);
      $cookies.putObject('numOpts', $scope.numOpts);
      $cookies.putObject('textOpts', $scope.textOpts);
    };

    $scope.repaint = function () {
      sessionStorage.setItem("text", $scope.text);
      console.log($cookies.getAll()); // this is to force cookies to update
      location.reload();
    };

    //load available versions
    $http.get('https://api.github.com/repos/showdownjs/showdown/releases')
        .then(
            function (response) {
              for (var i = 0; i < response.data.length; ++i) {
                if (compareVersions(response.data[i].tag_name, '1.0.0') >= 0) {
                  $scope.versions.push(response.data[i].tag_name);
                }
              }
            },
            function (error) {
              console.error('Error retrieving versions', error);
            }
        );

    $scope.updateOptions(false);

    // get text from URL or load the default text
    if (window.location.hash) {
      var hashText = window.location.hash.replace(/^#!(\/)?/, '');
      console.log(window.location.hash, hashText);
      hashText = decodeURIComponent(hashText);
      $scope.text = hashText;
    } else if (sessionStorage.getItem('text')) {
      $scope.text = sessionStorage.getItem('text');
    } else {
      $scope.text = "";
    }
  }]);

  angular.bootstrap(document, ['showdown.editor']);
};
