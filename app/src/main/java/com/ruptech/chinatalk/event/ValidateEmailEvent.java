package com.ruptech.chinatalk.event;


import com.ruptech.chinatalk.task.TaskResult;

public class ValidateEmailEvent {
    public final String msg;
    public final TaskResult result;

    public ValidateEmailEvent(String msg, TaskResult result) {
        this.msg = msg;
        this.result = result;
    }
}
