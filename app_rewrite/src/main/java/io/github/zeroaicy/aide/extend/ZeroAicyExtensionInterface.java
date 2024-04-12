


package io.github.zeroaicy.aide.extend;


import com.aide.codemodel.language.classfile.ClassFilePreProcessor;
import com.aide.ui.MainActivity;
import com.aide.ui.build.packagingservice.ExternalPackagingService;
import io.github.zeroaicy.aide.activity.ZeroAicyMainActivity;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.aide.services.ZeroAicyExternalPackagingService;
import com.aide.ui.util.BuildGradle;
import io.github.zeroaicy.aide.utils.ZeroAicyBuildGradle;

/**
 * 1.aapt2
 * 2.class解析器
 * 3.d8[打包流程]
 * 
 */
 
/**
 * AIDE+底包的修改点都将调用此类
 * 优点是可以随时更换实现
 */
public class ZeroAicyExtensionInterface {
	//扩展接口

	/**
	 * 返回入口Activity类
	 * 主要是替换点击通知后的启动
	 */
	public static Class<? extends MainActivity> getLaunchActivityClass() {
		return ZeroAicyMainActivity.class;
	}

	//打包服务替换
	public static Class<?extends ExternalPackagingService> getExternalPackagingServiceClass() {
		return ZeroAicyExternalPackagingService.class;
	}

	//替换ClassFilePreProcessor实现
	public static ClassFilePreProcessor getClassFilePreProcessor() {
		return ZeroAicyClassFilePreProcessor.getSingleton();
	}
	//拦截类默认接口方法
	public static boolean isDefaultMethod(String methodSignature) {
		return ZeroAicyClassFilePreProcessor.isDefaultMethod(methodSignature);
	}
	
	//替换默认安装，true则拦截，false则不拦截
	public static boolean instalApp(final String apkFilePath) {
		return DistributeEvents.instalApp(apkFilePath);
	}
	//在Java项目中解除android.jar限制
	public static boolean isEnableAndroidApi() {
		return ZeroAicySetting.isEnableAndroidApi();
	}
	public static boolean isEnableADRT(){
		return ZeroAicySetting.enableADRT();
	}
	/*
	* 控制台是否启用分屏
	*/
	public static boolean isEnableSplitScreenConsole(){
		return false;
	}
	/**
	 * 修改maven默认下载路径
	 */
	 public static String getUserM2Repositories(){
		 return ZeroAicySetting.getDefaultSpString("user_m2repositories", null);
	 }
	 
	/**
	 * 替换BuildGradle解析实现
	 */
	public static BuildGradle getBuildGradle(){
		return ZeroAicyBuildGradle.getSingleton();
	}
}
