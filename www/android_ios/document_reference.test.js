const uuid = require('uuid');
const DocumentReference = require('./document_reference');
const CollectionReference = require('./collection_reference');

const rootCollection = {path: 'root'};
const subCollection = 'sub';
const parentId = 'parent';
const childId = 'child';

describe('DocumentReference', () => {
  const parentDocRef = new DocumentReference(rootCollection, parentId);
  it('should implement path', () => {
    expect(parentDocRef.path).toBe('root/parent');
  });

  it('should autogenerate an id when none is specified', () => {
    const docRefWithAutoId = new DocumentReference(rootCollection);
    expect(docRefWithAutoId.id).toBeDefined();
    expect(docRefWithAutoId.id).toMatch(/[a-zA-Z0-9]{20}/);
  });

  describe('collection', () => {
    it('should return a CollectionReference with proper path', () => {

      console.log('What is document_reference in test', typeof DocumentReference);
      const subCollectionRef = (parentDocRef).collection(subCollection);
      expect(subCollectionRef).toBeInstanceOf(CollectionReference);
      expect(subCollectionRef.path).toBe('root/parent/sub');
    });
  });
});
