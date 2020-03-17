package com.office.teacher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.common.entity.*;
import com.office.common.entity.step.QuestionStep;
import com.office.common.entity.wrap.PageResult;
import com.office.common.entity.wrap.QuestionMessage;
import com.office.common.entity.wrap.ReplyMessage;
import com.office.common.utils.CookieUtils;
import com.office.teacher.entity.TeacherInfo;
import com.office.teacher.service.QuestionInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("mark")
public class QuestionInfoController {
    private QuestionInfoService questionInfoService;
    private static final String USER_COOKIE = "UserCookie";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    public void setQuestionInfoService(QuestionInfoService questionInfoService) {
        this.questionInfoService = questionInfoService;
    }

    /**
     * 创建试题
     *
     * @param questionInfo 题目对象
     * @author jie
     **/
    @PostMapping("createQuestion")
    public ResponseEntity<ReplyMessage> createQuestion(@RequestBody QuestionInfo questionInfo) {
        ReplyMessage message = new ReplyMessage();
        try {
            questionInfoService.createQuestion(questionInfo);
        } catch (Exception e) {
            message.setSuccess(false);
            message.setMessage("标题或描述信息过长");
            return ResponseEntity.badRequest().body(message);
        }
        message.setSuccess(true);
        message.setMessage(questionInfo.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * 检查试题id是否存在
     *
     * @param id 题目的id
     * @author jie
     **/
    @GetMapping("checkId/{id}")
    public ResponseEntity<ReplyMessage<QuestionMessage>> checkIdIfExist(HttpServletRequest request, @PathVariable String id) {
        String value = CookieUtils.getCookieValue(request, USER_COOKIE, true);
        String username = "";
        try {
            username = MAPPER.readValue(value, TeacherInfo.class).getUsername();
        } catch (Exception e) {
            username = null;
        }
        ReplyMessage<QuestionMessage> message = null;
        try {
            message = questionInfoService.checkIdIfExist(id, username);
            if (!message.isSuccess()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
            }
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        message = new ReplyMessage<>();
        message.setMessage("服务器内部错误,请稍后重试或联系管理员");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    /**
     * 上传素材word
     *
     * @param id         题目的id
     * @param uploadFile 上传的文件
     * @author jie
     **/
    @PostMapping("upload/raw/{type}/{id}")
    public ResponseEntity<ReplyMessage> uploadRawFile(@RequestParam("file") MultipartFile uploadFile, @PathVariable("type") String type, @PathVariable("id") String id) {
        try {
            ReplyMessage message = questionInfoService.uploadRawFile(uploadFile, type, id);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 上传某一操作步骤的文件
     *
     * @param id         题目的id
     * @param uploadFile 上传的文件
     * @param type       试题类型
     * @param step       试题某一步骤的计数
     * @author jie
     **/
    @PostMapping("upload/step/{type}/{id}/{step}")
    public ResponseEntity<ReplyMessage> uploadStepFile(@RequestParam("file") MultipartFile uploadFile, @PathVariable("type") String type,
                                                       @PathVariable("id") String id, @PathVariable("step") Integer step) {
        try {
            ReplyMessage message = questionInfoService.uploadStepFile(uploadFile, type, id, step);
            if (message.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 更新步骤描述信息
     *
     * @param questionStep 试题步骤对象
     * @author jie
     **/
    @PostMapping("stepDescription")
    public ResponseEntity submitStepDescription(@RequestBody QuestionStep questionStep) {
        try {
            questionInfoService.addQuestionStep(questionStep);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取试题某一步骤的描述信息
     *
     * @param id   题目的id
     * @param step 试题步骤数
     * @param type 试题类型
     * @author jie
     **/
    @GetMapping("getStepDescription")
    public ResponseEntity<ReplyMessage> getStepDescription(@RequestParam(value = "id", defaultValue = "") String id,
                                                           @RequestParam(value = "type", defaultValue = "") String type,
                                                           @RequestParam(value = "step", defaultValue = "2") Integer step) {
        try {
            ReplyMessage message = questionInfoService.getStepDescription(id, type, step);
            if (message.isSuccess()) {
                return ResponseEntity.ok(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 进行xml文件的比对
     *
     * @param questionInfo 试题对象
     * @author jie
     **/
    @PostMapping("completeEdit")
    public ResponseEntity<ReplyMessage> completeEdit(@RequestBody QuestionInfo questionInfo) {
        ReplyMessage message = questionInfoService.generateXmlDifference(questionInfo);
        if (!message.isSuccess()) {
            return ResponseEntity.badRequest().body(message);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 分页获取用户试题信息
     *
     * @param types   试题类型
     * @param sort    排序方式,取值asc或desc
     * @param column  待排序的列名
     * @param page    当前页码
     * @param request 请求
     * @author jie
     **/
    @GetMapping("getQuestionInfo")
    public ResponseEntity<PageResult<QuestionInfo>> getQuestionInfo
    (HttpServletRequest request,
     @RequestParam(value = "types", required = false) String[] types,
     @RequestParam(value = "page", defaultValue = "1") Integer page,
     @RequestParam(value = "column", required = false) String column,
     @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        String username = null;
        try {
            username = getUsername(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        try {
            PageResult<QuestionInfo> questionInfos = questionInfoService.getQuestionInfo(types, page, column, sort, username, null, false);
            return ResponseEntity.ok(questionInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 分页获取所有试题信息
     *
     * @param types  试题类型
     * @param sort   排序方式,取值asc或desc
     * @param column 待排序的列名
     * @param page   当前页码
     * @author jie
     **/
    @GetMapping("getAllQuestionInfo")
    public ResponseEntity<PageResult<QuestionInfo>> getAllQuestionInfo
    (@RequestParam(value = "types", required = false) String[] types,
     @RequestParam(value = "page", defaultValue = "1") Integer page,
     @RequestParam(value = "column", required = false) String column,
     @RequestParam(value = "sort", defaultValue = "asc") String sort,
     @RequestParam(value = "search", required = false) String search) {
        try {
            PageResult<QuestionInfo> questionInfos = questionInfoService.getQuestionInfo(types, page, column, sort, null, search, true);
            return ResponseEntity.ok(questionInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 删除word试题
     *
     * @param id           试题id
     * @param request      请求
     * @param questionType 试题类型
     * @author jie
     **/
    @DeleteMapping("deleteQuestionInfo")
    public ResponseEntity deleteQuestionInfo(HttpServletRequest request, @RequestParam("id") String id, @RequestParam("questionType") String questionType) {
        try {
            questionInfoService.deleteQuestionInfo(id, questionType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 工具类:获取用户信息
     *
     * @param request 请求
     * @author jie
     **/
    private static String getUsername(HttpServletRequest request) throws Exception {
        String value = CookieUtils.getCookieValue(request, USER_COOKIE, true);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return MAPPER.readValue(value, TeacherInfo.class).getUsername();
    }

    /**
     * 下载步骤文件
     *
     * @param questionType 试题类型
     * @param id           试题id
     * @param step         试题步骤
     * @param response     响应
     * @author jie
     **/
    @GetMapping("download/{questionType}/{id}/{step}")
    public ResponseEntity downloadStepFile(HttpServletRequest request, HttpServletResponse response,
                                           @PathVariable("questionType") String questionType, @PathVariable("id") String id, @PathVariable("step") Integer step) {
        try {
            String username = getUsername(request);
            String filename = questionInfoService.downloadStepFile(response, questionType, id, username, step);
            return ResponseEntity.ok().body(filename);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}