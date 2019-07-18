package X;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class User {

    @NotNull
    @NotBlank(message="Email cannot be blank!")
    private String username;
    @NotNull
    @NotBlank(message="Email cannot be blank!")
    private String email;
    @NotNull
    @NotBlank(message="Password cannot be blank!")
    private String password;
    private String registration;

    public User(){

    }
    public User(String username, String email, String password, String registration){
        this.username = username;
        this.email = email;
        this.password = password;
        this.registration = registration;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRegistration() {
        return registration;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }
}