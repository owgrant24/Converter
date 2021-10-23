package com.github.entity;

import java.io.File;
import java.util.Objects;


public class Task {

    private String name;
    private File file;
    private String status;
    private String time;
    private String param;
    private String beforeInput;

    public Task(String name, File file) {
        this.name = name;
        this.file = file;
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

    public String getBeforeInput() {
        return beforeInput;
    }

    public void setBeforeInput(String beforeInput) {
        this.beforeInput = beforeInput;
    }

    public void setParam(String param) {
        this.param = param;
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
