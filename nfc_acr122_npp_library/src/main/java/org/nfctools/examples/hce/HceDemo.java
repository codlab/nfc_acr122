package org.nfctools.examples.hce;

import javax.imageio.ImageIO;
import javax.smartcardio.CardTerminal;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.nfctools.examples.TerminalUtils;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class HceDemo implements IOnData {
    private static JFrame _frame;
    private static JLabel _label;

    private static class LaunchThread extends Thread {
        private HceDemo _this;

        public LaunchThread(HceDemo demo) {
            _this = demo;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    CardTerminal cardTerminal = TerminalUtils.getAvailableTerminal().getCardTerminal();
                    HostCardEmulationTagScanner tagScanner = new HostCardEmulationTagScanner(cardTerminal, _this);
                    tagScanner.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    _this.onError("Waiting for ACR122U");
                }
                try {
                    Thread.sleep(500);
                } catch (Exception e) {

                }
            }

        }
    }

    public void run() {
        LaunchThread launch = new LaunchThread(this);
        launch.start();
    }


    public static ImageIcon getBitmap(HceDemo hce, String res, int width, int height) {
        BufferedImage img = null;
        try {

            InputStream input = hce.getClass().getResourceAsStream(res);
            img = ImageIO.read(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (img != null) {
            return new ImageIcon(img.getScaledInstance(width, height,
                    Image.SCALE_SMOOTH));
        }
        return null;
    }

    public static void main(String[] args) {

        _frame = new JFrame("youwillneverfigurethisout");

        _frame.setVisible(true);
        _frame.setTitle("AQUINUM's Pass'Node");
        _frame.setSize(320, 240);
        _frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        try {
            _frame.setUndecorated(true);
        } catch (Exception e) {

        }
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        _label = new JLabel("Test", SwingConstants.CENTER);
        HceDemo hce = new HceDemo();
        JLabel label2 = new JLabel(getBitmap(hce, "/aquinum.png", 100, 100), SwingConstants.CENTER);
        _frame.getContentPane().add(_label, BorderLayout.CENTER);
        _frame.getContentPane().add(label2, BorderLayout.PAGE_END);
        hce.run();
    }

    @Override
    public void onUUId(String uuid) {
        _label.setText(uuid);
    }

    @Override
    public void onUserToken(String token) {
        _label.setText(token);
    }

    @Override
    public void onError(String error) {
        _label.setText(error);
    }
}
