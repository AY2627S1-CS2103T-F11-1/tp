---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-F11-1/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F11-1/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F11-1/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
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
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

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

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

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
| `* * *`  | artisan keyboard shop manager | delete outdated contacts by index or identifier                                            | have a contact list that stays relevant.                                  |
| `* * *`  | artisan keyboard shop manager | edit contact details                                                                       | reach the right people using updated communication details.               |
| `* * *`  | artisan keyboard shop manager | list all contacts in a compact table                                                       | quickly review staff, suppliers and service providers.                    |
| `* * *`  | artisan keyboard shop manager | tag contacts as employees, suppliers, or service providers                                 | distinguish staff from external partners.                                 |
| `* * *`  | artisan keyboard shop manager | search contacts by full or partial names regardless of letter case                         | quickly find a particular contact's details.                              |
| `* * *`  | artisan keyboard shop manager | filter contacts by component tags                                                          | find alternative suppliers when stock is low.                             |
| `* * *`  | artisan keyboard shop manager | contact and tag changes to be saved automatically to a local JSON file                     | persist changes between sessions.                                         |
| `* * *`  | artisan keyboard shop manager | load saved contacts automatically at startup                                               | begin work without re-entering all details.                               |
| `* * *`  | artisan keyboard shop manager | view command syntax and parameters using the help command                                  | learn commands within the application.                                    |
| `* * *`  | artisan keyboard shop manager | exit the application using a CLI command                                                   | exit gracefully while keeping my terminal open.                           |
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
| `*`      | artisan keyboard shop manager | customize CLI theme colors                                                                 | personalize my workspace                                                  |
| `*`      | artisan keyboard shop manager | check shipment tracking from a supplier's contact card using a tracking code               | monitor incoming product deliveries.                                      |
| `*`      | artisan keyboard shop manager | track past unit-price quotes for products                                                  | compare prices and negotiate better deals.                                |

### Use cases

(For all use cases below, the **System** is the `KeyBossWarriorsPro` and the **Actor** is the `user`, unless specified otherwise)

**Use Case: UC1 - Add a Contact**

**MSS**

1.  User requests to add a contact, providing its name, phone number, email, address, role, and any optional tags.
2.  `KeyBossWarriorsPro` adds the contact and confirms the addition.

    Use case ends.

**Extensions**

*  1a. A required field is missing or a value is invalid.

  *  1a1. `KeyBossWarriorsPro` shows an error message.
  
     Use case ends.

*  1b. The phone number or email belongs to an existing contact.

  *  1b1. `KeyBossWarriorsPro` shows an error message.
  
    Use case ends.


**Use Case: UC2 - Find a Contact**

**MSS**

1.  User requests to find contacts using a name keyword.
2.  `KeyBossWarriorsPro` displays matching contacts and their details.

    Use case ends.

**Extensions**

*  2a. No contacts match the keyword.

  *  2a1. `KeyBossWarriorsPro` informs the user that no contacts were found.

     Use case ends.


**Use Case: UC3 - Delete a Contact**

**MSS**

1.  User requests to list contacts.
2.  `KeyBossWarriorsPro` shows a list of contacts.
3.  User requests to delete a specific person in the contact list.
4.  `KeyBossWarriorsPro` deletes the contact.

    Use case ends.

**Extensions**

*  2a. The list is empty.

   Use case ends.

*  3a. The given index is invalid.

  *  3a1. `KeyBossWarriorsPro` shows an error message.

     Use case resumes at step 3.


**Use Case: UC4 - Tag a Contact**

**MSS**

1.  User requests to find contacts using a name keyword.
2.  `KeyBossWarriorsPro` displays matching contacts and their details.
3.  User requests to add one or more tags to a contact.
4.  `KeyBossWarriorsPro` adds the tags and displays the update contact information.

    Use case ends.

**Extensions**

*  2a. No contacts match the keyword.

  *  2a1. `KeyBossWarriorsPro` informs the user that no contacts were found.

     Use case ends.
 
*  3a. No tag is provided.
 
  * 3a1. `KeyBossWarriorsPro` shows an error message.

       Use case resumes at step 3.

*  3b. Duplicate tags are provided.

  *  3b1. `KeyBossWarriorsPro` retains one instance of each tag.
  
     Use case resumes at step 4.


**Use Case: UC5 - List Contacts**

**MSS**

1.  User requests to list contacts.
2.  `KeyBossWarriorsPro` shows a list of contacts.
    
    Use case ends.

**Extensions**

*  2a. The list is empty.

  *  2a1. `KeyBossWarriorsPro` informs the user that no contacts were found.

     Use case ends.

### Non-Functional Requirements

1.  **Operating Environment:** Should work on any _mainstream OS_ (Windows 10/11, macOS, Linux) as long as it has Java `25` or above installed.
2.  **Portability:** The application should be packaged as a single standalone executable JAR file that runs without requiring an installer.
3.  **Network Independence:** Should function fully offline without requiring an active internet connection or any remote server.
4.  **Capacity:** Should be able to store and manage up to 1,000 contacts and 5,000 tags without noticeable sluggishness in performance for typical usage.
5.  **Responsiveness:** All standard commands (such as `list`, `filter`, `find`, `add`, `delete`) should respond and update the interface within 1.0 second under normal workloads.
6.  **Package Size:** The packaged application JAR file should not exceed 100 MB.
7.  **Data Persistence:** Data must be stored in a human-editable text file (JSON format) located at `data/keyboss.json`.
8.  **Fault Tolerance:** If the data file is corrupted or formatted incorrectly, the application should start gracefully (e.g., discard or isolate corrupted state and notify the user) without unexpected crashes.
9.  **Database Independence:** The system should not depend on a Relational Database Management System (RDBMS) like MySQL or SQLite.
10. **Typing-Preferred Usability:** A user with above-average typing speed for regular English text should be able to accomplish all routine contact and inventory management tasks faster using CLI commands than using a mouse.
11. **Display Adaptability:** The user interface should remain fully functional and legible on screen resolutions of 1280x720 and above, across display scaling factors of 100% to 150%.
12. **Single-User Scope:** The software is designed for a single artisan keyboard shop manager working locally; concurrent multi-user access is out of scope.

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
* **Primary role**: The required top-level classification assigned to a contact (`employee`, `supplier`, or `service provider`). Every contact has exactly one primary role.
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

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
