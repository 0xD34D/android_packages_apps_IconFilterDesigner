/*
 * Copyright (C) 2014 Clark Scheff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.scheffsblend.iconfilters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import com.scheffsblend.iconfilters.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterDesignerActivity extends Activity {
    private static final String TAG_FILTER = "filter";
    private static final String FILTER_HUE = "hue";
    private static final String FILTER_SATURATION = "saturation";
    private static final String FILTER_INVERT = "invert";
    private static final String FILTER_BRIGHTNESS = "brightness";
    private static final String FILTER_CONTRAST = "contrast";
    private static final String FILTER_ALPHA = "alpha";
    private static final String FILTER_TINT = "tint";

    private IconPagerAdapter mIconAdapter;
    private FilterListAdapter mFilterAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ViewPager pager = (ViewPager) findViewById(R.id.icon_pager);
        mIconAdapter = new IconPagerAdapter(this);
        pager.setAdapter(mIconAdapter);
        mIconAdapter.notifyDataSetChanged();

        DynamicListView listView = (DynamicListView) findViewById(R.id.list_view);
        mFilterAdapter = new FilterListAdapter(this);
        listView.setAdapter(mFilterAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setFilterList(mFilterAdapter.getFilterList());

        ImageButton addFilterButton = (ImageButton) findViewById(R.id.add_filter);
        addFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] items = getResources().getStringArray(R.array.filters);
                AlertDialog.Builder b = new AlertDialog.Builder(FilterDesignerActivity.this);
                b.setTitle(R.string.filter_picker_title);
                b.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mFilterAdapter.addFilter(i);
                    }
                });
                b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                b.create().show();
            }
        });

        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.pager_indicator);
        indicator.setViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_xml:
                showXml();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateColorFilter(ColorMatrix cm) {
        if (mIconAdapter != null) mIconAdapter.setColorMatrix(cm);
    }

    private void showXml() {
        ArrayList<FilterListAdapter.FilterItem> filters = mFilterAdapter.getFilterList();
        StringBuilder sb = new StringBuilder();
        String name = null;
        String value = null;
        for (FilterListAdapter.FilterItem item : filters) {
            switch (item.filterType) {
                case FilterListAdapter.FILTER_TYPE_HUE:
                    name = FILTER_HUE;
                    value = "" + ((FilterListAdapter.AdjustableFilter) item).current;
                    break;
                case FilterListAdapter.FILTER_TYPE_SATURATION:
                    name = FILTER_SATURATION;
                    value = "" + ((FilterListAdapter.AdjustableFilter) item).current;
                    break;
                case FilterListAdapter.FILTER_TYPE_BRIGHTNESS:
                    name = FILTER_BRIGHTNESS;
                    value = "" + ((FilterListAdapter.AdjustableFilter) item).current;
                    break;
                case FilterListAdapter.FILTER_TYPE_CONTRAST:
                    name = FILTER_CONTRAST;
                    value = "" + ((FilterListAdapter.AdjustableFilter) item).current;
                    break;
                case FilterListAdapter.FILTER_TYPE_ALPHA:
                    name = FILTER_ALPHA;
                    value = "" + ((FilterListAdapter.AdjustableFilter) item).current;
                    break;
                case FilterListAdapter.FILTER_TYPE_INVERT:
                    name = FILTER_INVERT;
                    value = ((FilterListAdapter.ToggleableFilter) item).enabled ? "true" : "false";
                    break;
                case FilterListAdapter.FILTER_TYPE_TINT:
                    name = FILTER_TINT;
                    value = String.format("#%08x", ((FilterListAdapter.ValueFilter) item).value);
                    break;
            }
            sb.append(String.format("<%s name=\"%s\">%s</%s>\r\n",
                    TAG_FILTER, name, value, TAG_FILTER));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_show_xml_title);
        builder.setMessage(sb.toString());
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private class IconPagerAdapter extends PagerAdapter {
        private static final int ICONS_PER_PAGE = 5;

        List<ResolveInfo> mAppList;
        Context mContext;
        PackageManager mPm;
        LayoutInflater mInflater;
        private ColorMatrixColorFilter mColorFilter;

        public IconPagerAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mPm = context.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            mAppList = mPm.queryIntentActivities(mainIntent, 0);
            Collections.sort(mAppList, new ResolveInfo.DisplayNameComparator(mPm));
        }

        public void setColorMatrix(ColorMatrix cm) {
            mColorFilter = cm != null ? new ColorMatrixColorFilter(cm) : null;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (mAppList == null) return 0;

            int count = mAppList.size();
            return (int) Math.ceil((float)count / ICONS_PER_PAGE);
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View page = mInflater.inflate(R.layout.icon_page_item, container, false);
            for (int i = 0; i < ICONS_PER_PAGE; i++) {
                int index = position * ICONS_PER_PAGE + i;
                if (mAppList.size() <= index) continue;

                ImageView icon = (ImageView) ((ViewGroup) page).getChildAt(i);
                if (icon != null) {
                    ResolveInfo info = mAppList.get(index);
                    try {
                        Drawable d = mPm.getActivityIcon(
                                new ComponentName(info.activityInfo.packageName,
                                info.activityInfo.name));
                        if (d instanceof BitmapDrawable) {
                            BitmapDrawable bd = (BitmapDrawable) d;
                            bd.setColorFilter(mColorFilter);
                        }
                        icon.setImageDrawable(d);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            container.addView(page);
            return page;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof View) {
                container.removeView((View) object);
            }
        }
    }
}
