/*-------------------------------------------------------------------------
 * Copyright (c) 2012,2013 Alex Athanasopoulos.  All Rights Reserved.
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
package org.melato.bus.android.activity;

import org.melato.android.app.HelpActivity;
import org.melato.android.bookmark.BookmarksActivity;
import org.melato.android.menu.MenuCapture;
import org.melato.android.ui.PropertiesDisplay;
import org.melato.android.util.Invokable;
import org.melato.android.util.LocationField;
import org.melato.bus.android.Info;
import org.melato.bus.android.R;
import org.melato.bus.android.bookmark.BookmarkTypes;
import org.melato.bus.model.RStop;
import org.melato.bus.model.Route;
import org.melato.bus.model.RouteId;
import org.melato.bus.model.Stop;
import org.melato.bus.plan.Sequence;
import org.melato.client.Bookmark;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Displays information about one stop.
 * @author Alex Athanasopoulos
 *
 */
public class StopActivity extends FragmentActivity implements OnItemClickListener, OnClickListener
 {
  private ListView listView; 
  private StopContext stop;
  private PropertiesDisplay properties;
  private BusActivities activities;
  private RStop rstop;
  
  public StopActivity() {
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_activity);
    stop = new StopContext(this);
    properties = stop.getProperties();
    activities = new BusActivities(this);
    IntentHelper intentHelper = new IntentHelper(this);
    rstop = intentHelper.getRStop();
    if ( rstop == null || rstop.getStop() == null) {
      return;
    }
    Route route = activities.getRoute();
    stop.setRoute(route);
    Stop[] waypoints = Info.routeManager(this).getStops(rstop.getRouteId());
    
    int index = rstop.getStopIndex();
    if ( index < 0 ) {
      index = findWaypointIndex(waypoints, rstop.getStop());
    }
    stop.setMarkerIndex(index);
    stop.addProperties();
    setTitle(route.getLabel() + " " + stop.getMarker().getName());
    listView = (ListView) findViewById(R.id.listView);
    listView.setAdapter(stop.createAdapter(R.layout.stop_item));
    listView.setOnItemClickListener(this);
    MenuCapture.addIcons(this, (LinearLayout) findViewById(R.id.icons), R.menu.stop_menu, this);    
  }
  
  @Override
  protected void onDestroy() {
    stop.close();
    super.onDestroy();
  }  
  
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if ( resultCode == RESULT_OK) {
      stop.refresh();
      setResult(RESULT_OK);
    }
  }

  static int findWaypointIndex(Stop[] waypoints, Stop p) {
    int size = waypoints.length;
    for( int i = 0; i < size; i++ ) {
      if ( p.equals(waypoints[i])) {
        return i;
      }
    }
    return -1;
  }

  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.stop_menu, menu);
    HelpActivity.addItem(menu,this, Help.STOP);
    return true;
  }
 
  /**
   * Start the Schedule activity for the given stop.
   */
  private void showStopSchedule() {
    activities.showRoute(rstop, ScheduleActivity.class);
  }
  private void addToSequence(boolean after) {
    Sequence sequence = Info.getSequence(this);
    RouteId routeId = rstop.getRouteId();
    Stop stop = this.stop.getMarker();
    if ( after ) {
      sequence.addStopAfter(Info.routeManager(this), new RStop(routeId, stop));
    } else {
      sequence.addStopBefore(Info.routeManager(this), new RStop(routeId, stop));
    }
    startActivity(new Intent(this, SequenceActivity.class));    
  }
  
  public static void addBookmark(FragmentActivity activity, RStop rstop) {
    String label = Info.routeManager(activity).getRoute(rstop.getRouteId()).getLabel();
    String name = label + " " + rstop.getStop().getName(); 
    Bookmark bookmark = new Bookmark(BookmarkTypes.STOP, name, rstop);
    BookmarksActivity.addBookmarkDialog(activity, bookmark);
  }
  
  void shareLocation() {
    Stop stop = rstop.getStop();
    new LocationField(stop.getName(), stop).invoke(this);
  }
  
  public boolean onItemSelected(int itemId) {
    boolean handled = false;
    switch (itemId) {
      case R.id.search:
        PointSelectionActivity.selectPoint(this,rstop);
        handled = true;
        break;
      case R.id.schedule:
        showStopSchedule();
        handled = true;
        break;
      case R.id.add:
        addToSequence(true);
        handled = true;
        break;
      case R.id.share:
        shareLocation();
        handled = true;
        break;
      case R.id.bookmark:
        addBookmark(this, rstop);
        handled = true;
        break;
      default:
        break;
    }
    if ( handled )
      return true;
    return activities.onItemSelected(itemId);
  }
  
  
  @Override
  public void onClick(View v) {
    onItemSelected(v.getId());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return onItemSelected(item.getItemId());    
  }
  
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position,
      long id) {
    Object value = properties.getItem(position);
    if ( value instanceof Invokable) {
      ((Invokable)value).invoke(this);
    }
  }
}