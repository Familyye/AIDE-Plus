package io.github.zeroaicy.aide.utils;


import com.aide.common.AppLog;
import com.aide.ui.util.BuildGradle;
import com.aide.ui.util.Configuration;
import com.aide.ui.util.FileSystem;
import groovyjarjarantlr.collections.AST;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import io.github.zeroaicy.util.Log;
import java.util.Collections;
import com.aide.ui.services.AssetInstallationService;
import java.util.Set;
import java.util.HashSet;

public class ZeroAicyBuildGradle extends BuildGradle {

	private static ZeroAicyBuildGradle singleton;


	private boolean minifyEnabled;
	private boolean shrinkResources;
	private List<String> proguardFiles;

	public boolean isMinifyEnabled() {
		return this.minifyEnabled;
	}

	public boolean isShrinkResources() {
		return this.shrinkResources;
	}
	public List<String> getProguardFiles() {
		if (this.proguardFiles == null) {
			return Collections.emptyList();
		}
		return this.proguardFiles;
	}

	public static ZeroAicyBuildGradle getSingleton() {
		if (singleton == null) {
			singleton = new ZeroAicyBuildGradle();
			Log.d("ZeroAicyBuildGradle",  "替换gradle解析器");
		}
		return singleton;
	}

	@Override
	public ZeroAicyBuildGradle getConfiguration(String path) {
		return (ZeroAicyBuildGradle)super.getConfiguration(path);
	}
    public ZeroAicyBuildGradle makeConfiguration(String path) {
		return new ZeroAicyBuildGradle(path);
    }

	public ZeroAicyBuildGradle() {
		init();
    }



	public ZeroAicyBuildGradle(String filePath) {
		// getConfiguration在调用 makeConfiguration后会赋值
		// 导致解析时无法使用此变量, 赋值
		this.configurationPath = filePath;
		init();

		try {
			FileReader fileReader = new FileReader(filePath);
			SourceBuffer sourceBuffer = new SourceBuffer();
			UnicodeEscapingReader unicodeEscapingReader = new UnicodeEscapingReader(fileReader, sourceBuffer);
			GroovyLexer groovyLexer = new GroovyLexer(unicodeEscapingReader);
			unicodeEscapingReader.setLexer(groovyLexer);

			GroovyRecognizer groovyRecognizer = GroovyRecognizer.make(groovyLexer);
			groovyRecognizer.setSourceBuffer(sourceBuffer);
			groovyRecognizer.compilationUnit();
			fileReader.close();

			for (AST ast = groovyRecognizer.getAST(); ast != null; ast = getNextSibling(ast)) {
				nw(ast, "");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }
	private void init() {
		this.defaultConfigProductFlavor = new BuildGradle.ProductFlavor();
		this.curAndroidNodeLine = -1;
		this.curFlavorsNodeLine = -1;
		this.productFlavorMap = new TreeMap<>();
		this.curProjectsRepositorys = new ArrayList<>();
		this.allProjectsRepositorys = new ArrayList<>();
		this.subProjectsRepositorys = new ArrayList<>();
		this.curDependenciesNodeLine = -1;
		this.dependencies = new ArrayList<>();
		this.subProjectsDependencies = new ArrayList<>();
		this.allProjectsDependencies = new ArrayList<>();
		this.signingConfigMap = new HashMap<>();
	}

	// 是否是 parentNodeName的子节点
    private boolean isChildNode(String nodeName, String parentNodeName) {
		return nodeName.startsWith(parentNodeName + ".");
    }

    private String EQ(AST ast, String nodeName) {
		AST XL = getNextSibling(getFirstChild(getFirstChild(ast)));
		if (getType(XL) == 33 
			&& 
			getType(getFirstChild(XL)) == 27 
			&& nodeName.equals(getText(getFirstChild(getFirstChild(XL))))) {
			AST Ws = getFirstChild(getNextSibling(getFirstChild(getFirstChild(XL))));
			if (getType(Ws) == 28) {
				return getText(getFirstChild(Ws));
			}
			return null;
		}
		return null;
    }

    private void FH(String str, String str2, int i) {
		try {
			ArrayList<String> arrayList = new ArrayList<>();
			BufferedReader bufferedReader = new BufferedReader(new FileReader(((Configuration) this).configurationPath));
			int i2 = 1;
			while (true) {
				String readLine = bufferedReader.readLine();
				if (readLine == null) {
					break;
				}
				arrayList.add(readLine);
				if (i2 == i) {
					arrayList.add(str);
				}
				i2++;
			}
			if (i < 0) {
				arrayList.add(str2 + " {");
				arrayList.add(str);
				arrayList.add("}");
			}
			bufferedReader.close();
			FileWriter fileWriter = new FileWriter(((Configuration) this).configurationPath);
			for (String str3 : arrayList) {
				fileWriter.write(str3);
				fileWriter.write("\n");
			}
			fileWriter.close();
		}
		catch (IOException e) {
			AppLog.v5(e);
		}
    }

    private void Hw(String str) {
		FH("\t" + str, "dependencies", this.curDependenciesNodeLine);
    }

    private String J0(AST ast) {
		if (getType(ast) == 28) {
			if (getType(getFirstChild(ast)) == 27 || getType(getFirstChild(ast)) == 124) {
				return aM(getFirstChild(getFirstChild(ast)));
			}
			return null;
		}
		return null;
    }

    private String getAstValue(AST ast) {
		AST XL = getNextSibling(getFirstChild(getFirstChild(ast)));
		if (getType(XL) != 88 && getType(XL) != 199) {
			if (getType(XL) == 33) {
				if (getType(getFirstChild(XL)) == 28) {
					return getText(getFirstChild(getFirstChild(XL)));
				}
				return getText(getFirstChild(XL));
			}
			return null;
		}
		return getText(XL);
    }

    private void parserRepositories(AST ast, String repositorieName, List<Repository> repositorys) {
		// google
		repositorys.add(new RemoteRepository(ast.getLine(), "https://dl.google.com/dl/android/maven2"));
		switch (repositorieName) {
			case "jcenter":
				repositorys.add(new RemoteRepository(ast.getLine(), "https://jcenter.bintray.com"));
				break;
			case "mavenCentral":
				repositorys.add(new RemoteRepository(ast.getLine(), "http://repo.maven.apache.org/maven2"));
				break;
			case "maven.url":
				repositorys.add(new RemoteRepository(ast.getLine(), getAstValue(ast)));
				break;
			case "flatDir.dirs":
				FlatLocalRepository flatLocalRepository = new FlatLocalRepository(ast.getLine());
				flatLocalRepository.flatDir = getAstValue(ast);
				repositorys.add(flatLocalRepository);
				break;
			default: 
				return;
		}
    }

    private String Mr(String str, int i) {
		String[] split = str.split("\\.");
		if (split.length > i) {
			String str2 = "";
			for (int i2 = i; i2 < split.length; i2++) {
				if (str2.length() > 0) {
					str2 = str2 + ".";
				}
				str2 = str2 + split[i2];
			}
			return str2;
		}
		return null;

    }

    private void parserProductFlavor(AST ast, String str, ProductFlavor productFlavor) {
		switch (str) {
			case "minSdkVersion":
			case "minSdkVersion.apiLevel":
				productFlavor.minSdkVersion = getAstValue(ast);
				break;
			case "targetSdkVersion":
			case "targetSdkVersion.apiLevel":
				productFlavor.targetSdkVersion = getAstValue(ast);
				break;
			case "versionCode":
				productFlavor.versionCode = getAstValue(ast);
				break;
			case "versionName":
				productFlavor.versionName = getAstValue(ast);
				break;
			case "packageName":
			case "applicationId":
				productFlavor.applicationId = getAstValue(ast);
				break;
			case "multiDexEnabled":
				productFlavor.multiDexEnabled = getAstValue(ast);
				break;
		}

    }

    private void SI(AST ast, String str, int i) {
		String j32 = getNodeSimpleNameAt(str, i);
		if (!this.productFlavorMap.containsKey(j32)) {
			this.productFlavorMap.put(j32, new ProductFlavor());
		}
		String Mr2 = Mr(str, i + 1);
		if (Mr2 != null) {
			parserProductFlavor(ast, Mr2, this.productFlavorMap.get(j32));
		}

    }

    private static AST getFirstChild(AST ast) {
		if (ast == null) {
			return null;
		}
		return ast.getFirstChild();

    }

    private static AST getNextSibling(AST ast) {
		if (ast == null) {
			return null;
		}
		return ast.getNextSibling();

    }

    private String aM(AST ast) {

		if (getType(ast) == 90) {
			AST Ws = getFirstChild(ast);
			AST XL = getNextSibling(Ws);
			return aM(Ws) + "." + getText(XL);
		}
		return getText(ast);
    }

    private void cn(AST ast, String str, SigningConfig signingConfig) {
		switch (str) {
			case "storePassword":
				signingConfig.storePassword = getAstValue(ast);
				break;
			case "keyPassword":
				signingConfig.keyPassword = getAstValue(ast);
				break;
			case "keyAlias":
				signingConfig.keyAlias = getAstValue(ast);
				break;
			case "storeFile":
				signingConfig.storeFilePath = EQ(ast, "file");
				break;
		}
    }

    private void ei(AST ast, String str, List<Dependency> list) {
        Map<String, String> we;

		switch (str) {
			case "testCompile":
			case "androidTestCompile":
				list.add(new k(ast.getLine()));
				break;
			case "wearApp":
				String EQ = EQ(ast, "project");
				if (EQ == null && (we = we(ast, "project")) != null && we.containsKey("path")) {
					EQ = we.get("path");
				}
				if (EQ != null) {
					ProjectDependency projectDependency = new ProjectDependency(ast.getLine());
					this.wearAppProject = projectDependency;
					projectDependency.projectName = EQ;
					return;
				}
				list.add(new l(ast.getLine()));

				break;
			case "implementation":
			case "api":
			case "compile":
				{
					String EQ2 = EQ(ast, "project");
					if (EQ2 != null) {
						ProjectDependency projectDependency2 = new ProjectDependency(ast.getLine());
						list.add(projectDependency2);
						projectDependency2.projectName = EQ2;
						return;
					}
					String EQ3 = EQ(ast, "files");
					if (EQ3 != null) {
						FilesDependency filesDependency = new FilesDependency(ast.getLine());
						list.add(filesDependency);
						filesDependency.filesPath = EQ3;
						return;
					}
					Map<String, String> we2 = we(ast, "fileTree");
					if (we2 != null) {
						FileTreeDependency fileTreeDependency = new FileTreeDependency(ast.getLine());
						list.add(fileTreeDependency);
						fileTreeDependency.dirPath = we2.get("dir");
						we2.get("include");
						return;
					}
					String J8 = getAstValue(ast);
					if (J8 != null) {
						MavenDependency mavenDependency = new MavenDependency(ast.getLine());
						list.add(mavenDependency);
						mavenDependency.coords = J8;
						String[] split = J8.split(":");
						if (split.length > 0) {
							mavenDependency.groupId = split[0];
						}
						if (split.length > 1) {
							mavenDependency.artifactId = split[1];
						}
						if (split.length > 2) {
							String str2 = split[2];
							if (str2.indexOf("@") >= 0) {
								mavenDependency.version = str2.substring(0, str2.indexOf("@"));
								mavenDependency.packaging = str2.substring(str2.indexOf("@") + 1);
								return;
							}
							mavenDependency.version = str2;
							return;
						}
						return;
					}
					list.add(new l(ast.getLine()));
				}
				break;

			default: 
				list.add(new l(ast.getLine()));
				break;
		}
    }

    private String getNodeSimpleNameAt(String str, int index) {
		String[] split = str.split("\\.");
		if (split.length > index) {
			return split[index];
		}
		return null;

    }

    private static String getText(AST ast) {
		if (ast == null) {
			return null;
		}
		return ast.getText();

    }

    private void nw(AST ast, String parentNodeName) {
		String nodeName = J0(ast);
		if (nodeName == null) {
			return;
		}
		if (parentNodeName.length() != 0) {
			nodeName = parentNodeName + "." + nodeName;
		}
		//System.out.printf("解析%s\n", nodeName);

		switch (nodeName) {
			case "android":
			case "model.android":
				this.curAndroidNodeLine = tp(ast);
				break;

			case "android.productFlavors":
			case "model.android.productFlavors":
				this.curFlavorsNodeLine = tp(ast);
				break;
			case "dependencies":
				this.curDependenciesNodeLine = tp(ast);
				break;
			case "android.compileSdkVersion":
			case "model.android.compileSdkVersion":
				getAstValue(ast);
				break;
			default:
				if (isChildNode(nodeName, "android.defaultConfig")) {
					parserProductFlavor(ast, Mr(nodeName, 2), this.defaultConfigProductFlavor);
					break;
				} else if (isChildNode(nodeName, "model.android.defaultConfig")) {
					parserProductFlavor(ast, Mr(nodeName, 3), this.defaultConfigProductFlavor);
					break;
				} else if (isChildNode(nodeName, "model.android.defaultConfig.with")) {
					parserProductFlavor(ast, Mr(nodeName, 4), this.defaultConfigProductFlavor);
					break;
				} else if (isChildNode(nodeName, "android.productFlavors")) {
					SI(ast, nodeName, 2);
					break;
				} else if (isChildNode(nodeName, "model.android.productFlavors")) {
					SI(ast, nodeName, 3);
					break;
				} else if (isChildNode(nodeName, "android.signingConfigs")) {
					ro(ast, nodeName, 2);
					break;
				} else if (isChildNode(nodeName, "model.android.signingConfigs")) {
					ro(ast, nodeName, 3);
					break;
				} else if (isChildNode(nodeName, "dependencies")) {
					ei(ast, Mr(nodeName, 1), this.dependencies);
					break;
				} else if (isChildNode(nodeName, "subprojects.dependencies")) {
					ei(ast, Mr(nodeName, 2), this.subProjectsDependencies);
					break;
				} else if (isChildNode(nodeName, "allprojects.dependencies")) {
					ei(ast, Mr(nodeName, 2), this.allProjectsDependencies);
					break;
				} else if (isChildNode(nodeName, "repositories")) {
					parserRepositories(ast, Mr(nodeName, 1), this.curProjectsRepositorys);
					break;
				} else if (isChildNode(nodeName, "subprojects.repositories")) {
					parserRepositories(ast, Mr(nodeName, 2), this.subProjectsRepositorys);
					break;
				} else if (isChildNode(nodeName, "allprojects.repositories")) {
					parserRepositories(ast, Mr(nodeName, 2), this.allProjectsRepositorys);
					break;
				} else if (isChildNode(nodeName, "android.buildTypes.release")) {
					int i = 3;
					String nodeSimpleName = getNodeSimpleNameAt(nodeName, i);
					if ("minifyEnabled".equals(nodeSimpleName)) {
						this.minifyEnabled = "true".equals(getAstValue(ast));
						break;
					} 
					if ("shrinkResources".equals(nodeSimpleName)) {
						this.shrinkResources = "true".equals(getAstValue(ast));
						break;
					} 
					if (this.proguardFiles == null 
						&& "proguardFiles".equals(nodeSimpleName)) {

						AST nextSibling = getNextSibling(getFirstChild(getFirstChild(ast)));
						if (getType(nextSibling) != 33) {
							break;
						}

						List<String> proguardFiles = new ArrayList<>();

						for (AST firstChild1 = getFirstChild(nextSibling); firstChild1 != null; firstChild1 = getNextSibling(firstChild1)) {
							if (getType(firstChild1) == 88) {
								String proguardFilePath = FileSystem.Qq(FileSystem.getParent(this.configurationPath), getText(firstChild1));
								proguardFiles.add(proguardFilePath);
								continue;
							}
							if (getType(firstChild1) == 27) {
								AST firstChild2 = getFirstChild(firstChild1);

								if (getType(firstChild2) == 87 
									&& "getDefaultProguardFile".equals(getText(firstChild2))) {
									
									AST firstChild4 = getFirstChild(getFirstChild(getNextSibling(firstChild2)));
									String defaultProguardFile = getDefaultProguardFile(getText(firstChild4));
									proguardFiles.add(defaultProguardFile);
								}
							}
						}
						if (! proguardFiles.isEmpty()) {
							this.proguardFiles = proguardFiles;
						}
					}

					break;
				}
				break;
		}

		for (AST ast2 : u7(ast)) {
			nw(ast2, nodeName);
		}
	}

	private static Set<String> proguards = new HashSet<>();
	static{
		proguards.add("proguard-android.txt");
		proguards.add("proguard-android-optimize.txt");
		proguards.add("proguard-defaults.txt");
	}
	private String getDefaultProguardFile(String proguardFileName) {

		if (proguards.contains(proguardFileName)) {
			return AssetInstallationService.DW(proguardFileName, true);
		}
		return null;
	}


    private static int getType(AST ast) {
		if (ast == null) {
			return 0;
		}
		return ast.getType();

    }

    private void ro(AST ast, String str, int i) {
		String j32 = getNodeSimpleNameAt(str, i);
		if (!this.signingConfigMap.containsKey(j32)) {
			this.signingConfigMap.put(j32, new SigningConfig());
		}
		String Mr2 = Mr(str, i + 1);
		if (Mr2 != null) {
			cn(ast, Mr2, this.signingConfigMap.get(j32));
		}

    }

    private int tp(AST ast) {
		AST XL = getNextSibling(getFirstChild(getFirstChild(ast)));
		return XL == null ? ast.getLine() : XL.getLine();

    }

    private List<AST> u7(AST ast) {
		ArrayList<AST> arrayList = new ArrayList<>();
		for (AST Ws = getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast)))); Ws != null; Ws = getNextSibling(Ws)) {
			if (getType(Ws) == 28) {
				arrayList.add(Ws);
			}
		}
		return arrayList;

    }

    private Map<String, String> we(AST ast, String str) {
		AST XL = getNextSibling(getFirstChild(getFirstChild(ast)));
		if (getType(XL) == 33 && getType(getFirstChild(XL)) == 27 && str.equals(getText(getFirstChild(getFirstChild(XL))))) {
			HashMap<String, String> hashMap = new HashMap<>();
			for (AST Ws = getFirstChild(getNextSibling(getFirstChild(getFirstChild(XL)))); Ws != null; Ws = getNextSibling(Ws)) {
				if (getType(Ws) == 54) {
					String lg = getText(getFirstChild(Ws));
					AST Ws2 = getFirstChild(getNextSibling(getFirstChild(Ws)));
					if (getType(Ws2) == 57) {
						hashMap.put(lg, getText(getFirstChild(getFirstChild(getFirstChild(Ws2)))));
					} else {
						hashMap.put(lg, getText(Ws2));
					}
				}
			}
			return hashMap;
		}
		return null;

    }

    public void addMavenDependency(String str) {
		Hw("api '" + str + "'");

    }

    public void addProductFlavor(String str) {
		String str2 = "\t\t" + str + " {\n\t\t}";
		if (this.curFlavorsNodeLine != -1) {
			FH(str2, "", this.curFlavorsNodeLine);
			return;
		}
		FH("\tproductFlavors {\n" + str2 + "\n\t}\n", "android", this.curAndroidNodeLine);

    }

    public void addProjectDependency(String str) {
		String BT = FileSystem.BT(FileSystem.getParent(FileSystem.getParent(((Configuration) this).configurationPath)), str);
		Hw("api project('" + (":" + BT.replace("/", ":")) + "')");

    }

    public String getFlavorApplicationId(String str) {
		if (str != null && this.productFlavorMap.containsKey(str) && this.productFlavorMap.get(str).applicationId != null) {
			return this.productFlavorMap.get(str).applicationId;
		}
		return this.defaultConfigProductFlavor.applicationId;

    }

    public String getMinSdkVersion(String productFlavorName) {
		if (productFlavorName != null && this.productFlavorMap.containsKey(productFlavorName) && this.productFlavorMap.get(productFlavorName).minSdkVersion != null) {
			return this.productFlavorMap.get(productFlavorName).minSdkVersion;
		}
		return this.defaultConfigProductFlavor.minSdkVersion;

    }

    public SigningConfig getSigningConfig(String str) {
		if (str == null) {
			return null;
		}
		return this.signingConfigMap.get(str);

    }

    public String getTargetSdkVersion(String str) {
		if (str != null 
			&& this.productFlavorMap.containsKey(str) 
			&& this.productFlavorMap.get(str).targetSdkVersion != null) {
			return this.productFlavorMap.get(str).targetSdkVersion;
		}
		return this.defaultConfigProductFlavor.targetSdkVersion;

    }

    public String getVersionCode(String str) {
		if (str != null
			&& this.productFlavorMap.containsKey(str) 
			&& this.productFlavorMap.get(str).versionCode != null) {
			return this.productFlavorMap.get(str).versionCode;
		}
		return this.defaultConfigProductFlavor.versionCode;

    }

    public String getVersionName(String str) {
		if (str != null 
			&& this.productFlavorMap.containsKey(str) 
			&& this.productFlavorMap.get(str).versionName != null) {
			return this.productFlavorMap.get(str).versionName;
		}
		return this.defaultConfigProductFlavor.versionName;
    }

    public boolean isMultiDexEnabled(String str) {
		if (str != null 
			&& this.productFlavorMap.containsKey(str) 
			&& this.productFlavorMap.get(str).multiDexEnabled != null) {
			return "true".equals(this.productFlavorMap.get(str).multiDexEnabled);
		}
		return "true".equals(this.defaultConfigProductFlavor.multiDexEnabled);
	}


}

