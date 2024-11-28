//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.weasis.dicom.explorer.wado;
import java.util.function.Function;
import java.util.Set;
import org.weasis.core.api.util.ResourceUtil.ResourceIconPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.swing.JProgressBar;
import javax.swing.JOptionPane;

import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.util.SafeClose;
import org.weasis.core.api.auth.AuthMethod;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.ObservableEvent.BasicAction;
import org.weasis.core.api.gui.task.SeriesProgressMonitor;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.Filter;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.SeriesImporter;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.api.media.data.MediaSeries.MEDIA_POSITION;
import org.weasis.core.api.media.data.TagW.TagType;
import org.weasis.core.api.model.PerformanceModel;
import org.weasis.core.api.util.AuthResponse;
import org.weasis.core.api.util.ClosableURLConnection;
import org.weasis.core.api.util.HttpResponse;
import org.weasis.core.api.util.NetworkUtil;
import org.weasis.core.api.util.ThreadUtil;
import org.weasis.core.api.util.URLParameters;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.ReferencedImage;
import org.weasis.core.ui.model.ReferencedSeries;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StreamIOException;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.HiddenSeriesManager;
import org.weasis.dicom.codec.HiddenSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.TransferSyntax;
import org.weasis.dicom.codec.TagD.Level;
import org.weasis.dicom.codec.utils.DicomMediaUtils;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomExplorer;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.ExplorerTask;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.ThumbnailMouseAndKeyAdapter;
import org.weasis.dicom.explorer.pacsrapor.PrLogger;
import org.weasis.dicom.mf.ArcParameters;
import org.weasis.dicom.mf.HttpTag;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.mf.WadoParameters;
import org.weasis.dicom.web.Multipart;
import org.weasis.core.api.service.WProperties;

public class LoadSeries extends ExplorerTask<Boolean, String> implements SeriesImporter {
    public static final String CONCURRENT_DOWNLOADS_IN_SERIES = "download.concurrent.series.images";
    public static final File DICOM_TMP_DIR = AppProperties.buildAccessibleTempDirectory(new String[]{"downloading"});
    public static final TagW DOWNLOAD_START_TIME;
    public static final TagW DOWNLOAD_TIME;
    public static final TagW DOWNLOAD_ERRORS;
    public static final String LOAD_TYPE_DICOMDIR = "DICOMDIR";
    public static final String LOAD_TYPE_URL = "URL";
    public static final String LOAD_TYPE_LOCAL = "local";
    public static final String LOAD_TYPE_WADO = "WADO";
    private PluginOpeningStrategy openingStrategy;
    public final int concurrentDownloads;
    private final DicomModel dicomModel;
    private final Series<?> dicomSeries;
    private final SeriesInstanceList seriesInstanceList;
    private final JProgressBar progressBar;
    private final URLParameters urlParams;
    private DownloadPriority priority;
    private final boolean writeInCache;
    private final boolean startDownloading;
    private final AuthMethod authMethod;
    private final AtomicInteger errors;
    private volatile boolean hasError;

    public void setPOpeningStrategy(PluginOpeningStrategy openingStrategy) {
        this.openingStrategy = openingStrategy;
    }

    public PluginOpeningStrategy getOpeningStrategy() {
        return this.openingStrategy;
    }

    public LoadSeries(Series<?> dicomSeries, DicomModel dicomModel, int concurrentDownloads, boolean writeInCache) {
        this(dicomSeries, dicomModel, (AuthMethod)null, concurrentDownloads, writeInCache, true);
    }

    public LoadSeries(Series<?> dicomSeries, DicomModel dicomModel, AuthMethod authMethod, int concurrentDownloads, boolean writeInCache, boolean startDownloading) {
        this(dicomSeries, dicomModel, authMethod, (JProgressBar)null, concurrentDownloads, writeInCache, startDownloading, false);
    }

    public LoadSeries(Series<?> dicomSeries, DicomModel dicomModel, AuthMethod authMethod, JProgressBar progressBar, int concurrentDownloads, boolean writeInCache, boolean startDownloading) {
        this(dicomSeries, dicomModel, authMethod, progressBar, concurrentDownloads, writeInCache, startDownloading, true);
    }

    public LoadSeries(Series<?> dicomSeries, DicomModel dicomModel, AuthMethod authMethod, JProgressBar progressBar, int concurrentDownloads, boolean writeInCache, boolean startDownloading, boolean externalProgress) {
        super(Messages.getString("DicomExplorer.loading"), writeInCache, true);
        this.priority = null;
        this.hasError = false;
        if (dicomModel != null && dicomSeries != null && (progressBar != null || !externalProgress)) {
            this.dicomModel = dicomModel;
            this.dicomSeries = dicomSeries;
            this.authMethod = authMethod;
            this.seriesInstanceList = (SeriesInstanceList)Optional.ofNullable((SeriesInstanceList)dicomSeries.getTagValue(TagW.WadoInstanceReferenceList)).orElseGet(SeriesInstanceList::new);
            this.writeInCache = writeInCache;
            this.progressBar = (JProgressBar)(externalProgress ? progressBar : this.getBar());
            if (!externalProgress && !writeInCache) {
                this.progressBar.setVisible(false);
            }

            this.dicomSeries.setSeriesLoader(this);
            this.concurrentDownloads = concurrentDownloads;
            this.urlParams = new URLParameters(getHttpTags((WadoParameters)dicomSeries.getTagValue(TagW.WadoParameters)));
            this.startDownloading = startDownloading;
            Integer downloadErrors = (Integer)dicomSeries.getTagValue(DOWNLOAD_ERRORS);
            if (downloadErrors == null) {
                downloadErrors = 0;
            }

            this.errors = new AtomicInteger(downloadErrors);
        } else {
            throw new IllegalArgumentException("null parameters");
        }
    }

    protected Boolean doInBackground() {
        return this.startDownload();
    }

    public JProgressBar getProgressBar() {
        return this.progressBar;
    }

    public boolean isStopped() {
        return this.isCancelled();
    }

    public boolean stop() {
        if (!this.isDone()) {
            boolean val = this.cancel();
            this.dicomSeries.setSeriesLoader(this);
            this.dicomSeries.setTag(DOWNLOAD_TIME, this.getDownloadTime());
            return val;
        } else {
            return true;
        }
    }

    public void resume() {
        if (this.isStopped()) {
            this.getPriority().setPriority(DownloadPriority.COUNTER.getAndDecrement());
            this.cancelAndReplace(this);
        }

    }

    protected void done() {
        if (!this.isStopped()) {
            this.progressBar.setIndeterminate(false);
            this.dicomSeries.setSeriesLoader((SeriesImporter)null);
            DownloadManager.removeLoadSeries(this, this.dicomModel);
            String loadType = this.getLoadType();
            String seriesUID = (String)this.dicomSeries.getTagValue(this.dicomSeries.getTagID());
            String modality = (String)TagD.getTagValue(this.dicomSeries, 524384, String.class);

            /* JOptionPane.showMessageDialog(null, modality); */
            int imageNumber = this.getImageNumber();
            long fileSize = this.dicomSeries.getFileSize();
            long time = this.getDownloadTime();
            String rate = this.getDownloadRate(time);
            Integer downloadErrors = (Integer)this.dicomSeries.getTagValue(DOWNLOAD_ERRORS);
            if (downloadErrors == null) {
                downloadErrors = 0;
            }

            if ("WADO".equals(loadType)) {
                String statisticServicePath = GuiUtils.getUICore().getStatisticServiceUrl();
                if (StringUtil.hasText(statisticServicePath)) {
                    Map<String, String> map = new HashMap();
                    map.put("Content-Type", "application/json");
                    URLParameters urlParameters = new URLParameters(map, true);
                    PerformanceModel model = new PerformanceModel(loadType, seriesUID, modality, imageNumber, fileSize, time, rate, downloadErrors);

                    try {
                        ClosableURLConnection http = NetworkUtil.getUrlConnection(statisticServicePath, urlParameters);
                        OutputStream out = http.getOutputStream();

                        try {
                            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                            writer.write((new ObjectMapper()).writeValueAsString(model));
                        } catch (Throwable var20) {
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (Throwable var19) {
                                    var20.addSuppressed(var19);
                                }
                            }

                            throw var20;
                        }

                        if (out != null) {
                            out.close();
                        }

                        URLConnection var27 = http.getUrlConnection();
                        if (var27 instanceof HttpURLConnection) {
                            HttpURLConnection httpURLConnection = (HttpURLConnection)var27;
                            NetworkUtil.readResponse(httpURLConnection, urlParameters.getUnmodifiableHeaders());
                        }
                    } catch (Exception var21) {
                        Exception e = var21;
                        PrLogger.error("Cannot send log to the statisticService service", e);
                    }
                }
            }

            this.dicomSeries.removeTag(DOWNLOAD_START_TIME);
            this.dicomSeries.removeTag(DOWNLOAD_TIME);
            this.dicomSeries.removeTag(DOWNLOAD_ERRORS);
            SeriesThumbnail thumbnail = (SeriesThumbnail)this.dicomSeries.getTagValue(TagW.Thumbnail);
            if (thumbnail != null) {
                thumbnail.setProgressBar((JProgressBar)null);
                if (thumbnail.getThumbnailPath() != null && this.dicomSeries.getTagValue(TagW.DirectDownloadThumbnail) == null) {
                    thumbnail.repaint();
                } else {
                    thumbnail.reBuildThumbnail(MEDIA_POSITION.MIDDLE);
                }
            }

            if (DicomModel.isHiddenModality(this.dicomSeries)) {
                List<HiddenSpecialElement> list = (List)HiddenSeriesManager.getInstance().series2Elements.get(seriesUID);
                if (list != null) {
                    list.stream().filter(Objects::nonNull).forEach((d) -> {
                        this.dicomModel.firePropertyChange(new ObservableEvent(BasicAction.UPDATE, this.dicomModel, (Object)null, d));
                    });
                }
            }

            Integer splitNb = (Integer)this.dicomSeries.getTagValue(TagW.SplitSeriesNumber);
            if (splitNb != null) {
                this.dicomModel.firePropertyChange(new ObservableEvent(BasicAction.UPDATE, this.dicomModel, (Object)null, this.dicomSeries));
            } else if (this.dicomSeries.size((Filter)null) == 0 && this.dicomSeries.getTagValue(TagW.DicomSpecialElementList) == null && !this.hasDownloadFailed()) {
                this.dicomModel.firePropertyChange(new ObservableEvent(BasicAction.REMOVE, this.dicomModel, (Object)null, this.dicomSeries));
            }
        }

    }

    public boolean hasDownloadFailed() {
        return this.hasError;
    }

    public boolean isStartDownloading() {
        return this.startDownloading;
    }

    private String getLoadType() {
        WadoParameters wado = (WadoParameters)this.dicomSeries.getTagValue(TagW.WadoParameters);
        if (wado != null && StringUtil.hasText(wado.getBaseURL())) {
            return "WADO";
        } else if (wado != null) {
            return wado.isRequireOnlySOPInstanceUID() ? "DICOMDIR" : "URL";
        } else {
            return "local";
        }
    }

    private int getImageNumber() {
        int val = this.dicomSeries.size((Filter)null);
        Integer splitNb = (Integer)this.dicomSeries.getTagValue(TagW.SplitSeriesNumber);
        if (splitNb != null) {
            MediaSeriesGroup study = this.dicomModel.getParent(this.dicomSeries, DicomModel.study);
            if (study != null) {
                String uid = (String)TagD.getTagValue(this.dicomSeries, 2097166, String.class);
                if (uid != null) {
                    Collection<MediaSeriesGroup> list = this.dicomModel.getChildren(study);
                    list.remove(this.dicomSeries);
                    Iterator var6 = list.iterator();

                    while(var6.hasNext()) {
                        MediaSeriesGroup s = (MediaSeriesGroup)var6.next();
                        if (s instanceof Series && uid.equals(TagD.getTagValue(s, 2097166))) {
                            val += ((Series)s).size((Filter)null);
                        }
                    }
                }
            }
        }

        return val;
    }

    private long getDownloadTime() {
        Long val = (Long)this.dicomSeries.getTagValue(DOWNLOAD_START_TIME);
        if (val == null) {
            val = 0L;
        } else {
            val = System.currentTimeMillis() - val;
            this.dicomSeries.setTag(DOWNLOAD_START_TIME, (Object)null);
        }

        Long time = (Long)this.dicomSeries.getTagValue(DOWNLOAD_TIME);
        if (time == null) {
            time = 0L;
        }

        return val + time;
    }

    private String getDownloadRate(long time) {
        DecimalFormat format = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        return time <= 0L ? "0" : format.format((double)this.dicomSeries.getFileSize() / (double)time);
    }

    private boolean isSOPInstanceUIDExist(MediaSeriesGroup study, Series<?> dicomSeries, String sopUID) {
        TagW sopTag = TagD.getUID(Level.INSTANCE);
        if (dicomSeries.hasMediaContains(sopTag, sopUID)) {
            return true;
        } else {
            String uid = (String)TagD.getTagValue(dicomSeries, 2097166, String.class);
            if (study != null && uid != null) {
                Iterator var6 = this.dicomModel.getChildren(study).iterator();

                while(var6.hasNext()) {
                    MediaSeriesGroup group = (MediaSeriesGroup)var6.next();
                    if (dicomSeries != group && group instanceof Series) {
                        Series<?> s = (Series)group;
                        if (uid.equals(TagD.getTagValue(group, 2097166)) && s.hasMediaContains(sopTag, sopUID)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    private void incrementProgressBarValue() {
        GuiExecutor.execute(() -> {
            this.progressBar.setValue(this.progressBar.getValue() + 1);
        });
    }

    private Boolean startDownload() {
        MediaSeriesGroup study = this.dicomModel.getParent(this.dicomSeries, DicomModel.study);
        WadoParameters wado = (WadoParameters)this.dicomSeries.getTagValue(TagW.WadoParameters);
        String modality = (String)TagD.getTagValue(this.dicomSeries, 524384, String.class);
        WProperties localPersistence = GuiUtils.getUICore().getLocalPersistence();
        if (wado == null) {
            return false;
        } else {
            List<SopInstance> sopList = this.seriesInstanceList.getSortedList();
            ExecutorService imageDownloader = ThreadUtil.buildNewFixedThreadExecutor(this.concurrentDownloads, "Image Downloader");

            Boolean var17;
            label89: {
                try {
                    ArrayList<Callable<Boolean>> tasks = new ArrayList(sopList.size());
                    int[] dindex = this.generateDownloadOrder(sopList.size());
                    GuiExecutor.execute(() -> {
                        this.progressBar.setMaximum(sopList.size());
                        this.progressBar.setValue(0);
                    });

                    for(int k = 0; k < sopList.size(); ++k) {
                        SopInstance instance = (SopInstance)sopList.get(dindex[k]);
                        if (this.isCancelled()) {
                            var17 = true;
                            break label89;
                        }

                        if (!this.seriesInstanceList.isContainsMultiframes() || this.seriesInstanceList.getSopInstance(instance.getSopInstanceUID()) == instance) {
                            if (this.isSOPInstanceUIDExist(study, this.dicomSeries, instance.getSopInstanceUID())) {
                                this.incrementProgressBarValue();
                            } else {
                                String studyUID = "";
                                String seriesUID = "";
                                if (!wado.isRequireOnlySOPInstanceUID()) {
                                    studyUID = (String)TagD.getTagValue(study, 2097165, String.class);
                                    seriesUID = (String)TagD.getTagValue(this.dicomSeries, 2097166, String.class);
                                }

                                StringBuilder request = new StringBuilder(wado.getBaseURL());
                                String wadoTsuid;
                                if (instance.getDirectDownloadFile() == null) {
                                    request.append("?requestType=WADO&studyUID=");
                                    request.append(studyUID);
                                    request.append("&seriesUID=");
                                    request.append(seriesUID);
                                    request.append("&objectUID=");
                                    request.append(instance.getSopInstanceUID());
                                    request.append("&contentType=application%2Fdicom");
                                    wadoTsuid = (String)this.dicomSeries.getTagValue(TagW.WadoTransferSyntaxUID);
                                    if (StringUtil.hasText(wadoTsuid)) {
                                        request.append("&transferSyntax=");
                                        request.append(wadoTsuid);
                                        Integer rate = (Integer)this.dicomSeries.getTagValue(TagW.WadoCompressionRate);
                                        if (rate != null && rate > 0) {
                                            request.append("&imageQuality=");
                                            request.append(rate);
                                        }
                                    }
                                } else {
                                    request.append(instance.getDirectDownloadFile());
                                }

                                request.append(wado.getAdditionnalParameters());
                                wadoTsuid = request.toString();
                                Download ref = new Download(wadoTsuid);
                                tasks.add(ref);
                            }
                        }
                    }

                    try {
                        this.dicomSeries.setTag(DOWNLOAD_START_TIME, System.currentTimeMillis());
                        imageDownloader.invokeAll(tasks);
                    } catch (InterruptedException var15) {
                        Thread.currentThread().interrupt();
                    }

                    imageDownloader.shutdown();
                } catch (Throwable var16) {
                    if (imageDownloader != null) {
                        try {
                            imageDownloader.close();
                        } catch (Throwable var14) {
                            var16.addSuppressed(var14);
                        }
                    }

                    throw var16;
                }

                if (imageDownloader != null) {
                    imageDownloader.close();
                }

                return true;
            }

            if (imageDownloader != null) {
                imageDownloader.close();
            }

            return var17;
        }
    }

    private static Map<String, String> getHttpTags(WadoParameters wadoParams) {
        boolean hasWadoTags = wadoParams != null && wadoParams.getHttpTaglist() != null;
        boolean hasWadoLogin = wadoParams != null && wadoParams.getWebLogin() != null;
        if (!hasWadoTags && !hasWadoLogin) {
            return null;
        } else {
            HashMap<String, String> map = new HashMap();
            if (hasWadoTags) {
                Iterator var4 = wadoParams.getHttpTaglist().iterator();

                while(var4.hasNext()) {
                    HttpTag tag = (HttpTag)var4.next();
                    map.put(tag.getKey(), tag.getValue());
                }
            }

            if (hasWadoLogin) {
                map.put("Authorization", "Basic " + wadoParams.getWebLogin());
            }

            return map;
        }
    }

  public void startDownloadImageReference(final WadoParameters wadoParameters) {
    if (!seriesInstanceList.isEmpty()) {
      // Sort the UIDs for building the thumbnail that is in the middle of the Series
      List<SopInstance> sopList = seriesInstanceList.getSortedList();
      final SopInstance instance = sopList.get(sopList.size() / 2);

      GuiExecutor.execute(
          () -> {
            SeriesThumbnail thumbnail = (SeriesThumbnail) dicomSeries.getTagValue(TagW.Thumbnail);
            if (thumbnail == null) {
              int thumbnailSize =
                  GuiUtils.getUICore()
                      .getSystemPreferences()
                      .getIntProperty(Thumbnail.KEY_SIZE, Thumbnail.DEFAULT_SIZE);
              Function<String, Set<ResourceIconPath>> drawIcons =
                  HiddenSeriesManager::getRelatedIcons;
              thumbnail = new SeriesThumbnail(dicomSeries, thumbnailSize, drawIcons);
            }
            // In case series is downloaded or canceled
            thumbnail.setProgressBar(LoadSeries.this.isDone() ? null : progressBar);
            thumbnail.registerListeners();
            addListenerToThumbnail(thumbnail, LoadSeries.this, dicomModel);
            dicomSeries.setTag(TagW.Thumbnail, thumbnail);
            dicomModel.firePropertyChange(
                new ObservableEvent(
                    ObservableEvent.BasicAction.ADD, dicomModel, null, dicomSeries));
          });

      loadThumbnail(instance, wadoParameters);
    }
  }

    public void loadThumbnail(final SopInstance instance, final WadoParameters wadoParameters) {
        
        DicomExplorer.thumbDowbloadHandler.addTask(new SwingWorker<Object, Object>() {
            protected String doInBackground() {
                File file = null;
                URLParameters params = LoadSeries.this.urlParams;
                String modality = (String)TagD.getTagValue(LoadSeries.this.dicomSeries, 524384, String.class);
                if (!modality.contains("SR")){
                    /* JOptionPane.showMessageDialog(null, modality); */
                    String thumbURL;
                    String extension;
                    if (instance.getDirectDownloadFile() == null) {
                        thumbURL = "";
                        extension = "";
                        if (!wadoParameters.isRequireOnlySOPInstanceUID()) {
                            MediaSeriesGroup study = LoadSeries.this.dicomModel.getParent(LoadSeries.this.dicomSeries, DicomModel.study);
                            thumbURL = (String)TagD.getTagValue(study, 2097165, String.class);
                            extension = (String)TagD.getTagValue(LoadSeries.this.dicomSeries, 2097166, String.class);
                        }

                        try {
                            file = LoadSeries.this.getJpegThumbnails(wadoParameters, thumbURL, extension, instance.getSopInstanceUID());
                        } catch (Exception var9) {
                            Exception e = var9;
                            PrLogger.error("Downloading thumbnail", e);
                        }
                    } else {
                        extension = ".jpg";
                        if (wadoParameters.isWadoRS()) {
                            thumbURL = (String)TagD.getTagValue(LoadSeries.this.dicomSeries, 528784, String.class);
                            if (thumbURL != null) {
                                thumbURL = thumbURL + "/thumbnail?viewport=256%2C256";
                                HashMap<String, String> headers = new HashMap(LoadSeries.this.urlParams.getUnmodifiableHeaders());
                                headers.put("Accept", "image/jpeg");
                                params = new URLParameters(headers);
                            }
                        } else {
                            thumbURL = (String)LoadSeries.this.dicomSeries.getTagValue(TagW.DirectDownloadThumbnail);
                            if (StringUtil.hasLength(thumbURL)) {
                                if (thumbURL.startsWith(Thumbnail.THUMBNAIL_CACHE_DIR.getPath())) {
                                    file = new File(thumbURL);
                                    thumbURL = null;
                                } else {
                                    String var10000 = wadoParameters.getBaseURL();
                                    thumbURL = var10000 + thumbURL;
                                    extension = FileUtil.getExtension(thumbURL);
                                }
                            }
                        }

                        if (thumbURL != null) {
                            try {
                                HttpResponse httpCon = NetworkUtil.getHttpResponse(thumbURL, params, LoadSeries.this.authMethod);

                                try {
                                    int code = httpCon.getResponseCode();
                                    if (code >= 200 && code < 400) {
                                        File outFile = File.createTempFile("thumb_", extension, Thumbnail.THUMBNAIL_CACHE_DIR);
                                        FileUtil.writeStreamWithIOException(httpCon.getInputStream(), outFile);
                                        if (outFile.length() == 0L) {
                                            FileUtil.delete(outFile);
                                            throw new IllegalStateException("Thumbnail file is empty");
                                        }

                                        file = outFile;
                                    } else if (LoadSeries.this.authMethod != null && code == 401) {
                                        LoadSeries.this.authMethod.resetToken();
                                        LoadSeries.this.authMethod.getToken();
                                    }
                                } catch (Throwable var10) {
                                    if (httpCon != null) {
                                        try {
                                            httpCon.close();
                                        } catch (Throwable var8) {
                                            var10.addSuppressed(var8);
                                        }
                                    }

                                    throw var10;
                                }

                                if (httpCon != null) {
                                    httpCon.close();
                                }
                            } catch (Exception var11) {
                            }
                        }
                    }

                    if (file != null) {
                        File finalfile = file;
                        GuiExecutor.execute(() -> {
                            SeriesThumbnail thumbnail = (SeriesThumbnail)LoadSeries.this.dicomSeries.getTagValue(TagW.Thumbnail);
                            if (thumbnail != null) {
                                thumbnail.reBuildThumbnail(finalfile, MEDIA_POSITION.MIDDLE);
                            }

                        });
                    }

                }
                return null;

            }

            protected void done() {
            }
        });
    }

    public static void removeThumbnailMouseAndKeyAdapter(Thumbnail thumbnail) {
        MouseListener[] listener = thumbnail.getMouseListeners();
        MouseMotionListener[] motionListeners = thumbnail.getMouseMotionListeners();
        KeyListener[] keyListeners = thumbnail.getKeyListeners();
        MouseWheelListener[] wheelListeners = thumbnail.getMouseWheelListeners();
        MouseListener[] var5 = listener;
        int var6 = listener.length;

        int var7;
        for(var7 = 0; var7 < var6; ++var7) {
            MouseListener mouseListener = var5[var7];
            if (mouseListener instanceof ThumbnailMouseAndKeyAdapter) {
                thumbnail.removeMouseListener(mouseListener);
            }
        }

        MouseMotionListener[] var9 = motionListeners;
        var6 = motionListeners.length;

        for(var7 = 0; var7 < var6; ++var7) {
            MouseMotionListener motionListener = var9[var7];
            if (motionListener instanceof ThumbnailMouseAndKeyAdapter) {
                thumbnail.removeMouseMotionListener(motionListener);
            }
        }

        MouseWheelListener[] var10 = wheelListeners;
        var6 = wheelListeners.length;

        for(var7 = 0; var7 < var6; ++var7) {
            MouseWheelListener wheelListener = var10[var7];
            if (wheelListener instanceof ThumbnailMouseAndKeyAdapter) {
                thumbnail.removeMouseWheelListener(wheelListener);
            }
        }

        KeyListener[] var11 = keyListeners;
        var6 = keyListeners.length;

        for(var7 = 0; var7 < var6; ++var7) {
            KeyListener keyListener = var11[var7];
            if (keyListener instanceof ThumbnailMouseAndKeyAdapter) {
                thumbnail.removeKeyListener(keyListener);
            }
        }

    }

    private static void addListenerToThumbnail(Thumbnail thumbnail, LoadSeries loadSeries, DicomModel dicomModel) {
        ThumbnailMouseAndKeyAdapter thumbAdapter = new ThumbnailMouseAndKeyAdapter(loadSeries.getDicomSeries(), dicomModel, loadSeries);
        thumbnail.addMouseListener(thumbAdapter);
        thumbnail.addKeyListener(thumbAdapter);
        if (thumbnail instanceof SeriesThumbnail seriesThumbnail) {
            seriesThumbnail.setProgressBar(loadSeries.getProgressBar());
        }

    }

    public Series<?> getDicomSeries() {
        return this.dicomSeries;
    }

    public File getJpegThumbnails(WadoParameters wadoParameters, String studyUID, String seriesUID, String sopInstanceUID) throws Exception {
        String addParams = wadoParameters.getAdditionnalParameters();
        if (StringUtil.hasText(addParams)) {
            addParams = (String)Arrays.stream(addParams.split("&")).filter((p) -> {
                return !p.startsWith("transferSyntax") && !p.startsWith("anonymize");
            }).collect(Collectors.joining("&"));
        }

        URL url = new URL(wadoParameters.getBaseURL() + "?requestType=WADO&studyUID=" + studyUID + "&seriesUID=" + seriesUID + "&objectUID=" + sopInstanceUID + "&contentType=image/jpeg&imageQuality=70&rows=256&columns=256" + addParams);
        File outFile = File.createTempFile("thumb_", ".jpg", Thumbnail.THUMBNAIL_CACHE_DIR);

        try {
            HttpResponse httpCon = NetworkUtil.getHttpResponse(url.toString(), this.urlParams, this.authMethod);

            try {
                int code = httpCon.getResponseCode();
                if (code >= 200 && code < 400) {
                    FileUtil.writeStreamWithIOException(httpCon.getInputStream(), outFile);
                } else if (this.authMethod != null && code == 401) {
                    this.authMethod.resetToken();
                    this.authMethod.getToken();
                }

                if (outFile.length() == 0L) {
                    throw new IllegalStateException("Thumbnail file is empty");
                }
            } catch (Throwable var16) {
                if (httpCon != null) {
                    try {
                        httpCon.close();
                    } catch (Throwable var15) {
                        var16.addSuppressed(var15);
                    }
                }

                throw var16;
            }

            if (httpCon != null) {
                httpCon.close();
            }
        } finally {
            if (outFile.length() == 0L) {
                FileUtil.delete(outFile);
            }

        }

        return outFile;
    }

    private int[] generateDownloadOrder(int size) {
        int[] dindex = new int[size];
        if (size < 4) {
            for(int i = 0; i < dindex.length; dindex[i] = i++) {
            }

            return dindex;
        } else {
            boolean[] map = new boolean[size];
            int pos = 0;
            dindex[pos++] = 0;
            map[0] = true;
            dindex[pos++] = size - 1;
            map[size - 1] = true;
            int k = (size - 1) / 2;
            dindex[pos++] = k;

            for(map[k] = true; k > 0; k /= 2) {
                int i = 1;

                for(int start = 0; i < map.length; ++i) {
                    if (map[i]) {
                        if (!map[i - 1]) {
                            int mid = start + (i - start) / 2;
                            map[mid] = true;
                            dindex[pos++] = mid;
                        }

                        start = i;
                    }
                }
            }

            return dindex;
        }
    }

    private void applyPresentationModel(MediaElement media) {
        String sopUID = (String)TagD.getTagValue(media, 524312, String.class);
        SopInstance sop;
        if (this.seriesInstanceList.isContainsMultiframes()) {
            sop = this.seriesInstanceList.getSopInstance(sopUID, (Integer)TagD.getTagValue(media, 2097171, Integer.class));
        } else {
            sop = this.seriesInstanceList.getSopInstance(sopUID);
        }

        if (sop != null) {
            Object var5 = sop.getGraphicModel();
            if (var5 instanceof GraphicModel) {
                GraphicModel model = (GraphicModel)var5;
                int frames = media.getMediaReader().getMediaElementNumber();
                if (frames > 1 && media.getKey() instanceof Integer) {
                    String seriesUID = (String)TagD.getTagValue(media, 2097166, String.class);
                    Iterator var7 = model.getReferencedSeries().iterator();

                    while(true) {
                        List f;
                        label45:
                        do {
                            while(true) {
                                ReferencedSeries s;
                                do {
                                    if (!var7.hasNext()) {
                                        return;
                                    }

                                    s = (ReferencedSeries)var7.next();
                                } while(!s.getUuid().equals(seriesUID));

                                Iterator var9 = s.getImages().iterator();

                                while(var9.hasNext()) {
                                    ReferencedImage refImg = (ReferencedImage)var9.next();
                                    if (refImg.getUuid().equals(sopUID)) {
                                        f = refImg.getFrames();
                                        continue label45;
                                    }
                                }
                            }
                        } while(f != null && !f.contains(media.getKey()));

                        media.setTag(TagW.PresentationModel, model);
                    }
                } else {
                    media.setTag(TagW.PresentationModel, model);
                }
            }
        }

    }

    public synchronized DownloadPriority getPriority() {
        return this.priority;
    }

    public synchronized void setPriority(DownloadPriority priority) {
        this.priority = priority;
    }

    public void setPriority() {
        DownloadPriority p = this.getPriority();
        if (p != null && StateValue.PENDING.equals(this.getState())) {
            boolean change = DownloadManager.removeSeriesInQueue(this);
            if (change) {
                p.setPriority(DownloadPriority.COUNTER.getAndDecrement());
                DownloadManager.offerSeriesInQueue(this);
                synchronized(DownloadManager.getTasks()) {
                    Iterator var4 = DownloadManager.getTasks().iterator();

                    while(var4.hasNext()) {
                        LoadSeries s = (LoadSeries)var4.next();
                        if (s != this && StateValue.STARTED.equals(s.getState())) {
                            this.cancelAndReplace(s);
                            break;
                        }
                    }
                }
            }
        }

    }

    public LoadSeries cancelAndReplace(LoadSeries s) {
        LoadSeries taskResume = new LoadSeries(s.getDicomSeries(), this.dicomModel, s.authMethod, s.getProgressBar(), s.getConcurrentDownloads(), s.writeInCache, s.startDownloading);
        s.cancel();
        taskResume.setPriority(s.getPriority());
        taskResume.setPOpeningStrategy(s.getOpeningStrategy());
        Thumbnail thumbnail = (Thumbnail)s.getDicomSeries().getTagValue(TagW.Thumbnail);
        if (thumbnail != null) {
            removeThumbnailMouseAndKeyAdapter(thumbnail);
            addListenerToThumbnail(thumbnail, taskResume, this.dicomModel);
        }

        DownloadManager.addLoadSeries(taskResume, this.dicomModel, true);
        DownloadManager.removeLoadSeries(s, this.dicomModel);
        return taskResume;
    }

    public int getConcurrentDownloads() {
        return this.concurrentDownloads;
    }

    static {
        DOWNLOAD_START_TIME = new TagW("DownloadStartTime", TagType.TIME);
        DOWNLOAD_TIME = new TagW("DownloadTime", TagType.TIME);
        DOWNLOAD_ERRORS = new TagW("DownloadErrors", TagType.INTEGER);
    }

    class Download implements Callable<Boolean> {
        private final String url;
        private Status status;

        public Download(String url) {
            this.url = url;
            this.status = LoadSeries.Status.DOWNLOADING;
        }

        private void error() {
            this.status = LoadSeries.Status.ERROR;
            LoadSeries.this.dicomSeries.setTag(LoadSeries.DOWNLOAD_ERRORS, LoadSeries.this.errors.incrementAndGet());
        }

        private HttpResponse replaceToDefaultTSUID() throws IOException {
            StringBuilder buffer = new StringBuilder();
            int start = this.url.indexOf("&transferSyntax=");
            if (start != -1) {
                int end = this.url.indexOf(38, start + 16);
                buffer.append(this.url, 0, start + 16);
                buffer.append(TransferSyntax.EXPLICIT_VR_LE.getTransferSyntaxUID());
                if (end != -1) {
                    buffer.append(this.url.substring(end));
                }
            } else {
                buffer.append(this.url);
                buffer.append("&transferSyntax=");
                buffer.append(TransferSyntax.EXPLICIT_VR_LE.getTransferSyntaxUID());
            }

            return NetworkUtil.getHttpResponse(buffer.toString(), LoadSeries.this.urlParams, LoadSeries.this.authMethod);
        }

        public Boolean call() {
            try {
                this.process();
            } catch (StreamIOException var2) {
                StreamIOException es = var2;
                LoadSeries.this.hasError = true;
                this.error();
                PrLogger.error("Downloading", es);
            } catch (URISyntaxException | IOException var3) {
                Exception e = var3;
                this.error();
                PrLogger.error("Downloading", e);
            }

            return Boolean.TRUE;
        }

        private File getDicomTmpDir() {
            if (!LoadSeries.DICOM_TMP_DIR.exists()) {
                PrLogger.info("DICOM tmp dir not found. Re-creating it!");
                AppProperties.buildAccessibleTempDirectory(new String[]{"downloading"});
            }

            return LoadSeries.DICOM_TMP_DIR;
        }

        private boolean process() throws IOException, URISyntaxException {
            boolean cache = true;
            File tempFile = null;
            DicomMediaIO dicomReader = null;
            HttpResponse urlcon = NetworkUtil.getHttpResponse(this.url, LoadSeries.this.urlParams, LoadSeries.this.authMethod);
            int code = urlcon.getResponseCode();
            if (code >= 400) {
                if (LoadSeries.this.authMethod != null && code == 401) {
                    LoadSeries.this.authMethod.resetToken();
                    LoadSeries.this.authMethod.getToken();
                }

                throw new IllegalStateException("Response code of server: " + urlcon.getResponseCode());
            } else {
                InputStream stream = urlcon.getInputStream();

                label102: {
                    boolean var13;
                    try {
                        label110: {
                            if (!LoadSeries.this.writeInCache && this.url.startsWith("file:")) {
                                cache = false;
                            }

                            if (cache) {
                                tempFile = File.createTempFile("image_", ".dcm", this.getDicomTmpDir());
                            }

                            LoadSeries.this.progressBar.setIndeterminate(LoadSeries.this.progressBar.getMaximum() < 3);
                            if (LoadSeries.this.dicomSeries == null) {
                                break label102;
                            }

                            if (cache) {
                                int bytesTransferred = this.downloadInFileCache(urlcon, tempFile);
                                if (bytesTransferred != -1 && bytesTransferred >= 0) {
                                    var13 = false;
                                    break label110;
                                }

                                File renameFile = new File(DicomMediaIO.DICOM_EXPORT_DIR, tempFile.getName());
                                if (tempFile.renameTo(renameFile)) {
                                    tempFile = renameFile;
                                }
                            } else {
                                tempFile = new File(NetworkUtil.getURI(this.url));
                            }

                            FileUtil.safeClose(stream);
                            dicomReader = new DicomMediaIO(tempFile);
                            if (dicomReader.isReadableDicom() && LoadSeries.this.dicomSeries.size((Filter)null) == 0) {
                                MediaSeriesGroup patient = LoadSeries.this.dicomModel.getParent(LoadSeries.this.dicomSeries, DicomModel.patient);
                                dicomReader.writeMetaData(patient);
                                MediaSeriesGroup study = LoadSeries.this.dicomModel.getParent(LoadSeries.this.dicomSeries, DicomModel.study);
                                dicomReader.writeMetaData(study);
                                dicomReader.writeMetaData(LoadSeries.this.dicomSeries);
                                GuiExecutor.invokeAndWait(() -> {
                                    Thumbnail thumb = (Thumbnail)LoadSeries.this.dicomSeries.getTagValue(TagW.Thumbnail);
                                    if (thumb != null) {
                                        thumb.repaint();
                                    }

                                    LoadSeries.this.dicomModel.firePropertyChange(new ObservableEvent(BasicAction.UPDATE_PARENT, LoadSeries.this.dicomModel, (Object)null, LoadSeries.this.dicomSeries));
                                });
                            }
                            break label102;
                        }
                    } catch (Throwable var10) {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (Throwable var9) {
                                var10.addSuppressed(var9);
                            }
                        }

                        throw var10;
                    }

                    if (stream != null) {
                        stream.close();
                    }

                    return var13;
                }

                if (stream != null) {
                    stream.close();
                }

                if (this.status == LoadSeries.Status.DOWNLOADING) {
                    this.status = LoadSeries.Status.COMPLETE;
                    if (tempFile != null && LoadSeries.this.dicomSeries != null && dicomReader.isReadableDicom()) {
                        if (tempFile.getPath().startsWith(AppProperties.APP_TEMP_DIR.getPath())) {
                            dicomReader.getFileCache().setOriginalTempFile(tempFile);
                        }

                        DicomMediaIO reader = dicomReader;
                        GuiExecutor.execute(() -> {
                            this.updateUI(reader);
                        });
                    }
                }

                LoadSeries.this.incrementProgressBarValue();
                return true;
            }
        }

        private int downloadInFileCache(HttpResponse response, File tempFile) throws IOException {
            WadoParameters wadoParams = (WadoParameters)LoadSeries.this.dicomSeries.getTagValue(TagW.WadoParameters);
            int[] overrideList = (int[])Optional.ofNullable(wadoParams).map(ArcParameters::getOverrideDicomTagIDList).orElse(null);
            int bytesTransferred;
            if (overrideList == null) {
                if (wadoParams != null && wadoParams.isWadoRS()) {
                    int[] readBytes = new int[]{0};
                    Multipart.Handler handler = (multipartReader, partNumber, headers) -> {
                        InputStream in = multipartReader.newPartInputStream();

                        try {
                            readBytes[0] = FileUtil.writeStream(new SeriesProgressMonitor(LoadSeries.this.dicomSeries, in), tempFile, false);
                        } catch (Throwable var10) {
                            if (in != null) {
                                try {
                                    ((InputStream)in).close();
                                } catch (Throwable var9) {
                                    var10.addSuppressed(var9);
                                }
                            }

                            throw var10;
                        }

                        if (in != null) {
                            ((InputStream)in).close();
                        }

                    };
                    if (response instanceof ClosableURLConnection) {
                        ClosableURLConnection urlConnection = (ClosableURLConnection)response;
                        Multipart.parseMultipartRelated(urlConnection.getUrlConnection().getContentType(), response.getInputStream(), handler);
                    } else {
                        AuthResponse authResponse = (AuthResponse)response;
                        Multipart.parseMultipartRelated(authResponse.getResponse().getHeader("Content-Type"), response.getInputStream(), handler);
                    }

                    bytesTransferred = readBytes[0];
                } else {
                    bytesTransferred = FileUtil.writeStream(new DicomSeriesProgressMonitor(LoadSeries.this.dicomSeries, response.getInputStream(), false), tempFile);
                }
            } else {
                bytesTransferred = this.writFile(new DicomSeriesProgressMonitor(LoadSeries.this.dicomSeries, response.getInputStream(), false), tempFile, overrideList);
            }

            if (bytesTransferred == Integer.MIN_VALUE) {
                PrLogger.warn("Stop downloading unsupported TSUID, retry to download non compressed TSUID");
                InputStream stream2 = this.replaceToDefaultTSUID().getInputStream();

                try {
                    if (overrideList == null) {
                        bytesTransferred = FileUtil.writeStream(new DicomSeriesProgressMonitor(LoadSeries.this.dicomSeries, stream2, false), tempFile);
                    } else {
                        bytesTransferred = this.writFile(new DicomSeriesProgressMonitor(LoadSeries.this.dicomSeries, stream2, false), tempFile, overrideList);
                    }
                } catch (Throwable var11) {
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (Throwable var10) {
                            var11.addSuppressed(var10);
                        }
                    }

                    throw var11;
                }

                if (stream2 != null) {
                    stream2.close();
                }
            }

            return bytesTransferred;
        }

        public int writFile(InputStream in, File tempFile, int[] overrideList) throws StreamIOException {
            if (in != null && tempFile != null) {
                DicomInputStream dis = null;
                DicomOutputStream dos = null;
                boolean var26 = false;

                byte var41;
                label337: {
                    int var7;
                    List blkFilesxx;
                    Iterator var43;
                    File file;
                    label338: {
                        try {
                            var26 = true;
                            dis = new DicomInputStream(in);

                            String tsuid;
                            Attributes dataset;
                            try {
                                dis.setIncludeBulkData(IncludeBulkData.URI);
                                dataset = dis.readDataset();
                                tsuid = dis.getTransferSyntax();
                            } finally {
                                dis.close();
                            }

                            dos = new DicomOutputStream(tempFile);
                            if (overrideList != null) {
                                MediaSeriesGroup study = LoadSeries.this.dicomModel.getParent(LoadSeries.this.dicomSeries, DicomModel.study);
                                MediaSeriesGroup patient = LoadSeries.this.dicomModel.getParent(LoadSeries.this.dicomSeries, DicomModel.patient);
                                ElementDictionary dic = ElementDictionary.getStandardElementDictionary();
                                int[] var11 = overrideList;
                                int var12 = overrideList.length;

                                for(int var13 = 0; var13 < var12; ++var13) {
                                    int tag = var11[var13];
                                    TagW tagElement = patient.getTagElement(tag);
                                    Object value;
                                    if (tagElement == null) {
                                        tagElement = study.getTagElement(tag);
                                        value = study.getTagValue(tagElement);
                                    } else {
                                        value = patient.getTagValue(tagElement);
                                    }

                                    DicomMediaUtils.fillAttributes(dataset, tagElement, value, dic);
                                }
                            }

                            dos.writeDataset(dataset.createFileMetaInformation(tsuid), dataset);
                            dos.finish();
                            dos.flush();
                            var41 = -1;
                            var26 = false;
                            break label337;
                        } catch (InterruptedIOException var33) {
                            FileUtil.delete(tempFile);
                            PrLogger.error("Interruption when writing file: {}", var33.getMessage());
                            var7 = var33.bytesTransferred;
                            var26 = false;
                        } catch (IOException var34) {
                            FileUtil.delete(tempFile);
                            throw new StreamIOException(var34);
                        } catch (Exception var35) {
                            Exception e = var35;
                            FileUtil.delete(tempFile);
                            PrLogger.error("Writing DICOM temp file", e);
                            var7 = 0;
                            var26 = false;
                            break label338;
                        } finally {
                            if (var26) {
                                SafeClose.close(dos);
                                if (dis != null) {
                                    List<File> blkFilesx = dis.getBulkDataFiles();
                                    if (blkFilesx != null) {
                                        Iterator var19 = blkFilesx.iterator();

                                        while(var19.hasNext()) {
                                            File filexx = (File)var19.next();
                                            FileUtil.delete(filexx);
                                        }
                                    }
                                }

                            }
                        }

                        SafeClose.close(dos);
                        if (dis != null) {
                            blkFilesxx = dis.getBulkDataFiles();
                            if (blkFilesxx != null) {
                                var43 = blkFilesxx.iterator();

                                while(var43.hasNext()) {
                                    file = (File)var43.next();
                                    FileUtil.delete(file);
                                }
                            }
                        }

                        return var7;
                    }

                    SafeClose.close(dos);
                    if (dis != null) {
                        blkFilesxx = dis.getBulkDataFiles();
                        if (blkFilesxx != null) {
                            var43 = blkFilesxx.iterator();

                            while(var43.hasNext()) {
                                file = (File)var43.next();
                                FileUtil.delete(file);
                            }
                        }
                    }

                    return var7;
                }

                SafeClose.close(dos);
                if (dis != null) {
                    List<File> blkFiles = dis.getBulkDataFiles();
                    if (blkFiles != null) {
                        Iterator var46 = blkFiles.iterator();

                        while(var46.hasNext()) {
                            File filex = (File)var46.next();
                            FileUtil.delete(filex);
                        }
                    }
                }

                return var41;
            } else {
                return 0;
            }
        }

        private void updateUI(DicomMediaIO reader) {
            boolean firstImageToDisplay = false;
            MediaElement[] medias = reader.getMediaElement();
            MediaSeriesGroup patientx;
            if (medias != null) {
                firstImageToDisplay = LoadSeries.this.dicomSeries.size((Filter)null) == 0;
                if (firstImageToDisplay) {
                    MediaSeriesGroup patient = LoadSeries.this.dicomModel.getParent(LoadSeries.this.dicomSeries, DicomModel.patient);
                    String oldStudyUID;
                    if (patient != null) {
                        String oldDicomPtUID = (String)patient.getTagValue(TagW.PatientPseudoUID);
                        oldStudyUID = (String)reader.getTagValue(TagW.PatientPseudoUID);
                        if (!Objects.equals(oldDicomPtUID, oldStudyUID)) {
                            LoadSeries.this.dicomModel.mergePatientUID(oldDicomPtUID, oldStudyUID);
                        }
                    }

                    patientx = LoadSeries.this.dicomModel.getParent(LoadSeries.this.dicomSeries, DicomModel.study);
                    if (patientx != null) {
                        oldStudyUID = (String)patientx.getTagValue(TagD.get(2097165));
                        String studyUID = (String)TagD.getTagValue(reader, 2097165, String.class);
                        if (!Objects.equals(oldStudyUID, studyUID)) {
                            LoadSeries.this.dicomModel.mergeStudyUID(oldStudyUID, studyUID);
                        }
                    }
                }

                MediaElement[] var8 = medias;
                int var11 = medias.length;

                for(int var12 = 0; var12 < var11; ++var12) {
                    MediaElement media = var8[var12];
                    LoadSeries.this.applyPresentationModel(media);
                    LoadSeries.this.dicomModel.applySplittingRules(LoadSeries.this.dicomSeries, media);
                }
            }

            Thumbnail thumb = (Thumbnail)LoadSeries.this.dicomSeries.getTagValue(TagW.Thumbnail);
            if (thumb != null) {
                thumb.repaint();
            }

            patientx = LoadSeries.this.dicomModel.getParent(LoadSeries.this.dicomSeries, DicomModel.patient);
            if (patientx != null) {
                PluginOpeningStrategy open = LoadSeries.this.openingStrategy;
                if (open != null) {
                    open.openViewerPlugin(patientx, LoadSeries.this.dicomModel, LoadSeries.this.dicomSeries);
                }
            }

        }
    }

    public static enum Status {
        DOWNLOADING,
        COMPLETE,
        ERROR;

        private Status() {
        }
    }
}
