package io.github.zeroaicy.aide.aapt2;
import android.text.TextUtils;
import com.aide.ui.build.android.AaptService$b;
import com.aide.ui.util.FileSystem;
import com.sdklite.aapt.Aapt;
import com.sdklite.aapt.SymbolParser;
import com.sdklite.aapt.Symbols;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.aide.utils.AndroidManifestParser;
import io.github.zeroaicy.util.FileUtil;
import io.github.zeroaicy.util.Log;
import io.github.zeroaicy.util.MD5Util;
import io.github.zeroaicy.util.reflect.ReflectPie;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Aapt2TaskFromZeroAicy {
	public static final byte[] emptyZipBytes = new byte[]{0x50, 0x4B, 0x05, 0x06, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

	public static boolean hasError(AaptService$b aaptService$b) {
		return aaptService$b != null && aaptService$b.DW != null;
	}
	private static final String TAG = "aapt2";



	//com.aide.ui.build.android.AaptService.b b;
	private static String aapt$b = "com.aide.ui.build.android.AaptService$b";

	private static void streamTransfer(InputStream bufferedInputStream, OutputStream outputStream) throws IOException {
		byte[] data = new byte[4096];
		int read;
		while ((read = bufferedInputStream.read(data)) > 0) {
			outputStream.write(data, 0, read);
		}
	}

	public static AaptService$b proxyAapt(Object aapt$c) throws Exception {
		long oldTime = System.currentTimeMillis();
		AaptServiceArgs aaptServiceArgs = new AaptServiceArgs(aapt$c);
		AaptService$b proxyAapt = null;
		try {
			proxyAapt = proxyAapt2(aaptServiceArgs);
		}
		catch (Throwable e) {
			e.printStackTrace(aaptServiceArgs.log);
			proxyAapt = new AaptService$b(Log.getStackTraceString(e));
		}

		float diffTime = System.currentTimeMillis() - oldTime;
		aaptServiceArgs.log.println("aapt2 总耗时: " + diffTime / 1000.0f + "s");
		//日志不在使用，关闭流
		aaptServiceArgs.log.close();

		return proxyAapt;
	}

	public static AaptService$b proxyAapt2(AaptServiceArgs aaptServiceArgs) throws Exception {

		PrintStream log = aaptServiceArgs.log;
		//构建刷新
		if (aaptServiceArgs.isBuildRefresh) {
			//构建刷新
			aaptServiceArgs.buildRefresh();
		}

		// 合并清单
		AaptService$b mergedError = aaptServiceArgs.mergedAndroidManifestxml();
		if (mergedError != null && mergedError.DW != null) {
			return mergedError;
		}
		//编译
		Map<String, String> allResourceMap = aaptServiceArgs.allResourceMap;
		// 无序编译
		for (String resDir : allResourceMap.keySet()) {
			AaptService$b aaptError = compile(aaptServiceArgs, resDir);
			if (aaptError != null) {
				return aaptError;
			}
		}
		long currentTimeMillis = System.currentTimeMillis();
		// 增量 -link
		AaptService$b linkError = incrementalLink(aaptServiceArgs);
		aaptServiceArgs.log.println("aapt2 call link " + (System.currentTimeMillis() - currentTimeMillis) + "ms");
		if (linkError != null) {
			return linkError;
		}

		//删除无效缓存
		deleteCache(aaptServiceArgs);

		//需要将主项目R.java复制到主项目gen--以不同的包名
		long genRjavaTimeMillis = System.currentTimeMillis();

		//资源文件
		generateRjava(aaptServiceArgs);

		aaptServiceArgs.log.println("aapt2 生成R耗时: " + (System.currentTimeMillis() - genRjavaTimeMillis) + "ms");

		// ViewBinding
		if (ZeroAicySetting.isEnableViewBinding()) {
			try {
				// viewbinding
				GenerateViewBindingTask.run(aaptServiceArgs.mainProjectResPath, aaptServiceArgs.mainProjectGenDir, aaptServiceArgs.mainPackageName, ZeroAicySetting.isViewBindingAndroidX());
			}
			catch (Throwable e) {
				log.println("ViewBindingTask：");
				e.printStackTrace(log);
				log.println();
			}
		}

		//生成主项目DataBinderMapperImpl
		//生成Binding Java
		aaptServiceArgs.getDataBindingBuilder().generateJava();

		// 生成BuildConfig.java
		aaptServiceArgs.generateBuildConfigJava();

		return new AaptService$b(false);
	}

	private static void generateRjava(AaptServiceArgs aaptServiceArgs) throws IOException {
		//资源文件时间戳
		long resourcesApLastModified = new File(aaptServiceArgs.resourcesApPath).lastModified();

		//主项目gen路径
		String mainProjectGenDir = aaptServiceArgs.mainProjectGenDir;

		SymbolParser symbolParser = new SymbolParser();
		//主项目R.txt Symbols
		Symbols mainSymbols = symbolParser.parse(aaptServiceArgs.buildBin + "/intermediates/R.txt");
		String mainPackageName = aaptServiceArgs.mainPackageName;

		//主项目R.java相对gen路径
		String mainRJavaChildPath = mainPackageName.replace('.', '/') + "/R.java";

		//主项目R.java文件
		File mainRJavaFile = new File(mainProjectGenDir, mainRJavaChildPath);

		//R.java的内容按行储存
		List<String> rJavaLinelist = aaptServiceArgs.listLine(mainRJavaFile);
		// R.java包名所在行
		String packageNameLine = null;
		//R.java包名所在行数
		int packageNameLineCount = -1;

		for (int i = 0; i < rJavaLinelist.size(); i++) {
			String line = rJavaLinelist.get(i);
			if (line.contains(mainPackageName)) {
				packageNameLine = line;
				packageNameLineCount = i;
				break;
			}
		}

		//消除 final
		for (int i = 0; i < rJavaLinelist.size(); i++) {
			rJavaLinelist.set(i, rJavaLinelist.get(i).replace(" final int ", " int "));
		}

		//复制R.java到其它包
		Map<String, String> genPackageNameMap = aaptServiceArgs.genPackageNameMap;
		//遍历所有包名
		for (Map.Entry<String, String> subProjectGen : genPackageNameMap.entrySet()) {
			String subGenDirPath = subProjectGen.getKey();
			if (mainPackageName.equals(subProjectGen.getValue())) {
				// 跳过主项目包名
				continue;
			}
			//子项目包名
			String subPackageName = genPackageNameMap.get(subGenDirPath);
			//子项目R.java相对gen路径
			String subRJavaAbsolutePath = subPackageName.replace('.', '/') + "/R.java";

			String subRtxtPath = getRtxtFile(aaptServiceArgs, subGenDirPath);

			// R怎么只包含自己的资源呢🤔🤔🤔🤔
			// 根据R.txt生成
			if (subRtxtPath == null) {
				//没有R.txt使用主项目的
				//子项目R.java路径
				File subRJavaFile = new File(subProjectGen.getKey(), subRJavaAbsolutePath);
				rJavaLinelist.set(packageNameLineCount, packageNameLine.replace(mainPackageName, subPackageName));

				if (!subRJavaFile.exists() || subRJavaFile.lastModified() < resourcesApLastModified) {
					subRJavaFile.getParentFile().mkdirs();
					AaptServiceArgs.writeLines(subRJavaFile, rJavaLinelist);
				}

				//向主项目gen目录写入
				subRJavaFile = new File(mainProjectGenDir, subRJavaAbsolutePath);
				if (!subRJavaFile.exists() || subRJavaFile.lastModified() < resourcesApLastModified) {
					subRJavaFile.getParentFile().mkdirs();
					AaptServiceArgs.writeLines(subRJavaFile, rJavaLinelist);
				}
				continue;
			}

			Symbols subSymbols = 
				subRtxtPath.length()  == 0 
				? symbolParser.emptySymbols : symbolParser.parse(subRtxtPath);

			//同步
			for (Symbols.Entry subEntry : subSymbols.entries()) {
				Symbols.Entry mainEntry = mainSymbols.getEntry(subEntry.key);
				if (mainEntry != null) {
					subSymbols.put(mainEntry);				
				}
			}
			//向主项目gen目录写入，aar子项目不需要
			File subRJavaFile = new File(mainProjectGenDir, subRJavaAbsolutePath);
			if (!subRJavaFile.exists() || subRJavaFile.lastModified() < resourcesApLastModified) {
				subRJavaFile.getParentFile().mkdirs();
				//跳过此R生成
				Aapt.generateR(subRJavaFile, subPackageName, subSymbols);
			}
		}
	}


	private static AaptService$b incrementalLink(AaptServiceArgs aaptServiceArgs) throws Exception {
		PrintStream log = aaptServiceArgs.log;

		String mainProjectGenDir = aaptServiceArgs.mainProjectGenDir;
		//主项目R.java相对gen路径
		String mainRJavaAbsolutePath = aaptServiceArgs.mainPackageName.replace('.', '/') + "/R.java";
		//主项目R.java文件
		File mainRJavaFile = new File(mainProjectGenDir, mainRJavaAbsolutePath);

		//资源缓存文件路径
		String resourcesApPath = aaptServiceArgs.resourcesApPath;
		//资源文件
		File resourcesApFile = new File(resourcesApPath);
		//资源文件时间戳
		long resourcesApLastModified = resourcesApFile.lastModified();

		String rTxt = aaptServiceArgs.buildBin + "/intermediates/R.txt";

		// skipLink规则 resourcesApFile已存在
		// 主项目R.java存在 R.txt存在
		// flat.zip没有更新
		boolean skipLink = resourcesApFile.exists();
		skipLink &= mainRJavaFile.exists();
		skipLink &= FileSystem.exists(rTxt);

		//主项目依赖的res路径
		List<String> resDirs = aaptServiceArgs.genResDirsMap.get(mainProjectGenDir);

		List<String> flatZipFileList = new ArrayList<>();
		Set<String> flatZipFileSet = aaptServiceArgs.flatZipFileSet;

		//优先添加主项目res的缓存文件
		for (String resDir : resDirs) {
			if (resDir.endsWith("/generated")) {
				continue;
			}
			String flatZipPath = getMergedCacheDirFile(aaptServiceArgs, resDir);

			if (!flatZipFileSet.remove(flatZipPath)) {
				//没有编译
				AaptService$b compileError = compile(aaptServiceArgs, resDir);
				if (compileError != null) {
					return compileError;
				}
			}
			if (FileSystem.exists(flatZipPath)) {
				//按照res依赖顺序添加，从底层到顶层
				flatZipFileList.add(flatZipPath);

				//缓存新于资源文件
				File flatZipFile = new File(flatZipPath);
				skipLink &= flatZipFile.lastModified() < resourcesApLastModified;
			}

		}

		//添加已编译的缓存路径
		flatZipFileList.addAll(flatZipFileSet);
		//反序 aapt2 link -R 末尾优先
		Collections.reverse(flatZipFileList);

		//记录有效缓存
		aaptServiceArgs.flatZipFileSet.addAll(flatZipFileList);

		if (skipLink) {
			log.println("跳过link");
			return null;
		}

		AaptService$b linkError = link35(aaptServiceArgs, flatZipFileList, aaptServiceArgs.assetsList, mainProjectGenDir, resourcesApPath, false, aaptServiceArgs.getAaptRulesPath(), rTxt);
		if (linkError != null) {
			return linkError;
		}
		//优化

		return null;
	}

	private static void deleteCache(AaptServiceArgs aaptServiceArgs) {
		Set<String> flatDirSet = aaptServiceArgs.flatDirSet;
		Set<String> flatZipFileSet = aaptServiceArgs.flatZipFileSet;
		//删除无效缓存
		File[] flatZipFiles = aaptServiceArgs.getMergedDirFile().listFiles();
		if (flatZipFiles != null) {

			for (File file : flatZipFiles) {
				if (!flatZipFileSet.contains(file.getAbsolutePath())) {
					FileUtil.deleteFolder(file);
				}
			}
		}
		//删除无用的flat缓存目录
		File[] flatDirs = aaptServiceArgs.getFlatDirFile().listFiles();
		if (flatDirs != null) {
			for (File flatDir : flatDirs) {
				if (!flatDirSet.contains(flatDir.getAbsolutePath())) {
					FileUtil.deleteFolder(flatDir);
				}
			}						
		}
	}
	//实现更细的颗粒度
	private static AaptService$b compile(AaptServiceArgs aaptServiceArgs, String resDir) throws IOException {
		if (!new File(resDir).exists()) {
			return null;
		}
		long currentTimeMillis = System.currentTimeMillis();

		String flatDir = getAapt2ResCacheDir(aaptServiceArgs, resDir);
		//flat文件路径
		File flatDirFile = new File(flatDir);
		
		//记录使用的flat缓存目录，用于输出无用缓存
		aaptServiceArgs.flatDirSet.add(flatDir);


		//兼容旧版本
		if (flatDirFile.isFile()) {
			flatDirFile.delete();
		}
		if (!flatDirFile.exists()) {
			//保证输出路径是文件夹
			flatDirFile.mkdirs();
		}

		AaptService$b aaptError = null;
		String compileType;
		if (!flatDirFile.exists() 
			|| FileUtil.findFile(flatDirFile, null).isEmpty()) {
			//全量编译
			aaptError = fullCompile(aaptServiceArgs, resDir, flatDir, flatDirFile);
			compileType = "全量编译: ";
		} else {
			//增量编译
			aaptError = incrementalCompile(aaptServiceArgs, resDir, flatDir);
			compileType = "增量编译: "; // String.format("增量编译: %s " ,resDir);
		}
		currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
		if (currentTimeMillis > 30) {
			aaptServiceArgs.log.println(compileType + currentTimeMillis + " ms");			
		}

		return aaptError;
	}

	private static AaptService$b fullCompile(AaptServiceArgs aaptServiceArgs, String resDir, String flatDir, File flatDirFile) throws IOException {
		AaptService$b aaptError = fullCompile(aaptServiceArgs, resDir, flatDir);
		if (aaptError != null) {
			return aaptError;
		}
		//全量编译 合并zip[二级缓存]
		String flatsZipFile = getMergedCacheDirFile(aaptServiceArgs, resDir);

		File[] flatFiles = flatDirFile.listFiles();
		// 没有flat中间文件则不链接，无意义
		if (flatFiles == null 
			|| flatFiles.length == 0) {
			// 适配 No entries异常
			return null;
		}

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(flatsZipFile));
		//无损压缩 link时更快
		out.setMethod(ZipEntry.STORED);

		FlatByteArray flatByteArray = new FlatByteArray(1024 * 30);
		CRC32 crc = new CRC32();
		for (File file : flatFiles) {
			flatByteArray.reset();

			FileInputStream input = new FileInputStream(file);
			streamTransfer(input, flatByteArray);
			input.close();

			crc.reset();
			crc.update(flatByteArray.getBuf(), 0, flatByteArray.size());

			ZipEntry zipEntry = new ZipEntry(file.getName());
			zipEntry.setMethod(ZipEntry.STORED);
			zipEntry.setSize(file.length());
			zipEntry.setCrc(crc.getValue());

			out.putNextEntry(zipEntry);
			out.write(flatByteArray.getBuf(), 0, flatByteArray.size());
			out.closeEntry();
		}

		flatByteArray.close();
		out.close();

		//加入链接列表
		aaptServiceArgs.flatZipFileSet.add(flatsZipFile);
		
		return null;
	}

	public static String getRtxtFile(AaptServiceArgs aaptServiceArgs, String subGenDirPath) {
		String injectedAndroidManifestXml = aaptServiceArgs.injectedAManifestMap.get(subGenDirPath);
		if (injectedAndroidManifestXml == null) {
			return null;
		}

		String parent = FileSystem.getParent(injectedAndroidManifestXml);
		parent = parent.substring(0, parent.length() - "/bin/injected".length());

		if (parent.endsWith(".aar/bin/injected")) {
			//aar库
			File rTxtFile = new File(parent, "R.txt");
			if (rTxtFile.exists()) {
				return rTxtFile.getAbsolutePath();
			}
			return "";
		}
		//子项目
		parent = FileSystem.getParent(parent);
		File rTxtFile = new File(parent, "R.txt");
		if (rTxtFile.exists()) {
			return rTxtFile.getAbsolutePath();
		}
		return null;
	}

	// 半成品apk
	public static AaptService$b link35(AaptServiceArgs aaptServiceArgs, List<String> resourceList, List<String> assetsList,  String genDir, String outputPath, boolean isNonFinalIds , String proguardPath, String rTxtPath) throws Exception {

		String androidJar = aaptServiceArgs.androidJar;

		//merged
		String androidManifestXml = getAndroidManifestXml(aaptServiceArgs, genDir);

		AndroidManifestParser androidManifestRead = AndroidManifestParser.get(androidManifestXml);

		final int min;
		try {
			min = Integer.parseInt(androidManifestRead.getMinSdkVersion());
		}
		catch (Throwable e) {
			min = aaptServiceArgs.defaultMinSdk;
		}

		final int target;
		try {
			target = Integer.parseInt(androidManifestRead.getTargetSdkVersion());
		}
		catch (Throwable e) {
			target = aaptServiceArgs.defaultTargetSdk;
		}

		/*****/
		List<String> args = new ArrayList<>();
		args.add(aaptServiceArgs.getAapt2Path());
		args.add("link");

		args.add("-I");
		args.add(androidJar);
		args.add("--allow-reserved-package-id");
		args.add("--no-version-vectors");
		args.add("--no-version-transitions");
		args.add("--auto-add-overlay");
		if (min <= 0) {
			min = 21;
		}
		if (target <= 0) {
			target = 28;
		}

		args.add("--min-sdk-version");
		args.add(String.valueOf(min));
		args.add("--target-sdk-version");
		args.add(String.valueOf(target));

		if (!TextUtils.isEmpty(proguardPath)) {
			args.add("--proguard");
			args.add(proguardPath);
		}
		if (!TextUtils.isEmpty(genDir)) {
			args.add("--java");
			args.add(genDir);
		}
		//子项目
		if (isNonFinalIds) {
			args.add("--non-final-ids");
		}
		if (!TextUtils.isEmpty(androidManifestXml)) {
			args.add("--manifest");
			args.add(androidManifestXml);
		}
		if (!TextUtils.isEmpty(outputPath)) {
			//输出文件
			args.add("-o");
			args.add(outputPath);
		}

		if (!TextUtils.isEmpty(rTxtPath)) {
			args.add("--output-text-symbols");
			args.add(rTxtPath);
		}

		if (!assetsList.isEmpty()) {
			for (String s : assetsList) {
				File f = new File(s);
				if (f.exists()) {
					args.add("-A");
					args.add(f.getAbsolutePath());
				}
			}
		}

		// 自定义命令的实现
//		if (AdvancedSetting.isEnableCustomCommand()) {
//			String[] commandLines = AdvancedSetting.getCustomCommands().split("\n");
//			if (commandLines != null) {
//				args.addAll(Arrays.asList(commandLines));
//			}
//		}

		if (!resourceList.isEmpty()) {
			for (String path : resourceList) {
				File flatsZipFile = new File(path);
				if (!flatsZipFile.exists()) {
					continue;
				}
				args.add("-R");
				args.add(flatsZipFile.getAbsolutePath());
			}
		}



		//aaptServiceArgs.log.println(to(args));
		//
		abcd.wf j62 = abcd.xf.j6(args, null, null, true, null, null);

		if (j62.DW() != 0) {
			String s = aaptServiceArgs.getAapt2Error(j62);
			aaptServiceArgs.log.println("aapt2 错误: -> " + s);

			if (s != null) {
				return new AaptService$b(s);
			}
		}
		return null;
	}

	private static String to(List<String> args) {
		StringBuilder a = new StringBuilder("\n");
		for (String arg : args) {
			a.append(arg);
			a.append(" ");
		}
		return a.toString();
	}

	/**
	 * @author 3115093767
	 */
	private static String getAndroidManifestXml(AaptServiceArgs aaptServiceArgs, String subProjectGen) throws RuntimeException {
		String manifestXml = aaptServiceArgs.mergedAManifestMap.get(subProjectGen);
		if (FileSystem.exists(manifestXml)) {
			return manifestXml;
		}
		manifestXml = aaptServiceArgs.injectedAManifestMap.get(subProjectGen);
		if (FileSystem.exists(manifestXml)) {
			return manifestXml;
		}
		manifestXml = aaptServiceArgs.aManifestMap.get(manifestXml);
		if (FileSystem.exists(manifestXml)) {
			return manifestXml;
		}
		aaptServiceArgs.log.println("没有AndroidManifest文件玩尼玛\n");
		//没辙了
		throw new RuntimeException("没有AndroidManifest文件玩尼玛\n" + "Fuck you! Not found AndroidManifest file!!!");

	}

	// 编译成aapt2格式文件
	public static AaptService$b fullCompile(AaptServiceArgs aaptServiceArgs, String resDir, String output) {
		List<String> args = new ArrayList<>();

		args.add(aaptServiceArgs.getAapt2Path());
		args.add("compile");

		args.add("--dir");
		args.add(resDir);

		args.add("-o");
		args.add(output);

		//执行aapt2 compile命令
		//log.println(to(args));
		abcd.wf j62 = abcd.xf.j6(args, null, null, true, null, null);
		if (j62.DW() != 0) {
			String compileError = aaptServiceArgs.getAapt2Error(j62);
			if (compileError != null) {
				return new AaptService$b(compileError);
			}
		}

		return null;
	}

	public static boolean hasError(Object aaptService$bObject) {
		if (aaptService$bObject != null) {
			//com.aide.ui.build.android.AaptService.b b;
			// 类 com.aide.ui.build.android.AaptService$b
			ReflectPie on = ReflectPie.on(aaptService$bObject);
			if (on.get("DW") != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 优化
	 */
	private static AaptService$b incrementalCompile(AaptServiceArgs aaptServiceArgs, String resDir, String resFlatCacheDir) throws IOException {
		//增量编译
		List<String> incrementalInputFiles = new ArrayList<>();
		List<File> outFiles = new ArrayList<>();

		for (File resourceFile : FileUtil.findFile(new File(resDir), null)) {
			File flatFile = getAapt2FlatCacheFile(resFlatCacheDir, resourceFile);
			outFiles.add(flatFile);

			if (!flatFile.exists() || flatFile.lastModified() < resourceFile.lastModified()) {
				incrementalInputFiles.add(resourceFile.getAbsolutePath());
			}
		}

		//所有flat中间文件，去除需要的，剩下都是以删除的
		File flatDirFile = new File(resFlatCacheDir);

		List<File> oldFlatFiles = FileUtil.findFile(flatDirFile , null);
		oldFlatFiles.removeAll(outFiles);

		for (File oldFlatFile : oldFlatFiles) {
			oldFlatFile.delete();
		}

		if (! incrementalInputFiles.isEmpty()) {
			AaptService$b incrementalCompile = incrementalCompile(aaptServiceArgs, incrementalInputFiles, resFlatCacheDir);
			if (incrementalCompile != null) {
				//有错误，直接返回
				return incrementalCompile;
			}
		}


		File[] flatFiles = flatDirFile.listFiles();
		if (flatFiles == null || flatFiles.length == 0) {
			return null;
		}

		String flatsZipFile = getMergedCacheDirFile(aaptServiceArgs, resDir);
		//被使用，添加输出
		aaptServiceArgs.flatZipFileSet.add(flatsZipFile);
		//没有变动，增量
		if (incrementalInputFiles.isEmpty() 
			&& new File(flatsZipFile).exists()) {
			return null;
		}


		//合并成zip
		//增量编译后的[二级缓存]
		//后面再实现增量更新zip
		// 从zip中删除oldFlatFile名称的文件
		// 添加inputFiles的中间文件
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(flatsZipFile));
		out.setMethod(ZipEntry.STORED);

		FlatByteArray flatByteArray = new FlatByteArray(1024 * 30);
		CRC32 crc = new CRC32();
		for (File file : flatFiles) {
			flatByteArray.reset();

			FileInputStream input = new FileInputStream(file);
			streamTransfer(input, flatByteArray);
			input.close();

			crc.reset();
			crc.update(flatByteArray.getBuf(), 0, flatByteArray.size());

			ZipEntry zipEntry = new ZipEntry(file.getName());
			zipEntry.setMethod(ZipEntry.STORED);
			zipEntry.setSize(file.length());
			zipEntry.setCrc(crc.getValue());

			out.putNextEntry(zipEntry);
			out.write(flatByteArray.getBuf(), 0, flatByteArray.size());
			out.closeEntry();
		}

		flatByteArray.close();
		out.close();
		return null;
	}


	//增量编译
	public static AaptService$b incrementalCompile(AaptServiceArgs aaptServiceArgs, List<String> inputFiles, String output) {
		List<String> args = new ArrayList<>();
		args.add(aaptServiceArgs.getAapt2Path());
		args.add("compile");
		args.addAll(inputFiles);

		args.add("-o");
		args.add(output);

		abcd.wf j62 = abcd.xf.j6(args, null, null, true, null, null);
		if (j62.DW() != 0) {
			String errorInfo = aaptServiceArgs.getAapt2Error(j62);
			if (errorInfo != null) {
				return new AaptService$b(errorInfo);
			}
		}

		return null;
	}
	private static File getAapt2FlatCacheFile(String aapt2ResCacheDir, File resourceFile) {
		String parentFileName = resourceFile.getParentFile().getName();
		String resourceFileName = resourceFile.getName();

		String flatName = resourceFileName;
		if (parentFileName.startsWith("values")) {
			int flatSimpleNameEnd = resourceFileName.lastIndexOf('.');
			if (flatSimpleNameEnd > 0) {
				flatName = resourceFileName.substring(0, flatSimpleNameEnd);	
			}
			flatName = flatName + ".arsc.flat";
		} else {
			flatName = flatName + ".flat";
		}
		flatName = parentFileName + "_" + flatName;

		return new File(aapt2ResCacheDir, flatName);
	}

	// 返回编译输出路径 + 路径的md5码
	private static String getAapt2ResCacheDir(AaptServiceArgs aaptServiceArgs, String resPath) {
		return (aaptServiceArgs.getCompileDirPath() + "/" + MD5Util.stringMD5(resPath));
	}
	private static String getMergedCacheDirFile(AaptServiceArgs aaptServiceArgs, String resPath) {
		return (aaptServiceArgs.getMergedDirFile() + "/" + MD5Util.stringMD5(resPath) + ".zip");
	}

	public static class FlatByteArray extends ByteArrayOutputStream {
		public FlatByteArray() {}

		public FlatByteArray(int size) {
			super(size);
		}
		public byte[] getBuf() {
			return this.buf;
		}
	}
}
