/*
 * Copyright (c) 2016 Appolica Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appolica.interactiveinfowindow.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * This class simply calls {@link android.view.ViewGroup#requestDisallowInterceptTouchEvent(boolean)}
 * in {@link android.view.ViewGroup#dispatchTouchEvent(MotionEvent)} and passes to it
 * {@link #disallowParentIntercept}.
 */
public class DisallowInterceptLayout extends FrameLayout {

    private boolean disallowParentIntercept = false;

    public DisallowInterceptLayout(Context context) {
        super(context);
    }

    public DisallowInterceptLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DisallowInterceptLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DisallowInterceptLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (disallowParentIntercept) {
            final ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(disallowParentIntercept);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setDisallowParentIntercept(boolean disallowParentIntercept) {
        this.disallowParentIntercept = disallowParentIntercept;
    }

    public boolean willDisallowParentIntercept() {
        return disallowParentIntercept;
    }
}
