package com.laiketui.core.utils.tool;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import org.iherus.codegen.Codectx;
import org.iherus.codegen.qrcode.QrcodeConfig;
import org.iherus.codegen.qrcode.QreyesFormat;
import org.iherus.codegen.qrcode.SimpleQrcodeGenerator;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片处理工具
 *
 * @author Trick
 * @date 2020/12/24 15:22
 */
public class ImageTool
{

    /**
     * 生成一张底图
     *
     * @param imgBackGroundX - 宽度
     * @param imgBackGroundY - 长度
     * @param color          - 颜色
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/18 13:55
     */
    public static BufferedImage builderBaseMap(int imgBackGroundX, int imgBackGroundY, Color color) throws LaiKeAPIException
    {
        try
        {
            //生成底图
            BufferedImage mainImage  = new BufferedImage(imgBackGroundX, imgBackGroundY, BufferedImage.TYPE_INT_RGB);
            Graphics2D    graphics2D = mainImage.createGraphics();
            graphics2D.setBackground(color);
            graphics2D.fillRect(0, 0, imgBackGroundX, imgBackGroundY);
            graphics2D.dispose();
            return mainImage;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "图片加水印失败", "imageWatermark");
        }
    }

    /**
     * 图片加水印
     *
     * @param image - 图片流
     * @param text  - 文字
     * @param x     - x坐标
     * @param y     - y坐标
     * @param color - 颜色
     * @param font  - 自提
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/18 13:55
     */
    public static void imageWatermark(BufferedImage image, String text, int x, int y, Color color, Font font) throws LaiKeAPIException
    {
        try
        {
            //从原图中找出一片区域来显示水印文本
            Graphics2D g = image.createGraphics();
            //字体、字体大小，透明度，旋转角度
            g.setFont(font);
            char[] data = text.toCharArray();
            g.rotate(0);
            g.setColor(color);
            //设置文本显示坐标
            g.drawChars(data, 0, data.length, x, y);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "图片加水印失败", "imageWatermark");
        }
    }

    /**
     * 计算字体间距
     *
     * @return int -
     * @author Trick
     * @date 2021/3/23 18:39
     */
    public static int getWordWidth(Font font, String content)
    {
        FontMetrics       fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
        FontRenderContext frc         = new FontRenderContext(new AffineTransform(), true, true);
        return (int) Math.ceil(font.getStringBounds(content, frc).getWidth());
    }

//    public static void main(String[] args) {
//        Font font1 = new Font("微软雅黑", Font.PLAIN, 26);
//        System.out.println(getWordWidth(font1, "99999999") + "99999999".length() * 11);
//        System.out.println(getWordWidth(font1, "字字字字字字字字"));
//    }

    /**
     * 计算字体高度
     *
     * @return int -
     * @author Trick
     * @date 2021/3/23 18:39
     */
    public static int getWordHeight(Font font)
    {
        FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
        return fontMetrics.getHeight();
    }

    /**
     * 获取文字集合,每个下标都代表一行
     *
     * @param font       -
     * @param content    -
     * @param maxLineNum -当前行支持多少个像素
     * @return int -
     * @author Trick
     * @date 2021/3/23 18:39
     */
    public static List<String> getWord(Font font, String content, int maxLineNum)
    {
        List<String> list = new ArrayList<>();
        //当前字符串所占像素长度
        int titleFontX = ImageTool.getWordWidth(font, content);
        //获取一个字像素多少
        int pixelNum = ImageTool.getWordWidth(font, "字");
        //超出了多少像素
        int outNum = titleFontX - maxLineNum;
        if (outNum > 0)
        {
            //一行多少个字
            int lineNum = maxLineNum / pixelNum;
            //需要几行
            int line = content.length() / lineNum + 1;
            for (int i = 0; i < line; i++)
            {
                if (content.length() >= lineNum)
                {
                    list.add(content.substring(0, lineNum));
                    //更新字符串
                    content = content.substring(lineNum);
                }
                else
                {
                    list.add(content);
                }
            }
        }
        else
        {
            list.add(content);
        }
        return list;
    }

    /**
     * 生成二维码
     *
     * @param logo - 码眼 可选
     * @param data - 二维码内容
     * @param path - 保存路径
     * @return File -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/24 16:34
     */
    public static File builderQrcode(String logo, String data, String path) throws LaiKeAPIException
    {
        try
        {
            if (new SimpleQrcodeGenerator().setRemoteLogo(logo).generate(data).toFile(path))
            {
                return new File(path);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "生成二维码失败", "builderQrcode");
        }
        return null;
    }

    /**
     * 生成二维码
     *
     * @param logo  - 码眼 可选
     * @param style - 二维码样式
     * @param data  - 二维码内容
     * @param path  - 保存路径
     * @return File -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/24 16:34
     */
    public static File builderQrcode(String logo, QrcodeConfig style, String data, String path) throws LaiKeAPIException
    {
        try
        {
            if (style == null)
            {
                style = new QrcodeConfig()
                        .setBorderSize(1)
                        .setPadding(10)
                        .setLogoBorderColor("#FFA07A")
                        .setLogoShape(Codectx.LogoShape.CIRCLE)
                        .setCodeEyesFormat(QreyesFormat.DR2_BORDER_C_POINT);
            }
            if (new SimpleQrcodeGenerator(style).setRemoteLogo(logo).generate(data).toFile(path))
            {
                return new File(path);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "生成二维码失败", "builderQrcode");
        }
        return null;
    }

    /**
     * hex转rgv
     *
     * @param hex - #FFFFFF
     * @return Color
     * @author Trick
     * @date 2021/9/24 20:00
     */
    public static Color hexToRgb(String hex)
    {
        return new Color(
                Integer.valueOf(hex.substring(1, 3), 16),
                Integer.valueOf(hex.substring(3, 5), 16),
                Integer.valueOf(hex.substring(5, 7), 16)
        );
    }


    public static void main(String[] args) throws IOException
    {
        String content = "https://www.baidu.com/";
        String logoUrl = "https://laikeds.oss-cn-shenzhen.aliyuncs.com/1/1/20220316/1503914902267080704.jpeg";

        QrcodeConfig config = new QrcodeConfig()
                // 外层虚线Border
                .setBorderSize(1)
                // 二维码离虚线的填充空白
                .setPadding(10)
                // 二维码颜色
                .setMasterColor("#FF0000")
                // LOGO边框颜色
                .setLogoBorderColor("#FFA07A")
                // 圆形logo
                .setLogoShape(Codectx.LogoShape.RECTANGLE);

//        new SimpleQrcodeGenerator(config).setRemoteLogo(logoUrl).generate(content).toFile("123.jpg");
        System.out.println("success");

        Font font1 = new Font("微软雅黑", Font.PLAIN, 26);
        System.out.println(getWordWidth(font1, "99999999"));
        System.out.println(getWordWidth(font1, "字字字字字字"));
        System.out.println(getWordHeight(font1));
    }


    /**
     * java-RGB转Color
     *
     * @param r -
     * @param g -
     * @param b -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/10/19 20:11
     */
    public static Color rgbToColor(int r, int g, int b) throws LaiKeAPIException
    {
        try
        {
            String bufferedImage = rgb2Hex(r, g, b);
            String substring     = String.valueOf(bufferedImage).substring(3);
            int    color         = Integer.parseInt(substring, 16);
            return new Color(color);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "java-RGB转Color", "rgbToColor");
        }
    }

    /**
     * java-RGB转换成十六进制
     *
     * @param r -
     * @param g -
     * @param b -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/10/19 20:11
     */
    public static String rgb2Hex(int r, int g, int b) throws LaiKeAPIException
    {
        try
        {
            return String.format("0xFF%02X%02X%02X", r, g, b);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "java-RGB转换成十六进制", "rgb2Hex");
        }
    }

    /**
     * 16进制颜色字符串转换成rgb
     *
     * @param r -
     * @param g -
     * @param b -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/10/19 20:11
     */
    public static int[] hex2RGB(String hexStr) throws LaiKeAPIException
    {
        try
        {
            if (hexStr != null && !"".equals(hexStr) && hexStr.length() == 7)
            {
                int[] rgb = new int[3];
                rgb[0] = Integer.valueOf(hexStr.substring(1, 3), 16);
                rgb[1] = Integer.valueOf(hexStr.substring(3, 5), 16);
                rgb[2] = Integer.valueOf(hexStr.substring(5, 7), 16);
                return rgb;
            }
            return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "16进制颜色字符串转换成rgb", "hex2RGB");
        }
    }

}
