package com.allsopp.home;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Finds, sorts and launches the TV apps installed on the set. */
final class Apps {

    static final class Entry {
        final ResolveInfo info;
        final String label;
        final String pkg;

        Entry(ResolveInfo info, String label) {
            this.info = info;
            this.label = label;
            this.pkg = info.activityInfo.packageName;
        }
    }

    private Apps() {}

    static List<Entry> load(PackageManager pm, String selfPackage) {
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<Entry> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ResolveInfo ri : pm.queryIntentActivities(query, 0)) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(selfPackage) || !seen.add(pkg)) continue;
            CharSequence l = ri.loadLabel(pm);
            out.add(new Entry(ri, l == null ? pkg : l.toString().trim()));
        }
        Collections.sort(out, (a, b) -> a.label.compareToIgnoreCase(b.label));
        return out;
    }

    /** Matches an app by its on-screen name: exact first, then as a whole word. */
    static Entry find(List<Entry> apps, String wanted) {
        for (Entry e : apps) {
            if (e.label.equalsIgnoreCase(wanted)) return e;
        }
        Pattern word = Pattern.compile("(?i)\\b" + Pattern.quote(wanted) + "\\b");
        for (Entry e : apps) {
            if (word.matcher(e.label).find()) return e;
        }
        String lower = wanted.toLowerCase(Locale.ROOT);
        for (Entry e : apps) {
            if (e.pkg.toLowerCase(Locale.ROOT).contains(lower)) return e;
        }
        return null;
    }

    /** The app's wide TV banner if it has one, otherwise null. */
    static Drawable banner(Entry e, PackageManager pm) {
        Drawable d = e.info.activityInfo.loadBanner(pm);
        if (d == null) d = e.info.activityInfo.applicationInfo.loadBanner(pm);
        return d;
    }

    static Drawable icon(Entry e, PackageManager pm) {
        return e.info.loadIcon(pm);
    }

    static void launch(Activity from, Entry e) {
        PackageManager pm = from.getPackageManager();
        Intent i = pm.getLeanbackLaunchIntentForPackage(e.pkg);
        if (i == null) i = pm.getLaunchIntentForPackage(e.pkg);
        if (i == null) {
            Toast.makeText(from, "Can't open " + e.label, Toast.LENGTH_SHORT).show();
            return;
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            from.startActivity(i);
        } catch (Exception ex) {
            Toast.makeText(from, "Can't open " + e.label, Toast.LENGTH_SHORT).show();
        }
    }
}
