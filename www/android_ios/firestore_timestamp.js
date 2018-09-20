function FirestoreTimestamp(timestamp) {
  this._timestamp = timestamp;
}

FirestoreTimestamp.prototype = {
  toDate: function() {
    return new Date(this._timestamp);
  }
};

module.exports = FirestoreTimestamp;
