package com.office.common.utils;

import com.office.common.entity.MyElement;
import org.dom4j.Attribute;
import org.dom4j.Element;
import java.util.*;

public class DIFF {
    //用哈希值对应包装结点
    public static MyElement rootElement;
    public static MyElement rootElement1;
    public static HashMap<String, ArrayList<MyElement>> elementHashMap = new HashMap<String, ArrayList<MyElement>>();
    public static HashMap<String, ArrayList<MyElement>> elementHashMap1 = new HashMap<String, ArrayList<MyElement>>();
    public static Queue<MyElement> elementQueue = new LinkedList<MyElement>();

    /**
     * 本方法用于xml节点的属性比较
     *
     * @param oldNode 节点1
     * @param newNode 节点2
     * @return 两节点属性是否相同
     */
    public static boolean compareAttributes(Element oldNode, Element newNode) {
        if (oldNode.equals(newNode)) {
            return true;
        }
        List<Attribute> oldAttr = oldNode.attributes();
        List<Attribute> newAttr = newNode.attributes();
        if (oldAttr.size() != newAttr.size()) {
            return false;
        } else {
            for (int i = 0; i < oldAttr.size(); i++) {
                if (!oldAttr.get(i).getText().equals(newAttr.get(i).getText())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 本方法用于xml节点的内容比较
     *
     * @param oldNode 节点1
     * @param newNode 节点2
     * @return 两节点文本内容是否相同
     */
    public static boolean compareNodesText(Element oldNode, Element newNode) {
        if (oldNode.equals(newNode)) {
            return true;
        }
        return oldNode.getTextTrim().equals(newNode.getTextTrim());
    }

    /**
     * 本方法用于返回最长递增序列
     *
     * @param array 匹配数组
     * @return 最长递增序列
     */
    public static ArrayList<Integer> getLsc(int[] array) {
        int max = 0;
        int maxPos = -1;
        int size = array.length;
        int[] rec = new int[size];
        for (int i = 0; i < size; i++) {
            rec[i] = 1;
        }
        int[] from = new int[size];
        for (int i = 0; i < size; i++) {
            from[i] = -1;
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < i; j++) {
                if (array[i] > array[j] && rec[j] + 1 > rec[i]) {
                    rec[i] = rec[j] + 1;
                    from[i] = j;
                    if (rec[i] > max) {
                        maxPos = i;
                        max = rec[i];
                    }

                }
            }
        }
        ArrayList<Integer> ret = new ArrayList<>();
        int curr = maxPos;
        while (curr >= 0) {
            ret.add(array[curr]);
            curr = from[curr];
        }
        return ret;
    }

    /**
     * 本方法用于包装结点
     *
     * @param element 结点
     * @param father  父结点
     * @param number  在兄弟结点中的标号F
     */
    public static void wrapNode(Element element, MyElement father, int number, int i) {
        //根节点的父亲是自己
        MyElement myElement = new MyElement(element, element.getTextTrim(), element.attributes());
        myElement.setOrder(number);
        myElement.setFather(father);
        if (father != null) {
            father.getSons().add(myElement);
        } else {
            if (i == 1) {
                rootElement = myElement;
            } else {
                rootElement1 = myElement;
            }
        }
        if (element.elements().size() == 0) {
            return;
        }
        int num = 0;
        for (Element son : element.elements()) {
            num++;
            wrapNode(son, myElement, num, i);
        }
    }

    /**
     * 本方法用于计算所有结点的哈希值和权重
     *
     * @param myElement 结点
     * @param hashMap   储存结点的HashMap
     */
    public static void countStatics(MyElement myElement, HashMap<String, ArrayList<MyElement>> hashMap) {
        StringBuilder sb = new StringBuilder();
        sb.append(myElement.getText());
        for (Attribute attribute : myElement.getAttributes()) {
            sb.append(attribute.getName()).append(attribute.getValue());
        }
        sb.append("&");
        if (myElement.getSons().size() == 0) {
            myElement.setSignature(sb);
            myElement.setWeight(1 + Math.log(sb.length()));
            if (hashMap.keySet().contains(myElement.getSignature().toString())) {
                hashMap.get(myElement.getSignature().toString()).add(myElement);
            } else {
                ArrayList<MyElement> al = new ArrayList<MyElement>();
                al.add(myElement);
                hashMap.put(myElement.getSignature().toString(), al);
            }
            return;
        }
        myElement.setWeight(1);
        for (MyElement me : myElement.getSons()) {
            if (me.getWeight() < 0) {
                countStatics(me, hashMap);
            }
            myElement.setSignature(sb.append(me.getSignature()));
            myElement.setWeight(myElement.getWeight() + me.getWeight());
        }
        if (hashMap.keySet().contains(myElement.getSignature().toString())) {
            hashMap.get(myElement.getSignature().toString()).add(myElement);
        } else {
            ArrayList<MyElement> al = new ArrayList<MyElement>();
            al.add(myElement);
            hashMap.put(myElement.getSignature().toString(), al);
        }
    }

    /**
     * 本方法用于匹配结点
     */
    public static void matchNodes() {
        elementQueue.add(rootElement);
        while (!elementQueue.isEmpty()) {
            MyElement myElement = elementQueue.remove();
            ArrayList<MyElement> myElements = elementHashMap1.get(myElement.getSignature().toString());
            if (myElements != null && myElements.size() == 1) {
                myElement.setMatch(true);
                myElement.setMatchedElement(myElements.get(0));
                myElements.get(0).setMatch(true);
                myElements.get(0).setMatchedElement(myElement);
            } else if (myElements != null && myElements.size() > 1) {
                for (MyElement m : myElements) {
                    if (myElement.getFather().getMatchedElement() == m.getFather() && myElement.getElement().getPath().equals(m.getElement().getPath())) {
                        if (!m.isMatch()) {
                            myElement.setMatch(true);
                            myElement.setMatchedElement(m);
                            m.setMatch(true);
                            m.setMatchedElement(myElement);
                            break;
                        }
                    }
                }
            } else {
                for (MyElement me : myElement.getSons()) {
                    elementQueue.add(me);
                }
            }
            if (myElement.isMatch()) {
                matchAncestors(myElement, myElement.getMatchedElement());
            }
        }
    }

    /**
     * 本方法用于祖先结点的匹配
     *
     * @param me1 元素1
     * @param me2 元素2
     */
    public static void matchAncestors(MyElement me1, MyElement me2) {
        //这里有问题
//        int cur = 1 + (int) (me1.getWeight() / rootElement.getWeight());
        MyElement m1 = me1;
        MyElement m2 = me2;
//        while (cur-- > 0) {
        while (m1.getFather() != null) {
            m1 = m1.getFather();
            m2 = m2.getFather();
            if (!m1.isMatch() && !m2.isMatch() && m1.getElement().getName().equals(m2.getElement().getName())) {
                m1.setMatch(true);
                m1.setMatchedElement(m2);
                m2.setMatch(true);
                m2.setMatchedElement(m1);
            } else {
                break;
            }
        }
    }

    /**
     * 本方法用于遍历xml树匹配剩余结点以提高准确性
     *
     * @param myElement 结点
     */
    public static void matchRemainedNode(MyElement myElement) {
        if (!myElement.isMatch() && myElement.getFather() != null && myElement.getFather().isMatch()) {
            ArrayList<MyElement> sons = myElement.getFather().getMatchedElement().getSons();
            for (MyElement m : sons) {
                //可能有问题
                if (!myElement.isMatch() && !m.isMatch() && myElement.getElement().getPath().equals(m.getElement().getPath())) {

                    myElement.setMatch(true);
                    myElement.setMatchedElement(m);
                    m.setMatch(true);
                    m.setMatchedElement(myElement);
                    break;
                }
            }
        }
        if (myElement.getSons().size() == 0) {
            return;
        }
        for (MyElement me : myElement.getSons()) {
            matchRemainedNode(me);
        }
    }

    /**
     * 本方法用于标记所有结点
     */
    public static void noteNodes(MyElement myElement, int count) {
        if (!myElement.isNote()) {
            if (!myElement.isMatch()) {
                myElement.setNote(true);
                if (count == 1) {
                    myElement.setNote("del");
                } else if (count == 2) {
                    myElement.setNote("add");
                }
            } else {
                myElement.setNote(true);
                myElement.setNote(true);
                if (compareNodesText(myElement.getElement(), myElement.getMatchedElement().getElement())
                        && compareAttributes(myElement.getElement(), myElement.getMatchedElement().getElement())) {
                    if (myElement.getFather() == null || myElement.getFather().getMatchedElement() == myElement.getMatchedElement().getFather()) {
                        myElement.setNote("mat");
                        myElement.getMatchedElement().setNote("mat");
                    } else {
                        myElement.setNote("mov");
                        myElement.getMatchedElement().setNote("mov");
                    }
                } else {
                    myElement.setNote("mod");
                    myElement.getMatchedElement().setNote("mod");
                }
            }
        }
        if (myElement.getSons().size() == 0) {
            return;
        }
        for (MyElement me : myElement.getSons()) {
            noteNodes(me, count);
        }
    }

    /**
     * 本方法用于找出在父节点下移动的结点
     *
     * @param myElement 结点
     */
    public static void nodeMovedNodes(MyElement myElement) {
        if (myElement.getSons().size() == 0) {
            return;
        }
        HashMap<Integer, MyElement> hm = new HashMap<Integer, MyElement>();
        int[] vis = new int[myElement.getSons().size() + 1];
        for (int i = 0; i < vis.length; i++) {
            vis[i] = -1;
        }
        for (MyElement m : myElement.getSons()) {
            if (m.getNote().equals("mat")) {
                hm.put(m.getOrder(), m);
                vis[m.getOrder()] = m.getMatchedElement().getOrder();
            }
        }
        ArrayList<Integer> lsc = getLsc(vis);
        for (Integer integer : hm.keySet()) {
            if (!lsc.contains(integer)) {
                hm.get(integer).setNote("mov");
                hm.get(integer).getMatchedElement().setNote("mov");
            }
        }
        for (MyElement me : myElement.getSons()) {
            nodeMovedNodes(me);
        }
    }
}
