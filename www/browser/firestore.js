var PLUGIN_NAME = 'Firestore';

var loadJS = function(url, implementationCode, location) {
  var scriptTag = document.createElement('script');
  scriptTag.src = url;

  scriptTag.onload = implementationCode;
  scriptTag.onreadystatechange = implementationCode;

  location.appendChild(scriptTag);
};

function Firestore(persist, firebaseOptions) {

  var self = this;

  var initialise = function() {

    firebase.initializeApp(firebaseOptions);

    if (persist) {
      firebase.firestore().enablePersistence().then(function() {
        self.database = firebase.firestore();
      });
    } else {
      self.database = firebase.firestore();
    }
  }
  loadJS('https://www.gstatic.com/firebasejs/4.5.0/firebase.js', function() {
    loadJS('https://www.gstatic.com/firebasejs/4.5.0/firebase-firestore.js', initialise, document.body);
  }, document.body);

}

Firestore.prototype.get = function() {
  return this.database;
};

module.exports = {
  initialise: function(persist, firebaseOptions) {
    return new Firestore(persist, firebaseOptions);
  }
};
