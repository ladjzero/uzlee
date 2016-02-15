console.log('posts.js is loading');

/**
* @license MIT, GPL, do whatever you want
* @requires polyfill: Array.prototype.slice fix {@link https://gist.github.com/brettz9/6093105}
*/
if (!Array.from) {
    Array.from = function (object) {
        'use strict';
        return [].slice.call(object);
    };
}

var parseQueryString = function() {
    var str = window.location.search;
    var objURL = {};

    str.replace(
        new RegExp("([^?=&]+)(=([^&]*))?", "g"),
        function($0, $1, $2, $3) {
            objURL[$1] = $3;
        }
    );
    return objURL;
};

window.onScrollStateChange = function(state) {
    WebView2.onScroll(state);
};

var linkClickHandler = function(e) {
    e.preventDefault();
    e.cancelBubble = true;
    if (getSelection().toString().length == 0) {
        UZLEE.onLinkClick(this.href);
    }
};

var imageClickHandler = function(e) {
    e.preventDefault();
    e.cancelBubble = true;
    UZLEE.onImageClick(this.src);
};

var model = {
    printTime: function (){
        // console.log(Date.now());
    },
    onProfileClick: function(post) {
        UZLEE.onProfileClick(post.author.id, post.author.name);
    },
    onProfileLoad: function(data, e) {
        e.target.style.visibility = 'visible';
        e.target.previousElementSibling.style.visibility = 'hidden';
    },
    onPostClick: function(post, e) {
        UZLEE.onPostClick(post.id);
    },
    posts: ko.observableArray(),
    onPostRender: function(elements) {
        elements.forEach(function(el) {
            if (el.nodeName != '#text') {
                el.tagName == 'A' && el.addEventListener('click', linkClickHandler);

                var aa = el.getElementsByTagName('A'),
                    len = aa.length;

                for (var i = 0; i < len; ++i) {
                    aa[i].addEventListener('click', linkClickHandler);
                }

                var images = el.className.trim() == 'content-image' ? [el] : Array.from(el.querySelectorAll('.content-image'));

                images.forEach(function (img) {
                    img.dataset.echo = img.src;
                    img.src = 'placeholder.png';
                    img.addEventListener('click', imageClickHandler);
                });
            }
        });

        setTimeout(echo.render, 100);
    }
};

window._posts = model.posts;

document.addEventListener('DOMContentLoaded', function () {
    var queries = parseQueryString();
    var theme = queries.theme;
    var fontsize = queries.fontsize;
    var showSig = queries.showsig;

    var bodyClassList = document.body.classList;

    bodyClassList.add(theme);
    bodyClassList.add('night' == theme ? 'night' : 'day');
    bodyClassList.add('font-size-' + fontsize);
    showSig == 'true' && bodyClassList.add('show-sig');

    console.log("theme " + theme + ", fontsize " + fontsize);

    ko.applyBindings(model, document.getElementById('post-list'));

    echo.init({
        offset: 100,
        throttle: 250,
        unload: false,
        callback: function (element, op) {
            console.log(element, 'has been', op + 'ed')
        }
    });

    console.log('echo is initialized.');
});

window.loadPosts = function(posts, removeAll) {
    // console.log('loadPosts ' + Date.now());
    // console.log(JSON.stringify(posts[0], posts[1]));
    removeAll && model.posts.removeAll();

    posts.forEach(function(post) {
        model.posts.push(post);
    });

    echo.render();
};

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
};

console.log('posts.js is loaded');