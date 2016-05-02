module.exports = function (grunt) {
  'use strict';

  grunt.loadNpmTasks('grunt-contrib-connect');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-gh-pages');

  grunt.initConfig({
    'connect': {
      demo: {
        options: {
          open: {
            target: 'http://localhost:8000/'
          },
          keepalive: true
        }
      }
    },
    pkg: grunt.file.readJSON('bower.json'),
    uglify: {
      rating: {
        files: {
          'bootstrap-rating.min.js': ['bootstrap-rating.js']
        }
      },
      options: {
        banner:
          '// <%= pkg.name %> - v<%= pkg.version %> - (c) <%= grunt.template.today("yyyy") %> <%= pkg.author.name %> \n' +
          '// <%= pkg.homepage %> <%= pkg.license %>\n'
      }
    },
    'gh-pages': {
      options: {
        user: {
          name: '<%= pkg.author.name %>',
          email: '<%= pkg.author.email %>'
        }
      },
      src: [
        'index.html',
        'bootstrap-rating.js',
        'bootstrap-rating.css',
        'bower_components/**/*'
      ]
    }
  });

  grunt.registerTask('serve', ['connect']);
  grunt.registerTask('deploy', ['gh-pages']);
};
