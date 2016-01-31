module.exports = function(grunt) {
  var dest = '../app/src/main/assets/';

  grunt.initConfig({
    clean: {
      css: ['*.css']
    },
    sass: {
      dist: {
        options: {
          style: 'expanded',
          sourcemap: 'none'
        },
        files: {
         'posts.css': 'scss/posts.scss',
         'chats.css': 'scss/chats.scss'
        }
      }
    },
    postcss: {
      options: {
        map: false,
        processors: [
          require('autoprefixer')({
            browsers: ['Android 4.0']
          })
        ]
      },
      dist: {
        src: '*.css'
      }
    },
    cssmin: {
      options: {
        sourceMap: false
      },
      dist: {
        files: [{
          expand: true,
          src: ['*.css'],
          dest: dest
        }]
      }
    },
    uglify: {
      dist: {
        options: {
          sourceMap: false
        },
        files: [{
          expand: true,
          src: '*.js',
          dest: dest
        }]
      }
    },
    copy: {
      dist: {
        files: [
          {expand: true, src: ['*.html', '*.png'], dest: dest},
          {expand: true, cwd: 'node_modules/knockout/build/output/', src: 'knockout-latest.js', dest: dest}
        ]
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-sass');
  grunt.loadNpmTasks('grunt-postcss');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-copy');

  grunt.registerTask('default', ['clean', 'sass', 'postcss', 'cssmin', 'uglify', 'copy']);
};