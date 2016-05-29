<template>
    <div class="post-container">
        <div class="post-header">
            <div class="profile-wrapper" @click="userClick(post.author.id, post.author.name, $event)" v-if="postsStyle.showProfileImage">
                <span>{{post.author.name.charAt(0).toUpperCase()}}</span>
                <div class="profile" id="uid-{{post.author.id}}" v-bind:style="{backgroundImage: 'url(' + post.author.image + ')'}"></div>
            </div>
            <div class="name-and-date">
                <span class="name" @click="userClick(post.author.id, post.author.name, $event)">{{post.author.name}}</span>
                <span class="date">{{post.timeStr}}</span>
            </div>
            <div class="actions">
                <span class="post-index" v-bind:class="{'is-lz': post.lz}">{{post.postIndex}}#</span>
            </div>
        </div>
        <p class="body">{{{post.body}}}</p>
    </div>
</template>

<script>
import echo from './echo.js'

export default {
  props: ['post', 'postsStyle'],
  compiled: function () {
    let images = this.$el.querySelectorAll('.content-image')

    Array.from(images).forEach(function (img) {
      img.dataset.echo = img.src
      img.src = 'img/placeholder.png'
    });

    let links = this.$el.querySelectorAll('a')

    Array.from(links).forEach(a => a.addEventListener('click', function (e) {
      e.preventDefault()
      e.cancelBubble = true
      e.stopPropagation && e.stopPropagation()
      !window.isScrolling && UZLEE.onLinkClick(a.href)
    }))

    setTimeout(echo.render, 100)
  },
  methods: {
    userClick: function (uid, name, e) {
      e.preventDefault()
      e.cancelBubble = true
      e.stopPropagation && e.stopPropagation()
      !window.isScrolling && UZLEE.onProfileClick(uid, name)
    }
  }
}
</script>