package io.github.uxlabspk.teamup.controller;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;
import io.github.uxlabspk.teamup.service.ChannelService;
import io.github.uxlabspk.teamup.service.UserService;
import io.github.uxlabspk.teamup.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/workspaces/{workspaceId}/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;
    private final WorkspaceService workspaceService;
    private final UserService userService;

    @GetMapping
    public String listChannels(@PathVariable("workspaceId") Long workspaceId, Model model) {
        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(workspaceId);
        
        if (workspaceOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();
            model.addAttribute("workspace", workspace);
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            // Get channels for this user in this workspace
            List<Channel> channels = channelService.getChannelsByUserIdAndWorkspaceId(user.getId(), workspaceId);
            model.addAttribute("channels", channels);
            
            return "channel/list";
        } else {
            return "redirect:/workspaces";
        }
    }

    @GetMapping("/create")
    public String createChannelForm(@PathVariable("workspaceId") Long workspaceId, Model model) {
        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(workspaceId);
        
        if (workspaceOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();
            model.addAttribute("workspace", workspace);
            model.addAttribute("channel", new Channel());
            model.addAttribute("channelTypes", Channel.ChannelType.values());
            
            return "channel/create";
        } else {
            return "redirect:/workspaces";
        }
    }

    @PostMapping("/create")
    public String createChannel(@PathVariable("workspaceId") Long workspaceId,
                               @Valid @ModelAttribute("channel") Channel channel,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        
        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(workspaceId);
        
        if (!workspaceOpt.isPresent()) {
            return "redirect:/workspaces";
        }
        
        Workspace workspace = workspaceOpt.get();
        
        if (result.hasErrors()) {
            return "channel/create";
        }
        
        // Check if channel name already exists in this workspace
        if (channelService.existsByNameAndWorkspace(channel.getName(), workspace)) {
            result.rejectValue("name", "error.channel", "Channel name already exists in this workspace");
            return "channel/create";
        }
        
        try {
            // Create the channel
            Channel createdChannel = channelService.createChannel(channel, workspace);
            
            // Add current user to channel members
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            channelService.addUserToChannel(createdChannel.getId(), user.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Channel created successfully");
            return "redirect:/workspaces/" + workspaceId + "/channels";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Channel creation failed: " + e.getMessage());
            return "redirect:/workspaces/" + workspaceId + "/channels/create";
        }
    }

    @GetMapping("/{channelId}")
    public String viewChannel(@PathVariable("workspaceId") Long workspaceId,
                             @PathVariable("channelId") Long channelId,
                             Model model) {
        
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);
        
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            
            // Check if channel belongs to the specified workspace
            if (!channel.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            // Check if current user is a member of the channel
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            if (!channel.getMembers().contains(user) && channel.getType() != Channel.ChannelType.PUBLIC) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            model.addAttribute("workspace", channel.getWorkspace());
            model.addAttribute("channel", channel);
            
            return "channel/view";
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
    }

    @GetMapping("/{channelId}/edit")
    public String editChannelForm(@PathVariable("workspaceId") Long workspaceId,
                                 @PathVariable("channelId") Long channelId,
                                 Model model) {
        
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);
        
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            
            // Check if channel belongs to the specified workspace
            if (!channel.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            // Check if current user is a workspace owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            if (!channel.getWorkspace().getOwner().getId().equals(user.getId())) {
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId;
            }
            
            model.addAttribute("workspace", channel.getWorkspace());
            model.addAttribute("channel", channel);
            model.addAttribute("channelTypes", Channel.ChannelType.values());
            
            return "channel/edit";
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
    }

    @PostMapping("/{channelId}/edit")
    public String updateChannel(@PathVariable("workspaceId") Long workspaceId,
                               @PathVariable("channelId") Long channelId,
                               @Valid @ModelAttribute("channel") Channel channel,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        
        Optional<Channel> existingChannelOpt = channelService.getChannelById(channelId);
        
        if (!existingChannelOpt.isPresent()) {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
        
        Channel existingChannel = existingChannelOpt.get();
        
        // Check if channel belongs to the specified workspace
        if (!existingChannel.getWorkspace().getId().equals(workspaceId)) {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
        
        // Check if current user is a workspace owner
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        if (!existingChannel.getWorkspace().getOwner().getId().equals(user.getId())) {
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId;
        }
        
        if (result.hasErrors()) {
            return "channel/edit";
        }
        
        // Check if name is changed and already exists in this workspace
        if (!existingChannel.getName().equals(channel.getName()) && 
            channelService.existsByNameAndWorkspace(channel.getName(), existingChannel.getWorkspace())) {
            result.rejectValue("name", "error.channel", "Channel name already exists in this workspace");
            return "channel/edit";
        }
        
        try {
            // Update only allowed fields
            existingChannel.setName(channel.getName());
            existingChannel.setDescription(channel.getDescription());
            existingChannel.setType(channel.getType());
            
            channelService.updateChannel(existingChannel);
            
            redirectAttributes.addFlashAttribute("successMessage", "Channel updated successfully");
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Channel update failed: " + e.getMessage());
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/edit";
        }
    }

    @PostMapping("/{channelId}/delete")
    public String deleteChannel(@PathVariable("workspaceId") Long workspaceId,
                               @PathVariable("channelId") Long channelId,
                               RedirectAttributes redirectAttributes) {
        
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);
        
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            
            // Check if channel belongs to the specified workspace
            if (!channel.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            // Check if current user is a workspace owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            if (!channel.getWorkspace().getOwner().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to delete this channel");
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId;
            }
            
            try {
                channelService.deleteChannel(channelId);
                redirectAttributes.addFlashAttribute("successMessage", "Channel deleted successfully");
                return "redirect:/workspaces/" + workspaceId + "/channels";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Channel deletion failed: " + e.getMessage());
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId;
            }
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
    }

    @GetMapping("/{channelId}/members")
    public String listMembers(@PathVariable("workspaceId") Long workspaceId,
                             @PathVariable("channelId") Long channelId,
                             Model model) {
        
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);
        
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            
            // Check if channel belongs to the specified workspace
            if (!channel.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            model.addAttribute("workspace", channel.getWorkspace());
            model.addAttribute("channel", channel);
            
            List<User> members = channelService.getChannelMembers(channelId);
            model.addAttribute("members", members);
            
            // Get workspace members for adding to channel
            List<User> workspaceMembers = workspaceService.getWorkspaceMembers(workspaceId);
            model.addAttribute("workspaceMembers", workspaceMembers);
            
            return "channel/members";
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
    }

    @PostMapping("/{channelId}/members/add")
    public String addMember(@PathVariable("workspaceId") Long workspaceId,
                           @PathVariable("channelId") Long channelId,
                           @RequestParam("userId") Long userId,
                           RedirectAttributes redirectAttributes) {
        
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);
        
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            
            // Check if channel belongs to the specified workspace
            if (!channel.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            try {
                channelService.addUserToChannel(channelId, userId);
                redirectAttributes.addFlashAttribute("successMessage", "Member added successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to add member: " + e.getMessage());
            }
            
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/members";
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
    }

    @PostMapping("/{channelId}/members/remove")
    public String removeMember(@PathVariable("workspaceId") Long workspaceId,
                              @PathVariable("channelId") Long channelId,
                              @RequestParam("userId") Long userId,
                              RedirectAttributes redirectAttributes) {
        
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);
        
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            
            // Check if channel belongs to the specified workspace
            if (!channel.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            try {
                channelService.removeUserFromChannel(channelId, userId);
                redirectAttributes.addFlashAttribute("successMessage", "Member removed successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove member: " + e.getMessage());
            }
            
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/members";
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
    }

    @GetMapping("/direct/{userId}")
    public String getOrCreateDirectChannel(@PathVariable("workspaceId") Long workspaceId,
                                          @PathVariable("userId") Long userId,
                                          RedirectAttributes redirectAttributes) {
        
        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(workspaceId);
        Optional<User> targetUserOpt = userService.getUserById(userId);
        
        if (!workspaceOpt.isPresent() || !targetUserOpt.isPresent()) {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
        
        Workspace workspace = workspaceOpt.get();
        User targetUser = targetUserOpt.get();
        
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        try {
            // Check if direct channel already exists
            Optional<Channel> existingChannelOpt = channelService.getDirectChannelBetweenUsers(
                    workspaceId, currentUser.getId(), targetUser.getId());
            
            if (existingChannelOpt.isPresent()) {
                Channel existingChannel = existingChannelOpt.get();
                return "redirect:/workspaces/" + workspaceId + "/channels/" + existingChannel.getId();
            }
            
            // Create new direct channel
            Channel directChannel = channelService.createDirectChannel(workspace, currentUser, targetUser);
            
            return "redirect:/workspaces/" + workspaceId + "/channels/" + directChannel.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create direct channel: " + e.getMessage());
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
    }
}