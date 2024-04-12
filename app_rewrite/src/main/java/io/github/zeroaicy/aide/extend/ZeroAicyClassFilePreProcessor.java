package io.github.zeroaicy.aide.extend;
import com.aide.codemodel.language.classfile.ClassFilePreProcessor;
import io.github.zeroaicy.aide.ClassReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipException;
import io.github.zeroaicy.util.Log;
import java.util.Collections;
import java.util.Vector;
import java.io.File;


/**
 * ZeroAicy实现的ClassFilePreProcessor
 */
public class ZeroAicyClassFilePreProcessor extends ClassFilePreProcessor {

	public ZeroAicyClassFilePreProcessor() {}

	private static ZeroAicyClassFilePreProcessor singleton;

	public static boolean isDefaultMethod(String methodSignature) {
		return ClassReader.hasDefaultMethod(methodSignature);
	}
	
	public static ClassFilePreProcessor getSingleton() {
		if (singleton == null) {
			singleton = new ZeroAicyClassFilePreProcessor();
		}
		return singleton;
	}

	@Override
	public Reader QX(String zipFilePath, String className, String str3) {
		Reader readClassFile = ClassReader.Dc_ReadClassFile(zipFilePath, className);
		if (readClassFile != null) {
			return readClassFile;
		}
		return null;//super.QX(zipFilePath, className, str3);
	}
	
	@Override
	public List<String> J8(String zipFilePath, String listZipEntryName) {

		ZipFile zipFile = null;
		try {
			// str2 相对于Zip内部的路径
			Set<String> listZipNames = new HashSet<>();

			zipFile = yS(zipFilePath);
			//new ZipFile(zipFilePath);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				String zipEntryName = zipEntry.getName();

				if (zipEntryName.endsWith("/")) {
					//去除路径末尾 /
					zipEntryName = zipEntryName.substring(0, zipEntryName.length() - 1);
				}
				if (zipEntryName.equals(listZipEntryName)
					|| !zipEntryName.startsWith(listZipEntryName)) {
					continue;
				}

				if (listZipEntryName.length() > 0 && zipEntryName.charAt(listZipEntryName.length()) != '/') {
					//除了根目录 list ZipEntry子目录应该从/还是
					continue;
				}

				int indexOf = zipEntryName.indexOf('/', listZipEntryName.length() + 1);
				if (indexOf > 0) {
					listZipNames.add(zipFilePath + '/' + zipEntryName.substring(0, indexOf));
				} else {
					String lowerEntryName = zipEntryName.toLowerCase();

					if (zipEntry.isDirectory()) {
						listZipNames.add(zipFilePath + '/' + zipEntryName);
					} else if ((lowerEntryName.endsWith(".class")
							   && lowerEntryName.lastIndexOf('/') >= lowerEntryName.lastIndexOf('$'))) {
						// || lowerEntryName.endsWith(".dex")
						listZipNames.add(zipFilePath + '/' + zipEntryName);							
					} else if (lowerEntryName.endsWith(".java")) {
						if (lowerEntryName.startsWith("src/")) {
							zipEntryName = zipEntryName.substring(4, zipEntryName.length());
						} else {
							listZipNames.add(zipFilePath + '/' + zipEntryName);
						}
					}
				}
			}

			return Arrays.asList(listZipNames.toArray(new String[listZipNames.size()]));
		}
		catch (Exception zipException) {
			Log.d("ZeroAicyClassFilePreProcessor", "zip文件错误: " + zipFilePath);
			return Collections.emptyList();
		}
		catch (Throwable th) {
			throw new Error(th);
		}
	}
	
	@Override
	public void aM(String str, String str2, Vector<String> vector) {
        try {
            if (str2.length() > 0) {
                str2 = str2 + File.separator;
            }
            String[] list = new File(str + File.separatorChar).list();

            if (list != null) {
                for (String str3 : list) {
                    String str4 = str + File.separatorChar + str3;
                    if (new File(str4).isDirectory()) {
                        aM(str4, str2 + str3, vector);
                    } else if (str3.lastIndexOf(36) == -1 && str3.endsWith(".class")) {
                        vector.add(str2 + str3);
                    } else if (str3.endsWith(".java")) {
                        vector.add(str2 + str3);
                    }
                }
            }
        }
		catch (Throwable th) {

        }
    }
	
	@Override
	public String[] Ws(String str) {
        try {
            Vector<String> vector = new Vector<>();
            try {
                if (str.toUpperCase().endsWith(".CLASS")) {
                    String substring = str.lastIndexOf(File.separator) == -1 ? str : str.substring(str.lastIndexOf(File.separator) + 1, str.length());
                    if (substring.indexOf("$") == -1) {
                        vector.add(substring);
                    }
                } else if (new File(str).isDirectory()) {
                    aM(str, "", vector);
                } else {
                    Enumeration<? extends ZipEntry> entries = yS(str).entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry nextElement = entries.nextElement();
                        String name = nextElement.getName();
                        if (!nextElement.isDirectory() 
							&& name.lastIndexOf('/') >= name.lastIndexOf('$')
							&& name.endsWith(".class")) {
                            vector.add(name);
                        } else if (!nextElement.isDirectory() && name.endsWith(".java")) {
                            if (name.startsWith("src/") || name.startsWith("src\\")) {
                                name = name.substring(4, name.length());
                            }
                            vector.add(name);
                        }
                    }
                }
            }
			catch (Exception unused) {
            }
            String[] strArr = new String[vector.size()];
            vector.copyInto(strArr);
            return strArr;
        }
		catch (Throwable th) {
			return null;
        }
    }
}
