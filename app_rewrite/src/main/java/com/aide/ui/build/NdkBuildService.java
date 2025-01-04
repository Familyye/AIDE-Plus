/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/

//
// Decompiled by Jadx - 1257ms
//
package com.aide.ui.build;

import abcd.hy;
import abcd.wf;
import abcd.xf;
import android.os.Build;
import com.aide.common.AppLog;
import com.aide.common.StreamUtilities;
import com.aide.engine.SyntaxError;
import com.aide.ui.AppPreferences;
import com.aide.ui.ServiceContainer;
import com.aide.ui.build.android.NdkConfiguration;
import com.aide.ui.build.android.g;
import com.aide.ui.util.FileSystem;
import io.github.zeroaicy.aide.extend.ZeroAicyExtensionInterface;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import androidx.annotation.Keep;
import com.aide.ui.services.ProjectService;

public class NdkBuildService {

	private RunNdkBuildFutureTask DW;

	private g FH;

	private final ExecutorService executorService;

	public NdkBuildService() {
		this.executorService = ZeroAicyExtensionInterface.getProjectExecutorService();
	}

	static void DW(NdkBuildService ndkBuildService, Map<String, List<SyntaxError>> map) {
		ndkBuildService.we(map);
	}

	private void EQ() {
		if (this.FH != null) {
			this.FH.J0();
		}
	}

	static void FH(NdkBuildService ndkBuildService) {
		ndkBuildService.EQ();
	}

	static void Hw(NdkBuildService ndkBuildService, Throwable th) {
		ndkBuildService.tp(th);
	}

	private SyntaxError VH(String str, int i, int i2, String str2) {
		SyntaxError syntaxError = new SyntaxError();
		syntaxError.jw = i;
		syntaxError.fY = i2;
		syntaxError.qp = i;
		syntaxError.k2 = 1000;
		syntaxError.zh = str + ": " + str2;
		return syntaxError;
	}

	static Map<String, List<SyntaxError>> Zo(NdkBuildService ndkBuildService, String str, String str2) {
		return ndkBuildService.gn(str, str2);
	}

	private Map<String, List<SyntaxError>> gn(String str, String str2) {
		try {
			HashMap<String, List<SyntaxError>> hashMap = new HashMap<>();

			for (String errorLine : str2.split("\n")) {
				errorLine = errorLine.trim();

				if (errorLine.length() <= 0) {
					continue;
				}
				try {
					int indexOf = errorLine.indexOf(':');

					if (indexOf > 0) {
						String path = new File(str, errorLine.substring(0, indexOf)).getPath();

						if (FileSystem.KD(path)) {
							int i2 = indexOf + 1;
							int indexOf2 = errorLine.indexOf(58, i2);
							if (indexOf2 < 0) {
								indexOf2 = errorLine.indexOf(32, i2);
							}
							if (indexOf2 > 0) {
								int i;

								try {
									i = Integer.parseInt(errorLine.substring(i2, indexOf2));
								} catch (NumberFormatException unused) {
									i = 1;
								}
								int i3 = indexOf2 + 1;
								int indexOf3 = errorLine.indexOf(58, i3);
								if (indexOf3 > 0) {
									try {
										Integer.parseInt(errorLine.substring(i3, indexOf3));
									} catch (NumberFormatException unused2) {
									}
								}
								String trim2 = errorLine.substring(indexOf3 + 1, errorLine.length()).trim();
								if (trim2.startsWith("error:")) {
									SyntaxError VH = VH("NDK", i, 1, trim2.substring(6, trim2.length()).trim());
									if (!hashMap.containsKey(path)) {
										hashMap.put(path, new ArrayList<SyntaxError>());
									}
									hashMap.get(path).add(VH);
								}
							}
						}
					}
				} catch (Exception e) {
					AppLog.e(e);
				}

				if (!hashMap.containsKey(str)) {
					hashMap.put(str, new ArrayList<>());
				}
				hashMap.get(str).add(VH("NDK", 1, 1, errorLine));

			}
			return hashMap;
		} catch (Throwable th) {
			throw th;
		}
	}

	static void j6(NdkBuildService ndkBuildService, boolean z) {
		ndkBuildService.u7(z);
	}

	private void tp(Throwable th) {
		AppLog.e(th);
		if (this.FH != null) {
			this.FH.g3();
		}
	}

	private void u7(boolean z) {
		if (this.FH != null) {
			this.FH.vJ(z);
		}

	}

	static SyntaxError makeSyntaxError(NdkBuildService ndkBuildService, String str, int i, int i2, String str2) {
		return ndkBuildService.VH(str, i, i2, str2);
	}

	private void we(Map<String, List<SyntaxError>> map) {
		if (this.FH != null) {
			this.FH.Mz(map);
		}
	}

	@Keep
	public void J0(boolean z) {
		if (this.DW != null) {
			this.DW.cancel(true);
			this.DW = null;
		}
		RunNdkBuildFutureTask runNdkBuildFutureTask = new RunNdkBuildFutureTask(this, new RunNdkBuildCallable(this, z,
				AppPreferences.isNativeBuildParallel(), ServiceContainer.getProjectService().P8()));

		this.DW = runNdkBuildFutureTask;
		this.executorService.execute(runNdkBuildFutureTask);

	}

	@Keep
	public void J8(g gVar) {
		this.FH = gVar;
	}

	static class RunNdkBuildFutureTask extends FutureTask<Map<String, List<SyntaxError>>> {

		private RunNdkBuildCallable WB;

		@hy
		final NdkBuildService mb;

		public RunNdkBuildFutureTask(NdkBuildService ndkBuildService, RunNdkBuildCallable runNdkBuildCallable) {
			super(runNdkBuildCallable);
			this.mb = ndkBuildService;
			this.WB = runNdkBuildCallable;
		}

		@Override
		protected void done() {
			if (isCancelled()) {
				return;
			}
			boolean z = false;
			try {
				Iterator<String> it = this.WB.modules.iterator();
				while (it.hasNext()) {
					if (ServiceContainer.getProjectService().g3((String) it.next())) {
						z = true;
					}
				}
				Map<String, List<SyntaxError>> errors = get();
				if (errors == null) {
					NdkBuildService.j6(this.mb, z);
				} else {
					NdkBuildService.DW(this.mb, errors);
				}
			} catch (InterruptedException unused) {
				NdkBuildService.FH(this.mb);
			} catch (ExecutionException e) {
				NdkBuildService.Hw(this.mb, e.getCause());
			}
		}
	}

	static class RunNdkBuildCallable implements Callable<Map<String, List<SyntaxError>>> {

		private final boolean isNativeBuildParallel;

		public final List<String> modules;

		@hy
		final NdkBuildService ndkBuildService;

		private final boolean isClean;

		public RunNdkBuildCallable(NdkBuildService ndkBuildService, boolean isClean, boolean isNativeBuildParallel,
				List<String> modules) {
			this.ndkBuildService = ndkBuildService;
			this.isClean = isClean;

			this.isNativeBuildParallel = isNativeBuildParallel;
			this.modules = modules;
		}

		private String DW(byte[] data, int i) {
			String str = "";
			try {
				str = StreamUtilities.readTextReader(new InputStreamReader(new ByteArrayInputStream(data)));
			} catch (Exception unused) {
			}

			String trim = str.trim();
			if (trim.length() != 0) {
				return trim;
			}
			return "ndk-build exited with code " + i;

		}

		// has Android Mk Module
		private boolean hasAndroidMkModule() {

			ProjectService projectService = ServiceContainer.getProjectService();
			for (String module : this.modules) {
				// is Android Mk Module
				if (projectService.g3(module)) {
					return true;
				}
			}
			return false;
		}

		private void Hw(List<String> list, String str) {
			StringBuilder sb = new StringBuilder();

			sb.append("Running ndk-build [" + str + "] ");

			for (int i = 0; i < list.size(); i++) {
				sb.append('\"');
				sb.append(list.get(i));
				sb.append('\"');
				if (i != list.size() - 1) {
					sb.append(" ");
				}
			}
			AppLog.d(sb.toString());
		}

		private Map<String, List<SyntaxError>> runNdkBuild(String str, boolean isNativeBuildParallel) {

			ProjectService projectService = ServiceContainer.getProjectService();

			for (String module : this.modules) {
				// hasAndroidMkModule
				if (projectService.g3(module)) {

					List<String> ndkBuildArgs = NdkConfiguration.VH(str, isNativeBuildParallel ? 4 : 1);

					//  只有PATH
					Map<String, String> env = NdkConfiguration.gn();

					Hw(ndkBuildArgs, module);

					// 运行ndk-build
					wf j6 = xf.j6(ndkBuildArgs, module, env, true, (OutputStream) null, (byte[]) null);
					if (j6.DW() != 0) {
						return NdkBuildService.Zo(this.ndkBuildService, module, DW(j6.j6(), j6.DW()));
					}
				}
			}

			return null;
		}

		@Override
		public Map<String, List<SyntaxError>> call() {
			
			
			
			
			
			// 所有module没有 Android.mk项目
			if (!hasAndroidMkModule()) {
				return null;
			}

			// 没有安装Ndk
			if (!NdkConfiguration.isInstalledNdk()) {
				HashMap<String, List<SyntaxError>> hashMap = new HashMap<>();
				String module = this.modules.get(0);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ServiceContainer.isX86()) {

					hashMap.put(module, new ArrayList<SyntaxError>());

					SyntaxError makeSyntaxError = NdkBuildService.makeSyntaxError(this.ndkBuildService, "NDK", 1, 1,
							"Native development is not supported on X86 devices running Android 10 and above.");
					SyntaxError syntaxError = makeSyntaxError;

					hashMap.get(module).add(syntaxError);
				} else {

					hashMap.put(module, new ArrayList<SyntaxError>());

					SyntaxError makeSyntaxError = NdkBuildService.makeSyntaxError(this.ndkBuildService, "NDK", 1, 1,
							"NDK support not installed.");
					hashMap.get(module).add(makeSyntaxError);
				}
				return hashMap;
			}

			// busybox适配 从com.aide.ndk29创建软连接到 PATH
			// NdkConfiguration.U2();

			if (this.isClean) {
				Map<String, List<SyntaxError>> syntaxErrors = runNdkBuild("clean", false);
				if (syntaxErrors != null) {
					return syntaxErrors;
				}
			}

			long currentTimeMillis = System.currentTimeMillis();

			Map<String, List<SyntaxError>> v52 = runNdkBuild(null, this.isNativeBuildParallel);

			AppLog.d("NDK build elapsed " + (System.currentTimeMillis() - currentTimeMillis) + "ms");

			return v52;

		}
	}
}

