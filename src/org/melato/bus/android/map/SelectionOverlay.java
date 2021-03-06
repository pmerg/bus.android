/*-------------------------------------------------------------------------
 * Copyright (c) 2012,2013,2014 Alex Athanasopoulos.  All Rights Reserved.
 * alex@melato.org
 *-------------------------------------------------------------------------
 * This file is part of Athens Next Bus
 *
 * Athens Next Bus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Athens Next Bus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Athens Next Bus.  If not, see <http://www.gnu.org/licenses/>.
 *-------------------------------------------------------------------------
 */
package org.melato.bus.android.map;

import android.app.Activity;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * A map overlay for selecting destination, etc..
 * @author Alex Athanasopoulos
 */
public class SelectionOverlay extends Overlay {
  Activity activity;
  GeoPoint selectedPoint;
  
  
  public GeoPoint getSelectedPoint() {
    return selectedPoint;
  }
  public SelectionOverlay(Activity activity) {
    super();
    this.activity = activity;
  }

  @Override
  public boolean onTap(GeoPoint geoPoint, MapView mapView) {
    selectedPoint = geoPoint;
    activity.openContextMenu(mapView);
    return true;
  }
  
}
