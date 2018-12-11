package com.ardublock.ui.listener;

import com.ardublock.core.Context;
import com.ardublock.translator.AutoFormat;
import com.ardublock.translator.Translator;
import com.ardublock.translator.block.exception.BlockException;
import com.ardublock.translator.block.exception.SocketNullException;
import com.ardublock.translator.block.exception.SubroutineNameDuplicatedException;
import com.ardublock.translator.block.exception.SubroutineNotDeclaredException;
import com.ardublock.ui.UploadingCodeScreen;
import edu.mit.blocks.codeblocks.Block;
import edu.mit.blocks.renderable.RenderableBlock;
import edu.mit.blocks.workspace.Workspace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GenerateCodeButtonListener implements ActionListener {

    private JFrame parentFrame;
    private Context context;
    private Workspace workspace;
    private ResourceBundle uiMessageBundle;

    public GenerateCodeButtonListener(JFrame frame, Context context) {
        this.parentFrame = frame;
        this.context = context;
        workspace = context.getWorkspaceController().getWorkspace();
        uiMessageBundle = ResourceBundle.getBundle("com/ardublock/block/ardublock");
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arduino Programm", "ino", "c++ programm");
        chooser.setFileFilter(filter);
        int retrival = chooser.showSaveDialog(null);
        if (retrival == JFileChooser.APPROVE_OPTION) {
            boolean success;
            success = true;
            Translator translator = new Translator(workspace);
            translator.reset();

            Iterable<RenderableBlock> renderableBlocks = workspace.getRenderableBlocks();

            Set<RenderableBlock> loopBlockSet = new HashSet<RenderableBlock>();
            Set<RenderableBlock> setupBlockSet = new HashSet<RenderableBlock>();
            Set<RenderableBlock> subroutineBlockSet = new HashSet<RenderableBlock>();
            StringBuilder code = new StringBuilder();

            for (RenderableBlock renderableBlock : renderableBlocks) {
                Block block = renderableBlock.getBlock();

                if (!block.hasPlug() && (Block.NULL.equals(block.getBeforeBlockID()))) {
                    if (block.getGenusName().equals("loop")) {
                        loopBlockSet.add(renderableBlock);
                    }
                    if (block.getGenusName().equals("setup")) {
                        setupBlockSet.add(renderableBlock);
                    }
                    if (block.getGenusName().equals("subroutine")) {
                        String functionName = block.getBlockLabel().trim();
                        try {
                            translator.addFunctionName(block.getBlockID(), functionName);
                        } catch (SubroutineNameDuplicatedException e1) {
                            // Highlight the offending block.
                            context.highlightBlock(renderableBlock);

                            // Display an error message.
                            JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.subroutineNameDuplicated"), "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        subroutineBlockSet.add(renderableBlock);
                    }
                    if (block.getGenusName().equals("subroutine_var")) {
                        String functionName = block.getBlockLabel().trim();
                        try {
                            translator.addFunctionName(block.getBlockID(), functionName);
                        } catch (SubroutineNameDuplicatedException e1) {
                            // Highlight the offending block.
                            context.highlightBlock(renderableBlock);

                            // Display an error message.
                            JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.subroutineNameDuplicated"), "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        subroutineBlockSet.add(renderableBlock);
                    }
                }
            }

            // Make sure there's exactly one "loop" block.
            if (loopBlockSet.isEmpty()) {
                // Display an error message.
                JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.noLoopFound"), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (loopBlockSet.size() > 1) {
                // Highlight each "loop" block.
                for (RenderableBlock rb : loopBlockSet) {
                    context.highlightBlock(rb);
                }

                // Display an error message.
                JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.multipleLoopFound"), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Make sure there's no more than one "setup" block.
            if (setupBlockSet.size() > 1) {
                // Highlight each "setup" block.
                for (RenderableBlock rb : setupBlockSet) {
                    context.highlightBlock(rb);
                }

                // Display an error message.
                JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.multipleSetupFound"), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Translate the loop code blocks.

                // Translate other subroutine code blocks.
                for (RenderableBlock renderableBlock : subroutineBlockSet) {
                    translator.setRootBlockName("subroutine");
                    Block subroutineBlock = renderableBlock.getBlock();
                    code.append(translator.translate(subroutineBlock.getBlockID()));
                }

                // Translate setup code blocks.
                for (RenderableBlock renderableBlock : setupBlockSet) {
                    translator.setRootBlockName("setup");
                    Block setupBlock = renderableBlock.getBlock();
                    code.append(translator.translate(setupBlock.getBlockID()));
                }

                for (RenderableBlock renderableBlock : loopBlockSet) {
                    translator.setRootBlockName("loop");
                    Block loopBlock = renderableBlock.getBlock();
                    code.append(translator.translate(loopBlock.getBlockID()));
                }
                translator.setRootBlockName("setup");

                translator.beforeGenerateHeader();
                code.insert(0, translator.generateHeaderCommand());
            } catch (SocketNullException e1) {
                e1.printStackTrace(System.err);
                success = false;
                Long blockId = e1.getBlockId();
                Iterable<RenderableBlock> blocks = workspace.getRenderableBlocks();
                for (RenderableBlock renderableBlock2 : blocks) {
                    Block block2 = renderableBlock2.getBlock();
                    if (block2.getBlockID().equals(blockId)) {
                        context.highlightBlock(renderableBlock2);
                        break;
                    }
                }
                JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.socketNull"), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (BlockException e2) {
                e2.printStackTrace(System.err);
                success = false;
                Long blockId = e2.getBlockId();
                Iterable<RenderableBlock> blocks = workspace.getRenderableBlocks();
                for (RenderableBlock renderableBlock2 : blocks) {
                    Block block2 = renderableBlock2.getBlock();
                    if (block2.getBlockID().equals(blockId)) {
                        context.highlightBlock(renderableBlock2);
                        break;
                    }
                }
                JOptionPane.showMessageDialog(parentFrame, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SubroutineNotDeclaredException e3) {
                e3.printStackTrace(System.err);
                success = false;
                Long blockId = e3.getBlockId();
                Iterable<RenderableBlock> blocks = workspace.getRenderableBlocks();
                for (RenderableBlock renderableBlock3 : blocks) {
                    Block block2 = renderableBlock3.getBlock();
                    if (block2.getBlockID().equals(blockId)) {
                        context.highlightBlock(renderableBlock3);
                        break;
                    }
                }
                JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.subroutineNotDeclared"), "Error", JOptionPane.ERROR_MESSAGE);
            }

            if (success) {
                AutoFormat formatter = new AutoFormat();
                String codeOut = code.toString();

                if (context.isNeedAutoFormat) {
                    codeOut = formatter.format(codeOut);
                }

                if (!context.isInArduino()) {
                    System.out.println(codeOut);
                }
                File file = null;
                if (chooser.getSelectedFile().getParent() == null || !chooser.getSelectedFile().getParent().equals(chooser.getSelectedFile().getName())) {
                    file = new File(chooser.getSelectedFile().getAbsolutePath(), chooser.getSelectedFile().getName().replaceAll(".ino", "") + ".ino");
                    File create = new File(file.getParent());
                    create.mkdirs();
                } else {
                    file = chooser.getSelectedFile();
                }
                System.out.println(file.getAbsolutePath());
                try (FileWriter fw = new FileWriter(file.getAbsolutePath())) {
                    fw.write(codeOut);
                } catch (Exception ex) {

                }
                try {
                    this.runCommandInUploadScreen("\"C:\\Program Files (x86)\\Arduino\\arduino_debug.exe\" --install-boards esp8266:esp8266", "", "");
                } catch (IOException ex) {
                    Logger.getLogger(GenerateCodeButtonListener.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (String library : translator.getLibrarys()) {
                    try {
                        this.runCommandInUploadScreen("\"C:\\Program Files (x86)\\Arduino\\arduino_debug.exe\" --install-library \"" + library + "\" ", "", "");
                    } catch (IOException ex) {
                        Logger.getLogger(GenerateCodeButtonListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    this.runCommandInUploadScreen("\"C:\\Program Files (x86)\\Arduino\\arduino_debug.exe\" --verify \"" + file.getAbsolutePath() + ".ino\" --board esp8266:esp8266:nodemcuv2", "Código Enviado", "Falha ao Enviar o Código");
                } catch (IOException ex) {
                    Logger.getLogger(GenerateCodeButtonListener.class.getName()).log(Level.SEVERE, null, ex);
                }
                context.didGenerate(codeOut);
            }
        }
    }

    private void runCommandInUploadScreen(String command, String a, String b) throws IOException {
        Runtime rt = Runtime.getRuntime();
        final Process pr = rt.exec(command);
        UploadingCodeScreen telaUpload = new UploadingCodeScreen(pr, a, b);
        new Thread(new Runnable() {
            public void run() {
                BufferedReader input = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                String line = null;

                try {
                    while ((line = input.readLine()) != null) {
                        telaUpload.updateText(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            public void run() {
                BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                String line = null;

                try {
                    while ((line = input.readLine()) != null) {
                        telaUpload.updateText(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        telaUpload.setVisible(true);
    }
}
