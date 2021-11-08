# 保证IJson派生类的成员变量名不被混淆
-keepclassmembers public class * extends com.zwping.alibx.IJson { *; }