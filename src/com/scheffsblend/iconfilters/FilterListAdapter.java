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

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

class FilterListAdapter extends BaseAdapter {
    public static final int FILTER_TYPE_HUE = 0;
    public static final int FILTER_TYPE_SATURATION = 1;
    public static final int FILTER_TYPE_BRIGHTNESS = 2;
    public static final int FILTER_TYPE_CONTRAST = 3;
    public static final int FILTER_TYPE_ALPHA = 4;
    public static final int FILTER_TYPE_INVERT = 5;
    public static final int FILTER_TYPE_TINT = 6;

    final int INVALID_ID = -1;

    ArrayList<FilterItem> mFilterItems = new ArrayList<FilterItem>();
    LayoutInflater mInflater;
    HashMap<FilterItem, Integer> mIdMap = new HashMap<FilterItem, Integer>();
    Context mContext;

    class FilterItem {
        int filterType;
        String label;

        public FilterItem(int type, String label) {
            filterType = type;
            this.label = label;
        }
    }

    class AdjustableFilter extends FilterItem {
        int min = 0;
        int max = 100;
        int current = 0;

        public AdjustableFilter(int type, int min, int max, int defValue, String label) {
            super(type, label);
            this.min = min;
            this.max = max;
            this.current = defValue;
        }
    }

    class ToggleableFilter extends FilterItem {
        boolean enabled = false;

        public ToggleableFilter(int type, String label) {
            super(type, label);
        }
    }

    class ValueFilter extends FilterItem {
        int value;

        public ValueFilter(int type, String label) {
            super(type, label);
        }
    }

    public FilterListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mFilterItems.size();
    }

    @Override
    public Object getItem(int i) {
        return mFilterItems.get(i);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return INVALID_ID;
        }
        FilterItem item = (FilterItem) getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        switch(mFilterItems.get(i).filterType) {
            case FILTER_TYPE_HUE:
            case FILTER_TYPE_SATURATION:
            case FILTER_TYPE_BRIGHTNESS:
            case FILTER_TYPE_CONTRAST:
            case FILTER_TYPE_ALPHA:
                convertView = newAdjustableView(parent);
                bindAdjustableView(i, convertView);
                break;
            case FILTER_TYPE_INVERT:
                convertView = newToggleableView(parent);
                bindToggleableView(i, convertView);
                break;
            case FILTER_TYPE_TINT:
                convertView = newTintView(parent);
                bindTintView(i, convertView);
                break;
        }
        View removeFilter = convertView.findViewById(R.id.remove_filter);
        removeFilter.setTag(mFilterItems.get(i));
        removeFilter.setOnClickListener(mRemoveFilterClickListener);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private View newAdjustableView(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.adjustable_filter_item, parent, false);

        return v;
    }

    private void bindAdjustableView(int i, View v) {
        AdjustableFilter f = (AdjustableFilter) mFilterItems.get(i);
        SeekBar sb = (SeekBar) v.findViewById(R.id.seekbar);
        sb.setMax(f.max - f.min);
        sb.setProgress(f.current - f.min);

        TextView min = (TextView) v.findViewById(R.id.min_value);
        min.setText("" + f.min);
        TextView max = (TextView) v.findViewById(R.id.max_value);
        max.setText("" + f.max);
        TextView current = (TextView) v.findViewById(R.id.current_value);
        current.setText("" + f.current);
        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText(f.label);

        current.setTag(f);
        sb.setTag(current);
        sb.setOnSeekBarChangeListener(mSeekBarChangeListener);
    }

    private View newToggleableView(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.toggleable_filter_item, parent, false);

        return v;
    }

    private void bindToggleableView(int i, View v) {
        ToggleableFilter f = (ToggleableFilter) mFilterItems.get(i);
        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText(f.label);

        CheckBox cb = (CheckBox) v.findViewById(R.id.toggle);
        cb.setChecked(f.enabled);

        cb.setTag(f);
        cb.setOnCheckedChangeListener(mCheckChangedListener);
    }

    private View newTintView(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.color_filter_item, parent, false);

        return v;
    }

    private void bindTintView(int i, View v) {
        final ValueFilter f = (ValueFilter) mFilterItems.get(i);
        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText(f.label);

        final View color = v.findViewById(R.id.color);
        color.setBackgroundColor(f.value);

        color.setTag(f);
        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ColorPickerDialog cpd = new ColorPickerDialog(mContext, f.value);
                cpd.setTitle(R.string.color_picker_dialog_title);
                cpd.setButton(DialogInterface.BUTTON_POSITIVE, mContext.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ValueFilter f = (ValueFilter) color.getTag();
                                f.value = cpd.getColor();
                                color.setBackgroundColor(f.value);
                                notifyDataSetChanged();
                            }
                        });
                cpd.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // nothing to do here
                    }
                });
                cpd.setAlphaSliderVisible(true);
                cpd.show();
            }
        });
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        FilterItem item = mFilterItems.get(position);
        switch (item.filterType) {
            case FILTER_TYPE_HUE:
            case FILTER_TYPE_SATURATION:
            case FILTER_TYPE_BRIGHTNESS:
            case FILTER_TYPE_CONTRAST:
            case FILTER_TYPE_ALPHA:
                return 0;
            case FILTER_TYPE_INVERT:
                return 1;
            case FILTER_TYPE_TINT:
                return 2;
            default:
                throw new IllegalArgumentException("Invalid filter type");
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (mContext instanceof FilterDesignerActivity) {
            ColorFilterUtils.Builder b = new ColorFilterUtils.Builder();
            for (FilterItem f : mFilterItems) {
                switch (f.filterType) {
                    case FILTER_TYPE_HUE:
                        b.hue(((AdjustableFilter) f).current);
                        break;
                    case FILTER_TYPE_SATURATION:
                        b.saturate(((AdjustableFilter) f).current);
                        break;
                    case FILTER_TYPE_BRIGHTNESS:
                        b.brightness(((AdjustableFilter) f).current);
                        break;
                    case FILTER_TYPE_CONTRAST:
                        b.contrast(((AdjustableFilter) f).current);
                        break;
                    case FILTER_TYPE_ALPHA:
                        b.alpha(((AdjustableFilter) f).current);
                        break;
                    case FILTER_TYPE_INVERT:
                        if (((ToggleableFilter) f).enabled) {
                            b.invertColors();
                        }
                        break;
                    case FILTER_TYPE_TINT:
                        b.tint(((ValueFilter) f).value);
                        break;
                }
            }
            ((FilterDesignerActivity) mContext).updateColorFilter(b.build());
        }
        super.notifyDataSetChanged();
    }

    public void addFilter(int type) {
        FilterItem f = null;
        switch (type) {
            case FILTER_TYPE_HUE:
                f = new AdjustableFilter(type, -180, 180, 0,
                        mContext.getString(R.string.filter_hue));
                break;
            case FILTER_TYPE_SATURATION:
                f = new AdjustableFilter(type, 0, 200, 100,
                        mContext.getString(R.string.filter_saturation));
                break;
            case FILTER_TYPE_BRIGHTNESS:
                f = new AdjustableFilter(type, 0, 200, 100,
                        mContext.getString(R.string.filter_brightness));
                break;
            case FILTER_TYPE_CONTRAST:
                f = new AdjustableFilter(type, -100, 100, 0,
                        mContext.getString(R.string.filter_contrast));
                break;
            case FILTER_TYPE_ALPHA:
                f = new AdjustableFilter(type, 0, 100, 100,
                        mContext.getString(R.string.filter_alpha));
                break;
            case FILTER_TYPE_INVERT:
                f = new ToggleableFilter(type, mContext.getString(R.string.filter_invert));
                break;
            case FILTER_TYPE_TINT:
                f = new ValueFilter(type, mContext.getString(R.string.filter_tint));
                ((ValueFilter) f).value = 0xff000000;
                break;
        }
        if (f != null) {
            mIdMap.put(f, mFilterItems.size());
            mFilterItems.add(f);
            notifyDataSetChanged();
        }
    }

    public ArrayList<FilterItem> getFilterList() {
        return mFilterItems;
    }

    private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            TextView tv = (TextView) seekBar.getTag();
            AdjustableFilter f = (AdjustableFilter) tv.getTag();
            f.current = i + f.min;
            tv.setText("" + f.current);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            notifyDataSetChanged();
        }
    };

    private OnCheckedChangeListener mCheckChangedListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            ToggleableFilter f = (ToggleableFilter) compoundButton.getTag();
            f.enabled = b;
            notifyDataSetChanged();
        }
    };

    private View.OnClickListener mRemoveFilterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FilterItem f = (FilterItem) view.getTag();
            if (f != null) mFilterItems.remove(f);
            notifyDataSetChanged();
        }
    };
}
