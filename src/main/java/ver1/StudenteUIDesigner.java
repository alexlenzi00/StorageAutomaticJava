package ver1;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.sql.*;
import java.util.List;

public class StudenteUIDesigner extends JFrame {
    private JPanel mainPanel;
    private JPanel Center;
    private JPanel Sud;
    private JButton btPrevious;
    private JButton btNext;
    private JButton btInsert;
    private JButton btRemove;
    private JTextField tfId;
    private JTextField tfNome;
    private JTextField tfCognome;
    private StudenteModel students;

    public StudenteUIDesigner() {
        super();
        JMenuBar menu = generateMenu();
        setJMenuBar(menu);
        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(350, 250);
        setResizable(false);
        setVisible(true);
        btPrevious.addActionListener(e -> {
            if (students != null) {
                students.previous();
                update();
            }
        });
        btNext.addActionListener(e -> {
            if (students != null) {
                students.next();
                update();
            }
        });
        btInsert.addActionListener(e -> {
            String[] v = JOptionPane.showInputDialog(this, "Insert Student (name, surname)").split(";");
            if (v.length == 2)
                students.insert(new Studente(v[0], v[1]));
            update();
        });
        btRemove.addActionListener(e -> {
            if (students != null) {
                students.remove();
                update();
            }
        });
        tfNome.addActionListener(e -> students.setNome(tfNome.getText()));
        tfCognome.addActionListener(e -> students.setCognome(tfCognome.getText()));
        try {
            initData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        update();
    }

    private void initData() throws SQLException {
        Statement statement = DBManager.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        students = new StudenteModel(statement);
    }

    private void update() {
        Studente s = students.getSelected();
        if (s == null) {
            tfId.setText("");
            tfNome.setText("");
            tfCognome.setText("");
        } else {
            tfId.setText(Integer.toString(s.getIdStudente()));
            tfNome.setText(s.getNome());
            tfCognome.setText(s.getCognome());
        }
    }

    private JMenuBar generateMenu() {
        JMenuBar menu = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem fill = new JMenuItem("Import data");
        JMenuItem quit = new JMenuItem("Quit");
        file.add(fill);
        file.add(quit);
        menu.add(file);
        fill.addActionListener(e -> {
            JFileChooser jf = new JFileChooser(String.format("%s%s%s", System.getProperty("user.home"), System.getProperty("file.separator"), "Desktop"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV", "csv");
            jf.addChoosableFileFilter(filter);
            jf.setFileFilter(filter);
            int option = jf.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                try {
                    Statement statement = DBManager.getConnection().createStatement();
                    List<Studente> students = Studente.loadFromCSV(jf.getSelectedFile().getName(), new Studente(), Studente.class);
                    Studente.saveToDB(students, statement, new Studente());
                    initData();
                    statement.close();
                } catch (IOException | SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                update();
            }
        });
        quit.addActionListener(e -> dispose());
        return menu;
    }
}
