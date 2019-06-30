import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class App {
    private JButton clearButton;
    private JTextField textField1;
    private JTextField textField2;
    private JPanel jPanel;
    private JPanel jPanelChild;
    private JButton calculateButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Розробив: ст. групи РІ-151, Крень К. Б.");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CustomPanel customPanel = CustomPanel.createCustomPanel();
            customPanel.setBounds(0, 0, 800, 550);
            frame.getContentPane().add(customPanel);

            App app = new App();
            app.jPanel.setBounds(0,0,800, 600);
            frame.add(app.jPanel);
            app.clearButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    customPanel.clear();
                }
            });
            app.calculateButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (!app.textField1.getText().isEmpty() &&
                            !app.textField2.getText().isEmpty()) {
                        customPanel.calculate(
                                Integer.parseInt(app.textField1.getText()),
                                Integer.parseInt(app.textField2.getText())
                            );
                    }
                }
            });

            frame.setPreferredSize(new Dimension(800, 600));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public JTextField getTextField1() {
        return textField1;
    }
}
