package io.agora.vlive.utils;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtil {
    public static void removeFromParent(View view) {
        if (view == null) return;
        ViewGroup parent = view.getParent() instanceof ViewGroup ? (ViewGroup) view.getParent() : null;
        if (parent != null) {
            parent.removeView(view);
        }
    }
}
