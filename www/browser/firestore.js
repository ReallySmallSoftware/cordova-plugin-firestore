var PLUGIN_NAME = 'Firestore';

function Firestore(persist) {

  var self = this;

  if (persist) {
    firebase.firestore().enablePersistence().then(function() {
      self.database = firebase.firestore();
    });
  } else {
    self.database = firebase.firestore();
  }
}

Firestore.prototype.get = function() {
  return this.database;
};

module.exports = {
  initialise: function() {
    return new Firestore();
  }
};
