package io.github.zeroaicy.aide.cmake;

import android.text.TextUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import io.github.zeroaicy.aide.utils.Utils;

public class CmakeBuild {
	/* 
	 ANDROID_SDK_PATH=$HOME/android-sdk
	
	 $ANDROID_SDK_PATH/cmake/$CMAKE_VERSION/bin/cmake \
	 -H$PROJECT_PATH/$CMAKE_LISTS_TXT_PATH \
	 -DCMAKE_SYSTEM_NAME=Android \
	 -DCMAKE_EXPORT_COMPILE_COMMANDS=ON \
	 -DCMAKE_SYSTEM_VERSION=$SYSTEM_VERSION \
	 -DANDROID_PLATFORM=android-$SYSTEM_VERSION \
	 -DANDROID_ABI=$ANDROID_ABI \
	 -DCMAKE_ANDROID_ARCH_ABI=$ANDROID_ABI \
	 -DANDROID_NDK=$ANDROID_SDK_PATH/ndk/$NDK_VERSION \
	 -DCMAKE_ANDROID_NDK=$ANDROID_SDK_PATH/ndk/$NDK_VERSION \
	 -DCMAKE_TOOLCHAIN_FILE=$ANDROID_SDK_PATH/ndk/$NDK_VERSION/build/cmake/android.toolchain.cmake \
	 -DCMAKE_MAKE_PROGRAM=$ANDROID_SDK_PATH/cmake/$CMAKE_VERSION/bin/ninja \
	 -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=$PROJECT_PATH/" + CMAKE_OUTPUT_DIRECTORY_PATH + "/$ANDROID_ABI \
	 -DCMAKE_RUNTIME_OUTPUT_DIRECTORY=$PROJECT_PATH/" + CMAKE_OUTPUT_DIRECTORY_PATH + "/$ANDROID_ABI \
	 -DCMAKE_BUILD_TYPE=$CMAKE_BUILD_TYPE \
	 -B$CMAKE_BUILD_CACHE_PATH \
	 -GNinja
	 -B含义是cmake输出目录(构建脚本目录)
	 
	 $ANDROID_SDK_PATH/cmake/$CMAKE_VERSION/bin/ninja -C $CMAKE_BUILD_CACHE_PATH
	 
	 */

	private boolean error;
	private StringBuilder errorInfoBuilder = new StringBuilder();
	private List<String> cmakeCommandList;
	private List<String> ninjaCommandList;

	public void setCmakeCommandList(List<String> commandList) {
		this.cmakeCommandList = commandList;
	}

	static boolean debug = !true;
	public List<String> getCmakeCommandList() {
		if (debug) {
			for (String cmd : this.cmakeCommandList) {
				System.out.println(cmd);
			}
		}
		return this.cmakeCommandList;
	}

	public void setNinjaCommandList(List<String> commandList) {
		this.ninjaCommandList = commandList;
	}

	public List<String> getNinjaCommandList() {
		return this.ninjaCommandList;
	}
	public boolean error() {
		return error;
	}

	public CmakeBuild addErrorInfo(String errorInfo) {
		this.errorInfoBuilder.append("Error: ");
		this.errorInfoBuilder.append(errorInfo);
		this.errorInfoBuilder.append('\n');
		this.error = true;
		return this;
	}
	public String getBuildInfo() {
		return errorInfoBuilder.toString();
	}

	public static class Builder {
		public static boolean cmakeBuild(String projectPath) {

			return false;
		}

		// 项目路径
		private String PROJECT_PATH;
		public CmakeBuild.Builder setProjectPath(String projectPath) {
			this.PROJECT_PATH = projectPath;
			return this;
		}

		// android_sdk路径
		private String ANDROID_SDK_PATH;
		// ANDROID_SDK
		public CmakeBuild.Builder setAndroidSdkPath(String androidSdkPath) {
			this.ANDROID_SDK_PATH = androidSdkPath;

			File androidSdkFile = new File(ANDROID_SDK_PATH);
			if (!androidSdkFile.exists()) {
				cmakeBuild.addErrorInfo("ANDROID_SDK目录不存在 -> " + ANDROID_SDK_PATH);
				cmakeBuild.addErrorInfo("Ndk父目录ANDROID_SDK不存在");
				cmakeBuild.addErrorInfo("🤔 -> 使用 https://github.com/ZeroAicy/AIDE-Ndk-Install 安装Ndk");

				return this;
			}

			return this;
		}

		// cmake版本 默认最新版本
		private String CMAKE_VERSION;
		public CmakeBuild.Builder setCmakeVersion(String cmakeVersion) {
			this.CMAKE_VERSION = cmakeVersion;
			return this;
		}
		// ndk版本 默认最新版本
		private String NDK_VERSION;
		public CmakeBuild.Builder setNdkVersion(String ndkVersion) {
			this.NDK_VERSION = ndkVersion;
			return this;
		}

		// ANDROID_ABI
		private String ANDROID_ABI;
		// ANDROID_ABI
		public CmakeBuild.Builder setAndroidABI(String abi) {
			this.ANDROID_ABI = abi;

			if ("arm64-v8a".equals(abi)) {
				//android-21才支持arm64-v8a
				setSystemVersion("21");
			}
			return this;
		}

		private String SYSTEM_VERSION = "19";
		public CmakeBuild.Builder setSystemVersion(String systemVersion) {
			this.SYSTEM_VERSION = systemVersion;
			return this;
		}
		public String getSystemVersion() {
			return this.SYSTEM_VERSION;
		}

		// CMakeLists.txt 文件抽象路径 相对于项目 
		private String CMAKE_LISTS_TXT_PATH;
		public CmakeBuild.Builder setCmakeListsTxtPath(String cmakeListsTxtPath) {
			this.CMAKE_LISTS_TXT_PATH = cmakeListsTxtPath;
			return this;
		}

		// 默认与CMAKE_OUTPUT_DIRECTORY_PATH在同级目录
		private String CMAKE_BUILD_CACHE_PATH;
		public CmakeBuild.Builder setCmakeBuildCachePath(String cmakeBuildCachePath) {
			this.CMAKE_BUILD_CACHE_PATH = cmakeBuildCachePath;
			return this;
		}
		// 缓存路径
		public String getCmakeBuildCachePath() {
			return this.CMAKE_BUILD_CACHE_PATH;
		}

		// 库最终输出路径
		private String CMAKE_LIBRARY_OUTPUT_DIRECTORY;

		private String CMAKE_OUTPUT_DIRECTORY_PATH;
		public CmakeBuild.Builder setCmakeOutputDirectoryPath(String cmakeOutputDirectoryPath) {
			this.CMAKE_OUTPUT_DIRECTORY_PATH = cmakeOutputDirectoryPath;
			// 推算变量
			setCmakeBuildCachePath(CMAKE_OUTPUT_DIRECTORY_PATH + "/../obj/cmake");

			return this;
		}

		//可执行文件的输出目录
		private String CMAKE_RUNTIME_OUTPUT_DIRECTORY;
		public CmakeBuild.Builder setCmakeRuntimeOutputDirectoryPath(String cmakeRuntimeOutputDirectoryPath) {
			this.CMAKE_RUNTIME_OUTPUT_DIRECTORY = cmakeRuntimeOutputDirectoryPath;
			return this;
		}

		// cmake构建类型 默认Release 可选[Debug]
		public String CMAKE_BUILD_TYPE = "Release";
		public CmakeBuild.Builder setCmakeBuildType(String cmakeBuildType) {
			this.CMAKE_BUILD_TYPE = cmakeBuildType;
			return this;
		}

		private CmakeBuild cmakeBuild = new CmakeBuild();

		public CmakeBuild build() {

			// 检查 设置的cmake是否有效
			if (!TextUtils.isEmpty(this.CMAKE_VERSION)) {
				File cmakeFile = new File(ANDROID_SDK_PATH + "/cmake", CMAKE_VERSION + "/bin/cmake");
				if (!cmakeFile.isFile()) {
					// 错误版本 置空
					setCmakeVersion(null);
				}
			}
			
			//查找cmake
			if (TextUtils.isEmpty(this.CMAKE_VERSION)) {
				//未设置CMAKE_VERSION，自动查找
				File cmakeDir = new File(this.ANDROID_SDK_PATH, "cmake");
				String[] cmakeVersions = cmakeDir.list();

				if (cmakeVersions == null || cmakeVersions.length == 0) {
					cmakeBuild.addErrorInfo("未发现cmake可用版本: " + cmakeDir.getAbsolutePath());
				} else {
					// 排序
					setCmakeVersion(cmakeVersions[cmakeVersions.length - 1]);
				}
			}

			// 检查 设置的Ndk是否有效
			if (!TextUtils.isEmpty(this.NDK_VERSION)) {
				File ndkVersionDir = new File(this.ANDROID_SDK_PATH + "/ndk", NDK_VERSION);
				if (!ndkVersionDir.isDirectory()) {
					// cmakeBuild.addErrorInfo("cmake 不存在 : " + cmakeFile.getAbsolutePath());
					// 错误版本 置空
					setNdkVersion(null);
				}
			}
			
			//查找ndk
			if (TextUtils.isEmpty(this.NDK_VERSION)) {
				//未设置CMAKE_VERSION，自动查找
				File ndkDir = new File(this.ANDROID_SDK_PATH, "ndk");
				String[] ndkVersions = ndkDir.list();
				if (ndkVersions == null || ndkVersions.length == 0) {
					cmakeBuild.addErrorInfo("未发现ndk可用版本: " + ndkDir.getAbsolutePath());
				} else {
					//警告
					setNdkVersion(ndkVersions[ndkVersions.length - 1]);
				}
			}

			//确保可以编译
			if ("arm64-v8a".equals(ANDROID_ABI)) {
				//android-21才支持arm64-v8a
				if (Utils.parseInt(this.getSystemVersion(), 0) < 21) {
					setSystemVersion("21");
				}
			}

			// 检查环境
			//项目相关变量
			//目标ABI
			if (TextUtils.isEmpty(ANDROID_ABI)) {
				cmakeBuild.addErrorInfo("未设置ANDROID_ABI");
			}
			//项目路径
			if (TextUtils.isEmpty(PROJECT_PATH)) {
				cmakeBuild.addErrorInfo("未设置PROJECT_PATH");
			}
			// CMakeLists.txt所在目录相对于PROJECT_PATH
			if (TextUtils.isEmpty(CMAKE_LISTS_TXT_PATH)) {
				cmakeBuild.addErrorInfo("未设置CMAKE_LISTS_TXT_PATH");
			}
			// 编译输出目录相对于PROJECT_PATH
			if (TextUtils.isEmpty(CMAKE_OUTPUT_DIRECTORY_PATH)) {
				cmakeBuild.addErrorInfo("未设置CMAKE_OUTPUT_DIRECTORY_PATH");
			}

			// 库输出目录
			this.CMAKE_LIBRARY_OUTPUT_DIRECTORY = CMAKE_OUTPUT_DIRECTORY_PATH + "/" + ANDROID_ABI;

			//可执行输出目录
			if (TextUtils.isEmpty(this.CMAKE_RUNTIME_OUTPUT_DIRECTORY)) {
				//可执行输出目录未设置，与CMAKE_LIBRARY_OUTPUT_DIRECTORY相同
				this.CMAKE_RUNTIME_OUTPUT_DIRECTORY = CMAKE_LIBRARY_OUTPUT_DIRECTORY;
			}

			//编译环境变量
			if (TextUtils.isEmpty(ANDROID_SDK_PATH)) {
				cmakeBuild.addErrorInfo("未设置ANDROID_SDK_PATH");
			}

			File projectFile = new File(PROJECT_PATH);
			File buildNinjaFile = new File(projectFile, CMAKE_BUILD_CACHE_PATH + "/" + ANDROID_ABI + "/build.ninja");

			// 增量更新
			if (new File(projectFile, CMAKE_LISTS_TXT_PATH + "/CMakeLists.txt").lastModified() > buildNinjaFile
					.lastModified()) {

				buildNinjaFile.delete();

				new File(buildNinjaFile.getParentFile(), "CMakeCache.txt").delete();
			}

			if (cmakeBuild.error()) {
				return cmakeBuild;
			}

			List<String> cmakeCommandList = new ArrayList<String>();
			cmakeCommandList.add(ANDROID_SDK_PATH + "/cmake/" + CMAKE_VERSION + "/bin/cmake");
			cmakeCommandList.add("-DCMAKE_SYSTEM_NAME=Android");

			cmakeCommandList.add("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON");
			//设置CMakeLists.txt所在目录
			cmakeCommandList.add("-H" + PROJECT_PATH + "/" + CMAKE_LISTS_TXT_PATH);

			//指定安卓版本
			cmakeCommandList.add("-DCMAKE_SYSTEM_VERSION=" + SYSTEM_VERSION);
			cmakeCommandList.add("-DANDROID_PLATFORM=android-" + SYSTEM_VERSION);

			//设置ABI
			cmakeCommandList.add("-DANDROID_ABI=" + ANDROID_ABI);
			cmakeCommandList.add("-DCMAKE_ANDROID_ARCH_ABI=" + ANDROID_ABI);

			//设置NDK路径
			cmakeCommandList.add("-DANDROID_NDK=" + ANDROID_SDK_PATH + "/ndk/" + NDK_VERSION);
			cmakeCommandList.add("-DCMAKE_ANDROID_NDK=" + ANDROID_SDK_PATH + "/ndk/" + NDK_VERSION);
			//设置NDK初始化Cmake文件
			cmakeCommandList.add("-DCMAKE_TOOLCHAIN_FILE=" + ANDROID_SDK_PATH + "/ndk/" + NDK_VERSION
					+ "/build/cmake/android.toolchain.cmake");
			//设置ninja路径
			cmakeCommandList.add("-DCMAKE_MAKE_PROGRAM=" + ANDROID_SDK_PATH + "/cmake/" + CMAKE_VERSION + "/bin/ninja");
			//输出路径
			cmakeCommandList
					.add("-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=" + PROJECT_PATH + "/" + CMAKE_LIBRARY_OUTPUT_DIRECTORY);
			cmakeCommandList
					.add("-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=" + PROJECT_PATH + "/" + CMAKE_RUNTIME_OUTPUT_DIRECTORY);
			//构建类型
			cmakeCommandList.add("-DCMAKE_BUILD_TYPE=" + CMAKE_BUILD_TYPE);
			//缓存目录
			cmakeCommandList.add("-B" + PROJECT_PATH + "/" + CMAKE_BUILD_CACHE_PATH + "/" + ANDROID_ABI);
			//生成ninja脚本
			cmakeCommandList.add("-GNinja");

			cmakeBuild.setCmakeCommandList(cmakeCommandList);

			//使用ninja执行build.ninja
			List<String> ninjaCommandList = new ArrayList<>();
			ninjaCommandList.add(ANDROID_SDK_PATH + "/cmake/" + CMAKE_VERSION + "/bin/ninja");
			ninjaCommandList.add("-C");
			ninjaCommandList.add(PROJECT_PATH + "/" + CMAKE_BUILD_CACHE_PATH + "/" + ANDROID_ABI);
			cmakeBuild.setNinjaCommandList(ninjaCommandList);

			return cmakeBuild;
		}

		private void method() {
			Map<String, String> environmentMap = new HashMap<>();

			environmentMap.put("ANDROID_SDK_PATH", ANDROID_SDK_PATH);
			environmentMap.put("CMAKE_VERSION", CMAKE_VERSION);
			environmentMap.put("PROJECT_PATH", PROJECT_PATH);
			environmentMap.put("CMAKE_LISTS_TXT_PATH", CMAKE_LISTS_TXT_PATH);
			environmentMap.put("SYSTEM_VERSION", SYSTEM_VERSION);
			environmentMap.put("ANDROID_ABI", ANDROID_ABI);
			environmentMap.put("NDK_VERSION", NDK_VERSION);
			environmentMap.put("CMAKE_OUTPUT_DIRECTORY_PATH", CMAKE_OUTPUT_DIRECTORY_PATH);
			environmentMap.put("CMAKE_BUILD_TYPE", CMAKE_BUILD_TYPE);
			environmentMap.put("CMAKE_BUILD_CACHE_PATH", CMAKE_BUILD_CACHE_PATH);
		}

		private void old(List<String> commandList) {
			commandList.add(ANDROID_SDK_PATH + "/cmake/" + CMAKE_VERSION + "/bin/cmake");

			commandList.add("-DCMAKE_SYSTEM_NAME=Android");
			commandList.add("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON");
			//设置CMakeLists.txt所在目录
			commandList.add("-H$PROJECT_PATH/$CMAKE_LISTS_TXT_PATH");

			//指定安卓版本
			commandList.add("-DCMAKE_SYSTEM_VERSION=$SYSTEM_VERSION");
			commandList.add("-DANDROID_PLATFORM=android-$SYSTEM_VERSION");
			//设置ABI
			commandList.add("-DANDROID_ABI=$ANDROID_ABI");
			commandList.add("-DCMAKE_ANDROID_ARCH_ABI=$ANDROID_ABI");
			//设置NDK路径
			commandList.add("-DANDROID_NDK=$ANDROID_SDK_PATH/ndk/$NDK_VERSION");
			commandList.add("-DCMAKE_ANDROID_NDK=$ANDROID_SDK_PATH/ndk/$NDK_VERSION");
			//设置NDK初始化Cmake文件
			commandList.add(
					"-DCMAKE_TOOLCHAIN_FILE=$ANDROID_SDK_PATH/ndk/$NDK_VERSION/build/cmake/android.toolchain.cmake");
			//设置ninja路径
			commandList.add("-DCMAKE_MAKE_PROGRAM=$ANDROID_SDK_PATH/cmake/$CMAKE_VERSION/bin/ninja");
			//输出路径
			commandList.add(
					"-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=$PROJECT_PATH/" + CMAKE_OUTPUT_DIRECTORY_PATH + "/$ANDROID_ABI ");
			commandList.add(
					"-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=$PROJECT_PATH/" + CMAKE_OUTPUT_DIRECTORY_PATH + "/$ANDROID_ABI");
			//构建类型
			commandList.add("-DCMAKE_BUILD_TYPE=$CMAKE_BUILD_TYPE");
			//缓存目录
			commandList.add("-B$PROJECT_PATH/$CMAKE_BUILD_CACHE_PATH/$ANDROID_ABI");
			//生成ninja脚本
			commandList.add("-GNinja");

			commandList.add("&&");
			//使用ninja执行build.ninja
			//commandList.add("$ANDROID_SDK_PATH/cmake/$CMAKE_VERSION/bin/ninja -C $CMAKE_BUILD_CACHE_PATH/$ANDROID_ABI");
			commandList.add(ANDROID_SDK_PATH + "/cmake/" + CMAKE_VERSION
					+ "/bin/ninja -C $PROJECT_PATH/$CMAKE_BUILD_CACHE_PATH/$ANDROID_ABI");
		}

	}

	/*
	
	 $ANDROID_SDK_PATH/cmake/$CMAKE_VERSION/bin/cmake \
	 -DCMAKE_SYSTEM_NAME=Android \ √
	 -DCMAKE_EXPORT_COMPILE_COMMANDS=ON \ √
	
	 -H$PROJECT_PATH/$CMAKE_LISTS_TXT_PATH \ √
	
	 -DCMAKE_SYSTEM_VERSION=$SYSTEM_VERSION \ √
	 -DANDROID_PLATFORM=android-$SYSTEM_VERSION \ √
	 -DANDROID_ABI=$ANDROID_ABI \ √
	 -DCMAKE_ANDROID_ARCH_ABI=$ANDROID_ABI \ √
	 -DANDROID_NDK=$ANDROID_SDK_PATH/ndk/$NDK_VERSION \ √
	 -DCMAKE_ANDROID_NDK=$ANDROID_SDK_PATH/ndk/$NDK_VERSION \ √
	 -DCMAKE_TOOLCHAIN_FILE=$ANDROID_SDK_PATH/ndk/$NDK_VERSION/build/cmake/android.toolchain.cmake \ √
	 -DCMAKE_MAKE_PROGRAM=$ANDROID_SDK_PATH/cmake/$CMAKE_VERSION/bin/ninja \ √
	 -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=$PROJECT_PATH/" + CMAKE_OUTPUT_DIRECTORY_PATH + "/$ANDROID_ABI \ √
	 -DCMAKE_RUNTIME_OUTPUT_DIRECTORY=$PROJECT_PATH/" + CMAKE_OUTPUT_DIRECTORY_PATH + "/$ANDROID_ABI \ √
	 -DCMAKE_BUILD_TYPE=$CMAKE_BUILD_TYPE \ √
	 -B$CMAKE_BUILD_CACHE_PATH \ √
	 -GNinja
	 #-B含义是cmake输出目录(构建脚本目录)
	 $ANDROID_SDK_PATH/cmake/$CMAKE_VERSION/bin/ninja -C $CMAKE_BUILD_CACHE_PATH
	
	 */
}

