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

import org.melato.android.bookmark.BookmarksActivity;
import org.melato.android.location.Locations;
import org.melato.android.util.LabeledPoint;
import org.melato.bus.android.Info;
import org.melato.bus.android.R;
import org.melato.bus.android.bookmark.BookmarkTypes;
import org.melato.bus.model.RStop;
import org.melato.bus.plan.NamedPoint;
import org.melato.bus.plan.Sequence;
import org.melato.client.Bookmark;
import org.melato.gps.Point2D;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * This is an activity dialog that asks the user what to do with an incoming selected point.
 * It may do one of:
 *  - show nearby routes/stops
 *  - use as plan origin
 *  - use as plan destination
 *  - add to Sequence (requires RStop)
 **/
public class PointSelectionActivity extends FragmentActivity implements OnClickListener {
  public static final String POINT = "POINT";
  RStop rstop;
  NamedPoint point;
  /** Called when the activity is first created. */
  
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.point_selection);
    Intent intent = getIntent();
    IntentHelper intentHelper = new IntentHelper(intent);
    setTitle(R.string.point_selection);
    rstop = intentHelper.getRStop();
    if ( rstop == null) {
      point = (NamedPoint) intent.getSerializableExtra(POINT);
    }
    
    if ( rstop == null && point == null) {
      Point2D p = IntentHelper.getLocation(intent);
      if (p != null) {
        point = new NamedPoint(p);
      } else {
        LabeledPoint labeledPoint = Locations.getGeoUri(intent);
        if ( labeledPoint != null) {
          point = new NamedPoint(labeledPoint.getPoint(), labeledPoint.getLabel());
        }
      }
    }
    if ( point != null && point.getName() != null) {
      setTitle(point.getName());
    }
    initButton(R.id.nearby);
    //initButton(R.id.add_stop_after);
    initButton(R.id.origin);
    initButton(R.id.destination);
    initButton(R.id.bookmark);
  }
  
  private void initButton(int id) {
    Button button = (Button) findViewById(id);
    button.setOnClickListener(this);       
  }
  private void showNearby() {    
    finish();
    NearbyActivity.start(this, getPoint());
  }  
  private void showPlan() {
    finish();
    PlanTabsActivity.showSearch(this);
  }
 
  private void addToSequence(boolean after) {
    Sequence sequence = Info.getSequence(this);
    if ( after ) {
      sequence.addStopAfter(Info.routeManager(this), rstop);
    } else {
      sequence.addStopBefore(Info.routeManager(this), rstop);
    }
    finish();
    startActivity(new Intent(this, SequenceActivity.class));    
  }
  
  public static void selectPoint(Context context, RStop rstop) {
    Intent intent = new Intent(context, PointSelectionActivity.class);
    new IntentHelper(intent).putRStop(rstop);
    context.startActivity(intent);    
  }
  
  public static void selectPoint(Context context, Point2D point) {
    Intent intent = new Intent(context, PointSelectionActivity.class);
    IntentHelper.putLocation(intent, point);
    context.startActivity(intent);    
  }
  
  NamedPoint getNamedPoint() {
    if ( rstop != null) {
      return Info.namedPoint(this,  rstop);
    } else {
      return point;
    }
  }
  
  Point2D getPoint() {
    if ( rstop != null) {
      return rstop.getStop();
    } else {
      return point;
    }
  }
 
  public void addBookmark() {
    NamedPoint p = getNamedPoint();
    if ( p != null) {
      String name = p.getName(); 
      Bookmark bookmark = new Bookmark(BookmarkTypes.LOCATION, name, new Point2D(p));
      BookmarksActivity.addBookmarkDialog(this, bookmark);
    }
  }
    
    
  private boolean onItemSelected(int itemId) {
    NamedPoint p = null;
    switch(itemId) {
      case R.id.nearby:
        showNearby();
        break;
      case R.id.add:
        if ( rstop != null) {
          addToSequence(true);
        }
        break;
      case R.id.origin:
        p = getNamedPoint();
        if ( p != null) {
          PlanFragment.origin = p;
          showPlan();          
        }
        break;
      case R.id.destination:
        p = getNamedPoint();
        if ( p != null) {
          PlanFragment.destination = p;
          showPlan();          
        }
        break;
      case R.id.bookmark:
        addBookmark();
        break;
      default:
        return false;
    }
    return true;
  }
  @Override
  public void onClick(View v) {
    onItemSelected(v.getId());
  }

  
}