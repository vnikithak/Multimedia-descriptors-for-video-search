import java.util.*;
import java.awt.*;
import java.io.*;

public class RankVideos{

    int numFrames, numVideos;
    int[][] queryMotionDescriptor;
    String[] queryColorDescriptor;
    int[] queryAudioDescriptor;
    String[][] colorDescriptor;
    int[][] audioDescriptor;
    int[][][] motionDescriptor;
    double[][] colorFrameDiff, audioFrameDiff, motionFrameDiff, sumDiff;
    double[] completeVideoDiff;
    double colorWeight, audioWeight, motionWeight;
    int[] sortedIndices;
    int minColorDiff, maxColorDiff, minAudioDiff, maxAudioDiff;//initialize them
    double minMotionDiff, maxMotionDiff, minSumDiff, maxSumDiff;

    

    static String stripExtension (String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;
        return str.substring(0, pos);
    }

    public RankVideos(){
        this.numFrames = 480;
        this.numVideos = 29;
        this.queryMotionDescriptor = new int[this.numFrames][2];
        this.queryColorDescriptor= new String[this.numFrames];
        this.queryAudioDescriptor = new int[this.numFrames];

        this.colorDescriptor= new String[this.numVideos][this.numFrames];
        this.audioDescriptor= new int[this.numVideos][this.numFrames];
        this.motionDescriptor= new int[this.numVideos][this.numFrames][2];

        this.audioFrameDiff= new double[this.numVideos][this.numFrames];
        this.motionFrameDiff= new double[this.numVideos][this.numFrames];
        this.colorFrameDiff= new double[this.numVideos][this.numFrames];
        this.sumDiff = new double[this.numVideos][this.numFrames];
        this.completeVideoDiff = new double[this.numVideos];
        this.sortedIndices = new int[this.numVideos];

        this.colorWeight=0.4;
        this.audioWeight=0.3;
        this.motionWeight=0.3;

        this.minColorDiff = Integer.MAX_VALUE;
        this.minAudioDiff= Integer.MAX_VALUE;
        this.minMotionDiff = Integer.MAX_VALUE;

        this.maxColorDiff = Integer.MIN_VALUE;
        this.maxAudioDiff= Integer.MIN_VALUE;
        this.maxMotionDiff = Integer.MIN_VALUE;
       
    }

    public void getQueryDescriptors(String query){
        //query is ads_0.rgb
        String videoString = "/Users/nikithavemulapalli/Desktop/Courses/CSCI 576-Multimedia/Assignments/Project/query/"+stripExtension(query);
        System.out.println(videoString);
        File videoFolder = new File(videoString);
        String audioFile = videoString+".wav";

        //get audio descriptor for query
        AudioDescriptor ad = new AudioDescriptor();
        String audioTemp = ad.processAudio(audioFile, videoFolder.listFiles().length);
        StringTokenizer queryAd = new StringTokenizer(audioTemp);

        
         //get color descriptor for query
        ColorDescriptor cd = new ColorDescriptor();
        String colorTemp = cd.processVideo(videoString);
        StringTokenizer queryCd = new StringTokenizer(colorTemp) ;

         //get motion descriptor for query
        MotionDescriptor md = new MotionDescriptor();
        String motiontemp = md.processVideo(videoString);
        StringTokenizer queryMd = new StringTokenizer(motiontemp);

        //store query descriptors
        for(int i = 0; i < numFrames; i++){
            queryColorDescriptor[i] = queryCd.nextToken().trim();
            queryAudioDescriptor[i]= Integer.parseInt(queryAd.nextToken());
            if(i==0)
            {
                queryMotionDescriptor[i][0]= 0;
                queryMotionDescriptor[i][1]= 0;
                continue;
            }
            queryMotionDescriptor[i][0] = Integer.parseInt(queryMd.nextToken());
            queryMotionDescriptor[i][1] = Integer.parseInt(queryMd.nextToken());
        }   
    }

    public int[] compareDescriptors(){

        try{
            //read all offline descriptors
            FileInputStream cfstream = new FileInputStream("colordescriptor.txt");
            DataInputStream cin = new DataInputStream(cfstream);
            BufferedReader cbr = new BufferedReader(new InputStreamReader(cin));
            FileInputStream afstream = new FileInputStream("audiodescriptor.txt");
            DataInputStream ain = new DataInputStream(afstream);
            BufferedReader abr = new BufferedReader(new InputStreamReader(ain));
            FileInputStream mfstream = new FileInputStream("motiondescriptor.txt");
            DataInputStream min = new DataInputStream(mfstream);
            BufferedReader mbr = new BufferedReader(new InputStreamReader(min));
            String cline, aline, mline;

            int count = 0;
            while ((cline = cbr.readLine()) != null && (aline = abr.readLine()) != null && (mline = mbr.readLine())!=null && count < this.numVideos)   { 
                StringTokenizer cst = new StringTokenizer(cline);
                StringTokenizer ast = new StringTokenizer(aline);
                StringTokenizer mst = new StringTokenizer(mline);
                int videoIndex = Integer.parseInt(cst.nextToken());
                ast.nextToken();
                mst.nextToken();
                for(int i = 0; i < numFrames; i++){
                    colorDescriptor[videoIndex][i] = cst.nextToken().trim();
                    audioDescriptor[videoIndex][i] = Integer.parseInt(ast.nextToken());
                    if(i==0){
                        motionDescriptor[videoIndex][i][0]= 0;
                        motionDescriptor[videoIndex][i][1]= 0;
                        continue;
                    }
                    motionDescriptor[videoIndex][i][0]= Integer.parseInt(mst.nextToken());
                    motionDescriptor[videoIndex][i][1]= Integer.parseInt(mst.nextToken());
                }    
                count++;
            }
            cin.close();
            ain.close();
            min.close();

            for(int i=0; i<this.numVideos; i++){
                calculateHSVDifference(i);
                calculateAudioDifference(i);
                calculateMotionDifference(i);
            } 

            //normalize values and add them
            for(int i=0; i<this.numVideos; i++){
                for(int j=0;j<this.numFrames;j++){
                    colorFrameDiff[i][j]=(colorFrameDiff[i][j]-minColorDiff)/(maxColorDiff-minColorDiff);
                    motionFrameDiff[i][j]=(motionFrameDiff[i][j]-minMotionDiff)/(maxMotionDiff-minMotionDiff);
                    audioFrameDiff[i][j]=(audioFrameDiff[i][j]-minAudioDiff)/(maxAudioDiff-minAudioDiff);
                }
                colorWeight = (this.numFrames-Arrays.stream(colorFrameDiff[i]).sum())/480;
                motionWeight = (this.numFrames-Arrays.stream(motionFrameDiff[i]).sum())/480;
                audioWeight = (this.numFrames-Arrays.stream(audioFrameDiff[i]).sum())/480;
                for(int k=0;k<this.numFrames;k++){
                    //combined difference
                    sumDiff[i][k]=(colorWeight*colorFrameDiff[i][k])+(audioWeight*audioFrameDiff[i][k])+(motionWeight*motionFrameDiff[i][k]);
                    double z = sumDiff[i][k];
                    if(z<minSumDiff)
                        minSumDiff=z;
                    if(z>maxSumDiff)
                        maxSumDiff=z;
                }
            }

            //normalize sumdiff
            for(int i=0; i<this.numVideos; i++){
                for(int j=0;j<this.numFrames;j++){
                    sumDiff[i][j] = (sumDiff[i][j]-minSumDiff)/(maxSumDiff-minSumDiff);
                }
            }

            for(int i=0;i<this.numVideos;i++){
                completeVideoDiff[i]=0;
                for(int k=0;k<this.numFrames;k++){
                    completeVideoDiff[i]+=sumDiff[i][k];
                }
                completeVideoDiff[i]/=this.numFrames;
            }
            //sort descriptorsum to get respective indices.
            double[] array = new double[]{};
            Map<Double, Integer> map = new TreeMap<Double, Integer>();
            for (int i = 0; i < completeVideoDiff.length; ++i) {
                map.put(completeVideoDiff[i], i);
            }
            Collection<Integer> indices = map.values();
        
            // sortedIndices = indices.toArray();
            String str = Arrays.toString(indices.toArray());

            sortedIndices = Arrays.stream(str.substring(1, str.length()-1).split(","))
    .map(String::trim).mapToInt(Integer::parseInt).toArray();


            return sortedIndices;
        }
        catch(Exception e){
            e.printStackTrace();
            return sortedIndices;
        }

    }

    public double[][] getAudioFramDiff(){
        return audioFrameDiff;
    }

    public double[][] getMotionFrameDiff(){
        return motionFrameDiff;
    }

    public double[][] getColorFrameDiff(){
        return colorFrameDiff;
    }

    public double[][] getSumDiff(){
        return sumDiff;
    }

    public double[] getCompleteVideoDiff(){
        return completeVideoDiff;
    }

    public void calculateHSVDifference(int videoIndex){
        for(int i=0; i< this.numFrames; i++){
            String hsv1 = queryColorDescriptor[i];
            String hsv2= colorDescriptor[videoIndex][i];
            int h1 = Integer.parseInt(hsv1.substring(0, 2));
            int s1 = Integer.parseInt(hsv1.substring(2, 3));
            int v1 = Integer.parseInt(hsv1.substring(3));
            int h2 = Integer.parseInt(hsv2.substring(0, 2));
            int s2 = Integer.parseInt(hsv2.substring(2, 3));
            int v2 = Integer.parseInt(hsv2.substring(3));
            int h = Math.abs(h1 - h2);
            if (h > 8){
                int diff = h - 8;
                h = h - (2 * diff);
            }
            int x = (h * 2) + Math.abs(s1 - s2) + Math.abs(v1 - v2);
            if(x<minColorDiff)
                minColorDiff=x;
            if(x>maxColorDiff)
                maxColorDiff=x;
            colorFrameDiff[videoIndex][i]=x;
        }
    }

    public void calculateAudioDifference(int videoIndex){
        for(int i=0; i< this.numFrames; i++){
            int x = Math.abs(audioDescriptor[videoIndex][i]-queryAudioDescriptor[i]);
            if(x<minAudioDiff)
                minAudioDiff=x;
            if(x>maxAudioDiff)
                maxAudioDiff=x;
            audioFrameDiff[videoIndex][i]=x;
        }
    }

    public void calculateMotionDifference(int videoIndex){
        //motion doesn't have all frames check that
        for(int i=0; i< this.numFrames; i++){
                double x = Math.pow((Math.pow((motionDescriptor[videoIndex][i][0]-queryMotionDescriptor[i][0]),2)+ Math.pow((motionDescriptor[videoIndex][i][1]-queryMotionDescriptor[i][1]),2)),0.5);
                if(x<minMotionDiff)
                    minMotionDiff=x;
                if(x>maxMotionDiff)
                    maxMotionDiff=x;
                motionFrameDiff[videoIndex][i]=x;
        }
    }


}