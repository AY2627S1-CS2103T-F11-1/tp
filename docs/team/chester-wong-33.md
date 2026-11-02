---
layout: page
title: Chester Wong's Project Portfolio Page
---

### Project: KeyBossWarriorsPro

KeyBossWarriorsPro is a desktop contact management application tailored for Artisan Mechanical Keyboard Shop Managers. It combines a fast Command Line Interface (CLI) with a JavaFX Graphical User Interface (GUI).

Given below are my contributions to the project.

* **Major Feature: Contact Follow-Up Management**
  * **What it does**: Allows managers to create, replace, and clear a contact's follow-up using `followup`, with a due date and description. The `followups` command lists all contacts with follow-ups, overdue follow-ups, or follow-ups due on or before a specified date.
  * **Justification**: Helps managers track supplier quotations, service arrangements, and other business commitments directly alongside their contacts.
  * **Highlights**: Integrated follow-ups into the contact model, JSON storage, and edit workflow; implemented command parsing, follow-up queries, UI display, help text, and unit tests.
  * **Pull requests**: [\#146](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/146), [\#149](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/149), [\#151](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/151), [\#152](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/152)

* **Major Feature: Undo/Redo Integration and Testing**
  * **What it does**: Integrates version history into the model and records successful contact changes so that they can be undone and redone.
  * **Justification**: Allows managers to recover from accidental additions, edits, deletions, or clearing of contacts.
  * **Highlights**: Extended the `Model` contract and `ModelManager` implementation, added history commits after successful mutating commands, and expanded model and integration tests. This work integrated the undo and redo commands implemented by teammates.
  * **Pull requests**: [\#108](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/108), [\#115](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/115), [\#121](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/121)

* **Code contributed**: [RepoSense link](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=chester-wong-33)

* **Project management**:
  * Merged team contributions into the main branch and resolved integration conflicts across command parsing, version-history tests, and documentation.

* **Enhancements to existing features**:
  * **Name Search**: Added case-insensitive substring matching and excluded blank search keywords, with tests for the updated behavior ([\#58](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/58), [\#83](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/83)).
  * **Exit Feedback**: Added a one-second shutdown delay so users can read the exit acknowledgement, and feedback explaining that extra arguments are ignored. Added command, parser, and UI integration tests ([\#41](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/41), [\#49](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/49)).
  * **Command Feedback and Integration**: Clarified invalid-index feedback and updated parser tests ([\#193](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/193)); removed duplicate redo handling in `AddressBookParser` and redundant test cases ([\#124](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/124)).

* **Documentation**:
  * **User Guide**: Documented undo/redo behavior ([\#122](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/122)), follow-up commands ([\#153](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/153)), and sort and follow-up workflow examples ([\#195](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/195)).
  * **Developer Guide**: Added the target user profile, value proposition, and user stories ([\#29](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/29)); documented undo/redo integration and updated its diagrams ([\#122](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/122), [\#127](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/127), [diagram update](https://github.com/AY2627S1-CS2103T-F11-1/tp/commit/4db10c8d)); contributed follow-up documentation ([\#153](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/153)), corrected a storage filename reference ([\#248](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/248)), and added acknowledgements ([\#257](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/257)).
  * **Product Website**: Updated the key features section to reflect the implemented application capabilities ([\#247](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/247)).

* **Community**:
  * Helped integrate teammates' changes and resolve merge conflicts, including undo/redo model tests and Developer Guide updates.
