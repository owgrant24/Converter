package com.github.entity;

import java.io.File;
import java.util.Objects;


public class Task {

    private String name;
    private File file;
    private String status;
    private String time;
    private String param;
    private String specParam;
    private String size;

    public Task(File file, String formatSize) {
        this.file = file;
        this.name = file.getName();
        this.size = formatSize;
        this.status = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getParam() {
        return param;
    }

    public String getSpecParam() {
        return specParam;
    }

    public void setSpecParam(String specParam) {
        this.specParam = specParam;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Task task = (Task) o;

        return Objects.equals(file, task.file);
    }

    @Override
    public int hashCode() {
        return file != null ? file.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                '}';
    }

}
