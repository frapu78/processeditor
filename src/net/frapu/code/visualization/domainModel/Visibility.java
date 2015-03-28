/*
 * Process Editor
 *
 * (C) 2010 inubit AG
 * (C) 2014 the authors
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.domainModel;


/**
 *
 * @author fel
 */
public enum Visibility {

    PUBLIC(""), // "+" removed for smarter display
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
