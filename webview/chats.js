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

window.clean = function () {
    document.body.innerHTML = '';
}

window.toBottom = function () {
    document.body.scrollTop = document.body.scrollHeight;
}

window.loadHTML = function (html) {
    clean();

    var list = document.createElement('div');
    list.innerHTML = html;
    document.body.appendChild(list);

    var images = list.querySelectorAll('div.summary img');

    for (var i = 0, len = images.length; i < len; ++i) {
        images[i].addEventListener('click', function () {
            UZLEE.onImageClick(this.src);
        });
    }

    var avatarLinks = list.querySelectorAll('a.avatar');
    var uidRe = /uid=(\d+)/;

    for (var i = 0, len = avatarLinks.length; i < len; ++i) {
        var link = avatarLinks[i];

        link.addEventListener('click', function (e) {
            e.preventDefault();

            try {
                UZLEE.onProfileClick(parseInt(this.href.match(uidRe)[1]), '');
            } catch (e) {}
        });
    }

    var avatars = list.querySelectorAll('.avatar > img');

    for (var i = 0, len = avatars.length; i < len; ++i) {
      var avt = avatars[i];
      avt.removeAttribute('onerror');
      avt.style.visibility = 'hidden';
      avt.addEventListener('load', function (e) {
        e.target.style.visibility = 'visible';
      });
      var src = avt.getAttribute('src');
      avt.setAttribute('src', src.replace('_avatar_small', '_avatar_middle'));
    }

    toBottom();
};

window.onScrollStateChange = function (state) {
    UZLEE.onScroll(state);
};

document.addEventListener('DOMContentLoaded', function () {
    var queries = parseQueryString();
    var theme = queries.theme;
    var fontsize = queries.fontsize;

    var bodyClassList = document.body.classList;

    bodyClassList.add(theme);
    bodyClassList.add('night' == theme ? 'night' : 'day');
    bodyClassList.add('font-size-' + fontsize);

    console.log("theme " + theme + ", fontsize " + fontsize);

    UZLEE.onWebViewReady();
});