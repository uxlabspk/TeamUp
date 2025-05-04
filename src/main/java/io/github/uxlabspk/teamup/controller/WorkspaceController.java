package io.github.uxlabspk.teamup.controller;

import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;
import io.github.uxlabspk.teamup.service.UserService;
import io.github.uxlabspk.teamup.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final UserService userService;

    @GetMapping
    @Transactional(readOnly = true)
    public String listWorkspaces(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();

            List<Workspace> workspaces = workspaceService.getWorkspacesByUserId(user.getId());
            model.addAttribute("workspaces", workspaces);

            return "workspace/list";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/create")
    public String createWorkspaceForm(Model model) {
        model.addAttribute("workspace", new Workspace());
        return "workspace/create";
    }

    @PostMapping("/create")
    public String createWorkspace(@Valid @ModelAttribute("workspace") Workspace workspace,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "workspace/create";
        }

        // Check if workspace name already exists
        if (workspaceService.existsByName(workspace.getName())) {
            result.rejectValue("name", "error.workspace", "Workspace name already exists");
            return "workspace/create";
        }

        try {
            // Get the current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                User user = (User) auth.getPrincipal();

                // Create the workspace
                workspaceService.createWorkspace(workspace, user);

                redirectAttributes.addFlashAttribute("successMessage", "Workspace created successfully");
                return "redirect:/workspaces";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Authentication error");
                return "redirect:/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Workspace creation failed: " + e.getMessage());
            return "redirect:/workspaces/create";
        }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String viewWorkspace(@PathVariable("id") Long id, Model model) {
        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(id);

        if (workspaceOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();
            model.addAttribute("workspace", workspace);

            // Get workspace members
            List<User> members = workspaceService.getWorkspaceMembers(id);
            model.addAttribute("members", members);

            return "workspace/view";
        } else {
            return "redirect:/workspaces";
        }
    }

    @GetMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String editWorkspaceForm(@PathVariable("id") Long id, Model model) {
        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(id);

        if (workspaceOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();

            // Check if current user is the owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!workspace.getOwner().getId().equals(user.getId())) {
                return "redirect:/workspaces/" + id;
            }

            model.addAttribute("workspace", workspace);
            return "workspace/edit";
        } else {
            return "redirect:/workspaces";
        }
    }

    @PostMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String updateWorkspace(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("workspace") Workspace workspace,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "workspace/edit";
        }

        Optional<Workspace> existingWorkspaceOpt = workspaceService.getWorkspaceById(id);

        if (existingWorkspaceOpt.isPresent()) {
            Workspace existingWorkspace = existingWorkspaceOpt.get();

            // Check if current user is the owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!existingWorkspace.getOwner().getId().equals(user.getId())) {
                return "redirect:/workspaces/" + id;
            }

            // Check if name is changed and already exists
            if (!existingWorkspace.getName().equals(workspace.getName()) && 
                workspaceService.existsByName(workspace.getName())) {
                result.rejectValue("name", "error.workspace", "Workspace name already exists");
                return "workspace/edit";
            }

            try {
                // Update only allowed fields
                existingWorkspace.setName(workspace.getName());
                existingWorkspace.setDescription(workspace.getDescription());
                existingWorkspace.setIcon(workspace.getIcon());

                workspaceService.updateWorkspace(existingWorkspace);

                redirectAttributes.addFlashAttribute("successMessage", "Workspace updated successfully");
                return "redirect:/workspaces/" + id;
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Workspace update failed: " + e.getMessage());
                return "redirect:/workspaces/" + id + "/edit";
            }
        } else {
            return "redirect:/workspaces";
        }
    }

    @PostMapping("/{id}/delete")
    @Transactional(readOnly = true)
    public String deleteWorkspace(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(id);

        if (workspaceOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();

            // Check if current user is the owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!workspace.getOwner().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to delete this workspace");
                return "redirect:/workspaces/" + id;
            }

            try {
                workspaceService.deleteWorkspace(id);
                redirectAttributes.addFlashAttribute("successMessage", "Workspace deleted successfully");
                return "redirect:/workspaces";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Workspace deletion failed: " + e.getMessage());
                return "redirect:/workspaces/" + id;
            }
        } else {
            return "redirect:/workspaces";
        }
    }

    @GetMapping("/{id}/members")
    @Transactional(readOnly = true)
    public String listMembers(@PathVariable("id") Long id, Model model) {
        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(id);

        if (workspaceOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();
            model.addAttribute("workspace", workspace);

            List<User> members = workspaceService.getWorkspaceMembers(id);
            model.addAttribute("members", members);

            // Get all users for adding new members
            List<User> allUsers = userService.getAllUsers();
            model.addAttribute("allUsers", allUsers);

            return "workspace/members";
        } else {
            return "redirect:/workspaces";
        }
    }

    @PostMapping("/{id}/members/add")
    @Transactional(readOnly = true)
    public String addMember(@PathVariable("id") Long id,
                           @RequestParam("userId") Long userId,
                           RedirectAttributes redirectAttributes) {

        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(id);

        if (workspaceOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();

            // Check if current user is the owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!workspace.getOwner().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to add members");
                return "redirect:/workspaces/" + id + "/members";
            }

            try {
                workspaceService.addUserToWorkspace(id, userId);
                redirectAttributes.addFlashAttribute("successMessage", "Member added successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to add member: " + e.getMessage());
            }

            return "redirect:/workspaces/" + id + "/members";
        } else {
            return "redirect:/workspaces";
        }
    }

    @PostMapping("/{id}/members/remove")
    @Transactional(readOnly = true)
    public String removeMember(@PathVariable("id") Long id,
                              @RequestParam("userId") Long userId,
                              RedirectAttributes redirectAttributes) {

        Optional<Workspace> workspaceOpt = workspaceService.getWorkspaceById(id);

        if (workspaceOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();

            // Check if current user is the owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (!workspace.getOwner().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to remove members");
                return "redirect:/workspaces/" + id + "/members";
            }

            try {
                workspaceService.removeUserFromWorkspace(id, userId);
                redirectAttributes.addFlashAttribute("successMessage", "Member removed successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove member: " + e.getMessage());
            }

            return "redirect:/workspaces/" + id + "/members";
        } else {
            return "redirect:/workspaces";
        }
    }
}
