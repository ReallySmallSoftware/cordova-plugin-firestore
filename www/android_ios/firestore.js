/* global Promise: false */

var exec = require('cordova/exec');
var utils = require("cordova/utils");
var CollectionReference = require("./CollectionReference"),
  FirestoreTimestamp = require("./FirestoreTimestamp"),
  Transaction = require("./Transaction"),
  __wrap = require("./__wrap"),
  FirestoreOptions = require("./FirestoreOptions");

if (!window.Promise) {
  window.Promise = require('./Promise');
}

var PLUGIN_NAME = 'Firestore';

var __transactionList = {};

if (!String.prototype.startsWith) {
  String.prototype.startsWith = function (search, pos) {
    return this.substr(!pos || pos < 0 ? 0 : +pos, search.length) === search;
  };
}


var FieldValue = {
  delete: function () {
    return FirestoreOptions.fieldValueDelete;
  },
  serverTimestamp: function () {
    return FirestoreOptions.fieldValueServerTimestamp;
  }
};



function Firestore(options) {
  FirestoreOptions = options;

  if (FirestoreOptions.datePrefix === undefined) {
    this.datePrefix = "__DATE:";
  }
  if (FirestoreOptions.fieldValueDelete === undefined) {
    this.fieldValueDelete = "__DELETE";
  }
  if (FirestoreOptions.fieldValueServerTimestamp === undefined) {
    this.fieldValueServerTimestamp = "__SERVERTIMESTAMP";
  }
  if (FirestoreOptions.persist === undefined) {
    this.persist = true;
  }
  if (FirestoreOptions.timestampsInSnapshots === undefined) {
    this.timestampsInSnapshots = false;
  }

  exec(function () { }, null, PLUGIN_NAME, 'initialise', [FirestoreOptions]);
}

Firestore.prototype = {
  get: function () {
    return this;
  },
  batch: function () {
    throw "Firestore.batch: Not supported";
  },
  collection: function (path) {
    return new CollectionReference(path);
  },
  disableNetwork: function () {
    throw "Firestore.disableNetwork: Not supported";
  },
  doc: function () {
    throw "Firestore.doc: Not supported";
  },
  enableNetwork: function () {
    throw "Firestore.enableNetwork: Not supported";
  },
  enablePersistence: function () {
    throw "Firestore.enablePersistence: Not supported. Please specify using initialisation options.";
  },
  runTransaction: function (updateFunction) {

    var transactionId = utils.createUUID();
    var transaction = new Transaction(transactionId);

    __transactionList[transactionId] = {
      "transaction": transaction,
      "updateFunction": updateFunction
    };

    var args = [transactionId];

    return new Promise(function (resolve, reject) {
      var wrappedResolve = function (data) {
        delete __transactionList[transactionId];
        resolve(data);
      };
      var wrappedReject = function (err) {
        delete __transactionList[transactionId];
        reject(err);
      };
      exec(wrappedResolve, wrappedReject, PLUGIN_NAME, 'runTransaction', args);
    });
  },
  setLogLevel: function () {
    throw "Firestore.setLogLevel: Not supported";
  },
  settings: function () {
    throw "Firestore.settings: Not supported";
  }
};

Object.defineProperties(Firestore.prototype, {
  FieldValue: {
    get: function () {
      return FieldValue;
    }
  }
});













function initialise(options) {
  return new Promise(function (resolve, reject) {
    resolve(new Firestore(options));
  });
}

module.exports = {
  initialise: initialise, // original implementation
  initialize: initialise, // better for common usage

  __executeTransaction: function (transactionId) {
    var result;

    __transactionList[transactionId].updateFunction(__transactionList[transactionId].transaction).then(function (result) {
      var args = [transactionId, __wrap(result)];
      exec(function () { }, function () { }, PLUGIN_NAME, 'transactionResolve', args);
    }).catch(function (error) {
      throw new Error("Unexpected error in transaction " + error);
    });
  },
  newTimestamp: function(date) {
    return new FirestoreTimestamp(date.getTime());
  }
};
