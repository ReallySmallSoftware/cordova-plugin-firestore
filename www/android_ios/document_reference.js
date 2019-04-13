/* global Promise: false, DocumentSnapshot: false, DocumentSnapshot: false */

var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var Utilities = require('./Utilities');
var utils = require("cordova/utils");
var DocumentSnapshot = require("./DocumentSnapshot");

function DocumentReference(collectionReference, path) {
  console.log("DocumentReference" + path);
  this._path = Utilities.stripLeaf(path);
  this._id = Utilities.leaf(path);
  this._collectionReference = collectionReference;
}

DocumentReference.prototype = {
  _isFunction: function (functionToCheck) {
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
  },
  collection: function (collectionPath) {
    return this._collectionReference.newInstance(this, Utilities.combinePath(this._path, this._id, collectionPath));
  },
  delete: function () {
    var args = [this._path, this._id];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docDelete', args);
    }).then(function () {
      return;
    });
  },
  get: function () {
    var args = [this._path, this._id];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docGet', args);
    }).then(function (data) {
      return new DocumentSnapshot(data);
    });
  },
  onSnapshot: function (optionsOrObserverOrOnNext, observerOrOnNextOrOnError, onError) {

    var callbackId = utils.createUUID();

    var args = [this._path, this._id, callbackId];

    if (!this._isFunction(optionsOrObserverOrOnNext)) {
      args.push(optionsOrObserverOrOnNext);
    }
    var wrappedCallback;

    if (this._isFunction(optionsOrObserverOrOnNext)) {
      wrappedCallback = function (documentSnapshot) {
        optionsOrObserverOrOnNext(new DocumentSnapshot(documentSnapshot));
      };
    } else if (this._isFunction(observerOrOnNextOrOnError)) {
      wrappedCallback = function (documentSnapshot) {
        observerOrOnNextOrOnError(new DocumentSnapshot(documentSnapshot));
      };
    } else {
      wrappedCallback = function (documentSnapshot) { };
    }

    exec(wrappedCallback, function () {
      throw new Error("Undefined error in docOnSnapshot");
    }, PLUGIN_NAME, 'docOnSnapshot', args);

    return function () {
      exec(function () { }, function () {
        throw new Error("Undefined error in docUnsubscribe");
      }, PLUGIN_NAME, 'docUnsubscribe', [callbackId]);
    };
  },
  set: function (data, options) {

    var args = [this._path, this._id, Utilities.wrap(data), options];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docSet', args);
    });
  },
  update: function (data) {

    var args = [this._path, this._id, Utilities.wrap(data)];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docUpdate', args);
    });
  }
};

Object.defineProperties(DocumentReference.prototype, {
  firestore: {
    get: function () {
      throw "DocumentReference.firestore: Not supported";
    }
  },
  id: {
    get: function () {
      return this._id;
    }
  },
  parent: {
    get: function () {
      return this._collectionReference;
    }
  },
  path: {
    get: function () {
      return this._path;
    }
  }
});

module.exports = DocumentReference;
