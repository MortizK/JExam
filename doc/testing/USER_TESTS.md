# User Tests

## User 1

Pesistent Data was reset beforhand

### Overall

The New Button in AppHeaderNavigation gives no Feedback when creating a New Exam.

When Edeting a Text Field the 'New Line' Button on Keyboard does not exit the Field.

Entering Floats for Points is not intuitive. It is possible to enter Letters and Points which are not Possible (like 1.07). (Solution to implement: Use a Number Field with increment 0.5) 

When I edit an Exam and ether create a new one or open an existing Exam, the old on just dissapears, no warning of deletion or reminder to save.

It Would be nice if the Back Button (Esc or from Mouse) would work.

### XML Tab

When Updating Text in a Text Field the respected Values in TreeView and Breadcrumbs should update.

The Create Button for Children Elements (Chapter, Task, Variant) is not always visible on different Screen Sizes.

The Names in the TreeView can be really long. (Ether set Limits or shortem Them like I did with the Breadcrumbs)

### PDF Tab

In the Chapter Configuration the Excluded Chapters do are not responsiv to hover or select. Therefor the Include Button does 'randomly' inlcude a Chapter.

During the Test only Variant 1 was choosen during generation (Problem or Random?)

The Pop Up after generation shows to much Technical Information (ether simpler or remove)

It is not shown, that the are always generated when a pdf is generated.

The Default Points in Chapter Configuration should be choosen based on the optimal difficulty distribution. (instead of Highest Possible)

## User 2

Pesistent Data was NOT reset beforhand

### Overall

Floats validation is missing. (Should be fixed with the Solution from User 1)

When Saving a .xml File it should overwrite when one is opened. It should only open the File Explorer if a new .xml File is created. Also the status Label was not always synced with the current State.

### XML Tab

After a Delete Child Action, the User expects to navigate to the Parent instead of another Child.

When Deleting the Last Child two Pop Ups appeared. (Tested on Variants)
1. "Each task must have at least one variant"
2. "Remove selected variant?"

Then It does not remove the Variant (which is should not). It should only show the First PopUp and not remove the Variant.

### PDF Tab

In ChapterConfiguration when moving Chapters around the moved one should stay selected (instead of the ID it had)