package entity;

/**
 * Created by Cabeza on 2016/4/26.
 */
public class UserRelation {
    private String userId;
    private String userName;
    private String followeeId;
    private String followeeName;

    public UserRelation() {
    }

    public UserRelation(String userId, String userName, String followeeId, String followeeName) {
        this.userId = userId;
        this.userName = userName;
        this.followeeId = followeeId;
        this.followeeName = followeeName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFolloweeId() {
        return followeeId;
    }

    public void setFolloweeId(String followeeId) {
        this.followeeId = followeeId;
    }

    public String getFolloweeName() {
        return followeeName;
    }

    public void setFolloweeName(String followeeName) {
        this.followeeName = followeeName;
    }

    @Override
    public String toString() {
        return "UserRelation{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", followeeId='" + followeeId + '\'' +
                ", followeeName='" + followeeName + '\'' +
                '}';
    }
}
