package com.android.launcher3.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;

import com.android.launcher3.uioverrides.WallpaperColorInfo;

public class ThemeUtil {

    public static int getCurrentDarkTheme(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, ThemeConstants.SYSTEM_DARK_THEME_STYLE, ThemeConstants.DARK_THEME);
    }

    public static boolean nightModeWantsDarkTheme(Context context) {
        final Configuration config = context.getResources().getConfiguration();
        return (config.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int getQsbColor(Context context, WallpaperColorInfo wallpaperColorInfo) {
        final int currentTheme = getCurrentDarkTheme(context);
        final boolean nightModeWantsDarkTheme = nightModeWantsDarkTheme(context);
        if (nightModeWantsDarkTheme || wallpaperColorInfo.isDark()) {
            switch (currentTheme) {
                case ThemeConstants.DARK_THEME:
                    default:
                    return 0xD9282828;
                case ThemeConstants.BLACK_THEME:
                    return 0xFF111111;
                case ThemeConstants.EXTENDED_THEME:
                    return 0xFF332D4F;
                case ThemeConstants.CHOCOLATE_THEME:
                    return 0xFF473E38;
                case ThemeConstants.ELEGANT_THEME:
                    return 0xFF173145;
            }

        } else {
            return 0xCCFFFFFF;
        }
    }

    public static boolean isThemeDarkVariant(Context context, WallpaperColorInfo wallpaperColorInfo) {
        final boolean nightModeWantsDarkTheme = nightModeWantsDarkTheme(context);
        return (nightModeWantsDarkTheme || wallpaperColorInfo.isDark());
    }

}
