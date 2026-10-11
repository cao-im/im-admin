package com.caoim.admin.service;

import com.caoim.imcore.client.AdminImClient;
import com.caoim.imcore.common.Result;
import com.caoim.imcore.dto.AdminMessageVO;
import com.caoim.imcore.dto.AdminOverviewDTO;
import com.caoim.imcore.dto.PageResult;
import com.caoim.imcore.entity.Group;
import com.caoim.imcore.entity.SensitiveWord;
import com.caoim.imcore.entity.SystemConfig;
import com.caoim.imcore.entity.User;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ImProxyService {

    private final AdminImClient adminImClient;
    private final AuditService auditService;

    public ImProxyService(AdminImClient adminImClient, AuditService auditService) {
        this.adminImClient = adminImClient;
        this.auditService = auditService;
    }

    public Result<PageResult<User>> pageUsers(String keyword, int page, int size) {
        return call(() -> adminImClient.pageUsers(keyword, page, size));
    }

    public Result<Void> setUserEnabled(Long id, Integer enabled, HttpServletRequest request) {
        Result<Void> result = call(() -> adminImClient.setUserEnabled(id, enabled));
        if (result.getCode() == 200) {
            auditService.record(request, enabled != null && enabled == 0 ? "禁用用户" : "启用用户", "USER", String.valueOf(id), "", true);
        }
        return result;
    }

    public Result<PageResult<Group>> pageGroups(String keyword, int page, int size) {
        return call(() -> adminImClient.pageGroups(keyword, page, size));
    }

    public Result<Void> dismissGroup(Long id, HttpServletRequest request) {
        Result<Void> result = call(() -> adminImClient.dismissGroup(id));
        if (result.getCode() == 200) {
            auditService.record(request, "解散群", "GROUP", String.valueOf(id), "", true);
        }
        return result;
    }

    public Result<PageResult<AdminMessageVO>> pageMessages(Long userId, Long groupId, String keyword,
                                                           String startTime, String endTime, int page, int size) {
        return call(() -> adminImClient.pageMessages(userId, groupId, keyword, startTime, endTime, page, size));
    }

    public Result<AdminOverviewDTO> overview() {
        return call(adminImClient::overview);
    }

    public Result<PageResult<SensitiveWord>> pageSensitiveWords(String keyword, int page, int size) {
        return call(() -> adminImClient.pageSensitiveWords(keyword, page, size));
    }

    public Result<SensitiveWord> createSensitiveWord(String word, HttpServletRequest request) {
        Result<SensitiveWord> result = call(() -> adminImClient.createSensitiveWord(Map.of("word", word == null ? "" : word)));
        if (result.getCode() == 200) {
            auditService.record(request, "新增敏感词", "SENSITIVE", "", word, true);
        }
        return result;
    }

    public Result<Void> setSensitiveEnabled(Long id, Integer enabled, HttpServletRequest request) {
        Result<Void> result = call(() -> adminImClient.setSensitiveWordEnabled(id, enabled));
        if (result.getCode() == 200) {
            auditService.record(request, "更新敏感词", "SENSITIVE", String.valueOf(id), "enabled=" + enabled, true);
        }
        return result;
    }

    public Result<Void> deleteSensitive(Long id, HttpServletRequest request) {
        Result<Void> result = call(() -> adminImClient.deleteSensitiveWord(id));
        if (result.getCode() == 200) {
            auditService.record(request, "删除敏感词", "SENSITIVE", String.valueOf(id), "", true);
        }
        return result;
    }

    public Result<List<SystemConfig>> listConfig() {
        return call(adminImClient::listConfig);
    }

    public Result<Void> updateConfig(Map<String, String> values, HttpServletRequest request) {
        Result<Void> result = call(() -> adminImClient.updateConfig(values));
        if (result.getCode() == 200) {
            auditService.record(request, "修改系统配置", "CONFIG", "", String.valueOf(values), true);
        }
        return result;
    }

    private <T> Result<T> call(java.util.function.Supplier<Result<T>> supplier) {
        try {
            Result<T> result = supplier.get();
            return result == null ? Result.error("IM服务无响应") : result;
        } catch (FeignException e) {
            return Result.error("IM服务不可用");
        }
    }
}
