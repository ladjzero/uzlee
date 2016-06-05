<template>
  <ol id="post-list" class="unstyled" v-bind:class="classes">
    <li v-for="post in posts" v-if="!prepareRender || ~selected.indexOf(post.postIndex + '')" id="pid-{{post.id}}" class="post" v-bind:class="postClasses" @click="postClick(post, $event)">
      <post :post="post" :posts-style="postsStyle"></post>
      <input v-show="postsStyle.selection" value="{{post.postIndex}}" type="checkbox" v-model="selected" @change="onSelect(selected)"/>
    </li>
  </ol>
</template>

<script>
import Post from './Post.vue'

export default {
  props: ['posts', 'postsStyle', 'onSelect', 'prepareRender', 'selected'],
  computed: {
    classes: function () {
      let theme = this.postsStyle.theme
      let isDay = theme != 'night'
      let fontClass = 'font-size-' + this.postsStyle.fontsize

      return [isDay ? 'day' : '', theme, fontClass]
    },
    postClasses: function () {
        return [this.postsStyle.selection && !this.prepareRender ? 'selection' : '']
    }
  },
  components: {
    Post
  },
  methods: {
    postClick: function (post, e) {
      if (!window.isScrolling && !this.postsStyle.selection && !e.target.classList.contains('content-image')) {
        UZLEE.onPostClick(post.id);
      }
    }
  }
}
</script>

<style lang="sass">
@import "../scss/uzlee.scss";

#title {
    text-align: center;
    margin-bottom: 24px;
}

#footer {
    color: grey;
    font-size: 12px;
}

ol.unstyled {
    padding-bottom: $toolbar-height;
}

.post {
    position: relative;
}

.post>input {
    position: absolute;
    right: 0;
    top: 12px;
}

.selection .post-container {
    left: -24px;
    position: relative;
}

.post::after {
    content: '';
    display: block;
    background: #e8e8e8;
    height: 1px;
}

.post:last-child::after {
    content: none;
}

.night .post::after {
    background: #3e3e3e;
}

.error {
    opacity: 0.5;   
}

/* 1.25 dpr */
@media
 (-webkit-min-device-pixel-ratio: 1.25),
 (min-resolution: 120dpi){
    .post::after {
        transform: scaleY(0.8);
        transform-origin: 0 0;
    }
}

/* 1.5 dpr */
@media
(-webkit-min-device-pixel-ratio: 1.5),
(min-resolution: 144dpi){
    .post::after {
        transform: scaleY(0.666666);
        transform-origin: 0 0;
    }
}

/* 2 dpr */
@media
(-webkit-min-device-pixel-ratio: 2),
(min-resolution: 192dpi) {
    .post::after {
        transform: scaleY(0.5);
        transform-origin: 0 0;
    }
}

/* 3 dpr For iPhone 6 plus and similar devices */
@media
(-webkit-min-device-pixel-ratio: 3),
(min-resolution: 3dppx), /* Default way */
(min-resolution: 350dpi) /* dppx fallback */ {
    .post::after {
        transform: scaleY(0.333333);
        transform-origin: 0 0;
    }
}

strong i {
    font-style: normal;
}

body {
    margin: 0 12px;
}

[size] {
    font-size: inherit;
}

.show-sig .sig {
    display: block;
    text-align: right;
    font-size: 12px;
}

.sig {
    display: none;
}

img[src$="common/back.gif"] {
    display: none!important;
}

.body {
    line-height: 1.6;
    margin: 12px 0;
}

.pstatus {
    display: block;
    font-style: normal;
    text-align: center;
    font-size: 12px;
    color: gray;
    margin: 12px 0;
}

.pstatus + br {
    display: none;
}

.night .pstatus {
    color: $nightTextPrimary;
}

@each $theme, $primary in $primaries {
    .#{$theme} blockquote {
        border-left: 4px solid rgba($primary, .7);
        padding: 12px;
        margin: 12px 24px;
    }

    .#{$theme} a {
        color: $primary
    }
}

@each $font_size, $font_size_val in $fontSizes {
    .#{$font_size} .body {
        font-size: $font_size_val;
    }
}


.night .body, .night [color], .night a {
    color: $nightTextPrimary
}

.night .name-and-date .name {
    color: #464646;
}

.night .name-and-date .date {
    color: #464646;
}

    .post-header {
        padding-top: 12px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
        .profile-wrapper {
            position: relative;
            width: $profile-height;
            height: $profile-height;
            margin-right: 12px;

            > span {
                width: 100%;
                position: absolute;
                line-height: $profile-height;
                font-size: 16px;
                color: #c8c8c8;
                text-align: center;
            }

            .profile {
                position: relative;
                width: $profile-height;
                height: $profile-height;
                background-size: cover;
                background-position: center;
            }
        }

        .name-and-date {
            flex: 1;
            .name {
                color: black;
                font-size: 14px;
                font-weight: bolder;
            }

            .date {
                color: #a3a3a3;
                padding-left: 12px;
                font-size: 10px;
            }
        }

        .actions {
            color: #686868;

            .is-lz {
                font-weight: bolder;
            }
        }

    .body {
        img {
            max-width: 100%;
            vertical-align: middle;
        }

        img[smilieid] {
            width: 20px;
            height: 20px;
        }

        .content-image {
            vertical-align: top;
            display: block;
            margin: 2px auto;
            min-height: 20px;

            & + br {
                display: none;
            }
        }
    }
</style>