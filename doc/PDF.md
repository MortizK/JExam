# PDF

## The Generation

### EXAM

This only describes the Changes in Generation mode controls.

Have Chapters per selectable and de-selectable, so that Exams with different Chapters can be generated.

Each Chapter should have goal points, that this Chapter should have.

The goal points should selectable inline of each chapter. This should happen with a dropdown that updates its list live as the user types. What should be accepted as a match in the List:

- Nearest String Distance (Typed: "2", In List: "2.0", "2.1", "12.0", "20.0")
- Nearest two Numbers in addition to Nearest String Distance (Typed: "99", In List: "27.0", "26.5") Or (Typed: "21") or (Typed: "21", In List: "20.5", "21.5")

The Input should accept Integers or Doubles, but the only Numbers that are accepted in the Box should be one of the possible options from the list. In Case a non List Item is still entered fall back to the nearest Number (Higher or Lower)

The Generation of the Exam should not change.

### MOCK EXAM

Generate with all Tasks marked with `scope="mock-exam"`

For Tasks with multiple Variants, choose a random variant