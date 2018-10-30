/* global Promise: false, FirestoreOptions: false, FirestoreTimestamp: false */

function DocumentSnapshot(data) {
  this._data = data;

  if (data.exists) {
    this._data._data = this._parse(this._data._data);
  }
}

DocumentSnapshot.prototype = {
  _parse: function (data) {
    var keys = Object.keys(data);

    for (var i = 0; i < keys.length; i++) {
      var key = keys[i];

      if (Object.prototype.toString.call(data[key]) === '[object String]' &&
        data[key].startsWith(FirestoreOptions.datePrefix)) {
        var length = data[key].length;
        var prefixLength = FirestoreOptions.datePrefix.length;

        var timestamp = data[key].substr(prefixLength, length - prefixLength);

        if (FirestoreOptions.timestampsInSnapshots) {
          data[key] = new FirestoreTimestamp(parseInt(timestamp));
        } else {
          data[key] = new Date(parseInt(timestamp));
        }
      } else if (Object.prototype.toString.call(data[key]) === '[object Object]') {
        data[key] = this._parse(data[key]);
      }
    }

    return data;
  },
  _fieldPath: function (obj, i) {
    return obj[i];
  },
  data: function () {
    return this._data._data;
  },
  get: function (fieldPath) {
    return fieldPath.split('.').reduce(this._fieldPath, this._data);
  }
};

Object.defineProperties(DocumentSnapshot.prototype, {
  exists: {
    get: function () {
      return this._data.exists;
    }
  },
  id: {
    get: function () {
      return this._data.id;
    }
  },
  metadata: {
    get: function () {
      throw "DocumentReference.metadata: Not supported";
    }
  },
  ref: {
    get: function () {
      return this._data.ref;
    }
  }
});

module.exports = DocumentSnapshot;
