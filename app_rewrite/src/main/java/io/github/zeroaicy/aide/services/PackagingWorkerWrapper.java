package io.github.zeroaicy.aide.services;

import android.os.Build;
import android.text.TextUtils;
import com.aide.ui.build.packagingservice.ExternalPackagingService;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.util.Log;
import io.github.zeroaicy.util.MD5Util;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import io.github.zeroaicy.aide.utils.AndroidManifestParser;
import com.android.tools.r8.CompilationFailedException;
import com.aide.ui.util.FileSystem;
import com.aide.ui.util.BuildGradle;
import io.github.zeroaicy.aide.utils.ZeroAicyBuildGradle;

public abstract class PackagingWorkerWrapper extends ExternalPackagingService.ExternalPackagingServiceWorker {

	ExternalPackagingService externalPackagingService;
	private String noBackupFilesDirPath;
	public PackagingWorkerWrapper(ExternalPackagingService externalPackagingService) {
		//父类没有使用外部类[ExternalPackagingService]
		null.super();
		this.externalPackagingService = externalPackagingService;
	}
	public String getUserAndroidJar() {
		return ZeroAicySetting.getDefaultSpString("user_androidjar", getNoBackupFilesDirPath() + "/.aide/android.jar");
	}

	/**
	 * no_backup路径
	 */
	public String getNoBackupFilesDirPath() {
		if (noBackupFilesDirPath == null) {
			noBackupFilesDirPath = externalPackagingService.getNoBackupFilesDir().getAbsolutePath();
		}
		return this.noBackupFilesDirPath;
	}

	@Override
	public final void Zo(String str, String[] strArr, String[] strArr2, String[] strArr3, String str2, String str3, String str4, String[] strArr4, String str5, String str6, String str7, String str8, String str9, boolean z, boolean z2, boolean z3) {
		if (this.Hw == null) {
			this.Hw = new ArrayList<>();
		}
		//添加打包任务
		this.Hw.add(getTaskWrapper(str, strArr, strArr2, strArr3, str2, str3, str4, strArr4, str5, str6, str7, str8, str9, z, z2, z3));
	}

	public abstract TaskWrapper getTaskWrapper(String str, String[] strArr, String[] strArr2, String[] strArr3, String str2, String str3, String str4, String[] strArr4, String str5, String str6, String str7, String str8, String str9, boolean z, boolean z2, boolean z3);

	public abstract class TaskWrapper extends Task {
		//class转dex默认输出目录
		private final String defaultClassDexCacheDirPath;

		private final String defaultIntermediatesDirPath;

		/**
		 * android:extractNativeLibs="false"必须无压缩
		 */
		private boolean androidFxtractNativeLibs = true;
		// 低于21时d8无法dexing AIDE产生的class文件
		private int minSdk = 21;

		/*************************参数*********************************/
		// 主项目.class file缓存目录
		private final String mainClassCacheDir;
		//所有class文件，顶级父目录
		private String[] allClassFileRootDirs;

		//构建时缓存文件的父目录
		private String outDirPath;

		//构建文件地址[类型分为apk和zip]
		private String outFilePath;
		// .ap_文件(aapt2 link产物)
		private String aaptResourcePath;


		//项目的jardex默认目录
		private String defaultJarDexDirPath;

		//所有依赖库
		private String[] dependencyLibs;
		//源码目录
		private String[] sourceDirs;
		//源码目录
		private String[] nativeLibDirs;
		//构建刷新
		private final boolean buildRefresh;

		private final String zipalignPath;

		private final String signaturePath;

		private final String signaturePassword;

		private final String signatureAlias;

		private final String signatureAliasPassword;

		private String mergerCachePath;



		public int getMinSdk() {
			return minSdk;
		}

		public String getZipalignPath() {
			return this.zipalignPath;
		}

		public final boolean isNotDebugFormAIDE;
		public TaskWrapper(String mainClassCacheDir, String[] classFileRootDirs, String[] sourceDirs, String[] dependencyLibs, String outDirPath, String Zo, String aaptResourcePath, String[] nativeLibDirs, String outFilePath, String signaturePath, String signaturePassword, String signatureAlias, String signatureAliasPassword, boolean buildRefresh, boolean Ws, boolean QX) {
			super(mainClassCacheDir, classFileRootDirs, sourceDirs, dependencyLibs, outDirPath, Zo, aaptResourcePath, nativeLibDirs, outFilePath, signaturePath, signaturePassword, signatureAlias, signatureAliasPassword, buildRefresh, Ws, QX);

			this.mainClassCacheDir = mainClassCacheDir;

			File mainProjectClassCacheDirFile = new File(this.mainClassCacheDir);
			String classCacheDirFileName = mainProjectClassCacheDirFile.getName();

			File parentFile = mainProjectClassCacheDirFile.getParentFile();
			this.defaultIntermediatesDirPath = new File(parentFile, "intermediates").getAbsolutePath();

			this.defaultClassDexCacheDirPath = this.getIntermediatesChildDirPath(classCacheDirFileName);
			this.mergerCachePath = getIntermediatesChildDirPath("dex");

			this.allClassFileRootDirs = classFileRootDirs;
			this.sourceDirs = sourceDirs;
			this.dependencyLibs = dependencyLibs;

			//  build/bin | bin/[debug | release]/dex
			this.outDirPath = outDirPath;

			//Zo
			this.defaultJarDexDirPath = this.getIntermediatesChildDirPath("jardex");

			this.aaptResourcePath = aaptResourcePath;
			this.nativeLibDirs = nativeLibDirs;
			this.outFilePath = outFilePath;

			this.signaturePath = signaturePath;
			this.signaturePassword = signaturePassword;
			this.signatureAlias = signatureAlias;
			this.signatureAliasPassword = signatureAliasPassword;

			this.buildRefresh = buildRefresh;

			this.minSdk = this.getProjectMinSdk();
			this.zipalignPath = externalPackagingService.getApplicationInfo().nativeLibraryDir + "/libzipalign.so";
			

			// 安卓且输出是classesdebug 也没debug-aide
			this.isNotDebugFormAIDE = isAndroidProject() && !"classesdebug".equals(classCacheDirFileName);
			
		}

		private int getProjectMinSdk() {
			final int defaultProjectMinSdk = 21;
			final int projectMinSdk;

			if (isAndroidProject()) {
				AndroidManifestParser androidManifestParser = getAndroidManifestParser();
				if (androidManifestParser == null) {
					projectMinSdk = defaultProjectMinSdk;
				} else {
					this.androidFxtractNativeLibs = androidManifestParser.getExtractNativeLibs();

					final int androidProject ;
					try {
						String minSdkVersion = androidManifestParser.getMinSdkVersion();
						androidProject = Integer.parseInt(minSdkVersion);
					}
					catch (NumberFormatException e) {
						androidProject = defaultProjectMinSdk;
					}
					projectMinSdk = androidProject;
				}
			} else {
				//Java项目为当前设备
				projectMinSdk = Build.VERSION.SDK_INT;
			}
			return projectMinSdk;
		}

		private ZeroAicyBuildGradle zeroAicyBuildGradle;
		public ZeroAicyBuildGradle getZeroAicyBuildGradle() {
			if (zeroAicyBuildGradle != null) {
				return zeroAicyBuildGradle;
			}
			ZeroAicyBuildGradle singleton = ZeroAicyBuildGradle.getSingleton();
			String buildOutDirPath = this.getBuildOutDirPath();

			// 不是gradle项目
			String suffix = "/build/bin";

			if (!buildOutDirPath.endsWith(suffix)) {
				this.zeroAicyBuildGradle = singleton;
				return singleton;
			}

			File buildGradleFile = new File(buildOutDirPath.substring(0, buildOutDirPath.length() - suffix.length()), "build.gradle");
			if (!buildGradleFile.exists()) {
				this.zeroAicyBuildGradle = singleton;
				return singleton;				
			}
			return this.zeroAicyBuildGradle = singleton.getConfiguration(buildGradleFile.getAbsolutePath());
		}
		private AndroidManifestParser getAndroidManifestParser() {
			String buildOutDirPath = getBuildOutDirPath();
			AndroidManifestParser androidManifestParser = null;  
			for (String androidManifestAbsolutePath : new String[]{
				"injected/AndroidManifest.xml",
				"merged/AndroidManifest.xml",
				"../AndroidManifest.xml"}) {
				File androidManifestXmlFile = new File(buildOutDirPath, androidManifestAbsolutePath);
				if (androidManifestXmlFile.exists()) {
					androidManifestParser = AndroidManifestParser.get(androidManifestXmlFile);
					break;
				}
			}
			return androidManifestParser;
		}

		public boolean isAndroidProject() {
			return getOutFilePath().endsWith(".apk");
		}

		// intermediates目录地址
		public String getDefaultIntermediatesDirPath() {
			return defaultIntermediatesDirPath;
		}
		public String getMergerCacheDirPath() {
			return this.mergerCachePath;
		}

		//返回 intermediates子文件夹
		public String getIntermediatesChildDirPath(String childDirName) {
			File childFile = new File(getDefaultIntermediatesDirPath(), childDirName);
			if (!childFile.exists() && !childFile.mkdirs()) {
				Log.d("Could not create dir: " + childFile);
			}
			return childFile.getAbsolutePath();
		}

		@Override
		public final void Mr() {
			try {
				packaging();
			}
			catch (Throwable e) {
				if (e instanceof CompilationFailedException 
					|| e.getCause() instanceof CompilationFailedException) {
					e = e.getCause();
					while (e instanceof CompilationFailedException) {
						e = e.getCause();	
					}
					String message = e.getMessage();
					Log.d("日志", message);
					int index;
					if ((index = message.indexOf(" is defined multiple times:")) > 0) {
						//合并错误
						String type = message.substring(0, index);
						String[] files = message.substring(index + "is defined multiple times: ".length()).split(", ");

						StringBuilder sb = new StringBuilder("合并错误: ")
							.append(type)
							.append("在以下文件中重复定义");
						for (String path : files) {
							sb.append("\n");
							sb.append(FileSystem.getName(path));

						}
						throw new Error(sb.toString());
					}

				}
				Log.d("打包错误", "堆栈 -> ", e);
				Throwable cause = e;
				while ((cause = e.getCause()) != null) {
					e = cause;
				}
				if (e instanceof Error) {
					throw (Error)e;
				}
				if (e instanceof RuntimeException) {
					throw (RuntimeException)e;
				}
				throw new Error(e);
			}
		}
		/*************************抽象层，屏蔽所有父类方法*********************************/
		/**
		 * 打包
		 */
		public abstract void packaging() throws Throwable;
		public abstract void packagingJavaProject(List<String> dexZipPathList) throws Throwable;
		public abstract void packagingAndroidProject(List<String> dexZipPathList) throws Throwable;
		/**
		 * d8 class jar 并返回classesDexZip[d8输出zip]
		 */
		public abstract List<String> getClassesDexZipList() throws Throwable;


		/******************************************/
		/**
		 * 返回android:extractNativeLibs="false"的值
		 */
		public boolean getAndroidFxtractNativeLibs() {
			return this.androidFxtractNativeLibs;
		}


		/**********共用层，共用一些相同的代码逻辑***********************************/

		/**
		 * 签名路径
		 */
		public String getSignaturePath() {
			return this.signaturePath;
		}
		//签名密码
		public String getSignaturePassword() {
			return signaturePassword;
		}
		//别名
		public String getSignatureAlias() {
			return signatureAlias;
		}
		//别名密码
		public String getSignatureAliasPassword() {
			return signatureAliasPassword;
		}

		/**
		 * 构建刷新
		 */
		public boolean isBuildRefresh() {
			return this.buildRefresh;
		}

		public String[] getAllClassFileRootDirs() {
			return this.allClassFileRootDirs;
		}
		/**
		 * 返回主项目class文件缓存路径，即主项目class文件的输出目录
		 */
		public String getMainClassCacheDir() {
			return this.mainClassCacheDir;
		}
		/**
		 * 返回默认dex文件缓存路径，[从class文件dexing的]
		 */
		public String getDefaultClassDexCacheDirPath() {
			return defaultClassDexCacheDirPath;
		}

		/**
		 * 返回class dexing后的dex缓存路径
		 */
		public String getClassDexFileCache(String classFileSubPath) {
			int endIndex = classFileSubPath.length() - ".class".length();
			return getDefaultClassDexCacheDirPath() 
				+ classFileSubPath.substring(0, endIndex) + ".dex";
		}
		/**
		 * 返回jar的dex.zip缓存路径
		 */
		public String getJarDexCachePath(String jarFilePath) {
			String jarDexCacheDirPath = getJarDexCacheDirPath(jarFilePath);
			// 

			File jarFile = new File(jarFilePath);
			String depFileName = jarFile.getName();
			if (jarFilePath.endsWith(".exploded.aar/classes.jar")) {
				String name = jarFile.getParentFile().getName();
				depFileName = name.substring(0, name.length() - ".exploded.aar".length()) + ".jar";
			} else {

			}
			String jarDexZipName = depFileName + ".dex.zip";

			//不会为null
			if (jarDexCacheDirPath == null) {
				jarDexCacheDirPath = getDefaultJarDexDirPath();
			}
			return jarDexCacheDirPath + "/" +  jarDexZipName;
		}

		/**
		 * 返回 getIntermediatesChildDirPath("jardex")目录下路径的md5码
		 * 如果是maven仓库则去掉仓库路径在进行计算
		 */
		public String getJarDexCacheDirPath(String dependencyLibPath) {
			String userM2repositories = ZeroAicySetting.getDefaultSpString("user_m2repositories", null);
			String defaultM2repositoriesDirPath = getNoBackupFilesDirPath() + "/.aide/maven";

			String key;
			/**
			 * 是否是在自定义仓库缓存文件夹中
			 */
			if (!TextUtils.isEmpty(userM2repositories) 
				&& dependencyLibPath.startsWith(userM2repositories)) {
				//指定了minsdk，会污染缓存，所以只能放项目目录
				key = dependencyLibPath.substring(userM2repositories.length());
			} else if (dependencyLibPath.startsWith(defaultM2repositoriesDirPath)) {
				key = dependencyLibPath.substring(defaultM2repositoriesDirPath.length());
			} else {
				key = dependencyLibPath;	
			}

			return getIntermediatesChildDirPath("jardex/" + MD5Util.stringMD5(key));
		}
		/**
		 * 全部依赖，不只是jar
		 */
		public String[] getAllDependencyLibs() {
			return this.dependencyLibs;
		}

		public String getDefaultJarDexDirPath() {
			return this.defaultJarDexDirPath;
		}

		public String[] getSourceDirs() {
			return this.sourceDirs;
		}
		public String[] getNativeLibDirs() {
			return this.nativeLibDirs;
		}
		/**
		 * 输出
		 */
		public String getBuildOutDirPath() {
			return this.outDirPath;
		}

		/**
		 * 返回输出文件路径
		 * 可以在此处实现将apk生成项目缓存路径中
		 */
		public String getOutFilePath() {
			return this.outFilePath;
		}

		//resource.ap_文件
		public String getAAptResourceFilePath() {
			if (getZeroAicyBuildGradle().isShrinkResources()) {
				String resourcesAp_Dir = new File(this.aaptResourcePath).getParent();
				return new File(resourcesAp_Dir, "resources_optimize.ap_").getAbsolutePath();
			}
			return this.aaptResourcePath;
		}

		/**
		 * 显示构建总进度，只有一级进度
		 */
		public void showProgress(String progressTitle, int progress) {
			ExternalPackagingService.ExternalPackagingServiceWorker.v5(PackagingWorkerWrapper.this, progressTitle + "...", progress);
			//super.a8(progressTitle, progress);
		}
	}


	/**
	 * -dontwarn *
	 private void 混淆(boolean 混淆, List<String> classesDexZipList){
	 if ( 混淆 ){
	 String mixUpDexZipFilePath = getMixUpDexZipFilePath();
	 File outDexZipFile = new File(mixUpDexZipFilePath);
	 //删除缓存文件
	 outDexZipFile.delete();

	 List<String> argsList  = new ArrayList<String>();
	 String user_androidjar = getUserAndroidJar(); 
	 //fillD8Args(argsList, false, false, user_androidjar, null, mixUpDexZipFilePath);
	 // --pg-compat             # 在 Proguard 兼容模式下使用 R8 进行编译。
	 // --pg-conf <file>        # 混淆器配置<file>.
	 // --pg-conf-output <file> # 将集合配置输出到<file>.
	 // --pg-map-output <file>  # 将结果名称和线路映射输出到<file>
	 argsList.add("--pg-compat");
	 argsList.add("--pg-conf");
	 argsList.add("混淆配置");



	 argsList.add("--release");
	 argsList.add("--dex");

	 //输入dexs
	 argsList.addAll(classesDexZipList);
	 com.android.tools.r8.D8.main(argsList.toArray(new String[argsList.size()]));

	 //添加混淆后的dex.zip
	 classesDexZipList.clear();
	 classesDexZipList.add(mixUpDexZipFilePath);
	 }
	 }*/
}
