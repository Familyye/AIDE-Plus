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

public class PomXml extends Configuration<PomXml> {
    public  static PomXml empty = new PomXml();

	//子依赖
	//将一分为二
	public List<BuildGradle.MavenDependency> dependencies;
	public List<BuildGradle.MavenDependency> dependencyManagements;


	public String packaging = "jar";

    public PomXml() {
		this.dependencies = new ArrayList<>();
		this.dependencyManagements = new ArrayList<>();
    }

    private String FH(Element element, String str) {
        try {
            Node item = element.getElementsByTagName(str).item(0);
            return item == null ? "" : item.getTextContent();
        }
		catch (Throwable th) {
            throw new Error(th);
        }
    }

    public PomXml makeConfiguration(String str) {
        try {
            return new PomXml(str);
        }
		catch (Throwable th) {
            throw new Error(th);
        }
    }

    private PomXml(String str) {
        try {
            this.dependencies = new ArrayList<>();
			this.dependencyManagements = new ArrayList<>();
            try {
                FileInputStream fileInputStream = new FileInputStream(str);
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileInputStream);
                fileInputStream.close();

				Element root = document.getDocumentElement();
				//获取根节点下面的所有子节点（不包过子节点的子节点）
				NodeList childNodes = root.getChildNodes() ;
				//遍历List的方法
				for (int i = 0, size = childNodes.getLength(); i < size; i++) {
					Node item = childNodes.item(i);
					String nodeName = item.getNodeName();

					try {
						if ("packaging".equals(nodeName)) {
							String packaging = item.getTextContent();
							if ("bundle".equals(packaging)) {
								packaging = "jar";
							}
							if (packaging != null) {
								this.packaging = packaging;
							}
							continue;
						}
						if ("dependencyManagement".equals(nodeName)) {
							resolvingDependencyManagement((Element)item);
							continue;
						}
						if ("dependencies".equals(nodeName)) {
							resolvingDependencies((Element)item);
							continue;
						}
					}
					catch (Exception e) {}
				}
			}
			catch (Exception e2) {
                e2.printStackTrace();
            }
        }
		catch (Throwable th) {
            throw new Error(th);
        }
    }
	private void resolvingDependencyManagement(Element dependencyManagement) {
		NodeList dependencyNode = dependencyManagement.getElementsByTagName("dependency");
		for (int i = 0; i < dependencyNode.getLength(); i++) {
			try {
				Element dependencyElement = (Element) dependencyNode.item(i);
				String scope = FH(dependencyElement, "scope");

				BuildGradle.MavenDependency dependency = makeDependency(dependencyElement);
				if (dependency != null) {
					//除了bom，在dependencyManagement都只压入缓存用于控制版本
					if ("pom".equals(dependency.packaging) 
						|| "import".equals(scope)) {
						this.dependencies.add(dependency);
					} else {
						this.dependencyManagements.add(dependency);
					}
				}
			}
			catch (Throwable e) {

			}
		}
	}

	private void resolvingDependencies(Element dependencies) {
		NodeList dependencyNode = dependencies.getElementsByTagName("dependency");
		for (int i = 0; i < dependencyNode.getLength(); i++) {
			try {
				Element dependencyElement = (Element) dependencyNode.item(i);
				BuildGradle.MavenDependency dependency = makeDependency(dependencyElement);
				if (dependency != null) {
					this.dependencies.add(dependency);
				}
			}
			catch (Throwable e) {

			}
		}
	}

	private BuildGradle.MavenDependency makeDependency(Element element) {
		String groupId = FH(element, "groupId");
		String artifactId = FH(element, "artifactId");
		String version = FH(element, "version");
		String scope = FH(element, "scope");
		String type = FH(element, "type");

		if (groupId == null 
			|| groupId.length() == 0 
			|| artifactId == null 
			|| artifactId.length() == 0
			|| groupId.contains("$")
			|| artifactId.contains("$")
		//测试是否已隔离 dependencyManagement
		//|| "unspecified".equals(version)

			|| "test".equals(scope)
			|| "provided".equals(scope)
			|| "system".equals(scope)) {

			return null;
		}

		if (version == null 
			|| version.length() == 0 
			|| version.contains("$")) {
			version = "+";
		}

		BuildGradle.MavenDependency dependency = new BuildGradle.MavenDependency(1);
		dependency.groupId = groupId;
		dependency.artifactId = artifactId;
		dependency.version = version;
		dependency.packaging = type;

		return dependency;
	}
}

