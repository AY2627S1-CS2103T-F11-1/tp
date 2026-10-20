package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;

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

        ArgumentTokenizer.verifyNoUnrecognizedPrefixes(args, PREFIX_NAME);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME);

        List<String> prefixedNames = argMultimap.getAllValues(PREFIX_NAME);

        if (!prefixedNames.isEmpty()) {
            // Blank n/
            if (prefixedNames.stream().anyMatch(String::isBlank)) {
                throw new ParseException(FindCommand.MESSAGE_EMPTY_NAME_VALUE);
            }

            // Error: using both legacy and prefixed versions e.g. find Alice n/Tan
            if (!argMultimap.getPreamble().isBlank()) {
                throw new ParseException(FindCommand.MESSAGE_MIXED_SEARCH_MODE);
            }

            return new FindCommand(new NameContainsKeywordsPredicate(prefixedNames));
        }

        // Preserve legacy behaviour
        String[] legacyKeywords = args.trim().split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(legacyKeywords)));
    }
}
