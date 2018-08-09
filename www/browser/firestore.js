/* global firebase: false, Promise: false */

var PLUGIN_NAME = 'Firestore';

var loadJS = function(url, loaded, implementationCode, location) {

  if (!loaded) {
    var scriptTag = document.createElement('script');
    scriptTag.src = url;

    scriptTag.onload = implementationCode;
    scriptTag.onreadystatechange = implementationCode;

    location.appendChild(scriptTag);
  } else {
    implementationCode();
  }
};

function Firestore(options, resolve) {

  var self = this;

  var initialise = function() {

    if (firebase.apps.length === 0) {
      firebase.initializeApp(options.browser);
    }

    if (options.persist) {
      firebase.firestore().enablePersistence().then(function() {
        self.database = firebase.firestore();
        resolve(self);
      });
    } else {
      self.database = firebase.firestore();
      resolve(self);
    }
  };

  var firebaseLoaded = "firebase" in window;
  var firestoreLoaded = false;
  if (firebaseLoaded) {
    firestoreLoaded = "firestore" in firebase;
  }

  loadJS('https://www.gstatic.com/firebasejs/5.2.0/firebase.js', firebaseLoaded, function() {
    loadJS('https://www.gstatic.com/firebasejs/5.2.0/firebase-firestore.js', firestoreLoaded, initialise, document.body);
  }, document.body);

}

Firestore.prototype.get = function() {
  return this.database;
};

Object.defineProperties(Firestore.prototype, {
  FieldValue: {
    get: function() {
      return firebase.firestore.FieldValue;
    }
  }
});

module.exports = {
  initialise: function(options) {
    return new Promise(function(resolve, reject) {
      var db = new Firestore(options, resolve);
    });
  }
};
