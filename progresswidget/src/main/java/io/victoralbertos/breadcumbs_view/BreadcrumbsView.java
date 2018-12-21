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
    int currentStep = 0;
    List<SeparatorView> steps;
    boolean animIsRunning;

    public BreadcrumbsView(Context context, int nSteps) {
        super(context);
        this.nSteps = nSteps;

        PropertiesHelper.init(this);

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

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                createSteps();
            }
        });
    }

    /**
     * Start counting from 0.
     *
     * @return the index of the current step.
     */
    public int getCurrentStep() {
        return currentStep;
    }

    float i = 0f;

    /**
     * Move to the next step. Throw if not steps are left to move forward
     *
     * @throws IndexOutOfBoundsException
     */
    public void nextStep(int index, float progress) throws IndexOutOfBoundsException {
        i += 0.1f;
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

        i -= 0.1f;

        final SeparatorView separatorView = steps.get(index);
        separatorView.animateFromVisitedStepToNextStep(new Runnable() {
            @Override
            public void run() {
            }
        }, 0);
    }

    /**
     * Should be called before this view is measured. Otherwise throw an IllegalStateException.
     *
     * @param currentStep the desired step
     */
    public void setCurrentStep(int currentStep) throws IllegalStateException {
        if (steps != null) {
            throw new IllegalStateException(
                    "Illegal attempt to set the value of the current step once the view has been measured");
        }
        this.currentStep = currentStep;
    }

    private void createSteps() {
        setOrientation(LinearLayout.HORIZONTAL);
        int nSeparators = nSteps - 1;
//        int widthDot = radius * 2;
//        int widthStep = ((getWidth() - widthDot) / nSeparators) - widthDot;
        int widthStep = getWidth() / nSteps;

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
