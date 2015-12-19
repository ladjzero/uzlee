console.log('posts.js is loading');

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

var queries = parseQueryString();
var theme = queries.theme;
var fontsize = queries.fontsize;



window.onScrollStateChange = function(state) {
    UZLEE.onScroll(state);
};

var linkClickHandler = function(e) {
    if (!galleryOnShow) {
        e.preventDefault();
        e.cancelBubble = true;
        UZLEE.onLinkClick(this.href);
    }
};

var imageClickHandler = function(e) {
    e.preventDefault();
    e.cancelBubble = true;
    UZLEE.onImageClick(this.src);
};

var model = {
    onProfileClick: function(post) {
        UZLEE.onProfileClick(post.author.id, post.author.name);
    },
    onProfileLoad: function(data, e) {
        e.target.style.visibility = 'visible';
        e.target.previousElementSibling.style.visibility = 'hidden';
    },
    onPostClick: function(post, e) {
        !galleryOnShow && UZLEE.onPostClick(post.id);
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

                el.className.trim() == 'content-image' && el.addEventListener('click', imageClickHandler);

                aa = el.querySelectorAll('.content-image');
                len = aa.length;

                for (var i = 0; i < len; ++i) {
                    aa[i].addEventListener('click', imageClickHandler);
                }
            }
        });
    }
};

document.addEventListener('DOMContentLoaded', function () {
    var bodyClassList = document.body.classList;

    bodyClassList.add(theme);
    bodyClassList.add('night' == theme ? 'night' : 'day');
    bodyClassList.add('font-size-' + fontsize);

    ko.applyBindings(model, document.getElementById('post-list'));

    UZLEE.onWebViewReady();
});

window.loadPosts = function(posts, removeAll) {
    console.log(JSON.stringify(posts[0], posts[1]));
    removeAll && model.posts.removeAll();

    posts.forEach(function(post) {
        model.posts.push(post);
    });
};

window.addPost = function(post) {
    model.posts.push(post);
};

window.scrollToPost = function(pid) {
    location.hash = 'pid-' + pid;
};

console.log('posts.js is loaded');