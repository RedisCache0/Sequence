package org.advanced.plugin.advanced;

import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.WProperties;

public class AdvancedPreference {
   public static final String MODALITY_KEY = "MODALITY_LAYOUT_";
   public static final String DEFAULT_MODALITY_KEY = "MODALITY_LAYOUT_(Default)";
   private static final int ROW_INDEX = 0;
   private static final int COL_INDEX = 1;
   private static WProperties localPersistence = GuiUtils.getUICore().getLocalPersistence();

   public static void setSRFilter(boolean enable) {
      localPersistence.putBooleanProperty("SRFilter", enable);
   }

   public static void setFont(int fontSize) {
      localPersistence.putIntProperty("FONTSIZE", fontSize);
   }

   public static int getFont() {
      return localPersistence.getIntProperty("FONTSIZE", 17);
   }

   public static boolean getSRFilter() {
      return localPersistence.getBooleanProperty("SRFilter", true);
   }

   public static void setAutoClose(boolean enable) {
      localPersistence.putBooleanProperty("AutoClose", enable);
   }

   public static boolean getAutoClose() {
      return localPersistence.getBooleanProperty("AutoClose", false);
   }

   public static void setModalityLayout(String modality, int row, int col) {
      byte[] rc = new byte[]{(byte)row, (byte)col};
      localPersistence.putByteArrayProperty("MODALITY_LAYOUT_" + modality, rc);
   }

   public static int getModalityLayoutRow(String modality) {
      return getModalityLayout(modality)[0];
   }

   public static int getModalityLayoutCol(String modality) {
      return getModalityLayout(modality)[1];
   }

   public static byte[] getModalityLayout(String modality) {
      byte[] r = localPersistence.getByteArrayProperty("MODALITY_LAYOUT_(Default)", new byte[]{1, 2});
      return localPersistence.getByteArrayProperty("MODALITY_LAYOUT_" + modality, r);
   }

   public static void setVersion(String domainName, int v) {
      localPersistence.putIntProperty(domainName + "_version", v);
   }

   public static int getVersion(String domainName) {
      return localPersistence.getIntProperty(domainName + "_version", 1);
   }
}
