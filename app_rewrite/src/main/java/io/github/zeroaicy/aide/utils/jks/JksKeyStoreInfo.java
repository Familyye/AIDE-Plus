package io.github.zeroaicy.aide.utils.jks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.asn1.x500.X500Name; 


/** 
 * Helper class for dealing with the distinguished name RDNs. 
 */ 
public class JksKeyStoreInfo extends LinkedHashMap<ASN1ObjectIdentifier,String> { 

	public JksKeyStoreInfo() { 
		put(BCStyle.C, null); 
		put(BCStyle.ST, null); 
		put(BCStyle.L, null); 
		put(BCStyle.STREET, null); 
		put(BCStyle.O, null); 
		put(BCStyle.OU, null); 
		put(BCStyle.CN, null); 
	} 

	public String put(ASN1ObjectIdentifier oid, String value) { 
		if (value != null && value.equals("")) value = null; 
		if (containsKey(oid)) super.put(oid, value); // preserve original ordering 
		else { 
			super.put(oid, value); 
			//            String cn = remove(BCStyle.CN); // CN will always be last. 
			//            put(BCStyle.CN,cn); 
		} 
		return value; 
	} 

	public void setCountry(String country) { 
		put(BCStyle.C, country); 
	} 

	public void setStateOrProvince(String state) { 
		put(BCStyle.ST, state); 
	} 
	
	// 城市或区域名称
	public void setCityOrLocality(String locality) { 
		put(BCStyle.L, locality); 
	} 

	public void setStreet(String street) { 
		put(BCStyle.STREET, street); 
	} 

	public void setOrganization(String organization) { 
		put(BCStyle.O, organization); 
	} 

	public void setOrganizationalUnit(String organizationalUnit) { 
		put(BCStyle.OU, organizationalUnit); 
	} 

	public void setCommonName(String commonName) { 
		put(BCStyle.CN, commonName); 
	} 

	@Override 
	public int size() { 
		int result = 0; 
		for (String value : values()) { 
			if (value != null) result += 1; 
		} 
		return result; 
	} 
	
	// X509Principal @deprecated use the X500Name class.
	public X509Principal getPrincipal() { 
		Vector<ASN1ObjectIdentifier> oids = new Vector<ASN1ObjectIdentifier>(); 
		Vector<String> values = new Vector<String>(); 

		for (Map.Entry<ASN1ObjectIdentifier,String> entry : entrySet()) { 
			if (entry.getValue() != null && !entry.getValue().equals("")) { 
				oids.add(entry.getKey()); 
				values.add(entry.getValue()); 
			} 
		} 
		return new X509Principal(oids, values); 
	} 
	
	
//	public X500Name getX500Name() {
//		Vector<ASN1ObjectIdentifier> oids = new Vector<>();
//		Vector<String> values = new Vector<>();
//
//		// 假设 entrySet() 方法返回的是 Map.Entry<ASN1ObjectIdentifier, String> 的集合
//		for (Map.Entry<ASN1ObjectIdentifier, String> entry : entrySet()) {
//			if (entry.getValue() != null && !entry.getValue().isEmpty()) {
//				oids.add(entry.getKey());
//				values.add(entry.getValue());
//			}
//		}
//
//		// 由于 Bouncy Castle 的 X500Name 通常不接受 OID 和值的 Vector，
//		// 我们需要构建一个符合 X.500 名称格式的字符串。
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < oids.size(); i++) {
//			if (i > 0) {
//				sb.append(",");
//			}
//			sb.append(BCStyle.getInstance().getStringRepresentation(oids.get(i))).append("=").append(values.get(i));
//		}
//
//		// 使用 Bouncy Castle 的 X500Name 构造函数（注意：这取决于您使用的 Bouncy Castle 版本）
//		return new X500Name(sb.toString());
//	}
}
