/* global Promise: false */

var exec = require('cordova/exec');
var utils = require("cordova/utils");

var PLUGIN_NAME = 'Firestore';
var FirestoreOptions = {
  "datePrefix": "__DATE:",
  "fieldValueDelete": "__DELETE",
  "fieldValueServerTimestamp": "__SERVERTIMESTAMP",
  "persist": true,
  "timestampsInSnapshots": false
};

var __transactionList = {};

if (!String.prototype.startsWith) {
  String.prototype.startsWith = function (search, pos) {
    return this.substr(!pos || pos < 0 ? 0 : +pos, search.length) === search;
  };
}

function __wrap(data) {

  if (Object.prototype.toString.call(data) === '[object Date]') {
    return FirestoreOptions.datePrefix + data.getTime();
  }

  if (Object.prototype.toString.call(data) !== '[object Object]') {
    return data;
  }

  var keys = Object.keys(data);
  for (var i = 0; i < keys.length; i++) {
    var key = keys[i];

    if (Object.prototype.toString.call(data[key]) === '[object Date]') {
      data[key] = FirestoreOptions.datePrefix + data[key].getTime();
    } else if (Object.prototype.toString.call(data[key]) === '[object Object]') {
      data[key] = __wrap(data[key]);
    }
  }
  return data;
}

var FieldValue = {
  delete: function () {
    return FirestoreOptions.fieldValueDelete;
  },
  serverTimestamp: function () {
    return FirestoreOptions.fieldValueServerTimestamp;
  }
};

function Transaction(id) {
  this._id = id;
}

Transaction.prototype = {
  delete: function (documentReference) {
    var args = [this._id, documentReference._id, documentReference._collectionReference._path];

    var success = function () {
    };

    var failure = function () {
      throw new Error("Undefined error in transactionDocDelete");
    };

    exec(success, failure, PLUGIN_NAME, 'transactionDocDelete', args);

    return this;
  },
  get: function (documentReference) {
    var args = [this._id, documentReference._id, documentReference._collectionReference._path];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'transactionDocGet', args);
    }).then(function (data) {
      return new DocumentSnapshot(data);
    }).catch(function (err) {
    });
  },
  set: function (documentReference, data, options) {

    var args = [this._id, documentReference._id, documentReference._collectionReference._path, __wrap(data), options];

    var success = function () {
    };

    var failure = function () {
      throw new Error("Undefined error in transactionDocSet");
    };

    exec(success, failure, PLUGIN_NAME, 'transactionDocSet', args);

    return this;
  },
  update: function (documentReference, data) {

    var args = [this._id, documentReference._id, documentReference._collectionReference._path, __wrap(data)];

    var success = function () {
    };

    var failure = function () {
      throw new Error("Undefined error in transactionDocUpdate");
    };

    exec(success, failure, PLUGIN_NAME, 'transactionDocUpdate', args);

    return this;
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

function FirestoreTimestamp(timestamp) {
  this._timestamp = timestamp;
}

FirestoreTimestamp.prototype = {
  toDate: function() {
    return new Date(this._timestamp);
  }
}

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

function QueryDocumentSnapshot(data) {
  DocumentSnapshot.call(this, data);
}

QueryDocumentSnapshot.prototype = Object.create(DocumentSnapshot.prototype);
QueryDocumentSnapshot.prototype.constructor = QueryDocumentSnapshot;

function QuerySnapshot(data) {
  this._data = data;
}

QuerySnapshot.prototype = {
  forEach: function (callback, thisArg) {
    var keys = Object.keys(this._data.docs);
    for (var i = 0; i < keys.length; i++) {
      callback(new QueryDocumentSnapshot(this._data.docs[i]));
    }
  }
};

Object.defineProperties(QuerySnapshot.prototype, {
  docChanges: {
    get: function () {
      throw "QuerySnapshot.docChanges: Not supported";
    }
  },
  docs: {
    get: function () {
      return this._data.docs;
    }
  },
  empty: {
    get: function () {
      return this._data.docs.length === 0;
    }
  },
  metadata: {
    get: function () {
      throw "QuerySnapshot.metadata: Not supported";
    }
  },
  query: {
    get: function () {
      throw "QuerySnapshot.query: Not supported";
    }
  },
  size: {
    get: function () {
      return this._data.docs.length;
    }
  }
});

function DocumentReference(collectionReference, id) {
  this._id = id;
  this._collectionReference = collectionReference;
}

DocumentReference.prototype = {
  _isFunction: function (functionToCheck) {
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
  },
  collection: function (collectionPath) {
    return new SubCollectionReference(this, collectionPath);
  },
  delete: function () {
    var args = [this._collectionReference._path, this._id];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docDelete', args);
    }).then(function () {
      return;
    });
  },
  get: function () {
    var args = [this._collectionReference._path, this._id];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docGet', args);
    }).then(function (data) {
      return new DocumentSnapshot(data);
    });
  },
  onSnapshot: function (optionsOrObserverOrOnNext, observerOrOnNextOrOnError, onError) {

    var callbackId = utils.createUUID();

    var args = [this._collectionReference._path, this._id, callbackId];

    if (!this._isFunction(optionsOrObserverOrOnNext)) {
      args.push(optionsOrObserverOrOnNext);
    }
    var wrappedCallback;

    if (this._isFunction(optionsOrObserverOrOnNext)) {
      wrappedCallback = function (documentSnapshot) {
        optionsOrObserverOrOnNext(new DocumentSnapshot(documentSnapshot));
      };
    } else if (this._isFunction(observerOrOnNextOrOnError)) {
      wrappedCallback = function (documentSnapshot) {
        observerOrOnNextOrOnError(new DocumentSnapshot(documentSnapshot));
      };
    } else {
      wrappedCallback = function (documentSnapshot) { };
    }

    exec(wrappedCallback, function () {
      throw new Error("Undefined error in docOnSnapshot");
    }, PLUGIN_NAME, 'docOnSnapshot', args);

    return function () {
      exec(function () { }, function () {
        throw new Error("Undefined error in docUnsubscribe");
      }, PLUGIN_NAME, 'docUnsubscribe', [callbackId]);
    };
  },
  set: function (data, options) {

    var args = [this._collectionReference._path, this._id, __wrap(data), options];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docSet', args);
    });
  },
  update: function (data) {

    var args = [this._collectionReference._path, this._id, __wrap(data)];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docUpdate', args);
    });
  }
};

Object.defineProperties(DocumentReference.prototype, {
  firestore: {
    get: function () {
      throw "DocumentReference.firestore: Not supported";
    }
  },
  id: {
    get: function () {
      return this._id;
    }
  },
  parent: {
    get: function () {
      return this._collectionReference;
    }
  }
});

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
    var args = [this._ref._path, this._ref._queries];

    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'collectionGet', args);
    }).then(function (data) {
      return new QuerySnapshot(data);
    });
  },
  onSnapshot: function (callback, options) {

    var callbackId = utils.createUUID();
    var args = [this._ref._path, this._ref._queries, options, callbackId];

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

function CollectionReference(path, id) {
  this._path = path;
  this._id = id;
  this._ref = this;
  this._queries = [];
}

CollectionReference.prototype = Object.create(Query.prototype, {
  firestore: {
    get: function () {
      throw "CollectionReference.firestore: Not supported";
    }
  },
  id: {
    get: function () {
      return this._id;
    }
  },
  parent: {
    get: function () {
      throw "CollectionReference.parent: Not supported";
    }
  }
});

CollectionReference.prototype.add = function (data) {
  var args = [this._path, __wrap(data)];

  return new Promise(function (resolve, reject) {
    exec(resolve, reject, PLUGIN_NAME, 'collectionAdd', args);
  });
};

CollectionReference.prototype.doc = function (id) {
  return new DocumentReference(this, id);
};

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
function DocumentOfSubCollectionReference(subCollectionReference, id) {
  this._id = id;
  this._subCollectionReference = subCollectionReference;
}
DocumentOfSubCollectionReference.prototype = {
  delete: function () {
    var args = [this._subCollectionReference._documentReference._collectionReference._path,
    this._subCollectionReference._documentReference._id,
    this._subCollectionReference._id,
    this._id];
    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docOfSubCollectionDelete', args);
    }).then(function () {
      return;
    });
  },
  get: function () {
    var args = [this._subCollectionReference._documentReference._collectionReference._path,
    this._subCollectionReference._documentReference._id,
    this._subCollectionReference._id,
    this._id];
    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docOfSubCollectionGet', args);
    }).then(function (data) {
      return new DocumentSnapshot(data);
    });
  },
  set: function (data, options) {
    var args = [
      this._subCollectionReference._documentReference._collectionReference._path,
      this._subCollectionReference._documentReference._id,
      this._subCollectionReference._id,
      this._id,
      __wrap(data),
      options];
    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docOfSubCollectionSet', args);
    });
  },
  update: function (data) {
    var args = [this._subCollectionReference._documentReference._collectionReference._path,
    this._subCollectionReference._documentReference._id,
    this._subCollectionReference._id,
    this._id,
    __wrap(data)];
    return new Promise(function (resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docOfSubCollectionUpdate', args);
    });
  }
};

module.exports = {
  initialise: function (options) {
    return new Promise(function (resolve, reject) {
      resolve(new Firestore(options));
    });
  },
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
