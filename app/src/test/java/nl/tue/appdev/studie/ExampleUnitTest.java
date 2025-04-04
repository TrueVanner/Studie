package nl.tue.appdev.studie;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_note_constructor() {
        Note note = new Note("a", "b","c","d");
        assertEquals("b", note.getTitle());
        assertEquals("c", note.getAuthorId());
        assertEquals("d", note.getGroupID());
        assertEquals("a", note.getFilename());
    }

}