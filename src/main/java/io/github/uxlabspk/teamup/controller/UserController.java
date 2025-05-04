package io.github.uxlabspk.teamup.controller;

import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        model.addAttribute("user", user);
        return "user/profile";
    }

    @GetMapping("/edit-profile")
    public String editProfileForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        model.addAttribute("user", user);
        return "user/edit-profile";
    }

    @PostMapping("/edit-profile")
    public String updateProfile(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "user/edit-profile";
        }
        
        try {
            // Get the current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();
            
            // Update only allowed fields
            currentUser.setFullName(user.getFullName());
            currentUser.setStatus(user.getStatus());
            
            // If email is changed, check if it's already in use
            if (!currentUser.getEmail().equals(user.getEmail())) {
                if (userService.existsByEmail(user.getEmail())) {
                    result.rejectValue("email", "error.user", "Email is already in use");
                    return "user/edit-profile";
                }
                currentUser.setEmail(user.getEmail());
            }
            
            userService.updateUser(currentUser);
            
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
            return "redirect:/user/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Profile update failed: " + e.getMessage());
            return "redirect:/user/edit-profile";
        }
    }

    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        
        // Check if new password and confirm password match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password and confirm password do not match");
            return "redirect:/user/change-password";
        }
        
        try {
            // Get the current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();
            
            // Change password
            userService.changePassword(currentUser.getId(), newPassword);
            
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
            return "redirect:/user/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password change failed: " + e.getMessage());
            return "redirect:/user/change-password";
        }
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("status") String status,
                              RedirectAttributes redirectAttributes) {
        
        try {
            // Get the current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();
            
            // Update status
            userService.updateStatus(currentUser.getId(), status);
            
            redirectAttributes.addFlashAttribute("successMessage", "Status updated successfully");
            return "redirect:/user/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Status update failed: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }
}