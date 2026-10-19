package com.allsopp.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Small shared view helpers so both screens look the same. */
final class Ui {

    static final int CARD = 0xFF1A1E27;

    private Ui() {}

    static int dp(Context c, float v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics()));
    }

    static TextView text(Context c, float sp, int color, String family, float spacing) {
        TextView t = new TextView(c);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setTypeface(Typeface.create(family, Typeface.NORMAL));
        t.setLetterSpacing(spacing);
        t.setIncludeFontPadding(false);
        t.setShadowLayer(12f, 0f, 2f, 0x66000000);
        return t;
    }

    static GradientDrawable rounded(int color, float radiusPx) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radiusPx);
        return g;
    }

    /** White outline that only appears while the view has remote focus. */
    static Drawable focusRing(Context c, float radiusPx) {
        GradientDrawable ring = new GradientDrawable();
        ring.setColor(Color.TRANSPARENT);
        ring.setCornerRadius(radiusPx);
        ring.setStroke(dp(c, 3), Color.WHITE);
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, ring);
        s.addState(new int[]{android.R.attr.state_selected}, ring);
        s.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        return s;
    }

    /** A rounded card showing the app's banner, or its icon and name if it has no banner. */
    static FrameLayout appCard(Context c, Drawable banner, Drawable icon, String label, float radiusPx) {
        FrameLayout card = new FrameLayout(c);
        card.setBackground(rounded(CARD, radiusPx));
        card.setClipToOutline(true);
        if (banner != null) {
            ImageView iv = new ImageView(c);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageDrawable(banner);
            card.addView(iv, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        } else {
            LinearLayout col = new LinearLayout(c);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER);
            ImageView iv = new ImageView(c);
            iv.setImageDrawable(icon);
            col.addView(iv, new LinearLayout.LayoutParams(dp(c, 44), dp(c, 44)));
            TextView name = text(c, 12, Color.WHITE, "sans-serif-medium", 0.02f);
            name.setText(label);
            name.setSingleLine(true);
            name.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(c, 8);
            col.addView(name, lp);
            card.addView(col, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
        return card;
    }
}
