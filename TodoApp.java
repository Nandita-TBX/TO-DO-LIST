import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TodoApp extends JFrame {

    static class Task {
        String text;
        boolean done;
        Task(String text, boolean done) { this.text = text; this.done = done; }
    }

    private static final Path TASK_FILE = Paths.get("tasks.txt");
    private static final Path HTML_FILE = Paths.get("todo.html");

    private static final String DEFAULT_TEMPLATE =
        "<html><body bgcolor='#fff3e0'>"
      + "<table width='100%' cellpadding='12'><tr><td bgcolor='#7e57c2'>"
      + "<font color='white' size='5'>My To-Do List</font></td></tr>"
      + "<tr><td bgcolor='#ffca28'><!--COUNT--></td></tr></table>"
      + "<table width='100%' cellpadding='6'><!--TASKS--></table></body></html>";

    private final ArrayList<Task> tasks = new ArrayList<>();

    private final JTextField input = new JTextField(30);
    private final JEditorPane htmlView = new JEditorPane();
    private final JScrollPane scroll = new JScrollPane(htmlView);

    public TodoApp() {
        super("To-Do List");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(680, 560);
        setLocationRelativeTo(null);

        // ----- Top: add a task -----
        JButton addBtn = new JButton("Add Task");
        addBtn.setFont(addBtn.getFont().deriveFont(Font.BOLD));
        input.setFont(input.getFont().deriveFont(15f));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        top.add(new JLabel("New task:"), BorderLayout.WEST);
        top.add(input, BorderLayout.CENTER);
        top.add(addBtn, BorderLayout.EAST);

        // ----- Center: the HTML page -----
        htmlView.setContentType("text/html");
        htmlView.setEditable(false);
        htmlView.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                handleLink(e.getDescription());
            }
        });

        // ----- Bottom -----
        JButton clearBtn = new JButton("Clear Completed");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.add(clearBtn);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // ----- Actions -----
        addBtn.addActionListener(e -> addTask());
        input.addActionListener(e -> addTask());
        clearBtn.addActionListener(e -> {
            tasks.removeIf(t -> t.done);
            refresh();
        });

        load();
        refresh();
        SwingUtilities.invokeLater(() -> input.requestFocusInWindow());
    }

    // ---------- Task operations ----------

    private void addTask() {
        String text = input.getText().trim();
        if (text.isEmpty()) return;
        tasks.add(new Task(text, false));
        input.setText("");
        input.requestFocusInWindow();
        refresh();
    }

    /** Links in the page look like "done:2", "edit:2", "delete:2". */
    private void handleLink(String link) {
        String[] parts = link.split(":");
        if (parts.length != 2) return;
        int i;
        try { i = Integer.parseInt(parts[1]); } catch (NumberFormatException ex) { return; }
        if (i < 0 || i >= tasks.size()) return;

        switch (parts[0]) {
            case "done":
                tasks.get(i).done = !tasks.get(i).done;
                break;
            case "delete":
                tasks.remove(i);
                break;
            case "edit":
                String text = (String) JOptionPane.showInputDialog(this, "Edit task:", "Edit",
                        JOptionPane.PLAIN_MESSAGE, null, null, tasks.get(i).text);
                if (text == null || text.trim().isEmpty()) return;
                tasks.get(i).text = text.trim();
                break;
            default:
                return;
        }
        refresh();
    }

    // ---------- HTML: import the external template and fill it in ----------

    private void refresh() {
        int scrollPos = scroll.getVerticalScrollBar().getValue();
        htmlView.setText(buildHtml());
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(scrollPos));
        save();
    }

    private String buildHtml() {
        String template;
        try {
            if (!Files.exists(HTML_FILE)) {
                Files.write(HTML_FILE, DEFAULT_TEMPLATE.getBytes(StandardCharsets.UTF_8));
            }
            template = new String(Files.readAllBytes(HTML_FILE), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            template = DEFAULT_TEMPLATE;
        }

        StringBuilder rows = new StringBuilder();
        if (tasks.isEmpty()) {
            rows.append("<tr><td><i>No tasks yet. Type one above and press Add Task.</i></td></tr>");
        }
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            String text = escape(t.text);
            String links =
                  "<a href='done:" + i + "'><font color='#2e7d32'>" + (t.done ? "[Undo]" : "[Done]") + "</font></a> "
                + "<a href='edit:" + i + "'><font color='#1565c0'>[Edit]</font></a> "
                + "<a href='delete:" + i + "'><font color='#c62828'>[Delete]</font></a>";
            if (t.done) {
                rows.append("<tr><td bgcolor='#e8f5e9'><font color='#2e7d32'><b>DONE</b></font></td>")
                    .append("<td bgcolor='#e8f5e9' width='70%'><strike>").append(text).append("</strike></td>")
                    .append("<td bgcolor='#e8f5e9' align='right'>").append(links).append("</td></tr>");
            } else {
                rows.append("<tr><td bgcolor='#ffe0b2'><font color='#e65100'><b>TO DO</b></font></td>")
                    .append("<td bgcolor='#e3f2fd' width='70%'>").append(text).append("</td>")
                    .append("<td bgcolor='#e3f2fd' align='right'>").append(links).append("</td></tr>");
            }
        }

        long doneCount = tasks.stream().filter(t -> t.done).count();
        String count = tasks.size() + " task(s) &middot; " + doneCount + " done &middot; "
                + (tasks.size() - doneCount) + " remaining";

        return template.replace("<!--TASKS-->", rows.toString()).replace("<!--COUNT-->", count);
    }

    // ---------- Save / load (tasks.txt, one task per line: 0|text or 1|text) ----------

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Task t : tasks) lines.add((t.done ? "1|" : "0|") + t.text.replace("\n", " "));
        try {
            Files.write(TASK_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save: " + ex.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(TASK_FILE)) return;
        try {
            for (String line : Files.readAllLines(TASK_FILE, StandardCharsets.UTF_8)) {
                if (line.length() > 2 && line.charAt(1) == '|') {
                    tasks.add(new Task(line.substring(2), line.charAt(0) == '1'));
                }
            }
        } catch (IOException ex) {
            // start with an empty list
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TodoApp().setVisible(true));
    }
}
