/* global Promise: false */

var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var QuerySnapshot = require("./query_snapshot");
var utilities = require('./utilities');

var cordovaUtils = require("cordova/utils");
  
function Query(ref, queryType, value) {
  this._ref = ref;
  this._ref._queries.push({
    "queryType": queryType,
    "value": value
  });
}

Query.prototype = Object.create({
  _isFunction: function (functionToCheck) {
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
  },
  endAt: function (snapshotOrVarArgs) {
    return new Query(this._ref, "endAt", utilities.wrap(snapshotOrVarArgs));
  },
  endBefore: function (snapshotOrVarArgs) {
    return new Query(this._ref, "endBefore", utilities.wrap(snapshotOrVarArgs, true));
  },
  limit: function (limit) {
    return new Query(this._ref, "limit", limit);
  },
  orderBy: function (field, direction) {
    if (direction === undefined) {
      direction = "ASCENDING";
    }

    var orderByField = {
      "field": field,
      "direction": direction
    };
    return new Query(this._ref, "orderBy", orderByField);
  },
  get: function () {
    var args = [this._ref.path, this._ref._queries];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'collectionGet', args);
    }).then(function (data) {
      return new QuerySnapshot(data);
    });
  },
  onSnapshot: function (optionsOrObserverOrOnNext, observerOrOnNextOrOnError, onError) {

    var callbackId = cordovaUtils.createUUID();

    var options = undefined;

    if (!this._isFunction(optionsOrObserverOrOnNext)) {
      options = optionsOrObserverOrOnNext;
    }

    var wrappedCallback;
    var wrappedError;

    if (this._isFunction(optionsOrObserverOrOnNext)) {
      wrappedCallback = function (querySnapshot) {
        optionsOrObserverOrOnNext(new QuerySnapshot(querySnapshot));
      };

      if (this._isFunction(observerOrOnNextOrOnError)) {
        wrappedError = observerOrOnNextOrOnError;
      }
    } else if (this._isFunction(observerOrOnNextOrOnError)) {
      wrappedCallback = function (querySnapshot) {
        observerOrOnNextOrOnError(new QuerySnapshot(querySnapshot));
      };

      if (this._isFunction(onError)) {
        wrappedError = onError;
      }
    } else {
      wrappedCallback = function (querySnapshot) { };
    }

    var args = [this._ref.path, this._ref._queries, options, callbackId];

    if (!onError) {
      wrappedError = function () {
        throw new Error("Undefined error in collectionOnSnapshot");
      };
    }

    exec(wrappedCallback, wrappedError, PLUGIN_NAME, 'collectionOnSnapshot', args);

    return function () {
      exec(function () { }, function (e) {
        throw new Error("Undefined error in collectionUnsubscribe", e);
      }, PLUGIN_NAME, 'collectionUnsubscribe', [callbackId]);
    };
  },
  startAfter: function (snapshotOrVarArgs) {
    return new Query(this._ref, "startAfter", utilities.wrap(snapshotOrVarArgs));
  },
  startAt: function (snapshotOrVarArgs) {
    return new Query(this._ref, "startAt", utilities.wrap(snapshotOrVarArgs));
  },
  where: function (fieldPath, opStr, passedValue) {
    var value = utilities.wrap(passedValue);

    var whereField = {
      "fieldPath": fieldPath,
      "opStr": opStr,
      "value": value
    };
    return new Query(this._ref, "where", whereField);
  }
});

module.exports = Query;
