const QuerySnapshot = require('./query_snapshot');
const QueryDocumentSnapshot = require('./query_document_snapshot');


describe('QuerySnapshot', () => {
  describe('docs', () => {
    it('should return an array of QueryDocumentSnapshot', () => {
      const docs = (new QuerySnapshot({docs: ['foo', 'bar']})).docs;
      expect(docs.length).toBe(2);
      expect(docs[0]).toBeInstanceOf(QueryDocumentSnapshot);
      expect(docs[1]).toBeInstanceOf(QueryDocumentSnapshot);
    });
  });
});
