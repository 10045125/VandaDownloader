/*
 * Copyright 2016 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.victoralbertos.breadcumbs_view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class BreadcrumbsView extends LinearLayout {
    int visitedStepBorderDotColor;
    int visitedStepFillDotColor;
    int nextStepBorderDotColor;
    int nextStepFillDotColor;
    int visitedStepSeparatorColor;
    int nextStepSeparatorColor;
    int radius;
    int sizeDotBorder;
    int heightSeparator;
    int nSteps;
    List<SeparatorView> steps;

    public BreadcrumbsView(Context context, int nSteps) {
        super(context);
        this.nSteps = nSteps;

        PropertiesHelper.init(this);
//        createSteps();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                createSteps();
            }
        });
    }

    public BreadcrumbsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PropertiesHelper.init(this, attrs);
//        createSteps();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });
    }

    /**
     * Move to the next step. Throw if not steps are left to move forward
     *
     * @throws IndexOutOfBoundsException
     */
    public void nextStep(int index, float progress) throws IndexOutOfBoundsException {
        if ( steps == null || index > steps.size() - 1) {
            return;
        }
        SeparatorView separatorView = steps.get(index);
        separatorView.animateFromNextStepToVisitedStep(new Runnable() {
            @Override
            public void run() {
            }
        }, progress);
    }

    /**
     * Move to the previous step. Throw if not steps are left to go back.
     *
     * @throws IndexOutOfBoundsException
     */
    public void prevStep(int index) throws IndexOutOfBoundsException {
        final SeparatorView separatorView = steps.get(index);
        separatorView.animateFromVisitedStepToNextStep(new Runnable() {
            @Override
            public void run() {
            }
        }, 0);
    }

    private void createSteps() {
        setOrientation(LinearLayout.HORIZONTAL);
        int nSeparators = nSteps - 1;
        int widthStep = getWidth() / nSeparators;

        steps = new ArrayList<>(nSeparators);

        for (int i = 0; i < nSteps; i++) {
            SeparatorView separatorView =
                    new SeparatorView(getContext(), false, visitedStepSeparatorColor,
                            nextStepSeparatorColor, widthStep,
                            heightSeparator);
            addView(separatorView);

            steps.add(separatorView);
        }
    }

}
