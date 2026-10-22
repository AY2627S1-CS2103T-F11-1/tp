package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Role} contains at least one of the given
 * keywords as a substring, ignoring case.
 */
public class RoleContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    /**
     * Creates a predicate using a copy of the given keywords, excluding blank entries.
     * If no keywords remain, the predicate matches no contacts.
     *
     * @param keywords the search keywords
     * @throws NullPointerException if {@code keywords} or any keyword is null
     */
    public RoleContainsKeywordsPredicate(List<String> keywords) {
        requireNonNull(keywords);

        this.keywords = keywords.stream()
                .filter(keyword -> !keyword.isBlank())
                .toList();
    }

    @Override
    public boolean test(Person person) {
        String role = person.getRole().value.toLowerCase(Locale.ROOT);

        return keywords.stream()
                .anyMatch(keyword -> role.contains(keyword.toLowerCase(Locale.ROOT)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof RoleContainsKeywordsPredicate otherRoleContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherRoleContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
