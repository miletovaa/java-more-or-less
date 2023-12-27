# More or less, less is more

Write a Java program that allows playing the game "More or less, less is more". The game
consists of a two-dimensional field of size n × n. Each element is a button with a number.

## Game Initialization:
• Numbers on the buttons are set to a random digit between 1 and 9.

• The target value is displayed above the field.

• The current sum of numbers on the buttons is displayed below the field.

• The number of moves until the end of the game is displayed in the top right corner
above the field.

## Playing:
• The player selects a button (with a pre-defined digit 1 - 9) to choose possible candidates for the next move.

• Initially, Previous is invalid. The player starts by choosing any button (Current)
on the field, allowing the selection of all rows and columns divisible by the chosen
button’s digit.

1. The player can choose a button at the intersection of possible rows and columns between Previous and Current. This choice swaps the Current value to Previous, and Current becomes the digit value of the chosen button
(Previous = Current; Current = New value).
2. Increase the total sum by the value of Current. The selected button becomes
inactive.
3. Decrement the move counter and repeat from step 1 until the move counter
exceeds 0.

## Game Objective:
• The sum of numbers on the selected buttons should approach the target value.

## Tasks:

- [x] Develop a user interface for playing with a new field layout for each game, with a
basic field size 10 × 10 (n = 10). (50 points)

- [ ] Create a user interface that saves the game field to a file, allowing the choice of a
field layout from the file during play. (15 points)

- [x] Implement a game control menu (restart, set field size, set number of moves, set
target value). (10 points)

- [x] Enhance the game control menu with difficulty options (easy, medium, hard), affecting the target value and the number of moves. (10 points)

- [ ] Display the deviation from the final value in the graphical user interface when the
move counter reaches zero (solution quality). (5 points)

- [ ] Allow the game to detect when there is no possible continuation. (10 points)

## Additional points (for over 100%):

- [ ] UI (User Interface) assists the user in choosing button B in Playing step 1 by showing
the new value if the user selected a possible button B. (10 points)

- [ ] Add an option for the UI to suggest a good move for button B in Playing step 1 to
help the user approach the target value. (5 points)


*Note: Ensure correct use of object-oriented programming principles, avoid static methods, and define individual program modules as independent classes. Follow proper coding
practices. (-5 points for violations)*
