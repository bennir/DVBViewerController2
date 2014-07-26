package de.bennir.dvbviewercontroller2;

import android.app.ListActivity;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import de.bennir.dvbviewercontroller2.adapter.NsdAdapter;
import de.bennir.dvbviewercontroller2.service.DVBService;

public class NsdActivity extends ListActivity {
    public static final String TAG = NsdActivity.class.toString();
    public static final String SERVICE_TYPE = "_dvbctrl._tcp.";
    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    ArrayList<NsdServiceInfo> mItems = new ArrayList<NsdServiceInfo>();
    NsdAdapter mAdapter;
    ListView mListView;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh")
                .setIcon(R.drawable.ic_ab_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(1, 1, 1, "Skip")
                .setIcon(R.drawable.ic_ab_next_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
        }

        switch (item.getItemId()) {
            case 0:
                discoverServices();

                break;
            case 1:
                String title = DVBService.DEMO_DEVICE;
                String server = "127.0.0.1";
                String port = "8000";

                Intent mIntent = new Intent(NsdActivity.this, ControllerActivity.class);
                mIntent.putExtra(DVBService.DVBHOST_KEY, title);
                mIntent.putExtra(DVBService.DVBIP_KEY, server);
                mIntent.putExtra(DVBService.DVBPORT_KEY, port);

                stopDiscovery();
                startActivity(mIntent);
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nsd);

        mNsdManager = (NsdManager) getSystemService(NSD_SERVICE);

        getActionBar().setTitle("Select Device");

        initializeNsd();
        discoverServices();

        mAdapter = new NsdAdapter(getApplicationContext(), R.layout.list_item_nsd, mItems);
        mListView = getListView();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            }
        });
    }


    public void initializeNsd() {
        initializeResolveListener();
        initializeDiscoveryListener();
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);

                try {
                    mNsdManager.resolveService(service, mResolveListener);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost " + service);

                for (int i = 0; i < mItems.size(); i++) {
                    if (mItems.get(i).getServiceName().equalsIgnoreCase(service.getServiceName())) {
                        mItems.remove(i);

                        Log.e(TAG, "service removed " + service);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                mItems.add(serviceInfo);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
    }

    public void discoverServices() {
        try {
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    public void stopDiscovery() {
        try {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        stopDiscovery();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mItems.clear();
        mAdapter.notifyDataSetChanged();
        discoverServices();
    }

    @Override
    protected void onDestroy() {
        stopDiscovery();

        super.onDestroy();
    }
}
