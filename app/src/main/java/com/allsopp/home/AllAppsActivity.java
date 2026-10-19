package com.allsopp.home;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** Every installed TV app in a grid. Back returns to the home screen. */
public class AllAppsActivity extends Activity {

    private List<Apps.Entry> apps;
    private View selected;

    private int dp(float v) {
        return Ui.dp(this, v);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        PackageManager pm = getPackageManager();
        apps = Apps.load(pm, getPackageName());

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0B0E14);
        root.setPadding(dp(56), dp(40), dp(56), 0);

        TextView title = Ui.text(this, 26, Color.WHITE, "sans-serif-light", 0.04f);
        title.setText("All apps");
        root.addView(title);

        GridView grid = new GridView(this);
        grid.setNumColumns(5);
        grid.setHorizontalSpacing(dp(22));
        grid.setVerticalSpacing(dp(26));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setPadding(dp(12), dp(24), dp(12), dp(40));
        grid.setClipToPadding(false);
        grid.setClipChildren(false);
        grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
        grid.setAdapter(new Adapter(pm));
        grid.setOnItemClickListener((parent, view, pos, id) -> Apps.launch(this, apps.get(pos)));
        grid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                highlight(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                highlight(null);
            }
        });
        root.addView(grid, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);
        grid.requestFocus();
    }

    private void highlight(View v) {
        if (selected != null) {
            selected.animate().scaleX(1f).scaleY(1f).translationZ(0).setDuration(140).start();
            selected.setSelected(false);
        }
        selected = v;
        if (v != null) {
            v.setSelected(true);
            v.animate().scaleX(1.1f).scaleY(1.1f).translationZ(dp(10)).setDuration(140).start();
        }
    }

    private final class Adapter extends BaseAdapter {
        private final PackageManager pm;

        Adapter(PackageManager pm) {
            this.pm = pm;
        }

        @Override
        public int getCount() {
            return apps.size();
        }

        @Override
        public Object getItem(int i) {
            return apps.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convert, ViewGroup parent) {
            Apps.Entry e = apps.get(i);
            LinearLayout item = new LinearLayout(AllAppsActivity.this);
            item.setOrientation(LinearLayout.VERTICAL);

            FrameLayout card = Ui.appCard(AllAppsActivity.this,
                    Apps.banner(e, pm), Apps.icon(e, pm), e.label, dp(12));
            card.setForeground(Ui.focusRing(AllAppsActivity.this, dp(12)));
            card.setDuplicateParentStateEnabled(true);
            item.addView(card, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(92)));

            TextView name = Ui.text(AllAppsActivity.this, 13, 0xD9FFFFFF, "sans-serif", 0.02f);
            name.setText(e.label);
            name.setSingleLine(true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(8);
            item.addView(name, lp);
            return item;
        }
    }
}
