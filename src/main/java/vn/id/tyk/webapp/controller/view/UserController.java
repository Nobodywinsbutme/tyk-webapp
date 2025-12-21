package vn.id.tyk.webapp.controller.view;

import vn.id.tyk.webapp.entity.User;      
import vn.id.tyk.webapp.service.UserService; 
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;      
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/my-designs")
    public String myDesigns(Model model, HttpSession session, Principal principal) {
        User currentUser = userService.getAuthenticatedUser(session, principal);

        if (currentUser == null) {
            if (session != null) {
                session.invalidate();
            }
            return "redirect:/?message=expired";
        }
        
        model.addAttribute("user", currentUser);
        
        return "my-designs"; 
    }

    
    @GetMapping("/settings")
    public String showSettingsPage(Model model, HttpSession session, Principal principal) {
        User currentUser = userService.getAuthenticatedUser(session, principal);

        if (currentUser == null) {
            if (session != null) {
                session.invalidate();
            }
            return "redirect:/?message=expired";
        }

        model.addAttribute("user", currentUser);

        return "settings"; 
    }

}