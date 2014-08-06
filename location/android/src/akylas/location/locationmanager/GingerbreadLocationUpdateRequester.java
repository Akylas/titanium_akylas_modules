/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package akylas.location.locationmanager;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;

/**
 * Provides support for initiating active and passive location updates 
 * optimized for the Gingerbread release. Includes use of the Passive Location Provider.
 * 
 * Uses broadcast Intents to notify the app of location changes.
 */
public class GingerbreadLocationUpdateRequester extends FroyoLocationUpdateRequester{

  public GingerbreadLocationUpdateRequester(LocationManager locationManager) {
    super(locationManager);
  }

  @Override
  public void requestLocationUpdates(final String bestProvider, long minTime, float minDistance, Criteria criteria, PendingIntent pendingIntent) {
    // Gingerbread supports a location update request that accepts criteria directly.
    // Note that we aren't monitoring this provider to check if it becomes disabled - this is handled by the calling Activity.
    locationManager.requestLocationUpdates(bestProvider, minTime, minDistance, pendingIntent);
  }

  @Override
  public void requestPassiveLocationUpdates(long minTime, float minDistance, PendingIntent pendingIntent) {
      locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, minDistance, pendingIntent);  
  }

}
