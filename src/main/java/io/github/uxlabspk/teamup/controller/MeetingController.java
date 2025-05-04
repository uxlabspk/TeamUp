package io.github.uxlabspk.teamup.controller;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.Meeting;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;
import io.github.uxlabspk.teamup.service.ChannelService;
import io.github.uxlabspk.teamup.service.MeetingService;
import io.github.uxlabspk.teamup.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/workspaces/{workspaceId}/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final WorkspaceService workspaceService;
    private final ChannelService channelService;

    @GetMapping
    public String listMeetings(@PathVariable("workspaceId") Long workspaceId, Model model) {
        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(workspaceId);

        if (workspaceOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();
            model.addAttribute("workspace", workspace);

            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                User user = (User) auth.getPrincipal();

                try {
                    // Get upcoming meetings for this workspace
                    List<Meeting> upcomingMeetings = meetingService.getUpcomingMeetingsByWorkspace(workspaceId);
                    model.addAttribute("upcomingMeetings", upcomingMeetings);

                    // Get ongoing meetings
                    List<Meeting> ongoingMeetings = meetingService.getOngoingMeetings();
                    // Filter for this workspace
                    ongoingMeetings.removeIf(meeting -> !meeting.getWorkspace().getId().equals(workspaceId));
                    model.addAttribute("ongoingMeetings", ongoingMeetings);

                    // Get meetings where user is a participant
                    List<Meeting> userMeetings = meetingService.getMeetingsByParticipant(user.getId());
                    // Filter for this workspace
                    userMeetings.removeIf(meeting -> !meeting.getWorkspace().getId().equals(workspaceId));
                    model.addAttribute("userMeetings", userMeetings);

                    return "meeting/list";
                } catch (Exception e) {
                    // Log the exception
                    System.err.println("Error loading meetings: " + e.getMessage());
                    model.addAttribute("errorMessage", "Error loading meetings. Please try again.");
                    return "redirect:/workspaces/" + workspaceId;
                }
            } else {
                return "redirect:/login";
            }
        } else {
            return "redirect:/workspaces";
        }
    }

    @GetMapping("/schedule")
    public String scheduleMeetingForm(@PathVariable("workspaceId") Long workspaceId, Model model) {
        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(workspaceId);

        if (workspaceOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();
            model.addAttribute("workspace", workspace);
            model.addAttribute("meeting", new Meeting());

            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            // Get channels for this user in this workspace
            List<Channel> channels = channelService.getChannelsByUserIdAndWorkspaceId(user.getId(), workspaceId);
            model.addAttribute("channels", channels);

            return "meeting/schedule";
        } else {
            return "redirect:/workspaces";
        }
    }

    @PostMapping("/schedule")
    public String scheduleMeeting(@PathVariable("workspaceId") Long workspaceId,
                                 @Valid @ModelAttribute("meeting") Meeting meeting,
                                 @RequestParam("channelId") Long channelId,
                                 @RequestParam("startDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {

        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(workspaceId);
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);

        if (!workspaceOpt.isPresent() || !channelOpt.isPresent()) {
            return "redirect:/workspaces";
        }

        Workspace workspace = workspaceOpt.get();
        Channel channel = channelOpt.get();

        if (result.hasErrors()) {
            return "meeting/schedule";
        }

        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        try {
            // Set start time
            meeting.setStartTime(startDateTime);

            // Schedule the meeting
            Meeting scheduledMeeting = meetingService.scheduleMeeting(meeting, workspace, channel, user);

            redirectAttributes.addFlashAttribute("successMessage", "Meeting scheduled successfully");
            return "redirect:/workspaces/" + workspaceId + "/meetings/" + scheduledMeeting.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Meeting scheduling failed: " + e.getMessage());
            return "redirect:/workspaces/" + workspaceId + "/meetings/schedule";
        }
    }

    @GetMapping("/{meetingId}")
    public String viewMeeting(@PathVariable("workspaceId") Long workspaceId,
                             @PathVariable("meetingId") Long meetingId,
                             Model model) {

        Optional<Meeting> meetingOpt = meetingService.getMeetingById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            // Check if meeting belongs to the specified workspace
            if (!meeting.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            }

            model.addAttribute("workspace", meeting.getWorkspace());
            model.addAttribute("meeting", meeting);

            // Get meeting participants
            Set<User> participants = meetingService.getMeetingParticipants(meetingId);
            model.addAttribute("participants", participants);

            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            model.addAttribute("currentUser", user);

            // Check if user is the creator
            boolean isCreator = meeting.getCreator().getId().equals(user.getId());
            model.addAttribute("isCreator", isCreator);

            // Check if user is a participant
            boolean isParticipant = participants.contains(user);
            model.addAttribute("isParticipant", isParticipant);

            return "meeting/view";
        } else {
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }
    }

    @GetMapping("/{meetingId}/edit")
    public String editMeetingForm(@PathVariable("workspaceId") Long workspaceId,
                                 @PathVariable("meetingId") Long meetingId,
                                 Model model) {

        Optional<Meeting> meetingOpt = meetingService.getMeetingById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            // Check if meeting belongs to the specified workspace
            if (!meeting.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            }

            // Check if current user is the creator
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!meeting.getCreator().getId().equals(user.getId())) {
                return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
            }

            model.addAttribute("workspace", meeting.getWorkspace());
            model.addAttribute("meeting", meeting);

            // Get channels for this user in this workspace
            List<Channel> channels = channelService.getChannelsByUserIdAndWorkspaceId(user.getId(), workspaceId);
            model.addAttribute("channels", channels);

            return "meeting/edit";
        } else {
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }
    }

    @PostMapping("/{meetingId}/edit")
    public String updateMeeting(@PathVariable("workspaceId") Long workspaceId,
                               @PathVariable("meetingId") Long meetingId,
                               @Valid @ModelAttribute("meeting") Meeting meeting,
                               @RequestParam("channelId") Long channelId,
                               @RequestParam("startDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        Optional<Meeting> existingMeetingOpt = meetingService.getMeetingById(meetingId);
        Optional<Channel> channelOpt = channelService.getChannelById(channelId);

        if (!existingMeetingOpt.isPresent() || !channelOpt.isPresent()) {
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }

        Meeting existingMeeting = existingMeetingOpt.get();
        Channel channel = channelOpt.get();

        // Check if meeting belongs to the specified workspace
        if (!existingMeeting.getWorkspace().getId().equals(workspaceId)) {
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }

        // Check if current user is the creator
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        if (!existingMeeting.getCreator().getId().equals(user.getId())) {
            return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
        }

        if (result.hasErrors()) {
            return "meeting/edit";
        }

        try {
            // Update only allowed fields
            existingMeeting.setTitle(meeting.getTitle());
            existingMeeting.setDescription(meeting.getDescription());
            existingMeeting.setStartTime(startDateTime);
            existingMeeting.setChannel(channel);

            meetingService.updateMeeting(existingMeeting);

            redirectAttributes.addFlashAttribute("successMessage", "Meeting updated successfully");
            return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Meeting update failed: " + e.getMessage());
            return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId + "/edit";
        }
    }

    @PostMapping("/{meetingId}/cancel")
    public String cancelMeeting(@PathVariable("workspaceId") Long workspaceId,
                               @PathVariable("meetingId") Long meetingId,
                               RedirectAttributes redirectAttributes) {

        Optional<Meeting> meetingOpt = meetingService.getMeetingById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            // Check if meeting belongs to the specified workspace
            if (!meeting.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            }

            // Check if current user is the creator
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!meeting.getCreator().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only the meeting creator can cancel the meeting");
                return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
            }

            try {
                meetingService.cancelMeeting(meetingId);
                redirectAttributes.addFlashAttribute("successMessage", "Meeting cancelled successfully");
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Meeting cancellation failed: " + e.getMessage());
                return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
            }
        } else {
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }
    }

    @PostMapping("/{meetingId}/start")
    public String startMeeting(@PathVariable("workspaceId") Long workspaceId,
                              @PathVariable("meetingId") Long meetingId,
                              RedirectAttributes redirectAttributes) {

        Optional<Meeting> meetingOpt = meetingService.getMeetingById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            // Check if meeting belongs to the specified workspace
            if (!meeting.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            }

            // Check if current user is the creator
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!meeting.getCreator().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only the meeting creator can start the meeting");
                return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
            }

            try {
                meetingService.startMeeting(meetingId);
                redirectAttributes.addFlashAttribute("successMessage", "Meeting started successfully");
                return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId + "/join";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to start meeting: " + e.getMessage());
                return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
            }
        } else {
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }
    }

    @PostMapping("/{meetingId}/end")
    public String endMeeting(@PathVariable("workspaceId") Long workspaceId,
                            @PathVariable("meetingId") Long meetingId,
                            RedirectAttributes redirectAttributes) {

        Optional<Meeting> meetingOpt = meetingService.getMeetingById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            // Check if meeting belongs to the specified workspace
            if (!meeting.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            }

            // Check if current user is the creator
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!meeting.getCreator().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only the meeting creator can end the meeting");
                return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
            }

            try {
                meetingService.endMeeting(meetingId);
                redirectAttributes.addFlashAttribute("successMessage", "Meeting ended successfully");
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to end meeting: " + e.getMessage());
                return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
            }
        } else {
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }
    }

    @GetMapping("/{meetingId}/join")
    public String joinMeeting(@PathVariable("workspaceId") Long workspaceId,
                             @PathVariable("meetingId") Long meetingId,
                             Model model) {

        Optional<Meeting> meetingOpt = meetingService.getMeetingById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            // Check if meeting belongs to the specified workspace
            if (!meeting.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            }

            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                User user = (User) auth.getPrincipal();

                // Add user to participants if not already
                if (!meeting.getParticipants().contains(user)) {
                    try {
                        meetingService.addParticipantToMeeting(meetingId, user.getId());
                    } catch (Exception e) {
                        // Ignore if user is already a participant
                    }
                }

                model.addAttribute("workspace", meeting.getWorkspace());
                model.addAttribute("meeting", meeting);
                model.addAttribute("currentUser", user);

                return "meeting/join";
            } else {
                return "redirect:/login";
            }
        } else {
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }
    }

    @GetMapping("/join/{meetingCode}")
    public String joinMeetingByCode(@PathVariable("workspaceId") Long workspaceId,
                                   @PathVariable("meetingCode") String meetingCode,
                                   RedirectAttributes redirectAttributes) {

        Optional<Meeting> meetingOpt = meetingService.getMeetingByCode(meetingCode);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            // Check if meeting belongs to the specified workspace
            if (!meeting.getWorkspace().getId().equals(workspaceId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid meeting code for this workspace");
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            }

            return "redirect:/workspaces/" + workspaceId + "/meetings/" + meeting.getId() + "/join";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid meeting code");
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }
    }

    @PostMapping("/{meetingId}/participants/add")
    public String addParticipant(@PathVariable("workspaceId") Long workspaceId,
                                @PathVariable("meetingId") Long meetingId,
                                @RequestParam("userId") Long userId,
                                RedirectAttributes redirectAttributes) {

        Optional<Meeting> meetingOpt = meetingService.getMeetingById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            // Check if meeting belongs to the specified workspace
            if (!meeting.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            }

            // Check if current user is the creator
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!meeting.getCreator().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only the meeting creator can add participants");
                return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
            }

            try {
                meetingService.addParticipantToMeeting(meetingId, userId);
                redirectAttributes.addFlashAttribute("successMessage", "Participant added successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to add participant: " + e.getMessage());
            }

            return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
        } else {
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }
    }

    @PostMapping("/{meetingId}/participants/remove")
    public String removeParticipant(@PathVariable("workspaceId") Long workspaceId,
                                   @PathVariable("meetingId") Long meetingId,
                                   @RequestParam("userId") Long userId,
                                   RedirectAttributes redirectAttributes) {

        Optional<Meeting> meetingOpt = meetingService.getMeetingById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            // Check if meeting belongs to the specified workspace
            if (!meeting.getWorkspace().getId().equals(workspaceId)) {
                return "redirect:/workspaces/" + workspaceId + "/meetings";
            }

            // Check if current user is the creator
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!meeting.getCreator().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only the meeting creator can remove participants");
                return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
            }

            try {
                meetingService.removeParticipantFromMeeting(meetingId, userId);
                redirectAttributes.addFlashAttribute("successMessage", "Participant removed successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove participant: " + e.getMessage());
            }

            return "redirect:/workspaces/" + workspaceId + "/meetings/" + meetingId;
        } else {
            return "redirect:/workspaces/" + workspaceId + "/meetings";
        }
    }
}
