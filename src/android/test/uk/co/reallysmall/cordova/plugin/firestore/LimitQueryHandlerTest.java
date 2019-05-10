package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Query;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LimitQueryHandlerTest {
    LimitQueryHandler limitQueryHandler = new LimitQueryHandler();

    Query query = mock(Query.class);


    @Test
    public void testShouldHandleStringArgument() {
        limitQueryHandler.handle(query, "10");
        verify(query).limit(10);
    }

    @Test
    public void testShouldHandleIntArgument() {
        limitQueryHandler.handle(query, 10);
        verify(query).limit(10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldThrowIllegalArgumentExceptiononArrayArgument() {
        limitQueryHandler.handle(query, new String[5]);
    }
}

