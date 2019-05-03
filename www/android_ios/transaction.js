/* global Promise: false */

var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var DocumentSnapshot = require("./document_snapshot");
var Utilities = require('./utilities');

function Transaction(id) {
  this._id = id;
}

Transaction.prototype = {
  delete: function (documentReference) {
    var args = [this._id, documentReference.id, documentReference._collectionReference.path];

    var success = function () {
    };

    var failure = function () {
      throw new Error("Undefined error in transactionDocDelete");
    };

    exec(success, failure, PLUGIN_NAME, 'transactionDocDelete', args);

    return this;
  },
  get: function (documentReference) {
    var args = [this._id, documentReference.id, documentReference._collectionReference.path];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'transactionDocGet', args);
    }).then(function (data) {
      return new DocumentSnapshot(data);
    }).catch(function (err) {
    });
  },
  set: function (documentReference, data, options) {

    var args = [this._id, documentReference.id, documentReference._collectionReference.path, Utilities.wrap(data), options];

    var success = function () {
    };

    var failure = function () {
      throw new Error("Undefined error in transactionDocSet");
    };

    exec(success, failure, PLUGIN_NAME, 'transactionDocSet', args);

    return this;
  },
  update: function (documentReference, data) {

    var args = [this._id, documentReference.id, documentReference._collectionReference.path, Utilities.wrap(data)];

    var success = function () {
    };

    var failure = function () {
      throw new Error("Undefined error in transactionDocUpdate");
    };

    exec(success, failure, PLUGIN_NAME, 'transactionDocUpdate', args);

    return this;
  }
};

module.exports = Transaction;
