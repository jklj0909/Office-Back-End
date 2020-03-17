package com.office.student.controller;

import com.office.common.entity.wrap.RecordDisplay;
import com.office.student.service.PractiseRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("record")
public class PractiseRecordController {
    private PractiseRecordService practiseRecordService;

    @Autowired
    public void setPractiseRecordService(PractiseRecordService practiseRecordService) {
        this.practiseRecordService = practiseRecordService;
    }

    @RequestMapping("getPractiseRecord")
    public ResponseEntity<List<RecordDisplay>> getPractiseRecord(HttpServletRequest request) {
        try {
            String studentUsername = OperationController.getStudentUsername(request);
            List<RecordDisplay> recordList = practiseRecordService.getRecordMap(studentUsername);
            if (!CollectionUtils.isEmpty(recordList)) {
                return ResponseEntity.ok(recordList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("getTestResult/{id}")
    public ResponseEntity<Map<String, Object>> getTestResult(@PathVariable("id") String id) {
        try {
            Map<String, Object> testResult = practiseRecordService.getTestResult(id);
            if (!CollectionUtils.isEmpty(testResult)) {
                return ResponseEntity.ok(testResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }
}
