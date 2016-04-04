(function (window) {
// `debounce` is taken from `underscore`
var debounce = function(func, wait, immediate) {
  var timeout, args, context, timestamp, result;

  var later = function() {
    var last = Date.now() - timestamp;
    if (last < wait) {
      timeout = setTimeout(later, wait - last);
    } else {
      timeout = null;
      if (!immediate) {
        result = func.apply(context, args);
        context = args = null;
      }
    }
  };

  return function() {
    context = this;
        args = arguments;
    timestamp = Date.now();
    var callNow = immediate && !timeout;
    if (!timeout) {
      timeout = setTimeout(later, wait);
    }
    if (callNow) {
      result = func.apply(context, args);
      context = args = null;
    }

    return result;
  };
};

var isOnScrolling = false,
  isOnTouch = false,
  touchEventId;

var touchHandler = function (e) {
  console.log('touchType:' + e.type);
  switch (e.type) {
  case 'touchstart':
    isOnTouch = true;
    isOnScrolling && dispatchScrollEvent('start');
    break;
  case 'touchend':
    isOnTouch = false;
    dispatchScrollEvent(isOnScrolling ? 'fling' : 'end');
    break;
  }
};

var scrollHandler = function (e) {
  console.log('scroll:' + e);
  switch(e) {
  case 'start':
    isOnScrolling = true;
    dispatchScrollEvent('start');
    break;
  case 'end':
    isOnScrolling = false;
    !isOnTouch && dispatchScrollEvent('end');
  }
}

var scrollHandler1 = debounce(function () {
  scrollHandler('start');
}, 150, true);

var scrollHandler2 = debounce(function () {
  scrollHandler('end');
}, 950);

var dispatchScrollEvent = function (state) {
  console.log('scrollState:' + state);
  window.onScrollStateChange && window.onScrollStateChange(state);
}

window.addEventListener('touchstart', touchHandler);
window.addEventListener('touchend', touchHandler);
window.addEventListener('touchmove', touchHandler);
window.addEventListener('touchcancel', touchHandler);
window.addEventListener('scroll', scrollHandler1);
window.addEventListener('scroll', scrollHandler2);
})(window);
