package com.example.mapme.widgets;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

/**
 * Class CustomOverlay.
 */
public class CustomKmlFolder extends KmlFolder {

    /**
     * Build a FolderOverlay, containing (recursively) overlays from all items of this Folder.
     *
     * @param map
     * @param defaultStyle to apply when an item has no Style defined.
     * @param styler       to apply
     * @param kmlDocument  for Styles
     * @return the FolderOverlay built
     */
    public CustomOverlay buildOverlay(MapView map, Style defaultStyle, KmlFeature.Styler styler, KmlDocument kmlDocument, String id) {
        CustomOverlay folderOverlay = new CustomOverlay(id);
        folderOverlay.setName(mName);
        folderOverlay.setDescription(mDescription);
        for (KmlFeature k : mItems) {
            Overlay overlay = k.buildOverlay(map, defaultStyle, styler, kmlDocument);
            if (overlay != null)
                folderOverlay.add(overlay);
        }
        if (styler == null)
            folderOverlay.setEnabled(mVisibility);
        else
            styler.onFeature(folderOverlay, this);
        return folderOverlay;
    }

}