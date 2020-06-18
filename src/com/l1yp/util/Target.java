package com.l1yp.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Lyp
 * @Date 2020/6/9 0009
 * @Email l1yp@qq.com
 */
public enum Target {
    JDK1_1("1.1", 45, 3),
    JDK1_2("1.2", 46, 0),
    JDK1_3("1.3", 47, 0),

    /** J2SE1.4 = Merlin. */
    JDK1_4("1.4", 48, 0),

    /** JDK 5, codename Tiger. */
    JDK1_5("5", 49, 0),

    /** JDK 6. */
    JDK1_6("6", 50, 0),

    /** JDK 7. */
    JDK1_7("7", 51, 0),

    /** JDK 8. */
    JDK1_8("8", 52, 0),

    /** JDK 9. */
    JDK1_9("9", 53, 0),

    /** JDK 10. */
    JDK1_10("10", 54, 0),

    /** JDK 11. */
    JDK1_11("11", 55, 0),

    /** JDK 12. */
    JDK1_12("12", 56, 0),

    /** JDK 13. */
    JDK1_13("13", 57, 0),

    /** JDK 14. */
    JDK1_14("14", 58, 0);

    public static final Target MIN = Target.JDK1_7;

    private static final Target MAX = values()[values().length - 1];

    private static final Map<String,Target> tab = new HashMap();
    static {
        for (Target t : values()) {
            tab.put(t.name, t);

            tab.put("v_" + t.majorVersion + "_" + t.minorVersion, t);
        }
        tab.put("1.5", JDK1_5);
        tab.put("1.6", JDK1_6);
        tab.put("1.7", JDK1_7);
        tab.put("1.8", JDK1_8);
        tab.put("1.9", JDK1_9);
        tab.put("1.10", JDK1_10);
        tab.put("1.11", JDK1_11);
        tab.put("1.12", JDK1_12);
        tab.put("1.13", JDK1_13);
        tab.put("1.14", JDK1_14);
    }

    public final String name;
    public final int majorVersion;
    public final int minorVersion;
    Target(String name, int majorVersion, int minorVersion) {
        this.name = name;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public static final Target DEFAULT = values()[values().length - 1];

    public static Target lookup(String name) {
        return tab.get(name);
    }

    public static Target lookup(int majorVersion, int minorVersion) {
        return tab.get("v_" + majorVersion + "_" + minorVersion);
    }


    public boolean isSupported() {
        return this.compareTo(MIN) >= 0;
    }

    /** Return the character to be used in constructing synthetic
     *  identifiers, where not specified by the JLS.
     */
    public char syntheticNameChar() {
        return '$';
    }

    /** Does the target VM expect MethodParameters attributes?
     */
    public boolean hasMethodParameters() {
        return compareTo(JDK1_8) >= 0;
    }

    /** Does the target JDK contain StringConcatFactory class?
     */
    public boolean hasStringConcatFactory() {
        return compareTo(JDK1_9) >= 0;
    }

    /** Value of platform release used to access multi-release jar files
     */
    public String multiReleaseValue() {
        return Integer.toString(this.ordinal() - Target.JDK1_1.ordinal() + 1);
    }

    /** All modules that export an API are roots when compiling code in the unnamed
     *  module and targeting 11 or newer.
     */
    public boolean allApiModulesAreRoots() {
        return compareTo(JDK1_11) >= 0;
    }

    /** Does the target VM support nestmate access?
     */
    public boolean hasNestmateAccess() {
        return compareTo(JDK1_11) >= 0;
    }

    /** Does the target VM support virtual private invocations?
     */
    public boolean hasVirtualPrivateInvoke() {
        return compareTo(JDK1_11) >= 0;
    }

}
