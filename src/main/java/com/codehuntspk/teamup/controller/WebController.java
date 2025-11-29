package com.codehuntspk.teamup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/workspace")
    public String workspace(Model model) {
        return "workspace";
    }

    @GetMapping("/workspace/{workspaceId}")
    public String workspaceDetail(@PathVariable Long workspaceId, Model model) {
        model.addAttribute("workspaceId", workspaceId);
        return "workspace";
    }

    @GetMapping("/channel/{channelId}")
    public String channel(@PathVariable Long channelId, Model model) {
        model.addAttribute("channelId", channelId);
        return "channel";
    }

    @GetMapping("/meeting/{meetingId}")
    public String meeting(@PathVariable Long meetingId, Model model) {
        model.addAttribute("meetingId", meetingId);
        return "meeting";
    }
}

