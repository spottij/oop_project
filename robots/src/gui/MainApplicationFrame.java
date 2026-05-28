package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;

public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private static final File CONFIG_FILE = new File(System.getProperty("user.home"), ".robots_config.properties");

    public MainApplicationFrame() {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width  - inset*2,
                screenSize.height - inset*2);

        setContentPane(desktopPane);

        RobotModel robotModel = new RobotModel();

        LogWindow logWindow = createLogWindow();
        logWindow.setName("LogWindow");
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow(robotModel);
        gameWindow.setName("GameWindow");
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);

        RobotStateWindow stateWindow = new RobotStateWindow(robotModel);
        stateWindow.setName("StateWindow");
        stateWindow.setSize(250, 150);
        stateWindow.setLocation(320, 10);
        addWindow(stateWindow);

        setJMenuBar(generateMenuBar());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        loadFramesState();
    }

    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);

        logWindow.setSize(300, 800);
        setMinimumSize(new Dimension(400, 400));

        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    /**
     * Генерация главного меню приложения через вызовы декомпозированных методов.
     */
    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createFileMenu());
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());

        return menuBar;
    }

    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem exitItem = new JMenuItem("Выход", KeyEvent.VK_Q);
        exitItem.addActionListener((event) -> exitApplication());
        fileMenu.add(exitItem);

        return fileMenu;
    }

    private JMenu createLookAndFeelMenu()
    {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(systemLookAndFeel);

        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
        crossplatformLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(crossplatformLookAndFeel);

        return lookAndFeelMenu;
    }

    private JMenu createTestMenu()
    {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription("Тестовые команды");

        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> {
            Logger.debug("Новая строка");
        });
        testMenu.add(addLogMessageItem);

        return testMenu;
    }

    /**
     * Единый метод обработки выхода из приложения.
     * Выводит запрос подтверждения на русском языке и сохраняет состояние окон.
     */
    private void exitApplication() {
        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Вы уверены, что хотите закрыть приложение?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            saveFramesState();
            System.exit(0);
        }
    }

    private void saveFramesState() {
        Properties props = new Properties();
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            String prefix = frame.getName();
            if (prefix == null) continue;

            props.setProperty(prefix + ".x", String.valueOf(frame.getX()));
            props.setProperty(prefix + ".y", String.valueOf(frame.getY()));
            props.setProperty(prefix + ".w", String.valueOf(frame.getWidth()));
            props.setProperty(prefix + ".h", String.valueOf(frame.getHeight()));
            props.setProperty(prefix + ".isIcon", String.valueOf(frame.isIcon()));
            props.setProperty(prefix + ".isMax", String.valueOf(frame.isMaximum()));
        }

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Robots Application Windows Geometry Configuration");
        } catch (IOException e) {
            Logger.error("Ошибка при сохранении конфигурации окон: " + e.getMessage());
        }
    }

    private void loadFramesState() {
        if (!CONFIG_FILE.exists()) return;

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (IOException e) {
            Logger.error("Ошибка при загрузке конфигурации окон: " + e.getMessage());
            return;
        }

        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            String prefix = frame.getName();
            if (prefix == null || !props.containsKey(prefix + ".x")) continue;

            try {
                int x = Integer.parseInt(props.getProperty(prefix + ".x"));
                int y = Integer.parseInt(props.getProperty(prefix + ".y"));
                int w = Integer.parseInt(props.getProperty(prefix + ".w"));
                int h = Integer.parseInt(props.getProperty(prefix + ".h"));
                boolean isIcon = Boolean.parseBoolean(props.getProperty(prefix + ".isIcon"));
                boolean isMax = Boolean.parseBoolean(props.getProperty(prefix + ".isMax"));

                frame.setBounds(x, y, w, h);
                frame.setIcon(isIcon);
                frame.setMaximum(isMax);
            } catch (NumberFormatException | PropertyVetoException e) {
                Logger.error("Не удалось恢复параметры для окна: " + prefix);
            }
        }
    }

    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }
}