package study.strengthen.china.tv.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import study.strengthen.china.tv.base.App;


/**
 * 做尺寸转换的工具类。在dp/sp/px之间自由转换
 */
public class DensityUtil {


    public static int dip2px(float dipValue) {
        float scale = App.getInstance().getResources()
                .getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int dip2px(float dipValue, float toscale) {
        float scale = App.getInstance().getResources()
                .getDisplayMetrics().density;
        return (int) ((dipValue * scale + 0.5f) * toscale);
    }

    public static int px2dip(float pxValue) {
        float scale = App.getInstance().getResources()
                .getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2sp(float pxValue) {
        float scale = App.getInstance().getResources()
                .getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(float spValue) {
        float scale = App.getInstance().getResources()
                .getDisplayMetrics().density;
        return (int) (spValue * scale + 0.5f);
    }

    public static int getWidth() {
        initScreen();
        if (screen != null) {
            return screen.widthPixels;
        }
        return 0;
    }

    public static int getHeight() {
        initScreen();
        if (screen != null) {
            return screen.heightPixels;
        }
        return 0;
    }
    public static int getNavigationBarHeight() {
        int result = 0;
        try {
            Resources res = App.getInstance().getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    static void initScreen() {
        if (screen == null) {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) App.getInstance()
                    .getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(dm);
            if (dm.widthPixels > dm.heightPixels) {
                screen = new Screen(dm.heightPixels, dm.widthPixels);
            } else {
                screen = new Screen(dm.widthPixels, dm.heightPixels);
            }
        }
    }

    public static int getStatusHeight() {
        if (STATUS_HEIGHT <= 0) {
            try {
                int resourceId = App.getInstance().getResources()
                        .getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    STATUS_HEIGHT = App.getInstance().getResources()
                            .getDimensionPixelSize(resourceId);
                }
            } catch (Exception ex) {
            }
        }
        return STATUS_HEIGHT;
    }

    static Screen screen = null;
    static int STATUS_HEIGHT = 0;

    public static class Screen {
        public int widthPixels;
        public int heightPixels;

        public Screen() {
        }

        public Screen(int widthPixels, int heightPixels) {
            this.widthPixels = widthPixels;
            this.heightPixels = heightPixels;
        }

        @Override
        public String toString() {
            return "(" + widthPixels + "," + heightPixels + ")";
        }

    }
    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = App.getInstance().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = App.getInstance().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

//    public static int getRealHeight(final Window window){//去掉状态栏和虚拟按键的高度
//        Rect frame = new Rect();
//        window.getDecorView().getWindowVisibleDisplayFrame(frame);
//        return px2dip(frame.bottom - frame.top);
//    }

    /**
     * Get Screen Real Width 包含状态栏导航栏
     *
     * @param context Context
     * @return Real Width
     */
    public static int getRealWidth(Context context) {
        Display display = getDisplay(context);
        if (display == null) {
            return 0;
        }
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * Get Screen Real Height 包含状态栏导航栏
     *
     * @param context Context
     * @return Real Height
     */
    public static int getRealHeight(Context context) {
        Display display = getDisplay(context);
        if (display == null) {
            return 0;
        }
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * Get Display
     * @param context Context for get WindowManager
     * @return Display
     */
    private static Display getDisplay(Context context) {
        WindowManager wm;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            wm = activity.getWindowManager();
        } else {
            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        if (wm != null) {
            return wm.getDefaultDisplay();
        }
        return null;
    }

    /**
     * Get Screen Height 不含状态栏导航栏
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * Get Screen Width 不含状态栏导航栏
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
