function FirestoreTimestamp(seconds, nanoseconds) {
  this._seconds = seconds;
  this._nanoseconds = nanoseconds;
}

FirestoreTimestamp.prototype = {
  toDate: function() {
    return new Date((this._seconds * 1000) + (this._nanoseconds / 1000));
  },
  toString: function() {
    return JSON.stringify({ seconds: this.seconds, nanoseconds: this.nanoseconds});
  }
};

module.exports = FirestoreTimestamp;
