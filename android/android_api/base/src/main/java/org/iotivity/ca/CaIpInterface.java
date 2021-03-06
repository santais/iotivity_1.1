/******************************************************************
 *
 * Copyright 2014 Samsung Electronics All Rights Reserved.
 *
 *
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
 *
 ******************************************************************/

package org.iotivity.ca;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class CaIpInterface {
    private static Context mContext;

    public enum WifiAPState{
        WIFI_AP_STATE_DISABLING (10),
        WIFI_AP_STATE_DISABLED (11),
        WIFI_AP_STATE_ENABLING (12),
        WIFI_AP_STATE_ENABLED (13),
        WIFI_AP_STATE_FAILED (14)
        ; // semicolon needed when fields / methods follow


        private final int apstate;

        WifiAPState(int apstate)
        {
            this.apstate = apstate;
        }
        public int getIntValue() {
           return this.apstate;
        }
    }

    private CaIpInterface(Context context) {
        mContext = context;
        registerIpStateReceiver();
    }

    private void registerIpStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");

        mContext.registerReceiver(mReceiver, intentFilter);
    }

    public static void destroyIpInterface() {
        mContext.unregisterReceiver(mReceiver);
    }

    private static BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_DISABLED) {
                caIpStateDisabled();
            } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager manager = (ConnectivityManager)
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo nwInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if(nwInfo.isConnected()) {
                    caIpStateEnabled();
                }
            }

            if (intent.getAction().equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                if (intent.getIntExtra("wifi_state",
                    WifiAPState.WIFI_AP_STATE_DISABLED.getIntValue())
                    == WifiAPState.WIFI_AP_STATE_DISABLED.getIntValue())
                {
                    caIpStateDisabled();
                }else if(intent.getIntExtra("wifi_state",
                    WifiAPState.WIFI_AP_STATE_DISABLED.getIntValue())
                    == WifiAPState.WIFI_AP_STATE_ENABLED.getIntValue())
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    caIpStateEnabled();
                }
           }
        }
    };

    private native static void caIpStateEnabled();

    private native static void caIpStateDisabled();
}
