module.exports = function(grunt) {
  grunt.initConfig({
    shell: {
        multiple: {
            command: [
                'bower install',
                'mkdir -p lib',
               'cp -r bower_components/bootstrap/dist/ lib/bootstrap',
               'cp -r bower_components/bootstrap-rating/ lib/bootstrap-rating',
               'cp -r bower_components/bootstrap-select/dist/ lib/bootstrap-select',
               'cp -r bower_components/jquery/dist/jquery.min.js lib/jquery.min.js',
               'mkdir -p lib/font-awesome/',
               'cp -r bower_components/font-awesome/css lib/font-awesome/css',
               'cp -r bower_components/font-awesome/fonts lib/font-awesome/fonts',
               'cp -r bower_components/handlebars/handlebars.min.js lib/handlebars.min.js',
               'cp -r bower_components/jasny-bootstrap/dist/ lib/jasny-bootstrap',
               'cp -r bower_components/jquery-validation/dist/ lib/jquery-validation/',
               'cp -r bower_components/zeroclipboard/dist/ lib/zeroclipboard'
            ].join('&&')
        }
    },
    shell2: {
        multiple: {
            command: [
               'echo test'
            ].join('&&')
        }
    },  
    less: {
      development: {
        options: {
          compress: true,
          yuicompress: true,
          optimization: 2
        },
        files: {
          // target.css file: source.less file
          "src/main.css": "src/main.less"
        }
      }
    },
    watch: {
      styles: {
        files: ['less/**/*.less'], // which files to watch
        tasks: ['less'],
        options: {
          nospawn: true
        }
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-shell');

  grunt.registerTask('watch', ['watch']);
  grunt.registerTask('fetch', ['shell']);
  grunt.registerTask('lessc', ['less']);
};
