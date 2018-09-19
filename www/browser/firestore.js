 /* global firebase: false, Promise: false */

function Firestore(firestore) {
  var self = this;

  Object.defineProperty(self, 'database', {
    'value': firestore,
    'writable': false
  });
}

Firestore.prototype.get = function() {
  return this.database;
};

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

module.exports = Firestore;
