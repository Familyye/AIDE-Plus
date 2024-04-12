# 保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

#保持 Serializable 不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}


# 保持枚举 enum 类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#androidx annotation
-keep,allowobfuscation @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

-keepclassmembers,allowobfuscation class * {
  @androidx.annotation.DoNotInline <methods>;
}


#扩展接口
-keep class io.github.zeroaicy.aide.extend.ZeroAicyExtensionInterface {  
    static <methods>;
    <fields>;
    <methods>;
}

#签名模块
#签名模块
#保留注解，内部类，封闭类信息
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature

-keepclasseswithmembers class com.android.apksig.internal.asn1.Asn1DerEncoder* {
    *;
}
-keepclasseswithmembers class com.android.apksig.internal.asn1.Asn1BerParser* {
    *;
}

-keepclasseswithmembers class com.android.apksig.internal.asn1.Asn1Class
-keepclasseswithmembers class com.android.apksig.internal.asn1.Asn1Field
#Asn1Class注解的类
-keepclasseswithmembers @com.android.apksig.internal.asn1.Asn1Class class com.android.apksig.** { *;}
#保留Asn1Field注解的字段
-keepclassmembers class com.android.apksig.** {
    @com.android.apksig.internal.asn1.Asn1Field <fields>;
}

-keep class com.hjq.permissions.Permission {
    <fields>;
    <methods>;
}

# 重写AIDE底包方法类
-keep class com.aide.** {
    <fields>;
    <methods>;
}
-keep class abcd.** {
    <fields>;
    <methods>;
}
-keep class com.support.** {
    <fields>;
    <methods>;
}
-keep class androidx.** {
    <fields>;
    <methods>;
}
#反射
-keep class io.github.zeroaicy.util.reflect.** {
    <fields>;
    <methods>;
}


-keep class io.github.zeroaicy.readclass.classInfo.ClassInfoTest3 {
    <fields>;
    <methods>;
}
-keep class io.github.zeroaicy.readclass.classInfo.DefaultMethodAllowedList {
    <fields>;
    <methods>;
}

#AIDE底包
-dontwarn abcd.**
-dontwarn com.aide.**
-dontwarn java.util.Comparator$_DC 
-dontwarn

