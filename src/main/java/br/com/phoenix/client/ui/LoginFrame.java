package br.com.phoenix.client.ui;

import br.com.phoenix.client.net.ApiHttpClient;
import br.com.phoenix.client.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField txtUser = new JTextField("admin");
    private final JPasswordField txtPass = new JPasswordField("admin123");

    public LoginFrame() {
        setTitle("Phoenix Client - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 240);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUser = new JLabel("Usuário");
        JLabel lblPass = new JLabel("Senha");
        JButton btnLogin = new JButton("Entrar");

        c.gridx = 0; c.gridy = 0;
        panel.add(lblUser, c);
        c.gridx = 1;
        panel.add(txtUser, c);

        c.gridx = 0; c.gridy = 1;
        panel.add(lblPass, c);
        c.gridx = 1;
        panel.add(txtPass, c);

        c.gridx = 1; c.gridy = 2;
        panel.add(btnLogin, c);

        btnLogin.addActionListener(e -> onLogin());

        add(panel);
    }

    private void onLogin() {
        try {
            ApiHttpClient http = new ApiHttpClient();
            AuthService auth = new AuthService(http);

            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword()).trim();

            String token = auth.login(username, password);

            MainFrame mf = new MainFrame(http);
            mf.setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Falha no login: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}