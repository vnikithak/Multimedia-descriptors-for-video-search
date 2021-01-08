import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.lang.Math;
import javax.swing.*;
import java.util.HashMap;

public class ColorDescriptor{

    int height, width,numberFrames;
    // FileWriter fw;
    // BufferedWriter bw;

    public ColorDescriptor(){
        try{
            this.width = 640;
            this.height = 360;
            this.numberFrames = 480;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    static String stripExtension (String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;
        return str.substring(0, pos);
    }

    public String getHSV(int r, int g, int b){
        float[] hsv = new float[3];
		Color.RGBtoHSB(r,g,b,hsv);
        int h,s,v;
        h = (int)(Math.abs(hsv[0]*15));
        s = (int)(Math.abs(hsv[1]*3));
        v = (int)(Math.abs(hsv[2]*3));
        if (h < 10)
			return "0" + h + "" + s + "" + v;
		return "" + h + "" + s + "" + v;
    }

    public String processVideo(String video){

        try{
            String colorDescriptor="";
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

            for(int i=0;i<this.numberFrames;i++){
                HashMap<String, Integer> histogram = new HashMap<String, Integer>();
                BufferedImage img = new BufferedImage(this.width,this.height, BufferedImage.TYPE_INT_RGB);
                int frameLength = width*height*3;

                File file = listOfFiles[i];
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(0);

                long len = frameLength;
                byte[] bytes = new byte[(int) len];

                raf.read(bytes);

                int ind = 0;
                for(int y = 0; y < this.height; y++)
                {
                    for(int x = 0; x < this.width; x++)
                    {
                        int r = (int)(bytes[ind]);
                        int g = (int)(bytes[ind+this.height*this.width]);
                        int b = (int)(bytes[ind+this.height*this.width*2]);
                        //get hsv
                        String hsvString = getHSV(r,g,b);

                        //place in histogram
                        if (histogram.get(hsvString) == null)
                            histogram.put(hsvString, 1);
                        else
                            histogram.put(hsvString, histogram.get(hsvString) + 1);
                        ind++;
                    }
                }
                String maxHSVValue = "";
                for (String s : histogram.keySet())
                {
                    if (maxHSVValue.equals(""))
                            maxHSVValue = s;
                    else
                    {
                        if (histogram.get(s) > histogram.get(maxHSVValue))
                            maxHSVValue = s;
                    }
                }
                colorDescriptor+=("" + maxHSVValue + " ");
                raf.close();
            }
            return colorDescriptor;
        }
        catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }

	
}