var PLUGIN_NAME = 'Firestore',
  exec = require('cordova/exec'),
  SubCollectionReference = require('./SubCollectionReference'),
  __wrap = require('./__wrap');

function DocumentReference(collectionReference, id) {
  this._id = id;
  this._collectionReference = collectionReference;
}

DocumentReference.prototype = {
  _isFunction: function (functionToCheck) {
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
  },
  collection: function (collectionPath) {
    return new SubCollectionReference(this, collectionPath);
  },
  delete: function () {
    var args = [this._collectionReference._path, this._id];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docDelete', args);
    }).then(function () {
      return;
    });
  },
  get: function () {
    var args = [this._collectionReference._path, this._id];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docGet', args);
    }).then(function (data) {
      return new DocumentSnapshot(data);
    });
  },
  onSnapshot: function (optionsOrObserverOrOnNext, observerOrOnNextOrOnError, onError) {

    var callbackId = utils.createUUID();

    var args = [this._collectionReference._path, this._id, callbackId];

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

    var args = [this._collectionReference._path, this._id, __wrap(data), options];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docSet', args);
    });
  },
  update: function (data) {

    var args = [this._collectionReference._path, this._id, __wrap(data)];

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
  }
});

module.exports = DocumentReference;
