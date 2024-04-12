package com.aide.ui.util;
import com.aide.ui.util.BuildGradle.MavenDependency;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import java.util.List;
import org.apache.maven.model.Exclusion;

/**
 * 保留最最根本的信息，不会过滤或处理任何依赖
 * 待合并树时 将树结构拉平
 */
public class ArtifactNode extends BuildGradle.MavenDependency {

	public static class Factory {
		private Map<String, ArtifactNode> artifactNodes = new HashMap<>();

		// 复用已有解析(包括版本信息)
		// 
		public ArtifactNode makeArtifactNode(BuildGradle.MavenDependency dependency) {

			String coords = dependency.toString();

			ArtifactNode cache = artifactNodes.get(coords);
			if (cache == null) {
				if(! ( dependency instanceof ArtifactNode) ){
					cache = new ArtifactNode(dependency);
				}
				// 设置工厂类[在同一棵树中]
				cache.setFactory(this);
				artifactNodes.put(coords, cache);
				return cache;
			}
			return cache;
		}
		
		public ArtifactNode makeArtifactNode(String groupId, String artifactId, String version) {
			String coords = groupId + ":" + artifactId + ":" + version;
			ArtifactNode cache = artifactNodes.get(coords);
			if (cache == null) {
				cache = new ArtifactNode(groupId, artifactId, version);
				// 设置工厂类[在同一棵树中]
				cache.setFactory(this);
				artifactNodes.put(coords, cache);
			}
			
			return cache;
		}
	}
	/**
	 * 返回一颗新树
	 */
	public static ArtifactNode makeRootArtifactNode() {
		try {
			return new ArtifactNode();
		}
		catch (Throwable e) {
			return null;
		}
	}

	private final boolean isRoot;
	
	/**
	 * 抛出异常，防止被误调用
	 */
	private ArtifactNode() throws Throwable {
		super(-1);
		this.isRoot = true;
		this.factory = new Factory();
	}
	
	public ArtifactNode(int line) {
		super(line);
		this.isRoot = false;
	}
	public ArtifactNode(BuildGradle.MavenDependency dependency) {
		this(1);
		// 无用，但需要兼容底包调用(若无可以取消)
		this.coords = dependency.coords;

		this.groupId = dependency.groupId;
		this.artifactId = dependency.artifactId;
		this.version = dependency.version;
		this.packaging = dependency.packaging;
	}
	public ArtifactNode(String groupId, String artifactId, String version){
		this(1);
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	public void setType(String type) {
		if ("bundle".equals(type)) {
			type = "jar";
		}
		this.packaging = type;
	}

	private Factory factory;
	private void setFactory(Factory factory) {
		this.factory = factory;
	}
	public Factory getFactory() {
		return factory;
	}

	/**
	 * 复用ArtifactNode，会向上查找已有的
	 */
	public ArtifactNode makeArtifactNode(BuildGradle.MavenDependency dependency) {

		//查看factory记录的版本
		ArtifactNode artifactNode = this.factory.makeArtifactNode(dependency);
		// 添加父依赖节点
		artifactNode.parentArtifactNodes.add(this);

		if (artifactNode.version == dependency.version) {
			// 版本一致
			return artifactNode;
		}

		return artifactNode;
	}

	private String pomXmlFilePath;

	
	
	private Map<ArtifactNode, List<Exclusion>> exclusionsMap = new HashMap<>();
	
	// 子依赖 key String groupIdArtifactId，
	private Map<String, ArtifactNode> subArtifactNodes = new HashMap<>();

	private Set<ArtifactNode> parentArtifactNodes = new HashSet<>();
	
	public void parser(String depPomPath) {
		if (pomXmlFilePath != null 
			&& pomXmlFilePath.equals(depPomPath)) {
			return;
		}
		this.pomXmlFilePath = depPomPath;
		// 重置
		
		// 解析 填充
		Model model = null;//PomXml.empty.getConfiguration(this.pomXmlFilePath).getModel();
		
		// 从父节点查看自己的排除选项
		
		//统一版本
		if( model.getDependencyManagement() != null ){
			
		};
		
		// 填充子ArtifactNode
		for(Dependency dependency : model.getDependencies()){
			// 只能先添加，因为自己还未解析
			String scope = dependency.getScope();
			//依赖类型为test不依赖
			if ("test".equals(scope)) {
				return;
			}
			// 先添加，等待maven服务解析
			String version = dependency.getVersion();
			String groupId = dependency.getGroupId();
			String artifactId = dependency.getArtifactId();
			
			if (groupId.startsWith("${")) {
				//${project.groupId}
				if ("${project.groupId}".equals(groupId)) {
					groupId = model.getGroupId();
				}
				else {
					//自定义变量
					groupId = model.getProperties().getProperty(groupId.substring(2, groupId.length()  - 1));	
				}
			}
			if (artifactId.startsWith("${")) {
				//${project.artifactId}
				if ("${project.artifactId}".equals(artifactId)) {
					artifactId = model.getArtifactId();
				}
				else {
					artifactId = model.getProperties().getProperty(artifactId.substring(2, artifactId.length()  - 1));
				}
			}
			if (version != null) {
				if (version.startsWith("${")) {
					//${project.version}
					if ("${project.version}".equals(version)) {
						version = model.getVersion();
					}
					else {
						version = model.getProperties().getProperty(version.substring(2, version.length()  - 1));	
					}
				}
			}
			
			
			ArtifactNode cache = this.factory.makeArtifactNode(groupId, artifactId, version);
			
			
			dependency.getExclusions();
		}
		
		
		
		// 由父节点设置，但会有多个父节点设置
		// 所以应该是 当前节点保存 有排除选项的子节点
		// 并提供 依赖映射
		// Exclusions
	}

}
