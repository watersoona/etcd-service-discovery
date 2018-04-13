package com.github.layker.etcd.discovery.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author layker
 * @Description: 可做拓展
 * @date 2018/4/10 下午3:38
 */
public class Label {
    public static final String S_DE = ",";
    public static final String S_EQ = "=";

    private String name;
    private String value;

    public Label() {
    }

    public Label(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return Objects.equals(name, label.name) &&
                Objects.equals(value, label.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "Label{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public String formatStr() {
        return name + S_EQ + value;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static List<Label> parse(String labels) {
        List<Label> labelList = new ArrayList<>();
        String[] labelArray = labels.split(S_DE);
        Arrays.stream(labelArray).forEach(s -> {
            String[] labelStrs = s.split(S_EQ);
            if (labelStrs.length > 2) {
                labelList.add(new Label(labelStrs[0], labelStrs[1]));
            } else if (labelStrs.length == 1) {
                labelList.add(new Label(labelStrs[0], ""));
            } else {
                labelList.add(new Label(s, ""));
            }
        });
        return labelList;
    }
}
