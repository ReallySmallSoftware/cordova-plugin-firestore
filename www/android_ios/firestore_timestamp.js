function FirestoreTimestamp(seconds, nanoseconds) {
  this.seconds = seconds;
  this.nanoseconds = nanoseconds;
}

FirestoreTimestamp.prototype = {
  toDate: function() {
    return new Date((this.seconds * 1000) + (this.nanoseconds / 1000));
  }
};

module.exports = FirestoreTimestamp;
