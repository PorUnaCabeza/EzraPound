package entity;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class User {
    //短链接
    private String userId;
    private String name;
    //一句话介绍
    private String bio;
    //城市
    private String location;
    //行业
    private String business;
    //性别
    private String gender;
    //教育经历
    private String education;
    //学习方向
    private String educationExtra;
    //个人简介
    private String description;
    //赞同数
    private String agree;
    //感谢数
    private String thanks;
    //提问数
    private String asks;
    //回答数
    private String answers;
    //文章数
    private String posts;
    //收藏夹数
    private String collections;
    //公关编辑数
    private String logs;
    //关注数
    private String following;
    //粉丝数
    private String followers;

    private String hashId;

    public User() {
    }

    public User(String userId, String name, String bio, String location, String business, String gender, String education, String educationExtra, String description, String agree, String thanks, String asks, String answers, String posts, String collections, String logs, String following, String followers, String hashId) {
        this.userId = userId;
        this.name = name;
        this.bio = bio;
        this.location = location;
        this.business = business;
        this.gender = gender;
        this.education = education;
        this.educationExtra = educationExtra;
        this.description = description;
        this.agree = agree;
        this.thanks = thanks;
        this.asks = asks;
        this.answers = answers;
        this.posts = posts;
        this.collections = collections;
        this.logs = logs;
        this.following = following;
        this.followers = followers;
        this.hashId = hashId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getEducationExtra() {
        return educationExtra;
    }

    public void setEducationExtra(String educationExtra) {
        this.educationExtra = educationExtra;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAgree() {
        return agree;
    }

    public void setAgree(String agree) {
        this.agree = agree;
    }

    public String getThanks() {
        return thanks;
    }

    public void setThanks(String thanks) {
        this.thanks = thanks;
    }

    public String getAsks() {
        return asks;
    }

    public void setAsks(String asks) {
        this.asks = asks;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getPosts() {
        return posts;
    }

    public void setPosts(String posts) {
        this.posts = posts;
    }

    public String getCollections() {
        return collections;
    }

    public void setCollections(String collections) {
        this.collections = collections;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
    }

    public String getFollowers() {
        return followers;
    }

    public void setFollowers(String followers) {
        this.followers = followers;
    }

    public String getHashId() {
        return hashId;
    }

    public void setHashId(String hashId) {
        this.hashId = hashId;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", bio='" + bio + '\'' +
                ", location='" + location + '\'' +
                ", business='" + business + '\'' +
                ", gender='" + gender + '\'' +
                ", education='" + education + '\'' +
                ", educationExtra='" + educationExtra + '\'' +
                ", description='" + description + '\'' +
                ", agree='" + agree + '\'' +
                ", thanks='" + thanks + '\'' +
                ", asks='" + asks + '\'' +
                ", answers='" + answers + '\'' +
                ", posts='" + posts + '\'' +
                ", collections='" + collections + '\'' +
                ", logs='" + logs + '\'' +
                ", following='" + following + '\'' +
                ", followers='" + followers + '\'' +
                ", hashId='" + hashId + '\'' +
                '}';
    }
}
