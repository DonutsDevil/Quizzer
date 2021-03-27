package com.example.quizzer;

import java.util.List;

public class CategoryModel {
    private String url, name, key;
    private List<String> sets;
    public CategoryModel(){
        // This constructor is used by firebase
    }

    public CategoryModel(String url, String name, String key, List<String> sets) {
        this.url = url;
        this.name = name;
        this.key = key;
        this.sets = sets;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getSets() {
        return sets;
    }

    public void setSets(List<String> sets) {
        this.sets = sets;
    }
}
