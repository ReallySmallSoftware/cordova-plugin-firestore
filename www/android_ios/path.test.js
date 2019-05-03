var Path = require('./path');

describe('Path', () => {
  describe('parent', () => {
    it('should return empty string when on top level', () => {
      expect((new Path('collection')).parent).toEqual('');
    });

    it('should return parent', () => {
      expect((new Path('parent/sub')).parent).toEqual('parent');
    });
  });
});
