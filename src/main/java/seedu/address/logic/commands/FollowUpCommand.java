package seedu.address.logic.commands;

/**
 * Represents an operation that manages a contact's follow-up.
 */
public abstract class FollowUpCommand extends Command {

    public static final String COMMAND_WORD = "followup";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Creates, replaces or clears a contact's follow-up.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "by/YYYY-MM-DD abt/DESCRIPTION\n"
            + "Both fields are mandatory when setting a follow-up. Past dates are accepted.\n"
            + "To remove a follow-up: " + COMMAND_WORD + " INDEX clear\n"
            + "Example: " + COMMAND_WORD + " 1 by/2026-11-01 abt/Confirm PCB quotation\n"
            + COMMAND_WORD + " 1 clear";
}
