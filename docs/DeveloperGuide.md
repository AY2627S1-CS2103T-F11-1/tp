---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* Based on [AddressBook-Level 3](https://github.com/se-edu/addressbook-level3) by the [SE-EDU initiative](https://se-education.org), adapting its code, architecture, and documentation.
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), and [JUnit 5](https://junit.org/junit5/).
* `Email.VALIDATION_REGEX` adapts the regex published in the
  [WHATWG HTML Standard](https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address).
* AI assistance: OpenAI Codex, Anthropic Claude, and Google Gemini were used during development and documentation.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-F11-1/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-F11-1/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F11-1/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, `PersonDetailsPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-F11-1/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

#### Contact details and selection

`MainWindow` displays `PersonListPanel` and `PersonDetailsPanel` side by side. Each `PersonCard` provides a compact contact summary, while the details panel displays the selected contact’s complete information.

The contact interface:

* observes `PersonListPanel#selectedPersonProperty()` to update the details panel when selection changes.
* uses `CommandSelectionChange` to preserve selection across edits and sorting, and clears selection when the contact is removed, filtered out, or after undo/redo.
* supports mouse selection, arrow-key navigation, and [`select INDEX`](#selecting-contacts-with-select-index). Escape returns focus to command input while preserving the draft.
* uses a shared `UiDateTracker` to refresh follow-up urgency indicators when the local date changes.
* supports independent scrolling and wrapping content so full contact details remain accessible.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F11-1/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the successful path through the `Logic` component
for `execute("delete 1")`, assuming displayed index 1 exists. Validation branches and
storage interactions are omitted; see [Deleting a contact](#deleting-a-contact) for those details.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The command returns a `CommandResult` to `LogicManager`. The manager records selection-change metadata and requests a save through `Storage` before returning a successful result to the UI.
   Parsing or command-validation failures skip the save step. Saving failures are reported as `CommandException`;
   their effects on contact data and history are described under [Persistence and failures](#persistence-and-failures).

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* Command parsers such as `AddCommandParser`, `DeleteCommandParser`, and `ExitCommandParser` implement `Parser<T>`.
  Optional `help` arguments are parsed inside `AddressBookParser`. These paths return a `Command` for execution by `LogicManager`.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F11-1/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* owns a `VersionedAddressBook`, which extends `AddressBook` with session history. Its `UniquePersonList` contains immutable `Person` objects.
  Each contact contains a `Name`, `Phone`, `Email`, `Address`, custom `Role`, a set of `Tag` objects, and an optional `FollowUp`.
  The history snapshots and pointer remain in memory; storage serializes the current contact data.
* maintains a `FilteredList<Person>` backed by the address book's contact list, and a `SortedList<Person>` wrapping that filtered view. The `getFilteredPersonList()` API exposes the filtered and sorted displayed list as an unmodifiable `ObservableList<Person>`. The UI and index-based commands use the same displayed ordering, while the underlying contact order remains unchanged.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F11-1/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* can create timestamped backup copies of existing invalid contact-data and preference files during startup recovery.
  See [Startup loading and recovery](#startup-loading-and-recovery) for missing-file and failed-backup behaviour.
* writes through a temporary sibling file using `FileUtil#writeToFile`. Replacement requests an atomic move, with a non-atomic fallback when unsupported;
  see [Atomic file persistence](#atomic-file-persistence) for the guarantee boundary.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`).

The sequence diagram below shows how the application recovers when a corrupted data file is encountered at startup:

![BackupCorruptedFileSequenceDiagram](images/BackupCorruptedFileSequenceDiagram.png)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes the v1.6 implementation in the project working directory, ahead of its GitHub release.
Command entries follow the User Guide order. See [Shared mechanisms](#shared-mechanisms) for behaviour used across features.

### Manage contacts

#### Adding a contact: `add`

**Overview**

<a id="adding-a-contact"></a>

KeyBossWarriorsPro allows keyboard shop managers to add new contacts with an industry-specific `Role` (e.g., `Supplier`, `Employee`, `Partner`, `Customer`) and optional tags. Contact uniqueness is governed by communication identifiers (phone number and email address) rather than names.

See the [UG add entry](UserGuide.md#adding-a-contact-add) for syntax and examples.

**Relevant classes and data**

`AddCommandParser` constructs validated value objects and a `Person`; `AddCommand` uses the `Model` API to check uniqueness, insert the contact, and commit history.

**Parsing and validation**

Required prefixes are `n/`, `p/`, `e/`, `a/`, and `r/`, each supplied once. Tags may repeat. The parser rejects unknown prefixes and unexpected preamble text, then validates field values. See the shared [text rules](#contact-text-validation-formatting-and-searching) and [email and phone rules](#email-and-phone-validation).

**Execution and failure handling**

<a id="implementation-details"></a>

The sequence diagram below illustrates how an `add` command travels through `Logic` and `Model` components when a user executes `add n/John Doe p/98765432 ... r/Supplier`:

![AddSequenceDiagram](images/AddSequenceDiagram.png)

How the command parsing and execution works:

1. `LogicManager#execute()` calls `AddressBookParser#parseCommand()`.
2. `AddressBookParser` instantiates an `AddCommandParser` and invokes its `parse()` method.
3. `AddCommandParser` performs multi-tier input validation:
   * `ArgumentTokenizer#verifyNoUnrecognizedPrefixes()` ensures no unrecognized prefixes are supplied.
   * `ArgumentMultimap#verifyNoMissingPrefixesFor()` verifies that all compulsory prefixes (`n/`, `p/`, `e/`, `a/`, `r/`) are present.
   * Checks that the preamble is empty.
   * `ArgumentMultimap#verifyNoDuplicatePrefixesFor()` rejects repeated compulsory prefixes.
   * Parses individual fields into domain value objects (`Name`, `Phone`, `Email`, `Address`, `Role`, and a set of `Tag` objects) via `ParserUtil`.
4. A new `Person` entity `toAdd` is constructed, and `AddCommandParser` returns an `AddCommand` object (labeled `command`). The parser instance is no longer needed after parsing. The diagram's destroy marker (`X`) represents the end of its role in this interaction; Java garbage collection does not have a specified immediate destruction time.
5. `LogicManager` executes `AddCommand#execute(Model)`.
6. `AddCommand` queries `Model` to verify uniqueness:
   * `Model#hasPhone(toAdd.getPhone())`
   * `Model#hasEmail(toAdd.getEmail())`
7. If uniqueness validation passes, `Model#addPerson(toAdd)` inserts the contact into `UniquePersonList`, and `Model#commitAddressBook()` saves the state snapshot into `VersionedAddressBook` to support undo/redo.
8. A `CommandResult` containing success feedback (labeled `result`) is returned to `LogicManager`.

<a id="execution-flow"></a>

The activity diagram below illustrates the duplicate resolution and defensive error-handling branching during `AddCommand#execute()`. Each decision node specifies explicit, mutually exclusive guard conditions on all outgoing branches, and all execution paths converge cleanly to a single final node:

![AddCommandActivityDiagram](images/AddCommandActivityDiagram.png)

`ModelManager#addPerson()` resets the filter to all contacts and retains sorting. `LogicManager` saves after execution; committed additions are rolled back if saving fails. See [Persistence and failures](#persistence-and-failures).

**Design considerations**

<a id="design-considerations"></a>

**Aspect: Contact identity and duplicate criteria**

* **Alternative 1 (current choice):** Identity uniqueness enforced independently on phone number and email address.
  * Pros: Matches realistic keyboard shop management where multiple contacts may share identical names (e.g., common customer names), but communication channels must remain unique. Provides targeted error feedback indicating whether the phone, email, or both clashed.
  * Cons: Requires multiple uniqueness checks against `UniquePersonList` before insertion.
* **Alternative 2:** Identity determined solely by contact name (baseline AB3).
  * Pros: Simpler lookup logic.
  * Cons: Disallows contacts sharing a name and allows duplicate phone numbers and email addresses to enter the pipeline undetected.

**Aspect: Phone number representation and future work**

The current release stores one `Phone` per contact and uses exact-string equality for the phone part of
duplicate detection. This simplifies comparison but excludes useful formats and multiple contact numbers.
Simply accepting separators would let `91234567` and `9123 4567` bypass a phone match.
Future support should define canonical comparison values, country-code and extension handling while
preserving display formatting. Multiple numbers would also require model, command and storage changes,
with duplicate checks across all numbers. These enhancements are deferred beyond the current tP release.

**Aspect: Input prefix validation**

* **Alternative 1 (current choice):** Strict rejection of unrecognized prefixes via `ArgumentTokenizer#verifyNoUnrecognizedPrefixes()`.
  * Pros: Prevents user typos from failing silently (e.g., mistyping `r/Supplier` as `w/Supplier` or supplying extra unsupported prefixes).
  * Cons: Requires maintaining explicit prefix whitelists for each command parser.
* **Alternative 2:** Permissive parsing where unrecognized prefixes are treated as part of preamble or ignored.
  * Pros: Simpler parsing code.
  * Cons: Causes silent input drops where mistyped fields are omitted without warning the user.

**Testing references**

`AddCommandParserTest` covers prefix and field validation; `AddCommandTest` and `AddCommandIntegrationTest` cover insertion and duplicate identifiers. `LogicManagerTest` covers persistence and rollback. See the [manual add tests](#adding-contacts-with-add).

#### Editing a contact: `edit`

**Overview**

<a id="adding-and-deleting-contact-tags"></a>

The `edit` command supports incremental tag changes using `edit INDEX [t/TAG_TO_ADD]... [dt/TAG_TO_DELETE]...`, alongside the existing contact fields. Additions preserve existing tags, and deletions remove only explicitly named tags. Parsing and command-validation failures leave all contact details and the displayed view unchanged. A successful edit displays all contacts;
if its subsequent save fails, contact data is rolled back and the restored view also displays all contacts, as described under [Persistence and failures](#persistence-and-failures).

The same command edits names, phone numbers, emails, addresses, and roles. See the [UG edit entry](UserGuide.md#editing-a-contact-edit).

**Relevant classes and data**

`EditCommandParser`, `EditCommand.EditPersonDescriptor`, and `EditCommand` separate supplied changes from the original immutable `Person`. Optional scalar fields and independent tag-operation sets describe the requested edit.

**Parsing and validation**

<a id="parsing-and-descriptor"></a>

`CliSyntax` defines `PREFIX_TAG` (`t/`) and `PREFIX_DELETE_TAG` (`dt/`). `EditCommandParser` recognizes both prefixes and permits repeated occurrences of each.

`EditCommand.EditPersonDescriptor` stores `tagsToAdd` and `tagsToDelete` separately. Each setter makes a defensive copy, and each getter returns an `Optional<Set<Tag>>` containing an unmodifiable set. An absent optional means that operation was not supplied. Both sets participate in the copy constructor, `isAnyFieldEdited()`, `equals()`, and `toString()`.

Before tokenizing the arguments, `EditCommandParser` calls
`ArgumentTokenizer#verifyNoUnrecognizedPrefixes()` with all supported edit
prefixes (`n/`, `p/`, `e/`, `a/`, `r/`, `t/`, and `dt/`). This prevents unknown
prefixes from being silently ignored.

The parser requires a displayed index, rejects duplicate scalar prefixes, and parses each supplied name, phone, email, address, or role into the descriptor. Omitted fields stay absent.

After parsing these fields, `EditCommandParser`:

1. Parses additions, then deletions, using `parseTagsForEdit()`. An absent prefix produces an empty optional. A supplied blank tag is rejected, including bare `t/` and `dt/`; neither clears all tags.
2. Uses `ParserUtil.parseTags()` to validate tag names and construct sets. `Tag` collapses repeated ordinary spaces, and its equality and hash code support case-insensitive comparison and deduplication.
3. Rejects any tag present in both sets. If several tags conflict, the first tag alphabetically, ignoring case, is reported using the spelling from the additions set.
4. Rejects a command with no fields or tag operations, or returns an `EditCommand` containing a copy of the descriptor.

Invalid tag values are rejected before add/delete conflicts. Missing deletions require the selected contact's current data and are checked during execution.

**Execution and failure handling**

<a id="execution-and-validation"></a>

`EditCommand#execute()` resolves the index against the currently displayed list. `createEditedPerson()` checks every requested deletion against the original contact's tags before constructing the edited contact. If any requested tag is absent, it throws a `CommandException`; if several are absent, the first alphabetically, ignoring case, is reported.

For valid deletions, `createEditedPerson()` copies the original tag set into a new `HashSet`, removes `tagsToDelete`, adds `tagsToAdd`, and constructs a new `Person` using the supplied or existing contact details. Adding an existing tag succeeds without creating a duplicate. Removing every tag requires explicitly listing every tag for deletion.

`execute()` then performs the existing duplicate phone and email checks. Only after all validation succeeds does it call `Model#setPerson()` and reset the displayed list with `PREDICATE_SHOW_ALL_PERSONS`. A validation failure therefore changes neither the original contact nor the displayed list, including when a command combines valid deletions, a missing deletion, additions, and other field edits.

Condition | Error constant | Exception
--------|------------------|----------
Blank value after `t/` or `dt/` | `EditCommand.MESSAGE_EMPTY_TAG` | `ParseException`
Invalid tag name | `Tag.MESSAGE_CONSTRAINTS` | `ParseException`
Same tag in both operations | `EditCommand.MESSAGE_TAG_CONFLICT` | `ParseException`
Requested deletion absent from the selected contact | `EditCommand.MESSAGE_TAG_NOT_FOUND` | `CommandException`

Supplied scalar fields replace existing values; omitted fields and the optional follow-up are preserved. Duplicate checks exclude the contact being edited. The valid replacement is committed once and saved through `LogicManager`. A storage failure follows [Persistence and failures](#persistence-and-failures).

**Design considerations**

Validate requested deletions and duplicate communication identifiers before replacing the original contact. This makes combined field and tag edits succeed as one change. The descriptor represents omitted fields explicitly, so ordinary field edits preserve existing tags and follow-ups.

**Testing references**

<a id="test-coverage-2"></a>

* `EditPersonDescriptorTest` covers independent tag operations, defensive copying, unmodifiable getters, equality, and string output.
* `EditCommandParserTest` covers recognised and unrecognised prefixes, repeated and empty values, invalid names, case-insensitive conflicts, and validation order.
* `EditCommandTest` covers additions, selective deletions, combined operations, filtered lists, existing tags, missing deletions, and preservation of the full model on failure. It also parses command text through `AddressBookParser` before execution to cover the acceptance examples and failure cases.
* `AddressBookParserTest` checks that an edit containing both operations is routed to the edit parser.

#### Managing a contact's follow-up: `followup`

**Overview**

<a id="contact-follow-up-management"></a>

Each `Person` owns an `Optional<FollowUp>`. A contact can have zero or one follow-up.

See the [UG followup entry](UserGuide.md#managing-a-contacts-follow-up-followup) for setting and clearing syntax.

**Relevant classes and data**

<a id="model-and-persistence"></a>

`FollowUp` stores an immutable `LocalDate` due date and description. Both fields are mandatory when a follow-up exists.
* Due dates accept past values and restrict years to `0000`–`9999`.
* Descriptions use `strip()` and `StringUtil.isValidText()` with the [shared text rules](#contact-text-validation-formatting-and-searching), preserving capitalisation and internal spacing. The limit is 200 UTF-16 code units (`String.length()`) after slash decoding and trimming.
* Equality and hashing include both fields, with case-sensitive description comparison.
  `FollowUp` has no back-reference to `Person`. The owning contact establishes the association, so changing contact details does not require updating a separate contact identifier.

`Person.equals()` and `hashCode()` include the optional follow-up. `isSamePerson()` retains its existing phone/email identity rules. Contact edits preserve the follow-up, while deleting a contact removes it.

`JsonAdaptedFollowUp` converts the nested JSON representation to and from the model. `JsonAdaptedPerson` treats missing or null follow-ups as absent, preserving compatibility with older files. A present follow-up with missing or invalid fields is rejected through the existing invalid-data handling mechanism.

**Parsing and validation**

`FollowUpCommandParser` recognises either `INDEX clear` or an index with exactly one `by/` and one `abt/` value. It rejects mixed forms, unknown or repeated prefixes, invalid dates, and invalid descriptions. Command execution checks the index against the displayed list; clearing also requires an existing follow-up.

**Execution and failure handling**

<a id="setting-and-clearing-follow-ups"></a>

`AddressBookParser` routes `followup` to `FollowUpCommandParser`, which returns one of two commands:
* `SetFollowUpCommand` creates or replaces a follow-up using a displayed contact index, due date, and description.
* `ClearFollowUpCommand` removes the selected contact's follow-up and rejects contacts without one.

The abstract `FollowUpCommand` holds the shared command word and usage message.

The parser uses the existing tokenizer and prefix-validation utilities. Both set fields are mandatory, duplicate and unknown prefixes are rejected, and clear syntax cannot be combined with set parameters. `ParserUtil.parseFollowUpDate()` enforces the exact four-digit-year format and rejects impossible calendar dates.

Each modifying command resolves the index against the currently displayed list, constructs a replacement `Person`, calls `Model#setPerson()`, and commits once to undo history. Other contact fields and the current filter are preserved. Failed validation does not modify data or history. Automatic persistence uses the existing logic/storage flow.

The sequence diagram below illustrates how a `followup` command travels through `Logic` and `Model` components when a user executes `followup 1 by/2026-11-01 abt/Confirm quotation`:

![FollowUpSequenceDiagram](images/FollowUpSequenceDiagram.png)

How the command parsing and execution works:

1. `LogicManager#execute()` calls `AddressBookParser#parseCommand()`.
2. `AddressBookParser` instantiates a `FollowUpCommandParser` and invokes its `parse()` method.
3. `FollowUpCommandParser` performs validation:
   * `ArgumentTokenizer#verifyNoUnrecognizedPrefixes()` ensures no unrecognized prefixes are supplied.
   * `ParserUtil#parseIndex()` parses the target contact index.
   * `ArgumentMultimap#verifyNoMissingPrefixesFor()` verifies that both `by/` and `abt/` prefixes are present.
   * `ArgumentMultimap#verifyNoDuplicatePrefixesFor()` rejects repeated prefixes.
   * `ParserUtil#parseFollowUpDate()` parses the due date into a `LocalDate`.
   * `ParserUtil#unescapeSlashes()` and `FollowUp#isValidDescription()` validate the description string.
4. A `SetFollowUpCommand` object (labeled `f`) containing the parsed index and new `FollowUp` is returned. The parser is no longer needed after parsing; the destroy marker (`X`) marks its role ending in the illustrated interaction rather than a guaranteed Java garbage-collection event.
5. `LogicManager` executes `SetFollowUpCommand#execute(Model)`:
   * The command retrieves `personToUpdate` from the filtered list using the zero-based index.
   * `createPersonWithFollowUp()` constructs a replacement `Person` entity `updatedPerson` containing the new `FollowUp`.
   * `Model#setPerson(personToUpdate, updatedPerson)` updates the contact in the address book.
   * `Model#commitAddressBook()` saves the state to history for undo/redo support.
6. A `CommandResult` object (labeled `r`) containing the success feedback is created and returned to `LogicManager`.

Each valid change preserves the current comparator as well as the filter. Changing or clearing a follow-up can remove its contact from an active query, which also clears its selection. See [Follow-up urgency and details](#follow-up-urgency-and-details) and [Persistence and failures](#persistence-and-failures).

**Design considerations**

Store the follow-up inside its owning contact so replacement, deletion, persistence, and snapshots include it. The two concrete commands share `FollowUpCommand` usage constants but have distinct validation and execution responsibilities.

**Testing references**

<a id="test-coverage-3"></a>

* Model tests cover validation, normalization, equality, and contact identity.
* Storage tests cover JSON round trips, older records, and malformed nested follow-ups.
* Command tests cover creation, replacement, removal, filtered indexes, failure preservation, and undo/redo.
* Query tests use fixed clocks to verify date boundaries, time zones, filtering, and history preservation.
* Parser tests cover supported forms, invalid arguments, shared date parsing, and command routing.

#### Deleting a contact: `delete`

**Overview**

<a id="deleting-a-contact"></a>

`delete` removes one contact identified by its position in the current displayed list.
The [User Guide](UserGuide.md#deleting-a-contact-delete) describes its syntax and user-facing behaviour.
The [sequence diagram in the Logic component](#logic-component) shows the successful execution path.

**Relevant classes and data**

`DeleteCommandParser` produces an `Index`; `DeleteCommand` resolves it through `Model#getFilteredPersonList()`, then removes the `Person` from the underlying contact book.

**Parsing and validation**

<a id="parsing-and-validation-1"></a>

`AddressBookParser` delegates the arguments to `DeleteCommandParser#parse()`, which:

1. Trims the arguments and rejects empty input with `INDEX is required.`
2. Splits the trimmed input on whitespace and rejects multiple arguments with
   `delete accepts only one INDEX.`
3. Calls `ParserUtil#parseIndex()` to obtain an `Index`. The helper accepts integers from
   1 to `Integer.MAX_VALUE` and rejects zero, negative, nonnumeric, and overflowing values
   with `INDEX must be a whole number between 1 and 2147483647.`

These failures raise `ParseException` before execution. Parsing checks the numeric format
and supported range; whether the index refers to a displayed contact is checked by the command.

**Execution and failure handling**

<a id="execution-and-model-integration"></a>

`DeleteCommand#execute()`:

1. Retrieves `Model#getFilteredPersonList()`, which exposes the filtered and sorted displayed view.
2. Rejects an empty displayed list with `No contacts are currently displayed.`
3. Rejects an index beyond the displayed list using `Messages#getIndexOutOfRangeMessage()`.
   The feedback names the valid range, or the sole valid index when one contact is displayed.
4. Resolves the `Person` at the index, calls `Model#deletePerson()`, then calls
   `Model#commitAddressBook()` once to record the deletion.
5. Returns a `CommandResult` containing `Deleted contact: ...`, using `Messages#format()`
   to include the deleted contact's details and any follow-up.

The empty-list and out-of-range checks raise `CommandException`. All argument and index
validation failures preserve contact data, the displayed view, and undo/redo history.

The command removes the selected `Person` from the underlying contact list. It does not
reset the filter or comparator, so the remaining displayed contacts retain the active
search criteria and sort order. Displayed indices are recalculated automatically.
Deleting the contact currently selected in the UI clears its details; deleting another
contact preserves the selection, as described under [Contact details and selection](#contact-details-and-selection).

<a id="persistence-history-and-design-considerations"></a>

`LogicManager` saves the underlying address book after successful execution. If saving
fails, it rolls back the committed deletion, restores the contact data, and reports a
saving error. The restored view shows all contacts. The shared
[persistence and failure handling](#persistence-and-failures) section describes this mechanism.

**Design considerations**

* **Resolve against the displayed view:** Using the same view as the UI ensures that
  filtered and sorted indices identify the contact the user sees, rather than its stored position.
* **Commit after validation:** Rejected requests create no history entry. A successful deletion
  creates one undoable change and discards any remaining redo history. Undo restores the entire
  contact, including its follow-up; redo removes it again. Both operations show all contacts
  while retaining the active sort order.

**Testing references**

<a id="test-coverage-1"></a>

`DeleteCommandParserTest` covers missing, extra, and invalid arguments. `ParserUtilTest`
checks numeric boundaries and overflow. `DeleteCommandTest` covers empty and filtered lists,
out-of-range indices, follow-up restoration through undo/redo, and redo preservation after
failed validation. `LogicManagerTest` covers sorted deletion, history, and saving failures;
`MainWindowTest` covers selection updates after deletion.
The [manual delete tests](#deleting-contacts-with-delete) provide reproducible acceptance cases.

#### Clearing all contacts: `clear`

**Overview**

Removes every stored contact, including contacts hidden by a filter. See the [UG clear entry](UserGuide.md#clearing-all-contacts-clear).

**Relevant classes and data**

`ClearCommandParser` records whether extra arguments were supplied. `ClearCommand` replaces the model data with an empty `AddressBook`.

**Parsing and validation**

Extra non-blank text is recorded rather than rejected; it does not limit the deletion scope.

**Execution and failure handling**

If the underlying book is empty, return empty-book feedback without committing. Otherwise call `Model#setAddressBook(new AddressBook())`, commit once, and return success feedback with an ignored-arguments warning when needed. The view becomes empty and selection clears. A failed save rolls back the committed clear through the shared persistence path.

**Design considerations**

Check the underlying book rather than the displayed list so a filter never changes the scope of clearing. Treat an already-empty book as a no-op that preserves redo history.

**Testing references**

`ClearCommandParserTest`, `ClearCommandTest`, `UndoRedoIntegrationTest`, and `LogicManagerTest` cover extra arguments, empty-book behaviour, history, and rollback.

### Browse contacts

#### Listing all contacts: `list`

**Overview**

Restores the full displayed contact list. See the [UG list entry](UserGuide.md#listing-all-contacts-list).

**Relevant classes and data**

`ListCommandParser` records extra arguments; `ListCommand` changes the predicate through `Model#updateFilteredPersonList()`.

**Parsing and validation**

Whitespace-only arguments are treated as absent. Other extra text is accepted and produces a warning.

**Execution and failure handling**

Apply `PREDICATE_SHOW_ALL_PERSONS`, retain the sort comparator, and return normal or empty-book feedback based on the underlying contact count. Contact data and snapshot history are unchanged. `LogicManager` still attempts to save the current contact book after successful execution.

**Design considerations**

Reuse the filtered/sorted view pipeline rather than creating a separate list. See [Displayed contact views](#displayed-contact-views).

**Testing references**

`ListCommandParserTest`, `ListCommandTest`, and `LogicManagerTest` cover extra arguments, filtered views, empty books, and sorting preservation.

#### Locating contacts: `find`

**Overview**

<a id="finding-contacts"></a>

`find` filters the displayed contacts using either prefixed name, role, and tag phrases
or quick name search keywords. The [User Guide](UserGuide.md#locating-contacts-find)
describes both command formats and their user-facing behaviour.

**Relevant classes and data**

`FindCommandParser` constructs field-specific `Predicate<Person>` objects. `FindCommand` combines them and delegates view changes to the model; `Messages` formats the result count.

**Parsing and validation**

<a id="parsing-and-validation"></a>

`AddressBookParser#parseCommand()` delegates the arguments to `FindCommandParser#parse()`.
It uses the shared parser classes shown in the [Logic component](#logic-component) and
processes input in this order:

1. Reject empty or whitespace-only arguments, then reject prefixes other than `n/`, `r/`, and `t/`
   using `ArgumentTokenizer#verifyNoUnrecognizedPrefixes()`.
2. Tokenize the supported prefixes. `ArgumentMultimap#getAllValues()` retains every value
   for a repeated prefix, and tokenization trims leading and trailing whitespace.
   `normaliseSearchPhrases()` collapses repeated ordinary spaces within each value without
   splitting a phrase into separate keywords.
3. If any supported prefix is present, reject any blank value, then reject a nonempty
   preamble (unprefixed keywords before the first prefix). Create one predicate for each
   supplied field, containing all its search phrases.
4. Otherwise, if no supported prefixes are present, default to performing a quick name search: split the trimmed arguments on whitespace and
   create one `NameContainsKeywordsPredicate` containing the resulting keywords.

Invalid input raises a `ParseException` with a specific reason before a command is executed,
so the current filter and contact data are preserved. Prefix recognition follows the
shared syntax described under [Name, role, and tag formatting](#name-role-and-tag-formatting).

**Execution and failure handling**

<a id="predicate-composition-and-execution"></a>

`NameContainsKeywordsPredicate`, `RoleContainsKeywordsPredicate`, and
`TagContainsKeywordsPredicate` implement `Predicate<Person>`. They apply `StringUtil.normalizeCase()`
to field values and search criteria before using `String#contains()`. The helper maps each Unicode code point through
`Character.toUpperCase()` and then `Character.toLowerCase()`, supporting case equivalents such as `σ` and `ς`
without changing stored spelling or removing accents. The shared [text-processing explanation](#contact-text-validation-formatting-and-searching) describes validation and spacing.
Each predicate uses `anyMatch()` to implement OR within its field. The tag predicate
matches when at least one tag contains at least one supplied phrase; a contact with
no tags cannot satisfy a tag criterion.

`FindCommand#execute()` combines the field predicates using `allMatch()`, implementing
AND across fields. For example, `find n/ali n/bob r/supp` creates a name predicate for
`ali` OR `bob` and a role predicate for `supp`; both predicates must match the contact.
A contact appears once even when several criteria match it.

The command passes this combined predicate to `Model#updateFilteredPersonList()`.
`ModelManager` replaces the predicate on its `FilteredList<Person>`, which is backed by
the full contact list. Each search therefore replaces the previous filter. The wrapping
`SortedList<Person>` retains its comparator, so the selected sort order still applies.
The [model view diagram](#model-integration-persistence-and-history) shows these source
relationships. `Messages#getContactsFoundMessage()` uses the displayed list's size to
produce singular, plural, or `No contacts found.` feedback in a `CommandResult`.

Search changes only the displayed view and does not call `commitAddressBook()`, so it
neither creates an undo snapshot nor discards redo history. `LogicManager` still saves
the underlying address book after successful execution; the search filter is not serialized.

The activity diagram below summarises validation, both parsing paths, and filter execution.
It focuses on `FindCommandParser` and `FindCommand`.

<!-- Render diagrams/FindActivityDiagram.puml to images/FindActivityDiagram.png. -->
![Find command activity diagram](images/FindActivityDiagram.png)

The [find command sequence diagram](diagrams/FindSequenceDiagram.puml) shows the parsing
and execution of `find n/Alice n/Bob r/Supplier`. It focuses on search-specific interactions;
shared `LogicManager` bookkeeping and storage calls are omitted. Predicate creation is
summarised in a note: both `n/` values belong to one name predicate, while `r/` produces
a separate role predicate. Quick name search follows the same execution flow, using one
`NameContainsKeywordsPredicate` containing all whitespace-separated keywords.

<!-- Render diagrams/FindSequenceDiagram.puml to images/FindSequenceDiagram.png. -->
![Find command sequence diagram](images/FindSequenceDiagram.png)

**Design considerations**

<a id="design-considerations-1"></a>

* **Separate field predicates:** Keeping OR matching inside each predicate and AND matching
  in `FindCommand` allows alternatives within a field while narrowing across fields. Both
  search formats reuse the name predicate.
* **Accept partial phrases:** Search criteria do not pass through `ParserUtil`'s whole-field
  validation. This allows useful substrings, including partial phrases containing punctuation,
  while the parser still enforces command syntax and nonblank values.

**Testing references**

<a id="test-coverage"></a>

`FindCommandParserTest` covers both formats, multiword phrases, repeated prefixes and spaces,
and the missing-criteria, blank-value, mixed-format, and unsupported-prefix errors.
The three `*ContainsKeywordsPredicateTest` classes cover field-specific substring and case
matching. `FindCommandTest` checks combined predicates and overlapping matches.
`LogicManagerTest` covers adding and searching normalised fields, OR/AND composition,
restoring all contacts with `list`, and preserving sorting across searches.
The [manual find tests](#finding-contacts-with-find) provide a reproducible dataset and
expected results for these user-visible behaviours.

#### Sorting all contacts: `sort`

**Overview**

<a id="sorting-contacts"></a>

The sort feature changes the displayed contact order without rearranging the address book's underlying contact list.
It supports case-insensitive, ascending ordering by name, role, or both, with field order determining priority.

See the [UG sort entry](UserGuide.md#sorting-all-contacts-sort), including `sort clear`.

**Relevant classes and data**

`SortCommandParser` selects an ordered list of `SortField` values or the clearing mode. `SortCommand` builds comparators and changes the model view; [Displayed contact views](#displayed-contact-views) describes the source relationships.

**Parsing and validation**

<a id="parsing-and-validation-2"></a>

`AddressBookParser` delegates sort arguments to `SortCommandParser`. Empty or
whitespace-only arguments default to `SortField.NAME`. Otherwise, the parser
accepts standalone `n/` and `r/` tokens in the supplied priority order, or the
standalone `clear` argument. Duplicate fields, values attached to prefixes,
unsupported tokens, and combinations such as `clear n/` produce specific
`ParseException` messages before any model state changes.

`SortCommand` independently requires one or two distinct, non-null fields and makes an immutable defensive copy of
the supplied list. This preserves field priority even if the caller later modifies its original list.

**Execution and failure handling**

For field-based sorting, the following comparator path applies.

<a id="comparator-construction-and-execution"></a>

`SortCommand#execute()`:

1. Creates a comparator for the first field using `Comparator.comparing()` with `String.CASE_INSENSITIVE_ORDER`.
2. Chains any subsequent field using `thenComparing()`, so it breaks ties in the preceding field.
3. Clears the active filter using `Model#updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS)`.
4. Applies the comparator using `Model#updateSortedPersonList()`.
5. Returns a `CommandResult`, selecting empty-address-book feedback when the underlying contact list is empty.

The empty-address-book check affects feedback only: sorting is still applied so
contacts added afterwards follow the selected order. The user receives an
informational message instead of an error. An empty search result does not
trigger this feedback if the underlying address book contains contacts.
Executing another sort command replaces the previous comparator rather than
adding to the previous sorting criteria.

`sort clear` takes an earlier branch: it clears the filter, sets the comparator to `null`, and returns clearing feedback. Both modes show all contacts and retain stored insertion order. Sorting is a view change and creates no snapshot; the comparator is not persisted.

**Design considerations**

<a id="design-considerations-2"></a>

**Aspect: Sorting a displayed view rather than the stored list**

* **Current choice:** Wrap the filtered list in a `SortedList`. This separates presentation order from stored data
  and undo history, keeps displayed indices consistent, and automatically maintains ordering after contact changes.
* **Alternative:** Reorder the underlying contact list. This would change the order saved to storage and require
  additional handling to separate sorting from contact-data history and maintain ordering after later changes.

**Aspect: Sorting all contacts rather than only search results**

Sort explicitly resets the filter so it provides a way to browse the complete address book in a chosen order.
Filtering remains independent: users can run find afterwards to narrow the sorted view without losing its comparator.

**Testing references**

<a id="test-coverage-4"></a>

* `SortCommandParserTest` covers default sorting, field priority, whitespace,
  `clear`, invalid arguments, and invalid combinations of `clear` with sorting
  fields.
* `SortCommandTest` covers constructor validation, defensive copying, comparator
  priority, case handling, empty and single-contact lists, filter clearing,
  replacement sorting, clearing the sort, restoring insertion order, and sorting
  contacts added after an empty-book sort.
* `ModelManagerTest` covers displayed ordering, chained comparators, filtering, live add/edit/delete updates through
  an existing list reference, and model equality.
* `AddressBookParserTest` checks command dispatch to the sort parser.
* `LogicManagerTest` exercises complete command sequences involving displayed indices, filtering, editing,
  deletion, undo/redo, invalid sorts, equal-key contact identities, and preservation of saved contact order.

#### Selecting a contact: `select`

**Overview**

<a id="selecting-contacts-with-select-index"></a>

Selects a contact by its displayed index and requests its full details. See the [UG select entry](UserGuide.md#selecting-a-contact-select).

**Relevant classes and data**

`SelectCommandParser` constructs an indexed `SelectCommand`. The command resolves the chosen immutable `Person` and returns its exact reference in `CommandResult`. The [shared reconciliation mechanism](#contact-selection-reconciliation) applies the request through `LogicManager` and the UI.

**Parsing and validation**

`AddressBookParser` dispatches `select` to `SelectCommandParser`, which accepts exactly one positive integer using
`ParserUtil.parseIndex()`. Whether that index exists in the current view is checked during execution.

Condition | Handling
----------|---------
Missing index | `ParseException` with `SelectCommand.MESSAGE_INDEX_REQUIRED`.
Extra arguments | `ParseException` with `SelectCommand.MESSAGE_EXTRA_ARGUMENTS`.
Malformed, non-positive, or overflowing integer | `ParseException` with `ParserUtil.MESSAGE_INVALID_INDEX`.
Empty displayed list | `CommandException` with `SelectCommand.MESSAGE_NO_CONTACTS_TO_SELECT`.
Index outside the current view | `CommandException` with the range message, or the single-valid-index variant.

**Execution and failure handling**

`SelectCommand#execute()` resolves the index against the current filtered and sorted `Model#getFilteredPersonList()`.
It returns feedback and the exact contact reference without changing data, filter predicates, comparators, or history.
It does not call `commitAddressBook()`.

The existing automatic saving flow still runs after selection. Because the explicit request is recorded before
saving, a storage exception does not discard a valid selection. Rejected parsing or index validation leaves no
new request, so the UI preserves the previous selection. `help select` returns usage with no selection request;
it neither executes a selection nor opens the full Help window.

An explicit request highlights and reveals the target while keeping command-input focus. Repeated selection of the same object retains the details panel reading position.

**Design considerations**

Use the existing command-result and selection pipeline rather than storing selected state in the model or saved JSON. Object identity allows two contacts with equal names to remain distinct and supports replacement tracking when editable identity fields change.

**Testing references**

Tests cover the command through the real parser, logic, storage, and JavaFX UI pipeline:

* `SelectCommandParserTest` and `AddressBookParserTest`: argument validation, integer boundaries, and dispatch.
* `SelectCommandTest`: exact targets, filtered/sorted indexes, unchanged model state, and invalid displayed indexes.
* `CommandResultTest` and `CommandSelectionChangeTest`: request defaults, value methods, identity, and reset precedence.
* `LogicManagerTest`: request forwarding/reset, unchanged saved contact JSON, undo/redo preservation, and storage failures.
  A package-private parser-injection constructor also supports isolated command-result tests.
* `MainWindowTest`: focus, duplicate names, invalid-input recovery, empty results, edits/deletion after selection,
  saving errors, off-screen and newly added rows, and repeated selection retaining the details scroll position.
  Visibility checks wait for the real viewport row rather than a fixed number of frames, without issuing extra scrolls.
  Regression tests cover dropped index-scroll events, stale endpoint offsets, transient visibility, and cancellation
  after another command, selection, or manual scroll. Failures report the target/selected indexes and visible row bounds.
* `HelpCommandTest` and `HelpWindowTest`: targeted usage, supported-command guidance, browse-category placement,
  formatting, and absence of a selection side effect.

#### Viewing contacts with follow-ups: `followups`

**Overview**

Queries contacts with pending follow-ups without changing their data. See the [UG followups entry](UserGuide.md#viewing-contacts-with-follow-ups-followups).

**Relevant classes and data**

`FollowUpsCommandParser` creates a `FollowUpsCommand` through its `all()`, `overdue()`, or `dueBy()` factory. The command owns its mode, optional cutoff, and injectable `Clock`; it reads the optional `FollowUp` on each contact.

**Parsing and validation**

Accept no arguments, the standalone `overdue` keyword, or one `by/` cutoff. Modes cannot be combined. The parser rejects extra tokens, repeated or unsupported prefixes, and invalid dates.

**Execution and failure handling**

<a id="querying-follow-ups"></a>

`FollowUpsCommandParser` supports an empty query, `overdue`, or a single `by/` date cutoff. These map to the `all()`, `overdue()`, and `dueBy()` factories of `FollowUpsCommand`.
The command replaces the model's current contact predicate, searching the entire address book:

* `ALL` accepts contacts with a follow-up.
* `OVERDUE` accepts follow-ups due strictly before today.
* `BY_DATE` accepts follow-ups due on or before the cutoff.

The overdue query uses an injectable `Clock`. Today's date is captured when the command executes and remains fixed in the predicate until the query is rerun.
Results retain the comparator on the model's wrapping `SortedList<Person>`; without a comparator they follow insertion order.
Queries replace the filter over the complete contact list, so an earlier search does not restrict their input.

Queries do not commit snapshots or alter contact data. An empty result displays `No follow-ups found.`

**Design considerations**

Capture today once when executing an overdue query so membership uses one date throughout that query. UI badge refresh has a separate lifetime and does not silently rerun the filter. Replacing only the predicate preserves sorting and avoids creating undo snapshots.

**Testing references**

`FollowUpsCommandParserTest` checks modes and invalid input; `FollowUpsCommandTest` uses fixed clocks for date boundaries, zones, filtering, and history preservation. See the [manual follow-up tests](#managing-and-viewing-follow-ups).

### Undo and redo

**Shared snapshot mechanism**

<a id="undoredo-feature"></a>

**Relevant classes and data**

<a id="implementation-1"></a>

The implemented undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.
* `VersionedAddressBook#canUndo()` — Indicates whether a previous snapshot exists.
* `VersionedAddressBook#canRedo()` — Indicates whether a next snapshot exists.
* `VersionedAddressBook#rollbackCommit()` — Discards a newly committed state after a failed save and reinstates the redo states that commit displaced.

`ModelManager` delegates these operations through the `Model` API: `commitAddressBook()`, `undoAddressBook()`, `redoAddressBook()`,
`canUndoAddressBook()`, `canRedoAddressBook()`, and `rollbackAddressBook()`. Undo, redo, and rollback reset the displayed filter to show all contacts;
they retain the current sort comparator. History is cleared when the application closes.

`AddCommand`, `DeleteCommand`, `EditCommand`, `ClearCommand`, `SetFollowUpCommand` and `ClearFollowUpCommand` commit once after successful execution, immediately before returning their result. Validation failures do not commit. A valid edit or follow-up replacement can commit even when its result equals the previous data. Clearing an already-empty book is an exception:
it returns feedback without committing, so any available redo history is preserved.

`UndoCommandParser` and `RedoCommandParser` accept empty or whitespace-only arguments and reject additional arguments. Their commands check availability before requesting the corresponding model operation. Undo and redo do not create new snapshots.

The underlying `VersionedAddressBook` also checks its boundaries and throws `NoUndoableStateException` or `NoRedoableStateException` when navigation is unavailable.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the address book is already empty, `clear` does not modify contact data or invoke `Model#commitAddressBook()`, keeping the existing `addressBookStateList` and redo history intact.
</div>

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

**Design considerations**

<a id="design-considerations-3"></a>

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

#### Undoing a change: `undo`

**Overview**

Restores the previous contact-data snapshot. See the [UG undo entry](UserGuide.md#undoing-a-change-undo).

**Relevant classes and data**

`UndoCommandParser` and `UndoCommand` use `Model#canUndoAddressBook()` and `Model#undoAddressBook()`. The snapshot mechanism described above owns the history list and pointer.

**Parsing and validation**

The parser accepts empty or whitespace-only arguments and rejects extra text. The command reports `No more commands to undo!` if no earlier snapshot exists.

**Execution and failure handling**

`ModelManager` delegates to `VersionedAddressBook#undo()`, then displays all contacts without changing the comparator. Successful execution clears selection. `LogicManager` saves the restored data; a failed save leaves the undo and history-pointer movement in memory. See [Persistence and failures](#persistence-and-failures).

**Design considerations**

Restore snapshots rather than re-executing inverse commands. Each undo is one history step and does not append another snapshot.

**Testing references**

`UndoCommandParserTest`, `UndoCommandTest`, `VersionedAddressBookTest`, and `UndoRedoIntegrationTest` cover boundaries, restored states, and multi-step history. `LogicManagerTest` covers save failures. See the [manual undo/redo tests](#undoing-and-redoing-commands-with-undo-and-redo).

#### Redoing a change: `redo`

**Overview**

Restores the next available snapshot after an undo. See the [UG redo entry](UserGuide.md#redoing-a-change-redo).

**Relevant classes and data**

`RedoCommandParser` and `RedoCommand` use `Model#canRedoAddressBook()` and `Model#redoAddressBook()` over the same history.

**Parsing and validation**

The parser accepts empty or whitespace-only arguments and rejects extra text. The command reports `No more commands to redo!` when no next snapshot remains.

**Execution and failure handling**

`VersionedAddressBook#redo()` moves the pointer forward and restores that snapshot. The model displays all contacts, preserves sorting, and the UI clears selection. A new successful commit discards the available redo branch; validation failures and view-only commands preserve it. A failed save after redo leaves its in-memory result applied.

**Design considerations**

Replay the recorded snapshot rather than rerunning the original command, avoiding dependence on indexes that may identify different contacts in the current view.

**Testing references**

`RedoCommandParserTest`, `RedoCommandTest`, `VersionedAddressBookTest`, and `UndoRedoIntegrationTest` cover availability, branch invalidation, and restored contact fields. See the [manual undo/redo tests](#undoing-and-redoing-commands-with-undo-and-redo).

### Application

#### Viewing help: `help`

**Overview**

<a id="command-specific-help"></a>

Opens full help or displays command-specific usage. See the [UG help entry](UserGuide.md#viewing-help-help).

**Relevant classes and data**

`AddressBookParser` creates `HelpCommand`; `COMMAND_USAGE_BY_NAME` supplies targeted text. `CommandResult#isShowHelp()` controls whether `MainWindow` opens `HelpWindow`.

**Parsing and validation**

Optional help arguments are parsed directly in `AddressBookParser`. No target requests full help; one token becomes a target command name; multiple tokens are rejected. Unsupported single-token targets produce guidance during execution.

**Execution and failure handling**

`AddressBookParser` parses the optional command name after `help` and creates a `HelpCommand`. Bare `help` opens the full help window, while `help COMMAND` displays usage for a supported command in the result area.

![Help command activity diagram](images/HelpActivityDiagram.png)

* Bare `help` returns opening feedback in a `CommandResult` with `showHelp` set to `true`.
* For a supported target, `HelpCommand` retrieves the command's `COMMAND_USAGE` from `COMMAND_USAGE_BY_NAME` and returns it as feedback with `showHelp` set to `false`.
* An unsupported target returns guidance with `showHelp` set to `false`. The supported command names in the guidance are derived from the same map.

`MainWindow` displays the feedback in `ResultDisplay` and opens or focuses `HelpWindow` only when `showHelp` is `true`. The activity diagram assumes at most one command target; input with multiple target words is rejected during parsing, and the parser error is displayed in the result area.

**Help window categories**

<a id="help-window-categories"></a>

`HelpWindow.HELP_MESSAGE` groups commands by user task:

| Category | Commands |
| --- | --- |
| Manage contacts | `add`, `edit`, `delete`, `clear`, `followup` |
| Browse contacts | `list`, `find`, `sort`, `select`, `followups` |
| Undo and redo | `undo`, `redo` |
| Application | `help`, `exit` |

`select` belongs under Browse contacts because it changes only the displayed selection. `followup` belongs under Manage contacts because it changes contact data. `followups` belongs under Browse contacts because it filters the displayed contacts without modifying their data.

When adding a user command, define its help constants, register its `COMMAND_USAGE` in `HelpCommand.COMMAND_USAGE_BY_NAME`, and add it to the appropriate category in `HelpWindow.HELP_MESSAGE`. Update `HelpCommandTest` and `HelpWindowTest` to cover targeted help, category placement, and formatting. Update parser tests when usage text changes, and keep the User Guide's command list, categories, and help screenshot consistent with the implementation.

**Design considerations**

<a id="help-text-conventions"></a>

Each user command defines its help text in the command class. `COMMAND_USAGE` is the shared source for both command-specific help and the full help window. `HelpWindow.formatCommandUsage` converts the command name into an uppercase heading while preserving the rest of the usage text.

* Use `COMMAND_WORD`, `COMMAND_DESCRIPTION`, and `COMMAND_USAGE` for help content.
* Commands without parameters define `COMMAND_USAGE` as `COMMAND_WORD + ": " + COMMAND_DESCRIPTION`. Do not add parameter or example fields for these commands.
* Most commands with required or optional parameters define `COMMAND_PARAMETERS` and `COMMAND_EXAMPLE`.
  Their `COMMAND_USAGE` combines the description with `Parameters:` and `Example:` lines. Commands
  with optional arguments, such as `help`, `sort`, and `followups`, follow this format even though
  they can run without arguments. `find` instead uses `COMMAND_FORMATS` and `COMMAND_EXAMPLES`
  with separate `Formats:` and `Examples:` blocks to distinguish prefixed search from quick name search.
* Use the `MESSAGE_` prefix for result feedback and errors, such as `MESSAGE_SUCCESS` and `MESSAGE_FAILURE`. Parsers that include usage instructions in an error should reference `COMMAND_USAGE` rather than duplicate its text.

**Testing references**

`HelpCommandTest` and `HelpWindowTest` cover target lookup, guidance, grouping, and formatting. `AddressBookParserTest` covers argument handling. See the [manual help tests](#viewing-contextual-help-with-help).

#### Exiting the program: `exit`

**Overview**

Requests application shutdown after a short closing message. See the [UG exit entry](UserGuide.md#exiting-the-program-exit).

**Relevant classes and data**

`ExitCommandParser` creates `ExitCommand`. Its `CommandResult#isExit()` flag is consumed by `MainWindow`, while `MainApp#stop()` saves preferences.

**Parsing and validation**

The parser records extra non-blank arguments. The command accepts them and chooses feedback explaining that they are ignored.

**Execution and failure handling**

`ExitCommand` returns an exit flag without changing contacts. After the normal save step succeeds, `MainWindow` disables command input, waits one second, captures window settings, and hides the application windows. If that save fails, the result does not reach the closing handler and the application remains open. Session history is not serialized.

**Design considerations**

Keep command parsing and feedback separate from JavaFX window management. The result flag allows the UI to control the closing delay and preference capture.

**Testing references**

`ExitCommandParserTest`, `ExitCommandTest`, and the UI shutdown checks in `MainWindowTest` cover argument handling, result flags, and closing behaviour.

### Shared mechanisms

#### Contact text validation, formatting, and searching

`Name`, `Role`, `Tag` and `Address` validate trimmed input with `StringUtil.isValidText()`, rejecting
blank/invisible-only values, ISO controls and Unicode line/paragraph separators. Names, roles and tags
accept Unicode, punctuation and literal backslashes.

`normalizeSpaces()` strips Java whitespace and collapses ordinary U+0020 spaces for names, roles and tags.
Addresses only strip their edges. Non-breaking spaces and formatting characters remain literal alongside
visible text; capitalisation is preserved. Tags have a 50-unit limit after decoding and normalisation,
measured by `String.length()` (UTF-16); names, roles and addresses have no maximum.

Name/address equality is case-sensitive; role/tag/email equality uses `equalsIgnoreCase()`.
`normalizeCase()` applies `toLowerCase(toUpperCase(...))` per code point for locale-independent searches
and role/tag/email hashes, preserving stored spelling and accent distinctions.

Prefixes are recognised before `ParserUtil.unescapeSlashes()` decodes each `\/` once. Models and JSON
adapters never decode escapes. Phone, email and date values do not use slash decoding.

`FindCommandParser.normaliseSearchPhrases()` applies `normalizeSpaces()` to prefixed queries, retaining
substring matching, OR within fields and AND across fields. Queries have no whole-field validation or
length limit. Simple name search retains whitespace-based keyword splitting.

#### Email and phone validation

`Email.VALIDATION_REGEX` uses the WHATWG HTML pattern with Java `String.matches()`; the
[UG lists its supported syntax](UserGuide.md#supported-email-syntax). Parsing trims input and preserves
case. Validation checks syntax only, without mailbox verification or IDN conversion.

`Phone.VALIDATION_REGEX` accepts 3–20 ASCII digits, preserving leading zeroes.

Contact identity is separate from full value equality: `Person#isSamePerson()` matches either the phone number or email,
while `Person#equals()` compares all stored fields, including tags and the optional follow-up. Duplicate checks examine
all stored contacts, including those hidden by a filter. A role is custom validated text rather than an enumeration of fixed categories.

#### Follow-up urgency and details

<a id="ui-integration"></a>

`PersonDetailsPanel` populates the follow-up date and description defined in `PersonDetailsPanel.fxml` when a contact
is selected. Contacts without follow-ups hide the section and set it unmanaged so it occupies no layout space.
The compact `PersonCard` shows only a `Follow-up`, `Due today`, or `Overdue` badge beside the role when a follow-up is
present, leaving its date and description in the details panel. Replacement contacts rebuild the card through the
existing list-cell factory, so setting, replacing, clearing, undoing, or redoing a follow-up refreshes the badge without
new model state. The shared `UiDateTracker` exposes a `ReadOnlyObjectProperty<LocalDate>` to the cards and details panel on the JavaFX application thread.
It checks the injected clock once per minute while the window is shown and immediately when the window is shown or regains focus.
It notifies observers only when the date changes. These presentation updates preserve the command draft, selection, and scroll positions;
they do not mutate contacts, commit history, or trigger a save. The `followups overdue` query keeps its separately captured date;
rerun it to update result membership.

The command selection handling described under contact details and selection tracks replacement contacts and refreshes
the details panel after follow-up changes. A contact that no longer matches a follow-up filter clears selection.

#### Displayed contact views

<a id="model-integration-persistence-and-history"></a>

`ModelManager` constructs `filteredPersons` from the address book's observable contact list, then constructs
`sortedPersons` from `filteredPersons`. `updateFilteredPersonList()` changes the predicate on the filtered view;
`updateSortedPersonList()` changes the comparator on the sorted view. Neither operation rearranges stored contacts.
With no comparator, the sorted view follows its source order.

`sort clear` takes a separate branch before comparator construction. It first resets the filter to `PREDICATE_SHOW_ALL_PERSONS`,
then calls `updateSortedPersonList(null)` and returns clearing feedback. A `SortedList` without a comparator follows its source order,
so this displays all contacts in insertion order without rearranging stored data.

![Partial class diagram of the model's filtered and sorted contact views](images/SortModelClassDiagram.png)

The partial class diagram shows each view's source relationship. The sorted view supplies displayed order;
the underlying contact list retains its source order for persistence. Unrelated model classes and list inheritance
relationships are omitted for clarity.

`getFilteredPersonList()` retains its existing API name but returns `sortedPersons`. The UI and commands such as
`EditCommand` and `DeleteCommand` therefore resolve displayed indices against the same list. Changes to the source
contacts propagate through both views, including repositioning a contact when an edit changes its sort keys.
`ModelManager#equals()` also compares the displayed lists so models with different visible ordering are distinguished.

After successful execution, `LogicManager` saves `model.getAddressBook()`, not the displayed view. The comparator is
not serialized, so sorting does not change saved contact order and is reset on restart.

Sort and `sort clear` do not call `commitAddressBook()`, so neither creates an
undo snapshot or discards redo history. Undo and redo restore contact data and
retain the active sort order unless the user explicitly runs `sort clear`.
Subsequent find and list commands retain the selected sort order.

#### Contact selection reconciliation

`CommandResult#getSelectedPerson()` carries an optional explicit selection request. Existing constructors default to
no request; `CommandResult(String, Person)` retains the exact immutable contact reference. Equality, hashing, and the
string representation include the request. `LogicManager` forwards it into `CommandSelectionChange` before saving.
Each command attempt resets this information before parsing. Selection precedence is:

1. Successful undo/redo resets clear selection.
2. An explicit selection request chooses its target, even when there was no previous selection.
3. Otherwise, retain the previous contact or its recorded replacement when it remains visible.

`MainWindow` applies the result through its existing `finally` block and `PersonListPanel#endCommand(Person)`.
The panel resolves the current index by object identity, updates the highlight, and scrolls to the contact without
moving focus out of command input. It resolves pending styling/layout and completes any required scroll after the
scene's layout pass so freshly added rows are positioned correctly. Repeated selection of the same object retains
the details panel's reading position.
There are no new Model or Storage fields and no selection data is persisted.

#### Persistence and failures

After successful command execution, `LogicManager` requests a save of `model.getAddressBook()` through `Storage`.
This includes browsing and help commands. Parsing and command-validation failures exit before the save step.
Only the current contact data is serialized; filters, sorting, selection, snapshots, and the history pointer remain in memory.

The manager records the history pointer before execution. Its error handling depends on whether the command committed a new state:

Outcome | Contact data and history | Display and selection
--------|--------------------------|----------------------
A modifying command commits, but saving fails | `rollbackAddressBook()` delegates to `rollbackCommit()`: the previous snapshot is restored, the new state is removed, and displaced redo states are reinstated. | The filter resets to all contacts while sorting is retained. Replacement metadata is cleared; the UI retains the original selected object when it is present in the restored view.
Saving after `undo` or `redo` fails | No rollback is performed. The restored data and moved history pointer remain in memory. | The all-contact view and selection reset from the executed undo/redo remain applied.
Saving a command that did not advance the history pointer fails | No earlier contact-data change is rolled back. | The command's view effects remain. For example, a valid explicit selection request is retained.

`AccessDeniedException` and other `IOException` failures are converted to `CommandException` feedback.
`MainWindow` reconciles selection in its `finally` block even when execution reports an error. Selection resets take precedence over an explicit request;
an explicit request takes precedence over retaining the previous contact or its recorded replacement. The UI then checks that the chosen object is still visible.

Memory rollback and file-replacement guarantees are separate concerns. See [Atomic file persistence](#atomic-file-persistence) for filesystem limitations.

#### Atomic file persistence

**Overview**

<a id="overview"></a>

File persistence uses `FileUtil#writeToFile` to write a complete temporary file before replacing the destination.
It requests atomic replacement where supported. If the filesystem rejects atomic moves, the fallback replaces the file without requesting atomicity;
the application therefore cannot guarantee atomic replacement on every filesystem.

**Implementation details**

<a id="implementation-details-1"></a>

After a command executes successfully, `LogicManager` requests `Storage` to persist the current address book.
The following steps describe a contact-data save; preference JSON uses the same file-writing utility.

1. `JsonAddressBookStorage` serializes the address book into JSON via `JsonUtil#saveJsonFile`.
2. `JsonUtil` delegates writing to `FileUtil#writeToFile(Path file, String content)`.
3. `FileUtil#writeToFile` creates any missing parent directories, then creates a temporary file in the same parent directory (`tmp_<filename>.<rand>.tmp`). Using the same directory ensures the temporary file resides on the same filesystem volume, enabling atomic file replacement.
4. The serialized JSON content is written to the temporary file using UTF-8 encoding.
5. `FileUtil#writeToFile` atomically replaces the destination file using `Files.move(..., StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)`.
6. If the filesystem does not support atomic moves (`AtomicMoveNotSupportedException`), it falls back to `StandardCopyOption.REPLACE_EXISTING`.
7. A `finally` block attempts to delete any remaining temporary file using `Files.deleteIfExists()`. I/O failures propagate to the caller.
8. For contact-data saves during command execution, `LogicManager` converts `AccessDeniedException` or another `IOException` into `CommandException` feedback.
   In-memory recovery follows [Persistence and failures](#persistence-and-failures). Preference-save errors are handled separately by `MainApp`.

Failures before replacement leave the destination unmodified. The non-atomic fallback does not provide the same all-or-nothing replacement guarantee;
callers must not assume every reported failure means the old file is intact. This mechanism also does not establish a power-loss durability guarantee.

The sequence diagram below illustrates the successful atomic-move path across components. The fallback and failure paths are described above:

![AtomicWriteSequenceDiagram](images/AtomicWriteSequenceDiagram.png)

**Design considerations**

<a id="design-considerations-4"></a>

**Aspect: How file writes are performed**
* **Alternative 1 (current choice):** Write to a sibling temporary file and atomically replace the destination file.
  * Pros: Keeps serialization and temporary-file writing separate from the destination file; an atomic move, when supported, prevents a partially replaced destination.
  * Limitation: The fallback move is not guaranteed atomic, and the implementation does not explicitly flush file or directory metadata for power-loss durability.
  * Cons: Requires temporary disk space equal to the size of the saved file during the write operation.
* **Alternative 2:** Direct in-place file write using `Files.writeString` with standard truncation.
  * Pros: Simpler implementation without temporary files.
  * Cons: The file is truncated before new content is written. A crash or disk error mid-write leaves the data file empty or partially written, corrupting user data.

#### Startup loading and recovery

`MainApp` initializes preferences and contact data independently. Paths are relative to the launch folder:
`preferences.json` and `data/addressbook.json`.

File state | Contact data | Preferences
-----------|--------------|------------
Missing | Load sample contacts into memory. A contact-data save is attempted only after a successfully executed command. | Use defaults and attempt to save the initialized preferences during startup.
Existing but invalid | Attempt a timestamped backup, report the backup path or failure, and initialize an empty contact book. One invalid contact rejects the whole file. | Attempt a timestamped backup, report the outcome, and use default preferences. Startup still attempts to save those initialized preferences.
Valid | Load the saved contacts, including optional follow-ups. | Load saved GUI settings.

Backup copies are made beside the original file using `FileUtil#createTimestampedBackup()`. They preserve the existing file contents,
including invalid content, and are not a history of successful saves. Failed backup creation does not prevent fallback initialization.
Warnings from preferences and contact loading are combined for display when the main window opens.

An empty fallback contact book can overwrite the saved contact file after any successful command, including a browsing command or `exit`.
Recovery therefore requires preserving the available backup or original file and closing the window without entering more commands.
See the [User Guide recovery procedure](UserGuide.md#recovering-data-that-fails-to-load) for the user-facing steps.

### Future design notes

#### Proposed data archiving

_{Explain here how the data archiving feature will be implemented}_

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* owns or manages a small artisan keyboard shop
* has a need to organize and find contacts for employees, suppliers and service providers
* prefers desktop apps over other types of applications
* is proficient in typing
* is reasonably comfortable using keyboard commands and CLI apps

**Value proposition**: KeyBossWarriorsPro helps small artisan keyboard shop owners and managers
organize and find employee, supplier, and service provider contacts using keyboard commands,
enabling experienced users to coordinate daily operaitons faster than with a typical mouse-driven
GUI application.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As  an …                      | I want to …                                                                                | So that I can…                                                            |
|----------|-------------------------------|--------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| `* * *`  | artisan keyboard shop manager | add contacts with their names, phone numbers, emails and addresses                         | keep all shop contacts in one place.                                      |
| `* * *`  | artisan keyboard shop manager | delete outdated contacts by index                                            | have a contact list that stays relevant.                                  |
| `* * *`  | artisan keyboard shop manager | edit contact details                                                                       | reach the right people using updated communication details.               |
| `* * *`  | artisan keyboard shop manager | list all contacts in a compact table                                                       | quickly review staff, suppliers and service providers.                    |
| `* * *`  | artisan keyboard shop manager | tag contacts as employees, suppliers, or service providers                                 | distinguish staff from external partners.                                 |
| `* * *`  | artisan keyboard shop manager | search contacts by full or partial names regardless of letter case                         | quickly find a particular contact's details.                              |
| `* * *`  | artisan keyboard shop manager | filter contacts by component tags                                                          | find alternative suppliers when stock is low.                             |
| `* * *`  | artisan keyboard shop manager | combine name, role, and tag search criteria                                                | narrow my search to relevant staff or suppliers.                           |
| `* * *`  | artisan keyboard shop manager | contact and tag changes to be saved automatically to a local JSON file                     | persist changes between sessions.                                         |
| `* * *`  | artisan keyboard shop manager | load saved contacts automatically at startup                                               | begin work without re-entering all details.                               |
| `* * *`  | artisan keyboard shop manager | view command syntax and parameters using the help command                                  | learn commands within the application.                                    |
| `* * *`  | artisan keyboard shop manager | exit the application using a CLI command                                                   | exit gracefully while keeping my terminal open.                           |
| `* *`    | artisan keyboard shop manager | sort contacts by name, role, or both in my chosen priority                                  | browse contacts alphabetically or group contacts with matching roles.      |
| `* *`    | artisan keyboard shop manager | tag contacts with finer-grain details like suppliers by the components supplied            | find suitable suppliers and service providers for custom keyboard builds. |
| `* *`    | artisan keyboard shop manager | record suppliers' minimum order quantities                                                 | check order requirements before placing group-buy orders.                 |
| `* *`    | artisan keyboard shop manager | record suppliers' manufacturing and shipping lead times in days                            | estimate delivery dates for keyboard pre-orders.                          |
| `* *`    | artisan keyboard shop manager | record payment terms on supplier profiles                                                  | arrange payments according to agreements with suppliers.                  |
| `* *`    | artisan keyboard shop manager | tag technicians by their verified assembly skills                                          | assign suitable staff for custom builds.                                  |
| `* *`    | artisan keyboard shop manager | associate contacts with keyboard group-buy projects                                        | view all suppliers and artisans involved in a particular project.         |
| `* *`    | artisan keyboard shop manager | record emergency phone numbers for part-time soldering staff                               | contact them during urgent builds.                                        |
| `* *`    | artisan keyboard shop manager | record hourly pay rates of staff                                                           | calculate payroll for my employees.                                       |
| `* *`    | artisan keyboard shop manager | record part-time staff's weekly availability                                               | schedule shifts without repeatedly asking when they are free.             |
| `* *`    | artisan keyboard shop manager | filter staff by their availability on a specific weekend                                   | quickly arrange staffing for major assembly sessions.                     |
| `* *`    | artisan keyboard shop manager | filter contacts using a combination of tags                                                | find contacts matching several requirements.                              |
| `* *`    | artisan keyboard shop manager | view a supplier's notes, tags and transaction history in an expanded card                  | review the relationship before negotiating a contract renewal.            |
| `* *`    | artisan keyboard shop manager | use single-letter command aliases and shortened prefixes                                   | manage my contacts with fewer keystrokes.                                 |
| `* *`    | artisan keyboard shop manager | browse recent commands using the Up and Down arrow keys                                    | reuse or edit commands without retyping them.                             |
| `* *`    | artisan keyboard shop manager | export all contacts to a formatted CSV file using one command                              | share supplier details or use them in spreadsheets.                       |
| `* *`    | artisan keyboard shop manager | import contacts from a CSV backup with automatic format validation                         | migrate existing contact lists into KeyBossWarriorsPro.                   |
| `*`      | artisan keyboard shop manager | create timestamped contact backups automatically after operations                          | restore data after accidental changes or deletions.                       |
| `*`      | artisan keyboard shop manager | record defect rates and quality ratings for suppliers                                      | avoid suppliers with recurring product defects.                           |
| `*`      | artisan keyboard shop manager | toggle terminal notifications for upcoming supplier contract expiries and restocking dates | place orders before assembly supplies run out.                            |
| `*`      | artisan keyboard shop manager | customize CLI theme colors                                                             | personalize my workspace                                                  |
| `*`      | artisan keyboard shop manager | check shipment tracking from a supplier's contact card using a tracking code           | monitor incoming product deliveries.                                      |
| `*`      | artisan keyboard shop manager | track past unit-price quotes for products                                              | compare prices and negotiate better deals.                                |

### Use cases

(For all use cases below, the **System** is the `KeyBossWarriorsPro` and the **Actor** is the `user`, unless specified otherwise)

**Use Case: UC1 - Add a Contact**

**MSS**

1.  User requests to add a contact, providing its name, phone number, email, address, role, and any optional tags.
2.  `KeyBossWarriorsPro` adds the contact and confirms the addition.

    Use case ends.

**Extensions**

*  1a. One or more mandatory fields are missing.

  *  1a1. `KeyBossWarriorsPro` shows an error message indicating the missing parameter(s).

     Use case ends.

*  1b. A provided field value is invalid or extraneous arguments are provided.

  *  1b1. `KeyBossWarriorsPro` shows an error message.

     Use case ends.

*  1c. The phone number or email belongs to an existing contact.

  *  1c1. `KeyBossWarriorsPro` shows an error message.

    Use case ends.


**Use Case: UC2 - Find a Contact**

**MSS**

1.  User requests to find contacts by specifying one or more search criteria
    (by name, role, or tag).

2.  `KeyBossWarriorsPro` displays all contacts matching the given criteria.

    Use case ends.

**Extensions**

*  1a. No search criteria are provided, or any prefixed value is blank.
   *  1a1. `KeyBossWarriorsPro` shows an error message.
       
      Use case ends.

*  1b. An unsupported prefix is provided, or unprefixed keywords are mixed with prefixed values.
   *  1b1. `KeyBossWarriorsPro` shows an error message.
       
      Use case ends.

*  2a. No contacts match the given criteria.
   *  2a1. `KeyBossWarriorsPro` informs the user that no contacts were found.
       
       Use case ends.


**Use Case: UC3 - Delete a Contact**

**Preconditions**

*  `KeyBossWarriorsPro` is running and a contact list is displayed.

**MSS**
1.  User requests to delete a contact by specifying its index in the currently displayed contact list.
2.  `KeyBossWarriorsPro` removes the contact, saves the change, and displays the deleted contact's
    details, including any follow-up.

    Use case ends.

**Extensions**

*  1a. The request has missing, multiple, or malformed index arguments.
   *  1a1. `KeyBossWarriorsPro` explains the input error.

      Use case ends.

*  1b. The request has a valid positive index, but no contacts are displayed.
   *  1b1. `KeyBossWarriorsPro` informs the user that no contacts are currently displayed.

      Use case ends.

*  1c. The request has a valid positive index beyond the nonempty displayed list.
   *  1c1. `KeyBossWarriorsPro` explains the valid index or range.

      Use case ends.

*  2a. Saving the deletion fails.
   *  2a1. `KeyBossWarriorsPro` rolls back the deletion, displays all contacts, and reports the saving error.

      Use case ends.

Argument and index errors leave contact data, the displayed view, and undo/redo history unchanged.


**Use Case: UC4 - Add or Delete Contact Tags**

**MSS**

1.  User requests to find contacts using a name keyword.
2.  `KeyBossWarriorsPro` displays matching contacts and their details.
3.  User requests to add or delete named tags on a contact using its displayed index. Both operations may be supplied in one edit.
4.  `KeyBossWarriorsPro` applies the tag changes, preserves the remaining tags, and displays the updated contact information.

    Use case ends.

**Extensions**

*  2a. No contacts match the keyword.

  *  2a1. `KeyBossWarriorsPro` informs the user that no contacts were found.

     Use case ends.
 
*  3a. A tag prefix has an empty or invalid value.

  * 3a1. `KeyBossWarriorsPro` shows an error message and leaves all contact details unchanged.

       Use case resumes at step 3.

*  3b. Tags are repeated within the same operation, or an addition names a tag already on the contact.

  *  3b1. `KeyBossWarriorsPro` retains one instance of each tag, comparing names case-insensitively.
  
     Use case resumes at step 4.

*  3c. The same tag is supplied for addition and deletion, including with different letter casing.

  *  3c1. `KeyBossWarriorsPro` shows a conflict error and leaves all contact details unchanged.

     Use case resumes at step 3.

*  3d. At least one requested deletion names a tag the contact does not have.

  *  3d1. `KeyBossWarriorsPro` identifies a missing tag and rejects the entire edit. Valid deletions, additions, and other field edits in the same command are not applied.

     Use case resumes at step 3.


**Use Case: UC5 - List Contacts**

**MSS**

1.  User requests to list contacts.
2.  `KeyBossWarriorsPro` resets the active search filter (if any) and shows a list of contacts.
    
    Use case ends.

**Extensions**

*  1a. The user supplies additional arguments.
  *  1a1. `KeyBossWarriorsPro` displays a warning that additional arguments are ignored.

     Use case resumes at step 2.

*  2a. The address book is empty.

  *  2a1. `KeyBossWarriorsPro` informs the user that no contacts were found.

     Use case ends.

**Use Case: UC6 - Sort Contacts**

**MSS**

1. User requests to sort contacts, optionally specifying name, role,
   or both in the desired priority.
2. `KeyBossWarriorsPro` displays all contacts in ascending,
   case-insensitive order using the requested criteria, defaulting
   to name when none are specified.

   Use case ends.

**Extensions**

* 1a. The sorting criteria are invalid, such as repeated fields,
  unsupported fields, or values supplied after a sorting field.

  * 1a1. `KeyBossWarriorsPro` explains the error and leaves the
    displayed list and active sorting criteria unchanged.

    Use case ends.

* 2a. The address book contains no contacts.

  * 2a1. `KeyBossWarriorsPro` displays an informational message instead of an
    error. The selected sorting criteria remain active for contacts added
    afterwards.

    Use case ends.

**Use Case: UC7 - Clear Sort Order**

**MSS**

1. User requests to clear the active sort order using `sort clear`.
2. `KeyBossWarriorsPro` displays all contacts in their original insertion order.

   Use case ends.

**Extensions**

* 1a. The address book contains no contacts.

  * 1a1. `KeyBossWarriorsPro` displays an empty list and confirms that sorting
    has been cleared.

    Use case ends.

**Use Case: UC8 - Set a Follow-up**

**Preconditions**

* `KeyBossWarriorsPro` is running and a contact list is displayed.

**MSS**

1. User requests to set a follow-up on a specific contact by specifying its displayed index, a due date, and a description.
2. `KeyBossWarriorsPro` attaches the follow-up to the contact, updates the displayed details, saves the address book to disk, and displays a success confirmation.

   Use case ends.

**Extensions**

* 1a. The request has missing, multiple, or malformed index arguments.

  * 1a1. `KeyBossWarriorsPro` explains the input error.

    Use case ends.

* 1b. The request has a valid positive index, but no contacts are currently displayed.

  * 1b1. `KeyBossWarriorsPro` informs the user that no contacts are currently displayed.

    Use case ends.

* 1c. The request has a valid positive index that is beyond the range of the displayed list.

  * 1c1. `KeyBossWarriorsPro` explains the valid index range.

    Use case ends.

* 1d. The request is missing mandatory `by/` or `abt/` prefixes, contains duplicate prefixes, or supplies unrecognized prefixes.

  * 1d1. `KeyBossWarriorsPro` displays an invalid command format error showing the correct usage.

    Use case ends.

* 1e. The provided due date is not in `YYYY-MM-DD` format or is not a valid calendar date.

  * 1e1. `KeyBossWarriorsPro` displays a date format constraint error.

    Use case ends.

* 1f. The provided description is blank or exceeds 200 characters.

  * 1f1. `KeyBossWarriorsPro` displays a description constraint error.

    Use case ends.

**Use Case: UC9 - Remove a Follow-up**

**Preconditions**

* `KeyBossWarriorsPro` is running and a contact list is displayed.

**MSS**

1. User requests to clear an existing follow-up by specifying the displayed index of the contact using `followup INDEX clear`.
2. `KeyBossWarriorsPro` clears the follow-up from the contact, updates the displayed details, saves the change to disk, and displays a confirmation message.

   Use case ends.

**Extensions**

* 1a. The request has missing, multiple, or malformed index arguments.

  * 1a1. `KeyBossWarriorsPro` explains the input error.

    Use case ends.

* 1b. The request has a valid positive index, but no contacts are currently displayed.

  * 1b1. `KeyBossWarriorsPro` informs the user that no contacts are currently displayed.

    Use case ends.

* 1c. The request has a valid positive index that is beyond the range of the displayed list.

  * 1c1. `KeyBossWarriorsPro` explains the valid index range.

    Use case ends.

* 1d. The request contains extraneous arguments or invalid syntax after `clear`.

  * 1d1. `KeyBossWarriorsPro` displays an invalid command format error.

    Use case ends.

* 1e. The specified contact does not have an active follow-up to clear.

  * 1e1. `KeyBossWarriorsPro` displays an error informing the user that the contact has no follow-up to clear.

    Use case ends.

**Use Case: UC10 - View Follow-ups**

**Preconditions**

* `KeyBossWarriorsPro` is running.

**MSS**

1. User requests to view contacts with follow-ups using `followups` (optionally specifying `overdue` or `by/DUE_DATE`).
2. `KeyBossWarriorsPro` filters and displays all contacts matching the follow-up query.

   Use case ends.

**Extensions**

* 1a. The request contains invalid arguments or malformed date values.

  * 1a1. `KeyBossWarriorsPro` displays an error message indicating the invalid format or date constraint.

    Use case ends.

* 1b. The request contains extraneous arguments (e.g., additional trailing words after `overdue`).

  * 1b1. `KeyBossWarriorsPro` shows an error message rejecting extraneous arguments.

    Use case ends.

* 2a. No contacts match the follow-up query (or no contacts have follow-ups).

  * 2a1. `KeyBossWarriorsPro` displays an empty list and informs the user that no follow-ups were found.

    Use case ends.

**Use Case: UC11 - Undo a Command**

**Preconditions**

* `KeyBossWarriorsPro` is running.

**MSS**

1. User requests to undo the most recent modifying command.
2. `KeyBossWarriorsPro` reverts the address book state to the previous snapshot, saves the address book to disk, resets the view to show all contacts, and displays a success confirmation.

   Use case ends.

**Extensions**

* 1a. The undo history is empty (no modifying commands have been executed in the current session, or all changes have already been undone).

  * 1a1. `KeyBossWarriorsPro` displays an error message indicating there are no more commands to undo.

    Use case ends.

* 1b. The user supplies additional arguments to `undo` (e.g., `undo 2`).

  * 1b1. `KeyBossWarriorsPro` shows an error message rejecting extraneous arguments.

    Use case ends.

**Use Case: UC12 - Redo a Command**

**Preconditions**

* `KeyBossWarriorsPro` is running.

**MSS**

1. User requests to redo the previously undone command.
2. `KeyBossWarriorsPro` restores the undone state, saves the address book to disk, resets the view to show all contacts, and displays a success confirmation.

   Use case ends.

**Extensions**

* 1a. The redo history is empty (no undone commands exist to redo, or a new modifying command was executed after an undo).

  * 1a1. `KeyBossWarriorsPro` displays an error message indicating there are no more commands to redo.

    Use case ends.

* 1b. The user supplies additional arguments to `redo` (e.g., `redo 2`).

  * 1b1. `KeyBossWarriorsPro` shows an error message rejecting extraneous arguments.

    Use case ends.

### Non-Functional Requirements

1.  **Operating Environment:** Should work on any _mainstream OS_ (Windows 10/11, macOS, Linux) as long as it has Java `25` or above installed.
2.  **Portability:** The application should be packaged as a single standalone executable JAR file that runs without requiring an installer.
3.  **Network Independence:** Should function fully offline without requiring an active internet connection or any remote server.
4.  **Capacity:** Should be able to store and manage up to 1,000 contacts and 5,000 tags without noticeable sluggishness in performance for typical usage.
5.  **Responsiveness:** All standard commands (such as `list`, `filter`, `find`, `add`, `delete`) should respond and update the interface within 1.0 second under normal workloads.
6.  **Package Size:** The packaged application JAR file should not exceed 100 MB.
7.  **Data Persistence:** Data must be stored in a human-editable text file (JSON format) located at `data/addressbook.json`.
8.  **Fault Tolerance:** If the data file is corrupted or formatted incorrectly, the application should start gracefully (e.g., discard or isolate corrupted state and notify the user) without unexpected crashes.
9.  **Database Independence:** The system should not depend on a Relational Database Management System (RDBMS) like MySQL or SQLite.
10. **Typing-Preferred Usability:** A user with above-average typing speed for regular English text should be able to accomplish all routine contact and inventory management tasks faster using CLI commands than using a mouse.
11. **Display Adaptability:** The user interface should remain fully functional and legible on screen resolutions of 1920x1080 with display scaling up to 150%, and on 1280x720 at 100% scaling (effective minimum viewport of 1280x720).
12. **Single-User Scope:** The software is designed for a single artisan keyboard shop manager working locally; concurrent multi-user access is out of scope.
13. **Data Integrity:** File saves must be atomic so that unexpected crashes or disk write errors do not leave data files truncated or partially written.

### Glossary

#### Keyboard terms

* **Artisan keyboard**: A custom mechanical keyboard built from individual parts such as custom cases, switches, printed circuit boards (PCBs), and keycaps.
* **Switch**: The mechanical mechanism seated under each keycap that opens and closes an electrical circuit on keypress. Common types include linear, tactile, and clicky switches.
* **PCB (Printed Circuit Board)**: The internal circuit board that registers switch activations and connects to the computer, some using hot-swap sockets so users can replace switches without soldering.
* **Keycap**: A removable plastic cover mounted on a switch stem. Keycaps are commonly molded from PBT or ABS plastic.
* **Stabilizer**: A mechanical wire and housing assembly used on wider keys (such as space, enter, and shift) to keep the key level when pressed off-center.

#### Contact terms

* **Contact**: A person or commercial entity stored in the address book.
* **Duplicate contact**: Two contact entries that share either the same phone number or the same email address.
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **Primary role**: The required custom-text classification assigned to a contact. Examples include `Employee`, `Supplier`, and `Service Provider`; these are not an exhaustive set of permitted values. Every contact has exactly one role.
* **Tag**: An optional label added to a contact to record categories, supplied parts (such as `switches` or `keycaps`), or services. A contact can have zero or more tags.

#### Technical terms

* **Mainstream OS**: Windows, Linux, Unix, or macOS.
* **CLI (Command Line Interface)**: A text-based interface where the user types commands into a prompt to operate the application.
* **GUI (Graphical User Interface)**: A visual interface created with JavaFX that displays contact cards, command outputs, and application status.
* **JSON (JavaScript Object Notation)**: A lightweight, human-readable text file format used to save address book records locally on disk.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Selecting a contact

Start with at least two sample/test contacts. Use the current displayed numbers after each search or sort.

Action | Expected result
-------|----------------
Run `select 2`. | Contact 2 is highlighted, scrolled into view, and shown in the right panel; command input keeps focus.
Scroll the list away, then repeat `select 2`. | The same contact is revealed; the details panel keeps its reading position.
Run `find` with a matching name, then `select 1`. | The first current search result is selected.
Run `sort n/`, then `select 1`. | The first contact in the sorted view is selected.
Try `select`, `select 0`, `select -1`, `select abc`, `select 1 2`, and an index beyond the list size. | An appropriate error appears; selection and details remain unchanged.
Run a search with no matches, then `select 1`. | The empty-state prompt remains and feedback explains that no contacts are displayed.
With enough contacts to scroll, add a contact with wrapping tags and immediately select its displayed number. | Its selected card is scrolled into view using the updated layout.
Run `help select` while a contact is selected. | Usage appears in command feedback; selection stays unchanged and no Help window opens.
Make one edit, undo it, run `select 1`, then redo. | Redo is still available and reapplies the edit; undo/redo clear selection.
Restart the application. | Contact data remains available and the details panel starts in its empty state.

### Deleting contacts with `delete`

Start a fresh application session using a disposable copy in a separate folder.
Run `clear` to remove the sample contacts, then enter these setup commands separately:

```text
add n/Alice p/81009201 e/alice@delete.example.com a/Test Address r/Employee
add n/Bob p/81009202 e/bob@delete.example.com a/Test Address r/Supplier
add n/Charlie p/81009203 e/charlie@delete.example.com a/Test Address r/Supplier
```

The list should show Alice, Bob, and Charlie at indices 1, 2, and 3.
Run the rows below in order. Where a row contains several commands, enter them separately.
Each rejected request must leave the displayed list and contact data unchanged.

| Command | Expected result |
|---------|-----------------|
| `delete 2` | Bob is deleted. Feedback starts with `Deleted contact: Bob;`. Alice and Charlie remain at indices 1 and 2. |
| `delete 1` | Alice is deleted. Charlie is now at index 1. |
| `delete` | `INDEX is required.` Charlie remains. |
| `delete 1 2` | `delete accepts only one INDEX.` Charlie remains. |
| `delete 0`, `delete abc`, then `delete 2147483648` | Each reports `INDEX must be a whole number between 1 and 2147483647.` Charlie remains. |
| `delete 2` | `INDEX 2 is outside the displayed contact list (valid index: 1).` Charlie remains. |
| `delete 1` | Charlie is deleted and the list becomes empty. |
| `delete 1` | `No contacts are currently displayed.` The list stays empty. |

### Name, role, and tag formatting

Use a disposable application copy and add a contact with unused phone and email details:

```text
add n/Anne-Marie   O'Connor p/81009001 e/formatting@example.com a/Test Address r/R&D   Partner t/lubed/filmed   switches
```

Expected: The contact has name `Anne-Marie O'Connor`, role `R&D Partner`, and tag
`lubed/filmed switches`. Punctuation and capitalisation are preserved, while each
run of internal spaces becomes one space. Run `find n/Anne-Marie O'Connor` and
confirm there is exactly one result before using index 1 below.

Command | Expected result
--------|------------------
`find n/Anne-Marie   O'Connor`, then `find n/Anne-Marie O'Connor` | Both searches return the test contact.
`find r/R&D   Partner`, then `find r/R&D Partner` | Both searches return the test contact.
`find t/lubed/filmed   switches`, then `find t/lubed/filmed switches` | Both searches return the test contact.
`edit 1 n/Anne-Marie    O'Connor r/R&D    Partner t/PCB    Supplier` | All supplied fields use single internal spaces. The new tag is `PCB Supplier`, and the existing tag remains.
`edit 1 n/Changed xx/value` | Unknown-prefix error; the contact's name, role, and tags remain unchanged.
Restart the application, then run `find n/Anne-Marie O'Connor`. | The normalised name, role, and both tags are retained.

### Finding contacts with `find`

Use a disposable application copy in a separate folder: this setup replaces saved contacts.
Run `clear`, then `sort clear`, and execute each setup command separately:

```text
add n/Alice Tan p/81009101 e/alice.tan@find.example.com a/Test Address r/Employee t/tactile switch t/keycap
add n/Alice Lee p/81009102 e/alice.lee@find.example.com a/Test Address r/Regional Supplier t/tactile switch t/trusted partner
add n/Bob Tan p/81009103 e/bob.tan@find.example.com a/Test Address r/Regional Supplier t/keycap
add n/Carol Lim p/81009104 e/carol.lim@find.example.com a/Test Address r/Distributor
```

Run the rows below in order. Where a row contains several commands, enter them separately
from left to right.

| Command | Expected result |
|---------|-----------------|
| `find aLI tAN` | Alice Tan, Alice Lee, and Bob Tan; `3 contacts found.` Keywords use OR and ignore case. |
| `find n/Alice Tan` | Alice Tan only; `1 contact found.` The value is one continuous phrase. |
| `find r/gional supp t/tactile swi` | Alice Lee only. Both the role and tag substrings must match. |
| `find t/tactile t/keycap` | Alice Tan, Alice Lee, and Bob Tan, each once. Either tag phrase suffices; Carol has no tags. |
| `find n/ali n/bob r/supp` | Alice Lee and Bob Tan. The name alternatives use OR; the role condition uses AND. |
| `find NoSuchFindContact` | An empty list and `No contacts found.` |
| `find n/Carol`, then `find n/Alice` | First Carol Lim, then both Alice contacts. The second search checks the full contact list. |
| `sort r/ n/`, then `find r/supp`, then `list` | Find shows Alice Lee, then Bob Tan. List restores Carol Lim, Alice Tan, Alice Lee, then Bob Tan, retaining role-then-name order. |

Before checking errors, run `find n/Alice Tan`. Each rejected command below must preserve
that one-contact view and all stored contacts.

| Command | Expected feedback |
|---------|-------------------|
| `find` | `find requires at least one search criterion.` |
| `find n/Alice r/` | `find requires a non-blank value after n/, r/, t/.` A blank prefix is rejected even when another value is present. |
| `find Alice r/Supplier` | `find cannot mix unprefixed keywords with prefixed search values.` |
| `find p/81009101` | `Unrecognized prefix(es) provided: p/.` |

### Editing contact tags

Create a test contact with a unique phone number and email, for example `add n/TagEditExample p/12345678 e/tagedit@example.com a/Test Address r/Employee t/trainee t/supplier`. Run `find TagEditExample` and confirm this is the only result. Before each command below, run `find TagEditExample` again because a successful edit displays all contacts. Perform the cases in the order shown.

Command | Expected result
--------|------------------
`edit 1 t/solderer` | Tags become `trainee`, `supplier`, and `solderer`; other fields are preserved.
`edit 1 dt/TRAINEE` | Tags become `supplier` and `solderer`.
`edit 1 t/builder dt/solderer` | Tags become `supplier` and `builder`.
`edit 1 n/Alex dt/supplier dt/trainee` | Missing-tag error for `trainee`. The name remains `TagEditExample`, both tags remain, and the filtered list is unchanged.
`edit 1 t/builder dt/BUILDER` | Conflict error for `builder`; the contact and filtered list are unchanged.
`edit 1 t/` and `edit 1 dt/` (separately) | Empty-tag error; the contact and filtered list are unchanged.
`edit 1 dt/supplier dt/builder` | Both named tags are removed, leaving no tags.

### Managing and viewing follow-ups

Create a contact with a unique name, phone number, and email. Run `find` with that name so only the test contact is displayed. Use valid dates relative to the computer's current local date. Select the contact to inspect its follow-up in the right details panel; select it again after undo, redo, or restarting the application.

Action | Expected result
-------|----------------
Set a follow-up using `followup 1 by/DATE abt/Confirm quotation`. | The appropriate Follow-up, Due today, or Overdue badge appears; the details panel shows the date and description.
Set another follow-up with a different date and description. | Both values replace the existing follow-up; the badge remains.
Run `edit 1 r/Employee`, then find the test contact again. | The follow-up is preserved.
Run `followups overdue` with a follow-up due yesterday, then today. | Yesterday's follow-up appears; today's does not.
Run `followups by/DATE` using the follow-up's exact due date. | The contact appears because the cutoff is inclusive.
Run `followups by/DATE overdue` or `followups by/DATE extra`. | An invalid command format error appears without changing the view.
Find the test contact and run `followup 1 clear`. | The badge and follow-up details disappear; the contact remains, with no reserved badge space.
Run `undo`, then `redo`. | The follow-up and badge are restored, then removed again.
Try a missing field, duplicate prefix, impossible date, and invalid index. | An error appears without changing data or the view.
Restart after setting a follow-up. | The saved follow-up is restored.
Display a long description and resize the window. | Verify the full text remains accessible through vertical scrolling in the details panel.
Set due dates for yesterday, today, and tomorrow. | The cards show Overdue, Due today, and Follow-up respectively, including at the minimum pane width.
Leave the window open across local midnight, or return focus after sleep. | Within the next minute, or immediately on focus return, badge urgency and selected overdue details update while selection, scrolling, and draft input remain intact. Rerun any overdue query to refresh which contacts it includes.

### Sorting contacts {#sorting-contacts-1}

Use a disposable application copy in a separate folder: these tests replace saved contacts.
Run `clear`, then `sort clear`, and enter these setup commands separately, in order:

```text
add n/Zoe p/81000001 e/zoe@sort.example.com a/Test Address r/Employee
add n/ben p/81000002 e/ben@sort.example.com a/Test Address r/Supplier
add n/Alice p/81000003 e/alice.supplier@sort.example.com a/Test Address r/Supplier
add n/alice p/81000004 e/alice.distributor@sort.example.com a/Test Address r/Distributor
```

The roles distinguish the two Alice contacts. Run the rows below in order, entering each command separately.

| Action / command | Expected result |
|------------------|-----------------|
| Run `sort` (also try `sort n/`). | Both Alice contacts appear before ben, then Zoe, ignoring case. Feedback: `Listed all contacts in sorted order.` |
| Run `sort n/ r/`. | Order: alice (Distributor), Alice (Supplier), ben, Zoe. Role breaks the matching-name tie. |
| Run `sort r/ n/`. | Order: alice (Distributor), Zoe, Alice (Supplier), ben. Name breaks the Supplier tie. |
| Run `find r/Supplier`, then `sort n/`. | Find shows two Suppliers; sort reveals all four contacts in name order. |
| Try `sort n/ n/`, `sort n/Alice`, and `sort t/`. | Each reports the specific problem: repeated field, unexpected value, or unsupported token. Contacts and sorting remain unchanged. |
| Run `sort clear`. | Original insertion order is restored: Zoe, ben, Alice (Supplier), alice (Distributor). Feedback: `Sorting cleared. Contacts will be displayed in insertion order.` |
| Run `clear`, then `sort n/`. | The list is empty. An informational message says the selected sort order will apply to contacts added later. |
| Add Zoe and Alice (Supplier) using their setup commands, in that order. | Alice appears before Zoe, showing that sorting also applies to newly added contacts. |

### Adding contacts with `add`

Start a fresh application session using a disposable copy in a separate folder.
Run `clear` to remove all contacts, then enter these commands in order.
Where a row contains several commands, enter them separately.
Each rejected request must leave the displayed list and stored contact data unchanged.

| Command | Expected result |
|---------|-----------------|
| `add n/John Doe p/98765432 e/johnd@example.com a/311, Clementi Ave 2, #02-25 r/Supplier t/friends t/owesMoney` | Adds John Doe with role `Supplier` and tags `[friends][owesMoney]`. Feedback starts with `New person added: John Doe;`. |
| `add n/David s\/o Muthu p/81001234 e/david@example.com a/Blk 456 Jurong r/R\/D Partner t/lubed\/filmed switches` | Decodes `\/` once. Adds David with name `David s/o Muthu`, role `R/D Partner`, and tag `[lubed/filmed switches]`. |
| `add n/Jane Doe p/98765432 e/jane@example.com a/Test Address r/Employee` | Duplicate phone error: `This phone number already belongs to an existing contact.` |
| `add n/Johnny Doe p/81009999 e/johnd@example.com a/Test Address r/Employee` | Duplicate email error: `This email address already belongs to an existing contact.` |
| `add n/Jack Doe p/98765432 e/johnd@example.com a/Test Address r/Employee` | Duplicate phone and email error: `This phone number and email address are already used by existing contacts.` |
| `add n/No Role p/81005555 e/norole@example.com a/Test Address` | Missing parameter error: `Missing mandatory parameter(s): r/.` |
| `add n/David s/o Muthu p/81008888 e/david.unescaped@example.com a/Test Address r/Supplier` | Unescaped slash is recognized as an unknown prefix: `Unrecognized prefix(es) provided: s/.` |
| `add n/Bad Phone p/12 e/badphone@example.com a/Test Address r/Supplier` | Phone number constraint error: `Phone numbers should only contain digits, and should be at least 3 digits long.` |

### Undoing and redoing commands with `undo` and `redo`

Use a disposable application copy in a separate folder.
Run `clear` to remove all contacts, then add one baseline contact:

```text
add n/Alice p/81000001 e/alice@undo.example.com a/Test Address r/Employee
```

Run the commands below in order. Where a row contains several commands, enter them separately.

| Command | Expected result |
|---------|-----------------|
| `delete 1` | Alice is deleted; the contact list becomes empty. |
| `undo` | `Undo success!` Alice is restored at index 1. |
| `redo` | `Redo success!` Alice is deleted again. |
| `undo` | `Undo success!` Alice is restored at index 1. |
| `add n/Bob p/81000002 e/bob@undo.example.com a/Test Address r/Supplier` | Bob is added at index 2. |
| `followup 2 by/2026-12-01 abt/Inventory check` | Bob's follow-up is set. The Follow-up badge appears on Bob's card. |
| `undo` | `Undo success!` Bob's follow-up is removed; Alice and Bob remain. |
| `undo` | `Undo success!` Bob is removed; only Alice remains. |
| `redo` | `Redo success!` Bob is restored at index 2. |
| `redo` | `Redo success!` Bob's follow-up is restored. |
| `undo` | `Undo success!` Bob's follow-up is removed again. |
| `add n/Charlie p/81000003 e/charlie@undo.example.com a/Test Address r/Service Provider` | Charlie is added. This modifying command purges the redo branch. |
| `redo` | `No more commands to redo!` Redo history was cleared when Charlie was added. |
| `find Alice`, `list`, then `help` | Filter shows Alice, list restores all contacts, help window opens. These non-modifying commands do not change undo history. |
| `undo` | `Undo success!` Charlie is removed; Alice and Bob remain. |
| `undo` | `Undo success!` Bob is removed; only Alice remains. |
| `undo` | `Undo success!` Alice is removed; the list becomes empty (reversing baseline setup). |
| `undo` | `No more commands to undo!` |
| `undo 2`, then `redo 2` | Each is rejected with invalid command format; history and contacts remain unchanged. |

### Viewing contextual help with `help`

Start with the application open. Commands can be run in any order.

| Command | Expected result |
|---------|-----------------|
| `help add` | Result display shows `add` command description, parameters, and example. No popup window opens. |
| `help followup` | Result display shows `followup` command description, parameters, and example. No popup window opens. |
| `help select` | Result display shows `select` command description, parameters, and example. No popup window opens. |
| `help sort` | Result display shows `sort` command description, parameters, and example. No popup window opens. |
| `help exit` | Result display shows `exit` command description without parameters. No popup window opens. |
| `help` | The full Help window opens, displaying all commands grouped by category (`Manage contacts`, `Browse contacts`, `Undo and redo`, `Application`). Result display shows `Opened help window.`. |
| `help unknowncommand` | Result display shows `No command-specific help is available for 'unknowncommand'.` and lists supported commands. No window opens. |
| `help add extra` | Syntax error: `Specify only one command after help.`. No window opens. |

### Saving data

1. Dealing with missing/corrupted data files

   1. Missing data file
      1. Prerequisites: Close the application. Delete `data/addressbook.json` if it exists.
      1. Test case: Launch the application.<br>
         Expected: The application starts normally and creates a new `data/addressbook.json` file populated with sample contact data. No backup file is created.

   1. Corrupted data file
      1. Prerequisites: Close the application. Open `data/addressbook.json` in a text editor and corrupt its content (e.g., enter `{ invalid json content }` or delete required closing brackets).
      1. Test case: Launch the application.<br>
         Expected: The application starts with an empty contact list. A timestamped backup file named `data/addressbook.json.backup.YYYYMMDD_HHMMSS` (or with a `_1`, `_2` suffix if a file with the same name already exists) is created in the `data/` folder with the exact corrupted content. A warning message appears in the result display stating that the file is corrupted, identifying the backup path, and indicating that the application started with an empty contact book.

   1. Corrupted data file with failed backup (e.g., write-protected folder)
      1. Prerequisites: Close the application. Corrupt `data/addressbook.json` and make the `data/` directory read-only to prevent writing new files.
      1. Test case: Launch the application.<br>
         Expected: The application starts with an empty contact list. A warning message appears in the result display stating that the data file is corrupted and creating a backup failed.

1. _{ more test cases …​ }_
