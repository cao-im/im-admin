package com.caoim.admin.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuNode {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String permission;
    private String icon;
    private Integer menuType;
    private Integer sortOrder;
    private List<MenuNode> children = new ArrayList<>();
}
