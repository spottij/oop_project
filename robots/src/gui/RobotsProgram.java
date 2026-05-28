package gui;

import java.awt.Frame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class RobotsProgram {
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

      UIManager.put("OptionPane.yesButtonText", "Да");
      UIManager.put("OptionPane.noButtonText", "Нет");
      UIManager.put("OptionPane.cancelButtonText", "Отмена");

      UIManager.put("InternalFrameTitlePane.closeButtonToolTip", "Закрыть");
      UIManager.put("InternalFrameTitlePane.minimizeButtonToolTip", "Свернуть");
      UIManager.put("InternalFrameTitlePane.maximizeButtonToolTip", "Развернуть");
      UIManager.put("InternalFrameTitlePane.restoreButtonToolTip", "Восстановить");
    } catch (Exception e) {
      e.printStackTrace();
    }
    SwingUtilities.invokeLater(() -> {
      MainApplicationFrame frame = new MainApplicationFrame();
      frame.pack();
      frame.setVisible(true);
      frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    });
  }
}