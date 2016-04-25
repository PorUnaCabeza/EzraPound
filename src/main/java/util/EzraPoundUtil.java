package util;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class EzraPoundUtil {
    public static final String CAPTCHA_DIR="cabeza.gif";
    public static final String LOGIN_COOKIES_DIR="zhihu_cookies.txt";

    public static String unicode2Character(String str)
    {
        str = (str == null ? "" : str);
        if (str.indexOf("\\u") == -1)//如果不是unicode码则原样返回
            return str;

        StringBuffer sb = new StringBuffer(1000);

        for (int i = 0; i < str.length() - 6;)
        {
            String strTemp = str.substring(i, i + 6);
            String value = strTemp.substring(2);
            int c = 0;
            for (int j = 0; j < value.length(); j++)
            {
                char tempChar = value.charAt(j);
                int t = 0;
                switch (tempChar)
                {
                    case 'a':
                        t = 10;
                        break;
                    case 'b':
                        t = 11;
                        break;
                    case 'c':
                        t = 12;
                        break;
                    case 'd':
                        t = 13;
                        break;
                    case 'e':
                        t = 14;
                        break;
                    case 'f':
                        t = 15;
                        break;
                    default:
                        t = tempChar - 48;
                        break;
                }

                c += t * ((int) Math.pow(16, (value.length() - j - 1)));
            }
            sb.append((char) c);
            i = i + 6;
        }
        return sb.toString();
    }
}
