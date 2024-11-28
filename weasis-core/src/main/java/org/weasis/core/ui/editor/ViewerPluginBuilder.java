package org.weasis.core.ui.editor;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.AbstractFileModel;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.MimeInspector;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.FileCache;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.serialize.XmlSerializer;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.WProperties;

public class ViewerPluginBuilder {
   private static final Logger LOGGER = LoggerFactory.getLogger(ViewerPluginBuilder.class);
   public static final String CMP_ENTRY_BUILD_NEW_VIEWER = "cmp.entry.viewer";
   public static final String BEST_DEF_LAYOUT = "best.def.layout";
   public static final String LAYOUT_ROW = "layout.row";
   public static final String LAYOUT_COL = "layout.col";
   public static final String OPEN_IN_SELECTION = "add.in.selected.view";
   public static final String ADD_IN_SELECTED_VIEW = "add.in.selected.view";
   public static final String SCREEN_BOUND = "screen.bound";
   public static final String ICON = "plugin.icon";
   public static final String UID = "plugin.uid";
   public static final FileModel DefaultDataModel = new FileModel();
   private final SeriesViewerFactory factory;
   private final List series;
   private final DataExplorerModel model;
   private final Map properties;

   public ViewerPluginBuilder(SeriesViewerFactory factory, List series, DataExplorerModel model, Map props) {
      if (factory != null && series != null && model != null) {
         this.factory = factory;
         this.series = series;
         this.model = model;
         this.properties = props == null ? Collections.synchronizedMap(new HashMap()) : props;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public SeriesViewerFactory getFactory() {
      return this.factory;
   }

   public List getSeries() {
      return this.series;
   }

   public DataExplorerModel getModel() {
      return this.model;
   }

   public Map getProperties() {
      return this.properties;
   }

   public static void openSequenceInPlugin(SeriesViewerFactory factory, List series, DataExplorerModel model, boolean compareEntryToBuildNewViewer, boolean removeOldSeries, int row, int col) {
      if (factory != null && series != null && model != null) {
         Map<String, Object> props = Collections.synchronizedMap(new HashMap());
         props.put("cmp.entry.viewer", compareEntryToBuildNewViewer);
         props.put("best.def.layout", removeOldSeries);
         props.put("screen.bound", (Object)null);
         props.put("layout.row", row);
         props.put("layout.col", col);
         ViewerPluginBuilder builder = new ViewerPluginBuilder(factory, series, model, props);
         model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REGISTER, model, (Object)null, builder));
      }
   }

   public static void openSequenceInPlugin(SeriesViewerFactory factory, MediaSeries series, DataExplorerModel model, boolean compareEntryToBuildNewViewer, boolean removeOldSeries) {
      if (factory != null && series != null && model != null) {
         ArrayList<MediaSeries<? extends MediaElement>> list = new ArrayList(1);
         list.add(series);
         openSequenceInPlugin(factory, (List)list, model, compareEntryToBuildNewViewer, removeOldSeries);
      }
   }

   public static void openSequenceInPlugin(SeriesViewerFactory factory, List series, DataExplorerModel model, boolean compareEntryToBuildNewViewer, boolean removeOldSeries) {
      openSequenceInPlugin(factory, series, model, compareEntryToBuildNewViewer, removeOldSeries, (Rectangle)null);
   }

   public static void openSequenceInPlugin(SeriesViewerFactory factory, MediaSeries series, DataExplorerModel model, boolean compareEntryToBuildNewViewer, boolean removeOldSeries, boolean openIn) {
      if (factory != null && series != null && model != null) {
         ArrayList<MediaSeries<? extends MediaElement>> list = new ArrayList(1);
         list.add(series);
         openSequenceInPlugin(factory, (List)list, model, compareEntryToBuildNewViewer, removeOldSeries, openIn);
      }
   }

   public static void openSequenceInPlugin(SeriesViewerFactory factory, List series, DataExplorerModel model, boolean compareEntryToBuildNewViewer, boolean removeOldSeries, boolean openIn) {
      openSequenceInPlugin(factory, series, model, compareEntryToBuildNewViewer, removeOldSeries, (Rectangle)null, openIn);
   }

   public static void openSequenceInPlugin(SeriesViewerFactory factory, List series, DataExplorerModel model, boolean compareEntryToBuildNewViewer, boolean removeOldSeries, Rectangle screenBound) {
      if (factory != null && series != null && model != null) {
         Map<String, Object> props = Collections.synchronizedMap(new HashMap());
         props.put("cmp.entry.viewer", compareEntryToBuildNewViewer);
         props.put("best.def.layout", removeOldSeries);
         props.put("screen.bound", screenBound);
         ViewerPluginBuilder builder = new ViewerPluginBuilder(factory, series, model, props);
         model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REGISTER, model, (Object)null, builder));
      }
   }

   public static void openSequenceInPlugin(SeriesViewerFactory factory, List series, DataExplorerModel model, boolean compareEntryToBuildNewViewer, boolean removeOldSeries, Rectangle screenBound, boolean openIn) {
      if (factory != null && series != null && model != null) {
         Map<String, Object> props = Collections.synchronizedMap(new HashMap());
         props.put("cmp.entry.viewer", compareEntryToBuildNewViewer);
         props.put("best.def.layout", removeOldSeries);
         props.put("screen.bound", screenBound);
         ViewerPluginBuilder builder = new ViewerPluginBuilder(factory, series, model, props);
         if(openIn){
            model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REGISTER, model, (Object)null, builder));
         }
         else{
            WProperties localPersistence = GuiUtils.getUICore().getLocalPersistence();
            Integer openin = localPersistence.getIntProperty("OPENIN", 0);
            if (openin == 1){
               localPersistence.putIntProperty("OPENIN", openin+1);
               model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REGISTER, model, (Object)null, builder));
            }
         }
      }
   }

   public static void openSequenceInPlugin(ViewerPluginBuilder builder) {
      if (builder != null) {
         DataExplorerModel model = builder.getModel();
         model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REGISTER, model, (Object)null, builder));
      }
   }

public static void openSequenceInDefaultPlugin(
      List<? extends MediaSeries<? extends MediaElement>> series,
      DataExplorerModel model,
      boolean compareEntryToBuildNewViewer,
      boolean removeOldSeries) {
    ArrayList<String> mimes = new ArrayList<>();
    for (MediaSeries<?> s : series) {
      String mime = s.getMimeType();
      if (mime != null && !mimes.contains(mime)) {
        mimes.add(mime);
      }
    }
    for (String mime : mimes) {
      SeriesViewerFactory plugin = GuiUtils.getUICore().getViewerFactory(mime);
      if (plugin != null) {
        ArrayList<MediaSeries<? extends MediaElement>> seriesList = new ArrayList<>();
        for (MediaSeries<? extends MediaElement> s : series) {
          if (mime.equals(s.getMimeType())) {
            seriesList.add(s);
          }
        }
        openSequenceInPlugin(
            plugin, seriesList, model, compareEntryToBuildNewViewer, removeOldSeries);
      }
    }
  }

   public static void openSequenceInDefaultPlugin(MediaSeries series, DataExplorerModel model, boolean compareEntryToBuildNewViewer, boolean removeOldSeries) {
      if (series != null) {
         String mime = series.getMimeType();
         SeriesViewerFactory plugin = GuiUtils.getUICore().getViewerFactory(mime);
         openSequenceInPlugin(plugin, (MediaSeries)series, (DataExplorerModel)(model == null ? DefaultDataModel : model), compareEntryToBuildNewViewer, removeOldSeries);
      }

   }

   public static void openSequenceInDefaultPlugin(MediaElement media, DataExplorerModel model, boolean compareEntryToBuildNewViewer, boolean bestDefaultLayout) {
      if (media != null) {
         openSequenceInDefaultPlugin(media.getMediaReader().getMediaSeries(), model, compareEntryToBuildNewViewer, bestDefaultLayout);
      }

   }

   public static void openSequenceInDefaultPlugin(File file, boolean compareEntryToBuildNewViewer, boolean bestDefaultLayout) {
      MediaReader<MediaElement> reader = getMedia(file);
      if (reader != null) {
         MediaSeries<MediaElement> s = buildMediaSeriesWithDefaultModel(reader);
         openSequenceInDefaultPlugin((MediaSeries)s, DefaultDataModel, compareEntryToBuildNewViewer, bestDefaultLayout);
      }

   }

   public static MediaReader getMedia(File file) {
      return getMedia(file, true);
   }

   public static MediaReader getMedia(File file, boolean systemReader) {
      if (file != null && file.canRead()) {
         boolean cache = file.getPath().startsWith(AppProperties.FILE_CACHE_DIR.getPath());
         String mimeType = MimeInspector.getMimeType(file);
         if (mimeType != null) {
            Codec<?> codec = BundleTools.getCodec(mimeType, "dcm4che");
            if (codec != null) {
               MediaReader mreader = codec.getMediaIO(file.toURI(), mimeType, (Hashtable)null);
               if (cache) {
                  mreader.getFileCache().setOriginalTempFile(file);
               }

               return mreader;
            }
         }

         if (systemReader) {
            MediaReader<MediaElement> mreader = new DefaultMimeIO(file.toURI(), (String)null);
            if (cache) {
               mreader.getFileCache().setOriginalTempFile(file);
            }

            return mreader;
         }
      }

      return null;
   }

   public static MediaSeries buildMediaSeriesWithDefaultModel(MediaReader reader) {
      return buildMediaSeriesWithDefaultModel(reader, (String)null, (TagW)null, (String)null);
   }

   public static MediaSeries buildMediaSeriesWithDefaultModel(MediaReader reader, String groupUID, TagW groupName, String groupValue) {
      return buildMediaSeriesWithDefaultModel(reader, groupUID, groupName, groupValue, (String)null);
   }

   public static MediaSeries buildMediaSeriesWithDefaultModel(MediaReader reader, String groupUID, TagW groupName, String groupValue, String seriesUID) {
      if (reader instanceof DefaultMimeIO) {
         return reader.getMediaSeries();
      } else {
         MediaSeries<MediaElement> series = null;
         MediaElement[] medias = reader.getMediaElement();
         if (medias == null) {
            return null;
         } else {
            String sUID = seriesUID == null ? UUID.randomUUID().toString() : seriesUID;
            String gUID = groupUID == null ? UUID.randomUUID().toString() : groupUID;
            MediaSeriesGroup group1 = DefaultDataModel.getHierarchyNode(MediaSeriesGroupNode.rootNode, gUID);
            if (group1 == null) {
               group1 = new MediaSeriesGroupNode(TagW.Group, gUID, AbstractFileModel.group.tagView());
               group1.setTagNoNull(groupName, groupValue);
               DefaultDataModel.addHierarchyNode(MediaSeriesGroupNode.rootNode, group1);
            }

            MediaSeriesGroup group2 = DefaultDataModel.getHierarchyNode(group1, sUID);
            if (group2 instanceof Series) {
               series = (Series)group2;
            }

            try {
               if (series == null) {
                  series = reader.getMediaSeries();
                  series.setTag(TagW.ExplorerModel, DefaultDataModel);
                  DefaultDataModel.addHierarchyNode(group1, series);
               } else {
                  TagW sopTag = TagW.get("SOPInstanceUID");
                  if (((Series)series).hasMediaContains(sopTag, reader.getTagValue(sopTag))) {
                     return series;
                  }

                  for(MediaElement media : medias) {
                     series.addMedia(media);
                  }
               }

               for(MediaElement media : medias) {
                  openAssociatedGraphics(media);
               }
            } catch (Exception e) {
               LOGGER.error("Build series error", e);
            }

            return series;
         }
      }
   }

   public static void openAssociatedGraphics(MediaElement media) {
      if (media instanceof ImageElement) {
         FileCache fc = media.getFileCache();
         Optional<File> fo = fc.getOriginalFile();
         if (fc.isLocalFile() && fo.isPresent()) {
            File gpxFile = new File(((File)fo.get()).getPath() + ".xml");
            GraphicModel graphicModel = XmlSerializer.readPresentationModel(gpxFile);
            if (graphicModel != null) {
               media.setTag(TagW.PresentationModel, graphicModel);
            }
         }
      }

   }
}
