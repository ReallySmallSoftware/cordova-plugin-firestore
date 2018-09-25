var PLUGIN_NAME = 'Firestore',
  exec = require('cordova/exec'),
  DocumentOfSubCollectionReference = require("./DocumentOfSubCollectionReference");

function SubCollectionReference(documentReference, id) {
  this._id = id;
  this._documentReference = documentReference;
}
SubCollectionReference.prototype.doc = function (id) {
  return new DocumentOfSubCollectionReference(this, id);
};
SubCollectionReference.prototype.get = function () {
  var args = [this._documentReference._collectionReference._path,
  this._documentReference._id,
  this._id];
  return new Promise(function (resolve, reject) {
    exec(resolve, reject, PLUGIN_NAME, 'subCollectionGet', args);
  }).then(function (data) {
    return new QuerySnapshot(data);
  });
};

module.exports = SubCollectionReference;
