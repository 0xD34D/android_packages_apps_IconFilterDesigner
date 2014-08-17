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

import android.graphics.Color;
import android.graphics.ColorMatrix;

import java.util.ArrayList;
import java.util.List;

public class ColorFilterUtils {
    /**
     * See the following links for reference
     * http://groups.google.com/group/android-developers/browse_thread/thread/9e215c83c3819953
     * http://gskinner.com/blog/archives/2007/12/colormatrix_cla.html
     * @param value
     */
    public static ColorMatrix adjustHue(float value) {
        ColorMatrix cm = new ColorMatrix();
        value = value / 180 * (float) Math.PI;
        if (value != 0) {
            float cosVal = (float) Math.cos(value);
            float sinVal = (float) Math.sin(value);
            float lumR = 0.213f;
            float lumG = 0.715f;
            float lumB = 0.072f;
            float[] mat = new float[]{
                    lumR + cosVal * (1 - lumR) + sinVal * (-lumR),
                    lumG + cosVal * (-lumG) + sinVal * (-lumG),
                    lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
                    lumR + cosVal * (-lumR) + sinVal * (0.143f),
                    lumG + cosVal * (1 - lumG) + sinVal * (0.140f),
                    lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
                    lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)),
                    lumG + cosVal * (-lumG) + sinVal * (lumG),
                    lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
                    0, 0, 0, 1, 0,
                    0, 0, 0, 0, 1};
            cm.set(mat);
        }
        return cm;
    }

    public static ColorMatrix adjustSaturation(float saturation) {
        saturation = Math.min(Math.max(saturation / 100, 0), 2);
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(saturation);

        return cm;
    }

    public static ColorMatrix invertColors() {
        float[] matrix = {
                -1, 0, 0, 0, 255, //red
                0, -1, 0, 0, 255, //green
                0, 0, -1, 0, 255, //blue
                0, 0, 0, 1, 0 //alpha
        };

        return new ColorMatrix(matrix);
    }

    public static ColorMatrix adjustBrightness(float brightness) {
        brightness = brightness / 100;
        ColorMatrix cm = new ColorMatrix();
        cm.setScale(brightness, brightness, brightness, 1);

        return cm;
    }

    public static ColorMatrix adjustContrast(float contrast) {
        contrast = contrast / 100 + 1;
        float o = (-0.5f * contrast + 0.5f) * 255;
        float[] matrix = {
                contrast, 0, 0, 0, o, //red
                0, contrast, 0, 0, o, //green
                0, 0, contrast, 0, o, //blue
                0, 0, 0, 1, 0 //alpha
        };

        return new ColorMatrix(matrix);
    }

    public static ColorMatrix adjustAlpha(float alpha) {
        alpha = Math.min(Math.max(alpha / 100, 0), 1);
        ColorMatrix cm = new ColorMatrix();
        cm.setScale(1, 1, 1, alpha);

        return cm;
    }

    public static ColorMatrix applyTint(int color) {
        float alpha = Color.alpha(color) / 255f;
        float red = Color.red(color) * alpha;
        float green = Color.green(color) * alpha;
        float blue = Color.blue(color) * alpha;

        float[] matrix = {
                1, 0, 0, 0, red, //red
                0, 1, 0, 0, green, //green
                0, 0, 1, 0, blue, //blue
                0, 0, 0, 1, 0 //alpha
        };

        return new ColorMatrix(matrix);
    }

    public static class Builder {
        private List<ColorMatrix> mMatrixList;

        public Builder() {
            mMatrixList = new ArrayList<ColorMatrix>();
        }

        public Builder hue(float value) {
            mMatrixList.add(adjustHue(value));
            return this;
        }

        public Builder saturate(float saturation) {
            mMatrixList.add(adjustSaturation(saturation));
            return this;
        }

        public Builder brightness(float brightness) {
            mMatrixList.add(adjustBrightness(brightness));
            return this;
        }

        public Builder contrast(float contrast) {
            mMatrixList.add(adjustContrast(contrast));
            return this;
        }

        public Builder alpha(float alpha) {
            mMatrixList.add(adjustAlpha(alpha));
            return this;
        }

        public Builder invertColors() {
            mMatrixList.add(ColorFilterUtils.invertColors());
            return this;
        }

        public Builder tint(int color) {
            mMatrixList.add(applyTint(color));
            return this;
        }

        public ColorMatrix build() {
            if (mMatrixList == null || mMatrixList.size() == 0) return null;

            ColorMatrix colorMatrix = new ColorMatrix();
            for (ColorMatrix cm : mMatrixList) {
                colorMatrix.postConcat(cm);
            }
            return colorMatrix;
        }
    }
}
