package io.victoralbertos.breadcumbs_view;

import android.content.res.TypedArray;
import android.util.AttributeSet;

final class PropertiesHelper {

    static void init(BreadcrumbsView breadcrumbsView) {
        breadcrumbsView.visitedStepBorderDotColor =
                breadcrumbsView.getResources().getColor(R.color.def_visited_step_border_dot_color);
        breadcrumbsView.visitedStepFillDotColor =
                breadcrumbsView.getResources().getColor(R.color.def_visited_step_fill_dot_color);
        breadcrumbsView.nextStepBorderDotColor =
                breadcrumbsView.getResources().getColor(R.color.def_next_step_border_dot_color);
        breadcrumbsView.nextStepFillDotColor =
                breadcrumbsView.getResources().getColor(R.color.def_next_step_fill_dot_color);
        breadcrumbsView.visitedStepSeparatorColor =
                breadcrumbsView.getResources().getColor(R.color.def_visited_step_separator_color);
        breadcrumbsView.nextStepSeparatorColor =
                breadcrumbsView.getResources().getColor(R.color.def_next_step_separator_color);
        breadcrumbsView.radius = breadcrumbsView.getResources().getDimensionPixelSize(R.dimen.def_radius_dot);
        breadcrumbsView.sizeDotBorder =
                breadcrumbsView.getResources().getDimensionPixelSize(R.dimen.def_size_dot_border);
        breadcrumbsView.heightSeparator =
                breadcrumbsView.getResources().getDimensionPixelSize(R.dimen.def_height_separator);
    }

    static void init(BreadcrumbsView breadcrumbsView, AttributeSet attrs) {
        TypedArray a = breadcrumbsView.getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BreadcrumbsView,
                0, 0);

        try {
            breadcrumbsView.nSteps = a.getInt(R.styleable.BreadcrumbsView_numberOfSteps, 0);
            if (breadcrumbsView.nSteps == 0) {
                throw new IllegalStateException(
                        "You must set a number of steps. Use 'numberOfSteps' attribute to supply a value greater than 1");
            }

            breadcrumbsView.visitedStepBorderDotColor =
                    a.getColor(R.styleable.BreadcrumbsView_visitedStepBorderDotColor,
                            breadcrumbsView.getResources().getColor(
                                    R.color.def_visited_step_border_dot_color));
            breadcrumbsView.visitedStepFillDotColor =
                    a.getColor(R.styleable.BreadcrumbsView_visitedStepFillDotColor,
                            breadcrumbsView.getResources().getColor(
                                    R.color.def_visited_step_fill_dot_color));
            breadcrumbsView.nextStepBorderDotColor =
                    a.getColor(R.styleable.BreadcrumbsView_nextStepBorderDotColor,
                            breadcrumbsView.getResources().getColor(
                                    R.color.def_next_step_border_dot_color));
            breadcrumbsView.nextStepFillDotColor = a.getColor(R.styleable.BreadcrumbsView_nextStepFillDotColor,
                    breadcrumbsView.getResources().getColor(R.color.def_next_step_fill_dot_color));
            breadcrumbsView.visitedStepSeparatorColor =
                    a.getColor(R.styleable.BreadcrumbsView_visitedStepSeparatorColor,
                            breadcrumbsView.getResources().getColor(
                                    R.color.def_visited_step_separator_color));
            breadcrumbsView.nextStepSeparatorColor =
                    a.getColor(R.styleable.BreadcrumbsView_nextStepSeparatorColor,
                            breadcrumbsView.getResources().getColor(
                                    R.color.def_next_step_separator_color));
            breadcrumbsView.radius = a.getDimensionPixelSize(R.styleable.BreadcrumbsView_radiusDot,
                    breadcrumbsView.getResources().getDimensionPixelSize(R.dimen.def_radius_dot));
            breadcrumbsView.sizeDotBorder = a.getDimensionPixelSize(R.styleable.BreadcrumbsView_sizeDotBorder,
                    breadcrumbsView.getResources().getDimensionPixelSize(R.dimen.def_size_dot_border));
            breadcrumbsView.heightSeparator = a.getDimensionPixelSize(R.styleable.BreadcrumbsView_heightSeparator,
                    breadcrumbsView.getResources().getDimensionPixelSize(R.dimen.def_height_separator));
        } finally {
            a.recycle();
        }
    }
}
