import Vue from 'vue'
import Posts from './Posts.vue'

let data = window._postsData

Vue.config.debug = true

new Vue({
  el: 'body',
  data: data,
  components: {
    posts: Posts
  }
})