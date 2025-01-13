/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/
package io.github.zeroaicy.aide.ui.project;
import com.aide.ui.project.NativeExecutableProjectSupport;
import java.io.File;
import com.aide.ui.project.internal.GradleTools;

public class ZeroAicyNativeExecutableProjectSupport extends NativeExecutableProjectSupport{

	@Override
	public boolean isSupport(String projectPath) {
		//NDK C/C++本机可执行项目
		return isNativeExecutableCmakeProject(projectPath) || super.isSupport(projectPath);
	}

	// hasAndroidMk
	@Override
	public boolean vy(String projectPath) {
		
		return GradleTools.getAndroidMkPath(projectPath);
	}
	
	
	public static boolean isNativeExecutableCmakeProject(String projectPath) {
		if (isAndroidProject(projectPath)) {
			return false;
		}
		if (GradleTools.isGradleProject(projectPath)) {
			return false;
		}
		return isCmakeProject(projectPath, false);
	}

	private static boolean isAndroidProject(String projectPath) {
		return new File(projectPath, "AndroidManifest.xml").exists();
	}


	public static boolean isCmakeProject(String projectPath) {
		return isCmakeProject(projectPath, GradleTools.isGradleProject(projectPath));
	}
	
    //判断是否是 cmake项目，指定CMakeLists.txt(计划支持)
    public static boolean isCmakeProject(String projectPath, boolean isGradle) {

        //检查 cmake及ndk安装情况
		File cMakeListsTxtFile = 
            new File(projectPath, 
                     isGradle ? 
                     "src/main/cpp/CMakeLists.txt" : 
                     "cpp/CMakeLists.txt");

        return cMakeListsTxtFile.isFile();
	}
}
