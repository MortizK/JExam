# LAYOUT

This document captures measured PDF layout knowledge for planning and implementation of cover/page styling.

## Scope

- Reference analyzed: `doc/Exams/WE1_Probeklausur.pdf` (cover/page geometry)
- Supporting comparison: `doc/Exams/TI2_Probeklausur_Lsg.pdf` (global margins/typography)
- Data source artifacts:
  - `doc/testing/pdf_analysis_report.json`
  - `doc/testing/we1_cover_analysis.json`
  - scripts in `doc/testing/analyze_reference_pdfs.py` and `doc/testing/analyze_we1_cover.py`

## Global Page Layout Baseline

- Page size: A4, about `595.30 x 841.89 pt`
- Typical content margins (median):
  - Left: `56.8 pt`
  - Right: `64-66 pt`
  - Top: `57.33 pt`
  - Bottom: `42.47 pt`
- Dominant body text:
  - Font size: `14 pt`
  - Median line height: `15.5-15.85 pt`
- Larger title/display sizes seen: mostly `16 pt`, `20 pt`, and `24 pt`

## Cover Layout (WE1 Reference)

### Required ordering (top to bottom)

1. Matrikelnummer area with 7 square boxes
2. Label `Matrikelnummer`
3. Title block (`Probeklausur:` and module title)
4. Notes/instructions section
5. Points/breakdown table section

### Matrikelnummer block

- The label is above the title block (confirmed by y coordinates).
- There are exactly 7 boxes for digits.
- Boxes are encoded as text glyphs (`U+25A2`) in the PDF, not drawn rectangles.
- Box row bbox (combined):
  - `x: 334.75 -> 538.61`
  - `y: 53.20 -> 85.10`
- Individual box width is about `24 pt`, with step around `29.98 pt`.
- `Matrikelnummer` label bbox:
  - `x: 457.45 -> 538.68`
  - `y: 84.81 -> 98.09`

### Title block

- `Probeklausur:` bbox:
  - `x: 248.55 -> 346.87`
  - `y: 114.89 -> 132.61`
  - Font size `16 pt`, bold serif
- Module title (`Web Engineering I`) bbox:
  - `x: 201.10 -> 394.30`
  - `y: 133.62 -> 160.18`
  - Font size `24 pt`, bold serif
- Professor line directly below title:
  - Font size `12 pt`, bold serif

### Cover table/grid geometry (right-side scoring area)

- Main horizontal separator lines:
  - `y = 365.35` from `x = 59.1` to `538.0`
  - `y = 559.25` from `x = 59.1` to `538.0`
- Score mini-grid columns:
  - Left vertical: `x = 456.2`
  - Right vertical: `x = 504.2`
- Score cell width:
  - about `49.5 pt` (`x = 455.45 -> 504.95`)
- Regular row pitch:
  - about `23.1 pt` between horizontal row lines
- Header labels near top of the scoring area:
  - `Aufg.` and `Punkte`
  - second header line with `erreicht` and `von`

## Single-Chapter Breakdown Styling Guidance

When the exam has a single chapter, keep the same visual language as WE1 while reducing row count:

- Keep the same column x positions (`456.2` and `504.2`) and score-cell width (`~49.5 pt`).
- Keep the same header pair:
  - Row 1: `Aufg.` / `Punkte`
  - Row 2: `erreicht` / `von`
- Render one chapter row only (for example `WE1-1` equivalent), aligned to the same baseline system.
- Keep total points row in the lower summary area using the same x anchors as WE1.
- Preserve the same top separators and spacing rhythm where possible; do not re-center the score columns.

## Known Rendering Detail

- The 7 Matrikel boxes are text glyphs in WE1 (`U+25A2`, font observed: `YuGothicUI-Light`, size `24 pt`).
- No rectangle drawing operators were detected for these boxes on the cover page.

## Planning Checklist For Next Phase

- Confirm whether to render Matrikel boxes as glyphs (WE1-like) or drawn squares (more robust font independence).
- Lock canonical cover constants (margins, y anchors, table columns) in one place in PDF generation code.
- Implement a dedicated single-chapter cover branch that reuses multi-chapter geometry constants.
- Add regression tests for:
  - Matrikel block order (boxes above title)
  - Exactly 7 boxes on cover
  - Single-chapter points row alignment and totals rendering
