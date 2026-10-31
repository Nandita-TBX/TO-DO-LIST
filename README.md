# To-Do List Application

A simple desktop To-Do List built with **Java Swing** and the **Java Collections Framework** (`ArrayList`). The task list is shown as an HTML page inside the Swing window, and the page layout comes from an external file, `todo.html`.

**Name:** [Your Name]
**Roll No.:** [Your Roll No.]
**Class:** [Your Class]

## Features

- Add a task from the text box (button or Enter key)
- Mark a task as done, or undo it
- Edit the text of a task
- Delete a task
- Clear all completed tasks
- Tasks are saved automatically and come back when the app is restarted
- Page design (colors, layout) can be changed in `todo.html` without touching the Java code

## Files

| File | Purpose |
|------|---------|
| `TodoApp.java` | The whole application: Swing window, `ArrayList` of tasks, save and load |
| `todo.html` | External HTML template that is loaded and filled with the tasks |
| `tasks.txt` | Created automatically on first run; stores the saved tasks |

## Requirements

- Java JDK 8 or newer (`javac` and `java` available in the terminal)

## How to Run

1. Download or clone this repository:
   ```
   git clone https://github.com/your-username/your-repo.git
   cd your-repo
   ```
2. Compile:
   ```
   javac TodoApp.java
   ```
3. Run it from the same folder that contains `todo.html`:
   ```
   java TodoApp
   ```

If you change `TodoApp.java` and the old behavior still appears, delete the old `.class` files and compile again.

## How to Use

| Action | How |
|--------|-----|
| Add a task | Type in the **New task** box and press Enter or click **Add Task** |
| Mark done / undo | Click `[Done]` (or `[Undo]`) next to the task |
| Edit | Click `[Edit]`, change the text in the dialog |
| Delete | Click `[Delete]` |
| Clear finished tasks | Click **Clear Completed** at the bottom |

## How It Works

1. The program reads `todo.html` as a template.
2. It replaces the `<!--TASKS-->` marker with one table row for every task in the `ArrayList`, and `<!--COUNT-->` with the task counts.
3. The finished HTML is shown in a `JEditorPane`.
4. Clicking `[Done]`, `[Edit]` or `[Delete]` runs `handleLink()`, which changes the list and rebuilds the page.
5. After every change the list is written to `tasks.txt` (one task per line, `0|text` for pending and `1|text` for done).

### Java concepts used

- `ArrayList<Task>` to store the tasks
- Swing: `JFrame`, `JTextField`, `JButton`, `JEditorPane`, `JOptionPane`
- Lambda expressions and streams (`removeIf`, `stream().filter(...).count()`)
- File I/O with `java.nio.file.Files`
- Hyperlink events to handle clicks on the HTML page

## Screenshots

![Main window](screenshots/main-window.png)
![Tasks added](screenshots/tasks-added.png)
![Task marked done](screenshots/task-done.png)

*(Add your screenshots to a `screenshots` folder with these names.)*

## Author

[Your Name] - [Your Roll No.] - [Your Class]
