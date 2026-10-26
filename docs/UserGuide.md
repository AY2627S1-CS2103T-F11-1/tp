---
layout: page
title: User Guide
---

KeyBossWarriorsPro is a specialized **desktop application for managing contacts and vendor pipelines, optimized for Artisan Mechanical Keyboard Shop Managers through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, KeyBossWarriorsPro can help you manage your keyboard shop associates, hardware suppliers, and fabrication partners faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-F11-1/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your KeyBossWarriorsPro.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar keybosswarriorspro.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 r/Supplier` : Adds a contact named `John Doe` to KeyBossWarriorsPro.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters are ignored.<br>
  For example, `help 123` is interpreted as `help`.
* For `list`, additional arguments are ignored and a warning is displayed.
  For example, `list 3` lists all contacts and does not navigate to contact 3.
* For `exit`, a message explains that any extra text is ignored before the application closes.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Adding a contact: `add`

Adds a contact to KeyBossWarriorsPro.

**Format:** `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS r/ROLE [t/TAG]…​`
* `ROLE` represents the contact's role or affiliation in your keyboard shop (e.g., `Supplier`, `Employee`, `Partner`, `Customer`). It cannot be empty. Multiple consecutive spaces are collapsed into a single space, and role comparisons are case-insensitive.
* Contacts are identified as duplicates by phone number or email address (email comparisons are case-insensitive). You cannot add a contact who shares a phone number or email address with an existing contact, but contacts may share the same name.
* Unrecognized prefixes and extraneous arguments are not permitted and will be rejected with an error message indicating the unrecognized prefix(es).

**Field Constraints**

Field | Constraint
--------|------------------
`NAME` | May contain alphanumeric characters and spaces.
`PHONE_NUMBER` | Must contain at least 3 digits. Only digits are accepted.
`EMAIL` | Must follow a valid email format, such as `john@example.com`.
`ADDRESS` | May contain spaces and special characters.
`ROLE` | May contain alphanumeric characters, spaces, and punctuation (`-`, `/`, `&`, `'`). Must start and end with an alphanumeric character. Multiple consecutive spaces are collapsed into a single space.
`TAG` | Optional. May contain alphanumeric characters, spaces, and punctuation (`-`, `/`, `&`, `'`). Must start and end with an alphanumeric character, and cannot exceed 50 characters. Multiple consecutive spaces are collapsed into a single space. Tag comparisons and deduplication are case-insensitive.


<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A contact can have any number of tags, including zero.
</div>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 r/Supplier`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 r/Employee t/criminal`


### Clearing all entries: `clear`

Clears all entries from KeyBossWarriorsPro.

**Format:** `clear`


### Deleting a contact: `delete`

Deletes the contact at the specified position in the currently displayed contact list.

**Format:** `delete INDEX`

* `INDEX` **must be a positive integer** 1, 2, 3, …​
* `INDEX` refers to the contact's position in the contact list currently displayed to the user.
* The index is temporary and may change when commands such as `list`, `find`, `add`, `edit`, or `delete` change the displayed list.

Examples:
* `list` followed by `delete 2` deletes the second contact in the currently displayed list.
* `find Betsy` followed by `delete 1` deletes the 1st contact in the results of the `find` command.


### Editing a contact: `edit`

Edits an existing contact in KeyBossWarriorsPro.

**Format:** `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [r/ROLE] [t/TAG_TO_ADD]…​ [dt/TAG_TO_DELETE]…​`

* `INDEX` must be a positive integer and must refer to a contact in the currently displayed list.
* At least one field or tag operation must be provided.
* Supplied name, phone number, email address, address, and role values replace the corresponding existing values. Omitted fields keep their existing values.
* Field constraints described under `add` apply to every field supplied with `edit`.
* You cannot edit a contact's phone number or email address to match another existing contact in KeyBossWarriorsPro (email comparisons are case-insensitive).
* `t/TAG_TO_ADD` adds the named tag while preserving the contact's existing tags. Adding a tag the contact already has succeeds without creating a duplicate.
* `dt/TAG_TO_DELETE` removes the named tag while preserving all other tags. Every tag requested for deletion must already belong to the selected contact.
* Repeat `t/` or `dt/` to add or delete several tags. Both operations can be used in the same command, in any order.
* Tag comparisons are case-insensitive, and multiple consecutive spaces within a tag name are collapsed into one. For example, `dt/TRAINEE` removes an existing `trainee` tag.
* A tag cannot be supplied to both `t/` and `dt/` in the same command, even with different letter casing.
* Empty or whitespace-only `t/` and `dt/` values are invalid. To remove all tags, explicitly name each one using a separate `dt/TAG_TO_DELETE`.
* Each edit succeeds in full or makes no changes. If any requested deletion is missing or another part of the command is invalid, all contact details and tags stay unchanged.

Examples:

* `edit 1 r/Employee` Changes the role of the 1st contact to `Employee`.
* `edit 1 p/91234567 e/johndoe@example.com r/Supplier` Updates the 1st contact's phone number, email address, and role.
* `edit 2 n/Betsy Crower r/Customer t/regular` Changes the 2nd contact's name and role, and adds `regular` while preserving existing tags.

For each example below, assume contact 1 initially has the tags `trainee` and `supplier`:

* `edit 1 t/solderer` Results in `trainee`, `supplier`, and `solderer`.
* `edit 1 dt/trainee` Results in `supplier`.
* `edit 1 t/solderer dt/trainee` Results in `supplier` and `solderer`.
* `edit 1 dt/trainee dt/supplier` Removes both named tags, leaving the contact with no tags.

**Tag errors**

Command | Error message
--------|------------------
`edit 1 t/` or `edit 1 dt/` | `Tag names cannot be empty. Specify a tag after t/ or dt/.`
`edit 1 t/trainee dt/TRAINEE` | `Cannot add and delete the same tag: trainee. Remove it from either t/ or dt/.`
`edit 1 n/Alex dt/trainee dt/solderer` | `Cannot delete tag: solderer. The selected contact does not have this tag.` The name and both original tags remain unchanged.


### Managing a contact's follow-up: `followup`

Creates, replaces, or removes a contact's pending follow-up. Each contact can have zero or one follow-up, regardless of role.

**Format:** `followup INDEX by/DUE_DATE abt/DESCRIPTION`

* `INDEX` must be a positive integer referring to a contact in the currently displayed list, including results from `find` or `followups`.
* Both `by/` and `abt/` are mandatory and must appear exactly once. They may appear in either order.
* Setting a follow-up replaces any existing follow-up in full. To change only the date or description, supply both fields again.
* `DUE_DATE` must be a valid calendar date in `YYYY-MM-DD` format, with a year from `0000` to `9999`. Past dates are accepted.
* `DESCRIPTION` must not be blank and must not exceed 200 Java string characters after surrounding whitespace is removed. Most ordinary characters count as one; some characters, such as emoji, count as two.
* Description casing and internal spacing are preserved. Internal control characters, such as tabs and newlines, and Unicode line or paragraph separators are not permitted.
* Prefix-like text preceded by a space is interpreted as command syntax. Avoid text such as ` by/`, ` abt/`, or ` date/` inside descriptions; these can produce duplicate-prefix, unknown-prefix, or invalid-value errors.
* Successful changes preserve other contact details and the current filter. A contact may disappear from the results if it no longer matches that filter.
* Follow-ups are displayed on contact cards and saved automatically. Changes support `undo` and `redo`.
* Editing other contact details preserves the follow-up. Deleting the contact removes its follow-up.

Examples:

* `followup 1 by/2026-11-01 abt/Confirm PCB quotation` sets or replaces contact 1's follow-up.
* `find n/Betsy` followed by `followup 1 by/2026-11-02 abt/Confirm weekend availability` sets a follow-up for the first displayed search result.

#### Removing a follow-up

**Format:** `followup INDEX clear`

* Removes the follow-up without deleting the contact.
* Use this when the action is completed or cancelled. Completed follow-ups are not retained in a history.
* The selected contact must already have a follow-up.
* `clear` cannot be combined with `by/`, `abt/`, or extra arguments.
* This is different from the standalone `clear` command, which removes all contacts.

Example:

* `followup 1 clear` removes contact 1's follow-up.


### Viewing contacts with follow-ups: `followups`

Displays contacts with pending follow-ups across the entire address book, replacing any previous search filter.

**Formats:**

Command | Behavior
--------|---------
`followups` | Displays all contacts with a follow-up.

`followups overdue` | Displays contacts with follow-ups due before today.

`followups by/DUE_DATE` | Displays contacts with follow-ups due on or before the specified date.
* Contacts without follow-ups are excluded.
* Results retain address-book order and are numbered from 1. Use these displayed indexes for subsequent contact commands.
* A follow-up due today is not overdue.
* Today is determined using the computer's local date when the command executes. Rerun the command to refresh date-based results after the date changes.
* Date cutoffs follow the same date constraints as `followup`. Past cutoffs are accepted.
* Query modes cannot be combined. Index arguments are not supported.
* Queries do not modify contact details or undo/redo history.
* If no contacts match, the list is empty and the application displays `No follow-ups found.`

Examples:

* `followups`
* `followups overdue`
* `followups by/2026-11-01`


### Undoing a change: `undo`

Reverses the most recent successful modifying command (`add`, `delete`, `edit`, or `clear`).

**Format:** `undo`

* Each `undo` reverses one modifying command. You can undo repeatedly until you reach the start of the current session.
* Successful undo displays `Undo success!` and resets the view to show all contacts.
* If no changes can be undone, the application displays `No more commands to undo!`.
* Commands such as `find`, `list`, and `help` are not undone.
* Undo history is cleared when you close the application. Changes from previous sessions cannot be undone.
* Additional arguments, such as `undo 2`, are rejected.

Example:

1. `delete 1` deletes the first displayed contact.
2. `undo` restores the deleted contact and displays all contacts.

### Redoing a change: `redo`

Reapplies the most recently undone modifying command.

**Format:** `redo`

* Each `redo` reapplies one undone change. You can redo repeatedly while undone changes remain.
* Successful redo displays `Redo success!` and resets the view to show all contacts.
* If no changes can be redone, the application displays `No more commands to redo!`.
* A new successful modifying command after undo discards the remaining redo history.
* Failed validation and commands such as `find`, `list`, and `help` preserve redo history.
* Redo history is cleared when you close the application.
* Additional arguments, such as `redo 2`, are rejected.

Example:

1. `delete 1` deletes the first displayed contact.
2. `undo` restores the contact.
3. `redo` deletes that contact again.

Undo and redo automatically save the resulting contact data. If saving fails,
the application reports an error, but the change remains in the running application.


### Exiting the program: `exit`

Exits the program after displaying a closing message for a short time.

**Format:** `exit`

Any extra text after `exit` is ignored. The closing message explains this, and the application exits without asking for confirmation.

For example, `exit -f` exits in the same way as `exit`.



### Locating contacts: `find`

Finds contacts matching one or more name, role, or tag criteria. All searches are case-insensitive and support partial matching (substring matching).

#### Prefixed Search

**Format:** `find [n/NAME_PHRASE]... [r/ROLE_PHRASE]... [t/TAG_PHRASE]...`

* At least one `n/`, `r/`, or `t/` prefix must be provided.
* `n/` specifies a name search phrase.
* `r/` specifies a role search phrase.
* `t/` specifies a tag search phrase. Note that since tags cannot contain spaces, multi-word tag phrases (e.g. `t/good friend`) will return no results.
* Each value after a prefix is treated as a continuous search phrase until the next prefix or the end of the command.
* **Partial & Case-Insensitive Matching**: Matches if the term appears anywhere within the field, regardless of upper or lower case (e.g., `n/alex` matches `Alexander`, `r/distrib` matches `Distributor`).
* **Search Logic:**
    * **Same prefix → `OR` logic**: A contact matches if it satisfies *at least one* of the values given for that prefix type (e.g. `n/alice n/bob` returns contacts whose name contains "alice" *or* "bob", case-insensitively).
    * **Different prefixes → `AND` logic**: Results are then narrowed so that a contact must satisfy *all* prefix types specified (e.g. `n/alice r/manager` returns contacts whose name contains "alice" *and* whose role contains "manager", case-insensitively).
    * In short: **OR within a prefix, AND across prefixes.**

Examples:
* `find n/alex` matches contacts whose names contain `alex`, such as
  `Alex Yeoh`, `alexander`, and `Alexis Tan`.
* `find n/John Doe` matches contacts whose names contain the phrase
  `John Doe`, such as `John Doe` and `John Doeder`.
* `find r/distrib` matches contacts whose roles contain `distrib`, such as
  `Distributor` and `Regional Distributor`.
* `find t/frie` matches contacts with at least one tag containing `frie`,
  such as `friend` or `Friendly`.
* `find r/supp r/distrib` matches contacts whose roles contain either
  `supp` or `distrib`.
* `find t/friend t/family` matches contacts with at least one tag containing
  either `friend` or `family`.
* `find n/ali r/emp` matches contacts whose names contain `ali` and whose
  roles contain `emp`, such as a contact named `Alice` with role `Employee`.
* `find n/John n/ali r/supp` matches contacts whose names contain either
  `John` or `ali`, and whose roles contain `supp`.

#### Legacy Search
Searches contacts by name using space-separated keywords without prefixes.

**Format:** `find NAME_KEYWORD [NAME_KEYWORD]...`

* Searches contacts by **name only**.
* Space-separated words are treated as individual name keywords.
* **Search Logic**: Keywords use `OR` logic. A contact is returned if its name contains *at least one* of the specified keywords.
* **Partial & Case-Insensitive Matching**: Matches if the keyword appears anywhere within a name, regardless of upper or lower case (e.g., `han` matches `Hans Bo` and `Johan Meyer`).
* Keyword order does not matter (e.g., `find Bo Hans` can match a contact named `Hans Bo`).
* Adding more keywords broadens the search results; it does not narrow them.
* **Cannot Mix with Prefixes**: Legacy unprefixed keywords cannot be combined with prefixed search values (e.g., `find Alice n/Bob` or `find Alice r/Employee` is rejected).

Examples:
* `find John` matches `john` and `John Doe` *(case-insensitive)*.
* `find han` matches `Hans Bo` and `Johan Meyer` *(partial substring match)*.
* `find alex david` matches `Alex Yeoh` and `David Li` *(OR logic: matches either keyword)*.<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)
* `find Alice n/Bob` or `find Alice r/Employee` is invalid because legacy keywords and prefixed values cannot be mixed.



### Viewing help: `help`

Opens the help window, which lists the available commands and their usage instructions. For detailed explanations and examples, refer to this user guide.

![help message](images/help.png)

**Format:** `help`


### Listing all contacts: `list`

Displays all contacts in KeyBossWarriorsPro and resets any active search filter.

**Format:** `list`

Examples:

* `list` displays all contacts.
* `list 3` displays all contacts and warns that `3` was ignored.
* `list abc xyz` displays all contacts and warns that the arguments were ignored.

Whitespace-only arguments are treated as empty input.

If there are no contacts, the application displays:

```text
No contacts found.
```


### Saving the data

KeyBossWarriorsPro automatically saves data after every command. You do not need to save manually.

Data saves are performed atomically. The application writes updates to a temporary file before replacing the saved data file. This prevents file corruption if the application closes unexpectedly or experiences a disk error during a save. If saving fails, the application leaves your existing data file untouched and displays an error message explaining the failure.

### Editing the data file

KeyBossWarriorsPro data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="block" class="alert alert-warning">

:exclamation: **Caution:**<br>

If manual edits make the data file invalid or corrupted:

* KeyBossWarriorsPro automatically creates a timestamped backup copy in the `data/` folder (for example, `addressbook.json.backup.20261012_123456`).

* A warning message appears in the result box on launch showing the backup file path.

* KeyBossWarriorsPro starts with an empty contact book so that you can continue using the application without immediately overwriting your corrupted data file.

* To restore your contacts, close KeyBossWarriorsPro, open the backup file in a text editor to fix any syntax errors, and rename it back to `addressbook.json` before relaunching the app.

Certain invalid values can also cause unexpected behavior. Edit the data file only if you are confident in editing JSON formatted files.
</div>


### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------


## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous KeyBossWarriorsPro home folder.

**Q**: What should I do if my data file is corrupted?<br>
**A**: When KeyBossWarriorsPro detects a corrupted data file on startup, it automatically creates a timestamped backup in the `data/` folder and starts with an empty contact list. Close KeyBossWarriorsPro, open the backup file in a text editor to fix any invalid syntax, and rename it back to `addressbook.json` before relaunching the app.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS r/ROLE [t/TAG]…​` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 r/Supplier t/friend t/colleague`
**Clear** | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [r/ROLE] [t/TAG_TO_ADD]…​ [dt/TAG_TO_DELETE]…​`<br> e.g., `edit 1 t/solderer dt/trainee` (requires contact 1 to have `trainee`)
**Exit** | `exit` 
**Find** | `find [n/NAME_PHRASE]... [r/ROLE_PHRASE]... [t/TAG_PHRASE]...` or `find KEYWORD [MORE_KEYWORDS]` (Legacy Search)<br> e.g., `find n/John r/Supplier`, `find James Jake`
**Help** | `help`
**List** | `list`
**Redo** | `redo`
**Undo** | `undo`
**Follow-up** | `followup INDEX by/DUE_DATE abt/DESCRIPTION` or `followup INDEX clear`<br> e.g., `followup 1 by/2026-11-01 abt/Confirm quotation`
**Follow-up queries** | `followups`, `followups overdue`, or `followups by/DUE_DATE`<br> e.g., `followups by/2026-11-01`
