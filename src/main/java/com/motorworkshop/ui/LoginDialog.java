package com.motorworkshop.ui;

import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private UserRole authenticatedRole;

    public LoginDialog(Frame owner) {
        super(owner, "Motor Workshop Login", true);
        initComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(20, 22, 18, 22));
        content.setBackground(UIHelper.CARD_BG);

        JPanel header = UIHelper.createSectionHeader(
                "Sign in to Motor Workshop",
                "Choose the account level for this session"
        );
        content.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        form.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        form.add(txtPassword, gbc);
        content.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(UIHelper.CARD_BG);
        JButton btnLogin = UIHelper.createSuccessButton("Sign In");
        btnLogin.addActionListener(e -> authenticate());
        JButton btnExit = UIHelper.createSecondaryButton("Exit");
        btnExit.addActionListener(e -> dispose());
        buttons.add(btnExit);
        buttons.add(btnLogin);
        content.add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnLogin);
        setContentPane(content);
    }

    private void authenticate() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if ("admin".equalsIgnoreCase(username) && "admin123".equals(password)) {
            authenticatedRole = UserRole.ADMIN;
        } else if ("employee".equalsIgnoreCase(username) && "employee123".equals(password)) {
            authenticatedRole = UserRole.EMPLOYEE;
        } else {
            UIHelper.showWarning(this, "Invalid username or password.");
            txtPassword.setText("");
            txtUsername.requestFocus();
            return;
        }
        dispose();
    }

    public UserRole getAuthenticatedRole() {
        return authenticatedRole;
    }
}
