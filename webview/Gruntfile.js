module.exports = function(grunt) {
  var dest = '../app/src/main/assets/';

  grunt.initConfig({
    sass: {
      dist: {
        options: {
          style: 'expanded',
          sourcemap: 'none'
        },
        files: {
         'posts.css': 'scss/posts.scss'
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
          {expand: true, src: ['*.html', '*.png'], dest: dest}
        ]
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-sass');
  grunt.loadNpmTasks('grunt-postcss');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-clean');

  grunt.registerTask('default', ['sass', 'postcss', 'cssmin', 'uglify', 'copy']);

};