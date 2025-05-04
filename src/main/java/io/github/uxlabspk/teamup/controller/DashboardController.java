package io.github.uxlabspk.teamup.controller;

import io.github.uxlabspk.teamup.model.Meeting;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;
import io.github.uxlabspk.teamup.service.MeetingService;
import io.github.uxlabspk.teamup.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final WorkspaceService workspaceService;
    private final MeetingService meetingService;

    @GetMapping("/")
    public String home() {
        // Check if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "home";
    }

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Model model) {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            model.addAttribute("user", user);

            try {
                // Get user's workspaces
                List<Workspace> workspaces = workspaceService.getWorkspacesByUserId(user.getId());
                model.addAttribute("workspaces", workspaces);

                // Get user's upcoming meetings
                List<Meeting> userMeetings = meetingService.getMeetingsByParticipant(user.getId());
                // Filter for upcoming meetings
                LocalDateTime now = LocalDateTime.now();
                userMeetings.removeIf(meeting ->
                    meeting.getStatus() != Meeting.MeetingStatus.SCHEDULED ||
                    meeting.getStartTime().isBefore(now));
                model.addAttribute("upcomingMeetings", userMeetings);

                // Get ongoing meetings where user is a participant
                List<Meeting> ongoingMeetings = meetingService.getOngoingMeetings();
                ongoingMeetings.removeIf(meeting -> !meeting.getParticipants().contains(user));
                model.addAttribute("ongoingMeetings", ongoingMeetings);

                return "dashboard";
            } catch (Exception e) {
                // Log the exception
                System.err.println("Error loading dashboard: " + e.getMessage());
                // Return to home page with error
                model.addAttribute("errorMessage", "Error loading dashboard. Please try again.");
                return "home";
            }
        } else {
            return "redirect:/login";
        }
    }
}
