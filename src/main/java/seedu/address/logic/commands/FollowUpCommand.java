package seedu.address.logic.commands;

/**
 * Represents an operation that manages a contact's follow-up.
 */
public abstract class FollowUpCommand extends Command {

    public static final String COMMAND_WORD = "followup";

    public static final String COMMAND_DESCRIPTION = "Creates, replaces or clears a contact's follow-up. "
            + "INDEX refers to the currently displayed contact list and must be a positive integer. "
            + "Both fields are mandatory when setting a follow-up. Past dates are accepted.";
    public static final String COMMAND_PARAMETERS = "INDEX by/YYYY-MM-DD abt/DESCRIPTION or INDEX clear";
    public static final String COMMAND_EXAMPLE = COMMAND_WORD + " 1 by/2026-11-01 abt/Confirm PCB quotation"
            + "\n         " + COMMAND_WORD + " 1 clear";
    public static final String COMMAND_USAGE = COMMAND_WORD + ": " + COMMAND_DESCRIPTION + "\nParameters: "
            + COMMAND_PARAMETERS + "\nExample: " + COMMAND_EXAMPLE;

}
