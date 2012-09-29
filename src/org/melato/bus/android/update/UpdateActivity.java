package org.melato.bus.android.update;

import java.util.List;

import org.melato.android.progress.ActivityProgressHandler;
import org.melato.android.progress.ProgressTitleHandler;
import org.melato.bus.android.R;
import org.melato.bus.android.activity.BusActivities;
import org.melato.bus.android.activity.RoutesActivity;
import org.melato.bus.model.Route;
import org.melato.log.Log;
import org.melato.log.PLog;
import org.melato.progress.CanceledException;
import org.melato.progress.ProgressGenerator;
import org.melato.update.UpdateFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UpdateActivity extends Activity implements Runnable {
  private UpdateManager updateManager;
  private ActivityProgressHandler progress;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.info(getClass().getName() + ".onCreate");
    super.onCreate(savedInstanceState);
    progress = new ProgressTitleHandler(this);
    updateManager = new UpdateManager(this);
    setContentView(R.layout.update);
    if ( updateManager.isRequired() ) {
      setTitle(R.string.update_required);
    }
    StringBuilder buf = new StringBuilder();
    Log.info( "getAvailable" );
    for(UpdateFile f: updateManager.getAvailableUpdates()) {
      if ( f.getNote() != null ) {
        if ( buf.length() > 0 ) {
          buf.append( "\n" );
        }
        buf.append(f.getNote());
      }
    }
    TextView noteView = (TextView) findViewById(R.id.note);
    noteView.setText(buf.toString());
  }

  /** Called from the update button */
  public void update(View view) {
    Button button = (Button) findViewById(R.id.update);
    button.setEnabled(false);
    new Thread(this).start();
  }
  
  /** Called from the cancel button */
  public void cancel(View view) {
    PLog.info( "cancel()" );
    Button button = (Button) findViewById(R.id.cancel);
    button.setEnabled(false);
    progress.cancel();    
    finish();
  }

  void startMain() {
    BusActivities activities = new BusActivities(this);
    List<Route> recent = activities.getRecentRoutes();
    if ( recent.size() > 0 ) {
      RoutesActivity.showRecent(this);
    } else {
      RoutesActivity.showAll(this);
    }
  }
  
  void startUpdate() {
    
  }
  @Override
  public void run() {
    if ( progress != null ) {
      ProgressGenerator.setHandler(progress);
    }
    try {
      updateManager.update(updateManager.getAvailableUpdates());
      runOnUiThread(new Runnable() {

        @Override
        public void run() {
          startMain();
        }
        
      });
      
    } catch( CanceledException e ) {      
    }
    finish();
  }
  
  
  @Override
  protected void onDestroy() {
    progress.cancel();
    super.onDestroy();
  }
  
  public static boolean isConnected(Context context) {
    ConnectivityManager network = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = network.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.getState() == State.CONNECTED;      
  }
  
  /**
   * Checks for available updates.  It does so in a background thread, if there is need to download anything.
   * Otherwise it checks in the ui thread.
   * @author Alex Athanasopoulos
   */
  static class UpdatesChecker implements Runnable {
    UpdateManager updateManager;
    Activity activity;
    
    public UpdatesChecker(Activity activity) {
      this.activity = activity;
      updateManager = new UpdateManager(activity);
    }
    
    /** Check for updates, if necessary.
     * Return true if the calling activity can continue.
     * Return false if the calling activity should exit and let the update activity take over.
     * @return
     */
    public boolean checkUpdates() {
      if ( updateManager.isRequired() ) {
        // assume that we have not data.  We have to update or die.
        activity.startActivity(new Intent(activity, UpdateActivity.class));
        return false;
      } else {
        if ( isConnected(activity) ) {
          if ( updateManager.needsRefresh() ) {
            // refresh in the background
            new Thread(this).start();
          }
          else {
            // check here
            run();
          }
        }
      }
      return true;
    }
    
    public void run() {
      if ( ! updateManager.getAvailableUpdates().isEmpty() ) {
        activity.startActivity(new Intent(activity, UpdateActivity.class));
      }
    }
  }
  
  /** Check for updates, and if there are any available give the option of downloading them.
   * return true if the application should proceed normally.
   * false if it should do nothing and let the UpdateActivity take over.
   **/
  public static boolean checkUpdates(Activity activity) {
    UpdatesChecker checker = new UpdatesChecker(activity);
    return checker.checkUpdates();
  }


}
