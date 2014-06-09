/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package net.frapu.code.converter;

/**
 *
 * @author jos
 */
public class ConversiontSettingsDTO {

    public static final String TIBCO = "TIBCO";
    public final static String BIZAGI = "BizAgi";
    public final static String ITP = "itp-commerce";
    public final static String SemTalk = "SemTalk";
    public static int ConversionScale = 3;
    //Element Position absolute
    private boolean Orgin_Absolut;
    private boolean TibcoLaneHeight;
    //True if all pools are contained in one Process
    private boolean oneProcess;
    //True if the xpdl contain pages;
    private boolean hasPages;
    //
    private boolean BizAgiGateways;
    private boolean hasInvisibleMainPool;
    // Bizagi
    private boolean lanesRelativetoPool;
    // if the coordinates use are colon as separator, e.g 100,2 
    private boolean useKomma;
    // if orgin is down left
    private boolean orginDown;
    //
    private boolean needConnectorGraphicsInfos;
    //itp
    private boolean needsScaleConversion;
    // if lanes are nedded
    private boolean needsLanes;
    private String vendor;
    //needed for itp  
    private int pagewidth = 500;
    private int pageheigth = 500;
    private double scaleY = 1.3;
    private double scaleX = 2.4;
    private double scaleWidth = 2.5;
    private double scaleHeight = 1.2;

    public ConversiontSettingsDTO() throws Exception {
        setHasPages(false);
        setOneProcess(false);
        setOrgin_Absolut(true);
        setHasInvisibleMainPool(false);
        setLanesRelativetoPool(false);
        setTibcoLaneHeight(false);
        setBizAgiGateways(false);
        setOrginDown(false);
        setUseKomma(false);
        setNeedConnectorGraphicsInfos(false);
        setNeedsScaleConversion(false);
    }

    public ConversiontSettingsDTO(String targetTool) throws Exception {
        setValues(targetTool);
    }

    private void setValues(String targetTool) {
        if (targetTool.contains(TIBCO)) {
            setHasPages(false);
            setOneProcess(true);
            setOrgin_Absolut(false);
            setHasInvisibleMainPool(false);
            setLanesRelativetoPool(true);
            setTibcoLaneHeight(true);
            setBizAgiGateways(false);
            setOrginDown(false);
            setUseKomma(false);
            setNeedConnectorGraphicsInfos(false);
            setNeedsScaleConversion(false);
            setNeedsLanes(true);
            vendor = TIBCO;
        } else if (targetTool.contains(BIZAGI)) {
            setHasPages(false);
            setOneProcess(false);
            setOrgin_Absolut(true);
            setHasInvisibleMainPool(true);
            setLanesRelativetoPool(true);
            setTibcoLaneHeight(false);
            setBizAgiGateways(true);
            setOrginDown(false);
            setUseKomma(false);
            setNeedConnectorGraphicsInfos(false);
            setNeedsScaleConversion(false);
            setNeedsLanes(false);
            vendor = BIZAGI;
        } else if (targetTool.contains(ITP)) {
            setHasPages(true);
            setOneProcess(false);
            setOrgin_Absolut(true);
            setHasInvisibleMainPool(false);
            setLanesRelativetoPool(false);
            setTibcoLaneHeight(false);
            setBizAgiGateways(false);
            setOrginDown(true);
            setUseKomma(true);
            setNeedConnectorGraphicsInfos(true);
            setNeedsScaleConversion(true);
            setNeedsLanes(false);
            vendor = ITP;
        } else if (targetTool.contains(SemTalk)) {
            setHasPages(true);
            setOneProcess(true);
            setOrgin_Absolut(true);
            setHasInvisibleMainPool(true);
            setLanesRelativetoPool(false);
            setTibcoLaneHeight(false);
            setBizAgiGateways(false);
            setOrginDown(false);
            setUseKomma(false);
            setNeedConnectorGraphicsInfos(false);
            setNeedsScaleConversion(false);
            setNeedsLanes(false);
            vendor = SemTalk;
        } 
    }

    public boolean isNeedsLanes() {
        return needsLanes;
    }

    public void setNeedsLanes(boolean needsLanes) {
        this.needsLanes = needsLanes;
    }



    public boolean isNeedsScaleConversion() {
        return needsScaleConversion;
    }

    public void setNeedsScaleConversion(boolean needsScaleConversion) {
        this.needsScaleConversion = needsScaleConversion;
    }

    public boolean isNeedConnectorGraphicsInfos() {
        return needConnectorGraphicsInfos;
    }

    public void setNeedConnectorGraphicsInfos(boolean needConnectorGraphicsInfos) {
        this.needConnectorGraphicsInfos = needConnectorGraphicsInfos;
    }

    public double getScaleHeight() {
        return scaleHeight;
    }

    public void setScaleHeight(double scaleHeight) {
        this.scaleHeight = scaleHeight;
    }

    public double getScaleWidth() {
        return scaleWidth;
    }

    public void setScaleWidth(double scaleWidth) {
        this.scaleWidth = scaleWidth;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    public boolean isUseKomma() {
        return useKomma;
    }

    public void setUseKomma(boolean useKomma) {
        this.useKomma = useKomma;
    }

    public boolean isOrginDown() {
        return orginDown;
    }

    public void setOrginDown(boolean orginDown) {
        this.orginDown = orginDown;
    }

    public boolean isTibcoLaneHeight() {
        return TibcoLaneHeight;
    }

    public void setTibcoLaneHeight(boolean TibcoLaneHeight) {
        this.TibcoLaneHeight = TibcoLaneHeight;
    }

    public boolean isBizAgiGateways() {
        return BizAgiGateways;
    }

    public void setBizAgiGateways(boolean BizAgiGateways) {
        this.BizAgiGateways = BizAgiGateways;
    }

    public int getPageheigth() {
        return pageheigth;
    }

    public void setPageheigth(int itp_pageheigth) {
        this.pageheigth = itp_pageheigth;
    }

    public int getPagewidth() {
        if (needsScaleConversion) {
            Double returnValue = (pagewidth / this.getScaleWidth()) * 1.1;
            return returnValue.intValue();
        } else {
            return pagewidth;
        }

    }

    public void setPagewidth(int itp_pagewidth) {
        this.pagewidth = itp_pagewidth;
    }

    public boolean isLanesRelativetoPool() {
        return lanesRelativetoPool;
    }

    public void setLanesRelativetoPool(boolean lanesRelativetoPool) {
        this.lanesRelativetoPool = lanesRelativetoPool;
    }

    public boolean isHasInvisibleMainPool() {
        return hasInvisibleMainPool;
    }

    public void setHasInvisibleMainPool(boolean hasInvisibleMainPool) {
        this.hasInvisibleMainPool = hasInvisibleMainPool;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
        setValues(vendor);
    }

    public void setOrgin_Absolut(boolean Ursprung_Absolut) {
        this.Orgin_Absolut = Ursprung_Absolut;
    }

    public boolean isUrsprung_Absolut() {
        return Orgin_Absolut;
    }

    public boolean hasPages() {
        return hasPages;
    }

    public void setHasPages(boolean hasPages) {
        this.hasPages = hasPages;
    }

    public void setOneProcess(boolean oneProcess) {
        this.oneProcess = oneProcess;
    }

    public boolean isOneProcess() {
        return oneProcess;
    }
}
