package io.github.zeroaicy.aide;

import java.util.*;


//Keep
public class CompileOnlyJar {
	//Keep
	//Lcom/aide/ui/build/java/h;->we()V
	//修改Labcd/lj;->Mr(Ljava/lang/String;)[Ljava/lang/String;返回值
	/**
	 #v9为Labcd/lj;->Mr(Ljava/lang/String;)[Ljava/lang/String;返回值
	 invoke-static {v9}, Lio/github/zeroaicy/aide/CompileOnlyJar;->filterCompileOnlyJar([Ljava/lang/String;)[Ljava/lang/String;
	 move-result-object v9
	 */

	public static String FilterCompileOnlyJar  = "_compileonly.jar";

    public static String[] filterCompileOnlyJar(String[] libPaths) {
        if (libPaths == null) return libPaths;
        ArrayList<String> libsList = new ArrayList<String>(Arrays.asList(libPaths));
        filterCompileOnlyJar(libsList);
        return libsList.toArray(new String[libsList.size()]);
    }
	
	/**
	 * 兼容AIDE Pro的Proguard混淆
	 */
	//返回所有compileOnlyJar
	public static void addProguardJAVAibraryjars(List<String> proguardArgs, String[] libPaths) {
		if( proguardArgs == null || proguardArgs.isEmpty()) return;
		for (String jarPath : getCompileOnlyJar(libPaths)) {
			proguardArgs.add("-libraryjars");
			proguardArgs.add(jarPath);
		}
	}

	public static List<String> getCompileOnlyJar(String[] libPaths) {
        List<String> libsList = new ArrayList<String>();
        if (libPaths == null) return libsList;
		for (String jarPath : libPaths) {
			if (jarPath == null) continue;
			if (jarPath.toLowerCase().endsWith(FilterCompileOnlyJar)) {
				libsList.add(jarPath);
			}
		}
        return libsList;
    }


	//Keep
	//gj->j6(Ljava/util/Map;)[Ljava/lang/String;
	/*
	 invoke-static {v3}, Lio/github/zeroaicy/aide/CompileOnlyJar;->filterCompileOnlyJar(Ljava/util/HashMap;)V
	 #在[ invoke-interface {v3}, Ljava/util/Map;->size()I ] 之前调用
	 */
	public static void filterCompileOnlyJar(HashMap<String, String> hashMap) {
		try {
            if (hashMap == null) return;
			for (Map.Entry<String, String> entry : new HashSet<Map.Entry<String, String>>(hashMap.entrySet())) {
				String value = entry.getValue();
				if (value == null) continue;

				if (value.toLowerCase().endsWith(FilterCompileOnlyJar)) {
					String remove = hashMap.remove(entry.getKey());
				}
			}
		} catch (Throwable e) {
		}
    }
	//
	public static void filterCompileOnlyJar(List<String> list) {
        try {
			filterCompileOnlyJar(list, FilterCompileOnlyJar);
        } catch (Throwable e) {
			
        }
    }
    public static void filterCompileOnlyJar(List<String> list, String suffix) {
        try {
            for (int i = list.size() - 1; i >= 0; i--) {
                String jarPath = list.get(i);
				if (jarPath == null) continue;

                if (jarPath.toLowerCase().endsWith(suffix)) {
                    list.remove(i);
                }
            }
        } catch (Throwable e) {
        }
    }
}
