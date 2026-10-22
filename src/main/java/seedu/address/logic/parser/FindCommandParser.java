package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.RoleContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        requireNonNull(args);

        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_MISSING_CRITERIA);
        }

        ArgumentTokenizer.verifyNoUnrecognizedPrefixes(args, PREFIX_NAME, PREFIX_ROLE, PREFIX_TAG);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_ROLE, PREFIX_TAG);

        List<String> prefixedNames = argMultimap.getAllValues(PREFIX_NAME);
        List<String> prefixedRoles = argMultimap.getAllValues(PREFIX_ROLE);
        List<String> prefixedTags = argMultimap.getAllValues(PREFIX_TAG);

        boolean hasPrefixedNames = !prefixedNames.isEmpty();
        boolean hasPrefixedRoles = !prefixedRoles.isEmpty();
        boolean hasPrefixedTags = !prefixedTags.isEmpty();

        if (hasPrefixedNames || hasPrefixedRoles || hasPrefixedTags) {
            // Blank n/ or blank r/ or blank t/
            if (prefixedNames.stream().anyMatch(String::isBlank)
                    || prefixedRoles.stream().anyMatch(String::isBlank)
                    || prefixedTags.stream().anyMatch(String::isBlank)) {
                throw new ParseException(FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);
            }

            // Error: using both legacy and prefixed versions e.g. find Alice n/Tan
            if (!argMultimap.getPreamble().isBlank()) {
                throw new ParseException(FindCommand.MESSAGE_MIXED_SEARCH_MODE);
            }

            List<Predicate<Person>> predicates = new ArrayList<>();

            if (hasPrefixedNames) {
                predicates.add(new NameContainsKeywordsPredicate(prefixedNames));
            }

            if (hasPrefixedRoles) {
                predicates.add(new RoleContainsKeywordsPredicate(prefixedRoles));
            }

            if (hasPrefixedTags) {
                predicates.add(new TagContainsKeywordsPredicate(prefixedTags));
            }

            assert !predicates.isEmpty() : "Prefixed search must produce at least one predicate";

            return new FindCommand(predicates.toArray(Predicate[]::new));
        }

        // Preserve legacy behaviour
        String[] legacyKeywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(legacyKeywords)));
    }
}
