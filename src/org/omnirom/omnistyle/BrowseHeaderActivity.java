/*
 *  Copyright (C) 2017 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omnistyle;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class BrowseHeaderActivity extends Activity {
    private static final String TAG = "BrowseHeaderActivity";
    private static final String DEFAULT_HEADER_PACKAGE = "com.android.systemui";
    private static final String STATUS_BAR_CUSTOM_HEADER_IMAGE = "status_bar_custom_header_image";

    private static final boolean DEBUG = false;
    private List<DaylightHeaderInfo> mHeadersList;
    private Resources mRes;
    private String mPackageName;
    private String mHeaderName;
    private ListView mHeaderListView;
    private Spinner mHeaderSelect;
    private Map<String, String> mHeaderMap;
    private List<String> mLabelList;
    private HeaderListAdapter mHeaderListAdapter;
    private ProgressBar mProgress;

    private class DaylightHeaderInfo {
        public int mType = 0;
        public int mHour = -1;
        public int mDay = -1;
        public int mMonth = -1;
        public String mImage;
    }

    public class HeaderListAdapter extends ArrayAdapter<DaylightHeaderInfo> {
        private final LayoutInflater mInflater;

        public HeaderListAdapter(Context context) {
            super(context, R.layout.header_image, mHeadersList);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderImageHolder holder = HeaderImageHolder.createOrRecycle(mInflater, convertView);
            convertView = holder.rootView;
            DaylightHeaderInfo di = mHeadersList.get(position);
            holder.mHeaderImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier(di.mImage, "drawable", mPackageName), null));
            holder.mHeaderName.setText(di.mImage);
            return convertView;
        }
    }

    public static class HeaderImageHolder {
        public View rootView;
        public ImageView mHeaderImage;
        public TextView mHeaderName;

        public static HeaderImageHolder createOrRecycle(LayoutInflater inflater, View convertView) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.header_image, null);
                HeaderImageHolder holder = new HeaderImageHolder();
                holder.rootView = convertView;
                holder.mHeaderImage = (ImageView) convertView.findViewById(R.id.header_image);
                holder.mHeaderName = (TextView) convertView.findViewById(R.id.header_name);
                convertView.setTag(holder);
                return holder;
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                return (HeaderImageHolder)convertView.getTag();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_browse);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP|ActionBar.DISPLAY_SHOW_TITLE);

        mProgress = (ProgressBar) findViewById(R.id.browse_progress);
        mHeaderSelect = (Spinner) findViewById(R.id.package_select);
        mHeaderMap = new HashMap<String, String>();
        mLabelList = new ArrayList<String>();
        getAvailableHeaderPacks(mHeaderMap);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this,
                R.layout.spinner_item, mLabelList);
        mHeaderSelect.setAdapter(adapter);

        mHeaderSelect.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String label = mLabelList.get(position);
                loadHeaderPackage(mHeaderMap.get(label));
                mHeaderListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mHeadersList = new ArrayList<DaylightHeaderInfo>();
        mHeaderListView = (ListView) findViewById(R.id.package_images);
        mHeaderListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                DaylightHeaderInfo di = mHeadersList.get(i);
                Settings.System.putString(getContentResolver(), STATUS_BAR_CUSTOM_HEADER_IMAGE, mPackageName + "/" + di.mImage);
                Toast.makeText(BrowseHeaderActivity.this, R.string.custom_header_image_notice, Toast.LENGTH_LONG).show();
                finish();
                return true;
            }
        });
        mHeaderListAdapter = new HeaderListAdapter(this);
        mHeaderListView.setAdapter(mHeaderListAdapter);
        loadHeaderPackage(mHeaderMap.get(mLabelList.get(0)));
        mHeaderListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void getAvailableHeaderPacks(Map<String, String> headerMap) {
        String defaultLabel = null;
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.DaylightHeaderPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            if (packageName.equals(DEFAULT_HEADER_PACKAGE)) {
                defaultLabel = label;
            } else {
                headerMap.put(label, packageName);
            }
        }
        i.setAction("org.omnirom.DaylightHeaderPack1");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = packageName;
            }
            headerMap.put(label, packageName  + "/" + r.activityInfo.name);
        }
        mLabelList.addAll(headerMap.keySet());
        Collections.sort(mLabelList);
        if (defaultLabel != null) {
            mLabelList.add(0, defaultLabel);
            headerMap.put(defaultLabel, DEFAULT_HEADER_PACKAGE);
        }
    }

    private void loadHeaderPackage(String label) {
        if (DEBUG) Log.i(TAG, "Load header pack " + label);
        mProgress.setVisibility(View.VISIBLE);
        int idx = label.indexOf("/");
        if (idx != -1) {
            String[] parts = label.split("/");
            mPackageName = parts[0];
            mHeaderName = parts[1];
        } else {
            mPackageName = label;
            mHeaderName = null;
        }
        try {
            PackageManager packageManager = getPackageManager();
            mRes = packageManager.getResourcesForApplication(mPackageName);
            loadHeaders();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load icon pack " + mHeaderName, e);
            mRes = null;
        }
        mProgress.setVisibility(View.GONE);
    }

    private void loadHeaders() throws XmlPullParserException, IOException {
        mHeadersList.clear();
        InputStream in = null;
        XmlPullParser parser = null;

        try {
            if (mHeaderName == null) {
                if (DEBUG) Log.i(TAG, "Load header pack config daylight_header.xml");
                in = mRes.getAssets().open("daylight_header.xml");
            } else {
                int idx = mHeaderName.lastIndexOf(".");
                String headerConfigFile = mHeaderName.substring(idx + 1) + ".xml";
                if (DEBUG) Log.i(TAG, "Load header pack config " + headerConfigFile);
                in = mRes.getAssets().open(headerConfigFile);
            }
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
            parser.setInput(in, "UTF-8");
            loadResourcesFromXmlParser(parser);
        } finally {
            // Cleanup resources
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void loadResourcesFromXmlParser(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        do {
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equalsIgnoreCase("day_header")) {
                DaylightHeaderInfo headerInfo = new DaylightHeaderInfo();
                headerInfo.mType = 0;
                String day = parser.getAttributeValue(null, "day");
                if (day != null) {
                    headerInfo.mDay = Integer.valueOf(day);
                }
                String month = parser.getAttributeValue(null, "month");
                if (month != null) {
                    headerInfo.mMonth = Integer.valueOf(month);
                }
                String image = parser.getAttributeValue(null, "image");
                if (image != null) {
                    headerInfo.mImage = image;
                }
                if (headerInfo.mImage != null && headerInfo.mDay != -1 && headerInfo.mMonth != -1) {
                    mHeadersList.add(headerInfo);
                }
            } else if (name.equalsIgnoreCase("hour_header")) {
                DaylightHeaderInfo headerInfo = new DaylightHeaderInfo();
                headerInfo.mType = 1;
                String hour = parser.getAttributeValue(null, "hour");
                if (hour != null) {
                    headerInfo.mHour = Integer.valueOf(hour);
                }
                String image = parser.getAttributeValue(null, "image");
                if (image != null) {
                    headerInfo.mImage = image;
                }
                if (headerInfo.mImage != null && headerInfo.mHour != -1) {
                    mHeadersList.add(headerInfo);
                }
            } else if (name.equalsIgnoreCase("random_header") ||
                    name.equalsIgnoreCase("list_header")) {
                DaylightHeaderInfo headerInfo = new DaylightHeaderInfo();
                headerInfo.mType = 2;
                String image = parser.getAttributeValue(null, "image");
                if (image != null) {
                    headerInfo.mImage = image;
                }
                if (headerInfo.mImage != null) {
                    mHeadersList.add(headerInfo);
                }
            }
        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);
        if (DEBUG) Log.i(TAG, "loaded size = " + mHeadersList.size());
    }

    private Drawable getHeaderImage(int index) {
        DaylightHeaderInfo di = mHeadersList.get(index);
        return mRes.getDrawable(mRes.getIdentifier(di.mImage, "drawable", mPackageName), null);
    }
}
