import entity.LoginInfo;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class LoginInfoTest {
    Logger log = LoggerFactory.getLogger(LoginInfoTest.class);
    LoginInfo loginInfo = new LoginInfo();

    @Test
    public void getXsrfTest() {
        loginInfo.getXsrf(0);
    }

    @Test
    public void getCaptchaTest() {
        loginInfo.getCaptchaImgAndCookies(0);
    }


    public static void main(String[] args) {
        new LoginInfo().login();
    }
}

