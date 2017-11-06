var exec = require('cordova/exec');

var PLUGIN_NAME = 'Firestore';

function Firestore(persist) {
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
}

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
})

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
  limit: function() {
    throw "CollectionReference.limit: Not supported";
    return this;
  },
  orderBy: function(field) {
    throw "CollectionReference.orderBy: Not supported";
    return this;
  },
  onSnapshot: function(callback) {
    var args = [this.path, [],
      []
    ];
    var wrappedCallback = function(data) {
      alert("create qs");

      callback(new QuerySnapshot(data));
    }
    exec(wrappedCallback, function() {}, PLUGIN_NAME, 'collectionOnShapshot', args);
  },
  startAfter: function(snapshotOrVarArgs) {
    throw "CollectionReference.startAfter: Not supported";
    return this;
  },
  startAt: function(snapshotOrVarArgs) {
    throw "CollectionReference.startAt: Not supported";
    return this;
  },
  where: function(fieldPath, opStr, value) {
    throw "CollectionReference.where: Not supported";
    return this;
  }
};

function DocumentReference(collectionReference, id) {
  this.id = id;
  this.collectionReference = collectionReference;
}

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
        alert("create ds1");
        optionsOrObserverOrOnNext(new DocumentSnapshot(documentSnapshot));
      }
    } else if (this._isFunction(observerOrOnNextOrOnError)) {
      wrappedCallback = function(documentSnapshot) {
        observerOrOnNextOrOnError(new DocumentSnapshot(documentSnapshot));
      }
    } else {
      wrappedCallback = function(documentSnapshot) {}
    }

    exec(wrappedCallback, function() {}, PLUGIN_NAME, 'docOnShapshot', args);
  },
  set: function(data, options) {

    var args = [this.collectionReference.path, this.id, data, options];

    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docSet', args);
    });
  },
  update: function(data ) {

    var args = [this.collectionReference.path, this.id, data];

    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'docUpdate', args);
    });
  }
};

function DocumentSnapshot(data) {
  this._data = data;
}

Object.defineProperties(DocumentSnapshot.prototype, {
  exists: {
    get: function() {
      return this._data.exists;
    }
  },
  id: {
    get: function() {
      return this.id;
    }
  },
  metadata: {
    get: function() {
      throw "DocumentReference.metadata: Not supported";
    }
  },
  ref: {
    get: function() {
      throw "DocumentReference.ref: Not supported";
    }
  }
});

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

function QuerySnapshot(data) {
  this._data = data;
}

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

QuerySnapshot.prototype = {
  forEach: function(callback, thisArg) {
    alert("fe");
    console.log(JSON.stringify(this._data.docs));
    $.each(this._data.docs, function(i, v) {
      alert("fe "+i);
      console.log(JSON.stringify(v));
      callback(new DocumentSnapshot(v));
    });
  }
};

module.exports = {
  initialise: function() {
    return new Firestore();
  }
};
