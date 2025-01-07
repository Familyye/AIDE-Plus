package io.github.zeroaicy.aide.ui.project;

import com.aide.engine.EngineSolution;
import com.aide.engine.EngineSolutionProject;
import com.aide.ui.project.AndroidProjectSupport;
import java.util.List;
import com.aide.ui.project.internal.GradleTools;
import com.aide.ui.util.FileSystem;
import io.github.zeroaicy.aide.utils.ZeroAicyBuildGradle;
import android.text.TextUtils;

public class ZeroAicyAndroidProjectSupport extends AndroidProjectSupport {
	public ZeroAicyAndroidProjectSupport() {

	}

	@Override
	public EngineSolution makeEngineSolution() {
		EngineSolution makeEngineSolution = super.makeEngineSolution();

		addCmakeSourceDir(makeEngineSolution);

		return makeEngineSolution;
	}
	
	// 添加 cmake的源码目录
	private void addCmakeSourceDir(EngineSolution makeEngineSolution) {
		List<EngineSolutionProject> engineSolutionProjects = (List<EngineSolutionProject>)makeEngineSolution.engineSolutionProjects;
		for (EngineSolutionProject engineSolutionProject : engineSolutionProjects) {
			String projectPath = engineSolutionProject.getProjectPath();
			if (!GradleTools.isGradleProject(projectPath)) {
				continue;
			}
			String buildGradlePath = GradleTools.getBuildGradlePath(projectPath);
			if (!FileSystem.exists(buildGradlePath)) {
				// 存在build.gradle
				// 错误的 gradle 项目
				continue;
			}

			ZeroAicyBuildGradle configuration = ZeroAicyBuildGradle.getSingleton().getConfiguration(buildGradlePath);

			String cmakeListsTxtPath = configuration.getCmakeListsTxtPath();

			if (TextUtils.isEmpty(cmakeListsTxtPath)) {
				cmakeListsTxtPath = "src/main/cpp";
			}
			if (cmakeListsTxtPath.endsWith("CMakeLists.txt")) {
				cmakeListsTxtPath = FileSystem.getParent(cmakeListsTxtPath);
			}
			
			engineSolutionProject.fY.add(new EngineSolution.File(projectPath + "/" + cmakeListsTxtPath, "C++", null, false, false));
		}
	}

}

