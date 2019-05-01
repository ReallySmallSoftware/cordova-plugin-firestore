/* global Promise: false */

var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var DocumentReference;
var Utilities = require("./utilities");
var Path = require('./path');
var Query = require("./query");

function CollectionReference(path, parent) {

  /*
   * weird, in tests if DocumentReference is imported at the top of the file
   * it is an empty object and not a function
   */
  if (DocumentReference === undefined) {
    DocumentReference = require('./document_reference');
  }
  this._path = new Path(path);
  this._id = this._path.id;
  this._ref = this;
  this._queries = [];
  this._parent = parent || null;
}

console.log('Query', typeof Query.prototype, Query.prototype);
console.log('Path', typeof Path.prototype, Path.prototype);
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
      return this._parent;
    }
  },
  path: {
    get: function() {
      return this._path.cleanPath;
    }
  }
});

CollectionReference.prototype.add = function (data) {
  var args = [this._path.cleanPath, Utilities.wrap(data)];

  return new Promise(function (resolve, reject) {
    exec(resolve, reject, PLUGIN_NAME, 'collectionAdd', args);
  });
};

CollectionReference.prototype.doc = function (id) {
  return new DocumentReference(this, id);
};

CollectionReference.prototype.newInstance = function(documentReference, path) {
  return new CollectionReference(path, documentReference);
};

module.exports = CollectionReference;
