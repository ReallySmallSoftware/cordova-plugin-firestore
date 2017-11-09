var exec = require('cordova/exec');

var PLUGIN_NAME = 'Firestore';
var listenerCtr = 1;

function Firestore(persist, datePrefix) {
  if (datePrefix === undefined) {
    this.datePrefix = "__DATE(";
  } else {
    this.datePrefix = datePrefix;
  }
  exec(function() {}, null, PLUGIN_NAME, 'initialise', [persist]);
}

Firestore.prototype = {
  get: function() {
    return this;
  },
  collection: function(path) {
    return new CollectionReference(path);
  }
};

function CollectionReference(path, id) {
  this.path = path;
  this.id = id;
  this.limit = -1;
  this.endAt = -1;
  this.endBefore = -1;
  this.orderByArray = [];
  this.whereArray = [];
}

CollectionReference.prototype = {
  add: function(data) {
    var args = [this.collectionReference.path, data];

    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'collectionAdd', args);
    });
  },
  doc: function(id) {
    return new DocumentReference(this, id);
  },
  endAt: function(snapshotOrVarArgs) {
    throw "CollectionReference.endAt: Not supported";
    return this;
  },
  endBefore: function(snapshotOrVarArgs) {
    throw "CollectionReference.endBefore: Not supported";
    return this;
  },
  get: function() {
    var args = [this.collectionReference.path];

    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'collectionGet', args);
    });
  },
  limit: function(limit) {
    this.limit = limit;
    return this;
  },
  orderBy: function(field, direction) {
    if (direction === undefined) {
      direction = "ASCENDING";
    }

    var orderByField = {
      "field": field,
      "direction": direction
    };
    this.orderByArray[] = orderByField;
    return this;
  },
  onSnapshot: function(callback, options) {
    var args = [this.path, this.whereArray, this.orderByArray, this.limit, options];

    var resolved = false;
    var id = callback.$listenerId || ++listenerCtr;
    callback.$listenerId = id;

    return new Promise(function(resolve, reject) {
      exec(function(data) {
        var snapshot = new QuerySnapshot(data);
        callback(snapshot);
        if (!resolved) {
          resolve(callback);
          resolved = true;
        }
      }, function() {}, PLUGIN_NAME, 'collectionOnShapshot', args);
    }.bind(this));
  },
  startAfter: function(snapshotOrVarArgs) {
    throw "CollectionReference.startAfter: Not supported";
    return this;
  },
  startAt: function(snapshotOrVarArgs) {
    throw "CollectionReference.startAt: Not supported";
    return this;
  },
  where: function(fieldPath, opStr, passedValue) {
    var value;
    if (typeof passedValue is Date) {
      value = "__DATE(" + passedValue.getTime() + ")";
    } else {
      value = passedValue;
    }
    var whereField = {
      "fieldPath": fieldPath,
      "opStr": opStr,
      "value": value
    };
    this.whereArray[] = whereField;
    return this;
  }
};

Object.defineProperties(CollectionReference.prototype, {
  firestore: {
    get: function() {
      throw "CollectionReference.firestore: Not supported";
    }
  },
  id: {
    get: function() {
      return this.id;
    }
  },
  parent: {
    get: function() {
      throw "CollectionReference.parent: Not supported";
    }
  }
});

function DocumentReference(collectionReference, id) {
  this.id = id;
  this.collectionReference = collectionReference;
}


DocumentReference.prototype = {
  _isFunction: function(functionToCheck) {
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
  },
  collection: function(collectionPath) {
    throw "DocumentReference.collection(): Not supported";
  },
  delete: function() {
    throw "DocumentReference.delete(): Not supported";
  },
  get: function() {
    throw "DocumentReference.get(): Not supported";
  },
  onSnapshot: function(optionsOrObserverOrOnNext, observerOrOnNextOrOnError, onError) {
    var args = [this.collectionReference.path, this.id];

    if (!this._isFunction(optionsOrObserverOrOnNext)) {
      args.push(optionsOrObserverOrOnNext);
    }
    var wrappedCallback;

    if (this._isFunction(optionsOrObserverOrOnNext)) {
      wrappedCallback = function(documentSnapshot) {
        optionsOrObserverOrOnNext(new DocumentSnapshot(documentSnapshot));
      }
    } else if (this._isFunction(observerOrOnNextOrOnError)) {
      wrappedCallback = function(documentSnapshot) {
        observerOrOnNextOrOnError(new DocumentSnapshot(documentSnapshot));
      }
    } else {
      wrappedCallback = function(documentSnapshot) {}
    }
    return new Promise(function(resolve, reject) {
      exec(wrappedCallback, function() {}, PLUGIN_NAME, 'docOnShapshot', args);
    }.bind(this));
  },
  set: function(data, options) {

    var args = [this.collectionReference.path, this.id, data, options];

    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docSet', args);
    });
  },
  update: function(data) {

    var args = [this.collectionReference.path, this.id, data];

    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docUpdate', args);
    });
  }
};


Object.defineProperties(DocumentReference.prototype, {
  firestore: {
    get: function() {
      throw "DocumentReference.firestore: Not supported";
    }
  },
  id: {
    get: function() {
      return this.id;
    }
  },
  parent: {
    get: function() {
      return this.collectionReference;
    }
  }
});

function DocumentSnapshot(data) {
  this._data = data;

  var keys = Object.keys(this._data._data);
  for (var i = 0; i < keys.length; i++) {
    var key = keys[i];
    console.log(key, this._data._data[key]);

    if (typeof this._data._data[key] === 'string' && this._data._data[key].startsWith("__DATE(")) {
      var length = this._data._data[key].length;
      var wrapperLength = "__DATE(".length;

      var timestamp = this._data._data[key].substring(wrapperLength, length - wrapperLength - 1);

      this._data._data[key] = new Date(timestamp);
    }
  }

}

DocumentSnapshot.prototype = {
  _fieldPath: function(obj, i) {
    return obj[i];
  },
  data: function() {
    return this._data._data;
  },
  get: function(fieldPath) {
    return fieldPath.split('.').reduce(this._fieldPath, this._data);
  }
};

Object.defineProperties(DocumentSnapshot.prototype, {
  exists: {
    get: function() {
      return this._data.exists;
    }
  },
  id: {
    get: function() {
      return this._data.id;
    }
  },
  metadata: {
    get: function() {
      throw "DocumentReference.metadata: Not supported";
    }
  },
  ref: {
    get: function() {
      return this._data.ref;
    }
  }
});


function QuerySnapshot(data) {
  this._data = data;
}

QuerySnapshot.prototype = {
  forEach: function(callback, thisArg) {
    var keys = Object.keys(this._data.docs);
    for (var i = 0; i < keys.length; i++) {
      callback(new DocumentSnapshot(this._data.docs[i]));
    }
  }
};

Object.defineProperties(QuerySnapshot.prototype, {
  docChanges: {
    get: function() {
      throw "QuerySnapshot.docChanges: Not supported";
    }
  },
  docs: {
    get: function() {
      return this._data.docs;
    }
  },
  empty: {
    get: function() {
      return this._data.docs.length === 0;
    }
  },
  metadata: {
    get: function() {
      throw "QuerySnapshot.metadata: Not supported";
    }
  },
  query: {
    get: function() {
      throw "QuerySnapshot.query: Not supported";
    }
  },
  size: {
    get: function() {
      return this._data.docs.length;
    }
  }
});
module.exports = {
  initialise: function() {
    return new Firestore();
  }
};
