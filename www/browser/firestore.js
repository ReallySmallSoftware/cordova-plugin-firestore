/* global firebase: false, Promise: false */

var PLUGIN_NAME = 'Firestore';

var loadJS = function(url, implementationCode, location) {
  var scriptTag = document.createElement('script');
  scriptTag.src = url;

  scriptTag.onload = implementationCode;
  scriptTag.onreadystatechange = implementationCode;

  location.appendChild(scriptTag);
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
  loadJS('https://www.gstatic.com/firebasejs/4.7.0/firebase.js', function() {
    loadJS('https://www.gstatic.com/firebasejs/4.7.0/firebase-firestore.js', initialise, document.body);
  }, document.body);

}

Firestore.prototype.get = function() {
  return this.database;
};

module.exports = {
  initialise: function(options) {
    return new Promise(function(resolve, reject) {
      var db = new Firestore(options, resolve);
    });
  }
};
