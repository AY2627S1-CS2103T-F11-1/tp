package seedu.address.logic.commands;

/**
 * Represents an operation that manages a contact's follow-up.
 */
public abstract class FollowUpCommand extends Command {

    public static final String COMMAND_WORD = "followup";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Creates, replaces or clears a contact's follow-up.\n"
            + "Parameters: INDEX by/YYYY-MM-DD abt/DESCRIPTION\n"
            + "INDEX refers to the currently displayed contact list and must be a positive integer.\n"
            + "Both fields are mandatory when setting a follow-up. An existing follow-up is replaced.\n"
            + "Past dates are accepted. Use clear instead of the fields to remove a follow-up.\n"
            + "Examples:\n"
            + COMMAND_WORD + " 1 by/2026-11-01 abt/Confirm PCB quotation\n"
            + COMMAND_WORD + " 1 clear";
}
