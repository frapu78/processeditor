/*
 * Process Editor
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.uml;

/**
 *
 * @author fel
 */
public enum Visibility {

    PUBLIC("+"),
    PROTECTED("#"),
    PACKAGE("~"),
    PRIVATE("-");

    private String umlString;

    Visibility(String stringValue) {
        this.umlString = stringValue;
    }

    public String getUMLString() {
        return this.umlString;
    }

    public static String[] stringValues() {
        String[] values = new String[ values().length ];
        int i = 0;
        for ( Visibility v : values() )
            values[i++] = v.toString();

        return values;
    }

    public static Visibility forUMLString( String s ) {
        if ( "+".equals( s ) ) {
            return Visibility.PUBLIC;
        } else if ( "#".equals( s ) ) {
            return Visibility.PROTECTED;
        } else if ( "~".equals( s ) ) {
            return Visibility.PACKAGE;
        } else if ( "-".equals( s ) ) {
            return Visibility.PRIVATE;
        }

        return null;
    }
}
