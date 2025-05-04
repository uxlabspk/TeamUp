package io.github.uxlabspk.teamup.controller;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.Message;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.service.ChannelService;
import io.github.uxlabspk.teamup.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/workspaces/{workspaceId}/channels/{channelId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ChannelService channelService;

    @GetMapping
    public String getMessages(@PathVariable("workspaceId") Long workspaceId,
                             @PathVariable("channelId") Long channelId,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "20") int size,
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
            
            // Get messages with pagination
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Message> messages = messageService.getMessagesByChannel(channel, pageRequest);
            
            model.addAttribute("workspace", channel.getWorkspace());
            model.addAttribute("channel", channel);
            model.addAttribute("messages", messages);
            model.addAttribute("currentUser", user);
            model.addAttribute("newMessage", new Message());
            
            return "message/list";
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
    }

    @PostMapping
    public String sendMessage(@PathVariable("workspaceId") Long workspaceId,
                             @PathVariable("channelId") Long channelId,
                             @ModelAttribute("newMessage") Message message,
                             RedirectAttributes redirectAttributes) {
        
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);
        
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            
            // Check if channel belongs to the specified workspace
            if (!channel.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            // Check if user is a member of the channel
            if (!channel.getMembers().contains(user) && channel.getType() != Channel.ChannelType.PUBLIC) {
                redirectAttributes.addFlashAttribute("errorMessage", "You are not a member of this channel");
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            try {
                // Send the message
                messageService.sendMessage(message, channel, user);
                
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to send message: " + e.getMessage());
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            }
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
    }

    @GetMapping("/{messageId}")
    public String viewMessage(@PathVariable("workspaceId") Long workspaceId,
                             @PathVariable("channelId") Long channelId,
                             @PathVariable("messageId") Long messageId,
                             Model model) {
        
        Optional<Message> messageOpt = messageService.getMessageById(messageId);
        
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            
            // Check if message belongs to the specified channel
            if (!message.getChannel().getId().equals(channelId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            }
            
            // Check if channel belongs to the specified workspace
            if (!message.getChannel().getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            // Check if user is a member of the channel
            Channel channel = message.getChannel();
            if (!channel.getMembers().contains(user) && channel.getType() != Channel.ChannelType.PUBLIC) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            model.addAttribute("workspace", channel.getWorkspace());
            model.addAttribute("channel", channel);
            model.addAttribute("message", message);
            model.addAttribute("currentUser", user);
            
            // Get replies to this message
            List<Message> replies = messageService.getRepliesByParentMessage(message);
            model.addAttribute("replies", replies);
            
            // For new reply
            model.addAttribute("newReply", new Message());
            
            return "message/view";
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
        }
    }

    @PostMapping("/{messageId}/reply")
    public String replyToMessage(@PathVariable("workspaceId") Long workspaceId,
                                @PathVariable("channelId") Long channelId,
                                @PathVariable("messageId") Long messageId,
                                @ModelAttribute("newReply") Message reply,
                                RedirectAttributes redirectAttributes) {
        
        Optional<Message> parentMessageOpt = messageService.getMessageById(messageId);
        
        if (parentMessageOpt.isPresent()) {
            Message parentMessage = parentMessageOpt.get();
            
            // Check if parent message belongs to the specified channel
            if (!parentMessage.getChannel().getId().equals(channelId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            }
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            try {
                // Send the reply
                messageService.replyToMessage(reply, parentMessage, user);
                
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages/" + messageId;
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to send reply: " + e.getMessage());
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages/" + messageId;
            }
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
        }
    }

    @PostMapping("/{messageId}/edit")
    public String editMessage(@PathVariable("workspaceId") Long workspaceId,
                             @PathVariable("channelId") Long channelId,
                             @PathVariable("messageId") Long messageId,
                             @RequestParam("content") String content,
                             RedirectAttributes redirectAttributes) {
        
        Optional<Message> messageOpt = messageService.getMessageById(messageId);
        
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            
            // Check if message belongs to the specified channel
            if (!message.getChannel().getId().equals(channelId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            }
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            // Check if user is the sender of the message
            if (!message.getSender().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own messages");
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            }
            
            try {
                // Update the message
                message.setContent(content);
                message.setUpdatedAt(LocalDateTime.now());
                messageService.updateMessage(message);
                
                redirectAttributes.addFlashAttribute("successMessage", "Message updated successfully");
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update message: " + e.getMessage());
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            }
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
        }
    }

    @PostMapping("/{messageId}/delete")
    public String deleteMessage(@PathVariable("workspaceId") Long workspaceId,
                               @PathVariable("channelId") Long channelId,
                               @PathVariable("messageId") Long messageId,
                               RedirectAttributes redirectAttributes) {
        
        Optional<Message> messageOpt = messageService.getMessageById(messageId);
        
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            
            // Check if message belongs to the specified channel
            if (!message.getChannel().getId().equals(channelId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            }
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            // Check if user is the sender of the message or workspace owner
            if (!message.getSender().getId().equals(user.getId()) && 
                !message.getChannel().getWorkspace().getOwner().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only delete your own messages");
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            }
            
            try {
                // Delete the message
                messageService.deleteMessage(messageId);
                
                redirectAttributes.addFlashAttribute("successMessage", "Message deleted successfully");
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete message: " + e.getMessage());
                return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
            }
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
        }
    }

    @PostMapping("/{messageId}/reaction")
    public String addReaction(@PathVariable("workspaceId") Long workspaceId,
                             @PathVariable("channelId") Long channelId,
                             @PathVariable("messageId") Long messageId,
                             @RequestParam("reactionType") String reactionType,
                             RedirectAttributes redirectAttributes) {
        
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        try {
            // Add reaction
            messageService.addReaction(messageId, user.getId(), reactionType);
            
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add reaction: " + e.getMessage());
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
        }
    }

    @PostMapping("/{messageId}/reaction/remove")
    public String removeReaction(@PathVariable("workspaceId") Long workspaceId,
                                @PathVariable("channelId") Long channelId,
                                @PathVariable("messageId") Long messageId,
                                RedirectAttributes redirectAttributes) {
        
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        try {
            // Remove reaction
            messageService.removeReaction(messageId, user.getId());
            
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove reaction: " + e.getMessage());
            return "redirect:/workspaces/" + workspaceId + "/channels/" + channelId + "/messages";
        }
    }

    @GetMapping("/search")
    public String searchMessages(@PathVariable("workspaceId") Long workspaceId,
                                @PathVariable("channelId") Long channelId,
                                @RequestParam("keyword") String keyword,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "20") int size,
                                Model model) {
        
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);
        
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            
            // Check if channel belongs to the specified workspace
            if (!channel.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            // Check if user is a member of the channel
            if (!channel.getMembers().contains(user) && channel.getType() != Channel.ChannelType.PUBLIC) {
                return "redirect:/workspaces/" + workspaceId + "/channels";
            }
            
            // Search messages
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<Message> messages = messageService.searchMessagesInChannel(channelId, keyword, pageRequest);
            
            model.addAttribute("workspace", channel.getWorkspace());
            model.addAttribute("channel", channel);
            model.addAttribute("messages", messages);
            model.addAttribute("keyword", keyword);
            model.addAttribute("currentUser", user);
            
            return "message/search";
        } else {
            return "redirect:/workspaces/" + workspaceId + "/channels";
        }
    }

    @GetMapping("/new")
    @ResponseBody
    public ResponseEntity<?> getNewMessages(@PathVariable("workspaceId") Long workspaceId,
                                           @PathVariable("channelId") Long channelId,
                                           @RequestParam("since") String sinceStr) {
        
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);
        
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            
            // Check if channel belongs to the specified workspace
            if (!channel.getWorkspace().getId().equals(workspaceId)) {
                return ResponseEntity.badRequest().body("Invalid workspace or channel");
            }
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            // Check if user is a member of the channel
            if (!channel.getMembers().contains(user) && channel.getType() != Channel.ChannelType.PUBLIC) {
                return ResponseEntity.badRequest().body("Not a member of this channel");
            }
            
            try {
                // Parse the since parameter
                LocalDateTime since = LocalDateTime.parse(sinceStr);
                
                // Get new messages
                List<Message> newMessages = messageService.getNewMessagesByChannel(channel, since);
                
                // Count new messages
                long count = messageService.countNewMessagesInChannel(channelId, since);
                
                Map<String, Object> response = new HashMap<>();
                response.put("messages", newMessages);
                response.put("count", count);
                
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid date format");
            }
        } else {
            return ResponseEntity.badRequest().body("Channel not found");
        }
    }
}