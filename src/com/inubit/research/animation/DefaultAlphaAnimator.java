/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import net.frapu.code.visualization.ProcessObject;

/**
 * @author ff
 *
 */
public class DefaultAlphaAnimator extends NodeAnimator{

	private float f_alpha;
	private float f_newAlpha;
	private float f_diffAlpha;
        private boolean includeEdges;
	
	/**
	 * 
	 */
	public DefaultAlphaAnimator(ProcessObject object,Animator animator, boolean includeEdges, float targetAlpha) {
		super(object,animator);
                this.includeEdges = includeEdges;
                this.f_newAlpha = targetAlpha;
	}

        public DefaultAlphaAnimator(ProcessObject object,Animator animator) {
		this(object, animator, true, object.getAlpha());
	}

	public float getTargetAlpha() {
		return f_newAlpha;
	}
	
	public void setTargetAlpha(float alpha) {
		f_newAlpha = alpha;
	}

	@Override
	protected void setNewValues() {
		if(getCurrentTick() < getSteps()) {
			double _percentage = getLinearProgress();
			float _nv =  (f_alpha + (float)(_percentage * f_diffAlpha));
			getProcessObject().setAlpha(_nv);
		}else {
			if (getProcessObject()!=null) getProcessObject().setAlpha(f_newAlpha);
		}
	}
	
	@Override
	protected void firstStep() {
                if (getProcessObject()!=null) {
                    f_alpha = getProcessObject().getAlpha();
                    f_diffAlpha = f_newAlpha - f_alpha;
                }
	}

    public boolean isIncludeEdges() {
        return includeEdges;
    }

}
