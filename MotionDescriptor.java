import java.util.ArrayList;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


public class MotionDescriptor{

    String[] videos;
    int height, width,numberFrames;
    int[][] img1;
	int[][] img2;

    static String stripExtension (String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;
        return str.substring(0, pos);
    }

    public MotionDescriptor(){
        try{
            this.width = 640;
            this.height = 360;
            this.numberFrames = 480;
            this.img1 = new int[width][height];
            this.img2 = new int[width][height];
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private long calculateMAD(int[][] previousImage, int[][] img,int blockStartY, int blockStartX, int i, int j){
		long sum = 0;
		for(int a=0; a < 16; ++a){
			for(int b=0; b < 16; ++b){
				sum +=  Math.abs((img[a + blockStartY][b + blockStartX]&0xff) - (previousImage[a + blockStartY + i][b + blockStartX + j]&0xff));
			}
		}
        
		return sum;
	}

    private boolean isCorrectIndex(int x, int y) {
		if(x < 0 || y < 0){
			return false;
		}
		if((x + 16 > this.width)|| (y + 16 > this.height)){
			return false;
		}
		return true;
    }

    //calculate motion vector for a block using brute force
	private int[] bruteForce(int[][] previousImage, int[][] img, int blockStartY, int blockStartX) {
		long minMAD = Long.MAX_VALUE;
		int[] mv =new int[2];
		mv[0]=0;
		mv[1]=0;

        //calculate motion vector
		for(int i=(-16); i <= 16; ++i){
			for(int j = (int)(-16); j <= 16; ++j){
				if(!isCorrectIndex(i + blockStartY, j + blockStartX)){
					continue;
				}
				long sum = calculateMAD(previousImage, img, blockStartY, blockStartX,i , j);
				if(minMAD > sum){
					minMAD = sum;
					mv[0] = i;
					mv[1] = j;
				}
			}
        }
        return mv;   
    }

    public String calculateMeanMotionVector(int[][] previousImage, int[][] img){
        try{
            int numberXBlocks= width/16;
            int numberYBlocks = height/16;
            int numBlocks = 0;
            int mvXSum=0;
            int mvYSum = 0;

            //divide into blocks and perform brute force
            for(int x = 0; x < numberYBlocks; ++x){
                for(int y = 0; y < numberXBlocks; ++y){
                    int[] mv = new int[2];
                    mv = bruteForce(previousImage, img, y*16, x*16);
                    mvXSum+=Math.abs(mv[0]);
                    mvYSum+=Math.abs(mv[1]);
                    numBlocks+=1;
                }
            }
            return (""+(mvXSum/numBlocks)+" "+(mvYSum/numBlocks)+" ");
        }
        catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public String processVideo(String video){
        try{
            String motionDescriptor="";
            File folder = new File(video);
            File[] listOfFiles = folder.listFiles((dir, name) -> !name.equals(".DS_Store"));
		    Arrays.sort(listOfFiles, new Comparator<File>() {
    public int compare(File f1, File f2) {
        try {
            int i1 = Integer.parseInt((stripExtension(f1.getName())).replaceAll("[^\\d.]", ""));
            int i2 = Integer.parseInt((stripExtension(f2.getName())).replaceAll("[^\\d.]", ""));
            return i1 - i2;
        } catch(NumberFormatException e) {
            throw new AssertionError(e);
        }
    }
});

            for(int i=1;i<this.numberFrames;i++){

                //read for previous file
                int frameLength = width*height*3;
                File file = listOfFiles[i-1];
                RandomAccessFile raf = new RandomAccessFile(file,"r");
                raf.seek(0);

                long len=frameLength;
                byte[] bytes = new byte[(int)len];

                raf.read(bytes);

                int ind =0;
                for(int y=0;y<this.height;y++){
                    for(int x =0; x<this.width;x++){
                        byte r = bytes[ind];
                        byte g = bytes[ind+height*width];
                        byte b = bytes[ind+height*width*2];
                        byte Y = (byte)(0.299 * Byte.toUnsignedInt(r) + 0.587 * Byte.toUnsignedInt(g) + 0.114 * Byte.toUnsignedInt(b));
                        img1[x][y]=Byte.toUnsignedInt(Y);
                        ind++;
                    }
                }

                //read next img

                File file1 = listOfFiles[i];
                RandomAccessFile raf1 = new RandomAccessFile(file1,"r");
                raf1.seek(0);

                byte[] bytes1 = new byte[(int)len];

                raf1.read(bytes1);

                ind = 0;
                for(int y = 0; y < this.height; y++)
                {
                    for(int x = 0; x < this.width; x++)
                    {
                        byte r = bytes1[ind];
                        byte g = bytes1[ind+height*width];
                        byte b = bytes1[ind+height*width*2];
                        byte Y = (byte)(0.299 * Byte.toUnsignedInt(r) + 0.587 * Byte.toUnsignedInt(g) + 0.114 * Byte.toUnsignedInt(b));
                        img2[x][y]=Byte.toUnsignedInt(Y);
                        ind++;
                    }
                }

                motionDescriptor+=calculateMeanMotionVector(img1, img2);
            }
            return motionDescriptor;
        }
        catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }

         
}