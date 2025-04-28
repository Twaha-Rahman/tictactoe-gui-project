import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Vector;

public class UserAuthApp {
    // Static user database accessible by all panels
    public static HashMap<String, String> userDatabase = new HashMap<>();
    public static HashMap<String, String> emailDatabase = new HashMap<>();

    public static String[] loggedInUsersArr = new String[2];
    public static String[] loggedInEmailsArr = new String[2];

    public static Db db = new Db();

    public static void main(String[] args) {
        // Set Nimbus look and feel for modern UI
//        try {
//            UIManager.setLookAndFeel(new FlatLightLaf());
//            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            // If Nimbus is not available, fall back to default
//            System.out.println("INFO: (LookAndFeel) Nimbus not found");
//        }

        SwingUtilities.invokeLater(() -> new AuthFrame().setVisible(true));
    }
}

class AuthFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private LoginPanel loginPanel = new LoginPanel(this);
    private RegisterPanel registerPanel = new RegisterPanel();

    public AuthFrame() {
        setTitle("User Authentication");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel.add(loginPanel, "login");
        mainPanel.add(registerPanel, "register");
        add(mainPanel);

        loginPanel.getSwitchBtn().addActionListener(e -> cardLayout.show(mainPanel, "register"));
        registerPanel.getSwitchBtn().addActionListener(e -> cardLayout.show(mainPanel, "login"));
    }
}

abstract class StyledPanel extends JPanel {
    StyledPanel() {
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 245));
        setLayout(new GridBagLayout());
    }
}

class LoginPanel extends StyledPanel {
    private JTextField userField = new JTextField(20);
    private JTextField emailField = new JTextField(20);
    private JPasswordField passField = new JPasswordField(20);
    private JButton loginBtn = new JButton("Login");
    private JButton switchBtn = new JButton("Register");

    private JLabel header;

    private AuthFrame refToSelf;
    private int loggedInUserCount = 0;

    LoginPanel(AuthFrame ownRef) {
        refToSelf = ownRef;

        GridBagConstraints gbc = createGbc();

        header = new JLabel("First User Login");
        header.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.setForeground(new Color(60, 60, 60));
        gbc.gridwidth = 2;
        add(header, gbc);

        // Username row
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(userField, gbc);

        // E-mail row
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(emailField, gbc);

        // Password row
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(passField, gbc);

        // Buttons row
        styleButtons();
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(loginBtn, gbc);
        gbc.gridx = 1;
        add(switchBtn, gbc);

        loginBtn.addActionListener(e -> {
            String user = userField.getText().trim();
            String email = userField.getText().trim();
            String pass = String.valueOf(passField.getPassword());

            UserAuthApp.userDatabase.clear();
            UserAuthApp.emailDatabase.clear();
            try {
                Vector<UserCredentials> usrCreds = UserAuthApp.db.getAllUserCredentials();

                usrCreds.forEach((usr) -> {
                    UserAuthApp.userDatabase.put(usr.name, usr.password);
                    UserAuthApp.emailDatabase.put(usr.email, usr.name);
                });

            } catch (Exception ex) {
                System.err.println("ERROR: " + ex.toString() + "\nAboring due to error!");

                System.exit(1);
            }

            if (UserAuthApp.userDatabase.containsKey(user) && UserAuthApp.userDatabase.get(user).equals(pass)) {
                // && UserAuthApp.emailDatabase.containsKey(email) &&
                // UserAuthApp.emailDatabase.get(email).equals(user)

                JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                header.setText("Second User Login");

                UserAuthApp.loggedInUsersArr[loggedInUserCount] = user;
                UserAuthApp.loggedInEmailsArr[loggedInUserCount] = email;
                loggedInUserCount++;

                if (loggedInUserCount >= 2) {
                    Ui gameUi = new Ui(UserAuthApp.loggedInUsersArr, UserAuthApp.loggedInEmailsArr);
                    refToSelf.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    JButton getSwitchBtn() {
        return switchBtn;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    private void styleButtons() {
        Font btnFont = new Font("SansSerif", Font.BOLD, 14);
        loginBtn.setFont(btnFont);
        switchBtn.setFont(btnFont);
        Dimension size = new Dimension(120, 30);
        loginBtn.setPreferredSize(size);
        switchBtn.setPreferredSize(size);
        loginBtn.setBackground(new Color(70, 130, 180));
        loginBtn.setForeground(Color.WHITE);
        switchBtn.setBackground(new Color(100, 100, 100));
        switchBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        switchBtn.setFocusPainted(false);
    }
}

class RegisterPanel extends StyledPanel {
    private JTextField userField = new JTextField(20);
    private JTextField emailField = new JTextField(20);
    private JPasswordField passField = new JPasswordField(20);

    private JButton registerBtn = new JButton("Register");
    private JButton switchBtn = new JButton("Back to Login");

    RegisterPanel() {
        GridBagConstraints gbc = createGbc();

        JLabel header = new JLabel("Create Account");
        header.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.setForeground(new Color(60, 60, 60));
        gbc.gridwidth = 2;
        add(header, gbc);

        // Username row
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(userField, gbc);

        // E-mail row
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(emailField, gbc);

        // Password row
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(passField, gbc);

        // Buttons row
        styleButtons();
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(registerBtn, gbc);
        gbc.gridx = 1;
        add(switchBtn, gbc);

        registerBtn.addActionListener(e -> {
            String user = userField.getText().trim();
            String email = emailField.getText().trim();
            String pass = String.valueOf(passField.getPassword());

            if (user.isEmpty() || pass.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserAuthApp.userDatabase.clear();
            UserAuthApp.emailDatabase.clear();
            try {
                Vector<UserCredentials> usrCreds = UserAuthApp.db.getAllUserCredentials();

                usrCreds.forEach((usr) -> {
                    UserAuthApp.userDatabase.put(usr.name, usr.password);
                    UserAuthApp.emailDatabase.put(usr.email, usr.name);
                });

            } catch (Exception ex) {
                System.err.println("ERROR: " + ex.toString() + "\nAboring due to error!");

                System.exit(1);
            }

            if (UserAuthApp.userDatabase.containsKey(user) || UserAuthApp.emailDatabase.containsKey(email)) {
                JOptionPane.showMessageDialog(this, "Username or email already exists! Please try again with different values!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // UserAuthApp.userDatabase.put(user, pass);
                // UserAuthApp.emailDatabase.put(email, user);
                try {
                    UserAuthApp.db.addUser(user, email, pass);
                } catch (Exception ex) {
                    System.err.println("ERROR: " + ex.toString() + "\nAboring due to error!");

                    System.exit(1);
                }

                JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    JButton getSwitchBtn() {
        return switchBtn;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    private void styleButtons() {
        Font btnFont = new Font("SansSerif", Font.BOLD, 14);
        registerBtn.setFont(btnFont);
        switchBtn.setFont(btnFont);
        Dimension size = new Dimension(140, 30);
        registerBtn.setPreferredSize(size);
        switchBtn.setPreferredSize(size);
        registerBtn.setBackground(new Color(34, 139, 34));
        registerBtn.setForeground(Color.WHITE);
        switchBtn.setBackground(new Color(100, 100, 100));
        switchBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        switchBtn.setFocusPainted(false);
    }
}
