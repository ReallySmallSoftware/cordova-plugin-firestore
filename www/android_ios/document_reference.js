var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var CollectionReference = require('./collection_reference');
var Utilities = require('./utilities');
var cordovaUtils = require("cordova/utils");
var DocumentSnapshot = require("./document_snapshot");
var utils = require('./utils');

function DocumentReference(collectionReference, id) {
  this._id = utils.getOrGenerateId(id);
  this._collectionReference = collectionReference;
}

DocumentReference.prototype = {
  _isFunction: function (functionToCheck) {
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
  },
  collection: function (collectionPath) {
    return new CollectionReference([collectionPath].join('/'), this);
  },
  delete: function () {
    var args = [this._collectionReference.path, this._id];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docDelete', args);
    }).then(function () {
      return;
    });
  },
  get: function () {
    var args = [this._collectionReference.path, this._id];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docGet', args);
    }).then(function (data) {
      return new DocumentSnapshot(data);
    });
  },
  onSnapshot: function (optionsOrObserverOrOnNext, observerOrOnNextOrOnError, onError) {

    var callbackId = cordovaUtils.createUUID();

    var args = [this._collectionReference.path, this._id, callbackId];

    if (!this._isFunction(optionsOrObserverOrOnNext)) {
      args.push(optionsOrObserverOrOnNext);
    }
    var wrappedCallback;
    var wrappedError;

    if (this._isFunction(optionsOrObserverOrOnNext)) {
      wrappedCallback = function (documentSnapshot) {
        optionsOrObserverOrOnNext(new DocumentSnapshot(documentSnapshot));
      };

      if (this._isFunction(observerOrOnNextOrOnError)) {
        wrappedError = observerOrOnNextOrOnError;
      }
    } else if (this._isFunction(observerOrOnNextOrOnError)) {
      wrappedCallback = function (documentSnapshot) {
        observerOrOnNextOrOnError(new DocumentSnapshot(documentSnapshot));
      };

      if (this._isFunction(onError)) {
        wrappedError = onError;
      }
    } else {
      wrappedCallback = function (documentSnapshot) { };
    }

    if (!wrappedError) {
      wrappedError = function () {
        throw new Error("Undefined error in docOnSnapshot");
      };
    }

    exec(wrappedCallback, wrappedError, PLUGIN_NAME, 'docOnSnapshot', args);

    return function () {
      exec(function () { }, function () {
        throw new Error("Undefined error in docUnsubscribe");
      }, PLUGIN_NAME, 'docUnsubscribe', [callbackId]);
    };
  },
  set: function (data, options) {

    var args = [this._collectionReference.path, this._id, Utilities.wrap(data), options];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docSet', args);
    });
  },
  update: function (data) {

    var args = [this._collectionReference.path, this._id, Utilities.wrap(data)];

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
      return this._collectionReference.path + '/' + this.id;
    }
  }
});

module.exports = DocumentReference;
