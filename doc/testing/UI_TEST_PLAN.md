# JExam Manual UI Testing Plan

**Testing Date**: 07.04.2026  
**Tester**: Moritz
**Test Environment**: Windows, Java 17+, JExam commit 6b8b1842d935f1c2972c961da8458a949a4fe7c9

---

## Overview
This test plan validates the recently simplified chapter goal point configuration UI:
- **Inline per-chapter goal fields** (editable ComboBox dropdown)
- **Static achievement-based suggestions** (no live filtering)
- **Tab selection & reload behavior** (single-click activation)
- **Removed features**: live suggestion filtering, infeasible-target fallback UI selector

---

## Test Phases

### Phase 1: Tab Activation & Content Reload
**Goal**: Verify PDF tab activates on first click and chapters load correctly

| # | Test Case | Steps | Expected Outcome | Status | Notes |
|---|-----------|-------|------------------|--------|-------|
| 1.1 | PDF tab single-click activation | Open app → Click "PDF" tab | PDF tab opens immediately (no second click needed) |  |  |
| 1.2 | XML tab single-click return | On PDF tab → Click "XML" tab | XML tab opens immediately |  |  |
| 1.3 | Chapter reload after XML open | 1. Open XML file with 3 chapters 2. Switch to PDF tab | Chapters appear in left panel; chapter names & order match XML |  |  |
| 1.4 | Chapter list updates on new XML load | 1. Load exam A (3 chapters) in XML tab 2. Load exam B (4 chapters) in XML tab 3. Switch to PDF tab | Chapter list shows exam B's 4 chapters (not stale exam A) |  |  |

---

### Phase 2: Chapter Goal Point Fields - Basic Input
**Goal**: Verify typing and dropdown selection work correctly

| # | Test Case | Steps | Expected Outcome | Status | Notes |
|---|-----------|-------|------------------|--------|-------|
| 2.1 | Dropdown shows achievable points | 1. Load exam with chapters 2. Click dropdown for chapter "Introduction" | Dropdown list shows 2-3 point values (e.g., 0.5, 1.0, 1.5) |  |  |
| 2.2 | Select from dropdown | 1. Click dropdown 2. Click "1.0" | Field shows "1.0"; value is committed |  |  |
| 2.3 | Type numeric value | 1. Click field text area 2. Type "2.5" 3. Press Enter | Field shows "2.5"; value is committed |  |  |
| 2.4 | Type comma as decimal separator | 1. Click field 2. Type "1,5" 3. Press Enter | Field commits (internally stores 1.5); displays as "1.5" (period) |  |  |
| 2.5 | Type decimal with period | 1. Click field 2. Type "1.5" 3. Press Enter | Field shows "1.5"; committed |  |  |

---

### Phase 3: Chapter Goal Point Fields - Advanced Input & Snapping
**Goal**: Verify snap-to-nearest and error handling

| # | Test Case | Steps | Expected Outcome | Status | Notes |
|---|-----------|-------|------------------|--------|-------|
| 3.1 | Snap to nearest achievable (higher) | Achievable: [1.0, 1.5]. 1. Type "1.2" 2. Press Enter | Field snaps to "1.5" (nearest valid value) |  |  |
| 3.2 | Snap to nearest achievable (lower) | Achievable: [1.0, 1.5]. 1. Type "1.1" 2. Press Enter | Field snaps to "1.0" (nearest lower) |  |  |
| 3.3 | Snap to exact achievable | Achievable: [0.5, 1.0, 1.5]. 1. Type "1.0" 2. Press Enter | Field shows "1.0" (no change) |  |  |
| 3.4 | Reject zero input | 1. Type "0" 2. Press Enter | Error dialog appears: "Goal points must be positive"; field reverts to prior value |  |  |
| 3.5 | Reject negative input | 1. Type "-1.5" 2. Press Enter | Error dialog appears; field reverts |  |  |
| 3.6 | Non-numeric input silent revert | 1. Type "abc" 2. Press Enter | Field silently reverts to last committed value; no error dialog |  |  |
| 3.7 | Empty field revert | 1. Clear field (delete all text) 2. Press Enter | Field reverts to last committed value |  |  |
| 3.8 | Escape cancels edit | 1. Type "2.5" (different from current) 2. Press Escape | Field reverts to prior committed value; dropdown closes |  |  |

---

### Phase 4: Removed Features - Absence Verification
**Goal**: Confirm removed UI elements are gone

| # | Test Case | Steps | Expected Outcome | Status | Notes |
|---|-----------|-------|------------------|--------|-------|
| 4.1 | No live suggestions while typing | 1. Click field 2. Type "1." slowly 3. Watch dropdown | Dropdown list **does NOT change** as you type (stays static) |  |  |
| 4.2 | No fallback preference selector | Examine GenerationControls (top-left above chapter list) | **No dropdown/combo** labeled "If chapter target is infeasible" |  |  |
| 4.3 | No "Updating..." status | While modifying goals | **No status text** saying "Updating list" or similar |  |  |

---

### Phase 5: Drag-Drop & Chapter Reorder
**Goal**: Verify chapter reordering still works after simplification

| # | Test Case | Steps | Expected Outcome | Status | Notes |
|---|-----------|-------|------------------|--------|-------|
| 5.1 | Drag chapter to new position | 1. Load exam with 3+ chapters 2. Drag chapter row up/down | Chapter moves; order persists in preview |  |  |
| 5.2 | Reorder persists in export | 1. Reorder chapters 2. Export PDF | PDF has chapters in reordered sequence |  |  |

---

### Phase 6: Chapter Inclusion/Exclusion
**Goal**: Verify exclude/include behavior and goal persistence

| # | Test Case | Steps | Expected Outcome | Status | Notes |
|---|-----------|-------|------------------|--------|-------|
| 6.1 | Exclude chapter (checkbox) | 1. Check included chapter's exclude checkbox 2. Watch preview | Chapter disappears from preview; goal field disabled/grayed |  |  |
| 6.2 | Re-include restores goal | 1. Set goal to "2.5" for chapter 2. Uncheck (exclude) 3. Check (re-include) | Goal field reappears with "2.5" still set |  |  |
| 6.3 | Only EXAM-scope tasks count | Chapter has 1 EXAM task (0.5 pts) + 2 MOCK tasks (1.0 pts each). Click dropdown | Achievable list shows only EXAM-derived points, not MOCK |  |  |

---

### Phase 7: Preview & Stale Indicator
**Goal**: Verify preview updates and stale flag works

| # | Test Case | Steps | Expected Outcome | Status | Notes |
|---|-----------|-------|------------------|--------|-------|
| 7.1 | Preview shows on load | 1. Switch to PDF tab 2. Wait 2 sec | PDF preview appears on right (first page) |  |  |
| 7.2 | Stale indicator on goal change | 1. In PDF tab, change any goal point 2. Watch top-left | "Preview is stale" label appears or highlight updates |  |  |
| 7.3 | Preview updates on mode switch | 1. Change generation mode (EXAM → MOCK) | Preview updates; stale flag clears |  |  |

---

### Phase 8: PDF Export & Generation
**Goal**: Verify end-to-end generation with configured goals

| # | Test Case | Steps | Expected Outcome | Status | Notes |
|---|-----------|-------|------------------|--------|-------|
| 8.1 | Export creates two PDFs | 1. Set chapter goals 2. Click Export 3. Choose save location | Both `exam.pdf` and `exam_solutions.pdf` created in chosen folder |  |  |
| 8.2 | PDF reflects chapter order | 1. Reorder chapters to [Ch3, Ch1, Ch2] 2. Export | Generated PDF has chapters in specified order |  |  |
| 8.3 | PDF respects included/excluded | 1. Exclude Chapter 2 2. Export | Generated PDF has Chapters 1 and 3 only |  |  |
| 8.4 | Point totals match goals | Generate and manually inspect PDF | Each chapter page shows total points ≥ configured goal |  |  |

---

### Phase 9: Layout Responsiveness
**Goal**: Verify UI adapts to window size

| # | Test Case | Steps | Expected Outcome | Status | Notes |
|---|-----------|-------|------------------|--------|-------|
| 9.1 | Narrow window (<1024px) hides preview | Resize window to <1024px | Preview hides; left panel takes full width |  |  |
| 9.2 | Wide window (>1024px) shows preview | Resize window to >1400px | Preview visible; split pane adjusts proportions |  |  |

---

### Phase 10: Typing Usability Regression
**Goal**: Confirm no typing-blocking issues remain

| # | Test Case | Steps | Expected Outcome | Status | Notes |
|---|-----------|-------|------------------|--------|-------|
| 10.1 | Type freely without char rewriting | 1. Click field 2. Type "123" slowly | Field shows "123" as typed (not "321" or mutated) |  |  |
| 10.2 | Multi-digit input accepted | 1. Type "250" 2. Press Enter | Field accepts; snaps to nearest valid if needed |  |  |
| 10.3 | Can clear and retype | 1. Select all text (Ctrl+A) 2. Type new value | New value appears (old not partially mixed in) |  |  |

---

## Overall Workflow Test (Integration)

| # | Test Case | Steps | Expected Outcome | Status |
|---|-----------|-------|------------------|--------|
| OW.1 | Full end-to-end exam generation | 1. Load exam 2. Config: include chapters, set goals, choose mode, set seed 3. Export 4. Open generated PDF | PDF generated with correct chapters, order, point totals |  |
| OW.2 | Switch tabs and return without data loss | 1. Set chapter goals 2. Switch to XML tab (view/edit) 3. Switch back to PDF tab | PDF tab shows same goals (data persisted) |  |  |

---

## Known Issues to Monitor

- **Snap-to-nearest behavior**: Expected but may surprise users who expect exact value entry
- **Decimal separator**: Both "." and "," accepted; encourage using "."
- **Editable ComboBox in virtualized list**: May have typing quirks in rare cases; report if chars repeat/disappear
- **Drag-drop precision**: Drop zone must be within chapter cell; edges may not register

---

## Pass Criteria
- ✅ All Phase 1 tests pass (single-click tab activation, no reload delays)
- ✅ All Phase 2 tests pass (basic typing and dropdown)
- ✅ All Phase 3 tests pass (snap behavior, error handling)
- ✅ All Phase 4 tests pass (removed features are actually absent)
- ✅ Phase 10 test 10.1 passes (typing not rewritten)
- ✅ OW.1 passes (full workflow executable)

**Overall Status**: PASS / FAIL
