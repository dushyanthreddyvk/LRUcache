package miniredis;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class SwingFrontend extends JFrame implements CacheEventListener {
    private final JTextField keyField = new JTextField(18);
    private final JTextField valueField = new JTextField(18);
    private final JTextField ttlField = new JTextField(6);
    private final JTextArea logArea = new JTextArea();
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Key", "Value"}, 0);
    private final JTable cacheTable = new JTable(tableModel);

    private final Server server;
    private final CommandProcessor commandProcessor;

    public SwingFrontend() {
        super("Mini Redis Clone");
        this.server = new Server(
                Server.DEFAULT_PORT,
                Server.DEFAULT_MAX_ENTRIES,
                Server.DEFAULT_STORAGE_FILE,
                this
        );
        this.commandProcessor = new CommandProcessor(server.getStorage());

        configureWindow();
        startServer();
        startUiRefresh();
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 520));
        setLayout(new BorderLayout(12, 12));

        add(createCommandPanel(), BorderLayout.NORTH);
        add(createCachePanel(), BorderLayout.CENTER);
        add(createLogPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createCommandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Key"), gbc);

        gbc.gridx = 1;
        panel.add(keyField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Value"), gbc);

        gbc.gridx = 3;
        panel.add(valueField, gbc);

        gbc.gridx = 4;
        panel.add(new JLabel("TTL"), gbc);

        gbc.gridx = 5;
        panel.add(ttlField, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton setButton = new JButton("SET");
        JButton getButton = new JButton("GET");
        JButton deleteButton = new JButton("DELETE");
        buttons.add(setButton);
        buttons.add(getButton);
        buttons.add(deleteButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        panel.add(buttons, gbc);

        setButton.addActionListener(event -> runSet());
        getButton.addActionListener(event -> runGet());
        deleteButton.addActionListener(event -> runDelete());

        return panel;
    }

    private JScrollPane createCachePanel() {
        cacheTable.setFillsViewportHeight(true);
        cacheTable.setRowHeight(26);
        cacheTable.getTableHeader().setFont(cacheTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        JScrollPane scrollPane = new JScrollPane(cacheTable);
        scrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Active Cache"));
        return scrollPane;
    }

    private JScrollPane createLogPanel() {
        logArea.setEditable(false);
        logArea.setRows(8);
        logArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Logs"));
        return scrollPane;
    }

    private void runSet() {
        String key = keyField.getText().trim();
        String value = valueField.getText().trim();
        String ttl = ttlField.getText().trim();

        String command = ttl.isEmpty()
                ? "SET " + key + " " + value
                : "SET " + key + " " + value + " " + ttl;
        showResponse(commandProcessor.process(command));
        refreshCacheTable();
    }

    private void runGet() {
        String key = keyField.getText().trim();
        String response = commandProcessor.process("GET " + key);
        showResponse("GET result: " + response);
        refreshCacheTable();
    }

    private void runDelete() {
        String key = keyField.getText().trim();
        showResponse(commandProcessor.process("DELETE " + key));
        refreshCacheTable();
    }

    private void showResponse(String response) {
        onEvent("UI command response: " + response);
        JOptionPane.showMessageDialog(this, response);
    }

    private void refreshCacheTable() {
        Map<String, String> snapshot = server.getStorage().snapshot();
        tableModel.setRowCount(0);
        for (Map.Entry<String, String> entry : snapshot.entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    private void startServer() {
        Thread serverThread = new Thread(server::start, "mini-redis-server");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void startUiRefresh() {
        Timer timer = new Timer(1000, event -> refreshCacheTable());
        timer.start();
    }

    @Override
    public void onEvent(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + System.lineSeparator());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingFrontend().setVisible(true));
    }
}
