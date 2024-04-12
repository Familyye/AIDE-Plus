package io.github.zeroaicy.aide.aapt2;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.aide.ui.build.android.AaptService;
import com.aide.ui.services.AssetInstallationService;
import com.aide.ui.util.BuildGradle;
import com.google.android.gms.internal.ads.q8;
import io.github.zeroaicy.util.ContextUtil;
import io.github.zeroaicy.util.Log;
import io.github.zeroaicy.util.reflect.ReflectPie;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.aide.ui.build.android.AaptService$b;
import com.aide.ui.util.*;
import java.util.*;
import io.github.zeroaicy.aide.utils.ZeroAicyBuildGradle;

public class AaptServiceArgs {

	AaptService AaptService;

	private ReflectPie mAaptS$cRef;

	public final String buildBin;


	private final File intermediates;
	private final File resDirOut;
	private final File compileDirFile;
	private final File mergedDirFile;

	//每个res路径的flatZip
	public final Set<String> flatZipFileSet = new LinkedHashSet<String>();
	//每个res路径的flat目录
	public final Set<String> flatDirSet = new HashSet<String>();

	public final PrintStream log;

	public final int defaultMinSdk;
	public final int defaultTargetSdk;


	private DataBindingBuilderProxy dataBindingBuilder;

	public DataBindingBuilderProxy getDataBindingBuilder() {
		if (this.dataBindingBuilder == null) {
			this.dataBindingBuilder = new DataBindingBuilderProxy(this);
		}
		return this.dataBindingBuilder;
	}

	public static SharedPreferences getProjectService() {
        return ContextUtil.getContext().getSharedPreferences("ProjectService", Context.MODE_PRIVATE);
    }
    public static String getCurrentAppHome() {
        return getProjectService().getString("CurrentAppHome", null);
    }

	//自定义数据
	public boolean isGradleProject;

	//主项目的res
	public String mainProjectResPath;

	public final boolean shrinkResources;

	public AaptServiceArgs(Object mAaptS$c_Object) {


		this.mAaptS$cRef = ReflectPie.on(mAaptS$c_Object);

		String currentAppHome = getCurrentAppHome();

		try {
			String buildGradlePath = currentAppHome + "/build.gradle";

			if (FileSystem.exists(buildGradlePath)) {
				isGradleProject = true;
				ZeroAicyBuildGradle buildGradle = ZeroAicyBuildGradle.getSingleton();
				buildGradle = buildGradle.getConfiguration((buildGradlePath));
				
				// 混淆代码
				this.shrinkResources = buildGradle.isShrinkResources();
				
				// 渠道包 暂时不用渠道包的 minsdk
				this.defaultMinSdk = parseInt(buildGradle.getMinSdkVersion(null), 14);
				this.defaultTargetSdk = parseInt(buildGradle.getTargetSdkVersion(null), 28);
			} else {
				this.defaultMinSdk = 14;
				this.defaultTargetSdk = 28;
				this.shrinkResources = false;
			}
		}
		catch (Throwable e) {
			this.defaultMinSdk = 14;
			this.defaultTargetSdk = 28;
			this.shrinkResources = false;
		}


		this.isBuildRefresh = ((Boolean)mAaptS$cRef.get("aM")).booleanValue();

		this.androidJar = mAaptS$cRef.get("Hw");

		//resource.ap_
		this.resourcesApPath = mAaptS$cRef.get("gn");
		// 构建缓存路径
		this.buildBin = new File(this.resourcesApPath).getParent();
		// 日志输出
		this.log = new PrintStream(Log.AsyncOutputStreamHold.createOutStream(new File(buildBin, "intermediates/aapt_log.log")));

		//gen查找packageName
		this.genPackageNameMap = mAaptS$cRef.get("EQ");

		//gen对应的 res(包含res依赖，[0]为gen所在res)
		this.genResDirsMap = mAaptS$cRef.get("tp");

		//fullCustomVar();


		//主项目gen目录
		this.mainProjectGenDir = mAaptS$cRef.get("Zo");

		//主项目res目录
		List<String> mainResDirs = this.genResDirsMap.get(this.mainProjectGenDir);
		for (String resDir : mainResDirs) {
			//有时mainResDirs.get(0)是generated
			//本着不修改AIDE给的所有数据，在此过滤
			if (resDir.endsWith("/res")) {
				this.mainProjectResPath = resDir;
				break;
			}
		}

		//res -> bin的res(正好可以用于DataBinding存放脱糖的xml)
		this.allResourceMap = mAaptS$cRef.get("u7");

		List<String> assets = mAaptS$cRef.get("VH");
		if (assets != null) {
			this.assetsList.addAll(assets);
		}


		this.intermediates = new File(this.buildBin, "intermediates");
		//aapt2缓存目录
		this.resDirOut = new File(this.intermediates, "res");

		this.compileDirFile = new File(this.resDirOut, "flat");
		this.compileDirFile.mkdirs();

		this.mergedDirFile = new File(this.resDirOut, "merged");
		this.mergedDirFile.mkdirs();


		// 子项目的gen目录
		this.subProjectGens = mAaptS$cRef.get("we");

		this.mergedAManifestMap = mAaptS$cRef.get("J8");

		this.injectedAManifestMap = mAaptS$cRef.get("QX");

		this.aManifestMap = mAaptS$cRef.get("Ws");

		this.mainPackageName = this.genPackageNameMap.get(this.mainProjectGenDir);
	}

	/**
	 * 填充自定义变量
	 */
	private void fullCustomVar() {
		this.resDirGenDir = new HashMap<>();
		this.packageDependencieMap = new HashMap<>();

		//res查找gen
		for (Map.Entry<String, List<String>> entry : this.genResDirsMap.entrySet()) {

			String genDir = entry.getKey();

			boolean isGradleProject = genDir.endsWith("/build/gen");
			String projectDir;
			int endIndex;
			if (isGradleProject) {
				endIndex = genDir.length() - "build/gen".length();
			} else {
				endIndex = genDir.length() - "gen".length();
			}
			projectDir = genDir.substring(0, endIndex);

			//初始化自定义
			for (String resDir :  entry.getValue()) {
				//排除 generated
				if (resDir.endsWith("/generated")) {
					continue;
				}
				if (resDir.startsWith(projectDir)) {
					this.resDirGenDir.put(resDir, genDir);
				}
			}
		}

		for (Map.Entry<String, List<String>> entry : genResDirsMap.entrySet()) {
			String genDir = entry.getKey();
			String packageName  = genPackageNameMap.get(genDir);
			//packageName依赖的子包名
			List<String> childPackageNames = new ArrayList<>();

			List<String> childResDirs = entry.getValue();

			//0是当前项目res
			for (int i = 1; i < childResDirs.size(); i++) {
				String childResDir = childResDirs.get(i);
				if (childResDir.endsWith("/generated")) {
					continue;
				}

				String childGenDir = this.resDirGenDir.get(childResDir);
				String childPackageName = genPackageNameMap.get(childGenDir);
				childPackageNames.add(childPackageName);
			}
			this.packageDependencieMap.put(packageName, childPackageNames);
		}
	}

	//根据已有推出，res查找gen目录
	private Map<String, String> resDirGenDir;
	//转化为包名依赖
	//根据已有推出，包名查找子包名
	public Map<String, List<String>> packageDependencieMap;

	public final boolean isBuildRefresh;

	public final String androidJar;

	public final String resourcesApPath;

	//主项目gen目录 Zo
	public final String mainProjectGenDir;

	public final List<String> assetsList = new ArrayList<>();

	//genDir -> packageName，但只有子项目，子项目的子项目没有
	public final Map<String, String> genPackageNameMap;

	//所有资源
	public final Map<String, String> allResourceMap;

	public final Map<String, List<String>> genResDirsMap;

	//所有的子项目gen路径
	public final List<String> subProjectGens;

	public final Map<String, String> mergedAManifestMap;

	public final Map<String, String> injectedAManifestMap;

	public final Map<String, String> aManifestMap;

	/**
	 * 根据已有数据计算
	 */
	public final String mainPackageName;
	/**
	 * 反射调用元方法
	 */
	public void buildRefresh() {
		this.mAaptS$cRef.call("v5");
	}
	public void generateBuildConfigJava() {
		this.mAaptS$cRef.call("Zo");
	}

	public String getAapt2Error(abcd.wf j62) {
		return this.mAaptS$cRef.call("VH", new Object[] {j62.j6(), j62.DW()}).get();
	}

	//合并AndroidManifestxml
	public AaptService$b mergedAndroidManifestxml() {
		return this.mAaptS$cRef.call("EQ").get();
	}


	public String getIntermediates() {
		if (!this.intermediates.exists()) {
			this.intermediates.mkdirs();
		}
		return this.intermediates.getAbsolutePath();
	}


	public String getAaptRulesPath() {
		return new File(this.intermediates, "aapt_rules.txt").getAbsolutePath();
	}
	//aapt2输出目录
	public File getResOutFile() {
		if (!this.resDirOut.exists()) {
			this.resDirOut.mkdirs();
		}
		return this.resDirOut;
	}

	//资源编译输出目录
	public File getFlatDirFile() {
		if (!compileDirFile.exists()) {
			compileDirFile.mkdirs();
		}
		// intermediates/res/flat
		return compileDirFile;
	}
	public String getCompileDirPath() {

		return getFlatDirFile().getAbsolutePath();
	}
	//资源合并输出目录
	public File getMergedDirFile() {
		if (!this.mergedDirFile.exists()) {
			this.mergedDirFile.mkdirs();
		}
		return this.mergedDirFile;
	}
	public String getMergedDirPath() {
		return getMergedDirFile().getAbsolutePath();
	}

	// 取得Aapt2路径
	public static String getAapt2Path2() {
		//会从assets自动解压
		String aapt2Path = AssetInstallationService.DW("aapt2", true);
		File aapt2File = new File(aapt2Path);
		if (!aapt2File.canExecute()) {
			aapt2File.setReadable(true, false);
			aapt2File.setExecutable(true, false);
		}
		return aapt2Path;
    }

	public static String getAapt2Path() {
		return ContextUtil.getContext().getApplicationInfo().nativeLibraryDir + "/libaapt2.so";
	}

	//字段含义
	//Map DW key: 项目路径 value: 依赖的项目(复数)

	//Map EQ key: gen路径 value: 包名

	//String Hw android.jar路径

	//Map J8 key: gen路径 value: merged AndroidManifest.xml路径

	//AaptService Mr

	//Map QX key: gen路径 value: injected AndroidManifest.xml路径

	//List VH assets路径

	//Ws key: injected AndroidManifest.xml路径 value: AndroidManifest.xml路径

	//XL true ?

	//Zo 主项目 gen路径

	//aM false ? 是否 构建缓存清除

	//gn resources.ap_

	//j3 false ?

	//j6 主项目路径 build.gradle父目录

	//Map tp key: gen路径 value: res路径

	//u7 key: res | bin/generated路径  value: build/bin/res路径

	//

	//we 子项目gen路径




	public static List<String> listLine(File file) {
		List<String> list = new ArrayList<String>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
			BufferedReader br = new BufferedReader(inputStreamReader);
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void writeLines(File outFile, List<String> lines) {
		File parentFile = outFile.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		try {
			PrintWriter printWriter = new PrintWriter(outFile);
			int size = lines.size();
			for (int i = 0; i < size; i++) {
				printWriter.println(lines.get(i));
			}
			printWriter.flush();
			printWriter.close();
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static int parseInt(String parseInt, int defaultValue){
		try {
			return Integer.parseInt(parseInt);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}
