/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/
package io.github.zeroaicy.aide.utils;
import android.content.Context;
import android.os.Build;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TermuxShellEnvironment封装
 * 
 */
public class ZeroAicyTermuxShellEnvironment {

	private static final ZeroAicyTermuxShellEnvironment environment = new ZeroAicyTermuxShellEnvironment();

	public static ZeroAicyTermuxShellEnvironment getInstance() {
		return ZeroAicyTermuxShellEnvironment.environment;
	}

	private static Context applicationContext;

	public static void init(Context context) {
		if (applicationContext != null) {
			return;
		}
		ZeroAicyTermuxShellEnvironment.applicationContext = context.getApplicationContext();
		initProotEnv(ZeroAicyTermuxShellEnvironment.applicationContext);
	}

	public ZeroAicyTermuxShellEnvironment() {}
	
	
	public Map<String, String> getEnvironment(boolean isFailSafe) {
		HashMap<String, String> environment = new HashMap<>();
		putCustomizeEnv(environment);
		return environment;
	}
	
	public Map<String, String> getEnvironment(boolean isFailSafe, Map<String, String> env) {
		HashMap<String, String> environment = new HashMap<>(env);
		putCustomizeEnv(environment);
		return environment;
	}
	
	public List<String> setupShellCommandArguments(List<String> arguments) {
		
		List<String> result = new ArrayList<>();
        
		if ( ZeroAicyTermuxShellEnvironment.ProotMod) {
			String PACKAGE_NAME_PATH = ZeroAicyTermuxShellEnvironment.PACKAGE_NAME_PATH;
			//以proot方式启动
			result.add(ZeroAicyTermuxShellEnvironment.PROOT_PATH);

			result.add("--rootfs=/");
			result.add("--bind=" + PACKAGE_NAME_PATH + ":/data/data/com.termux");
			result.add("--bind=" + PACKAGE_NAME_PATH + ":/data/user/0/com.termux");

			result.add("--bind=" + PACKAGE_NAME_PATH + "/cache" + ":/linkerconfig");
		}
		
		result.addAll(arguments);
		
		return arguments;
	}
	
	
	// proot模式
	public static boolean ProotMod;

	//proot路径
	public static String PROOT_PATH;
	///data/data/包名 路径
	public static String PACKAGE_NAME_PATH;
	// /linkerconfig/ld.config.txt路径
	public static String PROOT_TMP_DIR;


	private static void initProotEnv(Context currentPackageContext) {


		if (ZeroAicyTermuxShellEnvironment.PROOT_PATH != null) {
			return;
		}
		
		try {
			ZeroAicyTermuxShellEnvironment.ProotMod = currentPackageContext.getApplicationInfo().targetSdkVersion > Build.VERSION_CODES.P;
		}
		catch (Throwable e) { 
			ProotMod = true;
		}
		
		if (ZeroAicyTermuxShellEnvironment.PROOT_PATH == null) {
			ZeroAicyTermuxShellEnvironment.PROOT_PATH = currentPackageContext.getApplicationInfo().nativeLibraryDir + "/libproot.so";
		}

		if (ZeroAicyTermuxShellEnvironment.PACKAGE_NAME_PATH == null) {
			ZeroAicyTermuxShellEnvironment.PACKAGE_NAME_PATH = currentPackageContext.getDataDir().getAbsolutePath();
		}

		ZeroAicyTermuxShellEnvironment.PROOT_TMP_DIR = new File(ZeroAicyTermuxShellEnvironment.PROOT_PATH).getParent();

		File cacheDirFile = new File(PACKAGE_NAME_PATH, "cache");
		if (!cacheDirFile.exists()) {
			cacheDirFile.mkdir();
		}
		File ld_config_txt_file = new File(cacheDirFile, "ld.config.txt");
		if (!ld_config_txt_file.exists() || ld_config_txt_file.length() == 0) {
			try {
				Files.copy(Paths.get("/linkerconfig/ld.config.txt"), ld_config_txt_file.toPath(),
						   StandardCopyOption.REPLACE_EXISTING);
				ld_config_txt_file.setReadable(true, false);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void putCustomizeEnv(HashMap<String, String> environment) {
		//为proot添加缓存路径 PROOT_TMP_DIR
		environment.put("PROOT_TMP_DIR", PROOT_TMP_DIR);
		//自定义参数
		// environment.put("JAVA_TOOL_OPTIONS", "-Duser.language=zh -Duser.region=CN");
	}
}

