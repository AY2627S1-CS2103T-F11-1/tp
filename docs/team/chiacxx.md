---
layout: page
title: Xavier Chia's Project Portfolio Page
---

### Project: KeyBossWarriorsPro

KeyBossWarriorsPro is a desktop contact management application for artisan mechanical keyboard shop managers.
It combines keyboard commands with a JavaFX interface to help users organise employees, suppliers and service providers.
The project builds on [AddressBook-Level 3](https://github.com/se-edu/addressbook-level3) .

Given below are my contributions to the project.

* **Code contributed**: [RepoSense code dashboard](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=chiacxx&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other)

* **New feature: Contact sorting**
  * **What it does**: Sorts contacts by name, role, or both in a user-selected priority. Comparisons ignore letter case. `sort clear` restores insertion order.
  * **Justification**: Helps shop managers browse contacts alphabetically or group suppliers and employees by role while using the same displayed indices for subsequent commands.
  * **Highlights**: Implemented `SortCommand`, `SortCommandParser` and command registration. Integrated JavaFX's `SortedList` around the existing filtered view, keeping stored contact order unchanged. The displayed order updates as contacts are added, edited, deleted or filtered, and remains consistent with index-based commands and undo/redo. Added specific syntax errors, defensive copying of sort criteria, empty-book handling and sort reset.
  * **Pull requests**: [#142](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/142), [#145](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/145), [#191](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/191).

* **Enhancement: Prefixed contact searches**
  * **What it does**: Extends the existing `find` command with name (`n/`), role (`r/`) and tag (`t/`) search phrases, while preserving quick name searches without prefixes. Repeated values within a field use OR; different fields use AND.
  * **Justification**: Lets managers locate contacts using business roles or supplied components, and combine criteria to narrow results. Prefixed values also support multiword phrases without treating each word as a separate keyword.
  * **Highlights**: Added prefixed name parsing, `RoleContainsKeywordsPredicate`, `TagContainsKeywordsPredicate` and composition of multiple field predicates in `FindCommand`. The parser creates one predicate per supplied field, retains repeated values, and rejects missing criteria, blank values, unsupported prefixes and mixed search formats. Added phrase normalisation and tests for partial matches, case handling and combined criteria.
  * **Pull requests**: [#107](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/107), [#109](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/109), [#114](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/114).

* **Enhancement: Delete validation and feedback**
  * **What it does**: Improves the existing `delete` command with distinct feedback for missing or extra arguments, invalid indices, empty displayed lists and out-of-range indices.
  * **Justification**: Helps users correct mistakes and identify which displayed contact will be deleted, including after filtering or sorting.
  * **Highlights**: Separated parser errors from execution errors, checked integer boundaries and overflow, and provided valid-index or range feedback. Added tests for filtered lists and unchanged data after rejected requests. Later consolidated shared index diagnostics for other commands.
  * **Pull requests**: [#34](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/34), [#56](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/56), [#202](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/202), [#212](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/212).

* **Other enhancements to existing features**:
  * Added slash escaping for text fields and searches. Decoding happens after prefix recognition, so users can enter values such as `David s\/o John` without introducing an unintended prefix. Saved values are not decoded again on reload ([#225](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/225)).
  * Broadened contact text validation to support Unicode, punctuation and literal backslashes. Added shared spacing and case-normalisation helpers, aligned prefixed search normalisation with stored fields, adopted the WHATWG HTML email pattern, and limited phones to 3–20 digits ([#260](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/260)). The email pattern is credited to the [WHATWG HTML standard](https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address).
  * Improved `list` feedback for empty books and additional arguments ([#65](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/65)), and removed redundant follow-up date error text ([#211](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/211)).

* **Testing**:
  * Wrote parser, command, predicate and model tests for sorting and prefixed searches. Cases cover empty and single-contact lists, field priority and ties, repeated criteria, invalid inputs, null arguments and defensive copies.
  * Added integration tests for sort interactions with filtering, editing, deletion, undo/redo and saved contact order ([#145](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/145)).
  * Added regression tests for escaped values through add/edit/search and JSON reload, including tag deduplication and conflicting tag operations ([#225](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/225)).
  * Extended equivalence-partition and boundary tests for Unicode text, email syntax, phone lengths and decoded/normalised values ([#260](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/260)).

* **Documentation**:
  * **User Guide**: Documented [sorting](../UserGuide.md#sorting-all-contacts-sort), [prefixed and quick name searches](../UserGuide.md#locating-contacts-find), and [deletion](../UserGuide.md#deleting-a-contact-delete), including command syntax, examples and error handling. Updated [input rules](../UserGuide.md#contact-input-rules), [slash escaping](../UserGuide.md#slash-escaping) and validation limitations. Relevant PRs: [#154](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/154), [#227](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/227), [#228](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/228), [#260](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/260).
  * **Developer Guide**: Explained [sort implementation and design choices](../DeveloperGuide.md#sorting-contacts), [find parsing and predicate composition](../DeveloperGuide.md#finding-contacts), and [delete validation and execution](../DeveloperGuide.md#deleting-a-contact). Updated related use cases and manual testing instructions, including simplifying the sort tests ([#262](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/262)).
  * **UML diagrams**: Added the [sort model view diagram](../images/SortModelClassDiagram.png), updated the [delete sequence diagram](../images/DeleteSequenceDiagram.png), and added the [find activity](../images/FindActivityDiagram.png) and [find sequence](../images/FindSequenceDiagram.png) diagrams ([#154](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/154), [#125](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/125), [#227](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/227)).

* **Team contributions and reviews**:
  * Consolidated duplicate command-summary entries and resolved Developer Guide merge-conflict leftovers ([#203](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/203), [#165](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/165)).
  * Reviewed the UI/selection changes in #210, commenting on [reuse of shared index diagnostics](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/210#discussion_r4411463257) and [screenshot consistency](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/210#discussion_r4411477503).
  * Reviewed incremental tag editing and suggested an end-to-end test for successful multiword tag deletion ([#116 review](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/116#pullrequestreview-5608497544)).
  * Reviewed keyboard navigation, scrolling and Enter-key activation in the help window ([#192 review](https://github.com/AY2627S1-CS2103T-F11-1/tp/pull/192#pullrequestreview-5687183086)).
