/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.extjs;

/**
 *
 * @author fel
 */
public enum ExtJSProperty {

    ALLOW_BLANK("allowBlank"),//, ExtJSPropertyType.STRING),
    ANCHOR("anchor"),
    AUTO_DESTROY("autoDestroy"),//, ExtJSPropertyType.OTHER),
    AUTO_EXPAND_COLUMN("autoExpandColumn"),//, ExtJSPropertyType.STRING),
    AUTO_SCROLL("autoScroll"),//, ExtJSPropertyType.OTHER),
    AUTO_WIDTH("autoWidth"),//, ExtJSPropertyType.STRING),
    BODY_STYLE("bodyStyle"),//, ExtJSPropertyType.OTHER),
    BORDER("border"),//, ExtJSPropertyType.STRING),
    BOX_LABEL("boxLabel"),//, ExtJSPropertyType.STRING),
    BOX_MIN_HEIGHT("boxMinHeight"),//, ExtJSPropertyType.OTHER),
    BUTTONS("buttons"),
    CHECKBOXTOGGLE("checkboxToggle"),
    CHECKED("checked"),//, ExtJSPropertyType.OTHER),
    CLOSABLE("closable"),//, ExtJSPropertyType.OTHER),
    CLS("cls"),//, ExtJSPropertyType.STRING),
    COLLAPSE_MODE("collapseMode"),//, ExtJSPropertyType.STRING),
    COLLAPSED("collapsed"),
    COLLAPSIBLE("collapsible"),//, ExtJSPropertyType.OTHER),
    COLUMNS("columns"),//, ExtJSPropertyType.OTHER),
    DISABLE_KEY_FILTER("disableKeyFilter"),//, ExtJSPropertyType.STRING),
    DISABLED("disabled"),//, ExtJSPropertyType.OTHER),
    DISPLAY_FIELD("displayField"),//, ExtJSPropertyType.STRING),
    EDITABLE("editable"),//, ExtJSPropertyType.STRING),
    EMPTY_TEXT("emptyText"),//, ExtJSPropertyType.STRING),
    FIELD_LABEL("fieldLabel"),//, ExtJSPropertyType.STRING),
    FILE_UPLOAD("fileUpload"),
    FORCE_SELECTION("forceSelection"),//, ExtJSPropertyType.STRING),
    FORMAT("format"),
    FRAME("frame"),//, ExtJSPropertyType.OTHER),
    HEIGHT("height"),//, ExtJSPropertyType.OTHER),
    HIDDEN_NAME("hiddenName"),
    ICON("icon"),//, ExtJSPropertyType.STRING),
    ICON_ALIGN("iconAlign"),//, ExtJSPropertyType.STRING),
    ICON_CLS("iconCls"),
    ID("id"),//, ExtJSPropertyType.STRING),
    ID_INDEX("idIndex"),//, ExtJSPropertyType.OTHER),
    INCREMENT("increment"),
    INPUT_VALUE("inputValue"),
    ITEMS("items"),//, ExtJSPropertyType.OTHER),
    HANDLER("handler"),
    HIDE_LABELS("hideLabels"),//, ExtJSPropertyType.OTHER),
    HTML("html"),//, ExtJSPropertyType.STRING),
    INPUT_TYPE("inputType"),//, ExtJSPropertyType.STRING),
    LABEL_STYLE("labelStyle"),//, ExtJSPropertyType.STRING),
    LABEL_WIDTH("labelWidth"),
    LAYOUT("layout"),//, ExtJSPropertyType.STRING),
    LISTENERS("listeners"),//, ExtJSPropertyType.OTHER),
    MARGINS("margins"),//, ExtJSPropertyType.OTHER),
    MAX_VALUE("maxValue"),
    MIN_HEIGHT("minHeight"),//, ExtJSPropertyType.OTHER),
    MIN_VALUE("minValue"),
    MODE("mode"),//, ExtJSPropertyType.STRING),
    NAME("name"),//, ExtJSPropertyType.STRING),
    PADDING("padding"),//, ExtJSPropertyType.OTHER),
    QUERY_MODE("queryMode"),
    REGEX("regex"),
    REGION("region"),//, ExtJSPropertyType.STRING),
    RENDER_TO("renderTo"),
    SEL_MODEL("selModel"),//, ExtJSPropertyType.OTHER),
    SELECT_ON_FOCUS("selectOnFocus"),//, ExtJSPropertyType.OTHER),
    SPLIT("split"),//, ExtJSPropertyType.OTHER),
    STANDARD_SUBMIT("standardsubmit"),
    STORE("store"),//, ExtJSPropertyType.OTHER),
    STRIPE_ROWS("stripeRows"),//, ExtJSPropertyType.OTHER),
    STYLE("style"),//, ExtJSPropertyType.OTHER),
    SUBMIT_VALUE("submitValue"),
    TBAR("tbar"),//, ExtJSPropertyType.OTHER),
    TEXT("text"),//, ExtJSPropertyType.STRING),
    TITLE("title"),//, ExtJSPropertyType.STRING),
    TITLE_COLLAPSE("titleCollapse"),//, ExtJSPropertyType.OTHER),
    TRANSFORM("transform"),//, ExtJSPropertyType.STRING),
    TRIGGER_ACTION("triggerAction"),//, ExtJSPropertyType.STRING),
    TYPE_AHEAD("typeAhead"),//, ExtJSPropertyType.STRING),
    VALUE("value"),//, ExtJSPropertyType.STRING),
    VALUE_FIELD("valueField"),//, ExtJSPropertyType.STRING),
    WIDTH("width"), 
    HIDDEN("hidden");//, ExtJSPropertyType.OTHER),
    
//    public enum ExtJSPropertyType {
//        STRING,
//        OTHER
//    }

    private String name;
//    private ExtJSPropertyType type;

    private ExtJSProperty(String name) {//, ExtJSPropertyType type) {
        this.name = name;
//        this.type = type;
    }

    public String getName() {
        return this.name;
    }

//    public ExtJSPropertyType getType() {
//        return this.type;
//    }
}
