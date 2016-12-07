package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Min;

/**
 * Created by Siva Sudarsi on 1/12/16.
 */
@Entity
public class UserLogin extends Model {

    @Id
    @Min(10)
    public Long id;

    private String username;
    private String password;
    private String role;

    public static Finder<Long, User> find = new Finder<Long, User>(User.class);

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
