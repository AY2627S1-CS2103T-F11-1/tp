package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Tests that a {@code Person}'s {@code Tag}s contain at least one of the given
 * keywords as a substring, ignoring case.
 */
public class TagContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    /**
     * Creates a predicate using a copy of the given keywords, excluding blank entries.
     * If no keywords remain, the predicate matches no contacts.
     *
     * @param keywords the search keywords
     * @throws NullPointerException if {@code keywords} or any keyword is null
     */
    public TagContainsKeywordsPredicate(List<String> keywords) {
        requireNonNull(keywords);

        this.keywords = keywords.stream()
                .filter(keyword -> !keyword.isBlank())
                .toList();
    }

    @Override
    public boolean test(Person person) {
        return person.getTags().stream()
                .anyMatch(this::matchesAnyKeyword);
    }

    /**
     * Checks whether the specified {@code Tag} matches any of the search keywords.
     *
     * Extracted as a separate helper method to adhere to SLAP and avoid nested stream pipelines within
     * {@link #test(Person)}.
     */
    private boolean matchesAnyKeyword(Tag tag) {
        String lowerTag = tag.tagName.toLowerCase(Locale.ROOT);

        return keywords.stream().anyMatch(keyword -> lowerTag.contains(keyword.toLowerCase(Locale.ROOT)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TagContainsKeywordsPredicate otherTagContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherTagContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
