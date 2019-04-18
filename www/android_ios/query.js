/* global Promise: false */

var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var QuerySnapshot = require("./query_snapshot");
var __wrap = require('./__wrap');

var utils = require("cordova/utils");
  
function Query(ref, queryType, value) {
  this._ref = ref;
  this._ref._queries.push({
    "queryType": queryType,
    "value": value
  });
}

Query.prototype = {
  endAt: function (snapshotOrVarArgs) {
    return new Query(this._ref, "endAt", __wrap(snapshotOrVarArgs));
  },
  endBefore: function (snapshotOrVarArgs) {
    return new Query(this._ref, "endBefore", __wrap(snapshotOrVarArgs, true));
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
  onSnapshot: function (callback, options) {

    var callbackId = utils.createUUID();
    var args = [this._ref.path, this._ref._queries, options, callbackId];

    var callbackWrapper = function (data) {
      callback(new QuerySnapshot(data));
    };
    exec(callbackWrapper, function () {
      throw new Error("Undefined error in collectionOnSnapshot");
    }, PLUGIN_NAME, 'collectionOnSnapshot', args);

    return function () {
      exec(function () { }, function () {
        throw new Error("Undefined error in collectionUnsubscribe");
      }, PLUGIN_NAME, 'collectionUnsubscribe', [callbackId]);
    };
  },
  startAfter: function (snapshotOrVarArgs) {
    return new Query(this._ref, "startAfter", __wrap(snapshotOrVarArgs));
  },
  startAt: function (snapshotOrVarArgs) {
    return new Query(this._ref, "startAt", __wrap(snapshotOrVarArgs));
  },
  where: function (fieldPath, opStr, passedValue) {
    var value = __wrap(passedValue);

    var whereField = {
      "fieldPath": fieldPath,
      "opStr": opStr,
      "value": value
    };
    return new Query(this._ref, "where", whereField);
  }
};

module.exports = Query;
