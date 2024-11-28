//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.weasis.dicom.explorer.wado;

import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.ObservableEvent.BasicAction;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.util.NetworkUtil;
import org.weasis.core.util.StreamIOException;
import org.weasis.core.util.StringUtil;
import org.weasis.core.util.StringUtil.Suffix;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.ExplorerTask;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.PluginOpeningStrategy;

public class LoadRemoteDicomManifest extends ExplorerTask<Boolean, String> {
    private static final Logger PrLogger = LoggerFactory.getLogger(LoadRemoteDicomManifest.class);
    private final DicomModel dicomModel;
    private final List<String> xmlFiles;
    private final AtomicInteger retryNb = new AtomicInteger(0);
    private final List<LoadSeries> loadSeriesList = new ArrayList();
    private final PropertyChangeListener propertyChangeListener = (evt) -> {
        if (evt instanceof ObservableEvent event) {
            Object patt0$temp = event.getNewValue();
            if (patt0$temp instanceof LoadSeries series) {
                ObservableEvent.BasicAction cmd = event.getActionCommand();
                if (!BasicAction.LOADING_STOP.equals(cmd) && !BasicAction.LOADING_CANCEL.equals(cmd)) {
                    if (BasicAction.LOADING_START.equals(cmd) && !this.loadSeriesList.contains(series)) {
                        this.loadSeriesList.add(series);
                    }
                } else {
                    this.checkDownloadIssues(series);
                }
            }
        }

    };

    public LoadRemoteDicomManifest(List<String> xmlFiles, DataExplorerModel explorerModel) {
        super(Messages.getString("DicomExplorer.loading"), true);
        if (xmlFiles != null && explorerModel instanceof DicomModel) {
            this.xmlFiles = (List)xmlFiles.stream().filter(Objects::nonNull).collect(Collectors.toList());
            this.dicomModel = (DicomModel)explorerModel;
        } else {
            throw new IllegalArgumentException("invalid parameters");
        }
    }

    private void checkDownloadIssues(LoadSeries loadSeries) {
        if (!loadSeries.hasDownloadFailed()) {
            this.loadSeriesList.remove(loadSeries);
        }

        if (DownloadManager.getTasks().isEmpty() || DownloadManager.getTasks().stream().allMatch(LoadSeries::isStopped)) {
            if (!this.loadSeriesList.isEmpty() && this.tryDownloadingAgain((DownloadException)null)) {
                PrLogger.info("Try downloading ({}) the missing elements", this.retryNb.get());
                List<LoadSeries> oldList = new ArrayList(this.loadSeriesList);
                this.loadSeriesList.clear();
                this.dicomModel.removePropertyChangeListener(this.propertyChangeListener);
                Iterator var3 = oldList.iterator();

                while(var3.hasNext()) {
                    LoadSeries s = (LoadSeries)var3.next();
                    LoadSeries task = s.cancelAndReplace(s);
                    this.loadSeriesList.add(task);
                }

                this.startDownloadingSeries(this.loadSeriesList, true);
                this.dicomModel.addPropertyChangeListener(this.propertyChangeListener);
            } else {
                this.dicomModel.removePropertyChangeListener(this.propertyChangeListener);
            }
        }

    }

    private boolean tryDownloadingAgain(DownloadException e) {
        if (this.retryNb.getAndIncrement() == 0) {
            return true;
        } else {
            boolean[] ret = new boolean[]{false};
            GuiExecutor.invokeAndWait(() -> {
                int confirm = JOptionPane.showConfirmDialog(GuiUtils.getUICore().getApplicationWindow(), getErrorMessage(e), Messages.getString("LoadRemoteDicomManifest.net_err_msg"), 0);
                ret[0] = 0 == confirm;
            });
            return ret[0];
        }
    }

    private static String getErrorMessage(DownloadException e) {
        StringBuilder buf = new StringBuilder();
        if (e == null) {
            buf.append(Messages.getString("LoadRemoteDicomManifest.cannot_download"));
        } else {
            buf.append(StringUtil.getTruncatedString(e.getMessage(), 130, Suffix.THREE_PTS));
            if (e.getCause() instanceof StreamIOException) {
                String serverMessage = e.getCause().getMessage();
                if (StringUtil.hasText(serverMessage)) {
                    buf.append("\n");
                    buf.append(Messages.getString("LoadRemoteDicomManifest.server_resp"));
                    buf.append(": ");
                    buf.append(StringUtil.getTruncatedString(serverMessage, 100, Suffix.THREE_PTS));
                }
            }
        }

        buf.append("\n\n");
        buf.append(Messages.getString("LoadRemoteDicomManifest.download_again"));
        return buf.toString();
    }

    protected void done() {
        DownloadManager.CONCURRENT_EXECUTOR.prestartAllCoreThreads();
    }

    protected Boolean doInBackground() throws Exception {
        try {
            Iterator var4 = this.xmlFiles.iterator();

            while(var4.hasNext()) {
                String xmlFile = (String)var4.next();
                this.downloadManifest(xmlFile);
            }
        } catch (DownloadException var3) {
            DownloadException e = var3;
            PrLogger.error("Download failed", e);
            if (this.tryDownloadingAgain(e)) {
                PrLogger.info("Try downloading again: {}", this.xmlFiles);
                LoadRemoteDicomManifest mf = new LoadRemoteDicomManifest(this.xmlFiles, this.dicomModel);
                mf.retryNb.set(this.retryNb.get());
                mf.execute();
            }
        }

        this.dicomModel.addPropertyChangeListener(this.propertyChangeListener);
        return true;
    }

    private void downloadManifest(String path) throws DownloadException {
        try {
            URI uri = NetworkUtil.getURI(path);
            Collection<LoadSeries> wadoTasks = DownloadManager.buildDicomSeriesFromXml(uri, this.dicomModel);
            this.loadSeriesList.addAll(wadoTasks);
            boolean downloadImmediately = GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.download.immediately", true);
            this.startDownloadingSeries(wadoTasks, downloadImmediately);
        } catch (MalformedURLException | URISyntaxException var5) {
            Exception e = var5;
            PrLogger.error("Loading manifest", e);
        }

    }

    private void startDownloadingSeries(Collection<LoadSeries> wadoTasks, boolean downloadImmediately) {
        if (!wadoTasks.isEmpty()) {
            PluginOpeningStrategy openingStrategy = new PluginOpeningStrategy(DownloadManager.getOpeningViewer());
            openingStrategy.prepareImport();
            Iterator var4 = wadoTasks.iterator();

            while(var4.hasNext()) {
                LoadSeries loadSeries = (LoadSeries)var4.next();
                loadSeries.setPOpeningStrategy(openingStrategy);
                DownloadManager.addLoadSeries(loadSeries, this.dicomModel, downloadImmediately);
            }

            DownloadManager.getTasks().sort(Collections.reverseOrder(new DownloadManager.PriorityTaskComparator()));
        }

    }
}
