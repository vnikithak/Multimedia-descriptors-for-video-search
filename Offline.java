import java.awt.*;
import java.io.*;
import java.util.*;

public class Offline {

    static String stripExtension (String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;
        return str.substring(0, pos);
    }
    
    public static void main(String args[]) {
        try{

            Map<String, Integer> no_files = new HashMap<String, Integer>();
            FileWriter fwColor, fwMotion, fwAudio;
            BufferedWriter bwColor, bwMotion, bwAudio;
            int totalFiles=0;
            String dirName;
            int c=0;
             Map<String, Integer> wav_files = new HashMap<String, Integer>();

            //give your jpg folder to get number of frames per video
            File videoFolder = new File("/Users/nikithavemulapalli/Desktop/Courses/CSCI 576-Multimedia/Assignments/Project/Data_rgb");
            File audioFolder = new File("/Users/nikithavemulapalli/Desktop/Courses/CSCI 576-Multimedia/Assignments/Project/Data_wav");
            File[] listOfFiles = videoFolder.listFiles((dir, name) -> !name.equals(".DS_Store"));
		    Arrays.sort(listOfFiles);

            //listoffiles has ads,sports etc
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isDirectory()) {
                    File[] listOfsub = listOfFiles[i].listFiles((dir, name) -> !name.equals(".DS_Store"));
                    //listofsub has ads_0 etc which have to be passed
                    Arrays.sort(listOfsub);
                    totalFiles=totalFiles+listOfsub.length;
                    for (int j = 0; j < listOfsub.length; j++) {
						no_files.put(listOfsub[j].getName(), listOfsub[j].listFiles((dir, name) -> !name.equals(".DS_Store")).length);
                    }
                }
            }

            String[] videoFiles = new String[totalFiles];
            String[] audioFiles = new String[totalFiles];


            //get video files
            File[] listOfVideoDirs = videoFolder.listFiles((dir, name) -> !name.equals(".DS_Store"));
            Arrays.sort(listOfVideoDirs);
            for(int i=0; i<listOfVideoDirs.length;i++){
                //videoDirs[i]=ads,sports
                if (listOfVideoDirs[i].isDirectory()) {
                    //list of videos are ads_0 etc
                    File[] listOfVideos = listOfVideoDirs[i].listFiles((dir, name) -> !name.equals(".DS_Store"));
                    Arrays.sort(listOfVideos);
                    dirName=listOfVideoDirs[i].getName();
                    for (int j = 0; j < listOfVideos.length; j++) {
                        videoFiles[c]="/Users/nikithavemulapalli/Desktop/Courses/CSCI 576-Multimedia/Assignments/Project/Data_rgb/"+dirName+"/"+listOfVideos[j].getName();
                        c++;
                    }
                }
            }

            c=0;
            //get audio files
            File[] listOfAudioDirs = audioFolder.listFiles((dir, name) -> !name.equals(".DS_Store"));
            Arrays.sort(listOfAudioDirs);
            for(int i=0; i<listOfAudioDirs.length;i++){
                //audio[i]=ads,sports
                if (listOfAudioDirs[i].isDirectory()) {
                    //list of videos are ads_0.wav etc
                    File[] listOfAudios = listOfAudioDirs[i].listFiles((dir, name) -> !name.equals(".DS_Store"));
                    Arrays.sort(listOfAudios);
                    dirName=listOfAudioDirs[i].getName();
                    for (int j = 0; j < listOfAudios.length; j++) {
                        audioFiles[c]="/Users/nikithavemulapalli/Desktop/Courses/CSCI 576-Multimedia/Assignments/Project/Data_wav/"+dirName+"/"+listOfAudios[j].getName();
                        wav_files.put(audioFiles[c], no_files.get(stripExtension(listOfAudios[j].getName())));
                        c++;
                    }
                }
            }


            fwColor = new FileWriter("colordescriptor.txt");
            bwColor = new BufferedWriter(fwColor);
            fwMotion = new FileWriter("motiondescriptor.txt");
            bwMotion = new BufferedWriter(fwMotion);
            fwAudio = new FileWriter("audiodescriptor.txt");
            bwAudio = new BufferedWriter(fwAudio);


            for(int i=0;i<videoFiles.length;i++) {
                System.out.println(i);

                bwAudio.write("" + i + " ");
                AudioDescriptor ad = new AudioDescriptor();
                String audioTemp = ad.processAudio(audioFiles[i],wav_files.get(audioFiles[i]));
                bwAudio.write(audioTemp);
                bwAudio.write("\n");
                System.out.println("Audio done");

                bwColor.write("" + i + " ");
                ColorDescriptor cd = new ColorDescriptor();
                String colorTemp = cd.processVideo(videoFiles[i]);
                bwColor.write(colorTemp);
                bwColor.write("\n");
                System.out.println("Color done");

                bwMotion.write("" + i + " ");
                MotionDescriptor md = new MotionDescriptor();
                String motionTemp = md.processVideo(videoFiles[i]);
                bwMotion.write(motionTemp);
                System.out.println(motionTemp);
                bwMotion.write("\n");
                System.out.println("Motion done");
            }
            bwAudio.close();
            bwColor.close();
            bwMotion.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
    }
}


