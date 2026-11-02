---
layout: page
title: Kieran M's Project Portfolio Page
---

### Project: KeyBossWarriorsPro

KeyBossWarriorsPro is a desktop contact management application tailored for Artisan Mechanical Keyboard Shop Managers. It combines a fast Command Line Interface (CLI) with a JavaFX Graphical User Interface (GUI).

Given below are my contributions to the project.

* **Major Feature: Contact Role Classification & Status Badges**
  * **What it does**: Allows managers to assign mandatory business roles (`Employee`, `Supplier`, `Service Provider`) to contacts, rendered as dynamic color-coded badges on contact cards with auto-resizing.
  * **Justification**: Enables immediate visual recognition of suppliers and staff without opening detailed views.
  * **Highlights**: End-to-end integration across Model (`Role`), Storage (JSON serialization), Logic (parsing, normalization), and UI (`PersonCard` dynamic badge styling and squashing protection).
  * **Pull requests**: [\#32](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/32), [\#44](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/44), [\#94](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/94), [\#120](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/120), [\#204](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/204), [\#221](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/221)

* **Major Feature: Multi-Field Identity & Duplicate Detection**
  * **What it does**: Overhauled contact uniqueness from name-only matching to phone number and email address uniqueness, with granular reporting for phone, email, or joint collisions.
  * **Justification**: Contacts in business operations may legitimately share names, but communication channels must remain unique.
  * **Highlights**: Refactored core identity checks in `Person`, `UniquePersonList`, and `ModelManager`, updating `add` and `edit` execution pathways.
  * **Pull requests**: [\#47](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/47), [\#54](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/54), [\#71](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/71)

* **Code contributed**: [RepoSense link](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=Kimame04)

* **Project management**:
  * Set up team website and repository structure ([\#7](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/7)).
  * Managed milestones (`v1.1` to `v1.6`) and issue tracking on GitHub.
  * Configured CI for headless test execution ([\#34](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/34)) and enabled build assertions ([\#77](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/77)).

* **Enhancements to existing features**:
  * **UI Theming**: Harmonized dark theme styling, contrast, and typography across all windows ([\#201](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/201)).
  * **Parser Validation**: Added unrecognized prefix rejection, missing parameter feedback, and extra argument warnings for `clear` and `followups` ([\#47](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/47), [\#71](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/71), [\#166](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/166), [\#172](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/172)).
  * **Defensive Integrity**: Added null checks for Tag deserialization ([\#95](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/95), [\#97](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/97)) and prevented redundant undo state commits on empty clear ([\#169](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/169)).

* **Documentation**:
  * **User Guide**: Documented `r/ROLE` syntax and badge behavior ([\#44](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/44)), duplicate detection rules ([\#54](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/54)), and application branding ([\#67](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/67), [\#73](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/73), [\#170](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/170)).
  * **Developer Guide**: Authored PlantUML class diagrams for `Role` ([\#120](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/120)) and `FollowUp` ([\#217](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/217)), `AddCommand` diagrams ([\#148](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/148)), and `FollowUpSequenceDiagram` ([\#243](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/243)); authored Use Cases UC8–UC12 ([\#244](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/244)), manual testing instructions for `add`, `undo`/`redo`, and `help` ([\#245](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/245)), and NFR11 ([\#19](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/19), [\#101](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/101)).

* **Community**:
  * PRs reviewed (with non-trivial review comments): [\#104](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/104), [\#140](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/140), [\#210](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/210), [\#227](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/227), [\#228](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/228)
  * Participated in team discussions and issue triaging.
