# History
## 3.1.0
- Added type definitions

## 3.0.0
### User-facing improvements
- Breaking change: QuerySnapshot.docs() now wraps documents in QueryDocumentSnapshots like firebase-js-sdk
- Revert minimum cordova-ios to 4.5.0 and cordova-android to 7.0.0
- Firestore.doc() improvement, can now call .parent correctly on first-level documents
- CollectionReference.doc() supports id generation when no argument is passed
- Native logs improvement

### Technical improvements
- Rename javascript imports to be consistent with file names, this allows running
  the JavaScript when running tests
- Add tests for the new features and refactoring

## 2.0.0
- Updated README
- Updated Android dependencies
- Added full nested collection implementation - may break backwards compatibility
- Added doc() support to Firestore class
- Add parent to DocumentReference and CollectionReference
- Fix podspec for cordova-ios 5

## 1.3.2
- Refactor README
- Reset some significant breaking changes
- Make sure jshint task passes
- Implement Geopoint and Timestamp

## 1.3.1
- Organize file structure.
- Normalize initialize options.

## 1.3.0
- Merge multi-project config changes
- Merge sub document changes
- Update Web SDK reference to 5.2.0
- Introduce QueryDataSnapshot
- Implement timestampsInSnapshots option in configuration

## 1.2.0
- Update Android dependency versions
- Update iOS dependency versions
- Update plugin dependencies
- WARNING: The Android update may require you to update com.google.gms:google-services to 4.0.0, com.android.tools.build:gradle to 3.1.2 and gradle to 4.4.4 (look in platforms/android/cordova/lib/builders/GradleBuilder.js)

## 1.1.0
- Add support for FieldValue
- Add experimental support for Transactions. _Please note this is **experimental**!_
- Add startswith polyfill

## 1.0.10
- Correct log level when creating results

## 1.0.9
- Updated Dependencies
- Remove incorrect Java 7 dependency

## 1.0.8
- Ensure dates work for queries and nested data
- Implement delete()
- Update README

## 1.0.7
- Remove dependency on cordova-plugin-firebase-hooks

## 1.0.6
- Correct README History
- Make browser firebase dependency loading smarter

## 1.0.5
## 1.0.4
## 1.0.3
- Address plugin dependency issues

## 1.0.2
- Updated version
- Added firebase hooks dependency
- Corrected iOS source/header-file config

## 1.0.0
Initial release
