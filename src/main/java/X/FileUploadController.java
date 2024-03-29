package X;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import X.database.TagDatabaseService;
import X.database.UploadDatabaseService;
import X.database.UserDatabaseService;
import X.storage.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import X.storage.StorageFileNotFoundException;
import X.storage.StorageService;

import javax.servlet.http.HttpSession;

@Controller
public class FileUploadController {

    private final StorageService storageService;
    @Autowired
    private UploadDatabaseService uploadDatabaseService = new UploadDatabaseService();
    @Autowired
    private UserDatabaseService userDatabaseService = new UserDatabaseService();
    @Autowired
    private TagDatabaseService tagDatabaseService = new TagDatabaseService();


    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/new-project")
    public String handleFileUpload(Upload upload, @RequestParam("file") MultipartFile file, @RequestParam("tags") String tags,
                                   RedirectAttributes redirectAttributes, HttpSession session) {
        int id = (int) session.getAttribute("user");
        User user = userDatabaseService.findUserByID(id);
        upload.setUploaderID(user.getID());
        if(!PermissionManager.hasCreateProjectPermission(user)){
            redirectAttributes.addFlashAttribute("message", "You are not allowed to create a project!");
            return "redirect:/explore";
        }
        try {
            storageService.store(file, upload.getProjectName()+"-"+user.getID());
            upload.setFileName(upload.getProjectName()+"-"+user.getID()+"-"+file.getOriginalFilename());
            String cleanTags = tags.trim().replaceAll("( +)", " ");
            String[] tagList = cleanTags.split(" ");
            for (int i = 0; i<tagList.length; i++) {
                if (!tagDatabaseService.isTag(tagList[i])) {
                    return "upload";
                }
                Tag tag = tagDatabaseService.get(tagList[i]);
                upload.addTag(tag);
                tag.setUsage(tag.getUsage()+1);
                tagDatabaseService.increaseUsage(tag);
            }
            uploadDatabaseService.insert(upload);
            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded " + upload.getFileName() + "!");
            return "redirect:/explore";
        }
        catch (StorageException ex){
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            return "redirect:/explore";
        }

    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

   @PostMapping(path = "/user/{username}", params = "changeFile")
    public String usedUpdate(@RequestParam("file") MultipartFile file, HttpSession session){
        int id = (int) session.getAttribute("user");
        User user = userDatabaseService.findUserByID(id);
        String filename = user.getUsername()+"-"+user.getID();
        storageService.store(file, user.getUsername()+"-"+user.getID());
        filename= filename+"-"+file.getOriginalFilename();
        filename = filename.replaceAll("\\s+","-");
        userDatabaseService.updateProfile(filename, user.getID());
        return "redirect:/";
    }

    @PostMapping(path = "/user/{username}", params = "changeUsername")
    public String changeUsername(@RequestParam("newUsername") String newUsername, @PathVariable("username") String username){
        try {
            userDatabaseService.changeUsername(newUsername, username);
        }
        catch (Exception ex){
            return "redirect:/user/"+username+"";
        }
        return "redirect:/user/"+newUsername+"";
    }

    @PostMapping(path = "/user/{username}", params = "manageUser")
    public String manage(@RequestParam("role") String role, @PathVariable("username") String username){
        userDatabaseService.addRoleToUser(username, role);
        return "redirect:/user/"+username+"";
    }

    @PostMapping(path = "/user/{username}", params = "removeRole")
    public String removeRole(@RequestParam("role") String role, @PathVariable("username") String username){
        userDatabaseService.removeRoleFromUser(username, role);
        return "redirect:/user/"+username+"";
    }



}

