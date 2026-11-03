---
layout: page
title: Noah's Project Portfolio Page
---

### Project: KeyBossWarriorsPro

KeyBossWarriorsPro is a desktop contact management application for artisan mechanical keyboard shop managers. It combines keyboard commands with a JavaFX graphical interface to help users track employees, suppliers, and service providers. The application is written in Java and evolves AddressBook-Level 3.

Given below are my contributions to the project.

* **Code contributed**: [RepoSense code dashboard](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=Noyoth&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other)

* **Major feature: Corrupted file backup and diagnostic warnings**
  * **What it does**: Automatically creates a timestamped copy of `addressbook.json` and `preferences.json` if loading fails on startup, then starts the application with empty or default data. It reports the corruption reason and backup path in a startup status banner. If both files fail simultaneously, it shows both warnings. If a backup file with the same timestamp already exists, it appends an incrementing counter (`_1`, `_2`).
  * **Justification**: Prevents silent data loss when JSON files contain syntax errors, null values, or duplicate records. Users keep their existing records in a backup instead of losing them when the application saves new data.
  * **Implementation**: Implemented `FileUtil.createTimestampedBackup` to share collision-safe backup logic between data and preference storage. Updated `MainApp` to extract root-cause messages, including specific duplicate contact diagnostics (conflicting names, shared phones, or shared emails), and display them in the UI status banner.
  * **Pull requests**: [#37](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/37), [#52](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/52), [#258](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/258), [#259](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/259).

* **Major feature: Atomic file persistence**
  * **What it does**: Writes data to a temporary file in the destination directory and replaces the target file using an atomic move operation.
  * **Justification**: Direct file writes can leave behind a truncated or empty file if the application is closed during a save, power cuts out, or the disk write is interrupted.
  * **Implementation**: Implemented `FileUtil.writeToFile` using `Files.createTempFile` and `StandardCopyOption.ATOMIC_MOVE`, with fallback to standard replacement on file systems that do not support atomic moves. Cleaned up temporary files when writes fail. Added a package-private overload taking a `FileMover` interface for deterministic fault injection during testing.
  * **Pull requests**: [#60](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/60).

* **Major feature: Undo and redo with state rollback**
  * **What it does**: Reverses (`undo`) and reapplies (`redo`) mutating address book commands. If saving to disk fails after a command runs, it rolls back the in-memory address book state so the user interface matches what is stored on disk.
  * **Justification**: Allows shop managers to recover from mistaken deletions or edits. Prevents in-memory contacts from diverging from the saved file when disk writes fail.
  * **Implementation**: Built `VersionedAddressBook` to manage a history list of address book states and an active pointer. Added `UndoCommand`, `RedoCommand`, and their zero-argument parsers. Added `rollbackCommit()` in `VersionedAddressBook` and integrated it with `LogicManager` to undo changes when disk persistence throws an `IOException`. Staged discarded redo states during `commit()` so an immediate rollback restores them without history loss.
  * **Pull requests**: [#106](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/106), [#117](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/117), [#118](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/118), [#196](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/196), [#251](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/251).

* **Enhancements to existing features**:
  * Handled null root objects in `JsonUtil.readJsonFile`, throwing `DataLoadingException` instead of allowing an unhandled `NullPointerException` ([#250](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/250)).
  * Enforced non-null `GuiSettings` in `UserPrefs` using `@JsonCreator`, preventing crashes during equality checks and stopping null values from persisting on disk ([#250](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/250)).
  * Added validation in `JsonSerializableAddressBook` to reject null person array entries ([#194](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/194)).
  * Added keyboard focus, arrow-key scrolling, and Enter-key activation to the Help window command list and URL hyperlink button ([#192](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/192)).

* **Testing**:
  * Wrote unit tests for atomic file persistence in `FileUtilTest`, covering regular writes, parent directory creation, and temp file cleanup on write failure.
  * Wrote unit and integration tests for `VersionedAddressBook`, `UndoCommand`, and `RedoCommand`, covering single-step and multi-step navigation, filtered lists, and boundary conditions.
  * Added tests in `LogicManagerTest` to verify that disk write failures roll back the model and notify the user.
  * Added tests in `MainAppTest`, `JsonAddressBookStorageTest`, and `JsonUserPrefsStorageTest` verifying corrupted file detection, backup generation, collision numbering, and combined warning banners.

* **Documentation**:
  * **User Guide**: Documented corrupted file backups, manual recovery procedures, and atomic saving guarantees.
  * **Developer Guide**: Authored the storage component architecture section, detailing atomic saving and backup generation mechanisms. Added domain and technical terms to the glossary ([#26](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/26)).
  * **UML diagrams**: Created PlantUML sequence diagrams for atomic file saving (`SaveAddressBookSequenceDiagram.puml`) and corrupted data file backups (`BackupAddressBookSequenceDiagram.puml`) ([#130](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/130)).

* **Team contributions and reviews**:
  * Maintained storage component architecture, file persistence models, and related team documentation.
  * Collaborated on integrating `VersionedAddressBook` across Model and Logic components, coordinating with teammates implementing mutating commands ([#108](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/108), [#115](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/115)).
  * Helped integrate teammates' changes and resolve merge conflicts across feature branches, including undo/redo state committing, sorting documentation, and shared validation messages.
  * Reviewed teammates' PRs covering role management and validation ([#32](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/32), [#94](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/94)), duplicate contact detection ([#54](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/54)), command validation and index diagnostics ([#56](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/56), [#202](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/202), [#211](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/211)), and sorting documentation ([#154](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/154)).
