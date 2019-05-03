/* global Firestore */

var GeoPoint = require('./geo_point');

var wrap = function (data) {
  if (data instanceof GeoPoint) {
    return Firestore.options().geopointPrefix + data.latitude + "," + data.longitude;
  }

  if (Object.prototype.toString.call(data) === '[object Date]') {
    return Firestore.options().datePrefix + data.getTime();
  }

  if (Object.prototype.toString.call(data) !== '[object Object]') {
    return data;
  }

  var keys = Object.keys(data);
  for (var i = 0; i < keys.length; i++) {
    var key = keys[i];

    if (data[key] instanceof GeoPoint) {
      data[key] = Firestore.options().geopointPrefix + data[key].latitude + "," + data[key].longitude;
    } else if (Object.prototype.toString.call(data[key]) === '[object Date]') {
      if (Firestore.options().timestampsInSnapshots) {
        var seconds = data[key].getTime() / 1000;
        var nanoseconds = data[key].getMilliseconds() * 1000;
        data[key] = Firestore.options().timestampPrefix + seconds + "_" + nanoseconds;
      } else {
        data[key] = Firestore.options().datePrefix + data[key].getTime();
      }
    } else if (Object.prototype.toString.call(data[key]) === '[object Object]') {
      data[key] = wrap(data[key]);
    }
  }
  return data;
};

module.exports = {
  wrap: wrap
};
