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
package org.melato.bus.android.activity;

import org.melato.android.app.HelpActivity;
import org.melato.android.menu.Menus;
import org.melato.bus.android.R;
import org.melato.bus.model.RStop;
import org.melato.bus.model.Route;
import org.melato.bus.model.Stop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Displays the list of stops of a route
 * @author Alex Athanasopoulos
 */
public class StopsActivity extends FragmentActivity implements OnItemClickListener, OnClickListener {
  private BusActivities activities;
  private StopsContext stops;

  public StopsActivity() {
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_activity);
    ListView listView = (ListView) findViewById(R.id.listView);
    listView.setOnItemClickListener(this);
    registerForContextMenu(listView);
    activities = new BusActivities(this);
    stops = new StopsContext(this, listView);
    Route route = activities.getRoute();
    setTitle(route.getFullTitle());
    stops.setRoute(route);
    IntentHelper helper = new IntentHelper(this);
    RStop rstop = helper.getRStop();
    stops.setStop(rstop);
    Menus.addIcons(this, (LinearLayout) findViewById(R.id.icons), R.menu.stops_menu, this);
  }
  
  @Override
  protected void onDestroy() {
    stops.close();
    super.onDestroy();
  }

  RStop getRStop(int position) {
    Stop p = stops.getStops()[position];
    return new RStop(activities.getRouteId(), p);
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if ( resultCode == RESULT_OK) {
      stops.refresh();
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position,
      long id) {
    RStop rstop = getRStop(position);
    new StopActions(this).showSchedule(rstop);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    Menus.inflate(inflater,R.menu.stops_menu, menu);
    HelpActivity.addItem(menu, this, Help.STOPS);
    return true;
  }
  
  
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.stop_context_menu, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return activities.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View v) {
    activities.onItemSelected(v.getId());
  }
  
  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    RStop rstop = getRStop(info.position);
    return new StopActions(this).showRStop(rstop, item.getItemId());
  }
  
  
}