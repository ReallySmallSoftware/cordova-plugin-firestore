
var PLUGIN_NAME = 'Firestore',
  exec = require('cordova/exec'),
  Query = require("./Query"),
  DocumentReference = require("./DocumentReference"),
  __wrap = require("./__wrap");

function CollectionReference(path, id) {
  this._path = path;
  this._id = id;
  this._ref = this;
  this._queries = [];
}

CollectionReference.prototype = Object.create(Query.prototype, {
  firestore: {
    get: function () {
      throw "CollectionReference.firestore: Not supported";
    }
  },
  id: {
    get: function () {
      return this._id;
    }
  },
  parent: {
    get: function () {
      throw "CollectionReference.parent: Not supported";
    }
  }
});

CollectionReference.prototype.add = function (data) {
  var args = [this._path, __wrap(data)];

  return new Promise(function (resolve, reject) {
    exec(resolve, reject, PLUGIN_NAME, 'collectionAdd', args);
  });
};

CollectionReference.prototype.doc = function (id) {
  return new DocumentReference(this, id);
};

module.exports = CollectionReference;
