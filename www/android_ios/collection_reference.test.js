/* global Promise: false */

var PLUGIN_NAME = 'Firestore';
var exec = require('cordova/exec');
var Query = require("./query");
var DocumentReference = require("./document_reference");
var __wrap = require("./__wrap");
const CollectionReference = require('./collection_reference');

const mockParent = {};

describe('CollectionReference', () => {
  describe('parent', () => {
    it('should be null when no parent specified', () => {
      const collectionRef = new CollectionReference('path');
      expect(collectionRef.parent).toBe(null);
      expect(collectionRef.path).toBe('path');
      expect(collectionRef.id).toBe('path');
    });

    it('should return parent when passed to constructor', () => {
      const collectionRef = new CollectionReference('path', mockParent);
      expect(collectionRef.parent).toBe(mockParent);
    });
  });

  it('should dispatch path, id and parent when used as subcollection', () => {
    const subCollRef = new CollectionReference('/root/parent/sub/', mockParent);
    expect(subCollRef.id).toBe('sub');
    expect(subCollRef.path).toBe('root/parent/sub');
    expect(subCollRef.parent).toBe(mockParent);
  });

  describe('.doc allows for infinite recursion of collRef -> docRef -> collRef...', () => {
    const rootCollection = new CollectionReference('root');
    const parentDocument = rootCollection.doc('parent');
    const subCollection = parentDocument.collection('sub');
    const subDocument = subCollection.doc('child');
    const subSubCollection = subDocument.collection('subSub');

    it('should set paths and ids', () => {
      expect(rootCollection.path).toBe('root');
      expect(rootCollection.id).toBe('root');
      expect(parentDocument.path).toBe('root/parent');
      expect(parentDocument.id).toBe('parent');
      expect(subCollection.path).toBe('root/parent/sub');
      expect(subCollection.id).toBe('sub');
      expect(subDocument.path).toBe('root/parent/sub/child');
      expect(subDocument.id).toBe('child');
      expect(subSubCollection.path).toBe('root/parent/sub/child/subSub');
      expect(subSubCollection.id).toBe('subSub');
    });

    it('should set parents all the way', () => {
      expect(rootCollection.parent).toBe(null);
      expect(parentDocument.parent).toBe(rootCollection);
      expect(subCollection.parent).toBe(parentDocument);
      expect(subDocument.parent).toBe(subCollection);
      expect(subSubCollection.parent).toBe(subDocument);
    });
  });
});
