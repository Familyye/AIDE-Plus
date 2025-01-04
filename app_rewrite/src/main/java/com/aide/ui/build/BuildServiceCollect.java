package com.aide.ui.build;

import com.aide.ui.build.android.AndroidProjectBuildService;
import com.aide.ui.build.java.JavaProjectBuildService;
import com.aide.ui.build.javascript.JavaScriptBuildService;
import com.aide.ui.build.nativeexecutable.NativeProjectBuildService;
import com.aide.ui.htmluidesigner.HtmlCodeBuildService;
import com.aide.ui.project.JavaGradleProjectSupport;

public class BuildServiceCollect {


    public static AndroidProjectBuildService androidProjectBuildService;

    public static IBuildService[] buildServices;


    public static HtmlCodeBuildService htmlCodeBuildService;

    public static JavaProjectBuildService javaProjectBuildService;

    public static JavaScriptBuildService javaScriptBuildService;

    public static NativeProjectBuildService nativeProjectBuildService;

    static {
		androidProjectBuildService = new AndroidProjectBuildService();
		javaProjectBuildService = new JavaProjectBuildService();
		nativeProjectBuildService = new NativeProjectBuildService();
		htmlCodeBuildService = new HtmlCodeBuildService();
		javaScriptBuildService = new JavaScriptBuildService();
		buildServices = new IBuildService[]{
			androidProjectBuildService,
			javaProjectBuildService, 
			nativeProjectBuildService, 
			htmlCodeBuildService, 
			javaScriptBuildService, 
			JavaGradleProjectSupport.buildService
		};
    }
    public BuildServiceCollect() {}
}
