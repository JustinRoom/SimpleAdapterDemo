package jsc.kit.adapter.bean;

import java.io.Serializable;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2019/1/15 14:58 Tuesday
 *
 * @author jsc
 */
public class SelectableBean implements Serializable {

    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void toggleSelected(){
        setSelected(!isSelected());
    }
}
