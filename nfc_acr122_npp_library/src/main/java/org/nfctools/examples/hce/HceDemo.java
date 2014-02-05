package org.nfctools.examples.hce;

import javax.smartcardio.CardTerminal;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.nfctools.examples.TerminalUtils;

import java.awt.BorderLayout;

public class HceDemo implements IOnData{
    private static JFrame _frame;
    private static JLabel _label;

    private static class LaunchThread extends Thread{
        private HceDemo _this;
        public LaunchThread(HceDemo demo){
            _this = demo;
        }
        @Override
        public void run(){
            while(true){
                try {
                    CardTerminal cardTerminal = TerminalUtils.getAvailableTerminal().getCardTerminal();
                    HostCardEmulationTagScanner tagScanner = new HostCardEmulationTagScanner(cardTerminal,_this);
                    tagScanner.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    _this.onError("Waiting for ACR122U");
                }
                try{
                    Thread.sleep(500);
                }catch(Exception e){

                }
            }

        }
    }
    public void run() {
        LaunchThread launch = new LaunchThread(this);
        launch.start();
    }

    public static void main(String[] args) {

        _frame = new JFrame("youwillneverfigurethisout");

        _frame.setVisible(true);
        _frame.setTitle("Xbatz GUI Example");
        _frame.setSize(320, 240);
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        _label = new JLabel("Test", SwingConstants.CENTER);
        _frame.getContentPane().add(_label, BorderLayout.CENTER);
        new HceDemo().run();
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
