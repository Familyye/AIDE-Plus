package com.hjq.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.List;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/XXPermissions
 *    time   : 2021/12/31
 *    desc   : 权限判断类
 */
final class PermissionApi {

    
    private static final PermissionDelegate DELEGATE = new PermissionDelegateImplV34();

    /**
     * 判断某个权限是否授予
     */
    static boolean isGrantedPermission( Context context,  String permission) {
        return DELEGATE.isGrantedPermission(context, permission);
    }

    /**
     * 判断某个权限是否被永久拒绝
     */
    static boolean isDoNotAskAgainPermission( Activity activity,  String permission) {
        return DELEGATE.isDoNotAskAgainPermission(activity, permission);
    }

    /**
     * 获取权限设置页的意图
     */
    static Intent getPermissionSettingIntent( Context context,  String permission) {
        return DELEGATE.getPermissionSettingIntent(context, permission);
    }

    /**
     * 重新检查权限回调的结果
     */
    static boolean recheckPermissionResult( Context context,  String permission, boolean grantResult) {
        return DELEGATE.recheckPermissionResult(context, permission, grantResult);
    }

    /**
     * 判断某个权限是否是特殊权限
     */
    static boolean isSpecialPermission( String permission) {
        return PermissionHelper.isSpecialPermission(permission);
    }

    /**
     * 判断某个权限集合是否包含特殊权限
     */
    static boolean containsSpecialPermission(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        for (String permission : permissions) {
            if (isSpecialPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某些权限是否全部被授予
     */
    static boolean isGrantedPermissions( Context context,  List<String> permissions) {
        if (permissions.isEmpty()) {
            return false;
        }

        for (String permission : permissions) {
            if (!isGrantedPermission(context, permission)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取已经授予的权限
     */
    static List<String> getGrantedPermissions( Context context,  List<String> permissions) {
        List<String> grantedPermission = new ArrayList<>(permissions.size());
        for (String permission : permissions) {
            if (isGrantedPermission(context, permission)) {
                grantedPermission.add(permission);
            }
        }
        return grantedPermission;
    }

    /**
     * 获取已经拒绝的权限
     */
    static List<String> getDeniedPermissions( Context context,  List<String> permissions) {
        List<String> deniedPermission = new ArrayList<>(permissions.size());
        for (String permission : permissions) {
            if (!isGrantedPermission(context, permission)) {
                deniedPermission.add(permission);
            }
        }
        return deniedPermission;
    }

    /**
     * 在权限组中检查是否有某个权限是否被永久拒绝
     *
     * @param activity              Activity对象
     * @param permissions            请求的权限
     */
    static boolean isDoNotAskAgainPermissions( Activity activity,  List<String> permissions) {
        for (String permission : permissions) {
            if (isDoNotAskAgainPermission(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取没有授予的权限
     *
     * @param permissions           需要请求的权限组
     * @param grantResults          允许结果组
     */
    static List<String> getDeniedPermissions( List<String> permissions,  int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            // 把没有授予过的权限加入到集合中
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permissions.get(i));
            }
        }
        return deniedPermissions;
    }

    /**
     * 获取已授予的权限
     *
     * @param permissions       需要请求的权限组
     * @param grantResults      允许结果组
     */
    static List<String> getGrantedPermissions( List<String> permissions,  int[] grantResults) {
        List<String> grantedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            // 把授予过的权限加入到集合中
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions.get(i));
            }
        }
        return grantedPermissions;
    }

    /**
     * 根据传入的权限自动选择最合适的权限设置页
     *
     * @param permissions                 请求失败的权限
     */
    static Intent getSmartPermissionIntent( Context context, List<String> permissions) {
        // 如果失败的权限里面不包含特殊权限
        if (permissions == null || permissions.isEmpty()) {
            return PermissionIntentManager.getApplicationDetailsIntent(context);
        }

        // 危险权限统一处理
        if (!PermissionApi.containsSpecialPermission(permissions)) {
            if (permissions.size() == 1) {
                return PermissionApi.getPermissionSettingIntent(context, permissions.get(0));
            }
            return PermissionIntentManager.getApplicationDetailsIntent(context, permissions);
        }

        // 特殊权限统一处理
        switch (permissions.size()) {
            case 1:
                // 如果当前只有一个权限被拒绝了
                return PermissionApi.getPermissionSettingIntent(context, permissions.get(0));
            case 2:
                if (!AndroidVersion.isAndroid13() &&
                    PermissionUtils.containsPermission(permissions, Permission.NOTIFICATION_SERVICE) &&
                    PermissionUtils.containsPermission(permissions, Permission.POST_NOTIFICATIONS)) {
                    return PermissionApi.getPermissionSettingIntent(context, Permission.NOTIFICATION_SERVICE);
                }
                break;
            case 3:
                if (AndroidVersion.isAndroid11() &&
                    PermissionUtils.containsPermission(permissions, Permission.MANAGE_EXTERNAL_STORAGE) &&
                    PermissionUtils.containsPermission(permissions, Permission.READ_EXTERNAL_STORAGE) &&
                    PermissionUtils.containsPermission(permissions, Permission.WRITE_EXTERNAL_STORAGE)) {
                    return PermissionApi.getPermissionSettingIntent(context, Permission.MANAGE_EXTERNAL_STORAGE);
                }
                break;
            default:
                break;
        }
        return PermissionIntentManager.getApplicationDetailsIntent(context);
    }
}
