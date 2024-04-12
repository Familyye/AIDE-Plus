package com.aide.ui.project;

import com.aide.common.ValueRunnable;
import com.aide.engine.EngineSolution;
import com.aide.ui.services.ProjectSupport;
import com.aide.ui.services.TemplateService;
import java.util.List;
import java.util.Map;

public class JavaGradleProjectSupport implements ProjectSupport {

	@Override
	public void DW(boolean p) {
		// TODO: Implement this method
	}

	@Override
	public boolean EQ() {
		// TODO: Implement this method
		return false;
	}

	@Override
	public boolean FH(String string) {
		// TODO: Implement this method
		return false;
	}

	@Override
	public boolean Hw() {
		// TODO: Implement this method
		return false;
	}

	@Override
	public void J0(String string) {
		// TODO: Implement this method
	}

	@Override
	public boolean J8() {
		// TODO: Implement this method
		return false;
	}

	@Override
	public int KD(String string) {
		// TODO: Implement this method
		return 0;
	}

	@Override
	public void Mr() {
		// TODO: Implement this method
	}

	@Override
	public void P8(String string, String string1) {
		// TODO: Implement this method
	}

	@Override
	public String QX() {
		// TODO: Implement this method
		return null;
	}

	@Override
	public void SI(String string, ValueRunnable<String> valueRunnable) {
		// TODO: Implement this method
	}


	/**
	 * 解析
	 */
	@Override
	public void U2(String projectPath, Map<String, List<String>> subProjectMap, List<String> projectPaths) {

	}

	/**
	 * 模板
	 */
	@Override
	public TemplateService.TemplateGroup[] VH() {
		return null;
	}

	/**
	 * 返回EngineSolution
	 */
	@Override
	public EngineSolution Ws() {
		return null;
	}

	@Override
	public List<com.aide.ui.trainer.c.c> XL() {
		return null;
	}

	@Override
	public boolean Zo(String string) {
		// TODO: Implement this method
		return false;
	}

	@Override
	public boolean a8(String string) {
		// TODO: Implement this method
		return false;
	}

	@Override
	public boolean aM(String string) {
		// TODO: Implement this method
		return false;
	}

	@Override
	public void cn(List<String> list, boolean p) {
		// TODO: Implement this method
	}

	@Override
	public void ei(String string) {
		// TODO: Implement this method
	}

	/**
	 * 安卓项目-判断依据 项目目录 src文件夹存在 build.gradle存在
	 * 或者 AndroidManifest.xml存在
	 * Java项目-判断依据 项目目录 .classpath文件存在
	 *
	 * 是否是支持此项目
	 * 此项目不支持渠道包
	 * 
	 */
	@Override
	public boolean er(String projectPath) {
		return false;
	}

	@Override
	public boolean gW() {
		return false;
	}

	/*
	 * 不支持，渠道包
	 */
	@Override
	public List<String> getProductFlavors(String path) {
		return null;
	}

	@Override
	public void gn() {
		// TODO: Implement this method
	}

	@Override
	public boolean j3(String string) {
		// TODO: Implement this method
		return false;
	}

	@Override
	public void j6() {
		// TODO: Implement this method
	}

	@Override
	public boolean lg() {
		// TODO: Implement this method
		return false;
	}

	@Override
	public void nw(String string) {
		// TODO: Implement this method
	}

	@Override
	public int rN(String string) {
		// TODO: Implement this method
		return 0;
	}

	@Override
	public List<String> ro(String string) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public String sh(String string) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public String tp(String string) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public boolean u7(String string) {
		// TODO: Implement this method
		return false;
	}

	@Override
	public String v5(String string) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public boolean vy(String string) {
		// TODO: Implement this method
		return false;
	}

	@Override
	public int we(String string) {
		// TODO: Implement this method
		return 0;
	}

	@Override
	public String yS() {
		// TODO: Implement this method
		return null;
	}

}
