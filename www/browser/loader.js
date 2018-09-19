  /* global firebase: false, Promise: false */
var FireStoreClass = require('cordova-plugin-firestore.Firestore');

if (!window.Promise) {
  window.Promise = require('cordova-plugin-firestore.Promise');
}

var isInitialized = function(packageName) {
  var parent = window;
  var steps = packageName.split(/\./);
  var results = steps.filter(function(step) {
    if (step in parent) {
      parent = parent[step];
      return true;
    } else {
      return false;
    }
  });

  return results.length === steps.length;
};

var loadJs = function(options) {
  return new Promise(function(resolve, reject) {

    if (isInitialized(options.package)) {
      resolve();
    } else {
      var scriptTag = document.createElement('script');
      scriptTag.src = options.url;
      scriptTag.onload = function() {
        var timer = setInterval(function() {
          if (isInitialized(options.package)) {
            clearInterval(timer);
            resolve();
          }
        }, 10);
      };
      scriptTag.onerror = reject;
      document.body.appendChild(scriptTag);
    }
  });
};


function createInstance(options) {
  return new Promise(function(resolve, reject) {

    var initialised = false;

    for (var i = 0;i < firebase.apps.length;i++) {
      if (firebase.apps[i].options.projectId === options.config.projectId) {
        initialised = true;
      }
    }

    if (!initialised) {
      firebase.initializeApp(options.config);
    }

    // Default true, because firebase outputs error message
    var timestampsInSnapshots = 'timestampsInSnapshots' in options ?
      true : options.timestampsInSnapshots;

    var firestore = firebase.firestore();
    firestore.settings({
      'timestampsInSnapshots': timestampsInSnapshots
    });

    var instance = new FireStoreClass(firestore);

    if (options.persist) {
      firestore
        .enablePersistence()
        .then(function() {
          instance.database = firestore;
          resolve(instance);
        })
        .catch(reject);
    } else {
      instance.database = firestore;
      resolve(instance);
    }

  });
}

function initialise(options) {
  return loadJs({
    'url': 'https://www.gstatic.com/firebasejs/5.5.0/firebase-app.js',
    'package': 'firebase.app'
  })
  .then(function() {
    return loadJs({
      'url': 'https://www.gstatic.com/firebasejs/5.5.0/firebase-firestore.js',
      'package': 'firebase.firestore'
    });
  })
  .then(function() {
    return createInstance(options);
  });
}
module.exports = {
  initialise: initialise
};
