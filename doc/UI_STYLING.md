# UI Styling (Checkpoint 3)

Status: Draft for review
Scope: Design system, visual hierarchy, per-component styling specifications with figure references.
Depends on approved structure in [doc/UI_STRUCTURE.md](doc/UI_STRUCTURE.md) and components in [doc/UI_COMPONENTS.md](doc/UI_COMPONENTS.md).

## 1. Design System Foundation

### 1.1 Color Palette

**Primary Brand**
- Primary: `#0078D4` (Microsoft Blue) — buttons, active states, links
- Primary Dark: `#005A9E` — hover/pressed states
- Primary Light: `#E7F1FB` — background highlights

**Semantic Colors**
- Success: `#107C10` (Green) — validation passed, chapter included
- Warning: `#FFB900` (Amber) — stale preview, unsaved changes
- Error: `#E74856` (Red) — validation failed, required fields
- Info: `#0078D4` (Blue) — informational messages

**Neutral Palette**
- Foreground: `#323130` (Near Black) — text, icons
- Surface: `#FFFFFF` (White) — panels, backgrounds
- Border: `#D2D0CE` (Light Gray) — dividers, table borders
- Disabled: `#A19F9D` (Gray) — disabled controls, hints

**Dark Mode** (future support)
- Foreground: `#F3F2F1` (Near White)
- Surface: `#1E1E1E` (Dark)
- Border: `#3E3E42`

### 1.2 Typography

**Font Stack**: `Segoe UI, -apple-system, BlinkMacSystemFont, sans-serif`

**Scale**
- Display (Large headings): 24pt, 700 weight, line-height 1.3
- Heading (Section titles): 18pt, 600 weight, line-height 1.4
- Subheading (Component titles): 14pt, 600 weight, line-height 1.4
- Body (Default text): 12pt, 400 weight, line-height 1.5
- Small (Help text, labels): 11pt, 400 weight, line-height 1.4
- Mono (Code/numbers): `Consolas, monospace`, 11pt, 400 weight

**Text Treatments**
- Active/Selected: 600 weight
- Disabled: 400 weight + `opacity: 0.6`
- Error fields: 400 weight + color Error
- Clickable elements: underline on hover

### 1.3 Spacing Scale

All measurements use 4px base unit (`1u = 4px`):
- `xs`: 4px (1u) — tight spacing within components
- `sm`: 8px (2u) — component padding, gap between small elements
- `md`: 12px (3u) — standard padding, component spacing
- `lg`: 16px (4u) — section spacing, container margins
- `xl`: 24px (6u) — major section breaks
- `2xl`: 32px (8u) — page/tab-level spacing

### 1.4 Icon System

**Icons**: Use system/outline style (12pt, 16pt, 20pt sizes)
- Navigation: chevron-left, chevron-right, chevron-down, home
- Actions: add, delete, edit, refresh, settings
- States: checkmark, close, alert, info, lock
- Tables: sort-ascending, sort-descending, filter

**Icon Color**: Inherit from `Foreground`; Error color for delete/destructive; Success for checkmark

### 1.5 Shadows & Elevation

**Card/Container Shadows**
- Level 1 (panels, dialogs): `0 1px 2px rgba(0,0,0,0.1)`
- Level 2 (floating panels): `0 4px 8px rgba(0,0,0,0.15)`
- Level 3 (modals, tooltips): `0 8px 16px rgba(0,0,0,0.2)`

**No drop shadows on**:
- Buttons (use background color for elevation)
- Tables/lists (use background and borders)
- Inline editors (flat, minimal elevation)

### 1.6 Borders & Radius

**Corner Radius**
- Tight: 2px — form inputs, small buttons
- Standard: 4px — cards, containers, moderate buttons
- Loose: 8px — modals, overlays, large containers

**Border Width**
- Input focus: 2px solid Primary
- Component divider: 1px solid Border
- Strong divider (section break): 2px solid Border
- Table headers: 1px solid Border (bottom only by default)

## 2. Shared Components Styling

### 2.1 App Header Navigation

**Layout & Structure**
- Height: 56px (14u) with vertical center alignment
- Horizontal padding: `lg` (16px)
- Background: Surface with 1px bottom border (Border color)
- Sticky position: remains at top during scroll

**Logo/Brand Area** (left section)
- Logo/Text: `Heading` style (18pt, 600 weight)
- Left spacing: `lg` (16px)

**Tab Buttons** (center section)
- Style: Tab-strip (no visible buttons; underline only when active)
- Inactive tab: `Body` color, normal
- Active tab: Primary color + 2px solid underline
- Hover: Background Light + underline hints
- Spacing: `lg` (16px) between tabs horizontally; `md` (12px) bottom padding

**Action Buttons** (right section)
- Validate button: Primary background, white text, `md` padding
- Save button: Primary background, white text, `md` padding
- Language selector: Border style, Primary text, `md` padding
- Spacing between buttons: `sm` (8px)
- Disabled state: Background Disabled with opacity 0.5

**State Indicators** (within header)
- Unsaved indicator: Warning color badge or icon near save button
- Validation status: small icon (checkmark Success or alert Error) next to Validate button

### 2.2 Breadcrumb Navigation

**Layout**
- Height: 32px (8u)
- Background: Surface
- Horizontal padding: `lg` (16px)
- Border-bottom: 1px Border

**Breadcrumb Items**
- Style: `Body` (12pt, 400 weight)
- Separator: `/` character, `Foreground` with 0.4 opacity, `sm` (8px) margins on each side
- Active (rightmost): `Foreground` 600 weight
- Inactive (clickable): Primary color, underline on hover
- Disabled segment: Disabled color, no interaction

**Responsive Behavior**
- On narrow screens: truncate to show only current + root; use chevron-right separator

### 2.3 TreeView + Filter

**Container**
- Width: defined by layout (`scr` area width — typically 240-300px)
- Background: Surface
- Border-right: 1px Border
- Vertical padding: `md` (12px)

**Filter Input**
- Style: Standard text input
- Placeholder: "Filter chapters/tasks..."
- Padding: `md` (12px)
- Border: 1px solid Border; 2px Primary on focus
- Small radius: 2px
- Icon (left): search icon, Foreground opacity 0.6
- Icon (right): clear (X) icon if text present, Foreground opacity 0.6

**Tree Structure**
- Item height: 28px (7u)
- Vertical spacing: none (packed)
- Item padding: `sm` (8px) left + icon + text
- Hover background: Primary Light
- Selected background: Primary color, white text
- Indent per level: 16px (4u)
- Chevron icons (disclosure): 16px, left-aligned

**Empty State**
- Text: `Small` style, Disabled color, center-aligned
- Padding: `lg` (16px)

### 2.4 Delete Confirmation Dialog

**Modal Structure**
- Background overlay: rgba(0,0,0,0.4)
- Dialog box: Surface background, shadow Level 3
- Radius: Standard (4px)
- Padding: `lg` (16px)
- Min-width: 320px; max-width: 480px

**Content Sections**
- Title: `Heading` style (18pt, 600 weight), margin-bottom `md` (12px)
- Message: `Body` style, margin-bottom `md` (12px)
- Cascade warning: `Small` style in Warning color box (background Light + border Warning)
- Margin-bottom: `lg` (16px)

**Button Layout** (bottom)
- Two buttons side-by-side
- Cancel (left): Border style, Primary text
- Confirm Delete (right): Error background, white text
- Spacing between: `md` (12px)
- Equal width or right-aligned; prefer equal width
- Padding: `md` (12px) v, `lg` (16px) h

### 2.5 Localization Text Provider

No visual styling; text resolution provider only. Text rendering inherits from context component styles.

## 3. XML Tab Components Styling

### 3.1 XML Tab Container

**Layout**
- Background: Surface
- Left sidebar: 240-300px width (TreeView + Filter)
- Center area: flex-grow to fill remaining space
- Padding: `lg` (16px) between sections

**Empty State**
- Full-screen centered content
- Icon: Oversized file icon or document icon (48pt)
- Title: `Display` style (24pt, 700 weight), margin-bottom `md` (12px)
- Text: `Body` style, max-width 400px, margin-bottom `lg` (16px)
- Buttons: Two buttons stacked vertically
  - "Create New Exam": Primary background, white text, full-width, `md` padding
  - "Load XML": Border style, Primary text, full-width, `md` padding
  - Spacing: `md` (12px)

### 3.2 Exam Header + Inline Exam Editor

**Container**
- Background: Surface
- Border-bottom: 2px Border (strong divider)
- Padding: `lg` (16px)
- Align-items: center (vertical center)

**Layout** (horizontal)
- Heading text: "Exam" in `Subheading` style (14pt, 600)
- Left margin: 0
- Right margin: `md` (12px)
- Input field: flex-grow to fill remaining space
- Max-width: 400px for name input

**Name Input**
- Style: Standard text input
- Font: `Body` (12pt)
- Padding: `sm` (8px) internal, `md` (12px) total with border
- Border: 1px Border; 2px Primary on focus
- Radius: Tight (2px)
- Validation error: Border Error color, message below in Error text
- Unsaved indicator: Subtle Warning background tint or asterisk suffix

### 3.3 Chapter Table

**Container**
- Background: Surface
- Border-bottom: 1px Border
- Padding: `md` (12px)
- Margin-top: `lg` (16px)

**Header Row**
- Background: Primary Light
- Text: `Subheading` style, Primary color
- Padding: `md` (12px)
- Height: 32px (8u)

**Data Rows**
- Height: 36px (9u)
- Padding: `md` (12px)
- Text: `Body` style
- Hover: Background Primary Light (0.5 opacity)
- Selected: Background Primary color, white text

**Columns**
- Name: flex-grow, min-width 200px
- Delete button (far right): Icon button (trash icon), Error color on hover, `sm` (8px) margin-left

**Empty State**
- Full row height: 80px
- Text: `Small` style, Disabled color
- Button: "Add Chapter" link-style button, Primary color, underline on hover
- Padding: `md` (12px)

**Add Button** (below table)
- Style: Border with Primary text
- Icon (left): plus icon
- Text: "Add Chapter"
- Padding: `sm` (8px) v, `md` (12px) h
- Full-width or left-aligned; prefer full-width
- Margin-top: `sm` (8px)

### 3.4 Chapter Header + Inline Chapter Editor

**Container**
- Background: Primary Light
- Padding: `lg` (16px)
- Border-bottom: 1px Border
- Margin-top: `md` (12px)

**Layout** (same as exam header)
- Label: "Chapter" in `Subheading` style
- Input: flex-grow, max-width 400px

**Name Input & Validation**
- Same style as exam name input (3.2)

### 3.5 Task Table

**Container**
- Background: Surface
- Border-bottom: 1px Border
- Padding: `md` (12px)
- Margin-top: `lg` (16px)

**Header & Rows** (same column layout as Chapter Table)
- Name | Points | Difficulty | Delete
- Row height: 40px (10u)
- Hover: Primary Light background (0.5 opacity)
- Selected: Primary color background, white text

**Columns**
- Name: flex, min-width 150px
- Points: 80px, right-aligned, `Mono` style
- Difficulty: 100px, centered, tag-style badge
  - Easy: Success color background, white text
  - Medium: Warning color background, white text
  - Hard: Error color background, white text
- Delete: Icon button, Error on hover, `sm` margin-left

**Delete Button**
- Placement: far-right, `md` (12px) from edge

**Add Button** (below table)
- Same style as Chapter Table "Add Chapter" button

### 3.6 Task Header + Inline Task Editor

**Container**
- Background: Primary Light (slightly lighter tint than chapter header)
- Padding: `lg` (16px)
- Border-bottom: 1px Border
- Margin-top: `md` (12px)
- Max-width: 600px for editor region

**Layout** (multi-row form)
- Top row: Name input (full-width) + Points input (fixed width 100px, right-aligned)
- Middle row: Difficulty dropdown (left) + Scope dropdown (right)
- Bottom row: Validation message if present (Error color)

**Input Spacing**
- Horizontal gap between fields: `md` (12px)
- Vertical gap between rows: `md` (12px)

**Inputs**
- Style: Standard text input or dropdown select
- Padding: `sm` (8px)
- Border: 1px Border; 2px Primary on focus
- Radius: Tight (2px)
- Error state: Border Error, message below in Error text

**Difficulty Dropdown**
- Options as described in task table badges
- Selected option shows matching badge color

### 3.7 Variant List

**Container**
- Background: Surface
- Border-bottom: 1px Border
- Padding: `md` (12px)
- Margin-top: `lg` (16px)

**List Items**
- Height: 36px (9u)
- Padding: `md` (12px)
- Text: `Body` style
- Hover: Primary Light (0.5 opacity)
- Selected: Primary background, white text 600 weight

**Columns** (horizontal layout)
- Index/Number: 40px, centered, `Mono` style, Disabled color
- Question preview: flex-grow, truncate with ellipsis
- Delete button (far-right): Icon button, Error on hover

**Add Button** (below list)
- "Add Variant" style button
- Same styling as chapter/task add buttons

### 3.8 Inline Variant Editor

**Container**
- Background: Primary Light
- Padding: `lg` (16px)
- Border: 1px Border (all sides)
- Border-radius: Standard (4px)
- Margin-top: `md` (12px)

**Layout** (stacked form)
- Question textarea: full-width, min-height 100px, placeholder "Enter question text..."
- Margin-bottom: `md` (12px)
- Answer textarea: full-width, min-height 100px, placeholder "Enter answer (optional)..."

**Textarea Styling**
- Font: `Body` (12pt, monospace for code readability preferred)
- Padding: `md` (12px)
- Border: 1px Border; 2px Primary on focus
- Radius: Tight (2px)
- Resize: vertical only

**Validation**
- Error message below textarea in Error color
- Required indicator: red asterisk after "Question"

**Empty State** (no variant selected)
- Centered text: `Small` style, Disabled color
- Message: "Select a variant from the list above to edit"
- Padding: `xl` (24px)
- Area height: 200px min

## 4. PDF Tab Components Styling

### 4.1 PDF Tab Container

**Layout**
- Left sidebar: 300-400px width (controls + config stacked vertically)
- Right area: flex-grow filled by preview region
- Gap: `lg` (16px) between sections
- Background: Surface throughout
- Padding: `lg` (16px)

**Sidebar Sections** (stacked top-to-bottom)
- Generation Controls (4.2): top, fixed height or content-fit
- Validation Summary (4.3): middle, flex-grow or max-height 300px scrollable
- Chapter Configuration (4.4): bottom, flex-grow or scrollable

**Preview Area** (right)
- Background: Surface
- Border: 1px Border
- Radius: Standard (4px)
- Overflow: auto (scrollable)

### 4.2 Generation Controls (Top-Left)

**Container**
- Background: Surface with 1px bottom Border
- Padding: `md` (12px)
- Margin-bottom: `md` (12px)

**Layout** (stacked vertically)
- Section title: "Generation Mode" in `Subheading` style (14pt, 600)
- Margin-bottom: `md` (12px)

**Mode Selection** (radio button group or tabs)
- Options: Exam, Solution, StudentView (labels from spec)
- Style: Tabs with top border indicator (Primary color, 2px) when active
- Padding per option: `sm` (8px) h, `xs` (4px) v
- Spacing between: no gap (contiguous tabs)
- Text: `Body` style, Primary when active

**Action Buttons** (below mode)
- "Generate Preview": Primary background, white text, full-width, `md` (12px) padding, margin-top `md`
- "Export PDF": Border style, Primary text, full-width, `md` (12px) padding, margin-top `sm`
- Disabled state: Disabled background/border with explanatory tooltip on hover

**Stale Preview Indicator** (if applicable)
- Warning color background (Light)
- Text: "Preview is stale. Click refresh to update." in Warning color
- Icon: info or alert
- Padding: `sm` (8px)
- Margin-bottom: `sm` (8px)
- Height: 32px

### 4.3 Generation Validation Summary

**Container**
- Background: Surface with 1px Border
- Border-radius: Tight (2px)
- Padding: `md` (12px)
- Margin-bottom: `md` (12px)
- Max-height: 300px; overflow-y: auto

**Title**
- "Validation" in `Subheading` style
- Icon: Info or alert based on state
- Color: Error if issues present; Success if passed

**Issues List** (when errors exist)
- Grouped by Chapter (expandable sections)
  - Chapter header: `Body` 600 weight, Primary color, chevron icon for expand/collapse
  - Chevron rotation: 90deg when expanded
  - Margin: `sm` (8px) between chapters
  - Padding: `sm` (8px) left for group content

- Per-task issues under chapter
  - Task name: `Small` style, Foreground opacity 0.9
  - Issue text: `Small` style, Error color
  - Left border (accent): 3px Error color
  - Padding: `sm` (8px)
  - Margin-bottom: `xs` (4px)

- Click on issue: highlight in XML tab (OnIssueSelected event)

**Success State** (no issues)
- Icon: checkmark, Success color
- Text: "All validations passed. Ready to generate." in `Body` style, Success color
- Padding: `md` (12px)

### 4.4 Chapter Configuration Region (Below Left Main)

**Container**
- Background: Surface with 1px Border
- Border-radius: Tight (2px)
- Padding: `md` (12px)
- Flex-grow: 1 (fills remaining left sidebar space)
- Overflow-y: auto

**Title**
- "Chapter Configuration" in `Subheading` style (14pt, 600)
- Margin-bottom: `md` (12px)

**Two-column layout**
- Left column: "Included" (70% width)
- Right column: "Excluded" (30% width)
- Divider: 1px Border, vertical separator

**Included Chapters**
- Header: "Included" in `Small` weight bold
- List items:
  - Height: 32px (8u)
  - Padding: `sm` (8px)
  - Hover: Primary Light (0.5 opacity)
  - Selected: Primary background, white text
  - Text: `Body` style, left-aligned
  - Can drag-drop reorder (visual feedback: 2px Primary border on hover, 4px Primary border on drag-over)
  - Right-side controls (inline):
    - Up/down arrows (chevron-up, chevron-down): icon buttons, Primary text, disabled if at boundary
    - Exclude button: Icon (X or arrow-right), Error text, moves to Excluded
  - Spacing: `xs` (4px) between controls

- Reset button: Below list, Border style, full-width, `sm` (8px) padding

**Excluded Chapters**
- Header: "Excluded" in `Small` weight bold
- List items: Same formatting but read-only
  - Height: 32px (8u)
  - Text: Disabled color
  - Right control:
    - Include button: Icon (checkmark or arrow-left), Success text, moves to Included

**Empty State** (if included list empty)
- Full area background: Light warning
- Text: `Small` style, 600 weight, Foreground opacity 0.8
- Message: "At least one chapter required. Exclude to deselect."
- Padding: `md` (12px)

### 4.5 In-App Preview Region (Right)

**Container**
- Background: Surface
- Border: 1px Border, radius Standard (4px)
- Padding: `md` (12px)
- Flex-grow: 1 (fills remaining space)

**States & Styling**

**Idle State** (no preview yet)
- Centered content
- Icon: Document icon (48pt), Disabled color (opacity 0.4)
- Text: "No preview generated yet." in `Small` style, Disabled color
- Message: "Click 'Generate Preview' to see the PDF." in `Small` style
- Padding: `xl` (24px)

**Loading State**
- Spinner: 24pt, Primary color
- Text below: "Generating preview..." in `Small` style
- Centered, vertical center
- Padding: `xl` (24px)

**Ready State** (preview displayed)
- PDF viewer control (implementation deferred)
- Toolbar (if embedded viewer):
  - Icons: zoom-in, zoom-out, fullscreen, open-external
  - Background: Primary Light
  - Padding: `sm` (8px)
  - Spacing: `sm` (8px) between icons
  - Position: top-right or top-bar

**Stale State** (preview outdated after XML edit)
- Overlay: Translucent Primary color (0.1 opacity) covering preview
- Banner (top-center):
  - Background: Warning Light
  - Border: 2px Warning
  - Icon: alert
  - Text: "Preview is out of date. Click Refresh to update." in Warning color
  - Button: "Refresh" Primary background
  - Padding: `md` (12px)
  - Radius: Tight (2px)

**Error State**
- Icon: alert, Error color (48pt)
- Title: "Preview failed" in `Subheading` style, Error color
- Message: Error details in `Small` style, Foreground
- Button: "Retry" or "Open External" in Primary
- Padding: `xl` (24px)

## 5. Responsive & Layout Considerations

**Breakpoints**
- Large (≥1400px): Full sidebar + center + preview
- Medium (1024-1399px): Sidebar 240px, center area compressed, preview scaled down
- Small (<1024px): Sidebar collapses to icon-only or drawer; center area expands

**XML Tab Responsiveness**
- Large: 240px tree | center editor (flex-grow) | [empty or small panels]
- Medium: 200px tree | center editor (flex-grow)
- Small: Tree in drawer (toggle icon) or hidden; full-width editor

**PDF Tab Responsiveness**
- Large: 350px left sidebar (stacked sections) | right preview (flex-grow)
- Medium: 300px left sidebar | right preview scaled
- Small: Stacked vertically; sidebar on top, preview below (full-width, min-height 400px)

## 6. Interaction & Feedback Styling

**Hover States**
- Buttons: Darken background by 5-10% or show Primary color underline
- Links: Primary color underline on hover
- Table rows: Primary Light background (0.5 opacity)
- Icon buttons: Primary color on hover

**Focus States**
- Input: 2px Primary border outline
- Buttons: 2px Primary border outline (if visible focus needed)
- Dropdowns: 2px Primary border outline

**Active States**
- Tab: Primary color + 2px underline
- Selected row: Primary background, white 600 weight text
- Button pressed: darker shade of primary

**Disabled States**
- All elements: Disabled color (Gray), 60% opacity
- Cursor: not-allowed
- No hover effects

**Loading/Progress**
- Spinner: 24pt Primary color, animated rotation 1 second loop
- Progress bar: Primary color background, height 2px, fill % based on progress

**Validation Feedback**
- Invalid fields: 2px Error border + error message below in Error color (`Small` style)
- Valid field: Optional success checkmark icon (Success color) far right
- Required marker: Red asterisk after label

## 7. Figure References

Source figures referenced from [doc/LaTeX/chapters/ui.md](doc/LaTeX/chapters/ui.md):
- UI mockup placeholders: Reference figure numbers from ui.tex for component layouts
- Use-case diagrams from [doc/LaTeX/fig/uml.drawio](doc/LaTeX/fig/uml.drawio) for workflow context
- Actor diagrams from [doc/LaTeX/fig/aktoren.drawio](doc/LaTeX/fig/aktoren.drawio) for user interactions

**Layout Figure Mapping**
- Main two-tab structure: See [doc/UI_STRUCTURE.md](doc/UI_STRUCTURE.md) Figure 1
- XML tab hierarchy: See ui.tex section on tree navigation
- PDF preview positioning: See ui.tex section on in-app rendering
- Component interactions: See [doc/UI_COMPONENTS.md](doc/UI_COMPONENTS.md) interaction contracts

## 8. Open Questions for Implementation

1. **PDF Viewer Library**: Which embedded PDF viewer library to use? (PDFBox with FxCanvas, or third-party like IcePDF)
2. **Dark Mode**: Full dark mode support in phase implementation, or light mode only initially?
3. **Font System**: Use system fonts or bundle Google Fonts? (Current spec uses system defaults)
4. **Animation Timing**: Standard 0.2s easing (ease-in-out) for all transitions, or per-component customization?
5. **Accessibility**: WCAG 2.1 AA compliance required; add focus rings, ARIA labels, keyboard navigation in implementation phase.
