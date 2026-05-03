package ca.cgjennings.apps.arkham.plugins;

import ca.cgjennings.apps.arkham.AbstractGameComponentEditor;
import ca.cgjennings.apps.arkham.StrangeEons;
import ca.cgjennings.apps.arkham.StrangeEonsEditor;
import ca.cgjennings.apps.arkham.ToolWindow;
import ca.cgjennings.apps.arkham.component.GameComponent;
import ca.cgjennings.apps.arkham.project.Member;
import ca.cgjennings.apps.arkham.project.Project;
import ca.cgjennings.apps.arkham.project.ProjectView;
import ca.cgjennings.apps.arkham.project.Task;
import ca.cgjennings.ui.DocumentEventAdapter;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import resources.RawSettings;
import resources.SettingExplorerProxy;
import resources.Settings;

/**
 * Panel that contains the setting explorer controls and logic.
 *
 * @author Christopher G. Jennings (<https://cgjennings.ca/contact>)
 * @version 1.0
 */
final class SettingExplorerPanel extends javax.swing.JPanel {

    public SettingExplorerPanel(ToolWindow owner) {
        initComponents();
        table.setModel(model);
        table.getRowSorter().setSortKeys(Collections.singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                refreshTree();
            }
        });
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // use a different colour for inherited keys
                if (current != null) {
                    if (bold == null) {
                        regular = getFont();
                        bold = regular.deriveFont(Font.BOLD);
                    }
                    String key = table.getValueAt(row, table.convertColumnIndexToView(0)).toString();
                    if (current.getOverride(key) == null) {
                        setFont(regular);
                        setForeground(Color.LIGHT_GRAY);
                    } else {
                        setFont(bold);
                        setForeground(Color.WHITE);
                    }
                }
                setToolTipText(String.valueOf(value));
                return this;
            }
            private Font regular, bold;
        });

        owner.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                backgroundUpdater.stop();
                refreshTable();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                backgroundUpdater.start();
                if (editedSinceLastFocusLost) {
                    editedSinceLastFocusLost = false;
                    StrangeEons.getWindow().redrawPreviews();
                }
            }
        });

        owner.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // decide what to pick as default: project if project view has focus,
                // game component if current tab has one, or else shared prefs
                int sel = 2; // sahred prefs
                Project p = StrangeEons.getOpenProject();
                if (p != null && p.getView() != null && p.getView().isFocusOwner()) {
                    sel = 0; // project
                } else {
                    StrangeEonsEditor ed = StrangeEons.getWindow().getActiveEditor();
                    if (ed != null && ed.getGameComponent() != null) {
                        sel = 4; // game component
                    }
                }
                source.setSelectedIndex(sel);
            }
        });

        filterfield.getDocument().addDocumentListener(new DocumentEventAdapter() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                EventQueue.invokeLater(updateFilter);
            }
        });
    }

    private Runnable updateFilter = new Runnable() {
        @Override
        public void run() {
            String text = filterfield.getText();
            if (!text.equals(currentFilterText)) {
                RowFilter newFilter = null;
                if (!text.isEmpty()) {
                    newFilter = RowFilter.regexFilter(text);
                }
                ((TableRowSorter) table.getRowSorter()).setRowFilter(newFilter);
                currentFilterText = text;
            }
        }
    };

    private String currentFilterText = "";

    private Timer backgroundUpdater = new Timer(250, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Settings next = determineSettingsForSelection();
            if (next != current) {
                refreshTable();
            }
        }
    });

    /**
     * Returns the set of keys to list for the settings to be displayed. The
     * value returned depends on the parent combo box's selected value.
     *
     * @param s the settings to display
     * @return
     */
    private Set<String> makeKeySet(Settings s) {
        if (s == getDefaultSettings()) {
            // because we use a standard Settings instance to simulate this,
            // it will have Shared as its parent; we need to return
            // just the immediate key set so that keys from this fake
            // parent are never included
            return s.getKeySet();
        }

        int include = parentCombo.getSelectedIndex();

        Set<String> keys;
        switch (include) {
            case -1:
            case 0:
                keys = s.getKeySet();
                break;
            case 1:
                keys = new HashSet<String>(s.getKeySet());
                Settings p = getParent(s);
                if (p != null) {
                    keys.addAll(p.getKeySet());
                }
                break;
            case 2:
                keys = s.getVisibleKeySet();
                break;
            default:
                throw new AssertionError();
        }
        return keys;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel controlPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        source = new javax.swing.JComboBox();
        settingsInstanceName = new javax.swing.JLabel();
        tableTreeSplitter = new javax.swing.JSplitPane();
        tablePanel = new javax.swing.JPanel();
        javax.swing.JScrollPane tableScroll = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        filterPanel = new javax.swing.JPanel();
        filterfield = new ca.cgjennings.ui.JFilterField();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        parentCombo = new javax.swing.JComboBox();
        treePanel = new javax.swing.JPanel();
        javax.swing.JScrollPane treeScroll = new javax.swing.JScrollPane();
        tree = new javax.swing.JTextArea();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        controlPanel.setBackground(new java.awt.Color(64, 64, 64));
        controlPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(128, 128, 128)));

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()-1f));
        jLabel1.setForeground(new java.awt.Color(192, 192, 192));
        jLabel1.setText("Source");

        source.setFont(source.getFont().deriveFont(source.getFont().getSize()-1f));
        source.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Task Settings for Selected Project Member", "Default Settings", "└ Shared Settings", "    └ Settings for Game of Current Game Component", "        └ Private Settings of Current Game Component", "User Settings" }));
        source.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceActionPerformed(evt);
            }
        });

        settingsInstanceName.setFont(settingsInstanceName.getFont().deriveFont(settingsInstanceName.getFont().getStyle() | java.awt.Font.BOLD, settingsInstanceName.getFont().getSize()-1));
        settingsInstanceName.setForeground(new java.awt.Color(192, 192, 192));
        settingsInstanceName.setText(" ");

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(settingsInstanceName)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(source, 0, 368, Short.MAX_VALUE))
                .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(source, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsInstanceName)
                .addContainerGap())
        );

        add(controlPanel, java.awt.BorderLayout.PAGE_START);

        tableTreeSplitter.setBackground(new java.awt.Color(64, 64, 64));
        tableTreeSplitter.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableTreeSplitter.setDividerLocation(250);
        tableTreeSplitter.setDividerSize(2);
        tableTreeSplitter.setForeground(new java.awt.Color(192, 192, 192));
        tableTreeSplitter.setResizeWeight(0.5);

        tablePanel.setBackground(new java.awt.Color(64, 64, 64));
        tablePanel.setLayout(new java.awt.BorderLayout());

        tableScroll.setBackground(new java.awt.Color(64, 64, 64));
        tableScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableScroll.setForeground(new java.awt.Color(192, 192, 192));

        table.setAutoCreateRowSorter(true);
        table.setBackground(new java.awt.Color(64, 64, 64));
        table.setFont(table.getFont().deriveFont(table.getFont().getSize()-1f));
        table.setForeground(new java.awt.Color(255, 255, 255));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tableKeyPressed(evt);
            }
        });
        tableScroll.setViewportView(table);

        tablePanel.add(tableScroll, java.awt.BorderLayout.CENTER);

        filterPanel.setBackground(java.awt.Color.darkGray);
        filterPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(128, 128, 128)));
        filterPanel.setLayout(new java.awt.GridBagLayout());

        filterfield.setBackground(new java.awt.Color(64, 64, 64));
        filterfield.setForeground(java.awt.Color.lightGray);
        filterfield.setCaretColor(new java.awt.Color(255, 255, 255));
        filterfield.setLabel("Regex setting filter");
        filterfield.setSelectedTextColor(new java.awt.Color(0, 0, 0));
        filterfield.setSelectionColor(new java.awt.Color(255, 200, 0));
        filterfield.setTextForeground(new java.awt.Color(255, 255, 255));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        filterPanel.add(filterfield, gridBagConstraints);

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getSize()-1f));
        jLabel3.setForeground(new java.awt.Color(192, 192, 192));
        jLabel3.setText("Parent settings to list:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 4, 1, 4);
        filterPanel.add(jLabel3, gridBagConstraints);

        parentCombo.setFont(parentCombo.getFont().deriveFont(parentCombo.getFont().getSize()-1f));
        parentCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Immediate Parent Only", "All Inherited Keys" }));
        parentCombo.setSelectedIndex( 2 );
        parentCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parentComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 4);
        filterPanel.add(parentCombo, gridBagConstraints);

        tablePanel.add(filterPanel, java.awt.BorderLayout.SOUTH);

        tableTreeSplitter.setLeftComponent(tablePanel);

        treePanel.setBackground(java.awt.Color.darkGray);
        treePanel.setLayout(new java.awt.BorderLayout());

        treeScroll.setBackground(new java.awt.Color(64, 64, 64));
        treeScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 0, 2));

        tree.setEditable(false);
        tree.setBackground(new java.awt.Color(64, 64, 64));
        tree.setColumns(20);
        tree.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        tree.setForeground(java.awt.Color.white);
        tree.setLineWrap(true);
        tree.setTabSize(4);
        tree.setWrapStyleWord(true);
        tree.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        tree.setCaretColor(new java.awt.Color(255, 255, 255));
        tree.setDisabledTextColor(new java.awt.Color(128, 128, 128));
        tree.setSelectionColor(new java.awt.Color(255, 102, 0));
        treeScroll.setViewportView(tree);

        treePanel.add(treeScroll, java.awt.BorderLayout.CENTER);

        jLabel2.setBackground(new java.awt.Color(32, 32, 32));
        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel2.setForeground(java.awt.Color.orange);
        jLabel2.setText("Value Inheritance");
        jLabel2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(192, 192, 192)), javax.swing.BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        jLabel2.setMinimumSize(new java.awt.Dimension(16, 16));
        jLabel2.setOpaque(true);
        treePanel.add(jLabel2, java.awt.BorderLayout.PAGE_START);

        tableTreeSplitter.setRightComponent(treePanel);

        add(tableTreeSplitter, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // this will be attached to the currently view settings to update the table
    // automatically as its settings change
    private PropertyChangeListener refreshListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == current) {
                String key = evt.getPropertyName();
                String val = (String) evt.getNewValue();
                int row;
                for (row = 0; row < model.getRowCount(); ++row) {
                    if (key.equals(model.getValueAt(row, 0))) {
                        // we found the row matching the key that changed
                        if (val == null) {
                            // it was deleted
                            model.removeRow(row);
                        } else {
                            // the value changed
                            model.setValueAt(val, row, 1);
                        }
                        // no matter what happened, we're done since we found the key
                        return;
                    }
                }
                // we didn't find the key, this must have just been added
                model.addRow(new Object[]{key, val});
            }
        }
    };

    private Settings determineSettingsForSelection() {
        int sel = source.getSelectedIndex();
        if (sel < 0) {
            return null;
        }

        Settings next = null;
        switch (sel) {
            case 0:
                next = getProjectSettings();
                break;
            case 1:
                next = getDefaultSettings();
                break;
            case 2:
                next = getShared();
                break;
            case 3:
                next = getGameSettings();
                break;
            case 4:
                next = getComponentSettings();
                break;
            case 5:
                next = getUser();
                break;
        }

        if (next != null) {
            settingsInstanceName.setText(lastName);
        } else {
            settingsInstanceName.setText("<None>");
        }

        return next;
    }

    private void refreshTree() {
        String key = null;
        int row = table.getSelectedRow();
        if (row >= 0) {
            key = (String) table.getValueAt(row, table.convertColumnIndexToView(0));
        }

        LinkedList<String> stack = new LinkedList<String>();
        if (key != null) {
            Settings node = current;
            while (node != null) {
                String value = node.getOverride(key);
                if (value == null) {
                    value = "<Not defined>";
                }
                String lastNameTemp = lastName;
                String shortName = null;

                boolean checkGlobal = false;
                if (node == getDefaultSettings()) {
                    shortName = " [Default]";
                } else if (node == getUser()) {
                    shortName = " [User]";
                } else if (node == getShared()) {
                    shortName = " [Shared]";
                    checkGlobal = true;
                }

                lastName = lastNameTemp;
                if (shortName != null) {
                    value += shortName;
                }
                stack.push(value);

                if (checkGlobal) {
                    String global = SettingExplorerProxy.globalOverride(key, def.get(key));
                    if (global != null) {
                        stack.push(global + " [Global (plug-in default)]");
                    }
                }

                node = getParent(node);
            }
        }

        StringBuilder b = new StringBuilder(256);
        if (key != null) {
            b.append(key).append("\n\n");
        }
        int level = 0;
        while (!stack.isEmpty()) {
            String value = stack.pop();
            if (level > 0) {
                for (int i = 0; i < level; ++i) {
                    b.append("\u00a0\u00a0");
                }
                b.append("\u2514 ");
            }
            b.append(value).append('\n');
            ++level;
        }

        tree.setText(b.toString());
        tree.select(0, 0);
    }

    private void refreshTable() {
        Settings next = determineSettingsForSelection();

        if (current != null) {
            current.removePropertyChangeListener(refreshListener);
        }

        String selectedKey = null;
        int row = table.getSelectedRow();
        if (row >= 0) {
            selectedKey = (String) table.getValueAt(row, table.convertColumnIndexToView(0));
        }

        model.setRowCount(0);

        int selectedRow = -1;
        if (next != null) {
            for (String k : makeKeySet(next)) {
                if (k.equals(selectedKey)) {
                    selectedRow = model.getRowCount();
                }
                model.addRow(new Object[]{k, next.get(k)});
            }
        }

        if (selectedRow == -1) {
            table.getSelectionModel().clearSelection();
        } else {
            selectedRow = table.convertRowIndexToView(selectedRow);
            table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
            table.scrollRectToVisible(table.getCellRect(selectedRow, table.convertColumnIndexToView(0), true));
        }

        current = next;
        if (next != null) {
            next.addPropertyChangeListener(refreshListener);
        }

        refreshTree();
    }
    private Settings current;

    private void sourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceActionPerformed
        refreshTable();
    }//GEN-LAST:event_sourceActionPerformed

    private void tableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyPressed
        int code = evt.getKeyCode();
        if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_BACK_SPACE) {
            evt.consume();
            int row = table.getSelectedRow();
            if (row < 0) {
                UIManager.getLookAndFeel().provideErrorFeedback(table);
                return;
            }
            if (current == getDefaultSettings()) {
                JOptionPane.showMessageDialog(this, "Default settings cannot be modified.", "Delete Key", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Delete the key " + table.getValueAt(row, table.convertColumnIndexToView(0)) + '?', "Delete Key", JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                table.setValueAt(null, row, table.convertColumnIndexToView(1));
            }
        }
    }//GEN-LAST:event_tableKeyPressed

    private void parentComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parentComboActionPerformed
        refreshTable();
    }//GEN-LAST:event_parentComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel filterPanel;
    private ca.cgjennings.ui.JFilterField filterfield;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox parentCombo;
    private javax.swing.JLabel settingsInstanceName;
    private javax.swing.JComboBox source;
    private javax.swing.JTable table;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JSplitPane tableTreeSplitter;
    private javax.swing.JTextArea tree;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables

    private DefaultTableModel model = new DefaultTableModel(new Object[]{"Key", "Value"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            if (current == getDefaultSettings()) {
                return false;
            }
            return true;
        }

        @Override
        public void setValueAt(Object oValue, int row, int column) {
            String newValue = oValue == null ? null : oValue.toString().trim();

            // can't have empty key name
            if (column == 0 && (newValue == null || newValue.isEmpty())) {
                UIManager.getLookAndFeel().provideErrorFeedback(table);
                return;
            }

            if (newValue == null || !newValue.equals(getValueAt(row, column))) {
                if (newValue != null) {
                    super.setValueAt(newValue, row, column);
                }

                String key = getValueAt(row, 0).toString();
                String val = newValue == null ? null : getValueAt(row, 1).toString();

                if (current == getShared()) {
                    if (val == null) {
                        UIManager.getLookAndFeel().provideErrorFeedback(table);
                        return;
                    } else {
                        RawSettings.setGlobalSetting(key, val);
                    }
                } else {
                    if (val == null) {
                        current.reset(key);
                    } else {
                        current.set(key, val);
                    }
                }

                // don't redraw for projects!
                if (source.getSelectedIndex() > 0) {
                    editedSinceLastFocusLost = true;
                    if (editMayAffectEditedComponent(key)) {
                        StrangeEonsEditor ed = StrangeEons.getActiveEditor();
                        if (ed instanceof AbstractGameComponentEditor) {
                            ((AbstractGameComponentEditor) ed).redrawPreview();
                        }
                    }
                }
                row = table.convertRowIndexToView(row);
                table.getSelectionModel().setSelectionInterval(row, row);
                refreshTable();
            }
        }
    };

    private Settings getDefaultSettings() {
        if (def == null) {
            def = new Settings();
            try {
                def.addSettingsFrom("default.settings");
            } catch (IOException ex) {
                StrangeEons.log.log(Level.SEVERE, null, ex);
            }
        }
        lastName = "Default Settings";
        return def;
    }
    private Settings def;

    private Settings getParent(Settings s) {
        if (s == getDefaultSettings()) {
            return null;
        } else if (s == Settings.getShared()) {
            return getDefaultSettings();
        } else {
            return s.getParent();
        }
    }

    private Settings getShared() {
        lastName = Settings.getShared().toString();
        return Settings.getShared();
    }

    private Settings getUser() {
        lastName = Settings.getUser().toString();
        return Settings.getUser();
    }

    private Settings getProjectSettings() {
        Project p = StrangeEons.getOpenProject();
        if (p != null) {
            ProjectView v = p.getView();
            if (v != null) {
                Member[] sel = v.getSelectedMembers();
                for (int i = sel.length - 1; i >= 0; --i) {
                    if (sel[i] instanceof Task) {
                        lastName = "Settings (" + ((Task) sel[i]).getName() + ")";
                        return ((Task) sel[i]).getSettings();
                    }
                }
            }
        }
        return null;
    }

    private Settings getGameSettings() {
        Settings s = getComponentSettings();
        if (s != null) {
            lastName = s.getParent().toString();
            return s.getParent();
        }
        return null;
    }

    private Settings getComponentSettings() {
        StrangeEonsEditor ed = StrangeEons.getWindow().getActiveEditor();
        if (ed != null) {
            if (ed.getGameComponent() != null) {
                lastName = "Private Settings (" + ed.getGameComponent().getFullName() + ")";
                return ed.getGameComponent().getSettings();
            }
        }
        return null;
    }

    private boolean editMayAffectEditedComponent(String key) {
        GameComponent gc = StrangeEons.getActiveGameComponent();
        if (gc == null) {
            return false;
        }
        Settings s = gc.getSettings();
        while (s != null) {
            if (s == current) {
                return true; // current value comes from the settings we are editing
            }
            if (s.getOverride(key) != null) {
                return false; // current value is hidden by intervening change
            }
            s = getParent(s);
        }
        return true; // key is new; probably won't affect component but can't be sure
    }

    private String lastName;

    private boolean editedSinceLastFocusLost = false;
}
