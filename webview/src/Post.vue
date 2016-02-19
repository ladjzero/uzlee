<template>
    <div class="post-container">
        <div class="post-header">
            <div class="profile-wrapper"
                 data-bind="click: $root.onProfileClick, clickBubble: false">
                <span>{{post.author.name.charAt(0).toUpperCase()}}</span>
                <div class="profile" id="uid-{{post.author.id}}" v-bind:style="{backgroundImage: 'url(' + post.author.image + ')'}"></div>
            </div>
            <div class="name-and-date">
                <span class="name">{{post.author.name}}</span>
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
export default {
  props: ['post'],
  created () {
    console.log('abcd');
  }
}
</script>