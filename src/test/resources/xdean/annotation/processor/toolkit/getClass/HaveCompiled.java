package xdean.annotation.processor.toolkit.getClass;

import xdean.annotation.processor.toolkit.AssertException;
import xdean.annotation.processor.toolkit.CommonUtil;
import xdean.annotation.processor.toolkit.ElementUtil;

@Anno(classValue = CommonUtil.class, classArray = { ElementUtil.class, AssertException.class })
public class HaveCompiled {

}
