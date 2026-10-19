package com.allsopp.home;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Lewis's TV home screen: a time-of-day greeting, clock and date in the centre,
 * and a dock of favourite apps along the bottom.
 */
public class MainActivity extends Activity {

    // ---- Personalise here -------------------------------------------------
    static final String NAME = "Lewis";
    /** Dock apps, left to right, matched by the name shown on the TV. */
    static final String[] FAVOURITES = {"TOD", "Netflix", "Prime Video"};
    // ------------------------------------------------------------------------

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageView wallpaper;
    private TextView greeting, clock, ampm, date, focusLabel;
    private LinearLayout dock;
    private String period = "";
    private int lastFocused = 0;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updateTime();
            Calendar c = Calendar.getInstance();
            long delay = (60 - c.get(Calendar.SECOND)) * 1000L - c.get(Calendar.MILLISECOND);
            handler.postDelayed(this, Math.max(delay, 500));
        }
    };

    private int dp(float v) {
        return Ui.dp(this, v);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(buildScreen());
    }

    @Override
    protected void onResume() {
        super.onResume();
        period = "";
        handler.removeCallbacks(tick);
        handler.post(tick);
        buildDock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    @Override
    public void onBackPressed() {
        // This is the home screen; there is nothing behind it to go back to.
    }

    private View buildScreen() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams fill = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        wallpaper = new ImageView(this);
        wallpaper.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(wallpaper, fill);

        // Soft darkening so the text and dock stay readable on any wallpaper.
        View scrim = new View(this);
        scrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x40000000, 0x26000000, 0x33000000, 0xB3000000}));
        root.addView(scrim, new FrameLayout.LayoutParams(fill));

        // Centre: greeting, clock, date.
        LinearLayout centre = new LinearLayout(this);
        centre.setOrientation(LinearLayout.VERTICAL);
        centre.setGravity(Gravity.CENTER_HORIZONTAL);

        greeting = Ui.text(this, 34, 0xF2FFFFFF, "sans-serif-light", 0.04f);
        centre.addView(greeting, wrap());

        LinearLayout timeRow = new LinearLayout(this);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeRow.setGravity(Gravity.BOTTOM);
        clock = Ui.text(this, 112, Color.WHITE, "sans-serif-thin", -0.01f);
        timeRow.addView(clock, wrap());
        ampm = Ui.text(this, 26, 0xCCFFFFFF, "sans-serif-light", 0.12f);
        LinearLayout.LayoutParams ampmLp = wrap();
        ampmLp.leftMargin = dp(10);
        ampmLp.bottomMargin = dp(16);
        timeRow.addView(ampm, ampmLp);
        LinearLayout.LayoutParams rowLp = wrap();
        rowLp.topMargin = dp(10);
        centre.addView(timeRow, rowLp);

        date = Ui.text(this, 17, 0xB3FFFFFF, "sans-serif", 0.28f);
        LinearLayout.LayoutParams dateLp = wrap();
        dateLp.topMargin = dp(14);
        centre.addView(date, dateLp);

        FrameLayout.LayoutParams centreLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        centreLp.bottomMargin = dp(110);
        root.addView(centre, centreLp);

        // Top right: all apps and settings.
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.addView(roundButton("☷︎",
                v -> startActivity(new Intent(this, AllAppsActivity.class))));
        top.addView(roundButton("⚙︎", v -> openSettings()));
        FrameLayout.LayoutParams topLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.END);
        topLp.topMargin = dp(36);
        topLp.rightMargin = dp(44);
        root.addView(top, topLp);

        // Bottom: focused app name above the dock.
        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setGravity(Gravity.CENTER_HORIZONTAL);
        bottom.setClipChildren(false);
        bottom.setClipToPadding(false);

        focusLabel = Ui.text(this, 14, Color.WHITE, "sans-serif-medium", 0.04f);
        focusLabel.setBackground(Ui.rounded(0x66000000, dp(14)));
        focusLabel.setPadding(dp(14), dp(6), dp(14), dp(6));
        focusLabel.setVisibility(View.INVISIBLE);
        bottom.addView(focusLabel, wrap());

        dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setClipChildren(false);
        dock.setClipToPadding(false);
        dock.setPadding(dp(24), dp(18), dp(24), dp(18));
        bottom.addView(dock, wrap());

        FrameLayout.LayoutParams bottomLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        bottomLp.bottomMargin = dp(30);
        root.addView(bottom, bottomLp);
        root.setClipChildren(false);
        return root;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private View roundButton(String glyph, View.OnClickListener click) {
        TextView b = Ui.text(this, 20, Color.WHITE, "sans-serif", 0f);
        b.setText(glyph);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.rounded(0x4D000000, dp(24)));
        b.setForeground(Ui.focusRing(this, dp(24)));
        b.setFocusable(true);
        b.setClickable(true);
        b.setOnClickListener(click);
        b.setOnFocusChangeListener((v, has) ->
                v.animate().scaleX(has ? 1.12f : 1f).scaleY(has ? 1.12f : 1f).setDuration(140).start());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(48), dp(48));
        lp.leftMargin = dp(12);
        b.setLayoutParams(lp);
        return b;
    }

    private void openSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        } catch (Exception ignored) {
        }
    }

    private void buildDock() {
        dock.removeAllViews();
        PackageManager pm = getPackageManager();
        List<Apps.Entry> apps = Apps.load(pm, getPackageName());

        for (String wanted : FAVOURITES) {
            Apps.Entry e = Apps.find(apps, wanted);
            if (e == null) continue;
            View tile = Ui.appCard(this, Apps.banner(e, pm), Apps.icon(e, pm), e.label, dp(14));
            makeDockTile(tile, e.label, v -> Apps.launch(this, e));
            dock.addView(tile, tileLp(dp(212), dp(119)));
        }

        // "All apps" tile at the end of the dock.
        TextView all = Ui.text(this, 13, Color.WHITE, "sans-serif-medium", 0.06f);
        all.setText("☷︎\nALL APPS");
        all.setGravity(Gravity.CENTER);
        all.setLineSpacing(dp(6), 1f);
        all.setBackground(Ui.rounded(0x59FFFFFF, dp(14)));
        makeDockTile(all, "All apps", v -> startActivity(new Intent(this, AllAppsActivity.class)));
        dock.addView(all, tileLp(dp(119), dp(119)));

        int index = Math.min(lastFocused, dock.getChildCount() - 1);
        if (index >= 0) dock.getChildAt(index).requestFocus();
    }

    private LinearLayout.LayoutParams tileLp(int w, int h) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        lp.leftMargin = dp(12);
        lp.rightMargin = dp(12);
        return lp;
    }

    private void makeDockTile(View tile, String label, View.OnClickListener click) {
        tile.setFocusable(true);
        tile.setClickable(true);
        tile.setOnClickListener(click);
        tile.setForeground(Ui.focusRing(this, dp(14)));
        tile.setOnFocusChangeListener((v, has) -> {
            v.animate().scaleX(has ? 1.14f : 1f).scaleY(has ? 1.14f : 1f)
                    .translationZ(has ? dp(14) : 0).setDuration(160).start();
            if (has) {
                lastFocused = dock.indexOfChild(v);
                focusLabel.setText(label);
                focusLabel.setVisibility(View.VISIBLE);
            } else {
                handler.post(() -> {
                    if (!dock.hasFocus()) focusLabel.setVisibility(View.INVISIBLE);
                });
            }
        });
    }

    private void updateTime() {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        String p;
        if (h >= 5 && h < 12) p = "morning";
        else if (h >= 12 && h < 17) p = "afternoon";
        else if (h >= 17 && h < 22) p = "evening";
        else p = "night";

        String hello = p.equals("night") ? "evening" : p;
        greeting.setText("Good " + hello + ", " + NAME);
        clock.setText(new SimpleDateFormat("h:mm", Locale.UK).format(c.getTime()));
        ampm.setText(c.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
        date.setText(new SimpleDateFormat("EEEE   d MMMM", Locale.UK)
                .format(c.getTime()).toUpperCase(Locale.UK));

        if (!p.equals(period)) {
            period = p;
            applyWallpaper(p);
        }
    }

    /**
     * Uses wallpaper_morning / _afternoon / _evening / _night (or a single "wallpaper")
     * from res/drawable-nodpi if present; otherwise a time-of-day sky gradient.
     */
    @SuppressWarnings("DiscouragedApi")
    private void applyWallpaper(String p) {
        int id = getResources().getIdentifier("wallpaper_" + p, "drawable", getPackageName());
        if (id == 0) id = getResources().getIdentifier("wallpaper", "drawable", getPackageName());
        if (id != 0) {
            wallpaper.setImageResource(id);
            return;
        }
        int[] sky;
        switch (p) {
            case "morning":
                sky = new int[]{0xFF14305A, 0xFF4F78A8, 0xFFE9A97E};
                break;
            case "afternoon":
                sky = new int[]{0xFF0A3B6E, 0xFF2F7DB8, 0xFF8CC6E6};
                break;
            case "evening":
                sky = new int[]{0xFF160F33, 0xFF5A2A63, 0xFFD0646C};
                break;
            default:
                sky = new int[]{0xFF03060E, 0xFF0B1630, 0xFF1D2D52};
        }
        wallpaper.setImageDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, sky));
    }
}
