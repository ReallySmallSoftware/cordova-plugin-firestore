

var FirestoreOptions = require('./FirestoreOptions'),
  GeoPoint = require('./GeoPoint');

function __wrap(data) {

  if (data instanceof GeoPoint) {
    return FirestoreOptions.geopointPrefix + data.latitude + "," + data.longitude;
  }

  if (Object.prototype.toString.call(data) === '[object Date]') {
    return FirestoreOptions.datePrefix + data.getTime();
  }

  if (Object.prototype.toString.call(data) !== '[object Object]') {
    return data;
  }

  var keys = Object.keys(data);
  for (var i = 0; i < keys.length; i++) {
    var key = keys[i];

    if (data[key] instanceof GeoPoint) {
      data[key] = FirestoreOptions.geopointPrefix + data[key].latitude + "," + data[key].longitude;
    } else if (Object.prototype.toString.call(data[key]) === '[object Date]') {
      data[key] = FirestoreOptions.datePrefix + data[key].getTime();
    } else if (Object.prototype.toString.call(data[key]) === '[object Object]') {
      data[key] = __wrap(data[key]);
    }
  }
  return data;
}

module.exports = __wrap;
