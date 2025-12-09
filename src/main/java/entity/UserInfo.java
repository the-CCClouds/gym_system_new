package entity;

import java.sql.Timestamp;

public class UserInfo {
    private int userId;
    private String username;
    private String userType;
    private int referenceId;
    private String status;
    private Timestamp lastLogin;

    public UserInfo(int userId, String username, String userType,
                    int referenceId, String status, Timestamp lastLogin) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
        this.referenceId = referenceId;
        this.status = status;
        this.lastLogin = lastLogin;
    }



    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserType() {
        return userType;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }
}
