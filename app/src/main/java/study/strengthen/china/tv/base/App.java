package study.strengthen.china.tv.base;

import android.app.Activity;

import androidx.multidex.MultiDexApplication;

import study.strengthen.china.tv.callback.EmptyCallback;
import study.strengthen.china.tv.callback.LoadingCallback;
import study.strengthen.china.tv.data.AppDataManager;
import study.strengthen.china.tv.server.ControlManager;
import study.strengthen.china.tv.util.HawkConfig;
import study.strengthen.china.tv.util.OkGoHelper;
import study.strengthen.china.tv.util.PlayerHelper;

import com.codelang.window.FloatingWindowManager;
import com.kingja.loadsir.core.LoadSir;
import com.orhanobut.hawk.Hawk;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.onAdaptListener;

/**
 * @author pj567
 * @date :2020/12/17
 * @description:
 */
public class App extends MultiDexApplication {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FloatingWindowManager.init(this);
        initParams();
        // OKGo
        OkGoHelper.init();
        // 初始化Web服务器
        ControlManager.init(this);
        //初始化数据库
        AppDataManager.init();
        LoadSir.beginBuilder()
                .addCallback(new EmptyCallback())
                .addCallback(new LoadingCallback())
                .commit();
        AutoSizeConfig.getInstance().setCustomFragment(false).setOnAdaptListener(new onAdaptListener() {
            @Override
            public void onAdaptBefore(Object target, Activity activity) {
                //使用以下代码, 可支持 Android 的分屏或缩放模式, 但前提是在分屏或缩放模式下当用户改变您 App 的窗口大小时
                //系统会重绘当前的页面, 经测试在某些机型, 某些情况下系统不会重绘当前页面, ScreenUtils.getScreenSize(activity) 的参数一定要不要传 Application!!!
//                        AutoSizeConfig.getInstance().setScreenWidth(ScreenUtils.getScreenSize(activity)[0]);
//                        AutoSizeConfig.getInstance().setScreenHeight(ScreenUtils.getScreenSize(activity)[1]);
//                        Log.d(TAG, "%s onAdaptBefore!"+target.getClass().getName());
            }

            @Override
            public void onAdaptAfter(Object target, Activity activity) {
//                        Log.d(TAG, "%s onAdaptAfter!"+target.getClass().getName());
            }
        });
        PlayerHelper.init();
    }

    private void initParams() {
        // Hawk
        Hawk.init(this).build();
        Hawk.put(HawkConfig.DEBUG_OPEN, false);
        if (!Hawk.contains(HawkConfig.PLAY_TYPE)) {
            Hawk.put(HawkConfig.PLAY_TYPE, 1);
        }
    }

    public static App getInstance() {
        return instance;
    }
}