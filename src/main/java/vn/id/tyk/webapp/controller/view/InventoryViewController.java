package vn.id.tyk.webapp.controller.view;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import vn.id.tyk.webapp.entity.User;
import vn.id.tyk.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;


@Controller
public class InventoryViewController {

    @Autowired
    private UserService userService;

    @GetMapping("/inventory")
    public String showInventoryPage(Model model, HttpSession session, Principal principal) {
        User currentUser = userService.getAuthenticatedUser(session, principal);

        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);

        return "inventory"; 
    }
}