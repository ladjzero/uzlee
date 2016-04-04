import Vue from 'vue'
import './fling.js'
import Posts from './Posts.vue'
import echo from './echo.js'

let data = window._postsData

window.onScrollStateChange = function (state) {
  if (state == 'start') {
    window.isScrolling = true
  } else if (state == 'end') {
    window.isScrolling = false
  }
}

window._postsData.enableEcho = function (yes) {
  yes && echo.init({
    offset: 100,
    throttle: 250,
    unload: false
  })
}

Vue.config.debug = true

new Vue({
  el: 'body',
  data: data,
  components: {
    posts: Posts
  }
})

document.addEventListener('click', function (e) {
  if (e.target.classList.contains('content-image') && e.target.src.indexOf('http') > -1) {
    !window.isScrolling && UZLEE.onImageClick(e.target.src);
  }
})

window.scrollToPost = function(pid) {
  var id = 'pid-' + pid;
  var calledTimes = arguments[1] || 10;

  if (document.getElementById(id)) {
    console.log('Scroll to pid-' + pid + 'successfully.');
      location.hash = '';
      location.hash = id;
    } else if (calledTimes) {
      setTimeout(scrollToPost.bind(null, pid, calledTimes - 1), 500);
  }
}