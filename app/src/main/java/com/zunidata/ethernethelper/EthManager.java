package com.zunidata.ethernethelper;

/**
 * Created by user on 2015/9/11.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.net.EthernetDataTracker;
import android.net.ethernet.EthernetManager;
import android.provider.Settings;

import java.lang.reflect.Method;

public class EthManager {

    public static final int STATIC_IP_MODE = 1;
    private ContentResolver contentResolver;
    private boolean isZuniRKModel = false;
    private Context context = null;
    private final String ETHERNET_SERVICE = "ethernet";
    private EthernetManager mEthernetManager;

    public EthManager(Context context) {
        contentResolver = context.getContentResolver();
        isZuniRKModel = isZuniRKModel();
        this.context = context;
        mEthernetManager = (EthernetManager) context.getSystemService("ethernet");
    }

    public String getMode() {
        if (isZuniRKModel)
            return Settings.System.getString(contentResolver, "ethernet_use_static_ip");
        else
            return Settings.Secure.getString(contentResolver, "eth_mode");
    }

    public String getIP() {
        if (isZuniRKModel)
            return Settings.System.getString(contentResolver, "ethernet_static_ip");
        else
            return Settings.Secure.getString(contentResolver, "eth_ip");
    }

    public String getMask() {
        if (isZuniRKModel)
            return Settings.System.getString(contentResolver, "ethernet_static_netmask");
        else
            return Settings.Secure.getString(contentResolver, "eth_mask");
    }

    public String getGateway() {
        if (isZuniRKModel)
            return Settings.System.getString(contentResolver, "ethernet_static_gateway");
        else
            return Settings.Secure.getString(contentResolver, "eth_route");
    }

    public String getDNS() {
        if (isZuniRKModel)
            return Settings.System.getString(contentResolver, "ethernet_static_dns1");
        else
            return Settings.Secure.getString(contentResolver, "eth_dns");
    }

    public void setMode(int mode) {
        if (isZuniRKModel)
            Settings.System.putString(contentResolver, "ethernet_use_static_ip", "1");
        else
            Settings.Secure.putString(contentResolver, "eth_mode", "manual");
    }

    public void setIP(String ip) {
        if (isZuniRKModel)
            Settings.System.putString(contentResolver, "ethernet_static_ip", ip);
        else
            Settings.Secure.putString(contentResolver, "eth_ip", ip);
    }

    public void setMask(String ip) {
        if (isZuniRKModel)
            Settings.System.putString(contentResolver, "ethernet_static_netmask", ip);
        else
            Settings.Secure.putString(contentResolver, "eth_mask", ip);
    }

    public void setGateway(String gateway) {
        if (isZuniRKModel)
            Settings.System.putString(contentResolver, "ethernet_static_gateway", gateway);
        else
            Settings.Secure.putString(contentResolver, "eth_route", gateway);
    }

    public void setDNS(String dns) {
        if (isZuniRKModel)
            Settings.System.putString(contentResolver, "ethernet_static_dns1", dns);
        else
            Settings.Secure.putString(contentResolver, "eth_dns", dns);
    }

    private static boolean isZuniRKModel() {
        String manufacturer = getSystemProperties("ro.board.platform");
        return manufacturer != null && (manufacturer.equalsIgnoreCase("rk3188"));
    }

    private static String getSystemProperties(String property) {
        try {
            Class a = Class.forName("android.os.SystemProperties");
            Method getProp = a.getMethod("get", String.class);
            String str = (String) getProp.invoke(a, property);
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void enable() {
        if (mEthernetManager == null)
            return;
        /**
         * use code below for rk platform
         * remember to change the provided jar to "class-rk.jar" in dependencies */
        if (isZuniRKModel) {
            mEthernetManager.setEthernetEnabled(true);
            for (int i = 0; i < 10; i++) {
                if (mEthernetManager.getEthernetIfaceState() ==
                        EthernetDataTracker.ETHER_IFACE_STATE_UP) {
                    break;
                } else {
                    delay(1000);
                }
            }
        /**
         * use code below for mx platform
         * remember to change the provided jar to "class-mx.jar" in dependencies */
        /*
        } else {
            mEthernetManager.setEthEnabled(true);
            for (int i = 0; i < 10; i++) {
                if (mEthernetManager.getEthState() ==
                        EthernetManager.ETH_STATE_ENABLED) {
                    break;
                } else {
                    delay(1000);
                }
            }
        */
        }
    }

    public void disable() {
        if (mEthernetManager == null)
        return;
        /**
         * use code below for rk platform
         * remember to change the provided jar to "class-rk.jar" in dependencies */
        if (isZuniRKModel) {
            mEthernetManager.setEthernetEnabled(false);
            for (int i = 0; i < 10; i++) {
                if (mEthernetManager.getEthernetIfaceState() ==
                        EthernetDataTracker.ETHER_IFACE_STATE_DOWN) {
                    break;
                } else {
                    delay(1000);
                }
            }
        /**
         * use code below for mx platform
         * remember to change the provided jar to "class-mx.jar" in dependencies */
        /*
        } else {
            mEthernetManager.setEthEnabled(false);
            for (int i = 0; i < 10; i++) {
                if (mEthernetManager.getEthState() ==
                        EthernetManager.ETH_STATE_DISABLED) {
                    break;
                } else {
                    delay(1000);
                }
            }
        */
        }
    }

    private void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
