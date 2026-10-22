package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Name} contains at least one of the given
 * keywords as a substring, ignoring case.
 */
public class NameContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    /**
     * Creates a predicate using a copy of the given keywords, excluding blank entries.
     * If no keywords remain, the predicate matches no contacts.
     *
     * @param keywords the search keywords
     * @throws NullPointerException if {@code keywords} or any keyword is null
     */
    public NameContainsKeywordsPredicate(List<String> keywords) {
        requireNonNull(keywords);

        this.keywords = keywords.stream()
                .filter(keyword -> !keyword.isBlank())
                .toList();
    }

    @Override
    public boolean test(Person person) {
        assert person != null : "Person to test must not be null";

        String name = person.getName().fullName.toLowerCase(Locale.ROOT);

        return keywords.stream()
                .anyMatch(keyword -> name.contains(keyword.toLowerCase(Locale.ROOT)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameContainsKeywordsPredicate otherNameContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherNameContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
