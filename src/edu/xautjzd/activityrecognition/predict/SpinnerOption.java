package edu.xautjzd.activityrecognition.predict;

/** 
 * @author xautjzd
 * @function 实现text-value形式的下拉菜单, Spinner的toString()默认实现为显示每个下拉项(Object)的toString()形式,
 * 现在重写toString使其返回value
 */

public class SpinnerOption {

	private String value = null;  
    private String text = null;  
  
    public SpinnerOption() {  
        value = "";  
        text = "";  
    }  
  
    public SpinnerOption(String text, String value) {  
        this.text = text; 
        this.value = value;  
    }  
  
    @Override  
    public String toString() {   
        return text;        // What to display in the Spinner list
    }  
    
    public String getValue() {  
        return value;  
    }  
    public String getText() {  
        return text;  
    }  
}
