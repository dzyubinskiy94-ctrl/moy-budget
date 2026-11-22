package ru.mybudget.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;

/** Android home screen widget. Transactions are saved only by MainActivity's WebView. */
public final class QuickExpenseWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_WIDGET_CLICK = "ru.mybudget.app.WIDGET_CLICK";
    public static final String EXTRA_MODE = "widget_mode";
    public static final String EXTRA_ARGUMENT = "widget_arg";
    private static final String PREFS = "quick_expense_widget";
    private static final String KEY_SHORTCUTS = "shortcuts";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateOne(context, manager, id);
    }

    static void saveShortcuts(Context context, String payload) {
        if (payload == null || payload.length() > 30000) return;
        try {
            JSONObject json = new JSONObject(payload);
            JSONArray favorites = json.getJSONArray("favorites");
            JSONArray categories = json.getJSONArray("categories");
            if (favorites.length() > 3 || categories.length() > 100) return;
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_SHORTCUTS, payload).apply();
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName component = new ComponentName(context, QuickExpenseWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(component);
            for (int id : ids) updateOne(context, manager, id);
        } catch (Exception ignored) { }
    }

    private static void updateOne(Context context, AppWidgetManager manager, int id) {
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.quick_expense_widget);
        view.setOnClickPendingIntent(R.id.widgetAdd, action(context, id, 0, "add", ""));
        view.setOnClickPendingIntent(R.id.widgetTitle, action(context, id, 4, "add", ""));
        int[] buttons = {R.id.widgetChip1, R.id.widgetChip2, R.id.widgetChip3};
        int[] defaults = {100, 300, 500};
        String data = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SHORTCUTS, "");
        JSONArray favorites = null;
        JSONObject categories = new JSONObject();
        try {
            JSONObject info = new JSONObject(data);
            favorites = info.optJSONArray("favorites");
            JSONArray cats = info.optJSONArray("categories");
            if (cats != null) {
                for (int i = 0; i < cats.length(); i++) {
                    JSONObject c = cats.optJSONObject(i);
                    if (c != null) categories.put(c.optString("id"), c.optString("emoji"));
                }
            }
        } catch (Exception ignored) { }
        for (int i = 0; i < 3; i++) {
            String label = defaults[i] + " ₽";
            String actionType = "amount";
            String arg = String.valueOf(defaults[i]);
            JSONObject favorite = favorites == null ? null : favorites.optJSONObject(i);
            if (favorite != null && favorite.optDouble("amount") > 0) {
                String emoji = categories.optString(favorite.optString("category"), "⭐");
                String note = favorite.optString("note", "Расход").trim();
                if (note.length() > 10) note = note.substring(0, 10) + "…";
                int amount = (int) Math.round(favorite.optDouble("amount"));
                label = emoji + " " + note + "\n" + amount + " ₽";
                actionType = "favorite";
                arg = favorite.optString("id");
            }
            view.setTextViewText(buttons[i], label);
            view.setOnClickPendingIntent(buttons[i], action(context, id, i + 1, actionType, arg));
        }
        manager.updateAppWidget(id, view);
    }

    private static PendingIntent action(Context context, int widgetId, int buttonId, String mode, String arg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(ACTION_WIDGET_CLICK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(EXTRA_ARGUMENT, arg);
        int requestCode = widgetId * 10 + buttonId;
        return PendingIntent.getActivity(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
