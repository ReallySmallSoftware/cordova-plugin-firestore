/* global firebase: false, Promise: false */

var isInitialized = function (packageName) {
  var parent = window;
  var steps = packageName.split(/\./);
  var results = steps.filter(function (step) {
    if (step in parent) {
      parent = parent[step];
      return true;
    } else {
      return false;
    }
  });

  return results.length === steps.length;
};

var loadJs = function (options) {
  return new Promise(function (resolve, reject) {

    if (isInitialized(options.package)) {
      resolve();
    } else {
      var scriptTag = document.createElement('script');
      scriptTag.src = options.url;
      scriptTag.onload = function () {
        var timer = setInterval(function () {
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

function Firestore(options, resolve, reject) {

  var self = this;

  var initialised = false;

  for (var i = 0; i < firebase.apps.length; i++) {
    if (firebase.apps[i].options.projectId === options.config.projectId) {
      initialised = true;
    }
  }

  if (!initialised) {
    firebase.initializeApp(options.config);
  }

  // Default false, to maintain backwards compatability
  var timestampsInSnapshots = 'timestampsInSnapshots' in options ?
    options.timestampsInSnapshots : true;

  var firestore = firebase.firestore();
  firestore.settings({
    'timestampsInSnapshots': timestampsInSnapshots
  });

  if (options.persist) {
    firestore
      .enablePersistence({
        experimentalTabSynchronization: true
      })
      .then(function () {
        self.database = firestore;
        resolve(self);
      })
      .catch(reject);
  } else {
    self.database = firestore;
    resolve(self);
  }
}

Firestore.prototype.get = function () {
  return this.database;
};


function createInstance(options) {
  return new Promise(function (resolve, reject) {

    Firestore.FieldValue = firebase.firestore.FieldValue;
    Firestore.FieldPath = firebase.firestore.FieldPath;
    Firestore.GeoPoint = firebase.firestore.GeoPoint;

    var db = new Firestore(options, resolve, reject);
  });
}

function initialise(options) {
  return loadJs({
    'url': 'https://www.gstatic.com/firebasejs/7.6.1/firebase-app.js',
    'package': 'firebase.app'
  })
    .then(function () {
      return loadJs({
        'url': 'https://www.gstatic.com/firebasejs/7.6.1/firebase-firestore.js',
        'package': 'firebase.firestore'
      });
    })
    .then(function () {
      return createInstance(options);
    });
}

module.exports = {
  initialise: initialise, // original developer name
  initialize: initialise, // better for common usage
  Firestore: Firestore,

  newTimestamp: function (date) {
    return firebase.firestore.Timestamp.fromDate(date);
  },

  Timestamp: function (seconds, nanoseconds) {
    return new firebase.firestore.Timestamp(seconds, nanoseconds);
  },

  GeoPoint: function (latitude, longitude) {
    return new firebase.firestore.GeoPoint(latitude, longitude);
  }
};
