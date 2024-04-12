//
// Decompiled by Jadx - 1059ms
//
package com.aide.ui.util;

import com.aide.ui.util.BuildGradle;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import io.github.zeroaicy.util.Log;
import java.io.FileNotFoundException;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.Model;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.IOException;
import com.aide.ui.util.BuildGradle.MavenDependency;
import org.apache.maven.model.Exclusion;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import com.aide.ui.util.PomXml.ArtifactNode;
import java.util.Map;
import java.util.HashMap;
import org.apache.maven.model.Parent;

public class PomXml extends Configuration<PomXml> {



	public static class ArtifactNode extends BuildGradle.MavenDependency {
		// 依赖排除
		public static ArtifactNode pack(BuildGradle.MavenDependency dep) {
			if (dep instanceof ArtifactNode) {
				return (ArtifactNode)dep;
			}
			return new ArtifactNode(dep, dep.version);
		}

		private List<Exclusion> exclusions;
		public ArtifactNode(BuildGradle.MavenDependency dep, String version) {
			super(dep, version);
			if (dep instanceof ArtifactNode) {
				// 直接用字段，getExclusions可能返回emptyList
				setExclusions(((ArtifactNode)dep).exclusions);
			}
			// 保留 packaging
			this.packaging = dep.packaging;
		}
		
		public ArtifactNode() {
			super(1);
		}
		public ArtifactNode(PomXml pom) {
			this();
			this.groupId = pom.group;
			this.artifactId = pom.artifact;
			this.version = pom.curVersion;
			// 从pom解析出来的
			this.packaging = pom.getPackaging();
		}

		public void setExclusions(List<Exclusion> exclusions) {
			this.exclusions = exclusions;
		}

		public List<Exclusion> getExclusions() {
			if (this.exclusions == null) {
				return Collections.emptyList();
			}
			return this.exclusions;
		}

		public Set<String> getExclusionSet() {
			Set<String> exclusionSet = new HashSet<>();
			for (Exclusion exclusion : getExclusions()) {
				exclusionSet.add(exclusion.getGroupId() + ":" + exclusion.getArtifactId());
			}
			return exclusionSet;
		}

	}
    public  static PomXml empty = new PomXml();

	String group = "";
	String artifact = "";
	String curVersion = "";

	String packaging = null;

	public PomXml() {
		this.deps = null;
		this.depManages = null;

	}

	public void setPackaging(String packaging) {
		if ("bundle".equals(packaging)) {
			this.packaging = "jar";
		} else {
			this.packaging = packaging;
		}
	}
	public String getPackaging() {
		return this.packaging;
	}

    public PomXml makeConfiguration(String str) {
        try {
            return new PomXml(str);
        }
		catch (Throwable th) {
            throw new Error(th);
        }
    }

	public String getGroupIdArtifactId() {
		return this.group + ":" + this.artifact;		
	}
	@Override
	public String toString() {
		return this.group + ":" + this.artifact + ":" + this.curVersion;
	}

	//子依赖
	//将一分为二
	public final List<ArtifactNode> deps;

	public final List<ArtifactNode> depManages;


	/**
	 * 新版解析器
	 */
	private static final MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();

    private PomXml(String filePath) {

		this.deps = new ArrayList<>();
		this.depManages = new ArrayList<>();

		try {
			File file = new File(filePath);
			if (!file.exists()) {
				return;
			}
			FileInputStream inputStream = new FileInputStream(file);
			// pom文件模型
			Model model = mavenXpp3Reader.read(inputStream);
			inputStream.close();

			this.group = model.getGroupId();

			this.artifact = model.getArtifactId();
			this.curVersion = model.getVersion();
			this.setPackaging(model.getPackaging());

			Parent parent = model.getParent();
			if (this.group == null) {
				if (parent != null) {
					this.group = parent.getGroupId();
				}
			}

			init(model);
		}
		catch (Throwable e) {
			Log.d(e.getMessage(), e);
		}

		//Log.d(toString(), deps.toArray());
		//System.out.println();

	}

	private void init(Model model) {

		DependencyManagement depManagement = model.getDependencyManagement();
		if (depManagement != null) {
			// 版本统一
			for (Dependency dep : depManagement.getDependencies()) {
				String scope = dep.getScope();

				ArtifactNode artifactNode = make(model, dep);

				if (artifactNode != null) {
					//除了bom，在dependencyManagement都只压入缓存用于控制版本
					if ("pom".equals(artifactNode.packaging) 
						|| "import".equals(scope)) {
						this.deps.add(artifactNode);
					} else {
						this.depManages.add(artifactNode);
					}
				}
			}
		}

		for (Dependency dep : model.getDependencies()) {
			ArtifactNode dependency = make(model, dep);
			if (dependency == null) {
				continue;
			}
			deps.add(dependency);
		}
	}

	public ArtifactNode make(Model model, Dependency dep) {
		// 只能先添加，因为自己还未解析
		String scope = dep.getScope();
		//依赖类型为test不依赖
		// provided
		if ("test".equals(scope)
			|| "system".equals(scope)) {
			return null;
		}

		// 先添加，等待maven服务解析
		String groupId = dep.getGroupId();
		String artifactId = dep.getArtifactId();
		String version = dep.getVersion();

		String type = dep.getType();

		if (groupId == null) {
			groupId = "";
		}
		if (artifactId == null) {
			artifactId = "";
		}

		// 解析变量
		if (groupId.startsWith("${")) {
			//${project.groupId}
			if ("${project.groupId}".equals(groupId)) {
				groupId = model.getGroupId();
			} else {
				//自定义变量
				groupId = model.getProperties().getProperty(groupId.substring(2, groupId.length()  - 1));	
			}
		}
		if (artifactId.startsWith("${")) {
			//${project.artifactId}
			if ("${project.artifactId}".equals(artifactId)) {
				artifactId = model.getArtifactId();
			} else {
				artifactId = model.getProperties().getProperty(artifactId.substring(2, artifactId.length()  - 1));
			}
		}
		if (version == null) {
			String curGroupIdArtifactId = groupId + ":" + artifactId;
			for (ArtifactNode depManage : depManages) {
				if (curGroupIdArtifactId.equals(depManage.getGroupIdArtifactId())) {
					version = depManage.version;
				}
			}
		}
		if (version == null) {
			version = "+";
		}
		if (version.startsWith("${")) {
			//${project.version}
			if ("${project.version}".equals(version)) {
				version = model.getVersion();
			} else {
				version = model.getProperties().getProperty(version.substring(2, version.length()  - 1));	
			}
		}

		if (version.startsWith("[")
			&& version.endsWith("]")) {
			version = version.substring(1, version.length() - 1);
		}

		ArtifactNode artifactNode = new ArtifactNode();
		artifactNode.groupId = groupId;
		artifactNode.artifactId = artifactId;
		artifactNode.version = version;
		// 本质是 type
		artifactNode.packaging = type;

		List<Exclusion> exclusions = dep.getExclusions();
		if (exclusions != null) {
			// 添加排除依赖项
			artifactNode.setExclusions(exclusions);
		}
		return artifactNode;
	}
}

