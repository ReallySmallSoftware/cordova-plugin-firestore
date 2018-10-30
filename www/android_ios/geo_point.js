function GeoPoint(latitude, longitude) {
  this._lat = latitude;
  this._long = longitude;

  Object.defineProperty(this, 'latitude', {
    get: function() {
      return this._lat;
    }
  });
  Object.defineProperty(this, 'longitude', {
    get: function() {
      return this._long;
    }
  });
}

GeoPoint.prototype = {
  isEqual: function (other) {
    return other._lat === this._lat &&
      other._long === this._long;
  }
};

module.exports = GeoPoint;
