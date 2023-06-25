import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import it.*;
import it.Syntaxer;
import it.util.SintaticAnalysis.*;

public class GUI {
    public JFrame frame;
    public File dirLib;
    public File source;

    public GUI() {
        dirLib = new File("C:\\Users\\Gennaro Pio Rimoli\\IdeaProjects\\MyFun2C\\libC");
        source = new File("C:\\Users\\Gennaro Pio Rimoli\\IdeaProjects\\MyFun2C\\src\\test\\Obbl\\0\\Es0");
        initialize();
    }

    private void initialize() {
        try {
            frame = new JFrame("Compilatore MyFun2C");
            frame.setBounds(100, 100, 600, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            frame.add(panel);


            JButton bottone1 = new JButton("Libs");
            JButton bottone2 = new JButton("Source");
            JButton bottone3 = new JButton("Compila");
            JLabel lblNewLabel = new JLabel(dirLib.getCanonicalPath());

            JLabel lblNewLabel_1 = new JLabel(source.getCanonicalPath());
            JLabel lblNewLabel_2 = new JLabel("In attesa di azioni");

            JCheckBox checkBox = new JCheckBox("Attiva Debug");

//            Questo serve nel caso non sono sul mio pc per indicare dove sono le librerie necessarie
//            panel.add(bottone1);
//            panel.add(lblNewLabel);
            panel.add(bottone2);
            panel.add(lblNewLabel_1);
            panel.add(checkBox);
            panel.add(bottone3);
            panel.add(lblNewLabel_2);


            bottone1.addActionListener(event -> {
                try {
                    JFileChooser j = new JFileChooser(dirLib.getCanonicalPath());
                    int r = j.showSaveDialog(frame);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        String path = j.getSelectedFile().getAbsolutePath();
                        dirLib = new File(path);
                        lblNewLabel.setText(path);
                        frame.repaint();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            bottone2.addActionListener(event -> {
                JFileChooser j = new JFileChooser("C:\\Users\\Gennaro Pio Rimoli\\IdeaProjects\\MyFun2C\\src\\test");
                int r = j.showSaveDialog(frame);
                if (r == JFileChooser.APPROVE_OPTION) {
                    String path = j.getSelectedFile().getAbsolutePath();
                    source = new File(path);
                    lblNewLabel_1.setText(path);
                    frame.repaint();
                }
            });

            bottone3.addActionListener(event -> {
                try {
                    Syntaxer p = new Syntaxer(new Lexer(new FileReader(source)));
                    SintaticNode root = (SintaticNode) p.parse().value;

                    Semantiker semantiker = new Semantiker(root, source, checkBox.isSelected());
                    semantiker.analizza();

                    Traducer t = new Traducer(root, dirLib, source);
                    t.traduce();

//                    Runtime.getRuntime().exec("cmd /c make.bat", null, dirLib);

                    lblNewLabel_2.setText("Compilazione avvenuta con successo");
                } catch (Exception e) {
                    e.printStackTrace();
                    lblNewLabel_2.setText("Error -> " + e.getMessage());
                } finally {
                    frame.repaint();
                }

            });
        } catch (IOException ignored) {/*ignored*/}
    }

}
