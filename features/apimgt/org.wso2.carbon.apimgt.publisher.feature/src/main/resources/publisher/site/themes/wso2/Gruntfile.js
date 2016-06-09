module.exports = function(grunt) {
  grunt.initConfig({
    shell: {
        multiple: {
            command: [
                'bower install',
                'mkdir -p lib',
               'cp -r bower_components/bootstrap/dist/ libs/bootstrap_3.3.5',
               'cp -r bower_components/bootstrap-rating/ libs/bootstrap-rating',
               'cp -r bower_components/bootstrap-select/dist/ libs/bootstrap-select',
               'cp -r bower_components/jquery/dist/jquery.min.js libs/jquery.min.js',
               'mkdir -p lib/font-awesome/',
               'cp -r bower_components/font-awesome/css lib/font-awesome/css',
               'cp -r bower_components/font-awesome/fonts lib/font-awesome/fonts',
               'cp -r bower_components/handlebars/handlebars.min.js libs/handlebars.min.js',
               'cp -r bower_components/jasny-bootstrap/dist/ libs/jasny-bootstrap',
               'cp -r bower_components/jquery-validation/dist/ libs/jquery-validation/',
               'cp -r bower_components/zeroclipboard/dist/ libs/zeroclipboard'
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
