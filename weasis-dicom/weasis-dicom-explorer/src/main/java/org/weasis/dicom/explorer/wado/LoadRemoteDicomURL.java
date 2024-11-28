//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.weasis.dicom.explorer.wado;

import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JOptionPane;
import org.dcm4che3.util.UIDUtils;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagW;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.TagD.Level;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.ExplorerTask;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.mf.WadoParameters;
import javax.swing.JOptionPane;

public class LoadRemoteDicomURL extends ExplorerTask<Boolean, String> {
    private final URL[] urls;
    private final DicomModel dicomModel;

    public LoadRemoteDicomURL(String[] urls, DataExplorerModel explorerModel) {
        super(Messages.getString("DicomExplorer.loading"), true);

        if (urls != null && explorerModel instanceof DicomModel) {
            URL[] urlRef = new URL[urls.length];

            for(int i = 0; i < urls.length; ++i) {
                if (urls[i] != null) {
                    try {
                        urlRef[i] = new URL(urls[i]);
                    } catch (MalformedURLException var6) {
                    }
                }
            }

            this.urls = urlRef;
            this.dicomModel = (DicomModel)explorerModel;
        } else {
            throw new IllegalArgumentException("invalid parameters");
        }
    }

    public LoadRemoteDicomURL(URL[] urls, DataExplorerModel explorerModel) {
        super(Messages.getString("DicomExplorer.loading"), true);
        if (urls != null && explorerModel instanceof DicomModel) {
            this.urls = urls;
            this.dicomModel = (DicomModel)explorerModel;
        } else {
            throw new IllegalArgumentException("invalid parameters");
        }
    }

    protected Boolean doInBackground() throws Exception {
        String seriesUID = null;
        URL[] var2 = this.urls;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            URL item = var2[var4];
            if (item != null) {
                seriesUID = item.toString();
                break;
            }
        }

        if (seriesUID != null) {
            String unknown = "UNKNOWN";
            MediaSeriesGroup patient = new MediaSeriesGroupNode(TagD.getUID(Level.PATIENT), UIDUtils.createUID(), DicomModel.patient.tagView());
            patient.setTag(TagD.get(1048608), unknown);
            patient.setTag(TagD.get(1048592), unknown);
            this.dicomModel.addHierarchyNode(MediaSeriesGroupNode.rootNode, patient);
            MediaSeriesGroup study = new MediaSeriesGroupNode(TagD.getUID(Level.STUDY), UIDUtils.createUID(), DicomModel.study.tagView());
            this.dicomModel.addHierarchyNode(patient, study);
            Series dicomSeries = new DicomSeries(seriesUID);
            ((Series)dicomSeries).setTag(TagW.ExplorerModel, this.dicomModel);
            ((Series)dicomSeries).setTag(TagD.get(2097166), seriesUID);
            WadoParameters wadoParameters = new WadoParameters("", false);
            ((Series)dicomSeries).setTag(TagW.WadoParameters, wadoParameters);
            SeriesInstanceList seriesInstanceList = new SeriesInstanceList();
            ((Series)dicomSeries).setTag(TagW.WadoInstanceReferenceList, seriesInstanceList);
            this.dicomModel.addHierarchyNode(study, dicomSeries);
            URL[] var8 = this.urls;
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                URL value = var8[var10];
                if (value != null) {
                    String url = value.toString();
                    SopInstance sop = seriesInstanceList.getSopInstance(url, (Integer)null);
                    if (sop == null) {
                        sop = new SopInstance(url, (Integer)null);
                        sop.setDirectDownloadFile(url);
                        seriesInstanceList.addSopInstance(sop);
                    }
                }
            }

            if (!seriesInstanceList.isEmpty()) {
                String modality = (String)TagD.getTagValue(dicomSeries, 524384, String.class);
                boolean ps = "PR".equals(modality) || "KO".equals(modality);
                LoadSeries loadSeries = new LoadSeries(dicomSeries, this.dicomModel, GuiUtils.getUICore().getSystemPreferences().getIntProperty("download.concurrent.series.images", 4), true);
                if (!ps) {
                    loadSeries.startDownloadImageReference(wadoParameters);
                }

                PluginOpeningStrategy openingStrategy = new PluginOpeningStrategy(DownloadManager.getOpeningViewer());
                openingStrategy.prepareImport();
                loadSeries.setPOpeningStrategy(openingStrategy);
                loadSeries.setPriority(new DownloadPriority(patient, study, dicomSeries, true));
                DownloadManager.addLoadSeries(loadSeries, this.dicomModel, true);
                DownloadManager.CONCURRENT_EXECUTOR.prestartAllCoreThreads();
            }
        }

        return true;
    }
}
