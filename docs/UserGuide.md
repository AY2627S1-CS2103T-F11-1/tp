---
layout: page
title: User Guide
---

## Introduction

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
   ![Ui](images/UGQuickStartUI.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the full help window, or type **`help add`** to see the `add` command's usage in the result display.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `select 1` : Shows the first displayed contact's full details in the right panel.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 r/Supplier` : Adds a contact named `John Doe` to KeyBossWarriorsPro.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Command reference](#command-reference) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Understanding the interface

### Viewing contact details

Read a contact's full information in the details panel on the right.

**Steps and controls**

1. Click a contact in the list, or enter `select INDEX` using its currently displayed number.
2. When the contact list has keyboard focus, use the Up and Down arrow keys to move between contacts.
   The details panel updates without moving keyboard focus.
3. Scroll the details panel to read long content. Drag the divider to adjust the two panel widths.
4. Press **Esc** from the contact list, details panel, or command feedback to return to command input.
   Your draft command, cursor position, text selection, and selected contact are preserved.

**Expected result**

The selected contact's name, role, tags, phone number, email address, address, and follow-up appear in the details panel.
Empty tags and absent follow-ups do not leave blank sections.

On startup, or when no contact is selected, the panel shows `Select a contact to view details.`

![Selected contact and full details in the two-panel interface](images/UGViewingContactDetails.png)

**Notes**

List cards show the name, role, phone number, email address, and tags. Long names and email addresses use an ellipsis;
hover over either to read it in full. Each panel scrolls independently. The window has a minimum size of 800 by 600 pixels.

A badge beside the role identifies contacts with a follow-up. Select the contact to read its date and description.
Badges use your computer's local date:

Follow-up due date | Badge
-------------------|------
After today | **Follow-up** (bold)
Today | **Due today** (bold)
Before today | **Overdue** (bold and underlined)
No follow-up | No badge or reserved space

A follow-up due before today is also marked **Overdue** in the details panel. A follow-up due today is not overdue.
The badges and selected contact's overdue display refresh approximately once per minute while the window is open,
and immediately when you return focus to the window or reopen it. Refreshing preserves your selection, scroll positions,
and draft command. After midnight or sleep, the next refresh uses the current local date.

* **Selection after commands:** Commands keep the selected contact when it remains visible, even if its list position changes.
  Editing that contact or changing its follow-up refreshes its details. Changing another contact keeps your current selection.
* **Contacts removed from view:** If the selected contact is deleted or filtered out, or all contacts are cleared, the panel
  returns to the selection prompt. Deleting a selected contact does not automatically select its neighbour.
* **Undo, redo, and invalid input:** Successful `undo` and `redo` commands clear selection. Commands rejected because of
  invalid input keep selection.
* **Saving errors:** If saving an addition, edit, deletion, clear, or follow-up change fails, the contact changes are rolled back.
  The original selection is retained when that contact still exists in the restored list; otherwise the panel returns to the
  selection prompt. Command feedback shows the saving error.

--------------------------------------------------------------------------------------------------------------------

## Command conventions

Type one command in the command box and press **Enter** to execute it.

* Words in `UPPER_CASE` are placeholders for values you supply. In `add n/NAME`, replace `NAME` with a value such as `John Doe`.
* Lowercase command names, prefixes such as `n/`, and keywords such as `clear` are entered as shown.
* Square brackets mark optional parts. For example, `[t/TAG]` can be omitted. Do not type the brackets.
* An optional part followed by `…` can be repeated: `[t/TAG]…` allows no tags, one tag, or several `t/` fields. Do not type the ellipsis.
* Named fields can usually be supplied in any order. Keep positional arguments, such as `INDEX`, in the positions shown in the command's format.
  For `sort`, field order determines sorting priority.
* `INDEX` is a positive whole number starting at 1 and refers to the **currently displayed list**. Check the numbers again after a search,
  follow-up query, sort, edit, or deletion. Commands using an index target that displayed contact.
* Command-specific requirements, such as supplying at least one search criterion or one field to edit, appear with the command's format and parameters.
* See [Input rules](#input-rules) for accepted values and for entering prefix-like text inside a field value.

If you use a PDF version of this guide, take care when copying commands across line breaks: spaces may be lost when pasting into the application.

--------------------------------------------------------------------------------------------------------------------

<a id="features"></a>

## Command reference

Commands are grouped by task. The index-based commands use the numbers in your current displayed list.

### Manage contacts

#### Adding a contact: `add`

Adds a new contact to KeyBossWarriorsPro.

**Format:** `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS r/ROLE [t/TAG]…`

**Parameters**

* Name, phone number, email address, address, and role are required. Supply each once, in any order.
* Tags are optional. Repeat `t/` to add multiple tags; there is no limit on the number of tags.
* Separate fields with a space. Unknown prefixes are rejected, and a recognised prefix starts the next field.

See [Input rules](#input-rules) for accepted field values, spacing, character limits, and slash escaping.

**Behaviour**

* Adds the contact and saves the change automatically.
* Clears any active search or follow-up filter and displays all contacts. The current sort order is retained,
  so the new contact's position depends on that order.
* Rejects the addition if either the phone number or email matches an existing contact, including one hidden by a filter.
  See the [duplicate-contact rules](#contact-duplicates) for how values are compared.
* Supports [undo](#undoing-a-change-undo) and [redo](#redoing-a-change-redo) during the current session.

**Examples**

Before running either example, check that its phone number and email address are not already used by another contact.

Basic use:

```text
add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 r/Supplier
```

Result: Adds John Doe as a supplier with no tags and displays all contacts in the current order.

Adding a keyboard-parts supplier with multiple tags:

```text
add n/José Tan p/91234568 e/o'connor@example.com a/王 Street 1 r/Supplier (PCB) t/62.5g t/PCB v1.6 t/Switches (5-pin)
```

Result: Adds José Tan with the role `Supplier (PCB)` and the tags `62.5g`, `PCB v1.6`, and `Switches (5-pin)`.

**Common mistakes and recovery**

Problem | What to do
--------|-----------
A required field is missing or supplied more than once | Supply `n/`, `p/`, `e/`, `a/`, and `r/` exactly once each.
The phone number or email is already used | Check existing contacts. Use `edit` to update an existing contact, or ensure both values are unique when adding a new contact.
The phone number contains spaces, `+`, or punctuation | Use 3–20 digits (`0`–`9`) only.
Text inside a value looks like a field prefix | Escape its slash with `\/`. See [slash escaping](#slash-escaping) for examples.
Saving fails | Resolve the file or folder problem reported in the command feedback, then try again. The addition is rolled back if saving fails.

#### Editing a contact: `edit`

Updates the details or tags of a contact in the currently displayed list.

**Format:** `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [r/ROLE] [t/TAG_TO_ADD]… [dt/TAG_TO_DELETE]…`

**Parameters**

* `INDEX` identifies a contact in the [currently displayed list](#command-conventions).
* Supply at least one field or tag operation. Supply each of `n/`, `p/`, `e/`, `a/`, and `r/` at most once.
* Repeat `t/` to add tags or `dt/` to remove tags. Both operations can be used in the same command, in any order.
* Every tag requested for deletion must already belong to the contact. To remove all tags, name each one using a separate `dt/`.
* Empty or whitespace-only tag values are invalid. A tag cannot be added and deleted in the same command, including different spellings that differ only in letter case.
* Use the shared [input rules](#contact-input-rules) and [slash-escaping rules](#slash-escaping), including for `dt/` values.
  Unknown prefixes, such as `xx/`, are rejected.

**Behaviour**

* Supplied name, phone number, email address, address, and role values replace the existing values. Omitted fields keep their values.
* `t/` adds tags without replacing existing tags. Adding a tag that is already present succeeds without creating a duplicate.
* Tag comparisons ignore letter case and recognise Unicode case equivalents. For example, `dt/TRAINEE` removes `trainee`,
  and `σ` and `ς` are treated as equivalent. See [Input rules](#input-rules) for spacing and comparison details.
* Neither the new phone number nor the new email may match another contact, including one hidden by a filter.
  Keeping this contact's own phone number or email is allowed. See [duplicate contacts](#contact-duplicates).
* Each edit succeeds in full or makes no contact changes. A missing tag deletion or another invalid value rejects the entire edit.
* A successful edit clears any search or follow-up filter and displays all contacts. The current sort order is retained;
  changing a name or role may change the contact's displayed index.
* The contact's follow-up is preserved. Changes are saved automatically and support [undo](#undoing-a-change-undo) and [redo](#redoing-a-change-redo).

**Examples**

Basic use, assuming contact 1 is displayed:

```text
edit 1 r/Supplier
```

Result: Changes the first displayed contact's role to `Supplier`, keeps the other details, and displays all contacts.

Updating contact details, assuming contact 1 exists and the new phone number and email are not used by another contact:

```text
edit 1 p/91234567 e/johndoe@example.com r/Supplier
```

Result: Updates the contact's phone number, email, and role in one command.

For each independent example below, assume contact 1 initially has the tags `trainee` and `supplier`:

Command | Expected result
--------|----------------
`edit 1 t/solderer` | Keeps `trainee` and `supplier`, and adds `solderer`.
`edit 1 dt/trainee` | Removes `trainee`, leaving `supplier`.
`edit 1 t/solderer dt/trainee` | Leaves `supplier` and `solderer`.
`edit 1 dt/trainee dt/supplier` | Removes both tags, leaving no tags.

**Common mistakes and recovery**

Problem | What to do
--------|-----------
`edit 1` supplies no change | Add at least one field or tag operation.
`edit 1 t/` or `edit 1 dt/` | The feedback is `Tag names cannot be empty. Specify a tag after t/ or dt/.` Supply a tag name.
`edit 1 t/trainee dt/TRAINEE` | The feedback is `Cannot add and delete the same tag: trainee. Remove it from either t/ or dt/.`
A requested tag is absent | Check the contact's tags before using `dt/`. For example, if contact 1 has only `trainee` and `supplier`, `edit 1 n/Alex dt/trainee dt/solderer` reports `Cannot delete tag: solderer. The selected contact does not have this tag.` The name and both original tags remain unchanged.
An index is no longer valid after a search or sort | Check the displayed numbers again. Use `list` if you want to choose from all contacts.

#### Managing a contact's follow-up: `followup`

Creates, replaces, or removes a contact's pending follow-up. Each contact can have zero or one follow-up, regardless of role.

**Format**

Command | Purpose
--------|--------
`followup INDEX by/DUE_DATE abt/DESCRIPTION` | Creates or replaces a follow-up.
`followup INDEX clear` | Removes an existing follow-up without deleting the contact.

**Parameters**

* `INDEX` identifies a contact in the [currently displayed list](#command-conventions).
* When setting a follow-up, supply `by/` and `abt/` exactly once each, in either order.
* `DUE_DATE` must be a valid calendar date in `YYYY-MM-DD` format, with a year from `0000` to `9999`. Past dates are accepted.
* `DESCRIPTION` follows the [text validation and trimming rules](#text-field-rules), while preserving capitalisation and internal spacing.
  The limit is **200 [characters](#character-counting) after slash decoding and trimming**.
* Descriptions use the [slash-escaping rules](#slash-escaping). For example, `abt/Confirm PCB\/Case quotation` stores `Confirm PCB/Case quotation`.
* For removal, the contact must already have a follow-up. `clear` cannot be combined with `by/`, `abt/`, or extra arguments.

**Behaviour**

* Setting a follow-up replaces any existing follow-up in full. To change only the date or description, supply both fields again.
* Clearing a follow-up can be used when its action is completed or cancelled. Completed follow-ups are not retained in a history.
* Successful changes preserve the contact's other details, the current filter, and sorting. A contact may disappear from a follow-up query
  if the new date no longer matches, or if its follow-up is cleared.
* Contacts with a follow-up show **Follow-up**, **Due today**, or **Overdue** in the list. Select a contact to read its date and description
  in the details panel. See [Viewing contact details](#viewing-contact-details).
* Changes are saved automatically and support [undo](#undoing-a-change-undo) and [redo](#redoing-a-change-redo).
* Editing other contact details preserves the follow-up. Deleting the contact removes its follow-up.

**Examples**

Basic use, assuming contact 1 is displayed:

```text
followup 1 by/2026-11-01 abt/Confirm PCB quotation
```

Result: Sets or replaces the first displayed contact's follow-up with the specified date and description.

Setting a follow-up for a supplier (enter each command separately, and continue only if the search returns a contact):

```text
find r/Supplier
followup 1 by/2026-11-02 abt/Confirm delivery availability
```

Result: Sets the follow-up for the first displayed supplier. Other contact details are preserved.

<a id="removing-a-follow-up"></a>

Removing a follow-up, assuming the currently displayed contact 1 already has one:

```text
followup 1 clear
```

Result: Removes that contact's follow-up while keeping the contact.

**Common mistakes and recovery**

Problem | What to do
--------|-----------
Only a new date or description is supplied | Supply both `by/` and `abt/`; the follow-up is replaced as a whole.
A date is invalid or has the wrong format | Use a real calendar date in `YYYY-MM-DD` format, such as `2026-11-01`.
`followup 1 clear by/2026-11-01` mixes removal with setting fields | Use either the setting format or `followup INDEX clear`.
The contact has no follow-up to clear | Select it to check its details; there is no pending follow-up to remove.
Confusing `followup INDEX clear` with `clear` | The first removes one follow-up. The standalone `clear` command deletes all contacts.

#### Deleting a contact: `delete`

Deletes one contact from the currently displayed list.

**Format:** `delete INDEX`

**Parameters**

* Supply exactly one `INDEX`: a positive number identifying a contact in the [currently displayed list](#command-conventions).
* After a search, follow-up query, or sort, use the numbers shown in that view.

**Behaviour**

* **Deletes the contact immediately, without asking for confirmation. Check the displayed index before executing the command.**
* Removes the contact and its follow-up, if present. The change is saved automatically.
* Keeps the current filter and sort order. Remaining contacts are renumbered.
* If the deleted contact was selected, the details panel returns to its selection prompt without selecting a neighbour.
* Supports [undo](#undoing-a-change-undo) and [redo](#redoing-a-change-redo) during the current session.

**Examples**

Basic use, assuming contact 1 is displayed:

```text
delete 1
```

Result: Deletes the first displayed contact and renumbers the remaining contacts.

Deleting a supplier from search results (enter each command separately, and check the first result before deleting):

```text
find r/Supplier
delete 1
```

Result: Deletes the first matching contact. The supplier filter remains active. Continue with `delete` only if the search returned a contact.

**Common mistakes and recovery**

Problem | What to do
--------|-----------
`delete`, `delete 0`, or a non-numeric index | Supply one positive whole number from the displayed list.
`delete 1 2` | Delete one contact per command; extra indexes are rejected.
The index refers to a previous view | Check the current displayed numbers after filtering, sorting, or another deletion.
No contacts are displayed | The feedback is `No contacts are currently displayed.` Use `list` to show all contacts before choosing an index.
The wrong contact was deleted | Use `undo` before closing the application. If later changes were made, each `undo` reverses the latest change first.

<a id="clearing-all-entries-clear"></a>

#### Clearing all contacts: `clear`

Deletes every contact in KeyBossWarriorsPro.

**Format:** `clear`

**Behaviour**

* **Deletes all contacts immediately, including contacts hidden by a search or follow-up filter. No confirmation is requested.**
* Removes the contacts' follow-ups as well and saves the change automatically.
* The list becomes empty and the details panel returns to its selection prompt.
* A successful clear can be reversed with [undo](#undoing-a-change-undo) during the current session. Undo history is lost when the application closes.
* If the contact book is already empty, displays `KeyBossWarriorsPro is already empty; no contacts were cleared.` and records no new change.

**Examples**

```text
clear
```

Result: Removes all contacts. Use this command only when you intend to empty the entire contact book.

**Common mistakes and recovery**

* `clear 1` still clears **all contacts**; the extra text is ignored. When contacts are cleared, a warning explains that the arguments were ignored.
  Use `delete 1` to delete only the first displayed contact.
* To recover from an accidental clear, use `undo` before closing the application. Changes from previous sessions cannot be undone.

### Browse contacts

#### Listing all contacts: `list`

Displays all contacts in KeyBossWarriorsPro.

**Format:** `list`

**Behaviour**

* Clears any active search or follow-up filter and displays the complete contact list.
* Retains the current sort order. Use [sort](#sorting-all-contacts-sort) to change it or restore insertion order.
* Does not change contact details or undo/redo history.
* If there are no contacts, displays `No contacts found.`

**Examples**

Basic use:

```text
list
```

Result: Displays all contacts in the current order.

Returning to all contacts after a search (enter each command separately):

```text
find r/Supplier
list
```

Result: The first command shows contacts whose roles contain `Supplier`. The second removes that filter and displays
all contacts again, retaining the current sort order.

**Common mistakes and recovery**

`list 3` displays all contacts and warns that `3` was ignored; it does not select contact 3. To view that contact's details,
use `select 3` if that index exists in the displayed list. Other extra text, such as `list abc xyz`, is also ignored with a warning.
Whitespace-only arguments are treated as empty input and do not produce this warning.

#### Locating contacts: `find`

Finds contacts by name, role, or tag using partial matches, ignoring letter case.

**Format**

Search mode | Command
------------|--------
Prefixed search | `find [n/NAME_PHRASE]… [r/ROLE_PHRASE]… [t/TAG_PHRASE]…`
Simple name search | `find NAME_KEYWORD [NAME_KEYWORD]…`

**At least one search criterion is required.** Choose one search mode per command.

**Parameters**

<a id="prefixed-search"></a>

* **Prefixed search:** Use `n/` for names, `r/` for roles, and `t/` for tags. Each prefix needs a non-blank value and can be repeated.
* Each prefixed value is one continuous phrase up to the next prefix or the end of the command. A phrase may contain spaces;
  for example, `find t/good friend` searches for that phrase within a tag.
* Prefixed phrases are trimmed and repeated ordinary spaces are collapsed using the shared [spacing rules](#text-field-rules).
  For example, `find r/Regional   Supplier` and `find r/Regional Supplier` return the same results.

<a id="legacy-search"></a>

* **Simple name search:** Supply space-separated keywords without prefixes. This searches names only, and each word is a separate keyword.
* Do not mix unprefixed keywords with prefixed values. Use the [slash-escaping rules](#slash-escaping) for prefix-like text inside either search mode.

**Behaviour**

* Searches the entire contact book and replaces any previous search or follow-up filter. The current sort order is retained.
* Matches a phrase or keyword anywhere within its field. For example, `han` matches `Hans Bo` and `Johan Meyer`.
* Matching ignores letter case and recognises Unicode case equivalents: `σ` and `ς` match each other.
  Accents are preserved: `JOSÉ` matches `José`, but `Jose` does not match `José`.
* **Prefixed search uses OR within a prefix and AND across different prefixes.** Repeating `r/` accepts any matching role phrase;
  combining `n/` with `r/` requires both a matching name and a matching role.
* **Simple name search uses OR between keywords.** Keyword order does not matter, and adding more keywords broadens the results.
  For example, `find Bo Hans` can match `Hans Bo`.
* Does not change contact details or undo/redo history. Use `list` to return to all contacts.

**Examples**

Basic name search:

```text
find alex david
```

Result: Shows contacts whose names contain `alex` or `david`. With the original sample contacts, this finds Alex Yeoh and David Li.

![Result for the simple name search find alex david](images/UGLegacySearch.png)

Finding suppliers whose names contain a phrase:

```text
find n/John Doe r/Supplier
```

Result: Shows contacts whose names contain `John Doe` **and** whose roles contain `Supplier`.

Other prefixed searches:

Command | Expected result
--------|----------------
`find n/alex` | Names containing `alex`, such as `Alex Yeoh`, `alexander`, or `Alexis Tan`.
`find n/John Doe` | Names containing the continuous phrase `John Doe`, such as `John Doe` or `John Doeder`.
`find r/distrib` | Roles containing `distrib`, such as `Distributor` or `Regional Distributor`.
`find t/frie` | A tag containing `frie`, such as `friend` or `Friendly`.
`find r/supp r/distrib` | Roles containing either `supp` or `distrib`.
`find t/friend t/family` | At least one tag containing either `friend` or `family`.
`find n/ali r/emp` | A name containing `ali` and a role containing `emp`, such as Alice with the role Employee.
`find n/John n/ali r/supp` | A name containing either `John` or `ali`, together with a role containing `supp`.

**Common mistakes and recovery**

Problem | What to do
--------|-----------
`find` has no criteria, or a prefix such as `n/` has no value | Supply at least one keyword or a non-blank value after each prefix.
`find Alice r/Supplier` mixes the two modes | Use `find n/Alice r/Supplier`, or use simple name search without prefixes.
Expecting `find John Doe` to require both words | Simple name search matches either keyword. Use `find n/John Doe` to search for the continuous phrase.
A search value contains prefix-like text such as `s/o` | Escape the slash: `find n/David s\/o John` searches for the phrase `David s/o John`, while `find s\/o` searches names for `s/o`.
No contacts match | Change or broaden the criteria, or use `list` to show all contacts. A new `find` searches the entire book again.

#### Sorting all contacts: `sort`

Displays all contacts sorted by name, role, or both, or restores their insertion order.

**Format**

Command | Purpose
--------|--------
`sort` | Sorts by name.
`sort n/` | Sorts by name.
`sort r/` | Sorts by role.
`sort n/ r/` | Sorts by name, then role for contacts with matching names.
`sort r/ n/` | Sorts by role, then name for contacts with matching roles.
`sort clear` | Restores insertion order.

**Parameters**

* Use the lowercase prefixes `n/` and `r/`, separated by whitespace. Each may appear only once and takes no value.
* When both are supplied, the first field takes priority and the second breaks ties.
* `sort` without arguments defaults to name order. `clear` cannot be combined with fields or other arguments.

**Behaviour**

* Sorting is ascending and case-insensitive. For example, `alice` and `Alice` are treated as equal when comparing names.
* Every sort command, including `sort clear`, removes any active search or follow-up filter and displays **all contacts**.
* Contact indexes follow the displayed order. Check them again before using an index-based command.
* `find`, `list`, and `followups` retain the selected sort order. Adding, editing, or deleting contacts updates the displayed order automatically.
  Editing a name or role may move that contact to a different index.
* A new sort command replaces the previous sorting criteria. `sort clear` restores insertion order.
* Sorting changes only the displayed order, not contact details, saved insertion order, or undo/redo history.
  Use `sort clear`, rather than `undo`, to remove sorting. Sorting is reset when the application restarts.
* If the book is empty, the selected sorting still applies to contacts added later. The feedback is
  `No contacts are currently available. The selected sort order will apply to contacts added later.`

**Examples**

Basic use:

```text
sort
```

Result: Displays all contacts in name order.

Browsing by role and then name:

```text
sort r/ n/
```

Result: Groups contacts in ascending role order, then orders contacts with matching roles by name.

Restoring insertion order:

```text
sort clear
```

Result: Displays all contacts in insertion order and reports `Sorting cleared. Contacts will be displayed in insertion order.`

**Common mistakes and recovery**

Problem | What to do
--------|-----------
`sort n/Alice` supplies a value | Use `sort n/`. Sorting accepts field prefixes without values.
`sort n/ n/` repeats a field | Supply each field at most once.
`sort clear r/` combines incompatible modes | Use either `sort clear` or a field-based sort.
Expecting sorting to apply only to search results | Sort first, then run `find` or `followups` again. Those queries retain the selected sorting.

<a id="selecting-a-contact-select-index"></a>

#### Selecting a contact: `select`

Selects a contact from the currently displayed list and shows its full details in the panel on the right.

**Format:** `select INDEX`

**Parameters**

* Supply exactly one `INDEX`: a positive whole number from the [currently displayed list](#command-conventions).
* After `find`, `followups`, or `sort`, use the numbers shown in that view. An index from an earlier view may refer to a different contact.

**Behaviour**

* Highlights the contact, scrolls its card into view, and displays its details. Keyboard focus stays in command input.
* Selecting the same contact again brings its card back into view while keeping the details panel's scroll position.
* Does not change contacts, filters, sorting, or undo/redo history, and preserves available redo history.
* Selection is cleared on restart and after successful `undo` or `redo` commands.
* Invalid input or an empty displayed list leaves the current selection and details unchanged.
* If an automatic save fails after a valid selection, the contact remains selected and the feedback shows the saving error.
  Selection itself does not alter contact data.

**Examples**

Basic use, assuming at least one contact is displayed:

```text
select 1
```

Result: Shows the first displayed contact's full details without changing the current filter or sorting.

Selecting from a search (enter each command separately, and continue only if the search returns a contact):

```text
find r/Supplier
select 1
```

Result: Shows the first displayed supplier's details while keeping the supplier filter active.

Selecting from a sorted list, assuming the contact book is not empty:

```text
sort n/
select 1
```

Result: Shows the first contact in name order.

**Common mistakes and recovery**

Input or condition | Feedback and recovery
-------------------|----------------------
`select` | `INDEX is required.` Supply a displayed index.
`select 0`, `select -1`, `select abc`, or an integer larger than 2147483647 | `INDEX must be a whole number between 1 and 2147483647.` Choose a positive whole number from the displayed list.
`select 1 2` | `select accepts only one INDEX.` Select one contact at a time.
No contacts are displayed | `No contacts are currently displayed.` Use `list` to show all contacts before selecting.
`select 4` when three contacts are displayed | `INDEX 4 is outside the displayed contact list (valid range: 1-3).` Choose one of the displayed numbers.

When exactly one contact is displayed, an out-of-range error states that the only valid index is `1`.

#### Viewing contacts with follow-ups: `followups`

Displays contacts with pending follow-ups, optionally limited by their due dates.

**Format**

Command | Purpose
--------|--------
`followups` | Displays all contacts with a follow-up.
`followups overdue` | Displays contacts with follow-ups due before today.
`followups by/DUE_DATE` | Displays contacts with follow-ups due on or before the specified date.

**Parameters**

* Choose one query mode. `overdue` and `by/` cannot be combined, and contact indexes are not accepted.
* `DUE_DATE` follows the same [date requirements as followup](#managing-a-contacts-follow-up-followup): a valid date in `YYYY-MM-DD` format,
  with a year from `0000` to `9999`. Past cutoffs are accepted.

**Behaviour**

* Searches the entire contact book and replaces any previous search or follow-up filter. Contacts without follow-ups are excluded.
* Retains the current sort order, or insertion order if no sorting is active. Results are numbered from 1;
  use those displayed indexes for subsequent contact commands.
* A follow-up due today is not overdue. `overdue` uses your computer's local date when the command executes.
* Rerun the query after the date changes to refresh date-based results. The interface's automatic badge refresh does not rerun the query.
* Does not change contact details or undo/redo history.
* If no contacts match, displays an empty list and `No follow-ups found.`

**Examples**

Basic use:

```text
followups
```

Result: Displays every contact with a pending follow-up, in the current order.

Checking overdue work:

```text
followups overdue
```

Result: Displays contacts with follow-ups due before today. If there is at least one result, use `select 1` to read the first contact's follow-up.

Checking work due by a particular date:

```text
followups by/2026-11-01
```

Result: Displays contacts with follow-ups due on or before 1 November 2026, including any earlier dates.

**Common mistakes and recovery**

Problem | What to do
--------|-----------
`followups overdue by/2026-11-01` combines modes | Use `followups overdue` or `followups by/2026-11-01` separately.
`followups 1` supplies an index | Use `followups` to query contacts, then `select 1` if that index exists in the results.
Expecting a previous supplier search to remain active | A follow-up query replaces that filter and searches all contacts.
No follow-ups are found | Check the cutoff or use `followups` without a date restriction. Use `list` to return to all contacts.

### Undo and redo

#### Undoing a change: `undo`

Reverses the most recent successful modifying command: `add`, `edit`, `followup`, `delete`, or `clear`.

**Format:** `undo`

**Behaviour**

* Reverses one change at a time. Repeat `undo` to move back through changes made during the current session.
* Undoing `followup` restores the previous follow-up, or removes the newly added follow-up if none existed before.
  Undoing `followup INDEX clear` restores the cleared follow-up.
* A successful undo displays `Undo success!`, shows all contacts, and clears selection. The current sort order is retained.
* Commands such as `find`, `list`, `sort`, `select`, `help`, and the `followups` query do not create undoable contact changes.
* History is cleared when the application closes. Changes from previous sessions cannot be undone.
* The resulting contact data is saved automatically. If saving fails, the undo remains applied in the running application,
  but the saved file is unchanged.

**Examples**

Recovering a deleted contact, assuming contact 1 is displayed (enter each command separately):

```text
delete 1
undo
```

Result: Restores the deleted contact and displays all contacts, with no contact selected.

Reversing a follow-up change, assuming contact 1 is displayed:

```text
followup 1 by/2026-11-01 abt/Confirm quotation
undo
```

Result: Restores the contact's previous follow-up, or removes the new one if none existed before.

**Common mistakes and recovery**

Problem | What to do
--------|-----------
`No more commands to undo!` | There are no earlier changes available in this session. Closing and reopening the application clears history.
`undo 2` supplies an argument | Use `undo` without arguments. To reverse two changes, execute it twice.
Saving fails after undo | Resolve the saving problem reported in the feedback, then run `list` to retry saving the current contact data before closing.

#### Redoing a change: `redo`

Reapplies the most recently undone modifying command.

**Format:** `redo`

**Behaviour**

* Reapplies one undone change at a time. Repeat `redo` while undone changes remain available.
* A successful redo displays `Redo success!`, shows all contacts, and clears selection. The current sort order is retained.
* A new successful modifying command after undo discards the remaining redo history.
* Failed validation and commands such as `find`, `list`, `sort`, `select`, `help`, and the `followups` query preserve redo history.
* History is cleared when the application closes.
* The resulting contact data is saved automatically. If saving fails, the redo remains applied in the running application,
  but the saved file is unchanged.

**Examples**

Reapplying a deletion, assuming contact 1 is displayed (enter each command separately):

```text
delete 1
undo
redo
```

Result: Deletes a contact, restores it, then deletes the same contact again. Successful undo and redo each display all contacts and clear selection.

**Common mistakes and recovery**

Problem | What to do
--------|-----------
`No more commands to redo!` | There is no available undone change. Redo history may have been exhausted, discarded by a new modifying command, or cleared when the application closed.
`redo 2` supplies an argument | Use `redo` without arguments. Execute it once for each change to reapply.
Saving fails after redo | Resolve the saving problem reported in the feedback, then run `list` to retry saving the current contact data before closing.

### Application

<a id="viewing-help-help-command"></a>

#### Viewing help: `help`

Opens the full Help window or displays usage instructions for one command in the result display.

**Format:** `help [COMMAND]`

**Parameters**

* `COMMAND` is optional. Omit it to open the full Help window.
* Supply one supported command name to show its usage: `add`, `edit`, `followup`, `delete`, `clear`, `list`, `find`, `sort`,
  `select`, `followups`, `undo`, `redo`, `help`, or `exit`.

**Behaviour**

* `help` opens the full Help window. `help COMMAND` displays targeted help without opening that window.
* Targeted help for commands without parameters (`clear`, `exit`, `list`, `redo`, and `undo`) contains a brief description.
* Help for commands with required or optional parameters includes a description, syntax, and examples.
  This includes `help`, `sort`, and `followups`, which also work without arguments.
* An unsupported target displays guidance and a list of supported commands instead of opening the Help window.
* Help does not change contact details, filters, sorting, selection, or undo/redo history.

The full Help window uses these categories:

Category | Purpose | Commands
---------|---------|---------
Manage contacts | Add, update, or remove contact data, including follow-ups. | `add`, `edit`, `delete`, `clear`, `followup`
Browse contacts | View, select, filter, or sort contacts. | `list`, `find`, `sort`, `select`, `followups`
Undo and redo | Reverse or restore contact changes. | `undo`, `redo`
Application | View help or exit. | `help`, `exit`

**Examples**

Basic use:

```text
help
```

Result: Opens the full Help window.

Looking up a command:

```text
help add
```

Result: Displays the `add` command's usage, parameters, and an example in the result display.
Similarly, `help select` explains how to select a displayed contact, while `help undo` shows only a brief description.

![Help window showing commands for managing contacts](images/UGHelpWindowPt1.png)

![Help window showing browsing commands, including SELECT](images/UGHelpWindowPt2.png)

![Help window scrolled down to follow-up queries, undo and redo, and application commands](images/UGHelpWindowPt3.png)

**Common mistakes and recovery**

Problem | What to do
--------|-----------
`help unknown` or `help 123` uses an unsupported target | Read the supported command names in the feedback, or use `help` to open the full window.
`help add edit` supplies multiple targets | Request help for one command at a time.
The Help window remains minimised | Restore it manually. See [Known issues](#known-issues).

#### Exiting the program: `exit`

Closes KeyBossWarriorsPro.

**Format:** `exit`

**Behaviour**

* Displays a closing message briefly, then exits without asking for confirmation.
* Closing the application clears undo and redo history. Changes cannot be undone across sessions.

**Examples**

```text
exit
```

Result: Displays the closing message and closes the application.

**Common mistakes and recovery**

Extra text after `exit` is ignored. For example, `exit -f` exits in the same way as `exit`; the closing message explains
that the extra text was ignored.

--------------------------------------------------------------------------------------------------------------------

## Input rules

Refer to this section for accepted field values, duplicate detection, and slash escaping.
Each command explains which fields it accepts and any command-specific requirements.

<a id="contact-input-rules"></a>

**Contact field requirements**

Field | Accepted input
------|---------------
`NAME` | Unicode text, including punctuation, spaces and literal backslashes. Examples: `John A. Tan`, `José Tan`, `王小明`.
`PHONE_NUMBER` | **3–20 ASCII digits** (`0`–`9`). Spaces, `+`, hyphens, brackets and extensions are not accepted.
`EMAIL` | An address following the [supported email syntax](#supported-email-syntax), such as `o'connor@example.com` or `sales@keyboard--shop.com`.
`ADDRESS` | Unicode text, including punctuation, spaces and literal backslashes. Examples: `123 Street, #01-02`, `王 Street 1`.
`ROLE` | Unicode text, including punctuation, spaces and literal backslashes. Examples: `Supplier (PCB)`, `Employee`, `Customer`.
`TAG` | Unicode text, including punctuation, spaces and literal backslashes. Maximum **50 characters after slash decoding, trimming and collapsing repeated ordinary spaces**. Examples: `62.5g`, `PCB v1.6`, `Switches (5-pin)`.

<a id="text-field-rules"></a>

**Text validation and spacing**

Names, roles, tags and addresses must contain at least one character other than whitespace or invisible
formatting characters. After trimming, control characters (such as internal tabs/newlines) and Unicode
line/paragraph separators are rejected. Other Unicode spaces inside text are allowed.

Trimming removes surrounding ordinary spaces, tabs, line breaks and Unicode whitespace such as em spaces.
It does not remove non-breaking spaces or invisible formatting characters such as zero-width spaces;
these remain literal alongside visible text, even at the edges.

Names, roles, and tags also collapse repeated **ordinary spaces** into one: `n/Anne-Marie   O'Connor`
stores `Anne-Marie O'Connor`. Other Unicode spaces are not collapsed. Addresses preserve internal
spacing. All four fields preserve capitalisation; names, roles and addresses have no maximum length.
Duplicate tags are combined using Unicode-aware, case-insensitive comparison, preserving the first spelling.

<a id="character-counting"></a>

Length limits use Java string length: most characters count as one; some, such as emoji, count as two.

**Phone number limitation**

Formatted numbers, extensions and separate mobile/office numbers can be useful, but this release supports
one digits-only number per contact. Support for formatted or multiple numbers is possible future work.
See the [duplicate-contact rules](#contact-duplicates) for how phone numbers are compared.

<a id="supported-email-syntax"></a>

**Supported email syntax**

Email validation uses the [WHATWG HTML email syntax](https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address),
which checks the address's form rather than whether the mailbox exists:

* Supply one non-empty local part, an `@`, and a domain. Internal whitespace is not allowed.
* The local part accepts ASCII letters, digits and these characters: `` .!#$%&'*+/=?^_`{|}~- ``.
  Dots may appear at the start/end or consecutively, as permitted by WHATWG.
* Domain labels are separated by dots. Each label contains **1–63 ASCII letters, digits or hyphens**,
  and must start and end with a letter or digit. Consecutive internal hyphens are allowed.
* Single-label domains, such as `user@localhost`, and one-character domain labels are accepted.
* Quoted local parts, address literals such as `user@[127.0.0.1]`, and raw non-ASCII email characters are unsupported.
  International domain names must already use ASCII/Punycode spelling, such as `sales@xn--bcher-kva.example`.

<a id="contact-duplicates"></a>

**Duplicate contacts**

A new contact is rejected if **either** its phone number **or** its email matches
any existing contact, including contacts hidden by the current filter:

* Phone numbers are compared as exact digit strings, including leading zeroes. Local/international forms are not normalised.
* Email capitalisation is preserved, but comparison ignores case. For example, `John@example.com` and `john@example.com` are treated as the same email.
* Names are not used to detect duplicates. Contacts with identical or similar names can be added if both their phone numbers and emails differ.

<a id="slash-escaping"></a>

<div markdown="block" class="alert alert-warning">

**:warning: Entering `/` as part of a value**

**Write `\/` when text after a space looks like a command prefix but belongs to
your value.** For example, `n/David s/o John` is rejected because `s/` is treated
as an unknown prefix. Write `n/David s\/o John` to store `David s/o John`.
A recognised prefix such as `p/` after a space starts another field; escape its slash
too if it belongs to the value.

Field value entered | Stored value
--------------------|-------------
`n/David s\/o John` | `David s/o John`
`r/Supplier p\/Manufacturer` | `Supplier p/Manufacturer`
`t/parts xx\/custom` | `parts xx/custom`
`a/123 Street c\/o John` | `123 Street c/o John`

A slash within a word does not need escaping: `t/key/board` works as written.
However, `t/ key/board` is rejected because the space before `key/` makes it look
like a prefix; write `t/ key\/board` instead.

**If unsure, escape any slash inside a text value.** Both `n/John\/Tan` and
`n/John/Tan` store `John/Tan`. Keep actual field prefixes such as `n/` unchanged.

* This rule applies to names, roles, tags and addresses in `add`/`edit`, tags removed with `dt/`, name/role/tag searches in `find`, and descriptions in `followup`.
* Each `\/` becomes `/` once, before validation and length checks. The removed backslash does not count towards a length limit. Saving and reloading do not change the decoded value.
* Other backslashes remain literal in all five text fields. For example, `n/John\Tan` and `a/C:\Users\John` are unchanged. To retain a backslash immediately before a slash, enter two: `a/Archive \\/` stores `Archive \/`.
* Phone numbers, email addresses and dates do not use this escape syntax. In emails, enter `/` literally: `e/sales/parts@example.com` works, while `e/sales\/parts@example.com` is rejected because it contains a backslash.

</div>

Example of adding a contact whose name, address, and tag contain prefix-like text:

```text
add n/David s\/o John p/91234567 e/david@example.com a/123 Street c\/o John r/Supplier t/parts xx\/custom
```

Result: Stores the name `David s/o John`, address `123 Street c/o John`, and tag `parts xx/custom`.

--------------------------------------------------------------------------------------------------------------------

## Saving, transferring, and recovering data

### Saving the data

KeyBossWarriorsPro automatically saves data after every command. You do not need to save manually.

Data saves are performed atomically. The application writes updates to a temporary file before replacing the saved data file. This prevents file corruption if the application closes unexpectedly or experiences a disk error during a save. If saving fails, the application leaves your existing data file untouched and displays an error message explaining the failure.

### Editing the data file

KeyBossWarriorsPro data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="block" class="alert alert-warning">

:exclamation: **Caution:**<br>

Saved contacts must satisfy the current [input rules](#contact-input-rules). Older files may contain values that
are now invalid, such as phones exceeding 20 digits, email domain labels exceeding 63 characters, or addresses
with internal tabs. If any contact is invalid, the entire contact book fails to load; other valid contacts
are not loaded separately. The same recovery process applies to invalid manual edits:

* KeyBossWarriorsPro attempts to create a timestamped backup copy in the `data/` folder (for example, `addressbook.json.backup.20261012_123456`).

* A warning message appears in the result box on launch showing the backup file path, or explaining that backup creation failed.

* KeyBossWarriorsPro starts with an empty contact book. A subsequent successful command can save that empty book, so close the application before attempting recovery.

* To restore your contacts, keep a separate copy of the backup, correct both JSON syntax errors and invalid field values, then save the corrected content as `addressbook.json` before relaunching. If backup creation failed, preserve a copy of the original file before editing it.

Certain invalid values can also cause unexpected behavior. Edit the data file only if you are confident in editing JSON formatted files.
</div>


### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous KeyBossWarriorsPro home folder.

**Q**: What should I do if my data file is corrupted?<br>
**A**: When KeyBossWarriorsPro detects invalid saved data on startup, it attempts to create a timestamped backup in the `data/` folder and starts with an empty contact list. Follow the [data-file recovery instructions](#editing-the-data-file), correcting invalid field values as well as JSON syntax before relaunching.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.
3. **Role styling disparity between contact card and details panel**: When a contact is selected, the role is presented as a color-coded pill badge on the contact list card, but appears as unstyled plain text (`Role: <value>`) in the right details panel. This is a cosmetic presentation difference and does not affect contact data, selection behavior, or application functionality. Harmonized pill badge styling across both panes is planned for a future version.
4. **Compact vertical clearance on contact card role badges**: Contact card role pill badges use compact vertical padding and typography, which may cause letter descenders (e.g., `g`, `p`, `y`) or uppercase ascenders to appear vertically snug against the pill border on certain display scalings. All badge text remains fully readable, and contact management is unaffected. Relaxed vertical padding and refined typography are planned for a future version.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS r/ROLE [t/TAG]…​` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 r/Supplier t/friend t/colleague`
**Clear** | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [r/ROLE] [t/TAG_TO_ADD]…​ [dt/TAG_TO_DELETE]…​`<br> e.g., `edit 1 t/solderer dt/trainee` (requires contact 1 to have `trainee`)
**Exit** | `exit` 
**Find** | `find [n/NAME_PHRASE]... [r/ROLE_PHRASE]... [t/TAG_PHRASE]...`<br>or `find KEYWORD [MORE_KEYWORDS]` (legacy search)<br>e.g., `find n/John r/Supplier`, `find James Jake`
**Help** | `help [COMMAND]`<br>e.g., `help add` displays the `add` command's usage in the result display.<br>`help` opens the full help window.
**List** | `list`
**Select** | `select INDEX`<br> e.g., `select 2` displays the second currently listed contact's details.
**Redo** | `redo`
**Undo** | `undo`
**Follow-up** | `followup INDEX by/DUE_DATE abt/DESCRIPTION` or `followup INDEX clear`<br> e.g., `followup 1 by/2026-11-01 abt/Confirm quotation`
**Follow-up queries** | `followups`, `followups overdue`, or `followups by/DUE_DATE`<br> e.g., `followups by/2026-11-01`
**Sort** | `sort [n/] [r/]`
