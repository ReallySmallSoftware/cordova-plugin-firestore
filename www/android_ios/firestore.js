/* global Promise: false */

var exec = require('cordova/exec');
var utils = require("cordova/utils");
var DocumentReference = require("./document_reference");
var CollectionReference = require("./collection_reference");
var FirestoreTimestamp = require("./firestore_timestamp");
var Transaction = require("./transaction");
var GeoPoint = require("./geo_point");
var Utilities = require("./utilities");

var PLUGIN_NAME = 'Firestore';

var __transactionList = {};

var __firestoreOptions = {};

if (!String.prototype.startsWith) {
  String.prototype.startsWith = function (search, pos) {
    return this.substr(!pos || pos < 0 ? 0 : +pos, search.length) === search;
  };
}

var FieldValue = {
  delete: function () {
    return __firestoreOptions.fieldValueDelete;
  },
  serverTimestamp: function () {
    return __firestoreOptions.fieldValueServerTimestamp;
  }
};

function Firestore(options) {

  if (options.datePrefix === undefined) {
    options.datePrefix = "__DATE:";
  }
  if (options.timestampPrefix === undefined) {
    options.timestampPrefix = "__TIMESTAMP:";
  }
  if (options.fieldValueDelete === undefined) {
    options.fieldValueDelete = "__DELETE";
  }
  if (options.geopointPrefix === undefined) {
    options.geopointPrefix = "__GEOPOINT:";
  }
  if (options.fieldValueServerTimestamp === undefined) {
    options.fieldValueServerTimestamp = "__SERVERTIMESTAMP";
  }
  if (options.persist === undefined) {
    options.persist = true;
  }
  if (options.timestampsInSnapshots === undefined) {
    options.timestampsInSnapshots = false;
  }

  __firestoreOptions = options;

  exec(function () { }, null, PLUGIN_NAME, 'initialise', [__firestoreOptions]);
}

Firestore.prototype = {
  get: function () {
    return this;
  },
  batch: function () {
    throw "Firestore.batch: Not supported";
  },
  collection: function (path) {
    return new CollectionReference(null, path);
  },
  disableNetwork: function () {
    throw "Firestore.disableNetwork: Not supported";
  },
  doc: function (path) {
    var collectionReference = new CollectionReference(null, "");
    var documentReference = new DocumentReference(collectionReference, path);
    return documentReference;
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
  setLogLevel: function (logLevel) {
    if (['debug', 'error', 'silent'].indexOf(logLevel) > -1) {
      return new Promise(function(resolve, reject) {
        exec(resolve, reject, PLUGIN_NAME, 'setLogLevel', [logLevel]);
      });
    } else {
      return Promise.reject("supported logLevel is one of 'debug', 'error' or 'silent'");
    }
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
  },
  GeoPoint: {
    get: function () {
      return GeoPoint;
    }
  }
});

function initialise(options) {
  return new Promise(function (resolve) {

    if (options && options.config) {
      var normalizedOptions = {};
      normalizedOptions.applicationId = options.config.applicationId || options.config.applicationID;
      normalizedOptions.apiKey = options.config.apiKey || options.config.apikey;
      normalizedOptions.clientId = options.config.clientId || options.config.clientID;
      normalizedOptions.gcmSenderId = options.config.gcmSenderId || options.config.gcmSenderID;
      normalizedOptions.databaseUrl = options.config.databaseUrl || options.config.databaseURL;
      normalizedOptions.projectId = options.config.projectId || options.config.projectID;
      normalizedOptions.storageBucket = options.config.storageBucket;
      normalizedOptions.authDomain = options.config.authDomain;
      normalizedOptions.googleAppId = options.config.googleAppId || options.config.googleAppID;

      delete options.config;
      options.config = normalizedOptions;
    }
    resolve(new Firestore(options));
  });
}

module.exports = {
  initialise: initialise, // original implementation
  initialize: initialise, // better for common usage

  __executeTransaction: function (transactionId) {
    __transactionList[transactionId].updateFunction(__transactionList[transactionId].transaction).then(function (result) {
      var args = [transactionId, Utilities.wrap(result)];
      exec(function () { }, function () { }, PLUGIN_NAME, 'transactionResolve', args);
    }).catch(function (error) {
      throw new Error("Unexpected error in transaction " + error);
    });
  },

  newTimestamp: function(date) {
    return new FirestoreTimestamp(date.getTime() / 1000, date.getMilliseconds() * 1000);
  },

  options: function() {
    return __firestoreOptions;
  },

  Timestamp: function(seconds, nanoseconds) {
    return new FirestoreTimestamp(seconds, nanoseconds);
  },

  GeoPoint: function(latitude, longitude) {
    return new GeoPoint(latitude, longitude);
  }
};
