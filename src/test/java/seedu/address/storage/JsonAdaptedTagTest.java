package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.tag.Tag;

public class JsonAdaptedTagTest {
    private static final String INVALID_TAG = "#friend";
    private static final String VALID_TAG = "friend";

    @Test
    public void toModelType_validTagDetails_returnsTag() throws Exception {
        JsonAdaptedTag tag = new JsonAdaptedTag(VALID_TAG);
        assertEquals(new Tag(VALID_TAG), tag.toModelType());
    }

    @Test
    public void toModelType_invalidTagName_throwsIllegalValueException() {
        JsonAdaptedTag tag = new JsonAdaptedTag(INVALID_TAG);
        assertThrows(IllegalValueException.class, tag::toModelType);
    }

    @Test
    public void toModelType_nullTagName_throwsIllegalValueException() {
        JsonAdaptedTag tag = new JsonAdaptedTag((String) null);
        assertThrows(IllegalValueException.class, tag::toModelType);
    }
}
