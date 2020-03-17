package com.office.common.entity.wrap;

import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class MyElement {
    /**
     * @param element 对应结点
     * @param text 文本
     * @param attributes 属性
     * @param father 父亲
     * @param sons 儿子们
     * @param weight 权重
     * @param signature 哈希值
     * @param isMatch 标记是否已匹配
     * @param matchedElement 匹配的元素
     * @param isNote 标记是否已标注
     * @param note 标注内容
     * @param order 在兄弟节点中的序号
     */
    private Element element;
    private String text;
    private List<Attribute> attributes;
    private MyElement father;
    private ArrayList<MyElement> sons;
    private double weight;
    private StringBuilder signature;
    private boolean isMatch;
    private MyElement matchedElement;
    private boolean isNote;
    private String note;
    private int order;

    public MyElement(Element element, String text, List<Attribute> attributes) {
        this.element = element;
        this.text = text;
        this.attributes = attributes;
        isMatch = false;
        isNote = false;
        weight = -1;
        sons = new ArrayList<MyElement>();
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public MyElement getFather() {
        return father;
    }

    public void setFather(MyElement father) {
        this.father = father;
    }

    public ArrayList<MyElement> getSons() {
        return sons;
    }

    public void setSons(ArrayList<MyElement> sons) {
        this.sons = sons;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
    }

    public MyElement getMatchedElement() {
        return matchedElement;
    }

    public void setMatchedElement(MyElement matchedElement) {
        this.matchedElement = matchedElement;
    }

    public boolean isNote() {
        return isNote;
    }

    public void setNote(boolean note) {
        isNote = note;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public StringBuilder getSignature() {
        return signature;
    }

    public void setSignature(StringBuilder signature) {
        this.signature = signature;
    }
}
