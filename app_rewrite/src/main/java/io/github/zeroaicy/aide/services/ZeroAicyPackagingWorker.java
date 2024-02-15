package io.github.zeroaicy.aide.services;

import java.io.*;

import android.content.SharedPreferences;
import android.text.TextUtils;
import com.aide.common.AppLog;
import com.aide.ui.build.packagingservice.ExternalPackagingService;
import com.android.apksig.ApkSigner;
import com.android.tools.r8.OutputMode;
import com.android.tools.r8.R8;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.origin.Origin;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.util.FileUtil;
import io.github.zeroaicy.util.Log;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import sun1.security.pkcs.PKCS8Key;

public class ZeroAicyPackagingWorker extends PackagingWorkerWrapper{

	private static final String TAG = "Worker";
	public ZeroAicyPackagingWorker(ExternalPackagingService service){
		super(service);
	}
	@Override
	public PackagingWorkerWrapper.TaskWrapper 
	getTaskWrapper(String mainClassCacheDir, String[] classFileRootDirs, String[] sourceDirs, 
				   String[] dependencyLibs, String outDirPath, String jardexPath, 
				   String aAptResourcePath, String[] nativeLibDirs, String outFilePath, 
				   String signaturePath, String signaturePassword, String signatureAlias, 
				   String signatureAliasPassword, boolean buildRefresh, boolean Ws, boolean QX){

		/**
		 // Zo jardex路径
		 Log.d("Zo", Zo);

		 // Ws dex优化[optimze_dex]
		 Log.d("Ws", Ws);

		 // QX minsdk是否大于等于21
		 Log.d("QX", QX);
		 */

		return new ZeroAicyR8Task(mainClassCacheDir, classFileRootDirs, sourceDirs, 
								  dependencyLibs, outDirPath, jardexPath, aAptResourcePath, 
								  nativeLibDirs, outFilePath, signaturePath, signaturePassword, 
								  signatureAlias, signatureAliasPassword, buildRefresh, Ws, QX);
	}

	public class ZeroAicyR8Task extends TaskWrapper{
		public ZeroAicyR8Task(String mainClassCacheDir, String[] classFileRootDirs, String[] sourceDirs, String[] dependencyLibs, String outDirPath, String Zo, String aAptResourcePath, String[] nativeLibDirs, String outFilePath, String signaturePath, String signaturePassword, String signatureAlias, String signatureAliasPassword, boolean buildRefresh, boolean Ws, boolean QX){
			super(mainClassCacheDir, classFileRootDirs, sourceDirs, dependencyLibs, outDirPath, Zo, aAptResourcePath, nativeLibDirs, outFilePath, signaturePath, signaturePassword, signatureAlias, signatureAliasPassword, buildRefresh, Ws, QX);
		}

		@Override
		public void packaging() throws Throwable{
			long now = nowTime();

			this.initBuildEnvironment();

			if ( !getOutFilePath().endsWith(".zip") 
				&& isMinify() ){
				packagingAndroidMinify();
				logDebug("混淆打包共用时: " + (nowTime() - now) + "ms");

				return;
			}
			logDebug("开始dxing class");

			List<String> classesDexZipList = getClassesDexZipList();
			logDebug("dxing class 共用时: " + (nowTime() - now) + "ms");

			now = nowTime();

			//Java工程
			if ( getOutFilePath().endsWith(".zip") ){
				packagingJavaProject(classesDexZipList);
			}
			else{
				//打包安卓项目
				packagingAndroidProject(classesDexZipList);
			}

			logDebug("打包共用时: " + (nowTime() - now) + "ms");
		}

		/**
		 * 返回 合并class文件缓存后的输出路径？

		 */
		private String getMainClassesDexZipFilePath(){
			return getMergerCacheDirPath() + "/classes.dex.zip";
		}

		private String getDependencyMergerFilePath(){
			return getMergerCacheDirPath() + "/dependency_merger.dex.zip";
		}

		//混淆
		private File getMixUpDexZipFile(boolean delete){
			File file = new File(getMergerCacheDirPath() , "/classes_mix_up.dex.zip");
			if ( file.exists() && delete ){
				file.delete();
			}
			return file;
		}

		/**
		 * d8 class jar 并返回classesDexZip[d8输出zip]
		 */
		@Override
		public List<String> getClassesDexZipList() throws Throwable{
			List<String> classesDexZipList = new ArrayList<>();
			//先dexing 主classes.dex，即从源码编译的class
			checkInterrupted();

			showProgress("Run D8 Dexing", 60);

			String dexingMergingJarDexFiles = null;
			if ( !getValidDependencyLibs().isEmpty() ){
				dexingMergingJarDexFiles = dexingMergingJarDexFiles();
			}

			//dexing merging class文件
			String dexingMergingClassFiles = dexingMergingClassFiles();
			//保证主dex优先添加
			classesDexZipList.add(0, dexingMergingClassFiles);

			//添加依赖库dexing缓存
			if ( dexingMergingJarDexFiles != null ){
				classesDexZipList.add(dexingMergingJarDexFiles);
			}

			/* desugar_libs:{
			 for ( String desugar_libs : getValidDependencyLibs() ){
			 if ( desugar_libs.contains("/com/android/tools/desugar_jdk_libs/")
			 || desugar_libs.contains("/com/android/tools/desugar_jdk_libs_configuration/") ){
			 classesDexZipList.add(getJarDexCachePath(desugar_libs));
			 }
			 }
			 } */

			//混淆(false, classesDexZipList);
			return classesDexZipList;
		}
		/**
		 * 检查线程中断信号
		 */
		private void checkInterrupted() throws InterruptedException{
			if ( Thread.interrupted() ){
				throw new InterruptedException();
			}
		}
		/**
		 * dexing And merging Jar Class Files
		 */
		private String dexingMergingJarDexFiles() throws InterruptedException, IOException{

			//缓存文件不存在使用全量编译
			showProgress("Dexing - Libraries", 62);

			List<String> dependencyLibDexs = new ArrayList<>();

			String dependencyLibDexZipFilePath = getDependencyMergerFilePath();

			//缓存目录都不存在
			boolean existsCacheDir = new File(dependencyLibDexZipFilePath).exists();

			//dexing 没有Jardex缓存的依赖
			for ( String dependencyLibPath : getValidDependencyLibs() ){
				checkInterrupted();

				File jarFile = new File(dependencyLibPath);
				String dexCachePath = getJarDexCachePath(dependencyLibPath);
				File dexCacheFile = new File(dexCachePath);

				//根据时间戳判断是否需要dexing
				if ( isBuildRefresh() || ! existsCacheDir
					|| !dexCacheFile.exists() 
					|| jarFile.lastModified() > dexCacheFile.lastModified() ){
					dexingDependencyLibFile(dependencyLibPath);
				}

				/* desugar_libs:{
				 if ( dependencyLibPath.contains("/com/android/tools/desugar_jdk_libs/")
				 || dependencyLibPath.contains("/com/android/tools/desugar_jdk_libs_configuration/") ){
				 continue;
				 }
				 } */
				dependencyLibDexs.add(dexCachePath);
			}

			//合并
			//此缓存察觉不到依赖数量变化

			boolean needMerg = isMergingJarDexFiles(dependencyLibDexs);


			if ( !needMerg ){
				Log.d(TAG, "缓存文件没有更新，不需要合并");
				return dependencyLibDexZipFilePath;
			}

			checkInterrupted();
			showProgress("Merging - Libraries", 65);
			List<String> argsList  = new ArrayList<String>();
			String user_androidjar = null;
			fillD8Args(argsList, getMinSdk(), false, true, user_androidjar, null, dependencyLibDexZipFilePath);

			//输入dexs
			argsList.addAll(dependencyLibDexs);
			com.android.tools.r8.D8.main(argsList.toArray(new String[argsList.size()]));
			logDebug("合并依赖库，已输出: " + dependencyLibDexZipFilePath);

			return dependencyLibDexZipFilePath;
		}

		private boolean isMergingJarDexFiles(List<String> inputLibDexs){
			File dependencyMergerFile = new File(getDependencyMergerFilePath());
			long lastModified = dependencyMergerFile.lastModified();


			boolean isMergingJarDexFiles = false;

			//对比时间戳
			for ( String dependencyLibDex : inputLibDexs ){
				if ( new File(dependencyLibDex).lastModified() > lastModified ){
					isMergingJarDexFiles = true;
					break;
				}
			}

			//查看输入文件集合是否一致
			Set<String> inputJarFilesSet = new HashSet<String>(inputLibDexs);
			File inputInfoFile = new File(getDependencyMergerFilePath() + "_inputInfo.txt");

			if ( !isMergingJarDexFiles ){

				if ( !inputInfoFile.exists() ){
					isMergingJarDexFiles = true;
				}
				else{
					Set<String> lastInputJarFilesSet = new HashSet<>();
					BufferedReader br = null;
					try{
						br = new BufferedReader(new InputStreamReader(new FileInputStream(inputInfoFile)));
						String line;
						while ( (line = br.readLine()) != null ){
							lastInputJarFilesSet.add(line);
						}
					}
					catch (IOException e){

					}
					finally{
						if ( br != null ){
							try{
								br.close();
							}
							catch (IOException e){}
						}
					}
					//比较
					isMergingJarDexFiles = inputJarFilesSet.size() != lastInputJarFilesSet.size()
						|| !inputJarFilesSet.containsAll(lastInputJarFilesSet);			
				}
			}

			if ( isMergingJarDexFiles ){
				//写入inputJarFilesSet
				FileOutputStream output =null;
				try{ 
					output = new FileOutputStream(inputInfoFile);
					for ( String input : inputJarFilesSet ){
						output.write(input.getBytes());
						output.write('\n');
					}
					output.close();
				}
				catch (IOException e){}
				finally{
					if ( output != null ){
						try{
							output.close();
						}
						catch (IOException e){}
					}
				}
			}
			return isMergingJarDexFiles;
		}


		/**
		 * dexing库
		 */
		private String dexingDependencyLibFile(String dependencyLibPath) throws IOException{
			//out
			String dexCachePath = getJarDexCachePath(dependencyLibPath);
			File dexCacheFile = new File(dexCachePath);


			File dexZipTempFile = File.createTempFile(dexCacheFile.getName(), ".dex.zip", dexCacheFile.getParentFile());
			//更新时间
			dexZipTempFile.setLastModified(System.currentTimeMillis());

			List<String> argsList = new ArrayList<>();

			String user_androidjar = getUserAndroidJar();
			fillD8Args(argsList, getMinSdk(), false, false, user_androidjar, getValidDependencyLibs(), dexZipTempFile.getAbsolutePath());
			//添加需要编译的jar
			argsList.add(dependencyLibPath);
			try{
				//dexing jar
				com.android.tools.r8.D8.main(argsList.toArray(new String[argsList.size()]));

				//临时文件移动到实际输出文件
				dexZipTempFile.renameTo(dexCacheFile);
				//删除
				dexZipTempFile.delete();

				//更新文件修改时间
				dexCacheFile.setLastModified(System.currentTimeMillis() + 10);
			}
			finally{
				//临时文件用完时删除
				if ( dexZipTempFile.exists() ){
					logDebug(dexZipTempFile + "删除: " + dexZipTempFile.delete());						
				}
			}
			return dexCachePath;
		}

		/**
		 * dexing And merging Class Files
		 */
		private String dexingMergingClassFiles() throws InterruptedException{
			checkInterrupted();

			List<String> incrementalClassFiles = new ArrayList<>();
			Set<String> classFileMap = new HashSet<>();

			//填充class文件
			String mainProjectClassCacheDirPath = getMainClassCacheDir();
			fillClassFileCache(mainProjectClassCacheDirPath, incrementalClassFiles, classFileMap);

			//遍历添加所有项目的class缓存目录
			for ( String classFileRootDir : getAllClassFileRootDirs() ){
				checkInterrupted();
				if ( classFileRootDir != null 
					|| !classFileRootDir.equals(mainProjectClassCacheDirPath) ){

					fillClassFileCache(classFileRootDir, incrementalClassFiles, classFileMap);
				}
			}

			String mainClassesDexZipFilePath = getMainClassesDexZipFilePath();
			//增量为0，不dexing
			if ( incrementalClassFiles.isEmpty() ){
				return mainClassesDexZipFilePath;
			}
			//logDebug("待dexing类文件数量: " + incrementalClassFiles.size());

			showProgress("Dexing - Classes", 67);
			//dexing classFile[增量]
			dexingClassFilesFromD8(getDefaultClassDexCacheDirPath(), incrementalClassFiles);

			showProgress("Merging - Classes", 69);
			List<String> classeDexFiles = FileUtil.Files2Strings(FileUtil.findFile(new File(getDefaultClassDexCacheDirPath()), ".dex"));
			mergingClassDexs(mainClassesDexZipFilePath, classeDexFiles);				

			return mainClassesDexZipFilePath;
		}



		public void fillD8Args(List<String> argsList, int minSdk, boolean file_per_class_file, boolean intermediate, String user_androidjar, List<String> dependencyLibs, String outPath){
			argsList.add("--min-api");
			//待跟随minSDK
			argsList.add(String.valueOf(minSdk));

			if ( file_per_class_file ){
				argsList.add("--file-per-class-file");
			}
			if ( intermediate ){
				argsList.add("--intermediate");
			}
			if ( !TextUtils.isEmpty(user_androidjar) ){
				argsList.add("--lib");
				argsList.add(user_androidjar);
			}

			if ( dependencyLibs != null ){
				for ( String librarie : dependencyLibs ){
					argsList.add("--classpath");
					argsList.add(librarie);
				}
			}

			/* desugar_libs: {
			 argsList.add("--desugared-lib");
			 argsList.add("/storage/emulated/0/.MyAicy/.aide/maven/com/android/tools/desugar_jdk_libs_configuration/2.0.4/META-INF/desugar/d8/desugar.json");
			 } */

			argsList.add("--output");
			argsList.add(outPath);
		}

		/**
		 * 是否仅编译，接受小写
		 */
		private boolean isCompileOnly(String libFileNameLowerCase){
			return libFileNameLowerCase.endsWith("_compileonly.jar");
		}
		/**
		 * 是否仅打包，接受小写
		 */
		private boolean isRuntimeOnly(String libFileNameLowerCase){
			return libFileNameLowerCase.endsWith("_resource.jar");
		}
		/**
		 * 存在的依赖，但不包括_resource.jar
		 */
		public List<String> getCompileDependencyLibs(){
			List<String> existsDependencyLibs = new ArrayList<>();
			existsDependencyLibs.addAll(getValidDependencyLibs());
			existsDependencyLibs.addAll(compileOnlyLibs);

			return existsDependencyLibs;
		}
		// dexing所有class
		private void dexingClassFilesFromD8(String outPath, List<String> dexingClassFiles){
			List<String> argsList = new ArrayList<>();
			//检查输出目录
			File outDir = new File(outPath);
			if ( !outDir.exists() ){
				outDir.mkdirs();
			}
			String user_androidjar = getUserAndroidJar();
			int minSdk = getMinSdk();
			if ( minSdk < 21 ){
				//minSdk＜21，无法编译生成的class
				minSdk = 21;
			}
			fillD8Args(argsList, minSdk, true, true, user_androidjar, getCompileDependencyLibs(), outPath);
			//添加需要编译的jar
			argsList.addAll(dexingClassFiles);

			com.android.tools.r8.D8.main(argsList.toArray(new String[argsList.size()]));
		}

		//合并AIDE生成的class.dex
		private void mergingClassDexs(String outDexZipPath, Collection<String> classeDexFiles){
			File outDexZipFile = new File(outDexZipPath);
			//删除缓存文件
			outDexZipFile.delete();

			List<String> argsList  = new ArrayList<String>();
			String user_androidjar = null; 
			fillD8Args(argsList, getMinSdk(), false, false, user_androidjar, null, outDexZipPath);

			//输入dexs
			argsList.addAll(classeDexFiles);
			com.android.tools.r8.D8.main(argsList.toArray(new String[argsList.size()]));
			//logDebug("合并classes.dex，已输出: " + outDexZipPath);
		}

		/**
		 * 根据isBuildRefresh是否增量
		 */
		public void fillClassFileCache(String classCacheRootDirPath, List<String> incrementalClassFiles, Set<String> classFileSet){
			fillClassFileCacheMap(classCacheRootDirPath, new File(classCacheRootDirPath), incrementalClassFiles, classFileSet, this.isBuildRefresh());
		}
		/**
		 * 默认不增量
		 */
		public void fillAllClassFileCache(String classCacheRootDirPath, List<String> incrementalClassFiles, Set<String> classFileSet){
			fillClassFileCacheMap(classCacheRootDirPath, new File(classCacheRootDirPath), incrementalClassFiles, classFileSet, true);
		}

		/**
		 * 递归填充classCacheRootDirPath目录下的所有class文件
		 */
		public void fillClassFileCacheMap(String classCacheRootDirPath, File dirFile, List<String> incrementalClassFiles, Set<String> classFileSet, boolean isBuildRefresh){
			File[] listFiles = dirFile.listFiles();
			if ( listFiles == null ){
				return;
			}

			for ( File classFile : listFiles ){
				if ( classFile.isDirectory() ){
					fillClassFileCacheMap(classCacheRootDirPath, classFile, incrementalClassFiles, classFileSet, isBuildRefresh);
					continue;
				}

				if ( classFile.isFile() ){
					String classFilePath = classFile.getPath();
					if ( !getFileName(classFilePath).toLowerCase().endsWith(".class") ){
						continue;
					}

					String classFileSubPath = classFilePath.substring(classCacheRootDirPath.length());
					if ( classFileSet.contains(classFileSubPath) ){
						//AppLog.DW("忽略重复 .class 文件 " + classFilePath);
						continue;
					}
					classFileSet.add(classFileSubPath);

					if ( isBuildRefresh ){
						//构建刷新，直接添加
						incrementalClassFiles.add(classFilePath);
						continue;
					}

					//判断是否更新
					String classDexFileCache = getClassDexFileCache(classFileSubPath);

					File dexFile = new File(classDexFileCache);
					if ( classFile.lastModified() > dexFile.lastModified() ){
						//需要重新dexing的class
						incrementalClassFiles.add(classFilePath);
					}
				}
			}
		}
		/**
		 *有效依赖为[非(compile | runtime) only，存在且是jar]
		 */
		private List<String> validLibs = new ArrayList<>();
		/**
		 * compileOnly
		 */
		private List<String> compileOnlyLibs  = new ArrayList<>();

		/**
		 * runtimeOnly
		 */
		private List<String> runtimeOnlyLibs = new ArrayList<>();

		/**
		 * aar混淆文件
		 */
		private List<Path> proguardPaths  = new ArrayList<>();

		private void initBuildEnvironment(){
			if ( isBuildRefresh() ){
				File defaultJarDexDir = new File(getDefaultJarDexDirPath());
				File defaultClassDexCacheDir = new File(getDefaultClassDexCacheDirPath());
				File mergerCacheDir = new File(getMergerCacheDirPath());
				
				FileUtil.deleteFolder(defaultJarDexDir);
				FileUtil.deleteFolder(defaultClassDexCacheDir);
				FileUtil.deleteFolder(mergerCacheDir);
				
				defaultJarDexDir.mkdirs();
				defaultClassDexCacheDir.mkdirs();
				mergerCacheDir.mkdirs();
			}

			for ( String dependencyLib : getAllDependencyLibs() ){
				File jarFile = new File(dependencyLib);

				if ( !jarFile.exists() ){
					//不是依赖库跳过
					continue;
				}

				String fileName = getFileName(dependencyLib).toLowerCase();
				if ( !fileName.endsWith(".jar") ){
					continue;
				}
				if ( isRuntimeOnly(fileName) ){
					runtimeOnlyLibs.add(dependencyLib);
					continue;
				}

				if ( isCompileOnly(fileName) ){
					compileOnlyLibs.add(dependencyLib);
					continue;
				}

				if ( isMinify() && fileName.equals("classes.jar") ){
					File proguardFile = new File(jarFile.getParentFile(), "proguard.txt");
					if ( proguardFile.isFile() ){
						proguardPaths.add(proguardFile.toPath());
					}					
				}


				try{
					//嗅探一下，d8打不开zip，不报路径😭
					new ZipFile(jarFile);
				}
				catch (IOException e){
					throw new Error(dependencyLib + "不是一个zip文件");
				}

				validLibs.add(dependencyLib);
			}

			/* desugar_libs:{
			 validLibs.add("/storage/emulated/0/.MyAicy/.aide/maven/com/android/tools/desugar_jdk_libs/2.0.4/desugar_jdk_libs-2.0.4.jar");
			 validLibs.add("/storage/emulated/0/.MyAicy/.aide/maven/com/android/tools/desugar_jdk_libs_configuration/2.0.4/desugar_jdk_libs_configuration-2.0.4.jar");
			 } */

		}

		public List<String> getValidDependencyLibs(){
			return validLibs;
		}

		// dex.zip转换器，即根目录下有classes%d.dex的zip文件的转换器
		ZipEntryTransformer.DexZipTransformer dexZipEntryTransformer = new ZipEntryTransformer.DexZipTransformer();
		// 从jar依赖添加资源的过滤器，
		ZipEntryTransformer.ZipResourceTransformer zipResourceZipEntryTransformer = new ZipEntryTransformer.ZipResourceTransformer();

		public void minify() throws Exception{

			// 混淆class 
			// 查找混淆文件
			File aaptRulesFile = new File(getDefaultIntermediatesDirPath(), "aapt_rules.txt");
			if ( aaptRulesFile.isFile() ){
				proguardPaths.add(aaptRulesFile.toPath());			
			}
			String buildOutDirPath = getBuildOutDirPath();
			if ( buildOutDirPath.endsWith("/build/bin") ){
				String projectPath = buildOutDirPath.substring(0, buildOutDirPath.length() - "/build/bin".length());
				File proguardPulesProFile = new File(projectPath, "proguard-rules.pro");
				if ( proguardPulesProFile.isFile() ){
					//主项目混淆混淆，子项目的就先不加了
					proguardPaths.add(proguardPulesProFile.toPath());
				}
			}

			// 合并所有依赖除runtimeOnly外
			// compileOnly是库
			// runtimeOnly不参与r8，最后加入apk

			// 编译依赖库
			showProgress("合并混淆class文件", 60);
			logDebug("合并待混淆类库");

			//合并的目的是去重
			minifyClassJarFiles();

			List<String> mainClassFilePaths = method();

			//所有的dex
			File minifyClassJarFile = new File(getIntermediatesChildDirPath("r8"), "minifyClass.jar");
			File mixUpDexZipFile = getMixUpDexZipFile(true);

			Path androidJarPath = new File(getUserAndroidJar()).toPath();
			int minSdk = getMinSdk();

			List<Path> compileOnlyPaths = new ArrayList<>();

			for ( String jarPath : this.compileOnlyLibs ){
				compileOnlyPaths.add(new File(jarPath).toPath());
			}

			BaseDiagnosticsHandler baseDiagnosticsHandler = new BaseDiagnosticsHandler();
			R8Command.Builder builder = R8Command.builder(baseDiagnosticsHandler);

			logDebug("开始添加AIDE输出的类");
			for ( String inputClassPath : mainClassFilePaths ){

				DataInputStream dataInputStream = new DataInputStream(new FileInputStream(inputClassPath));
				byte[] readAllBytes = dataInputStream.readAllBytes();
				dataInputStream.close();
				builder.addClassProgramData(readAllBytes, Origin.root());
			}
			logDebug("开始配置");

			R8Command r8Command = builder
				// 所有类
				.addProgramFiles(minifyClassJarFile.toPath())
				.addProguardConfigurationFiles(proguardPaths)
				//主要是为了兼容AIDE的输出类
				.setMinApiLevel(minSdk < 21 ? 21 : minSdk)
				// Android SDK
				.addLibraryFiles(androidJarPath)
				// 依赖库
				.addClasspathFiles(compileOnlyPaths)
				// 输出
				.setOutput(mixUpDexZipFile.toPath(), OutputMode.DexIndexed)
				.setProguardMapOutputPath(new File(getIntermediatesChildDirPath("r8"), "proguardMap.txt").toPath())
				.build();

			showProgress("混淆class中", 65);

			//运行r8
			R8.run(r8Command);

			if ( baseDiagnosticsHandler.hasError() ){
				throw new RuntimeException(baseDiagnosticsHandler.getErrorMessage());
			}
		}

		private List<String> method() throws InterruptedException{
			List<String> allClassFiles = new ArrayList<>();
			//类名
			Set<String> classFileSet = new HashSet<>();
			//填充class文件
			String mainProjectClassCacheDirPath = getMainClassCacheDir();
			fillAllClassFileCache(mainProjectClassCacheDirPath, allClassFiles, classFileSet);
			//遍历添加所有项目的class缓存目录
			for ( String classFileRootDir : getAllClassFileRootDirs() ){
				checkInterrupted();
				if ( classFileRootDir != null 
					|| !classFileRootDir.equals(mainProjectClassCacheDirPath) ){
					fillAllClassFileCache(classFileRootDir, allClassFiles, classFileSet);
				}
			}
			//List<Path> classFilePaths = new ArrayList<>();

			return allClassFiles;
		}


		private void minifyClassJarFiles() throws Exception{
			// 过滤重复文件
			Set<String> zipEntrySet = new HashSet<>();
			zipEntrySet.add("META-INF/MANIFEST.MF");
			//输出zip流
			File minifyClassJarFile = new File(getIntermediatesChildDirPath("r8"), "minifyClass.jar");
			//后面在实现增量信息，保存输入文件信息
			//new File(getIntermediatesChildDirPath("r8"), "minifyClass.jar");

			ZipOutputStream minifyClassJarOutput = null;

			try{
				minifyClassJarOutput = new ZipOutputStream(new FileOutputStream(minifyClassJarFile));

				for ( String inputClassesJar : getValidDependencyLibs() ){

					File inputClassesJarFile = new File(inputClassesJar);
					ZipInputStream zipFileInput = null;
					try{
						zipFileInput = new ZipInputStream(new FileInputStream(inputClassesJarFile));

						ZipEntry originalZipEntry;
						while ( (originalZipEntry = zipFileInput.getNextEntry()) != null ){
							String name = originalZipEntry.getName();

							if ( zipEntrySet.contains(name) ){
								continue;
							}
							if ( originalZipEntry.isDirectory() ){
								//文件夹不用加
								continue;
							}
							ZipEntry newZipEntry = new ZipEntry(name);
							int method = originalZipEntry.getMethod();
							if ( method == ZipEntry.STORED ){
								newZipEntry.setMethod(method) ;
								newZipEntry.setCrc(originalZipEntry.getCrc());
								newZipEntry.setSize(originalZipEntry.getSize());								
							}

							zipEntrySet.add(name);
							minifyClassJarOutput.putNextEntry(newZipEntry);
							streamTransfer(zipFileInput, minifyClassJarOutput);
							//Entry写入完成
							minifyClassJarOutput.closeEntry();
						}
					}
					finally{
						if ( zipFileInput != null ) 
							zipFileInput.close();
					}
				}
			}
			finally{
				if ( minifyClassJarOutput != null ) 
					minifyClassJarOutput.close();
			}

		}



		public void shrinkResources(){
			//压缩资源 不会
		}


		private boolean isMinify(){
			return ZeroAicySetting.isEnableMinify();
		}

		private void packagingAndroidMinify() throws Throwable{
			if ( !false ) System.out.println("packagingAndroidMinify2+19");
			//混淆
			minify();

			showProgress("构建APK", 80);			
			//未zip优化，未签名
			File unZipAlignedUnSignedApkFile = getUnZipAlignedUnSignedApkFile(true);
			PackagingStream packagingZipOutput = new PackagingStream(new FileOutputStream(unZipAlignedUnSignedApkFile));

			logDebug("从aapt2生成文件添加资源");
			//resources_ap_file
			String aAptResourceFilePath = getAAptResourceFilePath();
			//打包resources.ap_ 文件
			packagingZipFile(aAptResourceFilePath, zipResourceZipEntryTransformer, packagingZipOutput, true);

			// 从文件夹添加原生库文件，
			ZipEntryTransformer.NativeLibFileTransformer nativeLibZipEntryTransformer = new ZipEntryTransformer.NativeLibFileTransformer(getAndroidFxtractNativeLibs());
			//从原生库目录添加so
			for ( String nativeLibDirPath : this.getNativeLibDirs() ){
				File nativeLibDirFile = new File(nativeLibDirPath);
				if ( nativeLibDirFile.exists() ){
					logDebug("从原生库添加" + nativeLibDirPath);
					packagingDirFile(nativeLibDirPath, nativeLibDirFile, nativeLibZipEntryTransformer, packagingZipOutput);
				}
			}
			//打包混淆后的dex
			packagingZipFile(getMixUpDexZipFile(false).getAbsolutePath(), dexZipEntryTransformer, packagingZipOutput, false);

			//打包源码目录下的资源
			packagingSourceDirsResource(packagingZipOutput);			
			//打包jar资源，优先第一个
			packagingJarResources(packagingZipOutput);
			//打包完成
			packagingZipOutput.close();

			//优化apk
			zipalignApk();
		}



		/**
		 * 优化对齐apk
		 */
		private void zipalignApk() throws Exception, Throwable{
			showProgress("ZeroAicy Zipalign APK ", 85);
			//优化前，未签名
			File unZipAlignedUnSignedApkFile = getUnZipAlignedUnSignedApkFile(false);
			//优化后，未签名
			File unSignedApkFile = getUnSignedApkFile(true);

			// zipalign命令路径
			String zipalignPath = getZipalignPath();
			//填充参数
			List<String> args = new ArrayList<>();
			args.add(zipalignPath);
			args.add("-p");
			args.add("-v");
			args.add("4");
			//输入
			args.add(unZipAlignedUnSignedApkFile.getAbsolutePath());
			//输出
			args.add(unSignedApkFile.getAbsolutePath());

			abcd.wf j62 = abcd.xf.j6(args, null, null, true, null, null);
			if ( j62.DW() != 0 ){
				throw new Exception(" zipalign Error: " + new String(j62.j6()));
			}
			//删除缓存
			unZipAlignedUnSignedApkFile.delete();

			//签名
			logDebug("开始Signing APK: ");
			long now = nowTime();
			//-zipaligned-unsigned 
			proxySign(unSignedApkFile, new File(getOutFilePath()));

			logDebug("Signing APK共用时: " + (nowTime() - now) + "ms");

		}

		public void proxySign(File unsignedApk, File signedApk) throws Throwable{

			showProgress("ZeroAicy Signing APK ", 90);
			if ( signedApk.exists() ){
				signedApk.delete();
			}
			String keystorePath = getSignaturePath();

			PrivateKey privateKey;
			X509Certificate certificate;
			//自定义签名文件是存在
			if ( new File(keystorePath).exists() ){
				//支持 .pk8 与 .x509.pem签名文件
				if ( keystorePath.endsWith(".x509.pem") 
					|| keystorePath.endsWith(".pk8") ){
					String keyNamePrefix;
					if ( keystorePath.endsWith(".x509.pem") ){
						keyNamePrefix = keystorePath.substring(0, keystorePath.length() - 9);
					}
					else{
						keyNamePrefix = keystorePath.substring(0, keystorePath.length() - 4);
					}

					InputStream cert = new FileInputStream(new File(keyNamePrefix + ".x509.pem"));
					certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(cert);
					cert.close();

					InputStream key = new FileInputStream(new File(keyNamePrefix + ".pk8"));
					PKCS8Key pkcs8 = new PKCS8Key();
					pkcs8.decode(key);
					privateKey = pkcs8;
					cert.close();
				}
				else{
					//支持AIDE创建的.keystore签名文件
					//然而有兼容性问题，不支持其它工具生成的
					String password = getSignaturePassword();
					String alias = getSignatureAlias();
					String keyPass = getSignatureAliasPassword();

					FileInputStream keystoreIs = new FileInputStream(keystorePath);

					KeyStore ks = new com.aide.ui.build.android.JKSKeyStore();
					ks.load(keystoreIs, password.toCharArray());

					privateKey = (PrivateKey) ks.getKey(alias, keyPass.toCharArray());
					certificate = (X509Certificate) ks.getCertificateChain(alias)[0];

					keystoreIs.close();
				}
			}
			else{
				//为设置签名文件使用内置签名文件
				String keyName = "testkey";
				Class clazz = getClass();
				InputStream certInputStream = clazz.getResourceAsStream("/keys/" + keyName + ".x509.pem");
				certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(certInputStream);
				certInputStream.close();

				InputStream pk8InputStream = clazz.getResourceAsStream("/keys/" + keyName + ".pk8");
				PKCS8Key pkcs8 = new PKCS8Key();
				pkcs8.decode(pk8InputStream);
				privateKey = pkcs8;
				certInputStream.close();
			}
			//签名
			signApk(privateKey, certificate, unsignedApk, signedApk);

			//删除输入文件
			unsignedApk.delete();
		}

		private void signApk(PrivateKey privateKey, X509Certificate certificate, File unsignedApk, File signedApk) throws Throwable{


			SharedPreferences defaultSp = ZeroAicySetting.getDefaultSp();
			boolean isapksignv1 = defaultSp.getBoolean("v1", true);
			boolean isapksignv2 = defaultSp.getBoolean("v2", true);
			boolean isapksignv3 = defaultSp.getBoolean("v3", true);

			ApkSigner.SignerConfig signerConfig = 
				new ApkSigner.SignerConfig.Builder("ANDROID",  privateKey, Collections.singletonList(certificate))
				.build();

			ApkSigner.Builder builder = new ApkSigner.Builder(Collections.singletonList(signerConfig));
			builder.setCreatedBy("Android Gradle 8.4")
				.setMinSdkVersion(getMinSdk())
				.setInputApk(unsignedApk)
				.setOutputApk(signedApk)
				.setV1SigningEnabled(isapksignv1)
				.setV2SigningEnabled(isapksignv2)
				.setV3SigningEnabled(isapksignv3)
				.build()
				.sign();
		}


		/**
		 * 打包Java项目即zip
		 */
		@Override
		public void packagingJavaProject(List<String> dexZipPathList) throws Throwable{
			showProgress("packaging - JavaProject", 73);

			File outFile = new File(getOutFilePath());
			File outDirFile = outFile.getParentFile();
			if ( !outDirFile.exists() ){
				outDirFile.mkdirs();
			}
			//构建输出文件
			PackagingStream packagingZipOutput = new PackagingStream(new FileOutputStream(outFile));
			//打包dex
			packagingDexs(dexZipPathList, packagingZipOutput);
			packagingSourceDirsResource(packagingZipOutput);
			//打包依赖库资源
			packagingJarResources(packagingZipOutput);
			packagingZipOutput.close();
		}

		private void packagingDexs(List<String> dexZipPathList, PackagingStream packagingZipOutput) throws IOException{
			logDebug("添加classes.dex");
			for ( String dexZipPath : dexZipPathList ){
				packagingZipFile(dexZipPath, dexZipEntryTransformer, packagingZipOutput, false);
			}
		}
		/**
		 * 打包安卓即apk
		 */
		@Override
		public void packagingAndroidProject(List<String> dexZipPathList) throws Throwable{

			showProgress("构建APK", 80);
			//未zip优化，未签名
			File unZipAlignedUnSignedApkFile = getUnZipAlignedUnSignedApkFile(true);

			PackagingStream packagingZipOutput = new PackagingStream(new FileOutputStream(unZipAlignedUnSignedApkFile));
			//resources_ap_file
			String aAptResourceFilePath = getAAptResourceFilePath();
			logDebug("Adding aapt generated resources from " + aAptResourceFilePath);
			//打包resources.ap_ 文件
			packagingZipFile(aAptResourceFilePath, zipResourceZipEntryTransformer, packagingZipOutput, true);

			// 从文件夹添加原生库文件，
			ZipEntryTransformer.NativeLibFileTransformer nativeLibZipEntryTransformer = new ZipEntryTransformer.NativeLibFileTransformer(getAndroidFxtractNativeLibs());
			//从原生库目录添加so
			logDebug("添加原生库");
			for ( String nativeLibDirPath : this.getNativeLibDirs() ){
				File nativeLibDirFile = new File(nativeLibDirPath);
				if ( nativeLibDirFile.exists() ){
					packagingDirFile(nativeLibDirPath, nativeLibDirFile, nativeLibZipEntryTransformer, packagingZipOutput);
				}
			}
			//打包dex
			packagingDexs(dexZipPathList, packagingZipOutput);
			//打包源码目录下的资源
			packagingSourceDirsResource(packagingZipOutput);			
			//打包jar资源，优先第一个
			packagingJarResources(packagingZipOutput);
			//打包完成
			packagingZipOutput.close();

			//优化apk
			zipalignApk();
		}

		private void packagingSourceDirsResource(PackagingStream packagingZipOutput) throws IOException{
			logDebug("从源码目录添加资源");
			//从源码目录添加
			String[] sourceDirs = getSourceDirs();
			if ( sourceDirs == null ){
				return;
			}
			for ( String sourceDir : sourceDirs ){
				File sourceDirFile = new File(sourceDir);
				if ( !sourceDirFile.exists() ){
					continue;
				}
				packagingDirFile(sourceDir, new File(sourceDir), zipResourceZipEntryTransformer, packagingZipOutput);
			}
		}

		private void packagingJarResources(PackagingStream packagingZipOutput) throws IOException{
			String[] dependencyLibs = getAllDependencyLibs();
			if ( dependencyLibs == null ){
				return;
			}
			logDebug("从JAR文件添加资源");

			for ( String dependencyLibPath : dependencyLibs ){
				String dependencyLibLowerCase = dependencyLibPath.toLowerCase();

				if ( isCompileOnly(dependencyLibLowerCase) ){
					//仅编译文件不打包
					continue;
				}
				if ( isRuntimeOnly(dependencyLibLowerCase) ){
					this.packagingZipFile(dependencyLibPath, dexZipEntryTransformer, packagingZipOutput, false);
					continue;
				}

				this.packagingZipFile(dependencyLibPath, zipResourceZipEntryTransformer, packagingZipOutput, false);
			}

		}

		/**
		 * 返回未优化,未签名时的apk输出文件，并清除已有缓存
		 */
		private File getUnZipAlignedUnSignedApkFile(boolean delete){
			File unZipAlignedUnSignedApkFile = new File(getOutFilePath() + "-unzipaligned-unsigned");
			if ( delete && unZipAlignedUnSignedApkFile.exists() ){
				unZipAlignedUnSignedApkFile.delete();
				//直接返回，文件存在所以父目录也存在
				return unZipAlignedUnSignedApkFile;
			}

			File parentFile = unZipAlignedUnSignedApkFile.getParentFile();
			if ( !parentFile.exists() ){
				parentFile.mkdirs();
			}
			return unZipAlignedUnSignedApkFile;
		}
		/**
		 * 返回未未签名apk的文件，并清除已有缓存
		 */
		private File getUnSignedApkFile(boolean delete){
			File unSignedApkFile = new File(getOutFilePath() + "-unsigned");
			File parentFile = unSignedApkFile.getParentFile();
			//删除输出
			if ( delete && unSignedApkFile.exists() ){
				unSignedApkFile.delete();
				return unSignedApkFile;
			}

			if ( !parentFile.exists() ){
				parentFile.mkdirs();
			}
			return unSignedApkFile;
		}



		public void packagingDirFile(String relativeRootDirFilePath, File file, ZipEntryTransformer transformer, PackagingStream packagingZipOutput) throws FileNotFoundException, IOException{
			if ( file.isDirectory() ){
				if ( file.isHidden() ){
					return;
				}
				for ( File childFile : file.listFiles() ){
					if ( childFile.isDirectory() && childFile.isHidden() ){
						return;
					}
					packagingDirFile(relativeRootDirFilePath, childFile, transformer, packagingZipOutput);
				}

			}
			else{
				String zipEntryName = getZipEntryName(file, relativeRootDirFilePath);
				ZipEntry zipEntry = new ZipEntry(zipEntryName);
				if ( transformer != null &&
					(zipEntry = transformer.transformer(zipEntry, packagingZipOutput)) == null ){
					//已被转换器过滤
					return;
				}

				if ( zipEntry.getMethod() == ZipEntry.STORED ){
					//未压缩时设置未压缩条目数据的CRC-32校验和
					zipEntry.setCrc(getFileCRC32(file));
				}

				zipEntry.setSize(file.length());
				zipEntry.setTime(file.lastModified());
				//添加zip条目
				packagingZipOutput.putNextEntry(zipEntry);

				BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
				streamTransfer(inputStream, packagingZipOutput);
				inputStream.close();

				packagingZipOutput.closeEntry();
			}

		}

		public long getFileCRC32(File file) throws IOException{
			CRC32 crc = new CRC32();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
			byte[] data = new byte[4096];
			int count;
			while ( (count = bufferedInputStream.read(data)) > 0 ){
				crc.update(data, 0, count);
			}
			bufferedInputStream.close();
			return crc.getValue();
		}


		private String getZipEntryName(File file, String relativeRootDirFilePath){
			String filePath = file.getAbsolutePath();
			int index = relativeRootDirFilePath.length();
			int length =filePath.length();
			while ( index < length 
				   && filePath.charAt(index) == '/' ){
				index ++;
			}
			return filePath.substring(index);
		}



		/**
		 * 不能添加class文件
		 * 但是 
		 */
		private void packagingZipFile(String zipFilePath, ZipEntryTransformer transformer, PackagingStream packagingZipOutput, boolean followZipEntryMethod) throws IOException{
			if ( !new File(zipFilePath).exists() ){
				AppLog.gn("Zip file not found: " + zipFilePath);
				return;
			}
			ZipInputStream zipFileInput = null;
			try{
				zipFileInput = new ZipInputStream(new FileInputStream(zipFilePath));
				ZipEntry originalZipEntry;
				while ( (originalZipEntry = zipFileInput.getNextEntry()) != null ){
					ZipEntry newZipEntry;
					if ( transformer != null ){
						newZipEntry = transformer.transformer(originalZipEntry, packagingZipOutput);
						//转换器过滤此条目
						if ( newZipEntry == null ){
							continue;
						}
					}
					else{
						newZipEntry = originalZipEntry;
					}

					if ( newZipEntry == originalZipEntry ){
						newZipEntry = new ZipEntry(originalZipEntry.getName());
					}

					if ( followZipEntryMethod 
						&& originalZipEntry.getMethod() != -1 ){
						if ( originalZipEntry.getMethod() == ZipEntry.STORED ){
							newZipEntry.setCrc(originalZipEntry.getCrc());
							newZipEntry.setSize(originalZipEntry.getSize());
						}
						newZipEntry.setMethod(originalZipEntry.getMethod());
					}

					packagingZipOutput.putNextEntry(newZipEntry);
					streamTransfer(zipFileInput, packagingZipOutput);
					//Entry写入完成
					packagingZipOutput.closeEntry();
				}
			}
			finally{
				if ( zipFileInput != null ) zipFileInput.close();
			}
		}

		private void streamTransfer(InputStream bufferedInputStream, OutputStream packagingZipOutput) throws IOException{
			byte[] data = new byte[4096];
			int read;
			while ( (read = bufferedInputStream.read(data)) > 0 ){
				packagingZipOutput.write(data, 0, read);
			}
		}
		private void logDebug(String msg){
			Log.i(TAG, msg);
		}

		private long nowTime(){
			return System.currentTimeMillis();
		}

	}

	public static String getFileName(String path){
		int fileNameStartIndex = path.lastIndexOf('/');
		if ( fileNameStartIndex >= 0 ){
			return path.substring(fileNameStartIndex);
		}
		return path;
	}
}
