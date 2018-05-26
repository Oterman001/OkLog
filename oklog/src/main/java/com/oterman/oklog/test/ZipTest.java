package com.oterman.oklog.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Author：Oterman on 2017/10/13 0013 08:34
 * Email：oterman@126.com
 */
public class ZipTest {

        public static void main ( String [ ] args ) throws IOException
        {
            String dirStr="E:/log/";
            final String dateStr="20171010";

            String zipFileName=dirStr+""+dateStr+"_BHLog.zip";

            File dir=new File(dirStr);

            File[] srcFiles = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    String fileName = file.getName();
                    fileName = fileName.replace("_", "");
                    return fileName.endsWith("txt") && fileName.contains(dateStr);
                }
            });

            ZipOutputStream zos = new ZipOutputStream (new FileOutputStream(zipFileName)) ;
            BufferedOutputStream bos = new BufferedOutputStream ( zos ) ;

            for (int i=0;i<srcFiles.length;i++){
                System.out.println("开始处理："+srcFiles[i].getName());
                FileInputStream fis = new FileInputStream (srcFiles[i]);

                BufferedInputStream bis = new BufferedInputStream (fis) ;

                //设置压缩文件里的文件名字
                zos.putNextEntry (new ZipEntry(srcFiles[i].getName())) ;

                //设置压缩级别
                zos.setLevel(9);

                byte [ ] b = new byte [1024] ;
                while ( true )
                {
                    int len = bis.read ( b ) ;
                    if ( len == - 1 )
                        break ;
                    bos.write ( b , 0 , len ) ;
                }
                fis.close ( ) ;
                bos.flush();
            }

            bos.close ( ) ;
        }
}
