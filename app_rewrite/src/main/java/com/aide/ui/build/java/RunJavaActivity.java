/**
 * @Author ZeroAicy
 * @AIDE AIDE+
 */
package com.aide.ui.build.java;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import com.aide.ui.build.OutputConsole;
import com.aide.ui.build.OutputConsoleActivity;
import com.aide.ui.rewrite.R;
import dalvik.system.DexClassLoader;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;
import io.github.zeroaicy.util.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

public class RunJavaActivity extends OutputConsoleActivity {


	static Handler getUiHandler(RunJavaActivity runJavaActivity) {
		return runJavaActivity.mb;
	}

	protected static void U2(Activity activity, Class<?> cls, boolean z, String str, String str2, boolean z2, int i) {
		Intent intent = new Intent(activity, cls);
		intent.putExtra("EXTRA_DEX", str);
		intent.putExtra("EXTRA_CLASS", str2);
		intent.putExtra("EXTRA_DEBUG", z2);
		OutputConsoleActivity.QX(activity, z, i, intent);
	}

	public static void a8(Activity activity, boolean z, String str, String str2, boolean z2) {
		U2(activity, RunJavaActivity.class, z, str, str2, z2, 0);
	}

	static void j3(RunJavaActivity runJavaActivity) {
		runJavaActivity.EQ();
	}

	private Class<?> adrtClass;

	static ThreadPoolService defaultThreadPoolService = ThreadPoolService.getDefaultThreadPoolService();

	@Override
	protected void XL() {


		String extarDex = getIntent().getExtras().getString("EXTRA_DEX");

		String extarClass = getIntent().getExtras().getString("EXTRA_CLASS");

		boolean extarDebug = getIntent().getBooleanExtra("EXTRA_DEBUG", false);
		File optimizedDirectory = getDir("outdex", 0);
		
		File extarDexFile = new File(extarDex);
		File extarDexOutFile = new File(optimizedDirectory, extarDexFile.getName());

		
		// extarDex -> extarDexOutFile
		RunMainMethodRunnable runMainMethodRunnable = new RunMainMethodRunnable(this, extarDexOutFile.getAbsolutePath(), extarClass, optimizedDirectory, extarDebug);
		
		CopyDexRunable copyDexRunable = new CopyDexRunable(this, extarDex, extarDexOutFile, optimizedDirectory, runMainMethodRunnable);
		
		defaultThreadPoolService.submit(copyDexRunable);

	}

	// 主线程中运行
	private void runMainMethod(String extarDex, String extarClass, File optimizedDirectory, boolean extarDebug) {
		DexClassLoader dexClassLoader = new DexClassLoader(extarDex, optimizedDirectory.getPath(), null,
														   ClassLoader.getSystemClassLoader());
		
		// 调试模式
		if (extarDebug) {
			try {
				this.adrtClass = dexClassLoader.loadClass("adrt/ADRT");
				this.adrtClass
					.getDeclaredMethod("connectDebugger", Context.class, String.class, Boolean.TYPE, Boolean.TYPE)
					.invoke(null, getApplicationContext(), getPackageName(), Boolean.FALSE, Boolean.FALSE);
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
		}

		try {
			Method declaredMethod = dexClassLoader.loadClass(extarClass).getDeclaredMethod("main", String[].class);
			OutputConsole outputConsole = this.WB;

			System.setOut(outputConsole.getPrintStream());
			System.setErr(outputConsole.getPrintStream());
			System.setIn(outputConsole.getInputStream());

			// 异步 运行 main方法
			defaultThreadPoolService.submit(new InvokeMainMethodRunnable(this, declaredMethod, new String[0]));

		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}

	protected void onDestroy() {
		if (this.adrtClass != null) {
			try {
				this.adrtClass.getDeclaredMethod("disconnectDebugger", new Class[0]).invoke(null, new Object[0]);
			}
			catch (Throwable th) {
				th.printStackTrace();
			}
		}
		super.onDestroy();

		// 退出
		Process.killProcess(Process.myPid());
	}

	protected int u7() {
		// 0x7f07007b
		return R.drawable.ic_launcher_java;
	}

	public static class CopyDexRunable implements Runnable {

		RunJavaActivity runJavaActivity;
		String extarDex;
		File optimizedDirectory;
		Runnable uiSyncRunnable;
		
		File extarDexOutFile;
		
		public CopyDexRunable(RunJavaActivity runJavaActivity, String extarDex, File extarDexOutFile, File optimizedDirectory, Runnable uiSyncRunnable) {
			this.runJavaActivity = runJavaActivity;
			this.extarDex = extarDex;
			this.extarDexOutFile = extarDexOutFile;
			this.optimizedDirectory = optimizedDirectory;

			this.uiSyncRunnable = uiSyncRunnable;
		}

		@Override
		public void run() {

			if (!optimizedDirectory.exists()) optimizedDirectory.mkdirs();

			File[] files = optimizedDirectory.listFiles();  
			if (files != null) {
				for (File file : files) {
					file.delete();
				}
			}
			FileInputStream input = null;
			FileOutputStream output = null;
			try {
				input = new FileInputStream(this.extarDex);
				output = new FileOutputStream(this.extarDexOutFile);
				IOUtils.streamTransfer(input, output);
			}
			catch (Throwable e) {}
			finally {
				IOUtils.close(input);
				IOUtils.close(output);
			}

			// 适配 Android 14 -> DexClassLoader 不再支持从可写文件加载 dex/jar 文件
			this.extarDexOutFile.setWritable(false, false);

			Handler uiHandler = RunJavaActivity.getUiHandler(this.runJavaActivity);
			uiHandler.post(this.uiSyncRunnable);
		}
	}
	
	public static class RunMainMethodRunnable implements Runnable {
		RunJavaActivity runJavaActivity;
		String extarDex;
		String extarClass;
		File optimizedDirectory;
		boolean extarDebug;

		public RunMainMethodRunnable(RunJavaActivity runJavaActivity, String extarDex, String extarClass, File optimizedDirectory, boolean extarDebug) {
			this.runJavaActivity = runJavaActivity;
			this.extarDex = extarDex;
			this.extarClass = extarClass;
			this.optimizedDirectory = optimizedDirectory;
			this.extarDebug = extarDebug;
		}

		@Override
		public void run() {
			runJavaActivity.runMainMethod(extarDex, extarClass, optimizedDirectory, extarDebug);
		}

	}

	public static class InvokeMainMethodRunnable implements Runnable {
		final RunJavaActivity runJavaActivity;
		final Method curMethod;
		final Object args;

		public InvokeMainMethodRunnable(RunJavaActivity runJavaActivity, Method curMethod, Object args) {
			this.runJavaActivity = runJavaActivity;
			this.curMethod = curMethod;
			this.args = args;
		}

		@Override
		public void run() {
			try {
				this.curMethod.invoke(null, this.args);
			}
			catch (Throwable th) {
				if (th.getCause() != null) {
					th.getCause().printStackTrace();
				} else {
					th.printStackTrace();
				}
			}
			Handler uiHandler = RunJavaActivity.getUiHandler(this.runJavaActivity);
			uiHandler.post(new SyncRunnable(this.runJavaActivity));
		}
	}

	public static class SyncRunnable implements Runnable {
		final RunJavaActivity runJavaActivity;

		public SyncRunnable(RunJavaActivity runJavaActivity) {
			this.runJavaActivity = runJavaActivity;
		}

		@Override
		public void run() {
			RunJavaActivity.j3(this.runJavaActivity);
		}

	}
}

