package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.RoleContainsKeywordsPredicate;

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

        ArgumentTokenizer.verifyNoUnrecognizedPrefixes(args, PREFIX_NAME, PREFIX_ROLE);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_ROLE);

        List<String> prefixedNames = argMultimap.getAllValues(PREFIX_NAME);
        List<String> prefixedRoles = argMultimap.getAllValues(PREFIX_ROLE);

        boolean hasPrefixedNames = !prefixedNames.isEmpty();
        boolean hasPrefixedRoles = !prefixedRoles.isEmpty();

        if (hasPrefixedNames || hasPrefixedRoles) {
            // Blank n/ or blank r/
            if (prefixedNames.stream().anyMatch(String::isBlank)
                    || prefixedRoles.stream().anyMatch(String::isBlank)) {
                throw new ParseException(FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);
            }

            // Error: using both legacy and prefixed versions e.g. find Alice n/Tan
            if (!argMultimap.getPreamble().isBlank()) {
                throw new ParseException(FindCommand.MESSAGE_MIXED_SEARCH_MODE);
            }

            NameContainsKeywordsPredicate namePredicate = new NameContainsKeywordsPredicate(prefixedNames);
            RoleContainsKeywordsPredicate rolePredicate = new RoleContainsKeywordsPredicate(prefixedRoles);

            if (hasPrefixedNames && hasPrefixedRoles) {
                return new FindCommand(namePredicate, rolePredicate);
            }

            if (hasPrefixedNames) {
                return new FindCommand(namePredicate);
            }

            return new FindCommand(rolePredicate);
        }

        // Preserve legacy behaviour
        String[] legacyKeywords = args.trim().split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(legacyKeywords)));
    }
}
