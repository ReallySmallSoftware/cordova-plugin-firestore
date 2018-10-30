/* global Promise: false */

var exec = require('cordova/exec');
var utils = require("cordova/utils");
var CollectionReference = require("./CollectionReference");
var FirestoreTimestamp = require("./FirestoreTimestamp");
var Transaction = require("./Transaction");
var __wrap = require("./__wrap");
var GeoPoint = require("./GeoPoint");
var FirestoreOptions = require("./FirestoreOptions");

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
  if (FirestoreOptions.geopointPrefix === undefined) {
    this.geopointPrefix = "__GEOPOINT";
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
  return new Promise(function (resolve, reject) {

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
