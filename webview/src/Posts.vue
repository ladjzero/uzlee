<template>
  <ol id="post-list" class="unstyled" v-bind:class="classes">
    <li v-for="post in posts" class="post">
      <post :post="post"></post>
    </li>
  </ol>
</template>

<script>
import Post from './Post.vue'

export default {
  props: ['posts', 'theme', 'fontsize'],
  data: function () {
    let isDay = this.theme != 'night';
    let fontClass = 'font-size-' + this.fontsize;

    return {
        classes: [isDay ? 'day' : '', this.theme, fontClass]
    }
  },
  components: {
    Post
  },
  created () {
    console.log('abcd');
  }
}
</script>

<style lang="sass">
@import "../scss/uzlee.scss";

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

img[src="http://www.hi-pda.com/forum/images/common/back.gif"] {
    display: none!important;
}

.body {
    line-height: 1.6;
}

.pstatus {
    display: block;
    font-style: normal;
    text-align: center;
    font-size: 12px;
    color: gray;
    margin: 12px 0;
}

.night .pstatus {
    color: $nightTextPrimary;
}

@each $theme, $primary in $primaries {
    .#{$theme} blockquote {
        border-left: 4px solid lighten($primary, .2);
        padding: 12px;
        margin: 24px;
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
    color: #585858;
}

.night .name-and-date .date {
    color: #464646;
}

    li:last-child  {
        padding-bottom: $toolbar-height;
    }

    .post-header {
        padding-top: 12px;
    }
        .profile-wrapper {
            position: relative;
            width: $profile-height;
            height: $profile-height;
            display: inline-block;

            > span {
                width: 100%;
                position: absolute;
                line-height: $profile-height;
                font-size: 20px;
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
            vertical-align: top;
            height: $profile-height;
            margin-left: 12px;
            padding: 2px 0;
            display: inline-block;

            .name {
                color: black;
                font-size: 14px;
                display: block;
                line-height: 16px;
            }

            .date {
                color: #3e3e3e;
                font-size: 10px;
                line-height: 12px;
            }
        }

        .actions {
            float: right;
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