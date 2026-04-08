package com.laiketui.domain.order;

import java.io.File;

/**
 * @description:
 * @author: wx
 * @date: Created in 2019/10/25 9:49
 * @version:
 * @modified By:
 */
public class t
{
    public static void main(String[] args)
    {
        String
                file = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3plugins/distribution/webapp/modules/distribution/templates";
        file = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3plugins/seconds/webapp/modules/seconds/templates";
        file = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3plugins/subtraction/webapp/modules/subtraction/templates";
        file = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3plugins/integral/webapp/modules/integral/templates";
        file = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3plugins/auction/webapp/modules/auction/templates";
        file = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3plugins/bargain/webapp/modules/bargain/templates";
        file = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3plugins/platform_activities/webapp/modules/platform_activities/templates";
        file = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3plugins/sign/webapp/modules/sign/templates";
        file = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3plugins/sign/webapp/modules/sign/templates";
        file = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3admin/webapp/modules/";
//        file ="/Users/wangxian/Documents/HBuilderProjects/svn/php/v3plugins";

//        File files  = new File(file);
//        File[] ret = files.listFiles();
//        int tplSize = 0;
//        for (File f:ret ) {
//            System.out.print(f.getName());
//            if(f.isDirectory()){
//                File[] tmpFiles = f.listFiles();
//                for(File f1:tmpFiles){
//                   if( f1.isDirectory()){
////                       System.out.println("\t"+f1.getName());
//                       if(f1.getName().equals("templates")){
////                           System.out.println(f1.getName());
//                           File[] files1 = f1.listFiles();
//                           int  i=0;
//                           for (File f11:files1){
//                               tplSize++;
//                               if(i==0){
//                                   System.out.println("\t"+f11.getName());
//                               }else{
//                                   System.out.println("\t"+f11.getName());
//                               }
//                               i++;
//
//                           }
//                       }
//                   }
//                }
//            }
//        }
//        System.out.println("总tpl："+tplSize);
//        for (File f:ret ) {
//            System.out.println(f.getName());
//            if(f.isDirectory() && !f.getName().equals(".svn")){
//                File[] tmpFiles = f.listFiles();
//                for(File f1:tmpFiles){
//                   if( f1.isDirectory()){
//                       System.out.println("\t"+f1.getName());
//                       File[] tmpFiles1 = f1.listFiles();
//                       for(File f12:tmpFiles1){
//                           System.out.println("\t\t"+f12.getName());
//                           File[] tmpFiles12 = f12.listFiles();
//                           for(File f122:tmpFiles12){
//                               System.out.println("\t\t\t"+f122.getName());
//                               File[] tmpFiles123 = f122.listFiles();
//                               if(tmpFiles123!=null){
//                                   for(File f121:tmpFiles123){
//                                       if(f121.getName().equals("templates")){
//                                           System.out.println("\t\t\t\t"+f121.getName());
//                                           File[] files12 = f121.listFiles();
//                                           for (File f112:files12){
//                                               tplSize++;
//                                               System.out.println("\t\t\t\t\t"+f112.getName());
//                                           }
//                                       }
//                                   }
//                               }
//                           }
//                       }
//                   }
//                }
//            }
//        }

        //
//        uniappPagesInfo();
        //pc商城页面
//        pcmallpagesINFO();
        //pc店铺页面
        pcmchpagesINFO();
    }

    //移动端页面
    public static void uniappPagesInfo()
    {
        String file  = "/Users/wangxian/Documents/HBuilderProjects/svn/php/v3Pages";
        File   files = new File(file);
        File[] ret   = files.listFiles();
        for (File file1 : ret)
        {
            String packgename = file1.getName();
            if (packgename.equals("pages") || packgename.equals("pagesA") || packgename.equals("pagesB") || packgename.equals("pagesC") || packgename.equals("components"))
            {
                File   files1 = new File(file + "/" + packgename);
                File[] ret1   = files1.listFiles();
                for (File file11 : ret1)
                {
                    String subpkg = file11.getName();
                    File   files2 = new File(file + "/" + packgename + "/" + subpkg);
                    if (files2.isDirectory())
                    {
                        File[] ret2 = files2.listFiles();
                        for (File file12 : ret2)
                        {
                            String pagefilename = file12.getName();
                            if ("components".equals(pagefilename) || "noDiyComponents".equals(pagefilename))
                            {
                                File   files3 = new File(file + "/" + packgename + "/" + subpkg + "/" + pagefilename);
                                File[] ret3   = files3.listFiles();
                                for (File file13 : ret3)
                                {
                                    System.out.println(packgename + "/" + subpkg + "/" + pagefilename + "/" + file13.getName());
                                }
                            }
                            else
                            {
                                System.out.println(packgename + "/" + subpkg + "/" + pagefilename);
                            }
                        }
                    }
                    else if (packgename.equals("components"))
                    {
                        System.out.println(packgename + "/" + subpkg);
                    }

                }
            }
        }
    }

    //商城页面
    public static void pcmallpagesINFO()
    {
        String file  = "/Users/wangxian/Documents/HBuilderProjects/svn/php/laikepc/common";
        File   files = new File(file);
        File[] ret   = files.listFiles();
        for (File file1 : ret)
        {
            String packgename = file1.getName();
            System.out.println("pages/" + packgename + "/" + packgename);
            if (".DS_Store".equals(packgename) || "静态页面目录".equals(packgename))
            {
                continue;
            }
            File   files1 = new File(file + "/" + packgename);
            File[] ret1   = files1.listFiles();
            for (File file11 : ret1)
            {
                String subpkg = file11.getName();
                System.out.println("pages/" + packgename + "/" + subpkg);
            }
        }
    }

    //pc店铺
    public static void pcmchpagesINFO()
    {
        String file  = "/Users/wangxian/Documents/HBuilderProjects/svn/php/laikemch/src/views";
        File   files = new File(file);
        File[] ret   = files.listFiles();
        for (File file1 : ret)
        {
            String packgename = file1.getName();
//            System.out.println("pages/"+packgename + "/" +packgename );
            if ("Home.vue".equals(packgename) || "router.vue".equals(packgename))
            {
                continue;
            }
            File   files1 = new File(file + "/" + packgename);
            File[] ret1   = files1.listFiles();
            for (File file11 : ret1)
            {
                String subpkg = file11.getName();
                System.out.println("pages/" + packgename + "/" + subpkg);
            }
        }
    }

}
