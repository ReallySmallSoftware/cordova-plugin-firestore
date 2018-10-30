/* global Promise: false, DocumentSnapshot: false */

var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var __wrap = require('./__wrap');

function DocumentOfSubCollectionReference(subCollectionReference, id) {
  this._id = id;
  this._subCollectionReference = subCollectionReference;
}
DocumentOfSubCollectionReference.prototype = {
  delete: function () {
    var args = [this._subCollectionReference._documentReference._collectionReference._path,
    this._subCollectionReference._documentReference._id,
    this._subCollectionReference._id,
    this._id];
    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docOfSubCollectionDelete', args);
    }).then(function () {
      return;
    });
  },
  get: function () {
    var args = [this._subCollectionReference._documentReference._collectionReference._path,
    this._subCollectionReference._documentReference._id,
    this._subCollectionReference._id,
    this._id];
    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docOfSubCollectionGet', args);
    }).then(function (data) {
      return new DocumentSnapshot(data);
    });
  },
  set: function (data, options) {
    var args = [
      this._subCollectionReference._documentReference._collectionReference._path,
      this._subCollectionReference._documentReference._id,
      this._subCollectionReference._id,
      this._id,
      __wrap(data),
      options];
    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docOfSubCollectionSet', args);
    });
  },
  update: function (data) {
    var args = [this._subCollectionReference._documentReference._collectionReference._path,
    this._subCollectionReference._documentReference._id,
    this._subCollectionReference._id,
    this._id,
    __wrap(data)];
    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docOfSubCollectionUpdate', args);
    });
  }
};

module.exports = DocumentOfSubCollectionReference;
