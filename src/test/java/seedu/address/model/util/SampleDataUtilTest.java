package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

public class SampleDataUtilTest {

    @Test
    public void getSamplePersons_returnsExpectedPersons() {
        Person[] samplePersons = SampleDataUtil.getSamplePersons();

        assertEquals(6, samplePersons.length);
        for (Person person : samplePersons) {
            assertEquals(SampleDataUtil.EMPTY_REMARK, person.getRemark());
        }
    }

    @Test
    public void getSampleAddressBook_returnsExpectedAddressBook() {
        ReadOnlyAddressBook sampleAddressBook = SampleDataUtil.getSampleAddressBook();

        assertEquals(6, sampleAddressBook.getPersonList().size());
    }

    @Test
    public void getTagSet_returnsExpectedTags() {
        Set<Tag> tags = SampleDataUtil.getTagSet("friends", "colleagues");

        assertEquals(Set.of(new Tag("friends"), new Tag("colleagues")), tags);
    }
}
