package X;


import X.database.UserDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Controller
public class HomeController {

    @Autowired
    HttpSession thisSession;

    @Autowired
    UserDatabaseService userDatabaseService = new UserDatabaseService();

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("user", sessionUser());
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model){
        model.addAttribute("user", sessionUser());
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model){
        model.addAttribute("user", sessionUser());
        return "contact";
    }


    @GetMapping("/user/{username}")
    public String userDetails(@PathVariable("username") String username, Model model, HttpSession session){
        try {
            User user = userDatabaseService.findUserByUsername(username);
            Integer id = (Integer) session.getAttribute("user");
            if (id != null) {
                User thisUser = (User) userDatabaseService.findUserByID(id);
                System.out.println(PermissionManager.hasChangeRoleManagementPermission(thisUser));
                boolean canChangeUsername = (PermissionManager.hasChangeUsernamePermission(thisUser) && (user.getRole().getPriority() >= thisUser.getRoles().get(0).getPriority()));
                model.addAttribute("changeUsername",canChangeUsername);
                model.addAttribute("roleManagement",PermissionManager.hasChangeRoleManagementPermission(thisUser));
                model.addAttribute("signedUser", thisUser);
                ArrayList<String> roles = userDatabaseService.getAllRoles();
                ArrayList<String> userRoles = userDatabaseService.getUserRoles(user.getID());
                model.addAttribute("roles", roles);
                model.addAttribute("userRoles", userRoles);
            }
           else {
                model.addAttribute("changeUsername",false);
            }
            model.addAttribute("user", user);
            return "user";
        }
        catch (NoSuchUserException ex){
            System.out.println("This user does not exist!");
        }

        return "noUser";
    }

    @GetMapping("/new-project")
    public String newProject(Model model, HttpSession session){
        if(session.getAttribute("user") == null)
        {
            model.addAttribute("user", null);
            return "redirect:/explore";
        }
        int id = (int) session.getAttribute("user");
        User user = userDatabaseService.findUserByID(id);
        model.addAttribute("upload", new Upload());
        model.addAttribute("user",user);
        return "upload";
    }

    private User sessionUser(){
        if(thisSession.getAttribute("user")== null){
            return null;
        }
        int id = (int) thisSession.getAttribute("user");
        User user = (User) userDatabaseService.findUserByID(id);
        return user;
    }
}
