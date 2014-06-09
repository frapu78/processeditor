/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.frapu.code.visualization.xforms;

/**
 *
 * @author fpu
 */
public class Tuple {

    private String key;
    private String value;

    public Tuple(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }



}
