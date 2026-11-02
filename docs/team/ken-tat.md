---
layout: page
title: Ken Tay's Project Portfolio Page
---

### Project: KeyBossWarriorsPro

KeyBossWarriorsPro is a desktop contact management application tailored for artisan mechanical keyboard shop managers. It combines a fast Command Line Interface (CLI) with a JavaFX Graphical User Interface (GUI).

Given below are my contributions to the project.

* **Code contributed**: [RepoSense code dashboard](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=Ken-Tat&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other)

* **Major Feature: Contact Selection and Details Panel**
  * **What it does**: Adds a resizable split layout with a compact contact list and a scrollable panel displaying the selected contact's complete information, tags and follow-up details. Users can select contacts with the mouse, arrow keys or `select INDEX`.
  * **Justification**: Lets managers browse contacts quickly while reading complete details, and supports the application's keyboard-first workflow.
  * **Highlights**: Integrated selection across parsing, command results, logic and JavaFX. Preserved selection across contact replacements and sorting, cleared it when the contact was removed or filtered out, and handled undo/redo. Added validation against displayed indices, scrolling to selected contacts, and Escape navigation back to command input while retaining the draft. Regression tests cover selection, focus, scrolling and storage-failure interactions.
  * **Pull requests**: [#206](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/206), [#208](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/208).

* **Major Enhancement: Incremental Tag Editing**
  * **What it does**: Changes `edit` to add tags with `t/TAG` and selectively delete tags with `dt/TAG`, preserving other tags. Both operations can be combined with contact-field edits in one command.
  * **Justification**: Managers can update a contact's capabilities or categories without retyping every tag they want to keep.
  * **Highlights**: Separated additions and deletions in the edit descriptor, with defensive copies and unmodifiable getters. Rejected blank values, conflicting operations and deletion of absent tags. Ensured validation failures preserve all contact fields, tags and the displayed list. Fixed locale-dependent tag hashing and tested case-insensitive matching, deduplication and complete command workflows.
  * **Pull request**: [#116](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/116).

* **Major Enhancement: Command-Specific Help**
  * **What it does**: Expands the Help window with command guidance and supports `help COMMAND` for targeted usage information.
  * **Justification**: Helps managers learn commands and correct syntax without repeatedly leaving the application to consult the User Guide.
  * **Highlights**: Refactored command descriptions and usage information into shared sources used by help and validation feedback. Added supported-command lookup, guidance for unsupported requests, keyboard navigation and tests for help content, formatting and User Guide link actions. Updated help for newly introduced commands.
  * **Pull requests**: [#40](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/40), [#62](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/62), [#167](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/167).

* **Other enhancements to existing features**:
  * **Follow-up visibility**: Added email summaries and follow-up urgency badges to contact cards. Implemented a shared date tracker to refresh Due today and Overdue indicators as the date changes or the window regains focus, while preserving selection, scrolling and draft input ([#210](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/210)).
  * **Role editing**: Extended the edit descriptor, parser, command usage and tests to support changing a contact's role ([#64](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/64)).
  * **Input validation**: Fixed invisible-character handling in addresses and follow-up descriptions, with regression coverage across parsers, model values, logic and JSON storage ([#198](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/198)).

* **Testing**:
  * Added unit, integration and regression tests for incremental tag edits, role editing, help and contact selection, including rejected-input cases and preservation of existing state.
  * Added JavaFX tests for the Help window, details panel, keyboard focus and contact-list scrolling. Tested selection through the parser, logic, storage and UI pipeline ([#206](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/206), [#208](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/208)).
  * Used controllable clocks to test follow-up urgency at date boundaries and window-focus transitions ([#210](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/210)).

* **Documentation**:
  * **User Guide**: Documented incremental tag editing, role editing, command-specific help, contact selection and the details panel in the corresponding feature PRs. Refreshed screenshots to match the application ([#256](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/256)). Proposed a guide-wide revision to standardise command entries, examples, feedback, known issues and the command summary ([#270](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/270), awaiting merge at the time of writing).
  * **Developer Guide**: Documented incremental tag editing, help, selection reconciliation, the details panel and urgency refresh. Reorganised the UI overview and selection documentation ([#254](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/254)). Proposed a wider revision covering implementation structure, shared technical explanations, persistence and failure behaviour, requirements and manual testing instructions ([#272](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/272), awaiting merge at the time of writing).
  * **UML diagrams**: Added and updated the help activity diagram ([#132](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/132), [#249](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/249)), and updated the UI class diagram for the details panel and selection ([#206](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/206)). The proposed Developer Guide revision also updates existing architecture, logic, model, parser, command, persistence and undo/redo diagrams to match the implementation ([#272](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/272)).

* **Team contributions and reviews**:
  * Updated the README, product UI mockup and build-status badge to reflect KeyBossWarriorsPro ([#22](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/22)).
  * Helped integrate teammates' changes and resolve merge conflicts across feature branches, including follow-up functionality, sorting, shared validation messages and UI changes.
  * Reviewed teammates' PRs covering sorting ([#142](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/142), [#145](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/145)), follow-ups ([#146](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/146), [#149](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/149), [#151](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/151), [#152](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/152)), UI theming ([#201](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/201)) and shared index feedback ([#202](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/202), [#212](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/212)).
