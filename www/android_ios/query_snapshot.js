var QueryDocumentSnapshot = require('./query_document_snapshot');

function QuerySnapshot(data) {
  this._data = data;
}

QuerySnapshot.prototype = {
  forEach: function (callback) {
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
      return this._data.docs.map(function(doc) {
       return new QueryDocumentSnapshot(doc); 
      });
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

module.exports = QuerySnapshot;
