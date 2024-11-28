package org.advanced.plugin.advanced;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.osgi.service.component.annotations.Component;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.PreferencesPageFactory;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@Component(
   service = {PreferencesPageFactory.class},
   immediate = false
)
public class LayoutPrefView extends AbstractItemDialogPage implements PreferencesPageFactory {
   private JComboBox cmbModality;
   private JSpinner spinnerRow;
   private JSpinner spinnerCol;
   public JSpinner spinnerFont;
   private JPanel rootPane;
   private JCheckBox SRCheckBox;
   private JCheckBox autoClose;
   private final String KEY = "LayoutPref_";

   public LayoutPrefView() {
      super("Advanced");
      this.$$$setupUI$$$();
      this.setBorder(new EmptyBorder(15, 10, 10, 10));
      this.setLayout(new BoxLayout(this, 1));
      this.add(this.rootPane);
      String modality = this.cmbModality.getItemAt(0).toString();
      this.spinnerRow.setValue(AdvancedPreference.getModalityLayoutRow(modality));
      this.spinnerCol.setValue(AdvancedPreference.getModalityLayoutCol(modality));
      this.spinnerFont.setValue(AdvancedPreference.getFont());
      this.SRCheckBox.setSelected(AdvancedPreference.getSRFilter());
      this.SRCheckBox.addChangeListener((e) -> AdvancedPreference.setSRFilter(this.SRCheckBox.isSelected()));
      this.autoClose.setSelected(AdvancedPreference.getAutoClose());
      this.autoClose.addChangeListener((e) -> AdvancedPreference.setAutoClose(this.autoClose.isSelected()));
      this.spinnerRow.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            LayoutPrefView.this.updateValue();
         }
      });
      this.spinnerCol.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            LayoutPrefView.this.updateValue();
         }
      });
      this.spinnerFont.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            AdvancedPreference.setFont((Integer) spinnerFont.getValue());
         }
      });
      this.cmbModality.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            String modality = LayoutPrefView.this.cmbModality.getSelectedItem().toString();
            LayoutPrefView.this.spinnerRow.setValue(AdvancedPreference.getModalityLayoutRow(modality));
            LayoutPrefView.this.spinnerCol.setValue(AdvancedPreference.getModalityLayoutCol(modality));
         }
      });
   }

   public void updateValue() {
      AdvancedPreference.setModalityLayout(this.cmbModality.getSelectedItem().toString(), (Integer)this.spinnerRow.getValue(), (Integer)this.spinnerCol.getValue());
   }

   private void $$$setupUI$$$() {
      this.rootPane = new JPanel();
      this.rootPane.setLayout(new GridBagLayout());
      this.rootPane.setBorder(BorderFactory.createTitledBorder((Border)null, "Layout", 0, 0, (Font)null, (Color)null));
      JLabel label1 = new JLabel();
      label1.setText("Modality: ");
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.anchor = 17;
      this.rootPane.add(label1, gbc);
      JPanel spacer1 = new JPanel();
      gbc = new GridBagConstraints();
      gbc.gridx = 1;
      gbc.gridy = 0;
      gbc.fill = 2;
      this.rootPane.add(spacer1, gbc);
      JPanel spacer2 = new JPanel();
      gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.fill = 3;
      this.rootPane.add(spacer2, gbc);
      this.cmbModality = new JComboBox();
      DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
      defaultComboBoxModel1.addElement("(Default)");
      defaultComboBoxModel1.addElement("CR");
      defaultComboBoxModel1.addElement("CT");
      defaultComboBoxModel1.addElement("DOC");
      defaultComboBoxModel1.addElement("DX");
      defaultComboBoxModel1.addElement("ES");
      defaultComboBoxModel1.addElement("KO");
      defaultComboBoxModel1.addElement("MG");
      defaultComboBoxModel1.addElement("MR");
      defaultComboBoxModel1.addElement("NM");
      defaultComboBoxModel1.addElement("OT");
      defaultComboBoxModel1.addElement("PR");
      defaultComboBoxModel1.addElement("PT");
      defaultComboBoxModel1.addElement("RF");
      defaultComboBoxModel1.addElement("SC");
      defaultComboBoxModel1.addElement("US");
      defaultComboBoxModel1.addElement("XA");
      this.cmbModality.setModel(defaultComboBoxModel1);
      gbc = new GridBagConstraints();
      gbc.gridx = 2;
      gbc.gridy = 0;
      gbc.gridwidth = 4;
      gbc.anchor = 17;
      gbc.fill = 2;
      this.rootPane.add(this.cmbModality, gbc);
      JLabel label2 = new JLabel();
      label2.setText("Row: ");
      gbc = new GridBagConstraints();
      gbc.gridx = 2;
      gbc.gridy = 2;
      gbc.anchor = 17;
      this.rootPane.add(label2, gbc);
      JLabel label3 = new JLabel();
      label3.setText("Col: ");
      gbc = new GridBagConstraints();
      gbc.gridx = 4;
      gbc.gridy = 2;
      gbc.anchor = 17;
      this.rootPane.add(label3, gbc);
      JPanel spacer3 = new JPanel();
      gbc = new GridBagConstraints();
      gbc.gridx = 6;
      gbc.gridy = 2;
      gbc.weightx = (double)1.0F;
      gbc.fill = 2;
      this.rootPane.add(spacer3, gbc);
      JPanel spacer4 = new JPanel();
      gbc = new GridBagConstraints();
      gbc.gridx = 6;
      gbc.gridy = 5;
      gbc.weighty = (double)1.0F;
      gbc.fill = 3;
      this.rootPane.add(spacer4, gbc);
      this.spinnerRow = new JSpinner();
      gbc = new GridBagConstraints();
      gbc.gridx = 3;
      gbc.gridy = 2;
      gbc.anchor = 17;
      gbc.fill = 2;
      this.rootPane.add(this.spinnerRow, gbc);
      this.spinnerCol = new JSpinner();
      gbc = new GridBagConstraints();
      gbc.gridx = 5;
      gbc.gridy = 2;
      gbc.anchor = 17;
      gbc.fill = 2;
      this.rootPane.add(this.spinnerCol, gbc);
      this.SRCheckBox = new JCheckBox();
      this.SRCheckBox.setText("SR Filter");
      gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.gridwidth = 3;
      gbc.anchor = 17;
      this.rootPane.add(this.SRCheckBox, gbc);
      this.autoClose = new JCheckBox();
      this.autoClose.setText("Auto Close Study and Open Next");
      gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 4;
      gbc.gridwidth = 6;
      gbc.anchor = 17;
      this.rootPane.add(this.autoClose, gbc);
      JLabel label4 = new JLabel();
      label4.setText("Font Size: ");
      gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 5;
      gbc.anchor = 17;
      this.rootPane.add(label4, gbc);
      this.spinnerFont = new JSpinner();
      gbc = new GridBagConstraints();
      gbc.gridx = 2;
      gbc.gridy = 5;
      gbc.gridwidth = 4;
      gbc.anchor = 17;
      this.rootPane.add(this.spinnerFont, gbc);
   }

   public JComponent $$$getRootComponent$$$() {
      return this.rootPane;
   }

   public AbstractItemDialogPage createInstance(Hashtable properties) {
      return new LayoutPrefView();
   }

   public void dispose(Insertable component) {
   }

   public boolean isComponentCreatedByThisFactory(Insertable component) {
      return false;
   }

   public Insertable.Type getType() {
      return null;
   }

   public void resetToDefaultValues() {
   }

   public void closeAdditionalWindow() {
   }
}
