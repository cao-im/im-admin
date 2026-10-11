package com.caoim.admin.controller;

import com.caoim.admin.security.RequirePermission;
import com.caoim.admin.service.ImProxyService;
import com.caoim.imcore.common.Result;
import com.caoim.imcore.dto.AdminMessageVO;
import com.caoim.imcore.dto.AdminOverviewDTO;
import com.caoim.imcore.dto.PageResult;
import com.caoim.imcore.entity.Group;
import com.caoim.imcore.entity.SensitiveWord;
import com.caoim.imcore.entity.SystemConfig;
import com.caoim.imcore.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/im")
public class ImController {

    private final ImProxyService imProxyService;

    public ImController(ImProxyService imProxyService) {
        this.imProxyService = imProxyService;
    }

    @GetMapping("/stats")
    @RequirePermission("dashboard:view")
    public Result<AdminOverviewDTO> stats() {
        return imProxyService.overview();
    }

    @GetMapping("/users")
    @RequirePermission("im:user:list")
    public Result<PageResult<User>> users(@RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return imProxyService.pageUsers(keyword, page, size);
    }

    @PutMapping("/users/{id}/enabled")
    @RequirePermission("im:user:enable")
    public Result<Void> setUserEnabled(@PathVariable Long id, @RequestParam Integer enabled, HttpServletRequest request) {
        return imProxyService.setUserEnabled(id, enabled, request);
    }

    @GetMapping("/groups")
    @RequirePermission("im:group:list")
    public Result<PageResult<Group>> groups(@RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return imProxyService.pageGroups(keyword, page, size);
    }

    @PutMapping("/groups/{id}/dismiss")
    @RequirePermission("im:group:dismiss")
    public Result<Void> dismiss(@PathVariable Long id, HttpServletRequest request) {
        return imProxyService.dismissGroup(id, request);
    }

    @GetMapping("/messages")
    @RequirePermission("im:message:list")
    public Result<PageResult<AdminMessageVO>> messages(@RequestParam(required = false) Long userId,
                                                       @RequestParam(required = false) Long groupId,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String startTime,
                                                       @RequestParam(required = false) String endTime,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return imProxyService.pageMessages(userId, groupId, keyword, startTime, endTime, page, size);
    }

    @GetMapping("/sensitive-words")
    @RequirePermission("im:sensitive:list")
    public Result<PageResult<SensitiveWord>> sensitiveWords(@RequestParam(required = false) String keyword,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return imProxyService.pageSensitiveWords(keyword, page, size);
    }

    @PostMapping("/sensitive-words")
    @RequirePermission("im:sensitive:edit")
    public Result<SensitiveWord> createSensitive(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return imProxyService.createSensitiveWord(body == null ? null : body.get("word"), request);
    }

    @PutMapping("/sensitive-words/{id}/enabled")
    @RequirePermission("im:sensitive:edit")
    public Result<Void> setSensitiveEnabled(@PathVariable Long id, @RequestParam Integer enabled, HttpServletRequest request) {
        return imProxyService.setSensitiveEnabled(id, enabled, request);
    }

    @DeleteMapping("/sensitive-words/{id}")
    @RequirePermission("im:sensitive:edit")
    public Result<Void> deleteSensitive(@PathVariable Long id, HttpServletRequest request) {
        return imProxyService.deleteSensitive(id, request);
    }

    @GetMapping("/configs")
    @RequirePermission("im:config:list")
    public Result<List<SystemConfig>> configs() {
        return imProxyService.listConfig();
    }

    @PutMapping("/configs")
    @RequirePermission("im:config:edit")
    public Result<Void> updateConfigs(@RequestBody Map<String, String> values, HttpServletRequest request) {
        return imProxyService.updateConfig(values, request);
    }
}
