var GeoPoint = require('./GeoPoint');

function Utilities() {
}

Utilities.combinePath = function () {

  var args = Array.prototype.slice.call(arguments);

  var combinePath = args.join("/");

  return combinePath;
}

Utilities.leaf = function (path) {

  var leaf;

  if (path === null || path === undefined || path === "") {

    leaf = path;

  } else {

    var pathArray = path.split("/");
    leaf = pathArray[pathArray.length - 1];

  }

  return leaf;
}

Utilities.stripLeaf = function (path) {

  var stripLeaf;

  if (path === null || path === undefined || path === "") {
    stripLeaf = path;
  } else {

    var pathArray = path.split("/");

    if (pathArray.length === 1) {
      stripLeaf = path;
    } else {
      stripLeaf = pathArray.slice(0, pathArray.length - 1).join("/");
    }
  }

  return stripLeaf;
}

Utilities.wrap = function (data) {

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
      data[key] = Utilities.wrap(data[key]);
    }
  }
  return data;
}

module.exports = Utilities;
