package gui;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.EventQueue;

public class RobotStateWindow extends JInternalFrame implements RobotListener {
    private final JLabel xLabel = new JLabel("X: 0.00");
    private final JLabel yLabel = new JLabel("Y: 0.00");
    private final JLabel dirLabel = new JLabel("Direction: 0.00 rad");
    private final RobotModel model;

    public RobotStateWindow(RobotModel model) {
        super("Координаты робота", true, true, true, true);
        this.model = model;
        model.addListener(this);

        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.add(xLabel);
        panel.add(yLabel);
        panel.add(dirLabel);

        getContentPane().add(panel);
        pack();
    }

    @Override
    public void onModelUpdated() {
        EventQueue.invokeLater(() -> {
            xLabel.setText(String.format("X: %.2f", model.getX()));
            yLabel.setText(String.format("Y: %.2f", model.getY()));
            dirLabel.setText(String.format("Угол: %.2f рад", model.getDirection()));
        });
    }
}