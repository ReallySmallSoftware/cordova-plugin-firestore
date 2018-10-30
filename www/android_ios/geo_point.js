function GeoPoint(latitude, longitude) {
  Object.defineProperty(this, 'latitude', {
    value: latitude,
    writable: false
  });
  Object.defineProperty(this, 'longitude', {
    value: longitude,
    writable: false
  });
}

GeoPoint.prototype = {
  isEqual: function (other) {
    return other.latitude === this.latitude &&
      other.longitude === this.longitude;
  },
  toString: function() {
    return JSON.stringify({ _lat: this.latitude, _long: this.longitude});
  }
};

module.exports = GeoPoint;
