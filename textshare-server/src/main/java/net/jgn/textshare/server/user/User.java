package net.jgn.textshare.server.user;

/**
 * @author jose
 */
public class User {
    private int id;
    private String userName;
    private String hashPassword;

    public User() {
    }

    public User(String userName, String hashPassword) {
        this.userName = userName;
        this.hashPassword = hashPassword;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", hashPassword='" + hashPassword + '\'' +
                '}';
    }
}
