package com.hjq.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import java.util.Collections;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/XXPermissions
 *    time   : 2022/06/11
 *    desc   : 权限委托基础实现
 */
class PermissionDelegateImplBase implements PermissionDelegate {

    @Override
    public boolean isGrantedPermission( Context context,  String permission) {
        if (PermissionUtils.equalsPermission(permission, Permission.BIND_VPN_SERVICE)) {
            return isGrantedVpnPermission(context);
        }

        return true;
    }

    @Override
    public boolean isDoNotAskAgainPermission( Activity activity,  String permission) {
        if (PermissionUtils.equalsPermission(permission, Permission.BIND_VPN_SERVICE)) {
            return false;
        }

        return false;
    }

    @Override
    public boolean recheckPermissionResult( Context context,  String permission, boolean grantResult) {
        // 如果这个权限是特殊权限，则需要重新检查权限的状态
        if (PermissionApi.isSpecialPermission(permission)) {
            return isGrantedPermission(context, permission);
        }

        if (PermissionHelper.findAndroidVersionByPermission(permission) > AndroidVersion.getAndroidVersionCode()) {
            // 如果是申请了新权限，但却是旧设备上面运行的，会被系统直接拒绝，在这里需要重新检查权限的状态
            return isGrantedPermission(context, permission);
        }
        return grantResult;
    }

    @Override
    public Intent getPermissionSettingIntent( Context context,  String permission) {
        if (PermissionUtils.equalsPermission(permission, Permission.BIND_VPN_SERVICE)) {
            return getVpnPermissionIntent(context);
        }

        return PermissionIntentManager.getApplicationDetailsIntent(context, Collections.singletonList(permission));
    }

    /**
     * 是否有 VPN 权限
     */
    private static boolean isGrantedVpnPermission( Context context) {
        return VpnService.prepare(context) == null;
    }

    /**
     * 获取 VPN 权限设置界面意图
     */
    private static Intent getVpnPermissionIntent( Context context) {
        Intent intent = VpnService.prepare(context);
        if (!PermissionUtils.areActivityIntent(context, intent)) {
            intent = PermissionIntentManager.getApplicationDetailsIntent(context);
        }
        return intent;
    }

    /**
     * 获取应用详情页 Intent
     */
    static Intent getApplicationDetailsIntent( Context context) {
        return PermissionIntentManager.getApplicationDetailsIntent(context);
    }
}
