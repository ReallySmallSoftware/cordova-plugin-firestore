declare namespace Firestore {

    export interface FirestoreOptions {
        datePrefix?: string;
        fieldValueDelete?: string;
        geopointPrefix?: string;
        fieldValueServerTimestamp?: string;
        persist?: boolean;
        timestampsInSnapshots?: boolean;
    }

    export interface FieldValue {
        delete(): string;
        serverTimestamp(): string;
    }

    export interface DocumentData {
        [field: string]: any;
    }

    export interface UpdateData extends DocumentData {
    }

    export interface DocumentSnapshotCallback {
        (documentSnapshot: DocumentSnapshot): void;
    }

    export interface DocumentErrorCallback {
        (error: Error): void;
    }

    export interface SnapshotListenOptions {
        includeMetadataChanges: boolean;
    }

    export interface SetOptions {
        merge: boolean;
    }

    export interface DocumentReference {
        collection(collectionPath: string): CollectionReference;
        delete(): Promise<void>;
        get(): Promise<DocumentSnapshot>;
        onSnapshot(optionsOrObserverOrOnNext: object | SnapshotListenOptions | DocumentSnapshotCallback,
            observerOrOnNextOrOnError?: object | DocumentSnapshotCallback | DocumentErrorCallback,
            onError?: DocumentErrorCallback): void;
        set(data: DocumentData, options?: SetOptions): void;
        update(data: UpdateData): Promise<any>;

        firestore: Firestore;
        id: string;
        parent: CollectionReference;
        path: string;
    }

    export interface DocumentSnapshot {
        data(): DocumentData | undefined;
        get(fieldPath: string): any;

        exists: boolean;
        id: string;
        metadata: any;
        ref: DocumentReference;
    }

    export interface QueryDocumentSnapshot extends DocumentSnapshot {
    }

    export interface QuerySnapshotCallback {
        (documentSnapshot: QueryDocumentSnapshot): void;
    }

    export interface FirestoreTimestamp {
        new (seconds: number, nanoseconds: number): FirestoreTimestamp;
        toDate():Date;
    }

    export interface GeoPoint {
        new (latitude: number, longitude: number): GeoPoint;
        latitude: number;
        longitude: number;
    }

    export interface Path {
        new (path:string): GeoPoint;
    }

    export interface QuerySnapshot {
        forEach(callback: QuerySnapshotCallback): void;
        docs: QueryDocumentSnapshot[];
        empty: boolean;
        metadata: any;
        query: Query;
        size: number;
    }

    export interface QueryCallback {
        (snapshot: QuerySnapshot): void;
    }

    export interface Query {
        endAt(snapshotOrFieldValues: DocumentSnapshot | any[]): Query;
        endBefore(snapshotOrFieldValues: DocumentSnapshot | any[]): Query;
        limit(limit: number): Query;
        orderBy(fieldPath: string, direction: string): Query;

        get(): QuerySnapshot;
        onSnapshot(callback: Function, options: SnapshotListenOptions): void;
        startAfter(snapshotOrFieldValues: DocumentSnapshot | any[]): Query;
        startAt(snapshotOrFieldValues: DocumentSnapshot | any[]): Query;
        where(fieldPath: string, opStr: string, value: any): Query;
    }

    export interface CollectionReference extends Query {
        firestore: Firestore;
        id: string;
        parent: Document;
        path: string;

        add(data: any): Promise<DocumentReference>;
        doc(id?: string): DocumentReference;
    }

    export interface Document {

    }

    export interface Transaction {
        delete(documentRef: DocumentReference): Transaction;
        get(documentRef: DocumentReference): Promise<DocumentSnapshot>;
        set(documentRef: DocumentReference, data: DocumentData, options?: SetOptions): Transaction;
        update(documentRef: DocumentReference, data: UpdateData): Transaction;
    }

    export interface RunTransactionUpdateFunction {
        (transaction: Transaction): Promise<any>;
    }

    export interface Firestore {
        get(): Firestore;
        batch(): void;
        collection(): CollectionReference;
        disableNetwork(): Promise<void>;
        doc(documentPath: string): Document;
        enableNetwork(): void;
        enablePersistence(): void;
        runTransation(callback: RunTransactionUpdateFunction): Promise<any>;
        setLogLevel(logLevel: string): void;
        settings(settings: any): void;

        FieldValue: string;
        GeoPoint: string;
    }

    export function initialise(options: FirestoreOptions): Promise<Firestore>;
}