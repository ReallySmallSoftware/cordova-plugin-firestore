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
  onSnapshot: function (callback, options) {

    var callbackId = cordovaUtils.createUUID();
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
    return new Query(this._ref, "startAfter", Utilities.wrap(snapshotOrVarArgs));
  },
  startAt: function (snapshotOrVarArgs) {
    return new Query(this._ref, "startAt", Utilities.wrap(snapshotOrVarArgs));
  },
  where: function (fieldPath, opStr, passedValue) {
    var value = Utilities.wrap(passedValue);

    var whereField = {
      "fieldPath": fieldPath,
      "opStr": opStr,
      "value": value
    };
    return new Query(this._ref, "where", whereField);
  }
});

module.exports = Query;
