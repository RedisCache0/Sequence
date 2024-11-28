package org.weasis.core.ui.pref;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.InsertableUtil;
import org.weasis.core.api.gui.PreferencesPageFactory;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.AbstractWizardDialog;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.util.StringUtil;

public class PreferenceDialog extends AbstractWizardDialog {
   private static final Logger LOGGER = LoggerFactory.getLogger(PreferenceDialog.class);
   public static final String KEY_SHOW_APPLY = "show.apply";
   public static final String KEY_SHOW_RESTORE = "show.restore";
   public static final String KEY_HELP = "help.item";
   protected final JButton jButtonHelp = new JButton();
   protected final JButton restoreButton = new JButton(Messages.getString("restore.values"));
   protected final JButton applyButton = new JButton(Messages.getString("LabelPrefView.apply"));
   protected final JPanel bottomPrefPanel;

   public PreferenceDialog(Window parentWin) {
      super(parentWin, Messages.getString("OpenPreferencesAction.title"), ModalityType.APPLICATION_MODAL, new Dimension(600, 450));
      this.bottomPrefPanel = GuiUtils.getFlowLayoutPanel(4, 10, 7, this.jButtonHelp, this.restoreButton, this.applyButton);
      this.jPanelBottom.add(this.bottomPrefPanel, 0);
      this.jButtonHelp.putClientProperty("JButton.buttonType", "help");
      this.applyButton.addActionListener((e) -> {
         if (this.currentPage != null) {
            this.currentPage.closeAdditionalWindow();
         }

      });
      this.restoreButton.addActionListener((e) -> {
         if (this.currentPage != null) {
            this.currentPage.resetToDefaultValues();
         }

      });
      this.initializePages();
      this.pack();
      this.showFirstPage();
   }

   protected void initializePages() {
      Hashtable<String, Object> properties = new Hashtable();
      properties.put("weasis.user.prefs", System.getProperty("weasis.user.prefs", "user"));
      ArrayList<AbstractItemDialogPage> list = new ArrayList();
      GeneralSetting generalSetting = new GeneralSetting(this);
      list.add(generalSetting);
      ViewerPrefView viewerSetting = new ViewerPrefView();
      list.add(viewerSetting);
      DicomPrefView dicomPrefView = new DicomPrefView();
      list.add(dicomPrefView);
      DrawPrefView drawPrefView = new DrawPrefView(this);
      list.add(drawPrefView);
      TestPrefView testPrefView = new TestPrefView();
      list.add(testPrefView);
      BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

      try {
         for(ServiceReference service : context.getServiceReferences(PreferencesPageFactory.class, (String)null)) {
            PreferencesPageFactory factory = (PreferencesPageFactory)context.getService(service);
            if (factory != null) {
               String className = GuiUtils.getUICore().getSystemPreferences().getProperty(factory.getClass().getName());
               if (!StringUtil.hasText(className) || Boolean.parseBoolean(className)) {
                  AbstractItemDialogPage page = factory.createInstance(properties);
                  if (page != null) {
                     int position = page.getComponentPosition();
                     if (position < 1000) {
                        AbstractItemDialogPage mainPage;
                        if (position > 500 && position < 600) {
                           mainPage = viewerSetting;
                        } else if (position > 600 && position < 700) {
                           mainPage = dicomPrefView;
                        } else if (position > 700 && position < 800) {
                           mainPage = drawPrefView;
                        } else {
                           mainPage = generalSetting;
                        }

                        JComponent menuPanel = mainPage.getMenuPanel();
                        mainPage.addSubPage(page, (a) -> this.showPage(page.getTitle()), menuPanel);
                        if (menuPanel != null) {
                           menuPanel.revalidate();
                           menuPanel.repaint();
                        }
                     } else {
                        list.add(page);
                     }
                  }
               }
            }
         }
      } catch (InvalidSyntaxException e) {
         LOGGER.error("Get Preference pages from service", e);
      }

      InsertableUtil.sortInsertable(list);

      for(AbstractItemDialogPage page : list) {
         page.sortSubPages();
         this.pagesRoot.add(new DefaultMutableTreeNode(page));
      }

      this.iniTree();
   }

   protected void selectPage(AbstractItemDialogPage page) {
      if (page != null) {
         super.selectPage(page);
         this.applyButton.setVisible(Boolean.TRUE.toString().equals(page.getProperty("show.apply")));
         this.restoreButton.setVisible(Boolean.TRUE.toString().equals(page.getProperty("show.restore")));
         String helpKey = page.getProperty("help.item");

         for(ActionListener al : this.jButtonHelp.getActionListeners()) {
            this.jButtonHelp.removeActionListener(al);
         }

         this.jButtonHelp.setVisible(StringUtil.hasText(helpKey));
         if (this.jButtonHelp.isVisible()) {
            this.jButtonHelp.addActionListener(GuiUtils.createHelpActionListener(this.jButtonHelp, helpKey));
         }
      }

   }

   public void cancel() {
      this.dispose();
   }

   public void dispose() {
      this.closeAllPages();
      super.dispose();
   }
}
