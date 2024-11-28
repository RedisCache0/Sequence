package org.weasis.dicom.explorer.pref.download;

import java.util.Hashtable;
import org.osgi.service.component.annotations.Component;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.PreferencesPageFactory;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;

@Component(
   service = {PreferencesPageFactory.class}
)
public class DicomExplorerPrefFactory implements PreferencesPageFactory {
   public AbstractItemDialogPage createInstance(Hashtable properties) {
      return new DicomExplorerPrefView();
   }

   public boolean isComponentCreatedByThisFactory(Insertable component) {
      return component instanceof DicomExplorerPrefView;
   }
}
