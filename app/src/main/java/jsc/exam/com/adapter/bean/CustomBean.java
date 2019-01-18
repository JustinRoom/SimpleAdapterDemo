package jsc.exam.com.adapter.bean;

import jsc.kit.adapter.bean.SelectableBean;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2019/1/18 20:08 Friday
 *
 * @author jsc
 */
public class CustomBean extends SelectableBean {

    private String label;

    public CustomBean() {
    }

    public CustomBean(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
