package it.unibo.oop.reactivegui03;

import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int TIME = 10_000;

    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;

    private final JButton stop = new JButton("STOP");
    private final JButton up = new JButton("UP");
    private final JButton down = new JButton("DOWN");
    private final JLabel display = new JLabel();

    /**
     * Builds a another CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        final JPanel panel = new JPanel();
        final JPanel panelButton = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(display, FlowLayout.LEFT);
        panelButton.setLayout(new FlowLayout());
        panelButton.add(up, FlowLayout.LEFT);
        panelButton.add(down, FlowLayout.CENTER);
        panelButton.add(stop, FlowLayout.RIGHT);
        panel.add(panelButton, FlowLayout.CENTER);
        this.getContentPane().add(panel);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        final Agent agent = new Agent();
        new Thread(agent).start();

        stop.addActionListener(e -> {
            agent.stopCounting();
            up.setEnabled(false);
            down.setEnabled(false);
        });
        up.addActionListener(e -> agent.timeUp());
        down.addActionListener(e -> agent.timeDown());

        new Thread(() -> {
            try {
                Thread.sleep(TIME);
            } catch (InterruptedException e1) {
                e1.printStackTrace(); //NOPMD
            }
            agent.stopCounting();
            try {
                SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.up.setEnabled(false));
                SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.down.setEnabled(false));
            } catch (InvocationTargetException | InterruptedException e1) {
                e1.printStackTrace(); //NOPMD
            }
        }).start();
    }

    /**
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class Agent implements Runnable {

        private volatile boolean stop;
        private volatile boolean up = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if (this.up) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    ex.printStackTrace(); // NOPMD
                }
            }
        }

        public void stopCounting() {
            this.stop = true;
        }

        public void timeUp() {
            this.up = true;
        }

        public void timeDown() {
            this.up = false;
        }
    }
}
