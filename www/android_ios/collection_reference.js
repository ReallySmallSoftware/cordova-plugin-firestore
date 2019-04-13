/* global Promise: false */

var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var Query = require("./Query");
var DocumentReference = require("./DocumentReference");
var Utilities = require("./Utilities");

function CollectionReference(documentReference, path) {
  console.log("CollectionReference" + path);

  this._path = path;
  this._id = Utilities.leaf(path);
  this._ref = this;
  this._queries = [];
  this._documentReference = documentReference;
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
      return this._documentReference;
    }
  },
  path: {
    get: function () {
      return this._path;
    }
  },
});

CollectionReference.prototype.add = function (data) {
  var args = [this._path, Utilities.wrap(data)];

  return new Promise(function (resolve, reject) {
    exec(resolve, reject, PLUGIN_NAME, 'collectionAdd', args);
  });
};

CollectionReference.prototype.doc = function (path) {
  return new DocumentReference(this, Utilities.combinePath(this._path, path));
};

CollectionReference.prototype.newInstance = function(documentReference, path) {
  return new CollectionReference(documentReference, path);
}

module.exports = CollectionReference;
