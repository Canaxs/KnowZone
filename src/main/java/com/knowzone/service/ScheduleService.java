package com.knowzone.service;

public interface ScheduleService {
    void performDailyTasks();
    void performHourlyTasks();
    void expireGroups();
}
