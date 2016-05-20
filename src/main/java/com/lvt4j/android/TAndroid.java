package com.lvt4j.android;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.lvt4j.basic.TLog;

public final class TAndroid {

    public static boolean isNetAvaiable(Context context) {
        ConnectivityManager cwjManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cwjManager.getActiveNetworkInfo();

        return info != null && info.isAvailable();
    }

    public static String getIP() {
        StringBuilder IPStringBuilder = new StringBuilder();
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnumeration
                        .nextElement();
                Enumeration<InetAddress> inetAddressEnumeration = networkInterface
                        .getInetAddresses();
                while (inetAddressEnumeration.hasMoreElements()) {
                    InetAddress inetAddress = inetAddressEnumeration
                            .nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && !inetAddress.isLinkLocalAddress()
                            && inetAddress.isSiteLocalAddress()) {
                        IPStringBuilder.append(inetAddress.getHostAddress()
                                .toString() + "\n");
                    }
                }
            }
        } catch (SocketException ex) {
            TLog.e("Error on get IP.", ex);
            return "0.0.0.0";
        }
        return IPStringBuilder.toString();
    }

    @SuppressWarnings("deprecation")
    public static int getScreenWidth(Context context) {
        return ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getWidth();
    }

    @SuppressWarnings("deprecation")
    public static int getScreenHeight(Context context) {
        return ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getHeight();
    }

    public static void setWidth(View v, int w) {
        LayoutParams lp = v.getLayoutParams();
        lp.width = w;
        v.setLayoutParams(lp);
    }

    public static void setHeight(View v, int h) {
        LayoutParams lp = v.getLayoutParams();
        lp.height = h;
        v.setLayoutParams(lp);
    }

    /**
     * 打开软键盘
     * 
     * @param actv
     */
    public static void openSoftInput(Activity actv) {
        ((InputMethodManager) actv
                .getSystemService(Activity.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                        InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 关闭软键盘
     * 
     * @param actv
     */
    public static void closeSoftInput(Activity actv) {
        ((InputMethodManager) actv
                .getSystemService(Activity.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(actv.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
	
	/**
     * 创建一个阴影Drawable
     * @param context
     * @param shadowColor 阴影颜色
     * @param centerAlpha 中心alpha值
     * @param aroundAlpha 四周alpha值
     * @param shadowWidth 阴影宽度
     * @param isOval 圆形还是矩形
     * @param radius 矩形时，圆角矩形的圆角值
     * @return
     */
    public static final Drawable shadowDrawable(Context context, int shadowColor, int centerAlpha, int aroundAlpha, int shadowWidth, boolean isOval, Integer radius){
        int stepWidth = (int) context.getResources().getDisplayMetrics().density;
        int red = Color.red(shadowColor);
        int green = Color.green(shadowColor);
        int blue = Color.blue(shadowColor);
        int stepNum = shadowWidth/stepWidth+1;
        if (stepNum<2) stepNum = 2;
        int stepAlpha = (centerAlpha-aroundAlpha)/(stepNum-1);
        GradientDrawable[] gds = new GradientDrawable[stepNum];
        for (int i = 0; i < stepNum; i++) {
            gds[i] = new GradientDrawable();
            int alpha = aroundAlpha+stepAlpha*i;
            int color = Color.argb(alpha, red, green, blue);
            gds[i].setStroke(stepWidth, color);
            if (isOval){
                gds[i].setShape(GradientDrawable.OVAL);
            } else {
                gds[i].setShape(GradientDrawable.RECTANGLE);
                if (radius!=null) gds[i].setCornerRadius(radius);
            }
        }
        LayerDrawable shadow = new LayerDrawable(gds);
        for (int i = 0; i < stepNum; i++) {
            shadow.setLayerInset(i,stepWidth*i, stepWidth*i, stepWidth*i, stepWidth*i);
        }
        return shadow;
    }
	
}
