package ca.cgjennings.apps.arkham.plugins;

import ca.cgjennings.apps.arkham.StrangeEons;
import ca.cgjennings.ui.textedit.CodeType;
import ca.cgjennings.io.EscapedTextCodec;
import ca.cgjennings.ui.dnd.FileDrop;
import ca.cgjennings.ui.theme.ThemeInstaller;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;

/**
 * Dialog for previewing and altering text file encoding.
 *
 * @author Chris Jennings <https://cgjennings.ca/contact>
 */
public final class ChangeEncodingDialog extends javax.swing.JDialog {

    public ChangeEncodingDialog(java.awt.Frame parent, File textFile, Charset cs) {
        super(parent, false);
        initComponents();
        inEncodingList.setSelectedIndex(0);
        outEncodingList.setSelectedIndex(0);
        setFile(textFile, cs);

        final FileDrop.Listener dropListener = (files) -> {
            if (files != null && files.length >= 1) {
                setFile(files[0], null);
            }
        };
        new FileDrop(this, dropListener);
        new FileDrop(previewField, dropListener);

        if (StrangeEons.getOpenProject() != null) {
            StrangeEons.getOpenProject().getView().moveToLocusOfAttention(this);
        }
    }

    public void setFile(File file, Charset cs) {
        this.file = file;

        // reset preview text and offset
        previewField.setText("");

        String windowTitle = ChangeEncodingAction.ACTION_NAME;
        setInfoText("", false);
        if (file != null) {
            try {
                fileBytes = Files.readAllBytes(file.toPath());
                windowTitle += " — " + file.getName();

                if (cs == null) {
                    cs = StandardCharsets.UTF_8;
                    CodeType ct = ChangeEncodingAction.guessCodeType(file);
                    if (ct != null) {
                        cs = ct.getEncodingCharset();
                    }
                }

                inEncodingList.setSelectedValue(cs, true);
                outEncodingList.setSelectedValue(cs, true);
            } catch (IOException ioex) {
                setInfoText("Unable to read input file", true);
                StrangeEons.log.log(Level.WARNING, "unable to read text " + file, ioex);
                fileBytes = null;
                file = null;
            }
        } else {
            setInfoText("", true);
        }

        setTitle(windowTitle);
        inEncodingListValueChanged(null);
    }

    private Charset getSelectedCharset(JList<Charset> list) {
        final Charset sel = list.getSelectedValue();
        return sel == null ? StandardCharsets.UTF_8 : sel;
    }

    private boolean standalone = false;
    private File file;
    private byte[] fileBytes;

    private ListModel<Charset> createCharsetModel(boolean mustSupportEncoding) {
        DefaultListModel<Charset> model = new DefaultListModel<>();

        TreeSet<Charset> charsets = new TreeSet<>((Charset a, Charset b) -> {
            final String lhs = a.toString();
            final String rhs = b.toString();
            final int lhp = specialPosition(lhs);
            final int rhp = specialPosition(rhs);

            if (lhp != rhp) {
                return lhp - rhp;
            }
            return lhs.compareToIgnoreCase(rhs);
        });

        charsets.addAll(Charset.availableCharsets().values());
        if (mustSupportEncoding) {
            charsets.removeIf((cs) -> !cs.canEncode());
        }

        for (Charset c : charsets) {
            model.addElement(c);
        }

        return model;
    }

    private final String[] specialCharsets = new String[]{
        "UTF-8", "ISO-8859-1", "ISO-8859-15", "windows-1252"
    };

    private int specialPosition(String cs) {
        for (int i = 0; i < specialCharsets.length; ++i) {
            if (specialCharsets[i].equals(cs)) {
                return i;
            }
        }
        return 1_000_000;
    }

    private void readTextWithEncoding(Charset encoding) {
        if (file == null) {
            return;
        }

        int oldPos = previewField.viewToModel(previewField.getVisibleRect().getLocation());

        String decoded = "";
        boolean decodeIsOk = true;
        CharsetDecoder decoder = encoding.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            decoded = decoder.decode(ByteBuffer.wrap(fileBytes)).toString();
            setInfoText("", false);
            // check if encoder is compatible
            outEncodingListValueChanged(null);
        } catch (CharacterCodingException ccex) {
            decodeIsOk = false;
            decoder.onMalformedInput(CodingErrorAction.REPLACE);
            decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            try {
                decoded = decoder.decode(ByteBuffer.wrap(fileBytes)).toString();
            } catch (CharacterCodingException ex) {
                // should have replacements instead of throwing
            }
            setInfoText("Not a valid encoding for this file", true);
        }

        if (unescapeCheck.isSelected()) {
            decoded = EscapedTextCodec.unescapeUnicode(decoded);
        }

        final int newPos = Math.min(oldPos, previewField.getDocument().getLength());
        previewField.setText(decoded);
        previewField.setForeground(decodeIsOk ? null : COLOR_ERROR);
        previewField.setCaretPosition(newPos);
        try {
            previewField.scrollRectToVisible(previewField.modelToView(newPos));
        } catch (BadLocationException ble) {
            StrangeEons.log.log(Level.SEVERE, "unexpected", ble);
        }

        saveBtn.setEnabled(decodeIsOk);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        outEncodingList = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        inEncodingList = new javax.swing.JList<>();
        closeBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        previewField = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        wrapLinesCheck = new javax.swing.JCheckBox();
        unescapeCheck = new javax.swing.JCheckBox();
        escapeCheck = new javax.swing.JCheckBox();
        infoLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Current encoding");

        outEncodingList.setModel(createCharsetModel(true));
        outEncodingList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        outEncodingList.setCellRenderer(renderer);
        outEncodingList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                outEncodingListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(outEncodingList);

        jLabel2.setText("New encoding");

        inEncodingList.setModel(createCharsetModel(false));
        inEncodingList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        inEncodingList.setCellRenderer(renderer);
        inEncodingList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                inEncodingListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(inEncodingList);

        closeBtn.setText("Close");
        closeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeBtnActionPerformed(evt);
            }
        });

        saveBtn.setText("Overwrite with New Encoding");
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        previewField.setEditable(false);
        previewField.setColumns(20);
        previewField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        previewField.setLineWrap(true);
        previewField.setRows(5);
        jScrollPane3.setViewportView(previewField);

        jLabel4.setText("Text (when interpreted with current encoding)");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));

        wrapLinesCheck.setSelected(true);
        wrapLinesCheck.setText("Wrap lines in text preview");
        wrapLinesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wrapLinesCheckActionPerformed(evt);
            }
        });

        unescapeCheck.setText("Decode \\uxxxx escape sequences in text preview");
        unescapeCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unescapeCheckActionPerformed(evt);
            }
        });

        escapeCheck.setText("Use \\uxxxx escape sequences when overwriting");
        escapeCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                escapeCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wrapLinesCheck)
                    .addComponent(unescapeCheck)
                    .addComponent(escapeCheck))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(wrapLinesCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unescapeCheck)
                .addGap(18, 18, 18)
                .addComponent(escapeCheck)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        infoLabel.setText("    ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(saveBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeBtn)
                        .addGap(6, 6, 6))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(0, 137, Short.MAX_VALUE))
                            .addComponent(jScrollPane3))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .addComponent(jScrollPane1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeBtn)
                    .addComponent(saveBtn)
                    .addComponent(infoLabel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void wrapLinesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wrapLinesCheckActionPerformed
        final boolean wrap = wrapLinesCheck.isSelected();
        previewField.setLineWrap(wrap);
    }//GEN-LAST:event_wrapLinesCheckActionPerformed

    private void inEncodingListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_inEncodingListValueChanged
        readTextWithEncoding(getSelectedCharset(inEncodingList));
    }//GEN-LAST:event_inEncodingListValueChanged

    private void outEncodingListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_outEncodingListValueChanged
        if (file == null) {
            return;
        }

        // check if can encode to this charset
        String text = previewField.getText();
        if (escapeCheck.isSelected()) {
            text = EscapedTextCodec.escapeUnicode(text);
        }
        Charset cs = getSelectedCharset(outEncodingList);
        CharsetEncoder encoder = cs.newEncoder();
        if (!encoder.canEncode(text)) {
            setInfoText("Text is incompatible with output encoding", true);
        } else {
            setInfoText("", false);
        }
    }//GEN-LAST:event_outEncodingListValueChanged

    private void closeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeBtnActionPerformed
        dispose();
    }//GEN-LAST:event_closeBtnActionPerformed

    private void unescapeCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unescapeCheckActionPerformed
        inEncodingListValueChanged(null);
        outEncodingListValueChanged(null);
    }//GEN-LAST:event_unescapeCheckActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        if (file == null) {
            return;
        }

        String text = previewField.getText();
        if (escapeCheck.isSelected()) {
            text = EscapedTextCodec.escapeUnicode(text);
        }

        Charset cs = getSelectedCharset(outEncodingList);
        try (FileOutputStream fout = new FileOutputStream(file)) {
            Writer out = new BufferedWriter(new OutputStreamWriter(fout, cs));
            out.write(text);
            out.flush();
        } catch (IOException ex) {
            StrangeEons.log.log(Level.SEVERE, "failed to save file: {0}", ex.getLocalizedMessage());
            setInfoText(ex.getLocalizedMessage(), true);
            return;
        }

        StrangeEons.log.log(Level.INFO, "wrote {0} with {1}{2}", new Object[]{file, cs, escapeCheck.isSelected() ? ", escapes" : ""});

        if (standalone || (evt.getModifiers() & ActionEvent.SHIFT_MASK) != ActionEvent.SHIFT_MASK) {
            dispose();
        }
    }//GEN-LAST:event_saveBtnActionPerformed

    private void escapeCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_escapeCheckActionPerformed
        outEncodingListValueChanged(null);
    }//GEN-LAST:event_escapeCheckActionPerformed

    private final Color COLOR_ERROR;

    {
        Color red = Color.RED;
        try {
            if (ThemeInstaller.getInstalledTheme().isDark()) {
                red = red.brighter();
            } else {
                red = red.darker();
            }
        } catch (Throwable t) {
        }
        COLOR_ERROR = red;
    }

    private void setInfoText(String text, boolean error) {
        infoLabel.setText(text);
        infoLabel.setForeground(error ? COLOR_ERROR : null);
        saveBtn.setEnabled(!error);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeBtn;
    private javax.swing.JCheckBox escapeCheck;
    private javax.swing.JList<Charset> inEncodingList;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<Charset> outEncodingList;
    private javax.swing.JTextArea previewField;
    private javax.swing.JButton saveBtn;
    private javax.swing.JCheckBox unescapeCheck;
    private javax.swing.JCheckBox wrapLinesCheck;
    // End of variables declaration//GEN-END:variables

    private final ListCellRenderer renderer = new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (index == specialCharsets.length) {
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                        getBorder()
                ));
            }
            return this;
        }
    };

    /**
     * Runs the change text encoding feature as a standalone tool.
     */
    public static void main(String[] argv) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
                // use default
            }
            ChangeEncodingDialog d = new ChangeEncodingDialog(null, null, null);
            d.standalone = true;
            d.setVisible(true);
        });
    }
}
