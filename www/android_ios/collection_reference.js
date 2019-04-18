/* global Promise: false */

var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var Query = require("./query");
var DocumentReference;
var __wrap = require("./__wrap");
var Path = require('./path');

console.log('What is document_reference at import', typeof DocumentReference);

function CollectionReference(path, parent) {
  // Weird, in tests if DocumentReference is imported at the top of the file
  // it is an empty object and not a function
  if (DocumentReference === undefined) {
    DocumentReference = require('./document_reference');
  }
  this._path = new Path(path);
  this._id = this._path.id;
  this._ref = this;
  this._queries = [];
  this._parent = parent || null;
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
  var args = [this._path.cleanPath, __wrap(data)];

  return new Promise(function (resolve, reject) {
    exec(resolve, reject, PLUGIN_NAME, 'collectionAdd', args);
  });
};

CollectionReference.prototype.doc = function (id) {
  return new DocumentReference(this, id);
};

module.exports = CollectionReference;
