function Path(path) {
  console.log('building path' + path);
  this._original = path;
  this.segments = path.split('/').filter(function(segment) {
   return segment.length > 0; 
  });

  this.cleanPath = this.segments.join('/');
  this.id = this.segments[this.segments.length - 1];
  this.parent = this.segments.slice(0, this.segments.length - 1).join('/');
}

module.exports = Path;
