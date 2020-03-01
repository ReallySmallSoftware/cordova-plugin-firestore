/* global Promise: false */

var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var DocumentSnapshot = require("./document_snapshot");
var Utilities = require('./utilities');

function WriteBatch(id) {
  this._id = id;
}

WriteBatch.prototype = {
  delete: function (documentReference) {
    var args = [this._id, documentReference.id, documentReference._collectionReference.path];

    var success = function () {
    };

    var failure = function () {
      throw new Error("Undefined error in batchDocDelete");
    };

    exec(success, failure, PLUGIN_NAME, 'batchDocDelete', args);

    return this;
  },
  set: function (documentReference, data, options) {

    var args = [this._id, documentReference.id, documentReference._collectionReference.path, Utilities.wrap(data), options];

    var success = function () {
    };

    var failure = function () {
      throw new Error("Undefined error in batchDocSet");
    };

    exec(success, failure, PLUGIN_NAME, 'batchDocSet', args);

    return this;
  },
  update: function (documentReference, data) {

    var args = [this._id, documentReference.id, documentReference._collectionReference.path, Utilities.wrap(data)];

    var success = function () {
    };

    var failure = function () {
      throw new Error("Undefined error in batchDocUpdate");
    };

    exec(success, failure, PLUGIN_NAME, 'batchDocUpdate', args);

    return this;
  },
  commit: function() {
    var args = [this._id];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'batchCommit', args);
    }).then(function () {
      return;
    });
  }
};

module.exports = Transaction;
