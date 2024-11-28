package org.weasis.dicom.explorer.pref.download;

import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.WProperties;
import org.weasis.dicom.explorer.DicomExplorer;
import org.weasis.dicom.explorer.DicomSorter;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.Messages;

public class DicomExplorerPrefView extends AbstractItemDialogPage {
   public static final String DOWNLOAD_IMMEDIATELY = "weasis.download.immediately";
   public static final String DOWNLOAD_OPEN_MODE = "weasis.download.open.view.mode";
   public static final String STUDY_DATE_SORTING = "weasis.sorting.study.date";
   private final JCheckBox downloadImmediatelyCheckbox = new JCheckBox(Messages.getString("SeriesDownloadPrefView.downloadImmediatelyCheckbox"));
   private final JSpinner spinner;
   private final JSpinner colSpinner;
   private final JComboBox openingViewerJComboBox = new JComboBox(HangingProtocols.OpeningViewer.values());
   private final JComboBox studyDateSortingComboBox = new JComboBox(DicomSorter.SortingTime.values());

   public DicomExplorerPrefView() {
      super(Messages.getString("DicomExplorer.title"), 607);
      WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
      int thumbnailSize = preferences.getIntProperty("explorer.thumbnail.size", 144);
      int colSize = preferences.getIntProperty("explorer.thumbnail.colsize", 1);
      JLabel thumbSize = new JLabel(Messages.getString("DicomExplorer.thmb_size"));
      SpinnerListModel model = new SpinnerListModel(List.of(48, 64, 72, 96, 124, 144, 160, 176, 192, 208, 224, 240, 256));
      this.spinner = new JSpinner(model);
      model.setValue(thumbnailSize);
      SpinnerListModel colModel = new SpinnerListModel(List.of(1, 2, 3));
      this.colSpinner = new JSpinner(colModel);
      colModel.setValue(colSize);
      this.add(GuiUtils.getFlowLayoutPanel(2, 5, thumbSize, this.spinner, this.colSpinner));
      JLabel labelStudyDate = new JLabel(Messages.getString("study.date.sorting") + ":");
      this.studyDateSortingComboBox.setSelectedItem(DicomSorter.getStudyDateSorting());
      this.add(GuiUtils.getFlowLayoutPanel(2, 5, labelStudyDate, this.studyDateSortingComboBox));
      JLabel labelOpenPatient = new JLabel(Messages.getString("DicomExplorer.open_win") + ":");
      this.openingViewerJComboBox.setSelectedItem(this.getOpeningViewer());
      this.add(GuiUtils.getFlowLayoutPanel(2, 5, labelOpenPatient, this.openingViewerJComboBox));
      this.downloadImmediatelyCheckbox.setSelected(preferences.getBooleanProperty("weasis.download.immediately", true));
      this.add(GuiUtils.getFlowLayoutPanel(2, 5, this.downloadImmediatelyCheckbox));
      this.add(GuiUtils.boxYLastElement(5));
      this.getProperties().setProperty("show.apply", Boolean.TRUE.toString());
      this.getProperties().setProperty("show.restore", Boolean.TRUE.toString());
      this.getProperties().setProperty("help.item", "dicom-explorer/#preferences");
   }

   private HangingProtocols.OpeningViewer getOpeningViewer() {
      String key = GuiUtils.getUICore().getSystemPreferences().getProperty("weasis.download.open.view.mode");
      return HangingProtocols.OpeningViewer.getOpeningViewer(key, HangingProtocols.OpeningViewer.ALL_PATIENTS);
   }

   public void resetToDefaultValues() {
      WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
      GuiUtils.getUICore().getSystemPreferences().resetProperty("weasis.download.immediately", Boolean.TRUE.toString());
      this.downloadImmediatelyCheckbox.setSelected(preferences.getBooleanProperty("weasis.download.immediately", true));
      preferences.resetProperty("weasis.sorting.study.date", String.valueOf(DicomSorter.SortingTime.INVERSE_CHRONOLOGICAL.getId()));
      this.studyDateSortingComboBox.setSelectedItem(DicomSorter.getStudyDateSorting());
      preferences.resetProperty("weasis.download.open.view.mode", HangingProtocols.OpeningViewer.ALL_PATIENTS.name());
      this.openingViewerJComboBox.setSelectedItem(this.getOpeningViewer());
      this.spinner.setValue(144);
   }

   public void closeAdditionalWindow() {
      WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
      preferences.putBooleanProperty("weasis.download.immediately", this.downloadImmediatelyCheckbox.isSelected());
      DicomSorter.SortingTime sortingTime = (DicomSorter.SortingTime)this.studyDateSortingComboBox.getSelectedItem();
      if (sortingTime != null) {
         preferences.putIntProperty("weasis.sorting.study.date", sortingTime.getId());
      }

      HangingProtocols.OpeningViewer openingViewer = (HangingProtocols.OpeningViewer)this.openingViewerJComboBox.getSelectedItem();
      if (openingViewer != null) {
         preferences.put("weasis.download.open.view.mode", openingViewer.name());
      }

      DataExplorerView dicomView = GuiUtils.getUICore().getExplorerPlugin(DicomExplorer.NAME);
      if (dicomView instanceof DicomExplorer explorer) {
         int size = (Integer)this.spinner.getValue();
         int col = (Integer)this.colSpinner.getValue();
         preferences.putIntProperty("explorer.thumbnail.size", size);
         preferences.putIntProperty("explorer.thumbnail.colsize", col);
         explorer.updateThumbnailSize(size, col);
      }

      GuiUtils.getUICore().saveSystemPreferences();
   }
}
