package br.com.phoenix.client.ui;

import br.com.phoenix.client.model.Customer;
import br.com.phoenix.client.net.ApiHttpClient;
import br.com.phoenix.client.service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final CustomerService service;
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Name", "Email"}, 0);
    private final JTable table = new JTable(model);
    private final JTextField txtName = new JTextField();
    private final JTextField txtEmail = new JTextField();

    public MainFrame(ApiHttpClient http) {
        this.service = new CustomerService(http);
        setTitle("Phoenix Client - Customers");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 480);
        setLocationRelativeTo(null);
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        JButton btnRefresh = new JButton("Atualizar");
        btnRefresh.addActionListener(e -> refresh());
        top.add(btnRefresh, BorderLayout.EAST);

        JPanel form = new JPanel(new GridLayout(1, 0, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Novo / Editar"));
        form.add(new JLabel("Nome:"));
        form.add(txtName);
        form.add(new JLabel("Email:"));
        form.add(txtEmail);
        JButton btnSave = new JButton("Salvar");
        JButton btnDelete = new JButton("Excluir");
        form.add(btnSave);
        form.add(btnDelete);

        btnSave.addActionListener(e -> onSave());
        btnDelete.addActionListener(e -> onDelete());

        JScrollPane scroll = new JScrollPane(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(top, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(form, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void refresh() {
        try {
            model.setRowCount(0);
            List<Customer> list = service.list();
            for (Customer c : list) {
                model.addRow(new Object[]{c.id, c.name, c.email});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Falha ao listar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        try {
            String name = txtName.getText() != null ? txtName.getText().trim() : "";
            String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Informe o nome.", "Validação", JOptionPane.WARNING_MESSAGE);
                txtName.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Informe o e-mail.", "Validação", JOptionPane.WARNING_MESSAGE);
                txtEmail.requestFocus();
                return;
            }

            int sel = table.getSelectedRow();
            if (sel >= 0) {
                Long id = (Long) model.getValueAt(sel, 0);
                service.update(id, name, email);
            } else {
                service.create(name, email);
            }
            txtName.setText("");
            txtEmail.setText("");
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Falha ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        try {
            int sel = table.getSelectedRow();
            if (sel >= 0) {
                Long id = (Long) model.getValueAt(sel, 0);
                service.delete(id);
                refresh();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Falha ao excluir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
