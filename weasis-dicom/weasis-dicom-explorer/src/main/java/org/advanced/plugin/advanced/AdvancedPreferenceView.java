package org.advanced.plugin.advanced;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class AdvancedPreferenceView {
   private JPanel rootPane;

   public AdvancedPreferenceView() {
      this.$$$setupUI$$$();
   }

   public JPanel getRootPane() {
      return this.rootPane;
   }

   private void $$$setupUI$$$() {
      this.rootPane = new JPanel();
      this.rootPane.setLayout(new GridBagLayout());
      this.rootPane.setBorder(BorderFactory.createTitledBorder((Border)null, "Advanced Settings", 0, 0, (Font)null, (Color)null));
      JPanel spacer1 = new JPanel();
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.weighty = (double)1.0F;
      gbc.fill = 3;
      this.rootPane.add(spacer1, gbc);
      JPanel spacer2 = new JPanel();
      gbc = new GridBagConstraints();
      gbc.gridx = 4;
      gbc.gridy = 0;
      gbc.weightx = (double)1.0F;
      gbc.fill = 2;
      this.rootPane.add(spacer2, gbc);
      JPanel panel1 = new JPanel();
      panel1.setLayout(new GridBagLayout());
      gbc = new GridBagConstraints();
      gbc.gridx = 1;
      gbc.gridy = 1;
      gbc.anchor = 11;
      gbc.fill = 2;
      this.rootPane.add(panel1, gbc);
      JPanel spacer3 = new JPanel();
      gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.fill = 3;
      this.rootPane.add(spacer3, gbc);
   }

   public JComponent $$$getRootComponent$$$() {
      return this.rootPane;
   }
}
