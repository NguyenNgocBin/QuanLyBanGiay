package models;

/**
 * Lớp mô hình (Model) đại diện cho một người dùng trong hệ thống.
 * Chứa các thông tin cơ bản: id, name, username, email và password.
 */
public class User {
    private int id;
    private String name;
    private String username;
    private String email;
    private String password;
    private String role;

    public User(int id, String name, String username, String email, String password) {
        this(id, name, username, email, password, "STAFF");
    }

    public User(int id, String name, String username, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
