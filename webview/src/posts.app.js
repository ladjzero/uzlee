import Vue from 'vue'
import Posts from './Posts.vue'
import './polyfill.js';

let data = window._postsData

Vue.config.debug = true

new Vue({
  el: 'body',
  data: data,
  components: {
    posts: Posts
  }
})

document.addEventListener('click', function (e) {
  if (e.target.tagName == 'IMG') {
    let src = e.target.dataset.echo || e.target.src;
    let allSrc = Array.from(document.querySelectorAll('.content-image')).map(el => el.dataset.echo || el.src);
    let passOut = JSON.stringify({
      index: allSrc.indexOf(src),
      srcs: allSrc
    })
    console.log(passOut)
    UZLEE.onImageClick(passOut)
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