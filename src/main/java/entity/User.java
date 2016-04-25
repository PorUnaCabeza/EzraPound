package entity;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class User {
    //短链接
    private String url;
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

    public User() {
    }

    public User(String url, String name, String bio, String location, String business, String gender, String education, String description, String agree, String thanks, String asks, String answers, String posts, String collections, String logs, String following, String followers) {
        this.url = url;
        this.name = name;
        this.bio = bio;
        this.location = location;
        this.business = business;
        this.gender = gender;
        this.education = education;
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
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
}
